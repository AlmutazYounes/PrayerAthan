package com.mutazyounes.prayerathan.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.mutazyounes.prayerathan.engine.CityCatalog
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Trial: Albany plus countdown in StackedClockWall.kt. No Jordan clock.
 * Set this false to restore the split layout in this file.
 */
const val UseStackedClocks = true

@Composable
fun WallScreen(
    viewModel: WallViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    var settingsOpen by remember { mutableStateOf(false) }
    var userAwake by remember { mutableStateOf(false) }

    LaunchedEffect(userAwake) {
        if (userAwake) {
            delay(15_000L)
            userAwake = false
        }
    }

    val showBlackout = state.isNightBlackout && !userAwake && !settingsOpen

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val assets = context.assets
        withContext(Dispatchers.IO) {
            CityCatalog.loadBundled { name ->
                assets.open(name).bufferedReader().use { it.readText() }
            }
        }
    }
    DisposableEffect(showBlackout) {
        val window = (context as? Activity)?.window
        if (window != null) {
            val params = window.attributes
            params.screenBrightness = when {
                showBlackout -> 0.0f
                else -> WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
            window.attributes = params
        }
        onDispose {
            val window = (context as? Activity)?.window
            if (window != null) {
                val params = window.attributes
                params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                window.attributes = params
            }
        }
    }

    CompositionLocalProvider(LocalWallPalette provides DarkWallPalette) {
        Box(modifier.fillMaxSize()) {
            WallScreen(
                state = state,
                onStopAthan = viewModel::stopAthan,
                onOpenSettings = { settingsOpen = true },
                modifier = Modifier.fillMaxSize(),
            )
            if (showBlackout) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { userAwake = true },
                        ),
                )
            }
            if (settingsOpen) {
                SettingsSheet(
                    cityLabel = state.locationCity,
                    latitude = state.locationLatitude,
                    longitude = state.locationLongitude,
                    locationError = state.locationError,
                    athanSoundId = state.athanSoundId,
                    athkarEnabled = state.athkarEnabled,
                    mutedPrayers = state.mutedPrayers,
                    nightBlackoutEnabled = state.nightBlackoutEnabled,
                    demoId = state.demoId,
                    onSelectLocation = { label, lat, lon, zone ->
                        if (viewModel.saveLocation(label, lat, lon, zone)) {
                            settingsOpen = false
                        }
                    },
                    onResetAlbany = {
                        viewModel.resetToAlbany()
                        settingsOpen = false
                    },
                    onUseGps = viewModel::useGps,
                    onSelectAthanSound = viewModel::setAthanSound,
                    onAthkarEnabledChange = viewModel::setAthkarEnabled,
                    onTogglePrayerMute = viewModel::togglePrayerMute,
                    onNightBlackoutChange = viewModel::setNightBlackout,
                    onPlayAthanDemo = viewModel::playAthanDemo,
                    onDismiss = {
                        viewModel.stopDemo()
                        settingsOpen = false
                    },
                )
            }
        }
    }
}

@Composable
fun WallScreen(
    state: WallUiState,
    onStopAthan: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalWallPalette provides DarkWallPalette) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { if (state.athanPlaying || state.athkarPlaying) onStopAthan() },
                    onLongClick = onOpenSettings,
                ),
        ) {
            val shortest = min(maxWidth.value, maxHeight.value)
            val type = typeScale(shortest)
            val inset = screenInset(shortest)
            val portrait = maxHeight >= maxWidth
            WallBackdrop(portrait = portrait)
            if (UseStackedClocks) {
                if (portrait) {
                    PortraitStackedWall(state, type, inset, onOpenSettings)
                } else {
                    LandscapeStackedWall(state, type, inset, onOpenSettings)
                }
            } else if (portrait) {
                PortraitWall(state, type, inset, onOpenSettings)
            } else {
                LandscapeWall(state, type, inset, onOpenSettings)
            }
        }
    }
}

