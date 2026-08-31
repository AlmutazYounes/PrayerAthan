package com.mutazyounes.prayerathan.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.mutazyounes.prayerathan.R
import kotlin.math.min

data class WallPalette(
    val background: Color,
    val backgroundLift: Color,
    val backgroundDeep: Color,
    val settingsPanel: Color,
    val clock: Color,
    val gold: Color,
    val goldDim: Color,
    val prayerPast: Color,
    val prayerNext: Color,
    val hairline: Color,
    val star: Color,
    val geometry: Color,
) {
    val highlightStroke: Color get() = Color(0xFFFFD54F)
    val highlightFill: Color get() = Color(0x22FFD54F)
    val cellBackground: Color get() = Color.Transparent
    val date: Color get() = clock
    val location: Color get() = clock
    val label: Color get() = gold
    val amPm: Color get() = clock
}

val DarkWallPalette = WallPalette(
    background = Color(0xFF0C1014),
    backgroundLift = Color(0xFF16202A),
    backgroundDeep = Color(0xFF080C10),
    settingsPanel = Color(0xFF121A22),
    clock = Color(0xFFFFFFFF),
    gold = Color(0xFFFFD54F),
    goldDim = Color(0xFFFFE082),
    prayerPast = Color(0xFFB0BEC5),
    prayerNext = Color(0xFFEAE0C8),
    hairline = Color(0x66FFFFFF),
    star = Color(0xFFFFD54F).copy(alpha = 0.15f),
    geometry = Color(0xFFFFD54F).copy(alpha = 0.12f),
)

val LocalWallPalette = staticCompositionLocalOf { DarkWallPalette }

val HairlineWidth = 1.5.dp
val HighlightStrokeWidth = 1.5.dp
val HighlightCornerRadius = 16.dp
val ScreenInset = 16.dp

/**
 * Landscape wall knobs. Each field is independent.
 * Change clock height and the countdown does not move.
 * Change countdownFromTop and the clock does not resize.
 * Prayer cell fractions only affect the prayer row.
 *
 * Heights and fromTop values are fractions of the screen height.
 */
object LandscapeWallLayout {
    val clockHeight = 0.28f
    val weatherHeight = 0.28f
    val heroRowFromTop = 0.16f
    val countdownHeight = 0.13f
    val countdownFromTop = 0.50f
    val countdownWidth = 0.46f
    val countdownFromStart = 0.52f
    val prayerHeight = 0.50f
    val prayerTimeFrac = 0.42f
    val prayerNameFrac = 0.18f
    val prayerWeatherFrac = 0.15f
    val prayerWeatherIconFrac = 0.13f
    val prayerWeatherTempScale = 0.70f
}

@OptIn(ExperimentalTextApi::class)
val EnglishFontFamily = FontFamily(
    Font(
        R.font.cinzel,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.cinzel,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.cinzel,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

val ArabicFontFamily = FontFamily(
    Font(R.font.noto_naskh_arabic, weight = FontWeight.Normal),
)

data class TypeScale(
    val albanyLandscape: TextUnit,
    val albanyPortrait: TextUnit,
    val jordanLandscape: TextUnit,
    val jordanPortrait: TextUnit,
    val countdownLandscape: TextUnit,
    val countdownPortrait: TextUnit,
    val dateLine: TextUnit,
    val weekday: TextUnit,
    val location: TextUnit,
    val label: TextUnit,
    val units: TextUnit,
    val prayerEn: TextUnit,
    val prayerAr: TextUnit,
    val prayerTime: TextUnit,
)

fun typeScale(shortestSideDp: Float) = TypeScale(
    albanyLandscape = (shortestSideDp * 0.10f).sp,
    albanyPortrait = (shortestSideDp * 0.11f).sp,
    jordanLandscape = (shortestSideDp * 0.10f).sp,
    jordanPortrait = (shortestSideDp * 0.09f).sp,
    countdownLandscape = (shortestSideDp * 0.08f).sp,
    countdownPortrait = (shortestSideDp * 0.09f).sp,
    dateLine = (shortestSideDp * 0.050f).sp,
    weekday = (shortestSideDp * 0.034f).sp,
    location = (shortestSideDp * 0.020f).sp,
    label = (shortestSideDp * 0.020f).sp,
    units = (shortestSideDp * 0.016f).sp,
    prayerEn = (shortestSideDp * 0.040f).sp,
    prayerAr = (shortestSideDp * 0.032f).sp,
    prayerTime = (shortestSideDp * 0.048f).sp,
)

fun fitSp(
    maxWidthDp: Float,
    maxHeightDp: Float,
    widthChars: Float,
    heightPercent: Float,
    widthPercent: Float = 1f,
): TextUnit {
    if (
        !maxWidthDp.isFinite() ||
        !maxHeightDp.isFinite() ||
        maxWidthDp <= 0f ||
        maxHeightDp <= 0f
    ) {
        return 12.sp
    }
    val byWidth = (maxWidthDp * widthPercent.coerceIn(0.05f, 1f)) /
        widthChars.coerceAtLeast(1f)
    val byHeight = maxHeightDp * heightPercent.coerceIn(0.05f, 1f)
    return min(byWidth, byHeight).coerceAtLeast(8f).sp
}

fun screenInset(shortestSideDp: Float): Dp =
    (shortestSideDp * 0.04f).dp.coerceAtLeast(ScreenInset)

fun tabularStyle(
    color: Color,
    size: TextUnit,
    weight: FontWeight = FontWeight.Medium,
    fontFamily: FontFamily = EnglishFontFamily,
    letterSpacing: TextUnit = 0.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
): TextStyle = TextStyle(
    color = color,
    fontSize = size,
    fontWeight = weight,
    fontFamily = fontFamily,
    letterSpacing = letterSpacing,
    lineHeight = lineHeight,
    fontFeatureSettings = "tnum, lnum",
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

@Composable
@ReadOnlyComposable
fun labelStyle(size: TextUnit, color: Color = LocalWallPalette.current.label): TextStyle = TextStyle(
    color = color,
    fontSize = size,
    lineHeight = size,
    fontWeight = FontWeight.Medium,
    fontFamily = EnglishFontFamily,
    letterSpacing = 0.12.em,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

fun arabicStyle(size: TextUnit, color: Color): TextStyle = TextStyle(
    color = color,
    fontSize = size,
    lineHeight = size,
    fontWeight = FontWeight.Normal,
    fontFamily = ArabicFontFamily,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)
