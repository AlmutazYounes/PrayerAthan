package com.mutazyounes.prayerathan.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.mutazyounes.prayerathan.audio.AthanCatalog
import com.mutazyounes.prayerathan.audio.AthanSoundChoice
import com.mutazyounes.prayerathan.audio.AthanVolume
import com.mutazyounes.prayerathan.audio.MedicineSlot
import com.mutazyounes.prayerathan.audio.MedicineVoice
import com.mutazyounes.prayerathan.engine.CityCatalog
import com.mutazyounes.prayerathan.engine.PlaceCity
import com.mutazyounes.prayerathan.engine.PlaceCountry
import com.mutazyounes.prayerathan.engine.PrayerMethod
import com.mutazyounes.prayerathan.engine.PrayerName
import com.mutazyounes.prayerathan.engine.PrayerOffsets
import java.time.DayOfWeek
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
private val CardShape = RoundedCornerShape(16.dp)
private val InnerCardShape = RoundedCornerShape(12.dp)
private val ChipShape = RoundedCornerShape(10.dp)
private val MenuShape = RoundedCornerShape(14.dp)
private val FieldShape = RoundedCornerShape(12.dp)
private const val CITY_QUERY_MIN = 2
private const val CITY_RESULT_CAP = 50
private const val COUNTRY_RESULT_CAP = 60

private enum class PlaceMenu { Country, City }

