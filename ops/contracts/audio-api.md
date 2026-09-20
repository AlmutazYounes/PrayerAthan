# Audio API

Athan audio owns this. Shell starts it. UI observes it and can stop it. Engine is read-only input.

Package: `com.mutazyounes.prayerathan.audio`

Files after shell copies them:

- `res/raw/athan_saudi.mp3`
- `res/raw/athkar_salawat.mp3`
- `res/raw/medicine_ar.mp3`
- `res/raw/medicine_en.mp3`

Athan file is `res/raw/athan_saudi.mp3`. Athkar masters live in `audio/athkar/`. Medicine masters live in `audio/medicine/`. See `audio/SOURCE.md`, `audio/athkar/SOURCE.md`, and `audio/medicine/SOURCE.md`.

## Types

```kotlin
data class AthanPlayback(
    val prayer: PrayerName,     // never SUNRISE
    val startedAt: Instant,
)

data class AthkarPlayback(
    val caption: String,        // Arabic line on the wall
    val startedAt: Instant,
)

data class MedicinePlayback(
    val primary: String,
    val secondary: String,
    val startedAt: Instant,
)

enum class MedicineVoice { ARABIC, ENGLISH }

data class MedicineSlot(
    val id: String,
    val hour: Int,              // 0-23
    val minute: Int,            // 0-59
    val days: Set<DayOfWeek>,   // non-empty
)
```

## Functions

```kotlin
interface AthanController {
    fun schedule(day: PrayerDay, now: Instant)
    fun stop()                  // tap during athan, athkar, or medicine
    fun playAthanDemo(soundId: String, volumePercent: Int = 100, demoKey: String = soundId)
    fun playAthkarDemo(clip: AthkarClip)
    fun playMedicineDemo()
    val playback: StateFlow<AthanPlayback?>  // null = idle
    val athkarPlayback: StateFlow<AthkarPlayback?>
    val medicinePlayback: StateFlow<MedicinePlayback?>
    val demoId: StateFlow<String?>
}
```

`schedule` uses `AlarmManager.setAlarmClock` for each remaining athan instant today, and tomorrow Fajr if next is tomorrow. It also arms remaining local `:00` hours between Fajr and Isha with `setExactAndAllowWhileIdle` so athkar does not steal the system alarm-clock slot. When medicine is enabled with slots, it arms upcoming day/time hits the same way (look ahead ~7 days, cap 32). Call again after midnight and after `BOOT_COMPLETED`.

Playback uses `MediaPlayer` on the alarm stream. Foreground service for the duration of the file. Selected Fajr file for Fajr. Selected standard file for the other four. Volume is 0–100 per prayer from `AudioSettingsStore`, applied with `MediaPlayer.setVolume` after prepare. Mute still skips the alarm. Sound-picker PLAY uses 100. Settings volume PLAY uses that prayer's percent. `AthanPlayer` pins output to `TYPE_BUILTIN_SPEAKER` so athan does not also play in a headset.

Live athan posts channel `athan_playback_alarm` at `IMPORTANCE_HIGH`, silent, lock-screen public, with a Stop action and a `fullScreenIntent` to `MainActivity`. `MainActivity` sets show-when-locked while `playback` is non-null. Demos skip the full-screen intent. While alarm audio plays, volume keys bind to `STREAM_ALARM`. Volume down to zero or a long press calls `AthanController.stop()`.

Hourly athkar rotates the remaining clips when the setting is on. Skip if athan is playing or that minute is an athan alarm. Silent from Isha until the next Fajr, and silent from 10:00 PM until 8:00 AM local even if Fajr already passed. Settings PLAY demos do not wait for the hour.

Medicine reminder plays the selected voice clip at each configured weekday/time. Skip if athan is playing or that minute is an athan alarm. Uses `AlarmManager.setAlarmClock` (same class as prayer athan) plus a 3-minute grace window so a late wake still speaks. If the slot is already due when Mutaz enables it, play immediately (once per occurrence). Stops athkar if both would collide; athkar also yields when a medicine slot is due that minute. Settings stores enable, voice, slots, and last-fire key in `prayerathan_medicine`. Off until the user turns it on. Preview uses `demoId = medicine:preview`.

Priority: athan > medicine > athkar.

When the file ends, `playback` / `athkarPlayback` / `medicinePlayback` goes null. UI returns to countdown (or clears the medicine banner).

Do not `delay()` in a composable to fire athan, athkar, or medicine.
