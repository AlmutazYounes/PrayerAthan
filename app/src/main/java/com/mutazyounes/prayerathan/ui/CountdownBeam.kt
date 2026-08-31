package com.mutazyounes.prayerathan.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlin.math.min

/**
 * Full-width countdown band above the prayer row.
 *
 * The countdown digits and next-prayer label sit centered. Beneath them a faint
 * gold horizon line spans the screen. A single luminous star drifts along it
 * from the previous prayer toward the next prayer as time passes, closing in
 * on the upcoming prayer. The star breathes with a slow pulse so the wall
 * feels alive without resorting to a progress bar.
 */
@Composable
fun CountdownBeam(
    cells: List<PrayerCellState>,
    countdown: String,
    progress: Float,
    nextLabel: String,
    type: TypeScale,
    portrait: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = LocalWallPalette.current
    val density = LocalDensity.current
    val count = cells.size.coerceAtLeast(1)
    val nextIndex = cells.indexOfFirst { it.kind == CellKind.NEXT }.coerceAtLeast(0)
    val prevIndex = nextIndex - 1
    val isNext = cells.getOrNull(nextIndex)?.kind == CellKind.NEXT

    val pulse = rememberInfiniteTransition(label = "star-pulse")
    val breath by pulse.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breath",
    )

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        fun tickX(i: Int): Float = (i + 0.5f) / count * widthPx
        val nextX = tickX(nextIndex)
        val prevX = if (prevIndex >= 0) tickX(prevIndex) else 0f
        val dotX = if (isNext) lerp(prevX, nextX, progress.coerceIn(0f, 1f)) else nextX

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    val labelSize = (type.label.value * 1.15f).coerceAtLeast(10f).sp
                    androidx.compose.material3.Text(
                        text = nextLabel,
                        style = labelStyle(labelSize, color = palette.goldDim),
                        maxLines = 1,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row {
                    val digitSize = min(heightPx * 0.40f, widthPx * 0.11f).sp
                    androidx.compose.material3.Text(
                        text = countdown,
                        style = tabularStyle(
                            color = palette.gold,
                            size = digitSize,
                            weight = FontWeight.Bold,
                            lineHeight = digitSize,
                            letterSpacing = (-0.03).sp,
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
            ) {
                val cy = size.height / 2f
                val hair = palette.hairline
                val gold = palette.gold
                val hairWidth = with(density) { 1.dp.toPx() }

                drawLine(
                    color = hair,
                    start = Offset(0f, cy),
                    end = Offset(size.width, cy),
                    strokeWidth = hairWidth,
                    cap = StrokeCap.Round,
                )

                val core = with(density) { 3.2.dp.toPx() } * breath
                drawCircle(color = gold.copy(alpha = 0.10f), radius = core * 4.5f, center = Offset(dotX, cy))
                drawCircle(color = gold.copy(alpha = 0.20f), radius = core * 2.6f, center = Offset(dotX, cy))
                drawCircle(color = gold.copy(alpha = 0.55f), radius = core * 1.5f, center = Offset(dotX, cy))
                drawCircle(color = gold, radius = core, center = Offset(dotX, cy))
            }
        }
    }
}
