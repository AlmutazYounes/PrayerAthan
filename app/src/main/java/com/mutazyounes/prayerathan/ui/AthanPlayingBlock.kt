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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlin.math.min

@Composable
fun AthanPlayingBlock(
    prayerName: String,
    type: TypeScale,
    countdownSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        val palette = LocalWallPalette.current
        val nameSize = if (maxWidth.value > 0f && maxHeight.value > 0f) {
            fitSp(
                maxWidthDp = maxWidth.value,
                maxHeightDp = maxHeight.value,
                widthChars = prayerName.length.coerceAtLeast(4).toFloat(),
                heightPercent = 0.36f,
                widthPercent = 0.90f,
            )
        } else {
            countdownSize
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "NOW",
                style = labelStyle(type.label),
                maxLines = 1,
            )
            Text(
                text = prayerName.uppercase(),
                style = tabularStyle(
                    color = palette.gold,
                    size = nameSize,
                    weight = FontWeight.Medium,
                ),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
            Text(
                text = "Adhan is playing",
                style = labelStyle((nameSize.value * 0.22f).coerceAtLeast(10f).sp, palette.gold),
                maxLines = 1,
            )
        }
    }
}

@Composable
fun CountdownBlock(
    label: String,
    hours: String,
    minutes: String,
    seconds: String,
    playingPrayerName: String?,
    type: TypeScale,
    countdownSize: TextUnit,
    portrait: Boolean,
    modifier: Modifier = Modifier,
    athkarCaption: String = "",
) {
    if (playingPrayerName != null) {
        AthanPlayingBlock(
            prayerName = playingPrayerName,
            type = type,
            countdownSize = countdownSize,
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.BottomCenter,
        ) {
            CountdownDigits(
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                landscape = !portrait,
            )
        }
        AthkarCaption(caption = athkarCaption, type = type)
    }
}

@Composable
private fun CountdownDigits(
    hours: String,
    minutes: String,
    seconds: String,
    landscape: Boolean,
) {
    if (landscape) {
        LandscapeCountdown(hours, minutes, seconds)
        return
    }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds(),
        verticalAlignment = Alignment.Top,
    ) {
        DigitPair(hours, "HRS", landscape = false, Modifier.weight(1f))
        DigitPair(minutes, "MIN", landscape = false, Modifier.weight(1f))
        DigitPair(seconds, "SEC", landscape = false, Modifier.weight(1f))
    }
}

@Composable
private fun LandscapeCountdown(
    hours: String,
    minutes: String,
    seconds: String,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        val palette = LocalWallPalette.current
        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()
        val heightPercent = 1.0f
        val widthPercent = 0.92f
        // Measure the exact string at a reference size instead of guessing a
        // char count, then scale to fit. A guessed width let seconds run off
        // the right edge because "00:00:00" is wider than the old estimate.
        val digitSize = remember(maxWidth, maxHeight, density) {
            if (maxWidth.value <= 0f || maxHeight.value <= 0f) {
                12.sp
            } else {
                val referenceSize = 100.sp
                val referenceStyle = tabularStyle(
                    color = palette.gold,
                    size = referenceSize,
                    weight = FontWeight.Bold,
                    lineHeight = referenceSize,
                )
                val referenceWidth = with(density) {
                    textMeasurer.measure("00:00:00", referenceStyle).size.width.toDp().value
                }
                val byWidth = if (referenceWidth > 0f) {
                    referenceSize.value * (maxWidth.value * widthPercent) / referenceWidth
                } else {
                    referenceSize.value
                }
                val byHeight = maxHeight.value * heightPercent
                min(byWidth, byHeight).coerceAtLeast(8f).sp
            }
        }
        val unitSize = (digitSize.value * 0.16f).coerceAtLeast(10f).sp
        val digitStyle = tabularStyle(
            color = palette.gold,
            size = digitSize,
            weight = FontWeight.Bold,
            lineHeight = digitSize,
        )
        // Measure once per size instead of letting each Text wrap to its own
        // content width, so the row never reflows as digits change every second.
        val pairWidth = remember(digitSize, density) {
            with(density) { textMeasurer.measure("00", digitStyle).size.width.toDp() }
        }
        val colonWidth = remember(digitSize, density) {
            with(density) { textMeasurer.measure(":", digitStyle).size.width.toDp() }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FixedWidthDigits(hours, pairWidth, digitStyle)
                FixedWidthDigits(":", colonWidth, digitStyle)
                FixedWidthDigits(minutes, pairWidth, digitStyle)
                FixedWidthDigits(":", colonWidth, digitStyle)
                FixedWidthDigits(seconds, pairWidth, digitStyle)
            }
            Row {
                UnitCaption("HRS", pairWidth, unitSize, palette.gold)
                Spacer(Modifier.width(colonWidth))
                UnitCaption("MIN", pairWidth, unitSize, palette.gold)
                Spacer(Modifier.width(colonWidth))
                UnitCaption("SEC", pairWidth, unitSize, palette.gold)
            }
        }
    }
}

@Composable
private fun FixedWidthDigits(
    text: String,
    width: Dp,
    style: TextStyle,
) {
    Box(
        modifier = Modifier.width(width),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = style.copy(textAlign = TextAlign.Center),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
        )
    }
}

