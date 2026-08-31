package com.mutazyounes.prayerathan.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Trial layout: Albany clock plus countdown. No Jordan clock.
 *
 * Rollback:
 * 1. Set `UseStackedClocks = false` in WallScreen.kt
 * 2. Delete this file
 */
@Composable
fun PortraitStackedWall(
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
            Spacer(Modifier.weight(10f))
            StackedClockColumn(
                state = state,
                type = type,
                landscapeHero = false,
                modifier = Modifier
                    .weight(34f)
                    .fillMaxWidth(),
            )
            Spacer(Modifier.weight(1f))
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
                    .weight(16f)
                    .fillMaxWidth(),
            )
            Spacer(Modifier.weight(2f))
            HorizontalHairline()
            PrayerGrid(
                cells = state.cells,
                portrait = true,
                type = type,
                modifier = Modifier
                    .weight(52f)
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
fun LandscapeStackedWall(
    state: WallUiState,
    type: TypeScale,
    inset: Dp,
    onOpenSettings: () -> Unit,
) {
    val (hours, minutes, seconds) = countdownParts(state.countdown)
    val playingName = state.playingName?.englishLabel()
    val layout = LandscapeWallLayout
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        SettingsButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .padding(top = inset)
                .align(Alignment.TopCenter),
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = screenHeight * layout.heroRowFromTop)
                .fillMaxWidth()
                .height(screenHeight * layout.clockHeight)
                .padding(horizontal = inset),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ClockBlock(
                label = "${state.weekday}  ${state.gregorianDate}",
                hourMinute = state.albanyTime,
                amPm = state.albanyAmPm,
                emphasis = ClockEmphasis.Local,
                showStar = false,
                type = type,
                portrait = false,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            WeatherHeroBlock(
                location = state.locationLabel,
                weatherLine = state.weatherLine,
                condition = state.weatherCondition,
                type = type,
                modifier = Modifier
                    .weight(1f)
                    .height(screenHeight * layout.weatherHeight),
            )
        }
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
                .align(Alignment.TopStart)
                .padding(
                    top = screenHeight * layout.countdownFromTop,
                    start = screenWidth * layout.countdownFromStart,
                )
                .width(screenWidth * layout.countdownWidth)
                .height(screenHeight * layout.countdownHeight),
        )
        if (state.themeMode == ThemeMode.LIGHT ||
            (state.themeMode == ThemeMode.AUTO && !state.darkTheme)
        ) {
            HorizontalHairline(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = screenHeight * layout.prayerHeight),
            )
        }
        PrayerGrid(
            cells = state.cells,
            portrait = false,
            type = type,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(screenHeight * layout.prayerHeight),
        )
    }
}

@Composable
private fun WeatherHeroBlock(
    location: String,
    weatherLine: String,
    condition: String,
    type: TypeScale,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        if (location.isNotEmpty()) {
            Text(
                text = location,
                style = labelStyle(type.label * 1.35f),
                maxLines = 1,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            WeatherHeroContent(
                weatherLine = weatherLine,
                condition = condition,
            )
        }
    }
}

@Composable
private fun WeatherHeroContent(
    weatherLine: String,
    condition: String,
) {
    val palette = LocalWallPalette.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        if (weatherLine.isEmpty()) return@BoxWithConstraints
        val iconRes = weatherIconRes(condition)
        val iconSizeDp = maxHeight * 0.46f
        val textSize = (maxHeight.value * 0.30f).coerceAtLeast(16f).sp
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = condition,
                tint = palette.gold,
                modifier = Modifier.size(iconSizeDp),
            )
            Text(
                text = weatherLine,
                style = tabularStyle(
                    color = palette.gold,
                    size = textSize,
                    weight = FontWeight.Medium,
                    letterSpacing = 0.04.em,
                ),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

@Composable
private fun StackedClockColumn(
    state: WallUiState,
    type: TypeScale,
    landscapeHero: Boolean,
    starDiameter: Dp? = null,
    modifier: Modifier = Modifier,
) {
    ClockBlock(
        label = "",
        hourMinute = state.albanyTime,
        amPm = state.albanyAmPm,
        emphasis = ClockEmphasis.Local,
        showStar = false,
        type = type,
        portrait = !landscapeHero,
        starDiameter = starDiameter,
        modifier = modifier.fillMaxSize(),
    )
}
