package com.mutazyounes.prayerathan.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun AthkarCaption(
    caption: String,
    type: TypeScale,
    modifier: Modifier = Modifier,
) {
    if (caption.isEmpty()) return
    val palette = LocalWallPalette.current
    Text(
        text = caption,
        style = arabicStyle(type.label * 1.7f, palette.gold),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