@Composable
private fun UnitCaption(
    text: String,
    width: Dp,
    size: TextUnit,
    color: Color,
) {
    Box(
        modifier = Modifier.width(width),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = labelStyle(size, color),
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
        )
    }
}

@Composable
private fun DigitPair(
    digits: String,
    unit: String,
    landscape: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = LocalWallPalette.current
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(0.70f)
                .fillMaxWidth()
                .clipToBounds(),
            contentAlignment = Alignment.Center,
        ) {
            val digitSize = fitSp(
                maxWidthDp = maxWidth.value,
                maxHeightDp = maxHeight.value,
                widthChars = if (landscape) 1.45f else 1.20f,
                heightPercent = if (landscape) 0.88f else 0.78f,
                widthPercent = if (landscape) 0.96f else 0.94f,
            )
            Text(
                text = digits,
                style = tabularStyle(
                    color = palette.gold,
                    size = digitSize,
                    weight = FontWeight.Medium,
                    lineHeight = digitSize,
                ).copy(textAlign = TextAlign.Center),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        }
        BoxWithConstraints(
            modifier = Modifier
                .weight(0.30f)
                .fillMaxWidth()
                .clipToBounds(),
            contentAlignment = Alignment.Center,
        ) {
            val unitSp = fitSp(
                maxWidthDp = maxWidth.value,
                maxHeightDp = maxHeight.value,
                widthChars = 3.2f,
                heightPercent = 0.78f,
                widthPercent = 0.90f,
            )
            Text(
                text = unit,
                style = labelStyle(unitSp, palette.gold),
                maxLines = 1,
                overflow = TextOverflow.Clip,
                softWrap = false,
            )
        }
    }
}

@Composable
fun ClockBlock(
    label: String,
    hourMinute: String,
    amPm: String,
    emphasis: ClockEmphasis,
    showStar: Boolean,
    type: TypeScale,
    portrait: Boolean,
    starDiameter: androidx.compose.ui.unit.Dp? = null,
    modifier: Modifier = Modifier,
) {
    val fallbackSize = when (emphasis) {
        ClockEmphasis.Local -> if (portrait) type.albanyPortrait else type.albanyLandscape
        ClockEmphasis.World -> if (portrait) type.jordanPortrait else type.jordanLandscape
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = labelStyle(if (portrait) type.label else type.label * 1.35f),
                maxLines = 1,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (portrait) Modifier.weight(1f) else Modifier.fillMaxHeight()),
            contentAlignment = Alignment.BottomCenter,
        ) {
            ClockDigits(
                hourMinute = hourMinute,
                amPm = amPm,
                fallbackSize = fallbackSize,
                showStar = showStar,
                starDiameter = starDiameter,
                landscape = !portrait,
            )
        }
    }
}

@Composable
private fun ClockDigits(
    hourMinute: String,
    amPm: String,
    fallbackSize: TextUnit,
    showStar: Boolean,
    starDiameter: androidx.compose.ui.unit.Dp?,
    landscape: Boolean,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        val palette = LocalWallPalette.current
        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()
        // Measure "12:00" at a reference size instead of guessing a char-width
        // factor, so a wider font (e.g. a display serif) cannot overflow the clip.
        val clockSize = if (maxWidth.value > 0f && maxHeight.value > 0f) {
            val referenceSize = 100.sp
            val referenceHourMinuteStyle = tabularStyle(
                color = palette.clock,
                size = referenceSize,
                weight = if (landscape) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = if (landscape) (-0.04).em else (-0.02).em,
            )
            val referenceWidth = with(density) {
                textMeasurer.measure("12:00", referenceHourMinuteStyle).size.width.toDp().value
            }
            val availableWidth = (maxWidth.value - 4f).coerceAtLeast(1f)
            val byWidth = if (referenceWidth > 0f) {
                referenceSize.value * availableWidth / referenceWidth
            } else {
                referenceSize.value
            }
            val byHeight = maxHeight.value * if (landscape) 0.90f else 0.86f
            min(byWidth, byHeight).coerceAtLeast(8f).sp
        } else {
            fallbackSize
        }
        if (showStar) {
            val diameter = starDiameter ?: (maxHeight * 0.90f)
            StarWatermark(Modifier.size(diameter))
        }
        val hourMinuteStyle = tabularStyle(
            color = palette.clock,
            size = clockSize,
            weight = if (landscape) FontWeight.Bold else FontWeight.Medium,
            lineHeight = clockSize,
            letterSpacing = if (landscape) (-0.04).em else (-0.02).em,
        )
        // Reserve the widest case ("12:00") so the clock does not slide
        // sideways when the hour flips between one and two digits.
        val hourMinuteWidth = remember(clockSize, landscape, density) {
            with(density) { textMeasurer.measure("12:00", hourMinuteStyle).size.width.toDp() }
        }
        Box(
            modifier = Modifier.width(hourMinuteWidth),
            contentAlignment = if (landscape) Alignment.BottomCenter else Alignment.Center,
        ) {
            Text(
                text = hourMinute,
                style = hourMinuteStyle.copy(textAlign = TextAlign.Center),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
