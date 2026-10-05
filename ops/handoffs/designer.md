# Handoff: Designer

Status: done

When: 2026-10-05
Agent: designer
Issue: #14

Portrait header battery percent + larger weather/date. Landscape unchanged.

## What changed

1. **`shell/BatteryMonitor.kt`**
   - Sticky `ACTION_BATTERY_CHANGED` percent helper.
   - Register/unregister for live updates. Display only.

2. **`ui/WallScreen.kt` — `Header`**
   - `showBattery` flag (default false).
   - Portrait: gold battery glyph + `NN%` above location.
   - Portrait weather max ~`1.65× dateLine`, date max ~`1.45× dateLine`.
   - Landscape callers leave `showBattery` false.
   - `PortraitWall` rollback also passes `showBattery = true`.

3. **`ui/StackedClockWall.kt`**
   - `PortraitStackedWall` passes `showBattery = true`.
   - `LandscapeStackedWall` keeps its own header (no battery).

4. **Docs**
   - `DESIGN.md` Header notes portrait battery + size bumps.
   - `ops/LOG.md` one line for #14.

## Not touched

Athan, engine, GPS, landscape header sizes, Play push.

## Verify

`./gradlew test assembleDebug` with `ANDROID_HOME` if needed.
