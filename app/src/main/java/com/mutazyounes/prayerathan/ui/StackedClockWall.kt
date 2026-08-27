package com.mutazyounes.prayerathan.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

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
            Spacer(Modifier.weight(5f))
            StackedClockColumn(
                state = state,
                type = type,
                landscapeHero = false,
                modifier = Modifier
                    .weight(32f)
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
                    .weight(18f)
                    .fillMaxWidth(),
            )
            Spacer(Modifier.weight(4f))
            HorizontalHairline()
            PrayerGrid(
                cells = state.cells,
                portrait = true,
                type = type,
                modifier = Modifier
                    .weight(40f)
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
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = inset),
    ) {
        Box(
            modifier = Modifier
                .weight(64f)
                .fillMaxWidth(),
        ) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val albanyWeight = 1.08f
                val countdownWeight = 0.92f
                val albanyWidth = maxWidth * albanyWeight / (albanyWeight + countdownWeight)
                val starDiameter = albanyWidth * 0.70f
                val headerClearance = maxHeight * 0.16f
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = headerClearance),
                ) {
                    StackedClockColumn(
                        state = state,
                        type = type,
                        landscapeHero = true,
                        starDiameter = starDiameter,
                        modifier = Modifier
                            .weight(albanyWeight)
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
                            .weight(countdownWeight)
                            .fillMaxHeight(),
                    )
                }
            }
            Header(
                location = state.locationLabel,
                weekday = state.weekday,
                dateLine = state.gregorianDate,
                type = type,
                onOpenSettings = onOpenSettings,
                weatherLine = state.weatherLine,
                leftWeight = 1.08f,
                rightWeight = 0.92f,
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
                .weight(36f)
                .fillMaxWidth(),
        )
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
