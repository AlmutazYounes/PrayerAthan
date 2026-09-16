package com.mutazyounes.prayerathan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MedicineReminderBanner(
    primary: String,
    secondary: String,
    type: TypeScale,
    modifier: Modifier = Modifier,
) {
    if (primary.isEmpty()) return
    val palette = LocalWallPalette.current
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(palette.settingsPanel.copy(alpha = 0.92f), shape)
            .border(1.5.dp, palette.gold.copy(alpha = 0.55f), shape)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "MEDICINE",
            style = labelStyle((type.label.value * 0.85f).sp, palette.gold),
            maxLines = 1,
        )
        val primaryIsArabic = primary.any { it in '\u0600'..'\u06FF' }
        Text(
            text = primary,
            style = if (primaryIsArabic) {
                arabicStyle(type.label * 2.1f, palette.clock)
            } else {
                labelStyle(type.label * 1.55f, palette.clock).copy(fontWeight = FontWeight.Bold)
            },
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (secondary.isNotEmpty()) {
            val secondaryIsArabic = secondary.any { it in '\u0600'..'\u06FF' }
            Text(
                text = secondary,
                style = if (secondaryIsArabic) {
                    arabicStyle(type.label * 1.15f, palette.gold)
                } else {
                    labelStyle(type.label * 0.95f, palette.gold)
                },
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
