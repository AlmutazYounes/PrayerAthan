package com.mutazyounes.prayerathan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
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
) {
    if (portrait) {
        PortraitPrayerGrid(cells, type, modifier)
    } else {
        LandscapePrayerGrid(cells, type, modifier)
    }
}

@Composable
private fun LandscapePrayerGrid(
    cells: List<PrayerCellState>,
    type: TypeScale,
    modifier: Modifier,
) {
    Row(modifier.fillMaxSize()) {
        cells.forEach { cell ->
            PrayerCell(
                cell = cell,
                type = type,
                portrait = false,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun PortraitPrayerGrid(
    cells: List<PrayerCellState>,
    type: TypeScale,
    modifier: Modifier,
) {
    val rows = cells.chunked(2)
    Column(modifier.fillMaxSize()) {
        rows.forEach { rowCells ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                PrayerCell(
                    cell = rowCells[0],
                    type = type,
                    portrait = true,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                if (rowCells.size > 1) {
                    PrayerCell(
                        cell = rowCells[1],
                        type = type,
                        portrait = true,
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
) {
    val palette = LocalWallPalette.current
    val textColor = when (cell.kind) {
        CellKind.PAST -> palette.prayerPast
        CellKind.NEXT -> palette.prayerNext
        CellKind.LATER -> palette.clock
    }
    val shape = RoundedCornerShape(HighlightCornerRadius)
    val arcadeSlots = !portrait && palette == DarkWallPalette
    val cellPad = if (portrait) 4.dp else if (arcadeSlots) 0.dp else 2.dp
    val timeFrac = if (portrait) 0.80f else if (arcadeSlots) LandscapeWallLayout.prayerTimeFrac else 0.46f
    val enFrac = if (portrait) 0.18f else if (arcadeSlots) LandscapeWallLayout.prayerNameFrac else 0.16f
    val timeChars = if (portrait) 3.2f else if (arcadeSlots) 2.65f else 2.8f
    val timeTracking = if (portrait) 0.sp else if (arcadeSlots) (-0.03).em else (-0.02).em
    val timeCondenseX = if (portrait) 1f else if (arcadeSlots) 0.98f else 0.98f
    val cellBg = when {
        arcadeSlots -> Color.Transparent
        cell.kind == CellKind.NEXT -> palette.highlightFill
        else -> palette.cellBackground
    }
    BoxWithConstraints(
        modifier = modifier
            .padding(cellPad)
            .then(
                if (!arcadeSlots && cell.kind == CellKind.NEXT) {
                    Modifier.border(HighlightStrokeWidth, palette.highlightStroke, shape)
                } else {
                    Modifier
                },
            )
            .background(cellBg, shape),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val sidePad = if (arcadeSlots) maxWidth * 0.01f else 4.dp
        val topPad = if (arcadeSlots) maxHeight * 0.0f else 8.dp
        val bottomPad = if (arcadeSlots) maxHeight * 0.12f else 4.dp
        val cellHeight = maxHeight
        val iconSizeDp = if (cellHeight.value > 0f) {
            cellHeight.value * if (arcadeSlots) LandscapeWallLayout.prayerWeatherIconFrac else 0.10f
        } else {
            16f
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .padding(start = sidePad, end = sidePad, top = topPad, bottom = bottomPad),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            if (cell.weatherCondition.isNotEmpty()) {
                val night = cell.name == PrayerName.FAJR ||
                    cell.name == PrayerName.MAGHRIB ||
                    cell.name == PrayerName.ISHA
                val iconRes = weatherIconRes(cell.weatherCondition, night = night)
                val iconColor = if (cell.kind == CellKind.NEXT) palette.prayerNext else palette.gold
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(
                            if (arcadeSlots) LandscapeWallLayout.prayerWeatherFrac else 0.10f,
                        ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    val iconSize = iconSizeDp
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = cell.weatherCondition,
                        tint = iconColor,
                        modifier = Modifier.size(iconSize.dp),
                    )
                    if (cell.weatherTempC != null) {
                        Text(
                            text = "${cell.weatherTempC}°C",
                            style = labelStyle(
                                    size = (iconSizeDp * if (arcadeSlots) {
                                        LandscapeWallLayout.prayerWeatherTempScale
                                    } else {
                                        0.62f
                                    }).coerceAtLeast(10f).sp,
                                color = iconColor,
                            ).copy(letterSpacing = 0.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            softWrap = false,
                            modifier = Modifier.padding(start = 4.dp, bottom = 1.dp),
                        )
                    }
                }
            }
            CellLine(
                text = cell.english,
                color = textColor,
                widthChars = if (portrait) 7.5f else if (arcadeSlots) 6.4f else 7.0f,
                kind = CellLineKind.English,
                heightPercent = if (arcadeSlots) 0.92f else 0.88f,
                letterSpacing = 0.sp,
                lineAlign = Alignment.BottomCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(enFrac),
            )
            CellLine(
                text = cell.time,
                color = textColor,
                widthChars = timeChars,
                kind = CellLineKind.Time,
                heightPercent = if (arcadeSlots) 0.98f else 0.96f,
                letterSpacing = timeTracking,
                condenseX = timeCondenseX,
                lineAlign = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(timeFrac),
            )
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
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds(),
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
