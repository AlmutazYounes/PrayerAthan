package com.mutazyounes.prayerathan.ui

import androidx.annotation.DrawableRes
import com.mutazyounes.prayerathan.R

@DrawableRes
fun weatherIconRes(condition: String): Int {
    val upper = condition.uppercase()
    return when {
        "DRIZZLE" in upper -> R.drawable.ic_weather_drizzle
        "RAIN" in upper -> R.drawable.ic_weather_rain
        "SNOW" in upper -> R.drawable.ic_weather_snow
        "STORM" in upper -> R.drawable.ic_weather_storm
        "FOG" in upper -> R.drawable.ic_weather_fog
        "CLEAR" in upper -> R.drawable.ic_weather_clear
        "FAIR" in upper -> R.drawable.ic_weather_fair
        else -> R.drawable.ic_weather_cloud
    }
}
