package com.mutazyounes.prayerathan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mutazyounes.prayerathan.audio.AthanController
import com.mutazyounes.prayerathan.audio.AthkarClip
import com.mutazyounes.prayerathan.audio.AudioSettingsStore
import com.mutazyounes.prayerathan.engine.CityCatalog
import com.mutazyounes.prayerathan.engine.LocationStore
import com.mutazyounes.prayerathan.engine.PrayerDay
import com.mutazyounes.prayerathan.engine.PrayerEngine
import com.mutazyounes.prayerathan.engine.PrayerName
import com.mutazyounes.prayerathan.engine.SavedLocation
import com.mutazyounes.prayerathan.engine.SystemWallClock
import com.mutazyounes.prayerathan.engine.WallClock
import com.mutazyounes.prayerathan.engine.WallTime
import com.mutazyounes.prayerathan.shell.LocationFixer
import com.mutazyounes.prayerathan.weather.WeatherClient
import com.mutazyounes.prayerathan.weather.HourlyWeather
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WallViewModel(
    private val engine: PrayerEngine,
    private val athan: AthanController,
    private val settings: WallSettingsStore,
    private val weather: WeatherClient,
    private val locationStore: LocationStore,
    private val locationFixer: LocationFixer,
    private val audioSettings: AudioSettingsStore,
    private val wallClock: WallClock = SystemWallClock,
) : ViewModel() {

    private val _state = MutableStateFlow(WallUiState.Empty)
    val state: StateFlow<WallUiState> = _state.asStateFlow()

    private var twelveHour: Boolean = true
    private var themeMode: ThemeMode = settings.themeMode()
    private var lastScheduledDate: LocalDate? = null
    private var wasPlaying: Boolean = false
    private var wasAthkar: Boolean = false
    private var weatherLine: String = ""
    private var weatherCondition: String = ""
    private var hourlyWeather: List<HourlyWeather> = emptyList()
    private var locationError: String? = null
    private var resolvedLocation: SavedLocation? = null
    private var lastCellKinds: List<CellKind>? = null

    init {
        if (!locationStore.hasPersistedLocation()) {
            useGps(keepAlbanyIfFailed = true)
        }
        refresh(now())
        viewModelScope.launch(Dispatchers.IO) {
            if (wallClock.syncIfDue()) {
                withContext(Dispatchers.Main) { refresh(now(), forceSchedule = true) }
            }
            while (isActive) {
                delay(CLOCK_SYNC_CHECK_MS)
                if (wallClock.syncIfDue()) {
                    withContext(Dispatchers.Main) { refresh(now(), forceSchedule = true) }
                }
            }
        }
        viewModelScope.launch {
            athan.playback.collect {
                refresh(now())
            }
        }
        viewModelScope.launch {
            athan.athkarPlayback.collect {
                refresh(now())
            }
        }
        viewModelScope.launch {
            athan.demoId.collect {
                refresh(now())
            }
        }
        viewModelScope.launch {
            while (isActive) {
                delay(WallTime.millisUntilNextSecond(now().toEpochMilli()))
                refresh(now())
            }
        }
        viewModelScope.launch {
            while (isActive) {
                pullWeather()
                delay(WEATHER_REFRESH_MS)
            }
        }
    }

    private suspend fun pullWeather() {
        val fetched = withContext(Dispatchers.IO) { weather.fetch() }
        if (fetched != null) {
            weatherLine = fetched.line
            weatherCondition = fetched.weatherCondition
            refresh(now())
        }
        val hourly = withContext(Dispatchers.IO) { weather.fetchHourly() }
        if (hourly != null) {
            hourlyWeather = hourly
            refresh(now())
        }
    }

    fun stopAthan() {
        athan.stop()
        refresh(now())
    }

    fun setThemeMode(mode: ThemeMode) {
        themeMode = mode
        settings.setThemeMode(mode)
        refresh(now())
    }

    fun setAthanSound(id: String) {
        audioSettings.setSoundId(id)
        refresh(now())
    }

    fun setAthkarEnabled(enabled: Boolean) {
        audioSettings.setAthkarEnabled(enabled)
        refresh(now(), forceSchedule = true)
    }

    fun setNightBlackout(enabled: Boolean) {
        settings.setNightBlackout(enabled)
        refresh(now())
    }

    fun togglePrayerMute(prayer: PrayerName) {
        val muted = audioSettings.isPrayerMuted(prayer)
        audioSettings.setPrayerMuted(prayer, !muted)
        refresh(now(), forceSchedule = true)
    }

    fun playAthanDemo(id: String) {
        if (athan.demoId.value == id) {
            stopDemo()
            return
        }
        athan.playAthanDemo(id)
        refresh(now())
    }

    fun playAthkarDemo(clip: AthkarClip) {
        val demo = "athkar:${clip.name}"
        if (athan.demoId.value == demo) {
            stopDemo()
            return
        }
        athan.playAthkarDemo(clip)
        refresh(now())
    }

    fun stopDemo() {
        if (athan.demoId.value == null) return
        athan.stop()
        refresh(now())
    }

    fun saveLocation(
        label: String,
        latitude: Double,
        longitude: Double,
        timeZoneId: String,
    ): Boolean {
        val parsed = SavedLocation.parse(label, latitude, longitude, timeZoneId)
        if (parsed == null) {
            locationError = "That city could not be saved. Try another."
            refresh(now())
            return false
        }
        resolvedLocation = null
        locationStore.write(parsed)
        locationError = null
        refresh(now(), forceSchedule = true)
        viewModelScope.launch { pullWeather() }
        return true
    }

    fun resetToAlbany() {
        resolvedLocation = null
        locationStore.write(SavedLocation.albany)
        locationError = null
        refresh(now(), forceSchedule = true)
        viewModelScope.launch { pullWeather() }
    }

    fun useGps(keepAlbanyIfFailed: Boolean = false) {
        locationFixer.requestOneFix { outcome ->
            when (outcome) {
                LocationFixer.Outcome.Saved -> {
                    resolvedLocation = null
                    locationError = null
                    refresh(now(), forceSchedule = true)
                    viewModelScope.launch { pullWeather() }
                }
                LocationFixer.Outcome.Failed -> {
                    if (keepAlbanyIfFailed && !locationStore.hasPersistedLocation()) {
                        locationStore.write(SavedLocation.albany)
                    }
                    locationError = if (keepAlbanyIfFailed) {
                        null
                    } else {
                        "GPS did not get a fix. Pick a city, or try again."
                    }
                    refresh(now(), forceSchedule = keepAlbanyIfFailed)
                }
            }
        }
    }

    override fun onCleared() {
        locationFixer.cancel()
        super.onCleared()
    }

    private fun now(): Instant = wallClock.now()

    private fun resolveCityLabel(location: SavedLocation): SavedLocation {
        val held = resolvedLocation
        if (held != null &&
            held.latitude == location.latitude &&
            held.longitude == location.longitude &&
            held.label == location.label &&
            !looksLikeCoords(held.label)
        ) {
            return held
        }
        val catalog = CityCatalog.cached() ?: return location.also { resolvedLocation = it }
        val name = catalog.wallLabel(location.latitude, location.longitude, location.label)
        if (name == location.label) {
            resolvedLocation = location
            return location
        }
        val hit = catalog.match(location.latitude, location.longitude, name)
            ?: catalog.nearest(location.latitude, location.longitude)
        val resolved = location.copy(
            label = name,
            timeZoneId = hit?.second?.timeZoneId ?: location.timeZoneId,
        )
        locationStore.write(resolved)
        resolvedLocation = resolved
        return resolved
    }

    private fun refresh(now: Instant, forceSchedule: Boolean = false) {
        val location = resolveCityLabel(engine.location())
        val day = engine.day(now, location)
        val clocks = engine.clocks(now, location)
        val remaining = Duration.between(now, day.nextAthanAt).let {
            if (it.isNegative) Duration.ZERO else it
        }
        maybeSchedule(day, now, forceSchedule)
        val playback = athan.playback.value
        val playingName = playback?.prayer
        val playing = playingName != null
        val athkar = athan.athkarPlayback.value
        val athkarOn = athkar != null && !playing
        val currentThemeMode = settings.themeMode()
        themeMode = currentThemeMode
        val mutedPrayers = audioSettings.mutedPrayers()
        val albanyClock = formatClock(clocks.albany, twelveHour)
        val jordanClock = formatClock(clocks.jordan, twelveHour)
        val previous = _state.value
        val cells = if (
            previous.cells.isNotEmpty() &&
            previous.playingName == playingName &&
            previous.mutedPrayers == mutedPrayers &&
            previous.locationTimeZoneId == location.timeZoneId &&
            cellKindsUnchanged(now, day, playingName, previous) &&
            previous.cells.firstOrNull()?.weatherCondition == weatherConditionFor(day.times.first().at)?.condition.orEmpty()
        ) {
            previous.cells
        } else {
            buildCells(now, day, location.timeZoneId, playingName, mutedPrayers)
        }
        _state.value = previous.copy(
            locationLabel = location.label.uppercase(Locale.ENGLISH),
            locationCity = location.label,
            locationLatitude = formatCoord(location.latitude),
            locationLongitude = formatCoord(location.longitude),
            locationTimeZoneId = location.timeZoneId,
            locationError = locationError,
            gregorianDate = clocks.albany.format(DATE_LINE),
            weekday = clocks.albany.format(WEEKDAY),
            albanyTime = albanyClock.first,
            albanyAmPm = albanyClock.second,
            jordanTime = jordanClock.first,
            jordanAmPm = jordanClock.second,
            nextLabel = if (playing) "NOW" else "NEXT ${day.nextAthan.englishLabel()}",
            countdown = formatCountdown(remaining),
            athanPlaying = playing,
            playingName = playingName,
            athkarPlaying = athkarOn,
            athkarCaption = if (playing) "" else athkar?.caption.orEmpty(),
            cells = cells,
            twelveHour = twelveHour,
            themeMode = currentThemeMode,
            darkTheme = resolveDarkTheme(currentThemeMode, now, day),
            weatherLine = weatherLine,
            weatherCondition = weatherCondition,
            athanSoundId = audioSettings.soundId(),
            athkarEnabled = audioSettings.athkarEnabled(),
            mutedPrayers = mutedPrayers,
            demoId = athan.demoId.value,
            nightBlackoutEnabled = settings.nightBlackout(),
            isNightBlackout = settings.nightBlackout() && isNightBlackoutWindow(clocks.albany.hour) && !playing && athan.demoId.value == null,
        )
    }

    private fun cellKindsUnchanged(
        now: Instant,
        day: PrayerDay,
        playingName: PrayerName?,
        previous: WallUiState,
    ): Boolean {
        if (previous.cells.size != day.times.size) return false
        val kinds = day.times.map { instant ->
            when {
                playingName != null && instant.name == playingName -> CellKind.NEXT
                playingName == null && instant.at == day.nextAthanAt -> CellKind.NEXT
                instant.at <= now -> CellKind.PAST
                else -> CellKind.LATER
            }
        }
        if (kinds == lastCellKinds && previous.cells.map { it.kind } == kinds) return true
        lastCellKinds = kinds
        return false
    }

    private fun maybeSchedule(day: PrayerDay, now: Instant, force: Boolean = false) {
        val playingNow = athan.playback.value != null
        val athkarNow = athan.athkarPlayback.value != null
        if (force || lastScheduledDate != day.localDate ||
            (wasPlaying && !playingNow) ||
            (wasAthkar && !athkarNow)
        ) {
            athan.schedule(day, now)
            lastScheduledDate = day.localDate
        }
        wasPlaying = playingNow
        wasAthkar = athkarNow
    }

    private fun buildCells(
        now: Instant,
        day: PrayerDay,
        timeZoneId: String,
        playingName: PrayerName?,
        mutedPrayers: Set<PrayerName>,
    ): List<PrayerCellState> {
        val zone = ZoneId.of(timeZoneId)
        return day.times.map { instant ->
            val kind = when {
                playingName != null && instant.name == playingName -> CellKind.NEXT
                playingName == null && instant.at == day.nextAthanAt -> CellKind.NEXT
                instant.at <= now -> CellKind.PAST
                else -> CellKind.LATER
            }
            PrayerCellState(
                name = instant.name,
                english = instant.name.englishLabel(),
                time = formatPrayerTime(instant.at.atZone(zone), twelveHour),
                kind = kind,
                muted = instant.name in mutedPrayers,
                weatherCondition = weatherConditionFor(instant.at)?.condition.orEmpty(),
                weatherTempC = weatherConditionFor(instant.at)?.temperatureC,
            )
        }
    }

    private fun weatherConditionFor(at: Instant): HourlyWeather? {
        if (hourlyWeather.isEmpty()) return null
        val target = at.epochSecond
        return hourlyWeather.minByOrNull { Math.abs(it.hourEpochSeconds - target) }
    }

    companion object {
        private val DATE_LINE: DateTimeFormatter =
            DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH)
        private val WEEKDAY: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)

        private fun looksLikeCoords(label: String): Boolean {
            val comma = label.indexOf(',')
            if (comma < 0) return false
            return label.substring(0, comma).trim().toDoubleOrNull() != null &&
                label.substring(comma + 1).trim().toDoubleOrNull() != null
        }
        private const val WEATHER_REFRESH_MS = 3 * 60 * 1000L
        private const val CLOCK_SYNC_CHECK_MS = 60L * 60L * 1000L

        fun factory(
            engine: PrayerEngine,
            athan: AthanController,
            settings: WallSettingsStore,
            weather: WeatherClient,
            locationStore: LocationStore,
            locationFixer: LocationFixer,
            audioSettings: AudioSettingsStore,
            wallClock: WallClock,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WallViewModel(
                        engine,
                        athan,
                        settings,
                        weather,
                        locationStore,
                        locationFixer,
                        audioSettings,
                        wallClock,
                    ) as T
                }
            }

        fun formatCoord(value: Double): String =
            String.format(Locale.US, "%.6f", value).trimEnd('0').trimEnd('.')

        fun resolveDarkTheme(mode: ThemeMode, now: Instant, day: PrayerDay): Boolean {
            return when (mode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.AUTO -> {
                    val sunrise = day.times.firstOrNull { it.name == PrayerName.SUNRISE }?.at
                    val maghrib = day.times.firstOrNull { it.name == PrayerName.MAGHRIB }?.at
                    if (sunrise == null || maghrib == null) return true
                    now < sunrise || now >= maghrib
                }
            }
        }

        fun formatClock(time: ZonedDateTime, twelveHour: Boolean): Pair<String, String> {
            if (!twelveHour) {
                return time.format(DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH)) to ""
            }
            val hour12 = time.hour % 12
            val displayHour = if (hour12 == 0) 12 else hour12
            val minute = time.minute.toString().padStart(2, '0')
            val amPm = if (time.hour < 12) "AM" else "PM"
            return "$displayHour:$minute" to amPm
        }

        fun formatPrayerTime(time: ZonedDateTime, twelveHour: Boolean): String =
            formatClock(time, twelveHour).first

        fun formatCountdown(duration: Duration): String {
            val totalSeconds = duration.seconds.coerceAtLeast(0)
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        }

        fun isNightBlackoutWindow(localHour: Int): Boolean =
            localHour >= 23 || localHour < 4
    }
}