@Composable
private fun PortraitWall(
    state: WallUiState,
    type: TypeScale,
    inset: Dp,
    onOpenSettings: () -> Unit,
) {
    val (hours, minutes, seconds) = countdownParts(state.countdown)
    val playingName = state.playingName?.englishLabel()
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = inset, end = inset, bottom = inset),
        ) {
            Spacer(Modifier.weight(7f))
            ClockBlock(
                label = "",
                hourMinute = state.albanyTime,
                amPm = state.albanyAmPm,
                emphasis = ClockEmphasis.Local,
                showStar = true,
                type = type,
                portrait = true,
                modifier = Modifier
                    .weight(42f)
                    .fillMaxWidth(),
            )
            Spacer(Modifier.weight(2f))
            CountdownBlock(
                label = state.nextLabel,
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                playingPrayerName = playingName,
                type = type,
                countdownSize = type.countdownPortrait,
                portrait = true,
                athkarCaption = state.athkarCaption,
                modifier = Modifier
                    .weight(20f)
                    .fillMaxWidth(),
            )
            Spacer(Modifier.weight(1f))
            HorizontalHairline()
            PrayerGrid(
                cells = state.cells,
                portrait = true,
                type = type,
                modifier = Modifier
                    .weight(28f)
                    .fillMaxWidth(),
            )
        }
        Header(
            location = state.locationLabel,
            weekday = state.weekday,
            dateLine = state.gregorianDate,
            type = type,
            onOpenSettings = onOpenSettings,
            weatherLine = state.weatherLine,
            modifier = Modifier
                .fillMaxWidth()
                .padding(inset),
        )
    }
}

@Composable
private fun LandscapeWall(
    state: WallUiState,
    type: TypeScale,
    inset: Dp,
    onOpenSettings: () -> Unit,
) {
    val (hours, minutes, seconds) = countdownParts(state.countdown)
    val playingName = state.playingName?.englishLabel()
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = inset),
    ) {
        Box(
            modifier = Modifier
                .weight(70f)
                .fillMaxWidth(),
        ) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val columnWidth = maxWidth / 2
                val starDiameter = columnWidth * 0.70f
                val headerClearance = maxHeight * 0.16f
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = headerClearance),
                ) {
                    ClockBlock(
                        label = "",
                        hourMinute = state.albanyTime,
                        amPm = state.albanyAmPm,
                        emphasis = ClockEmphasis.Local,
                        showStar = true,
                        type = type,
                        portrait = false,
                        starDiameter = starDiameter,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    CountdownBlock(
                        label = state.nextLabel,
                        hours = hours,
                        minutes = minutes,
                        seconds = seconds,
                        playingPrayerName = playingName,
                        type = type,
                        countdownSize = type.countdownLandscape,
                        portrait = false,
                        athkarCaption = state.athkarCaption,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = headerClearance),
                ) {
                    Spacer(Modifier.weight(1f))
                    VerticalHairline()
                    Spacer(Modifier.weight(1f))
                }
            }
            Header(
                location = state.locationLabel,
                weekday = state.weekday,
                dateLine = state.gregorianDate,
                type = type,
                onOpenSettings = onOpenSettings,
                weatherLine = state.weatherLine,
                leftWeight = 1f,
                rightWeight = 1f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = inset)
                    .align(Alignment.TopCenter),
            )
        }
        HorizontalHairline()
        PrayerGrid(
            cells = state.cells,
            portrait = false,
            type = type,
            modifier = Modifier
                .weight(30f)
                .fillMaxWidth(),
        )
    }
}

