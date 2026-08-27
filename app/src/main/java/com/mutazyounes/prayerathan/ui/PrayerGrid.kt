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
    val cellPad = if (portrait) 4.dp else 2.dp
    val timeFrac = if (portrait) 0.66f else 0.70f
    val enFrac = if (portrait) 0.28f else 0.26f
    val timeChars = if (portrait) 5.5f else 4.2f
    val cellBg = if (cell.kind == CellKind.NEXT) palette.highlightFill else palette.cellBackground
    Box(
        modifier = modifier
            .padding(cellPad)
            .then(
                if (cell.kind == CellKind.NEXT) {
                    Modifier.border(HighlightStrokeWidth, palette.highlightStroke, shape)
                } else {
                    Modifier
                },
            )
            .background(cellBg, shape),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CellLine(
                text = cell.english,
                color = textColor,
                widthChars = if (portrait) 8.2f else 7.4f,
                kind = CellLineKind.English,
                heightPercent = 0.78f,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(enFrac),
            )
            CellLine(
                text = cell.time,
                color = textColor,
                widthChars = timeChars,
                kind = CellLineKind.Time,
                heightPercent = 0.90f,
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
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        val size = fitSp(
            maxWidthDp = maxWidth.value,
            maxHeightDp = maxHeight.value,
            widthChars = widthChars,
            heightPercent = heightPercent,
            widthPercent = 0.94f,
        )
        when (kind) {
            CellLineKind.Time -> Text(
                text = text,
                style = tabularStyle(
                    color = color,
                    size = size,
                    weight = FontWeight.Normal,
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
