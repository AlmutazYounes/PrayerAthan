package com.mutazyounes.prayerathan.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.mutazyounes.prayerathan.engine.CityCatalog
import com.mutazyounes.prayerathan.engine.PlaceCity
import com.mutazyounes.prayerathan.engine.PlaceCountry
import com.mutazyounes.prayerathan.engine.PrayerName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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
    nightBlackoutEnabled: Boolean,
    demoId: String?,
    onSelectLocation: (String, Double, Double, String) -> Unit,
    onUseGps: () -> Unit,
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
                                onTogglePrayerMute = onTogglePrayerMute,
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

                        PrayerAthansCard(
                            mutedPrayers = mutedPrayers,
                            onTogglePrayerMute = onTogglePrayerMute,
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
private fun PrayerAthansCard(
    mutedPrayers: Set<PrayerName>,
    onTogglePrayerMute: (PrayerName) -> Unit,
) {
    ModernCardContainer(
        title = "Prayer Athans",
        icon = Icons.Default.Check,
        subtitle = "Tap a prayer to toggle athan audio",
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

