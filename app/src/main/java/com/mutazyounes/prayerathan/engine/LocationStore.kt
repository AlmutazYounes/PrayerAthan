package com.mutazyounes.prayerathan.engine

import android.content.Context

interface LocationStore {
    fun read(): SavedLocation
    fun write(location: SavedLocation)
    fun hasPersistedLocation(): Boolean
}

class InMemoryLocationStore(
    initial: SavedLocation = SavedLocation.albany,
    private var persisted: Boolean = false,
) : LocationStore {
    private var stored: SavedLocation = initial

    override fun read(): SavedLocation = stored

    override fun write(location: SavedLocation) {
        stored = location
        persisted = true
    }

    override fun hasPersistedLocation(): Boolean = persisted
}

class PrefsLocationStore internal constructor(
    private val prefs: LocationPrefsBackend,
) : LocationStore {

    constructor(context: Context) : this(
        SharedPreferencesLocationBackend(context.applicationContext),
    )

    override fun read(): SavedLocation {
        return try {
            if (!prefs.contains(KEY_LATITUDE) ||
                !prefs.contains(KEY_LONGITUDE) ||
                !prefs.contains(KEY_TIMEZONE)
            ) {
                return SavedLocation.albany
            }
            val latitude = prefs.getString(KEY_LATITUDE)?.toDoubleOrNull()
                ?: return SavedLocation.albany
            val longitude = prefs.getString(KEY_LONGITUDE)?.toDoubleOrNull()
                ?: return SavedLocation.albany
            val timeZoneId = prefs.getString(KEY_TIMEZONE)
                ?: return SavedLocation.albany
            val label = prefs.getString(KEY_LABEL).orEmpty()
            SavedLocation.parse(label, latitude, longitude, timeZoneId)
                ?: SavedLocation.albany
        } catch (_: Exception) {
            SavedLocation.albany
        }
    }

    override fun hasPersistedLocation(): Boolean {
        return prefs.contains(KEY_LATITUDE) &&
            prefs.contains(KEY_LONGITUDE) &&
            prefs.contains(KEY_TIMEZONE)
    }

    override fun write(location: SavedLocation) {
        prefs.write(
            mapOf(
                KEY_LABEL to location.label,
                KEY_LATITUDE to location.latitude.toString(),
                KEY_LONGITUDE to location.longitude.toString(),
                KEY_TIMEZONE to location.timeZoneId,
            ),
        )
    }

    companion object {
        internal const val PREFS = "prayerathan_location"
        internal const val KEY_LABEL = "label"
        internal const val KEY_LATITUDE = "latitude"
        internal const val KEY_LONGITUDE = "longitude"
        internal const val KEY_TIMEZONE = "timeZoneId"
    }
}

internal interface LocationPrefsBackend {
    fun getString(key: String): String?
    fun contains(key: String): Boolean
    fun write(values: Map<String, String>)
}

private class SharedPreferencesLocationBackend(
    context: Context,
) : LocationPrefsBackend {
    private val prefs = context.getSharedPreferences(
        PrefsLocationStore.PREFS,
        Context.MODE_PRIVATE,
    )

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun contains(key: String): Boolean = prefs.contains(key)

    override fun write(values: Map<String, String>) {
        prefs.edit().apply {
            values.forEach { (key, value) -> putString(key, value) }
            apply()
        }
    }
}
