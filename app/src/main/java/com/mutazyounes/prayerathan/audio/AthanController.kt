package com.mutazyounes.prayerathan.audio

import android.content.Context
import com.mutazyounes.prayerathan.PrayerAthanApp
import com.mutazyounes.prayerathan.engine.PrayerDay
import com.mutazyounes.prayerathan.engine.PrayerName
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AthanPlayback(
    val prayer: PrayerName,
    val startedAt: Instant,
)

data class AthkarPlayback(
    val caption: String,
    val startedAt: Instant,
)

data class MedicinePlayback(
    val primary: String,
    val secondary: String,
    val startedAt: Instant,
)

interface AthanController {
    fun schedule(day: PrayerDay, now: Instant)
    fun stop()
    fun playAthanDemo(
        soundId: String,
        volumePercent: Int = AthanVolume.DEFAULT,
        demoKey: String = soundId,
    )
    fun playAthkarDemo(clip: AthkarClip)
    fun playMedicineDemo()
    val playback: StateFlow<AthanPlayback?>
    val athkarPlayback: StateFlow<AthkarPlayback?>
    val medicinePlayback: StateFlow<MedicinePlayback?>
    val demoId: StateFlow<String?>
}

class DefaultAthanController(
    context: Context,
) : AthanController {

    private val appContext = context.applicationContext
    private val scheduler = AthanScheduler(appContext)
    private val athkarScheduler = AthkarScheduler(appContext)
    private val morningAthkarScheduler = MorningAthkarScheduler(appContext)
    private val medicineScheduler = MedicineScheduler(appContext)
    private val audioSettings = AudioSettingsStore(appContext)
    private val medicineSettings = MedicineSettingsStore(appContext)
    private val _playback = MutableStateFlow<AthanPlayback?>(null)
    private val _athkarPlayback = MutableStateFlow<AthkarPlayback?>(null)
    private val _medicinePlayback = MutableStateFlow<MedicinePlayback?>(null)
    private val _demoId = MutableStateFlow<String?>(null)

    override val playback: StateFlow<AthanPlayback?> = _playback.asStateFlow()
    override val athkarPlayback: StateFlow<AthkarPlayback?> = _athkarPlayback.asStateFlow()
    override val medicinePlayback: StateFlow<MedicinePlayback?> = _medicinePlayback.asStateFlow()
    override val demoId: StateFlow<String?> = _demoId.asStateFlow()

    override fun schedule(day: PrayerDay, now: Instant) {
        scheduler.schedule(day, now)
        if (audioSettings.athkarEnabled()) {
            athkarScheduler.schedule(day, now, zone())
        } else {
            athkarScheduler.cancelAll()
        }
        if (audioSettings.morningAthkarEnabled()) {
            morningAthkarScheduler.schedule(day, now, zone())
        } else {
            morningAthkarScheduler.cancelAll()
        }
        if (medicineSettings.enabled() && medicineSettings.slots().isNotEmpty()) {
            medicineScheduler.schedule(now, zone())
        } else {
            medicineScheduler.cancelAll()
        }
    }

    override fun stop() {
        val athanOn = _playback.value != null
        val athkarOn = _athkarPlayback.value != null
        val medicineOn = _medicinePlayback.value != null
        val demoOn = _demoId.value != null
        markIdle()
        markAthkarIdle()
        markMedicineIdle()
        markDemo(null)
        if (athanOn || demoOn) {
            try {
                appContext.startService(AthanService.stopIntent(appContext))
            } catch (_: IllegalStateException) {
            }
        }
        if (athkarOn || demoOn) {
            try {
                appContext.startService(AthkarService.stopIntent(appContext))
            } catch (_: IllegalStateException) {
            }
        }
        if (medicineOn || demoOn) {
            try {
                appContext.startService(MedicineService.stopIntent(appContext))
            } catch (_: IllegalStateException) {
            }
        }
    }

    override fun playAthanDemo(
        soundId: String,
        volumePercent: Int,
        demoKey: String,
    ) {
        stopAthkar()
        stopMedicine()
        markIdle()
        markDemo(demoKey)
        appContext.startForegroundService(
            AthanService.demoIntent(appContext, soundId, volumePercent, demoKey),
        )
    }

    override fun playAthkarDemo(clip: AthkarClip) {
        stopMedicine()
        try {
            appContext.startService(AthanService.stopIntent(appContext))
        } catch (_: IllegalStateException) {
        }
        markIdle()
        markDemo(AthkarService.demoId(clip))
        appContext.startForegroundService(AthkarService.demoIntent(appContext, clip))
    }

    override fun playMedicineDemo() {
        stopAthkar()
        try {
            appContext.startService(AthanService.stopIntent(appContext))
        } catch (_: IllegalStateException) {
        }
        markIdle()
        markDemo(MedicineService.DEMO_ID)
        appContext.startForegroundService(MedicineService.demoIntent(appContext))
    }

    fun stopAthkar() {
        val athkarOn = _athkarPlayback.value != null
        markAthkarIdle()
        if (!athkarOn) return
        try {
            appContext.startService(AthkarService.stopIntent(appContext))
        } catch (_: IllegalStateException) {
        }
    }

    fun stopMedicine() {
        val medicineOn = _medicinePlayback.value != null
        markMedicineIdle()
        if (!medicineOn) return
        try {
            appContext.startService(MedicineService.stopIntent(appContext))
        } catch (_: IllegalStateException) {
        }
    }

    fun markPlaying(prayer: PrayerName, startedAt: Instant) {
        if (prayer == PrayerName.SUNRISE) return
        stopMedicine()
        _playback.value = AthanPlayback(prayer, startedAt)
    }

    fun markIdle() {
        _playback.value = null
    }

    fun markAthkarPlaying(caption: String, startedAt: Instant) {
        _athkarPlayback.value = AthkarPlayback(caption, startedAt)
    }

    fun markAthkarIdle() {
        _athkarPlayback.value = null
    }

    fun markMedicinePlaying(primary: String, secondary: String, startedAt: Instant) {
        _medicinePlayback.value = MedicinePlayback(primary, secondary, startedAt)
    }

    fun markMedicineIdle() {
        _medicinePlayback.value = null
    }

    fun markDemo(id: String?) {
        _demoId.value = id
    }

    private fun zone(): ZoneId {
        val app = appContext as PrayerAthanApp
        return ZoneId.of(app.prayerEngine.location().timeZoneId)
    }
}
