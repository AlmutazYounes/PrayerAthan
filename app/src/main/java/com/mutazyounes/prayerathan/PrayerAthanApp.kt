package com.mutazyounes.prayerathan

import android.app.Application
import com.mutazyounes.prayerathan.audio.DefaultAthanController
import com.mutazyounes.prayerathan.engine.LocationStore
import com.mutazyounes.prayerathan.engine.PrefsLocationStore
import com.mutazyounes.prayerathan.engine.PrayerCalculator
import com.mutazyounes.prayerathan.engine.PrayerEngine
import com.mutazyounes.prayerathan.audio.AudioSettingsStore
import com.mutazyounes.prayerathan.shell.LocationFixer
import com.mutazyounes.prayerathan.ui.WallSettingsStore
import com.mutazyounes.prayerathan.weather.OpenMeteoWeather
import com.mutazyounes.prayerathan.weather.WeatherClient

class PrayerAthanApp : Application() {
    val locationStore: LocationStore by lazy { PrefsLocationStore(this) }
    val locationFixer: LocationFixer by lazy { LocationFixer(this, locationStore) }
    val prayerEngine: PrayerEngine by lazy { PrayerCalculator(locationStore) }
    val athanController: DefaultAthanController by lazy { DefaultAthanController(this) }
    val wallSettings: WallSettingsStore by lazy { WallSettingsStore(this) }
    val audioSettings: AudioSettingsStore by lazy { AudioSettingsStore(this) }
    val weatherClient: WeatherClient by lazy { OpenMeteoWeather(locationStore) }
}
