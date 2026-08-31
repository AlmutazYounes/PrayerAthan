package com.mutazyounes.prayerathan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

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
        CellKind.NEXT -> palette.gold
        CellKind.LATER -> palette.clock
    }
    val shape = RoundedCornerShape(HighlightCornerRadius)
    val arcadeSlots = !portrait && palette == DarkWallPalette
    val cellPad = if (portrait) 4.dp else if (arcadeSlots) 0.dp else 2.dp
    val timeFrac = if (portrait) 0.76f else if (arcadeSlots) 0.88f else 0.82f
    val enFrac = if (portrait) 0.20f else if (arcadeSlots) 0.16f else 0.16f
    val timeChars = if (portrait) 3.6f else if (arcadeSlots) 3.0f else 3.4f
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
        contentAlignment = if (arcadeSlots) Alignment.BottomCenter else Alignment.Center,
    ) {
        val sidePad = if (arcadeSlots) maxWidth * 0.01f else 4.dp
        val topPad = if (arcadeSlots) maxHeight * 0.04f else 2.dp
        val bottomPad = if (arcadeSlots) maxHeight * 0.02f else 2.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .padding(start = sidePad, end = sidePad, top = topPad, bottom = bottomPad),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (arcadeSlots) Arrangement.Bottom else Arrangement.Center,
        ) {
            CellLine(
                text = cell.english,
                color = textColor,
                widthChars = if (portrait) 7.5f else if (arcadeSlots) 6.4f else 7.0f,
                kind = CellLineKind.English,
                heightPercent = if (arcadeSlots) 0.85f else 0.80f,
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
                heightPercent = if (arcadeSlots) 0.98f else 0.94f,
                letterSpacing = 0.sp,
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
