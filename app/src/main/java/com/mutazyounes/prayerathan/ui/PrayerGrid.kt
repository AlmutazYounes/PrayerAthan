package com.mutazyounes.prayerathan.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.mutazyounes.prayerathan.engine.PrayerName

@Composable
fun PrayerGrid(
    cells: List<PrayerCellState>,
    portrait: Boolean,
    type: TypeScale,
    modifier: Modifier = Modifier,
    countdown: String = "00:00:00",
    athanPlaying: Boolean = false,
    playingName: String? = null,
) {
    if (portrait) {
        PortraitPrayerGrid(
            cells = cells,
            type = type,
            countdown = countdown,
            athanPlaying = athanPlaying,
            playingName = playingName,
            modifier = modifier,
        )
    } else {
        LandscapePrayerGrid(
            cells = cells,
            type = type,
            countdown = countdown,
            athanPlaying = athanPlaying,
            playingName = playingName,
            modifier = modifier,
        )
    }
}

@Composable
private fun LandscapePrayerGrid(
    cells: List<PrayerCellState>,
    type: TypeScale,
    countdown: String,
    athanPlaying: Boolean,
    playingName: String?,
    modifier: Modifier,
) {
    val rows = cells.chunked(3)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
    ) {
        rows.forEachIndexed { index, rowCells ->
            if (index > 0) {
                HorizontalHairline(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                rowCells.forEach { cell ->
                    val isNext = cell.kind == CellKind.NEXT
                    val weight = if (isNext) NextCellWeight else NormalCellWeight
                    PrayerCell(
                        cell = cell,
                        type = type,
                        portrait = false,
                        countdown = countdown,
                        athanPlaying = athanPlaying,
                        playingName = playingName,
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxHeight(),
                    )
                }
                repeat(3 - rowCells.size) {
                    Box(
                        modifier = Modifier
                            .weight(NormalCellWeight)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@Composable
private fun PortraitPrayerGrid(
    cells: List<PrayerCellState>,
    type: TypeScale,
    countdown: String,
    athanPlaying: Boolean,
    playingName: String?,
    modifier: Modifier,
) {
    val rows = cells.chunked(2)
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        rows.forEachIndexed { index, rowCells ->
            if (index > 0) {
                HorizontalHairline(
                    modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp),
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                rowCells.forEach { cell ->
                    PrayerCell(
                        cell = cell,
                        type = type,
                        portrait = true,
                        countdown = countdown,
                        athanPlaying = athanPlaying,
                        playingName = playingName,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@Composable
fun PrayerCell(
    cell: PrayerCellState,
    type: TypeScale,
    portrait: Boolean,
    modifier: Modifier = Modifier,
    countdown: String = "00:00:00",
    athanPlaying: Boolean = false,
    playingName: String? = null,
) {
    val palette = LocalWallPalette.current
    val isNext = cell.kind == CellKind.NEXT

    val nameColor = palette.gold
    val timeColor = when (cell.kind) {
        CellKind.PAST -> palette.prayerPast
        CellKind.NEXT -> palette.clock
        CellKind.LATER -> palette.clock
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val cellHeight = maxHeight
        val isLandscape = !portrait
        val iconFrac = when {
            isLandscape -> 0.12f
            else -> 0.22f
        }
        val iconSizeDp = if (cellHeight.value > 0f) {
            (cellHeight.value * iconFrac).coerceIn(14f, if (isLandscape) 26f else 48f)
        } else {
            16f
        }
        val night = cell.name == PrayerName.FAJR ||
            cell.name == PrayerName.MAGHRIB ||
            cell.name == PrayerName.ISHA
        val hasWeather = cell.weatherCondition.isNotEmpty()

        @Composable
        fun WeatherRow(modifier: Modifier = Modifier) {
            if (!hasWeather) return
            val iconRes = weatherIconRes(cell.weatherCondition, night = night)
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = cell.weatherCondition,
                    tint = palette.gold,
                    modifier = Modifier.size(iconSizeDp.dp),
                )
                if (cell.weatherTempC != null) {
                    Text(
                        text = "${cell.weatherTempC}°C",
                        style = labelStyle(
                            size = (iconSizeDp * if (isLandscape) 0.95f else 0.88f).coerceAtLeast(12f).sp,
                            color = palette.gold,
                        ).copy(letterSpacing = 0.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        softWrap = false,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            if (isLandscape) {
                // Name + weather on one line; prayer time stays large below.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.85f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CellLine(
                        text = cell.english,
                        color = nameColor,
                        widthChars = cell.english.length.coerceAtLeast(4) * 0.78f,
                        kind = CellLineKind.English,
                        heightPercent = 0.90f,
                        letterSpacing = 0.04.em,
                        lineAlign = Alignment.Center,
                        modifier = Modifier.fillMaxHeight(),
                    )
                    if (hasWeather) {
                        Spacer(modifier = Modifier.width(8.dp))
                        WeatherRow(Modifier.fillMaxHeight())
                    }
                }
                CellLine(
                    text = cell.time,
                    color = timeColor,
                    widthChars = 2.4f,
                    kind = CellLineKind.Time,
                    heightPercent = 0.98f,
                    letterSpacing = (-0.02).em,
                    condenseX = 0.98f,
                    lineAlign = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.45f),
                )
            } else {
                CellLine(
                    text = cell.english,
                    color = nameColor,
                    widthChars = if (isNext) 10.0f else 8.0f,
                    kind = CellLineKind.English,
                    heightPercent = 0.58f,
                    letterSpacing = 0.10.em,
                    lineAlign = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.48f),
                )
                CellLine(
                    text = cell.time,
                    color = timeColor,
                    widthChars = 2.8f,
                    kind = CellLineKind.Time,
                    heightPercent = 0.88f,
                    letterSpacing = (-0.02).em,
                    condenseX = 0.98f,
                    lineAlign = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.95f),
                )
            }
        }
    }
}

private enum class CellLineKind { English, Time }

@Composable
private fun CellLine(
    text: String,
    color: Color,
    widthChars: Float,
    kind: CellLineKind,
    heightPercent: Float,
    letterSpacing: androidx.compose.ui.unit.TextUnit = 0.sp,
    condenseX: Float = 1f,
    lineAlign: Alignment = Alignment.Center,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.clipToBounds(),
        contentAlignment = lineAlign,
    ) {
        val size = fitSp(
            maxWidthDp = maxWidth.value,
            maxHeightDp = maxHeight.value,
            widthChars = widthChars,
            heightPercent = heightPercent,
            widthPercent = 1f,
        )
        when (kind) {
            CellLineKind.Time -> Text(
                text = text,
                style = tabularStyle(
                    color = color,
                    size = size,
                    weight = FontWeight.Bold,
                    letterSpacing = letterSpacing,
                    lineHeight = size,
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                softWrap = false,
                modifier = Modifier.scale(condenseX, 1f),
            )
            CellLineKind.English -> Text(
                text = text,
                style = labelStyle(size, color).copy(
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = size,
                ),
                maxLines = 1,
                overflow = TextOverflow.Clip,
                softWrap = false,
            )
        }
    }
}

@Composable
fun HorizontalHairline(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(HairlineWidth)
            .background(LocalWallPalette.current.hairline),
    )
}

@Composable
fun VerticalHairline(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxHeight()
            .width(HairlineWidth)
            .background(LocalWallPalette.current.hairline),
    )
}
