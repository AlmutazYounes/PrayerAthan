package com.mutazyounes.prayerathan.engine

import java.time.DateTimeException
import java.time.ZoneId

data class SavedLocation(
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
) {
    companion object {
        val albany: SavedLocation = SavedLocation(
            label = "Albany, NY",
            latitude = 42.6526,
            longitude = -73.7562,
            timeZoneId = TimeZones.AMERICA_NEW_YORK,
        )

        fun parse(
            label: String,
            latitude: Double,
            longitude: Double,
            timeZoneId: String,
        ): SavedLocation? {
            if (latitude.isNaN() || longitude.isNaN()) return null
            if (latitude !in -90.0..90.0) return null
            if (longitude !in -180.0..180.0) return null
            val zone = try {
                ZoneId.of(timeZoneId)
            } catch (_: DateTimeException) {
                return null
            }
            return SavedLocation(
                label = label.trim(),
                latitude = latitude,
                longitude = longitude,
                timeZoneId = zone.id,
            )
        }
    }
}
