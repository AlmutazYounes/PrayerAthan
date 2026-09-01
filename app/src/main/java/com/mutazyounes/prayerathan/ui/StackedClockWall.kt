package com.mutazyounes.prayerathan.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Stacked clocks wall layout.
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
    val playingName = state.playingName?.englishLabel()
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = inset, end = inset, bottom = inset),
        ) {
            Spacer(Modifier.weight(14f))
            ArcWallClock(
                hourMinute = state.albanyTime,
                nextLabel = state.nextLabel,
                countdown = state.countdown,
                ringFraction = state.nextPrayerRing,
                playingPrayerName = playingName,
                type = type,
                arcWidthFraction = 0.72f,
                arcHeightFraction = 0.96f,
                countdownSize = type.countdownPortrait,
                modifier = Modifier
                    .weight(40f)
                    .fillMaxWidth(),
            )
            Spacer(Modifier.weight(10f))
            PrayerGrid(
                cells = state.cells,
                portrait = true,
                type = type,
                countdown = state.countdown,
                athanPlaying = state.athanPlaying,
                playingName = playingName,
                modifier = Modifier
                    .weight(34f)
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            )
        }
        Header(
            location = state.locationLabel,
            weekday = state.weekday,
            dateLine = state.gregorianDate,
            type = type,
            onOpenSettings = onOpenSettings,
            weatherLine = state.weatherLine,
            leftWeight = 1.35f,
            rightWeight = 0.65f,
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
    val playingName = state.playingName?.englishLabel()
    val palette = LocalWallPalette.current
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = inset, end = inset, bottom = inset),
        ) {
            Spacer(Modifier.weight(12f))
            ArcWallClock(
                hourMinute = state.albanyTime,
                nextLabel = state.nextLabel,
                countdown = state.countdown,
                ringFraction = state.nextPrayerRing,
                playingPrayerName = playingName,
                type = type,
                arcWidthFraction = 0.52f,
                arcHeightFraction = 0.98f,
                countdownSize = type.countdownLandscape,
                countdownScale = 1.0f,
                modifier = Modifier
                    .weight(48f)
                    .fillMaxWidth(),
            )
            Spacer(Modifier.weight(6f))
            PrayerGrid(
                cells = state.cells,
                portrait = false,
                type = type,
                countdown = state.countdown,
                athanPlaying = state.athanPlaying,
                playingName = playingName,
                modifier = Modifier
                    .weight(34f)
                    .fillMaxWidth(),
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(start = inset, end = inset, top = inset),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (state.locationLabel.isNotEmpty()) {
                    Text(
                        text = state.locationLabel,
                        style = labelStyle(type.label * 1.35f, palette.location),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                    )
                }
                if (state.weatherLine.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val iconRes = weatherIconRes(state.weatherCondition)
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = state.weatherCondition,
                            tint = palette.gold,
                            modifier = Modifier.size((type.label.value * 1.08f).dp),
                        )
                        Text(
                            text = state.weatherLine,
                            style = labelStyle(type.label * 1.08f, palette.gold),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = "${state.weekday}  ${state.gregorianDate}",
                    style = labelStyle(type.label * 1.35f, palette.date),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                )
                SettingsButton(
                    onClick = onOpenSettings,
                    compact = true,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun ArcWallClock(
    hourMinute: String,
    nextLabel: String,
    countdown: String,
    ringFraction: Float,
    playingPrayerName: String?,
    type: TypeScale,
    arcWidthFraction: Float,
    arcHeightFraction: Float = 0.92f,
    countdownSize: androidx.compose.ui.unit.TextUnit,
    countdownScale: Float = 0.92f,
    labelScale: Float = 0.9f,
    modifier: Modifier = Modifier,
) {
    val palette = LocalWallPalette.current
    if (playingPrayerName != null) {
        AthanPlayingBlock(
            prayerName = playingPrayerName,
            type = type,
            countdownSize = countdownSize,
            modifier = modifier,
        )
        return
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val side = minOf(maxWidth * arcWidthFraction, maxHeight * arcHeightFraction)
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(side),
            contentAlignment = Alignment.Center,
        ) {
            PrayerArcTimer(
                fraction = ringFraction,
                modifier = Modifier.fillMaxSize(),
            )
            ClockBlock(
                label = "",
                hourMinute = hourMinute,
                amPm = "",
                emphasis = ClockEmphasis.Local,
                showStar = false,
                type = type,
                portrait = true,
                portraitArc = true,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = side * 0.05f,
                        end = side * 0.05f,
                        top = side * 0.05f,
                        bottom = side * 0.14f,
                    ),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = side * 0.015f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = countdown,
                    style = tabularStyle(
                        color = palette.gold,
                        size = countdownSize * countdownScale,
                        weight = FontWeight.Bold,
                    ),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                )
                Text(
                    text = nextLabel,
                    style = labelStyle(type.label * labelScale, palette.gold),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

/** Horseshoe arc open at the bottom; bright stroke = time left until next prayer. */
@Composable
private fun PrayerArcTimer(
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    val palette = LocalWallPalette.current
    val clamped = fraction.coerceIn(0f, 1f)
    Canvas(modifier = modifier) {
        val strokeWidth = (size.minDimension * 0.045f).coerceAtLeast(4f)
        val inset = strokeWidth / 2f
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(inset, inset)
        // Gap at 6 o'clock (~70°); arc runs lower-left → top → lower-right.
        val startAngle = 125f
        val sweepAngle = 290f
        drawArc(
            color = palette.gold.copy(alpha = 0.30f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        if (clamped > 0f) {
            drawArc(
                color = palette.gold,
                startAngle = startAngle,
                sweepAngle = sweepAngle * clamped,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
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
