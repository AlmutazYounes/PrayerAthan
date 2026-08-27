package com.mutazyounes.prayerathan.audio

import android.content.Context
import com.mutazyounes.prayerathan.PrayerAthanApp
import com.mutazyounes.prayerathan.engine.PrayerDay
import com.mutazyounes.prayerathan.engine.PrayerName
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AthanPlayback(
    val prayer: PrayerName,
    val startedAt: Instant,
)

data class AthkarPlayback(
    val caption: String,
    val startedAt: Instant,
)

interface AthanController {
    fun schedule(day: PrayerDay, now: Instant)
    fun stop()
    fun playAthanDemo(soundId: String)
    fun playAthkarDemo(clip: AthkarClip)
    val playback: StateFlow<AthanPlayback?>
    val athkarPlayback: StateFlow<AthkarPlayback?>
    val demoId: StateFlow<String?>
}

class DefaultAthanController(
    context: Context,
) : AthanController {

    private val appContext = context.applicationContext
    private val scheduler = AthanScheduler(appContext)
    private val athkarScheduler = AthkarScheduler(appContext)
    private val audioSettings = AudioSettingsStore(appContext)
    private val _playback = MutableStateFlow<AthanPlayback?>(null)
    private val _athkarPlayback = MutableStateFlow<AthkarPlayback?>(null)
    private val _demoId = MutableStateFlow<String?>(null)

    override val playback: StateFlow<AthanPlayback?> = _playback.asStateFlow()
    override val athkarPlayback: StateFlow<AthkarPlayback?> = _athkarPlayback.asStateFlow()
    override val demoId: StateFlow<String?> = _demoId.asStateFlow()

    override fun schedule(day: PrayerDay, now: Instant) {
        scheduler.schedule(day, now)
        if (audioSettings.athkarEnabled()) {
            athkarScheduler.schedule(day, now, zone())
        } else {
            athkarScheduler.cancelAll()
        }
    }

    override fun stop() {
        val athanOn = _playback.value != null || _demoId.value != null
        val athkarOn = _athkarPlayback.value != null
        markIdle()
        markAthkarIdle()
        markDemo(null)
        if (athanOn) {
            try {
                appContext.startService(AthanService.stopIntent(appContext))
            } catch (_: IllegalStateException) {
            }
        }
        if (athkarOn) {
            try {
                appContext.startService(AthkarService.stopIntent(appContext))
            } catch (_: IllegalStateException) {
            }
        }
    }

    override fun playAthanDemo(soundId: String) {
        stopAthkar()
        markIdle()
        markDemo(soundId)
        appContext.startForegroundService(AthanService.demoIntent(appContext, soundId))
    }

    override fun playAthkarDemo(clip: AthkarClip) {
        try {
            appContext.startService(AthanService.stopIntent(appContext))
        } catch (_: IllegalStateException) {
        }
        markIdle()
        markDemo(AthkarService.demoId(clip))
        appContext.startForegroundService(AthkarService.demoIntent(appContext, clip))
    }

    fun stopAthkar() {
        val athkarOn = _athkarPlayback.value != null
        markAthkarIdle()
        if (!athkarOn) return
        try {
            appContext.startService(AthkarService.stopIntent(appContext))
        } catch (_: IllegalStateException) {
        }
    }

    fun markPlaying(prayer: PrayerName, startedAt: Instant) {
        if (prayer == PrayerName.SUNRISE) return
        _playback.value = AthanPlayback(prayer, startedAt)
    }

    fun markIdle() {
        _playback.value = null
    }

    fun markAthkarPlaying(caption: String, startedAt: Instant) {
        _athkarPlayback.value = AthkarPlayback(caption, startedAt)
    }

    fun markAthkarIdle() {
        _athkarPlayback.value = null
    }

    fun markDemo(id: String?) {
        _demoId.value = id
    }

    private fun zone(): ZoneId {
        val app = appContext as PrayerAthanApp
        return ZoneId.of(app.prayerEngine.location().timeZoneId)
    }
}
