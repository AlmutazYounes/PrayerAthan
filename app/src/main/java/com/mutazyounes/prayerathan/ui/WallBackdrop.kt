package com.mutazyounes.prayerathan.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.mutazyounes.prayerathan.R

@Composable
fun WallBackdrop(
    themeMode: ThemeMode,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    portrait: Boolean = false,
) {
    val palette = LocalWallPalette.current
    Box(
        modifier
            .fillMaxSize()
            .background(palette.backgroundDeep),
    ) {
        val isDark = themeMode == ThemeMode.DARK || (themeMode == ThemeMode.AUTO && darkTheme)
        val resId = if (isDark) {
            if (portrait) R.drawable.wall_backdrop_dark_portrait else R.drawable.wall_backdrop_dark
        } else {
            if (portrait) R.drawable.wall_backdrop_light_portrait else R.drawable.wall_backdrop_light
        }
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize(),
        )
        if (isDark) {
            // Soft overlay to maintain contrast without hiding the embedded mosque arches
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color(0x1A000000),
                            0.60f to Color(0x33000000),
                            1.0f to Color(0x66000000),
                        ),
                    ),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color(0x11FFFFFF),
                            0.45f to Color(0x22FFFFFF),
                            0.65f to Color(0x66FFFFFF),
                            1.0f to Color(0xAAFFFFFF),
                        ),
                    ),
            )
        }
    }
}
