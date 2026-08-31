package com.mutazyounes.prayerathan.weather

import com.mutazyounes.prayerathan.engine.LocationStore
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

data class WeatherNow(
    val temperatureC: Int,
    val condition: String,
    val weatherCondition: String = condition.removeSuffix(" SOON").trim(),
) {
    val line: String get() = "$temperatureC°C  $condition"
}

interface WeatherClient {
    fun fetch(): WeatherNow?
}

class OpenMeteoWeather(
    private val locations: LocationStore,
) : WeatherClient {
    override fun fetch(): WeatherNow? {
        val loc = locations.read()
        val url = URL(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=${loc.latitude}" +
                "&longitude=${loc.longitude}" +
                "&current=temperature_2m,weather_code,precipitation" +
                "&minutely_15=precipitation,weather_code" +
                "&forecast_minutely_15=4" +
                "&temperature_unit=celsius",
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "PrayerAthan/0.1 (wall clock)")
        }
        return try {
            if (connection.responseCode != 200) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            parse(body)
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        fun parse(body: String): WeatherNow? {
            val root = JSONObject(body)
            val current = root.optJSONObject("current") ?: return null
            if (!current.has("temperature_2m") || !current.has("weather_code")) return null
            val temp = current.getDouble("temperature_2m")
            val code = current.getInt("weather_code")
            var condition = conditionLabel(code)

            // When currently dry/cloudy, check near-term forecast for incoming precipitation
            if (condition in setOf("CLEAR", "FAIR", "CLOUD", "FOG")) {
                val minutely = root.optJSONObject("minutely_15")
                val precipArray = minutely?.optJSONArray("precipitation")
                val codeArray = minutely?.optJSONArray("weather_code")
                if (precipArray != null && codeArray != null) {
                    val count = minOf(precipArray.length(), codeArray.length())
                    for (i in 0 until count) {
                        val p = precipArray.optDouble(i, 0.0)
                        val c = codeArray.optInt(i, -1)
                        if (p > 0.0 || c in 51..99) {
                            val upcoming = conditionLabel(c)
                            if (upcoming in setOf("RAIN", "SNOW", "STORM", "DRIZZLE")) {
                                condition = "$upcoming SOON"
                                break
                            }
                        }
                    }
                }
            }

            return WeatherNow(
                temperatureC = kotlin.math.round(temp).toInt(),
                condition = condition,
            )
        }

        fun conditionLabel(code: Int): String = when (code) {
            0 -> "CLEAR"
            1, 2 -> "FAIR"
            3 -> "CLOUD"
            45, 48 -> "FOG"
            in 51..57 -> "DRIZZLE"
            in 61..67, in 80..82 -> "RAIN"
            in 71..77, in 85..86 -> "SNOW"
            in 95..99 -> "STORM"
            else -> "CLOUD"
        }
    }
}