@Composable
fun Header(
    location: String,
    weekday: String,
    dateLine: String,
    type: TypeScale,
    onOpenSettings: () -> Unit,
    weatherLine: String = "",
    leftWeight: Float? = null,
    rightWeight: Float? = null,
    modifier: Modifier = Modifier,
) {
    val palette = LocalWallPalette.current
    val split = leftWeight != null && rightWeight != null
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier
                .weight(leftWeight ?: 1f)
                .fillMaxWidth(),
        ) {
            Text(
                text = location,
                style = tabularStyle(
                    color = palette.location,
                    size = type.location,
                    weight = FontWeight.Medium,
                ).copy(letterSpacing = 0.12.em),
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
            if (weatherLine.isNotEmpty()) {
                val iconRes = weatherIconRes(weatherLine)
                val weatherMaxSp = if (split) type.dateLine.value * 1.15f else type.dateLine.value
                val weatherMinSp = type.label.value
                WeatherRow(
                    text = weatherLine,
                    iconRes = iconRes,
                    color = palette.gold,
                    maxSp = weatherMaxSp,
                    minSp = weatherMinSp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        SettingsButton(
            onClick = onOpenSettings,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp),
        )
        Column(
            // Always weighted: an unweighted Row child that fills to max width
            // claims the Row's full incoming width, starving the location
            // column on the left. That was why location/weather vanished
            // in portrait, where this column previously had no weight.
            modifier = Modifier.weight(rightWeight ?: 1f).fillMaxWidth(),
            horizontalAlignment = Alignment.End,
        ) {
            FittingLine(
                text = "$weekday  $dateLine",
                color = palette.date,
                weight = FontWeight.Normal,
                align = TextAlign.End,
                maxSp = if (split) type.dateLine.value * 1.15f else type.dateLine.value,
                minSp = type.label.value,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun WeatherRow(
    text: String,
    iconRes: Int,
    color: Color,
    maxSp: Float,
    minSp: Float,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()
        val fitted = remember(text, maxWidth, maxSp, minSp, density) {
            if (maxWidth.value <= 0f || text.isEmpty()) {
                minSp
            } else {
                val referenceSize = 100f
                val referenceStyle = tabularStyle(
                    color = color,
                    size = referenceSize.sp,
                    weight = FontWeight.Medium,
                )
                val referenceTextWidth = with(density) {
                    textMeasurer.measure(text, referenceStyle).size.width.toDp().value
                }
                // Icon size is ~1.15x line height, plus 6dp gap.
                // At referenceSize 100sp, icon is ~115dp + 6dp gap = 121dp.
                val referenceTotalWidth = referenceTextWidth + 121f
                val byWidth = if (referenceTotalWidth > 0f) {
                    referenceSize * maxWidth.value / referenceTotalWidth
                } else {
                    maxSp
                }
                byWidth.coerceIn(minSp, maxSp)
            }
        }
        val iconSizeDp = with(density) { (fitted * 1.15f).sp.toDp() }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(iconSizeDp),
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = text,
                style = tabularStyle(
                    color = color,
                    size = fitted.sp,
                    weight = FontWeight.Medium,
                ),
                textAlign = TextAlign.Start,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun FittingLine(
    text: String,
    color: Color,
    weight: FontWeight,
    align: TextAlign,
    maxSp: Float,
    minSp: Float,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()
        // Measure the real string instead of guessing an em-width-per-char
        // ratio. The guess ran wide for this font and let text overflow
        // its box uncaught, since Text() clips a maxLines=1 overrun silently.
        val fitted = remember(text, maxWidth, maxSp, minSp, weight, density) {
            if (maxWidth.value <= 0f || text.isEmpty()) {
                minSp
            } else {
                val referenceSize = 100f
                val referenceStyle = tabularStyle(
                    color = color,
                    size = referenceSize.sp,
                    weight = weight,
                )
                val referenceWidth = with(density) {
                    textMeasurer.measure(text, referenceStyle).size.width.toDp().value
                }
                val byWidth = if (referenceWidth > 0f) {
                    referenceSize * maxWidth.value / referenceWidth
                } else {
                    maxSp
                }
                byWidth.coerceIn(minSp, maxSp)
            }
        }
        Text(
            text = text,
            style = tabularStyle(
                color = color,
                size = fitted.sp,
                weight = weight,
            ),
            textAlign = align,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalWallPalette.current
    Box(
        modifier = modifier
            .size(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = "Settings",
            tint = palette.gold,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
fun StarWatermark(modifier: Modifier = Modifier) {
    val star = LocalWallPalette.current.star
    Canvas(modifier) {
        val stroke = HairlineWidth.toPx()
        val side = min(size.width, size.height) * 0.58f
        val topLeft = Offset((size.width - side) / 2f, (size.height - side) / 2f)
        val rect = Size(side, side)
        drawRect(
            color = star,
            topLeft = topLeft,
            size = rect,
            style = Stroke(width = stroke),
        )
        rotate(45f) {
            drawRect(
                color = star,
                topLeft = topLeft,
                size = rect,
                style = Stroke(width = stroke),
            )
        }
    }
}

internal fun countdownParts(countdown: String): Triple<String, String, String> {
    val parts = countdown.split(":")
    return Triple(
        parts.getOrElse(0) { "00" },
        parts.getOrElse(1) { "00" },
        parts.getOrElse(2) { "00" },
    )
}
