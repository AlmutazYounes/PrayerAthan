package com.mutazyounes.prayerathan.ui

import com.mutazyounes.prayerathan.R
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherIconsTest {
    @Test
    fun mapsConditionsToDrawables() {
        assertEquals(R.drawable.ic_weather_clear, weatherIconRes("CLEAR"))
        assertEquals(R.drawable.ic_weather_fair, weatherIconRes("FAIR"))
        assertEquals(R.drawable.ic_weather_cloud, weatherIconRes("CLOUD"))
        assertEquals(R.drawable.ic_weather_fog, weatherIconRes("FOG"))
        assertEquals(R.drawable.ic_weather_drizzle, weatherIconRes("DRIZZLE"))
        assertEquals(R.drawable.ic_weather_rain, weatherIconRes("RAIN"))
        assertEquals(R.drawable.ic_weather_rain, weatherIconRes("RAIN SOON"))
        assertEquals(R.drawable.ic_weather_snow, weatherIconRes("SNOW"))
        assertEquals(R.drawable.ic_weather_snow, weatherIconRes("SNOW SOON"))
        assertEquals(R.drawable.ic_weather_storm, weatherIconRes("STORM"))
        assertEquals(R.drawable.ic_weather_storm, weatherIconRes("STORM SOON"))
        assertEquals(R.drawable.ic_weather_cloud, weatherIconRes("UNKNOWN"))
    }

    @Test
    fun nightUsesMoonForClearAndFair() {
        assertEquals(R.drawable.ic_weather_clear_night, weatherIconRes("CLEAR", night = true))
        assertEquals(R.drawable.ic_weather_fair_night, weatherIconRes("FAIR", night = true))
        assertEquals(R.drawable.ic_weather_rain, weatherIconRes("RAIN", night = true))
    }
}