@Composable
fun SettingsSheet(
    cityLabel: String,
    latitude: String,
    longitude: String,
    locationError: String?,
    athanSoundId: String,
    athkarEnabled: Boolean,
    mutedPrayers: Set<PrayerName>,
    prayerVolumes: Map<PrayerName, Int>,
    prayerOffsets: Map<PrayerName, Int>,
    medicineEnabled: Boolean,
    medicineVoice: String,
    medicineSlots: List<MedicineSlot>,
    nightBlackoutEnabled: Boolean,
    demoId: String?,
    onSelectLocation: (String, Double, Double, String) -> Unit,
    onUseGps: () -> Unit,
    onSelectAthanSound: (String) -> Unit,
    onAthkarEnabledChange: (Boolean) -> Unit,
    onTogglePrayerMute: (PrayerName) -> Unit,
    onPrayerVolumeChange: (PrayerName, Int) -> Unit,
    onPrayerOffsetChange: (PrayerName, Int) -> Unit,
    onPlayPrayerVolumePreview: (PrayerName, Int) -> Unit,
    onMedicineEnabledChange: (Boolean) -> Unit,
    onMedicineVoiceChange: (MedicineVoice) -> Unit,
    onUpsertMedicineSlot: (MedicineSlot) -> Unit,
    onRemoveMedicineSlot: (String) -> Unit,
    onPlayMedicineDemo: () -> Unit,
    onNightBlackoutChange: (Boolean) -> Unit,
    onPlayAthanDemo: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = LocalWallPalette.current
    val context = LocalContext.current
    var catalog by remember { mutableStateOf(CityCatalog.cached()) }
    var selectedCountry by remember { mutableStateOf<PlaceCountry?>(null) }
    var selectedCity by remember { mutableStateOf<PlaceCity?>(null) }
    var openMenu by remember { mutableStateOf<PlaceMenu?>(null) }
    var countryQuery by remember { mutableStateOf("") }
    var cityQuery by remember { mutableStateOf("") }
    var cityQueryLive by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val ready = CityCatalog.cached()
        if (ready != null) {
            catalog = ready
            return@LaunchedEffect
        }
        val assets = context.assets
        catalog = withContext(Dispatchers.IO) {
            CityCatalog.loadBundled { name ->
                assets.open(name).bufferedReader().use { it.readText() }
            }
        }
    }

    LaunchedEffect(catalog, cityLabel, latitude, longitude) {
        val places = catalog ?: return@LaunchedEffect
        val hit = withContext(Dispatchers.Default) {
            places.match(latitude.toDoubleOrNull(), longitude.toDoubleOrNull(), cityLabel)
        }
        selectedCountry = hit?.first
        selectedCity = hit?.second
        countryQuery = ""
        cityQuery = ""
        cityQueryLive = ""
        openMenu = null
    }

    val placesReady = catalog != null
    val countryChoices by remember {
        derivedStateOf {
            catalog?.searchCountries(countryQuery, limit = COUNTRY_RESULT_CAP).orEmpty()
        }
    }

    LaunchedEffect(cityQueryLive) {
        delay(90)
        cityQuery = cityQueryLive
    }

    val cityChoices by remember {
        derivedStateOf {
            val places = catalog
            val code = selectedCountry?.code
            val q = cityQuery.trim()
            if (places == null || code == null || q.length < CITY_QUERY_MIN) {
                emptyList()
            } else {
                places.searchCities(code, q, limit = CITY_RESULT_CAP)
            }
        }
    }

    val cityValue = selectedCity?.let { city ->
        val country = selectedCountry
        if (country != null) city.label(country.name) else city.name
    }.orEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )

        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            val landscape = maxWidth > maxHeight
            val sheetMax = maxHeight * if (landscape) 0.94f else 0.88f

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = sheetMax)
                    .clip(SheetShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                palette.settingsPanel,
                                palette.backgroundDeep,
                            ),
                        ),
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                palette.gold.copy(alpha = 0.35f),
                                Color.Transparent,
                            ),
                        ),
                        shape = SheetShape,
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    )
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                // Drag handle pill
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 12.dp)
                        .size(width = 44.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(palette.gold.copy(alpha = 0.30f)),
                )

                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SETTINGS",
                            style = labelStyle(16.sp, palette.gold),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.16.em,
                        )
                        Text(
                            text = "Wall clock and athan preferences",
                            color = palette.prayerPast.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontFamily = EnglishFontFamily,
                        )
                    }

                    // Close round button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(palette.gold.copy(alpha = 0.12f))
                            .border(1.dp, palette.gold.copy(alpha = 0.25f), CircleShape)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = palette.gold,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    palette.gold.copy(alpha = 0.45f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )

                Spacer(Modifier.height(16.dp))

                if (landscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            LocationSectionCard(
                                placesReady = placesReady,
                                selectedCountry = selectedCountry,
                                selectedCity = selectedCity,
                                cityValue = cityValue,
                                countryQuery = countryQuery,
                                cityQuery = cityQueryLive,
                                countryChoices = countryChoices,
                                cityChoices = cityChoices,
                                openMenu = openMenu,
                                locationError = locationError,
                                onCountryQuery = { countryQuery = it },
                                onCityQuery = { cityQueryLive = it },
                                onOpenMenu = { openMenu = it },
                                onCountry = { country ->
                                    selectedCountry = country
                                    selectedCity = null
                                    cityQuery = ""
                                    cityQueryLive = ""
                                    countryQuery = ""
                                    openMenu = PlaceMenu.City
                                },
                                onCity = { city ->
                                    val country = selectedCountry
                                    if (country != null) {
                                        selectedCity = city
                                        openMenu = null
                                        onSelectLocation(
                                            city.label(country.name),
                                            city.latitude,
                                            city.longitude,
                                            city.timeZoneId,
                                        )
                                    }
                                },
                                onUseGps = onUseGps,
                            )
                            TimeAdjustmentsCard(
                                offsets = prayerOffsets,
                                onOffsetChange = onPrayerOffsetChange,
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            PrayerAthansCard(
                                mutedPrayers = mutedPrayers,
                                prayerVolumes = prayerVolumes,
                                demoId = demoId,
                                onTogglePrayerMute = onTogglePrayerMute,
                                onPrayerVolumeChange = onPrayerVolumeChange,
                                onPlayPrayerVolumePreview = onPlayPrayerVolumePreview,
                            )
                            AthanSoundCard(
                                athanSoundId = athanSoundId,
                                demoId = demoId,
                                onSelectAthanSound = onSelectAthanSound,
                                onPlayAthanDemo = onPlayAthanDemo,
                            )
                            AthkarCard(
                                athkarEnabled = athkarEnabled,
                                onAthkarEnabledChange = onAthkarEnabledChange,
                            )
                            MedicineCard(
                                enabled = medicineEnabled,
                                voice = MedicineVoice.fromStored(medicineVoice),
                                slots = medicineSlots,
                                demoId = demoId,
                                onEnabledChange = onMedicineEnabledChange,
                                onVoiceChange = onMedicineVoiceChange,
                                onUpsertSlot = onUpsertMedicineSlot,
                                onRemoveSlot = onRemoveMedicineSlot,
                                onPlayDemo = onPlayMedicineDemo,
                            )
                            NightBlackoutCard(
                                nightBlackoutEnabled = nightBlackoutEnabled,
                                onNightBlackoutChange = onNightBlackoutChange,
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        LocationSectionCard(
                            placesReady = placesReady,
                            selectedCountry = selectedCountry,
                            selectedCity = selectedCity,
                            cityValue = cityValue,
                            countryQuery = countryQuery,
                            cityQuery = cityQueryLive,
                            countryChoices = countryChoices,
                            cityChoices = cityChoices,
                            openMenu = openMenu,
                            locationError = locationError,
                            onCountryQuery = { countryQuery = it },
                            onCityQuery = { cityQueryLive = it },
                            onOpenMenu = { openMenu = it },
                            onCountry = { country ->
                                selectedCountry = country
                                selectedCity = null
                                cityQuery = ""
                                cityQueryLive = ""
                                countryQuery = ""
                                openMenu = PlaceMenu.City
                            },
                            onCity = { city ->
                                val country = selectedCountry
                                if (country != null) {
                                    selectedCity = city
                                    openMenu = null
                                    onSelectLocation(
                                        city.label(country.name),
                                        city.latitude,
                                        city.longitude,
                                        city.timeZoneId,
                                    )
                                }
                            },
                            onUseGps = onUseGps,
                        )

                        TimeAdjustmentsCard(
                            offsets = prayerOffsets,
                            onOffsetChange = onPrayerOffsetChange,
                        )

                        PrayerAthansCard(
                            mutedPrayers = mutedPrayers,
                            prayerVolumes = prayerVolumes,
                            demoId = demoId,
                            onTogglePrayerMute = onTogglePrayerMute,
                            onPrayerVolumeChange = onPrayerVolumeChange,
                            onPlayPrayerVolumePreview = onPlayPrayerVolumePreview,
                        )

                        AthanSoundCard(
                            athanSoundId = athanSoundId,
                            demoId = demoId,
                            onSelectAthanSound = onSelectAthanSound,
                            onPlayAthanDemo = onPlayAthanDemo,
                        )

                        AthkarCard(
                            athkarEnabled = athkarEnabled,
                            onAthkarEnabledChange = onAthkarEnabledChange,
                        )

                        MedicineCard(
                            enabled = medicineEnabled,
                            voice = MedicineVoice.fromStored(medicineVoice),
                            slots = medicineSlots,
                            demoId = demoId,
                            onEnabledChange = onMedicineEnabledChange,
                            onVoiceChange = onMedicineVoiceChange,
                            onUpsertSlot = onUpsertMedicineSlot,
                            onRemoveSlot = onRemoveMedicineSlot,
                            onPlayDemo = onPlayMedicineDemo,
                        )

                        NightBlackoutCard(
                            nightBlackoutEnabled = nightBlackoutEnabled,
                            onNightBlackoutChange = onNightBlackoutChange,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernCardContainer(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    badge: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val palette = LocalWallPalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(palette.backgroundDeep.copy(alpha = 0.65f))
            .border(
                width = 1.dp,
                color = palette.hairline.copy(alpha = 0.35f),
                shape = CardShape,
            )
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.gold.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = palette.gold,
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    style = labelStyle(13.sp, palette.gold),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.10.em,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = palette.prayerPast,
                        fontSize = 12.sp,
                        fontFamily = EnglishFontFamily,
                    )
                }
            }

            if (!badge.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.gold.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = badge,
                        color = palette.gold,
                        fontSize = 11.sp,
                        fontFamily = EnglishFontFamily,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun LocationSectionCard(
    placesReady: Boolean,
    selectedCountry: PlaceCountry?,
    selectedCity: PlaceCity?,
    cityValue: String,
    countryQuery: String,
    cityQuery: String,
    countryChoices: List<PlaceCountry>,
    cityChoices: List<PlaceCity>,
    openMenu: PlaceMenu?,
    locationError: String?,
    onCountryQuery: (String) -> Unit,
    onCityQuery: (String) -> Unit,
    onOpenMenu: (PlaceMenu?) -> Unit,
    onCountry: (PlaceCountry) -> Unit,
    onCity: (PlaceCity) -> Unit,
    onUseGps: () -> Unit,
) {
    val palette = LocalWallPalette.current
    ModernCardContainer(
        title = "Location",
        icon = Icons.Default.LocationOn,
        subtitle = "Search city or acquire GPS",
    ) {
        SearchSelectField(
            label = "Country",
            value = selectedCountry?.name.orEmpty(),
            placeholder = if (!placesReady) "Loading countries…" else "Choose country…",
            emptyHint = "No matching countries",
            query = countryQuery,
            onQueryChange = onCountryQuery,
            expanded = openMenu == PlaceMenu.Country,
            enabled = placesReady,
            options = countryChoices,
            selected = selectedCountry,
            optionText = { it.name },
            optionKey = { it.code },
            onToggle = {
                onOpenMenu(if (openMenu == PlaceMenu.Country) null else PlaceMenu.Country)
                onCountryQuery("")
            },
            onDismiss = { onOpenMenu(null) },
            onSelect = onCountry,
        )

        Spacer(Modifier.height(10.dp))

        SearchSelectField(
            label = "City",
            value = cityValue,
            placeholder = when {
                !placesReady -> "Loading cities…"
                selectedCountry == null -> "Select country first"
                else -> "Type at least 2 characters…"
            },
            emptyHint = if (cityQuery.trim().length < CITY_QUERY_MIN) {
                "Type at least 2 characters"
            } else {
                "No matching cities found"
            },
            query = cityQuery,
            onQueryChange = onCityQuery,
            expanded = openMenu == PlaceMenu.City,
            enabled = placesReady && selectedCountry != null,
            options = cityChoices,
            selected = selectedCity,
            optionText = { it.rowText() },
            optionKey = { "${it.name}|${it.admin1}|${it.latitude}|${it.longitude}" },
            onToggle = {
                if (selectedCountry == null) return@SearchSelectField
                onOpenMenu(if (openMenu == PlaceMenu.City) null else PlaceMenu.City)
                onCityQuery("")
            },
            onDismiss = { onOpenMenu(null) },
            onSelect = onCity,
        )

        if (!locationError.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .clip(InnerCardShape)
                    .background(palette.gold.copy(alpha = 0.12f))
                    .border(1.dp, palette.gold.copy(alpha = 0.45f), InnerCardShape)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = locationError,
                    color = palette.gold,
                    fontSize = 12.sp,
                    fontFamily = EnglishFontFamily,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        ModernActionButton(
            title = "Use GPS",
            icon = Icons.Default.LocationOn,
            onClick = onUseGps,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(10.dp))
        Text(
            text = PrayerMethod.settingsLine(),
            color = palette.prayerPast.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontFamily = EnglishFontFamily,
        )
    }
}

@Composable
private fun ModernActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalWallPalette.current
    Row(
        modifier = modifier
            .clip(InnerCardShape)
            .background(palette.gold.copy(alpha = 0.08f))
            .border(1.dp, palette.hairline.copy(alpha = 0.45f), InnerCardShape)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.gold,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = title,
            style = labelStyle(12.sp, palette.gold),
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun TimeAdjustmentsCard(
    offsets: Map<PrayerName, Int>,
    onOffsetChange: (PrayerName, Int) -> Unit,
) {
    ModernCardContainer(
        title = "Time adjustments",
        icon = Icons.Default.Refresh,
        subtitle = "Minutes on top of ISNA. They stay when times drift.",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrayerOffsets.ALL.forEach { prayer ->
                TimeOffsetRow(
                    prayer = prayer,
                    minutes = offsets[prayer] ?: 0,
                    onChange = { onOffsetChange(prayer, it) },
                )
            }
        }
    }
}

@Composable
private fun TimeOffsetRow(
    prayer: PrayerName,
    minutes: Int,
    onChange: (Int) -> Unit,
) {
    val palette = LocalWallPalette.current
    val label = formatOffsetMinutes(minutes)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = prayer.englishLabel(),
            color = palette.clock,
            fontSize = 11.sp,
            fontFamily = EnglishFontFamily,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        OffsetStepButton(
            label = "−",
            enabled = minutes > PrayerOffsets.MIN,
            onClick = { onChange(minutes - 1) },
        )
        Text(
            text = label,
            color = palette.gold,
            fontSize = 12.sp,
            fontFamily = EnglishFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(72.dp),
        )
        OffsetStepButton(
            label = "+",
            enabled = minutes < PrayerOffsets.MAX,
            onClick = { onChange(minutes + 1) },
        )
    }
}

@Composable
private fun OffsetStepButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val palette = LocalWallPalette.current
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(palette.gold.copy(alpha = if (enabled) 0.15f else 0.06f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = palette.gold.copy(alpha = if (enabled) 1f else 0.35f),
            fontSize = 16.sp,
            fontFamily = EnglishFontFamily,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun formatOffsetMinutes(minutes: Int): String {
    if (minutes == 0) return "0 min"
    if (minutes > 0) return "+$minutes min"
    return "$minutes min"
}

@Composable
private fun PrayerAthansCard(
    mutedPrayers: Set<PrayerName>,
    prayerVolumes: Map<PrayerName, Int>,
    demoId: String?,
    onTogglePrayerMute: (PrayerName) -> Unit,
    onPrayerVolumeChange: (PrayerName, Int) -> Unit,
    onPlayPrayerVolumePreview: (PrayerName, Int) -> Unit,
) {
    ModernCardContainer(
        title = "Prayer Athans",
        icon = Icons.Default.Check,
        subtitle = "Mute with the chips. Volume per prayer.",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PrayerName.athanTargets().forEach { prayer ->
                val active = prayer !in mutedPrayers
                PrayerToggleChip(
                    title = prayer.englishLabel(),
                    active = active,
                    onClick = { onTogglePrayerMute(prayer) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrayerName.athanTargets().forEach { prayer ->
                PrayerVolumeRow(
                    prayer = prayer,
                    volume = prayerVolumes[prayer] ?: AthanVolume.DEFAULT,
                    muted = prayer in mutedPrayers,
                    playing = demoId == AthanVolume.demoKey(prayer),
                    onVolumeChange = { onPrayerVolumeChange(prayer, it) },
                    onPlay = { percent -> onPlayPrayerVolumePreview(prayer, percent) },
                )
            }
        }
    }
}

@Composable
private fun PrayerVolumeRow(
    prayer: PrayerName,
    volume: Int,
    muted: Boolean,
    playing: Boolean,
    onVolumeChange: (Int) -> Unit,
    onPlay: (Int) -> Unit,
) {
    val palette = LocalWallPalette.current
    var sliding by remember { mutableFloatStateOf(volume.toFloat()) }
    LaunchedEffect(volume) {
        sliding = volume.toFloat()
    }
    val percent = sliding.roundToInt()
    val labelColor = if (muted) palette.prayerPast.copy(alpha = 0.65f) else palette.clock
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = prayer.englishLabel(),
            color = labelColor,
            fontSize = 11.sp,
            fontFamily = EnglishFontFamily,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.width(72.dp),
        )
        GoldVolumeSlider(
            value = sliding,
            onValueChange = { sliding = it },
            onValueChangeFinished = { onVolumeChange(sliding.roundToInt()) },
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "$percent",
            color = if (muted) palette.prayerPast.copy(alpha = 0.65f) else palette.gold,
            fontSize = 11.sp,
            fontFamily = EnglishFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(28.dp),
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (playing) palette.gold else palette.gold.copy(alpha = 0.15f))
                .clickable { onPlay(percent) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (playing) Icons.Default.Close else Icons.Default.PlayArrow,
                contentDescription = if (playing) "Stop preview" else "Preview volume",
                tint = if (playing) palette.settingsPanel else palette.gold,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun GoldVolumeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalWallPalette.current
    val trackColor = palette.hairline.copy(alpha = 0.35f)
    val fillColor = palette.gold
    Canvas(
        modifier = modifier
            .height(28.dp)
            .fillMaxWidth()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    fun at(x: Float): Float {
                        val fraction = (x / size.width.toFloat()).coerceIn(0f, 1f)
                        return fraction * AthanVolume.MAX.toFloat()
                    }
                    onValueChange(at(down.position.x))
                    drag(down.id) { change ->
                        change.consume()
                        onValueChange(at(change.position.x))
                    }
                    onValueChangeFinished()
                }
            },
    ) {
        val trackY = size.height / 2f
        val thumbRadius = 7.dp.toPx()
        val usable = (size.width - thumbRadius * 2f).coerceAtLeast(1f)
        val fraction = (value / AthanVolume.MAX.toFloat()).coerceIn(0f, 1f)
        val cx = thumbRadius + fraction * usable
        val stroke = 3.dp.toPx()
        drawLine(
            color = trackColor,
            start = Offset(thumbRadius, trackY),
            end = Offset(size.width - thumbRadius, trackY),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = fillColor,
            start = Offset(thumbRadius, trackY),
            end = Offset(cx, trackY),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = fillColor,
            radius = thumbRadius,
            center = Offset(cx, trackY),
        )
    }
}

@Composable
private fun PrayerToggleChip(
    title: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalWallPalette.current
    val bgAnim by animateColorAsState(
        targetValue = if (active) palette.gold.copy(alpha = 0.20f) else Color.Transparent,
        animationSpec = tween(150),
        label = "bgAnim",
    )
    val borderAnim by animateColorAsState(
        targetValue = if (active) palette.gold else palette.hairline.copy(alpha = 0.35f),
        animationSpec = tween(150),
        label = "borderAnim",
    )
    val textAnim by animateColorAsState(
        targetValue = if (active) palette.gold else palette.prayerPast.copy(alpha = 0.65f),
        animationSpec = tween(150),
        label = "textAnim",
    )

    Column(
        modifier = modifier
            .clip(ChipShape)
            .background(bgAnim)
            .border(1.dp, borderAnim, ChipShape)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            color = textAnim,
            fontSize = 11.sp,
            fontFamily = EnglishFontFamily,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (active) palette.gold else palette.prayerPast.copy(alpha = 0.35f)),
        )
    }
}

@Composable
private fun AthanSoundCard(
    athanSoundId: String,
    demoId: String?,
    onSelectAthanSound: (String) -> Unit,
    onPlayAthanDemo: (String) -> Unit,
) {
    val current = AthanCatalog.choice(athanSoundId)
    ModernCardContainer(
        title = "Athan Sound",
        icon = Icons.Default.PlayArrow,
        subtitle = "Audio recitation on prayer start",
        badge = current.title,
    ) {
        AthanCatalog.all.forEach { choice ->
            ModernSoundItem(
                choice = choice,
                selected = choice.id == athanSoundId,
                playing = demoId == choice.id,
                onSelect = { onSelectAthanSound(choice.id) },
                onPlay = { onPlayAthanDemo(choice.id) },
            )
        }
    }
}

@Composable
private fun ModernSoundItem(
    choice: AthanSoundChoice,
    selected: Boolean,
    playing: Boolean,
    onSelect: () -> Unit,
    onPlay: () -> Unit,
) {
    val palette = LocalWallPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(InnerCardShape)
            .background(if (selected) palette.gold.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) palette.gold.copy(alpha = 0.55f) else palette.hairline.copy(alpha = 0.25f),
                shape = InnerCardShape,
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(
                    width = 1.5.dp,
                    color = if (selected) palette.gold else palette.prayerPast.copy(alpha = 0.5f),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(palette.gold),
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = choice.title,
                color = if (selected) palette.gold else palette.clock,
                fontSize = 14.sp,
                fontFamily = EnglishFontFamily,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
            Text(
                text = choice.subtitle,
                color = palette.prayerPast,
                fontSize = 11.sp,
                fontFamily = EnglishFontFamily,
            )
        }

        // Play/Stop Button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (playing) palette.gold else palette.gold.copy(alpha = 0.15f))
                .clickable(onClick = onPlay)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (playing) Icons.Default.Close else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = if (playing) palette.settingsPanel else palette.gold,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (playing) "STOP" else "PLAY",
                style = labelStyle(
                    11.sp,
                    color = if (playing) palette.settingsPanel else palette.gold,
                ),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun AthkarCard(
    athkarEnabled: Boolean,
    onAthkarEnabledChange: (Boolean) -> Unit,
) {
    ModernCardContainer(
        title = "Hourly Athkar",
        icon = Icons.Default.PlayArrow,
        subtitle = "8 AM – 10 PM · Short salawat on the hour",
    ) {
        SegmentedToggle(
            active = athkarEnabled,
            onActiveChange = onAthkarEnabledChange,
            onLabel = "Active",
            offLabel = "Silent",
        )
    }
}

@Composable
private fun MedicineCard(
    enabled: Boolean,
    voice: MedicineVoice,
    slots: List<MedicineSlot>,
    demoId: String?,
    onEnabledChange: (Boolean) -> Unit,
    onVoiceChange: (MedicineVoice) -> Unit,
    onUpsertSlot: (MedicineSlot) -> Unit,
    onRemoveSlot: (String) -> Unit,
    onPlayDemo: () -> Unit,
) {
    val palette = LocalWallPalette.current
    ModernCardContainer(
        title = "Medicine Reminder",
        icon = Icons.Default.PlayArrow,
        subtitle = "Spoken cue on the days and hours you pick",
        badge = if (enabled && slots.isNotEmpty()) "${slots.size} slot${if (slots.size == 1) "" else "s"}" else null,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SegmentedToggle(
                active = enabled,
                onActiveChange = onEnabledChange,
                onLabel = "On",
                offLabel = "Off",
            )

            AnimatedVisibility(
                visible = enabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "VOICE",
                        style = labelStyle(10.sp, palette.prayerPast),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MedicineVoice.entries.forEach { option ->
                            PrayerToggleChip(
                                title = when (option) {
                                    MedicineVoice.ARABIC -> "Arabic"
                                    MedicineVoice.ENGLISH -> "English"
                                },
                                active = voice == option,
                                onClick = { onVoiceChange(option) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(InnerCardShape)
                            .background(palette.gold.copy(alpha = 0.10f))
                            .border(1.dp, palette.gold.copy(alpha = 0.40f), InnerCardShape)
                            .clickable(onClick = onPlayDemo)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (demoId == "medicine:preview") "Playing preview…" else "Preview voice",
                                color = palette.clock,
                                fontSize = 14.sp,
                                fontFamily = EnglishFontFamily,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = voice.wallPrimary,
                                color = palette.gold,
                                fontSize = 13.sp,
                                fontFamily = if (voice == MedicineVoice.ARABIC) ArabicFontFamily else EnglishFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Preview medicine reminder",
                            tint = palette.gold,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    slots.forEach { slot ->
                        MedicineSlotEditor(
                            slot = slot,
                            onChange = onUpsertSlot,
                            onRemove = { onRemoveSlot(slot.id) },
                        )
                    }

                    if (slots.size < MedicineSlot.MAX_SLOTS) {
                        Text(
                            text = "+ Add time",
                            color = palette.gold,
                            fontSize = 13.sp,
                            fontFamily = EnglishFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(InnerCardShape)
                                .border(1.dp, palette.gold.copy(alpha = 0.45f), InnerCardShape)
                                .clickable {
                                    onUpsertSlot(MedicineSlot.create())
                                }
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                        )
                    }

                    if (slots.isEmpty()) {
                        Text(
                            text = "Add at least one time so the reminder can fire.",
                            color = palette.prayerPast.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontFamily = EnglishFontFamily,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicineSlotEditor(
    slot: MedicineSlot,
    onChange: (MedicineSlot) -> Unit,
    onRemove: () -> Unit,
) {
    val palette = LocalWallPalette.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(InnerCardShape)
            .background(palette.settingsPanel.copy(alpha = 0.55f))
            .border(1.dp, palette.hairline.copy(alpha = 0.30f), InnerCardShape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatMedicineClock(slot.hour, slot.minute),
                color = palette.clock,
                fontSize = 20.sp,
                fontFamily = EnglishFontFamily,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Remove",
                color = palette.prayerPast,
                fontSize = 12.sp,
                fontFamily = EnglishFontFamily,
                modifier = Modifier
                    .clip(ChipShape)
                    .clickable(onClick = onRemove)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MedicineStepChip(
                label = "−1h",
                onClick = {
                    onChange(slot.copy(hour = (slot.hour + 23) % 24))
                },
                modifier = Modifier.weight(1f),
            )
            MedicineStepChip(
                label = "+1h",
                onClick = {
                    onChange(slot.copy(hour = (slot.hour + 1) % 24))
                },
                modifier = Modifier.weight(1f),
            )
            MedicineStepChip(
                label = "−15m",
                onClick = {
                    val total = (slot.hour * 60 + slot.minute - 15 + 24 * 60) % (24 * 60)
                    onChange(slot.copy(hour = total / 60, minute = total % 60))
                },
                modifier = Modifier.weight(1f),
            )
            MedicineStepChip(
                label = "+15m",
                onClick = {
                    val total = (slot.hour * 60 + slot.minute + 15) % (24 * 60)
                    onChange(slot.copy(hour = total / 60, minute = total % 60))
                },
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MEDICINE_DAY_ORDER.forEach { day ->
                val active = day in slot.days
                PrayerToggleChip(
                    title = dayShort(day),
                    active = active,
                    onClick = {
                        val next = if (active) slot.days - day else slot.days + day
                        if (next.isNotEmpty()) onChange(slot.copy(days = next))
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MedicineStepChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalWallPalette.current
    Text(
        text = label,
        color = palette.gold,
        fontSize = 12.sp,
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(ChipShape)
            .border(1.dp, palette.gold.copy(alpha = 0.40f), ChipShape)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    )
}

private val MEDICINE_DAY_ORDER = listOf(
    DayOfWeek.SUNDAY,
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
)

private fun dayShort(day: DayOfWeek): String = when (day) {
    DayOfWeek.SUNDAY -> "Su"
    DayOfWeek.MONDAY -> "Mo"
    DayOfWeek.TUESDAY -> "Tu"
    DayOfWeek.WEDNESDAY -> "We"
    DayOfWeek.THURSDAY -> "Th"
    DayOfWeek.FRIDAY -> "Fr"
    DayOfWeek.SATURDAY -> "Sa"
}

private fun formatMedicineClock(hour: Int, minute: Int): String {
    val h12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val amPm = if (hour < 12) "AM" else "PM"
    return String.format(Locale.US, "%d:%02d %s", h12, minute, amPm)
}

@Composable
private fun NightBlackoutCard(
    nightBlackoutEnabled: Boolean,
    onNightBlackoutChange: (Boolean) -> Unit,
) {
    ModernCardContainer(
        title = "Night Blackout",
        icon = Icons.Default.Refresh,
        subtitle = "11 PM – 4 AM · Black screen with tap to wake",
    ) {
        SegmentedToggle(
            active = nightBlackoutEnabled,
            onActiveChange = onNightBlackoutChange,
            onLabel = "Enabled",
            offLabel = "Disabled",
        )
    }
}

@Composable
private fun SegmentedToggle(
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    onLabel: String = "On",
    offLabel: String = "Off",
) {
    val palette = LocalWallPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(InnerCardShape)
            .background(palette.settingsPanel.copy(alpha = 0.8f))
            .border(1.dp, palette.hairline.copy(alpha = 0.35f), InnerCardShape)
            .padding(3.dp),
    ) {
        TogglePill(
            title = onLabel,
            active = active,
            onClick = { onActiveChange(true) },
            modifier = Modifier.weight(1f),
        )
        TogglePill(
            title = offLabel,
            active = !active,
            onClick = { onActiveChange(false) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TogglePill(
    title: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalWallPalette.current
    val bgAnim by animateColorAsState(
        targetValue = if (active) palette.gold else Color.Transparent,
        animationSpec = tween(150),
        label = "bgAnim",
    )
    val textAnim by animateColorAsState(
        targetValue = if (active) palette.settingsPanel else palette.prayerPast,
        animationSpec = tween(150),
        label = "textAnim",
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(bgAnim)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (active) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = textAnim,
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(14.dp),
            )
        }
        Text(
            text = title,
            color = textAnim,
            fontSize = 13.sp,
            fontFamily = EnglishFontFamily,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun <T> SearchSelectField(
    label: String,
    value: String,
    placeholder: String,
    emptyHint: String,
    query: String,
    onQueryChange: (String) -> Unit,
    expanded: Boolean,
    enabled: Boolean,
    options: List<T>,
    selected: T?,
    optionText: (T) -> String,
    optionKey: (T) -> String,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
) {
    val palette = LocalWallPalette.current
    val density = LocalDensity.current
    val focusRequester = remember { FocusRequester() }
    var fieldWidth by remember { mutableIntStateOf(0) }
    var fieldHeight by remember { mutableIntStateOf(0) }

    LaunchedEffect(expanded) {
        if (expanded) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    Column {
        Text(
            text = label,
            color = palette.prayerPast,
            fontSize = 12.sp,
            fontFamily = EnglishFontFamily,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    fieldWidth = coords.size.width
                    fieldHeight = coords.size.height
                },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FieldShape)
                    .background(palette.settingsPanel.copy(alpha = 0.9f))
                    .border(
                        width = 1.dp,
                        color = if (expanded) palette.gold else palette.hairline.copy(alpha = 0.40f),
                        shape = FieldShape,
                    )
                    .clickable(enabled = enabled, onClick = onToggle)
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value.ifBlank { placeholder },
                    color = when {
                        !enabled -> palette.prayerPast.copy(alpha = 0.45f)
                        value.isBlank() -> palette.prayerPast
                        else -> palette.gold
                    },
                    fontSize = 15.sp,
                    fontFamily = EnglishFontFamily,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (expanded) "▴" else "▾",
                    color = palette.gold.copy(alpha = if (enabled) 0.85f else 0.35f),
                    fontSize = 13.sp,
                )
            }

            if (expanded) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(0, fieldHeight + 6),
                    onDismissRequest = onDismiss,
                    properties = PopupProperties(focusable = true),
                ) {
                    Column(
                        modifier = Modifier
                            .width(with(density) { fieldWidth.toDp() })
                            .heightIn(max = 280.dp)
                            .clip(MenuShape)
                            .background(palette.settingsPanel)
                            .border(
                                width = 1.dp,
                                color = palette.gold.copy(alpha = 0.65f),
                                shape = MenuShape,
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        // Search textfield
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.backgroundDeep.copy(alpha = 0.7f))
                                .border(1.dp, palette.hairline.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = palette.gold.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = query,
                                onValueChange = onQueryChange,
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = palette.gold,
                                    fontSize = 14.sp,
                                    fontFamily = EnglishFontFamily,
                                    fontWeight = FontWeight.Medium,
                                ),
                                cursorBrush = SolidColor(palette.gold),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                decorationBox = { inner ->
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterStart,
                                    ) {
                                        if (query.isEmpty()) {
                                            Text(
                                                text = placeholder,
                                                color = palette.prayerPast.copy(alpha = 0.6f),
                                                fontSize = 14.sp,
                                                fontFamily = EnglishFontFamily,
                                            )
                                        }
                                        inner()
                                    }
                                },
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        if (options.isEmpty()) {
                            Text(
                                text = emptyHint,
                                color = palette.prayerPast,
                                fontSize = 13.sp,
                                fontFamily = EnglishFontFamily,
                                modifier = Modifier.padding(vertical = 12.dp),
                            )
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 210.dp)) {
                                items(options, key = optionKey) { option ->
                                    val label = optionText(option)
                                    val active = option == selected
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (active) palette.gold.copy(alpha = 0.15f) else Color.Transparent)
                                            .clickable { onSelect(option) }
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (active) palette.gold else palette.clock,
                                            fontSize = 14.sp,
                                            fontFamily = EnglishFontFamily,
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.weight(1f),
                                        )
                                        if (active) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = palette.gold,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

