# Audio API

Athan audio owns this. Shell starts it. UI observes it and can stop it. Engine is read-only input.

Package: `com.mutazyounes.prayerathan.audio`

Files after shell copies them:

- `res/raw/athan_saudi.mp3`
- `res/raw/athkar_salawat.mp3`

Athan file is `res/raw/athan_saudi.mp3`. Athkar masters live in `audio/athkar/`. See `audio/SOURCE.md` and `audio/athkar/SOURCE.md`.

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
```

## Functions

```kotlin
interface AthanController {
    fun schedule(day: PrayerDay, now: Instant)
    fun stop()                  // tap during athan or athkar
    fun playAthanDemo(soundId: String)
    fun playAthkarDemo(clip: AthkarClip)
    val playback: StateFlow<AthanPlayback?>  // null = idle
    val athkarPlayback: StateFlow<AthkarPlayback?>
    val demoId: StateFlow<String?>
}
```

`schedule` uses `AlarmManager.setAlarmClock` for each remaining athan instant today, and tomorrow Fajr if next is tomorrow. It also arms remaining local `:00` hours between Fajr and Isha with `setExactAndAllowWhileIdle` so athkar does not steal the system alarm-clock slot. Call again after midnight and after `BOOT_COMPLETED`.

Playback uses `MediaPlayer` on the alarm stream. Foreground service for the duration of the file. Selected Fajr file for Fajr. Selected standard file for the other four.

Hourly athkar rotates the remaining clips when the setting is on. Skip if athan is playing or that minute is an athan alarm. Silent from Isha until the next Fajr, and silent from 10:00 PM until 8:00 AM local even if Fajr already passed. Settings PLAY demos do not wait for the hour.

When the file ends, `playback` / `athkarPlayback` goes null. UI returns to countdown.

Do not `delay()` in a composable to fire athan or athkar.
