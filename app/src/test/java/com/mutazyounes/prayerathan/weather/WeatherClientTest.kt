package com.mutazyounes.prayerathan.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherClientTest {
    @Test
    fun parseCurrentCelsiusAndClear() {
        val json = """
            {"current":{"temperature_2m":21.6,"weather_code":0}}
        """.trimIndent()
        val now = OpenMeteoWeather.parse(json)!!
        assertEquals(22, now.temperatureC)
        assertEquals("CLEAR", now.condition)
        assertEquals("22°C  CLEAR", now.line)
    }

    @Test
    fun rainCodes() {
        assertEquals("RAIN", OpenMeteoWeather.conditionLabel(61))
        assertEquals("RAIN", OpenMeteoWeather.conditionLabel(81))
        assertEquals("STORM", OpenMeteoWeather.conditionLabel(95))
        assertEquals("SNOW", OpenMeteoWeather.conditionLabel(71))
    }

    @Test
    fun upcomingRainForecast() {
        val json = """
            {
                "current":{"temperature_2m":18.2,"weather_code":3},
                "minutely_15":{
                    "precipitation":[0.0, 0.4, 1.2, 0.8],
                    "weather_code":[3, 61, 61, 63]
                }
            }
        """.trimIndent()
        val now = OpenMeteoWeather.parse(json)!!
        assertEquals(18, now.temperatureC)
        assertEquals("RAIN SOON", now.condition)
        assertEquals("18°C  RAIN SOON", now.line)
    }

    @Test
    fun currentlyRainingOverridesUpcoming() {
        val json = """
            {
                "current":{"temperature_2m":15.0,"weather_code":61},
                "minutely_15":{
                    "precipitation":[1.5, 1.2, 0.0, 0.0],
                    "weather_code":[61, 61, 3, 3]
                }
            }
        """.trimIndent()
        val now = OpenMeteoWeather.parse(json)!!
        assertEquals(15, now.temperatureC)
        assertEquals("RAIN", now.condition)
        assertEquals("15°C  RAIN", now.line)
    }

    @Test
    fun missingCurrentIsNull() {
        assertNull(OpenMeteoWeather.parse("{}"))
    }
}
