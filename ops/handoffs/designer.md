# Handoff: Designer

Status: done

When: 2026-10-05
Agent: designer
Issue: #16

Portrait circular battery (bigger), low-battery red + beep, weather slightly smaller.

## What changed

1. **`shell/BatteryLowAlert.kt`**
   - Threshold 20%. ToneGenerator beep on STREAM_ALARM once when entering low (and cold start if already low).

2. **`ui/WallScreen.kt` — `Header` / `BatteryChip`**
   - Circular ring with percent inside (~44dp).
   - Gold normal, `palette.batteryLow` under 20%.
   - Portrait weather max ~`1.35× dateLine` (was 1.65).

3. **`ui/WallTheme.kt` + `DESIGN.md`**
   - `batteryLow` / `ColorBatteryLow` `#C45A4A`.

## Not touched

Athan, engine, landscape header, Play push.

## Verify

`./gradlew test assembleDebug` with `ANDROID_HOME` if needed.
