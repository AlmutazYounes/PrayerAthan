package com.mutazyounes.prayerathan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.mutazyounes.prayerathan.audio.AthanCatalog
import com.mutazyounes.prayerathan.audio.AthanSoundChoice
import com.mutazyounes.prayerathan.engine.CityCatalog
import com.mutazyounes.prayerathan.engine.PlaceCity
import com.mutazyounes.prayerathan.engine.PlaceCountry
import com.mutazyounes.prayerathan.engine.PrayerName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val SheetShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
private val ChipShape = RoundedCornerShape(10.dp)
private val MenuShape = RoundedCornerShape(12.dp)
private val FieldShape = RoundedCornerShape(10.dp)
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
    themeMode: ThemeMode,
    athanSoundId: String,
    athkarEnabled: Boolean,
    mutedPrayers: Set<PrayerName>,
    nightBlackoutEnabled: Boolean,
    demoId: String?,
    onSelectLocation: (String, Double, Double, String) -> Unit,
    onResetAlbany: () -> Unit,
    onUseGps: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onSelectAthanSound: (String) -> Unit,
    onAthkarEnabledChange: (Boolean) -> Unit,
    onTogglePrayerMute: (PrayerName) -> Unit,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.clock.copy(alpha = 0.32f))
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
            val sheetMax = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = sheetMax)
                    .clip(SheetShape)
                    .background(palette.settingsPanel)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    )
                    .imePadding()
                    .padding(horizontal = 28.dp, vertical = 20.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                        .height(4.dp)
                        .fillMaxWidth(0.10f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(palette.gold.copy(alpha = 0.45f)),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "SETTINGS",
                        style = labelStyle(16.sp),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "CLOSE",
                        style = labelStyle(13.sp),
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .padding(8.dp),
                    )
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HairlineWidth)
                        .background(palette.gold.copy(alpha = 0.55f)),
                )
                Spacer(Modifier.height(18.dp))
                if (landscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = sheetMax * 0.78f),
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            LocationBlock(
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
                                onResetAlbany = onResetAlbany,
                                onUseGps = onUseGps,
                            )
                            Spacer(Modifier.height(22.dp))
                            ThemeBlock(
                                themeMode = themeMode,
                                onThemeModeChange = onThemeModeChange,
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            PrayerAthansBlock(
                                mutedPrayers = mutedPrayers,
                                onTogglePrayerMute = onTogglePrayerMute,
                            )
                            Spacer(Modifier.height(22.dp))
                            AthanBlock(
                                athanSoundId = athanSoundId,
                                demoId = demoId,
                                onSelectAthanSound = onSelectAthanSound,
                                onPlayAthanDemo = onPlayAthanDemo,
                            )
                            Spacer(Modifier.height(22.dp))
                            AthkarBlock(
                                athkarEnabled = athkarEnabled,
                                onAthkarEnabledChange = onAthkarEnabledChange,
                            )
                            Spacer(Modifier.height(22.dp))
                            NightBlackoutBlock(
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
                    ) {
                        LocationBlock(
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
                            onResetAlbany = onResetAlbany,
                            onUseGps = onUseGps,
                        )
                        Spacer(Modifier.height(22.dp))
                        PrayerAthansBlock(
                            mutedPrayers = mutedPrayers,
                            onTogglePrayerMute = onTogglePrayerMute,
                        )
                        Spacer(Modifier.height(22.dp))
                        AthanBlock(
                            athanSoundId = athanSoundId,
                            demoId = demoId,
                            onSelectAthanSound = onSelectAthanSound,
                            onPlayAthanDemo = onPlayAthanDemo,
                        )
                        Spacer(Modifier.height(22.dp))
                        AthkarBlock(
                            athkarEnabled = athkarEnabled,
                            onAthkarEnabledChange = onAthkarEnabledChange,
                        )
                        Spacer(Modifier.height(22.dp))
                        NightBlackoutBlock(
                            nightBlackoutEnabled = nightBlackoutEnabled,
                            onNightBlackoutChange = onNightBlackoutChange,
                        )
                        Spacer(Modifier.height(22.dp))
                        ThemeBlock(
                            themeMode = themeMode,
                            onThemeModeChange = onThemeModeChange,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationBlock(
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
    onResetAlbany: () -> Unit,
    onUseGps: () -> Unit,
) {
    SettingsSection("Location") {
        SearchSelect(
            title = "Country",
            value = selectedCountry?.name.orEmpty(),
            placeholder = if (!placesReady) "Loading…" else "Search",
            emptyHint = "No matches",
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
        Spacer(Modifier.height(12.dp))
        SearchSelect(
            title = "City",
            value = cityValue,
            placeholder = when {
                !placesReady -> "Loading…"
                selectedCountry == null -> "Country first"
                else -> "Type two letters"
            },
            emptyHint = if (cityQuery.trim().length < CITY_QUERY_MIN) {
                "Type two letters"
            } else {
                "No matches"
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
                if (selectedCountry == null) return@SearchSelect
                onOpenMenu(if (openMenu == PlaceMenu.City) null else PlaceMenu.City)
                onCityQuery("")
            },
            onDismiss = { onOpenMenu(null) },
            onSelect = onCity,
        )
        if (!locationError.isNullOrBlank()) {
            Text(
                text = locationError,
                color = LocalWallPalette.current.gold,
                fontSize = 13.sp,
                fontFamily = EnglishFontFamily,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = "GPS",
                style = labelStyle(12.sp),
                modifier = Modifier
                    .clickable(onClick = onUseGps)
                    .padding(vertical = 6.dp),
            )
            Text(
                text = "Albany",
                style = labelStyle(12.sp),
                modifier = Modifier
                    .clickable(onClick = onResetAlbany)
                    .padding(vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun PrayerAthansBlock(
    mutedPrayers: Set<PrayerName>,
    onTogglePrayerMute: (PrayerName) -> Unit,
) {
    SettingsSection("Prayer athans", "Tap to mute or unmute") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PrayerName.athanTargets().forEach { prayer ->
                val muted = prayer in mutedPrayers
                ChoiceChip(
                    title = prayer.englishLabel(),
                    active = !muted,
                    onClick = { onTogglePrayerMute(prayer) },
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun AthanBlock(
    athanSoundId: String,
    demoId: String?,
    onSelectAthanSound: (String) -> Unit,
    onPlayAthanDemo: (String) -> Unit,
) {
    val current = AthanCatalog.choice(athanSoundId)
    SettingsSection("Athan", current.title) {
        AthanCatalog.all.forEach { choice ->
            SoundRow(
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
private fun AthkarBlock(
    athkarEnabled: Boolean,
    onAthkarEnabledChange: (Boolean) -> Unit,
) {
    SettingsSection("Hourly athkar", "8 AM to 10 PM · athan wins") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChoiceChip(
                title = "On",
                active = athkarEnabled,
                onClick = { onAthkarEnabledChange(true) },
                modifier = Modifier.weight(1f),
            )
            ChoiceChip(
                title = "Off",
                active = !athkarEnabled,
                onClick = { onAthkarEnabledChange(false) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NightBlackoutBlock(
    nightBlackoutEnabled: Boolean,
    onNightBlackoutChange: (Boolean) -> Unit,
) {
    SettingsSection("Night blackout", "11 PM to 4 AM · tap to wake") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChoiceChip(
                title = "On",
                active = nightBlackoutEnabled,
                onClick = { onNightBlackoutChange(true) },
                modifier = Modifier.weight(1f),
            )
            ChoiceChip(
                title = "Off",
                active = !nightBlackoutEnabled,
                onClick = { onNightBlackoutChange(false) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ThemeBlock(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    SettingsSection(title = "Theme") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChoiceChip(
                title = "Light",
                active = themeMode == ThemeMode.LIGHT,
                onClick = { onThemeModeChange(ThemeMode.LIGHT) },
                modifier = Modifier.weight(1f),
            )
            ChoiceChip(
                title = "Dark",
                active = themeMode == ThemeMode.DARK,
                onClick = { onThemeModeChange(ThemeMode.DARK) },
                modifier = Modifier.weight(1f),
            )
            ChoiceChip(
                title = "Auto",
                active = themeMode == ThemeMode.AUTO,
                onClick = { onThemeModeChange(ThemeMode.AUTO) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    caption: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = LocalWallPalette.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = labelStyle(13.sp),
        )
        if (!caption.isNullOrBlank()) {
            Text(
                text = caption,
                color = palette.prayerPast,
                fontSize = 13.sp,
                fontFamily = EnglishFontFamily,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HairlineWidth)
                .background(palette.gold.copy(alpha = 0.35f)),
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun <T> SearchSelect(
    title: String,
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
            text = title,
            color = palette.prayerPast,
            fontSize = 12.sp,
            fontFamily = EnglishFontFamily,
        )
        Spacer(Modifier.height(6.dp))
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
                    .border(
                        width = HairlineWidth,
                        color = if (expanded) palette.gold else palette.hairline.copy(alpha = 0.55f),
                        shape = FieldShape,
                    )
                    .clickable(enabled = enabled, onClick = onToggle)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value.ifBlank { placeholder },
                    color = when {
                        !enabled -> palette.prayerPast.copy(alpha = 0.55f)
                        value.isBlank() -> palette.prayerPast
                        else -> palette.gold
                    },
                    fontSize = 17.sp,
                    fontFamily = EnglishFontFamily,
                    fontWeight = FontWeight.Medium,
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
                            .heightIn(max = 300.dp)
                            .clip(MenuShape)
                            .background(palette.settingsPanel)
                            .border(
                                width = HairlineWidth,
                                color = palette.gold.copy(alpha = 0.55f),
                                shape = MenuShape,
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                color = palette.gold,
                                fontSize = 16.sp,
                                fontFamily = EnglishFontFamily,
                                fontWeight = FontWeight.Medium,
                            ),
                            cursorBrush = SolidColor(palette.gold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            decorationBox = { inner ->
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 24.dp),
                                        contentAlignment = Alignment.CenterStart,
                                    ) {
                                        if (query.isEmpty()) {
                                            Text(
                                                text = placeholder,
                                                color = palette.prayerPast,
                                                fontSize = 16.sp,
                                                fontFamily = EnglishFontFamily,
                                            )
                                        }
                                        inner()
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(HairlineWidth)
                                            .background(palette.gold.copy(alpha = 0.55f)),
                                    )
                                }
                            },
                        )
                        Spacer(Modifier.height(6.dp))
                        if (options.isEmpty()) {
                            Text(
                                text = emptyHint,
                                color = palette.prayerPast,
                                fontSize = 15.sp,
                                fontFamily = EnglishFontFamily,
                                modifier = Modifier.padding(vertical = 10.dp),
                            )
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                                items(options, key = optionKey) { option ->
                                    val label = optionText(option)
                                    val active = option == selected
                                    Text(
                                        text = label,
                                        color = if (active) palette.gold else palette.clock,
                                        fontSize = 16.sp,
                                        fontFamily = EnglishFontFamily,
                                        fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSelect(option) }
                                            .padding(vertical = 9.dp),
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

@Composable
private fun SoundRow(
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
            .padding(vertical = 2.dp)
            .clip(ChipShape)
            .clickable(onClick = onSelect)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(end = 10.dp)
                .width(2.dp)
                .height(18.dp)
                .background(if (selected) palette.gold else palette.hairline.copy(alpha = 0.35f)),
        )
        Text(
            text = choice.title,
            color = if (selected) palette.gold else palette.clock,
            fontSize = 16.sp,
            fontFamily = EnglishFontFamily,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = if (playing) "STOP" else "PLAY",
            style = labelStyle(11.sp),
            modifier = Modifier
                .clickable(onClick = onPlay)
                .padding(6.dp),
        )
    }
}

@Composable
private fun ChoiceChip(
    title: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 15.sp,
) {
    val palette = LocalWallPalette.current
    Text(
        text = title,
        color = if (active) palette.gold else palette.clock,
        fontSize = fontSize,
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = modifier
            .border(
                width = HairlineWidth,
                color = if (active) palette.gold else palette.hairline.copy(alpha = 0.55f),
                shape = ChipShape,
            )
            .clip(ChipShape)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
    )
}
