package com.mutazyounes.prayerathan.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

private val ATHKAR_PHRASES = listOf(
    "سُبْحَانَ اللهِ وَبِحَمْدِهِ",
    "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ",
    "لَا إِلَهَ إِلَّا اللهُ",
    "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
    "أَسْتَغْفِرُ اللهَ وَأَتُوبُ إِلَيْهِ",
    "سُبْحَانَ اللهِ الْعَظِيمِ",
    "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللهِ",
)

@Composable
fun AthkarTicker(
    type: TypeScale,
    modifier: Modifier = Modifier,
) {
    val palette = LocalWallPalette.current
    val density = LocalDensity.current
    val phrase = ATHKAR_PHRASES.joinToString("    •    ")
    val style = arabicStyle(type.label * 2.0f, palette.gold)

    val measurer = rememberTextMeasurer()
    val layoutResult: TextLayoutResult = remember(phrase, style) {
        measurer.measure(phrase, style)
    }
    val textWidthPx = layoutResult.size.width.toFloat()
    val gapPx = with(density) { 64.dp.toPx() }
    val spanPx = if (textWidthPx > 0f) textWidthPx + gapPx else 1f

    val transition = rememberInfiniteTransition(label = "athkar")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = spanPx,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (spanPx / 0.6f).toInt().coerceAtLeast(6000),
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "athkarScroll",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds(),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (textWidthPx > 0f) {
            val halfGap = with(density) { (gapPx / 2f).toDp() }
            Text(
                text = phrase,
                style = style,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                softWrap = false,
                modifier = Modifier
                    .offset { IntOffset(x = (-offset).toInt(), y = 0) }
                    .padding(horizontal = halfGap),
            )
            Text(
                text = phrase,
                style = style,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                softWrap = false,
                modifier = Modifier
                    .offset { IntOffset(x = (spanPx - offset).toInt(), y = 0) }
                    .padding(horizontal = halfGap),
            )
        }
    }
}
