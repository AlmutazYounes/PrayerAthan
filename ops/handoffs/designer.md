# Handoff: Designer

Status: done

When: 2026-08-31
Agent: designer

## What changed

Landscape layout experiment only. Portrait untouched.

1. **`PrayerGrid.kt` — `LandscapePrayerGrid`**
   - Was one `Row` of five cells.
   - Now `cells.chunked(3)` in a `Column` of two equal-height rows with 6.dp gaps.
   - Row 1: Fajr, Dhuhr, Asr.
   - Row 2: Maghrib, Isha, empty `Box` at `NormalCellWeight` so card widths match the top row.
   - Next cell still uses `NextCellWeight` (2.3) within its row. In-cell countdown unchanged.

2. **`WallTheme.kt` — `LandscapeWallLayout`**
   - `clockHeight` / `weatherHeight`: 0.36 → **0.28**
   - `heroRowFromTop`: 0.16 → **0.08**
   - `prayerHeight`: 0.36 → **0.44**
   - `NextCellWeight` still 2.3. Countdown knobs unused by current stacked wall (left alone).

3. **`StackedClockWall.kt`**
   - No code change. Absolute positioning already reads the knobs above.

4. **Docs**
   - `DESIGN.md`: landscape prayer block is 3×2, not one 5/6-across row.
   - `ops/LOG.md`: one line for the experiment.

## Not touched

Portrait stack, keep-screen-on, palettes, athan-kotlin, GPS / LocationFixer.

## Verify

Open landscape on a 7" or 10". Clock and weather sit higher. Two prayer rows with wider cards. Maghrib/Isha align under Fajr/Dhuhr with a blank third slot. Rotate to portrait: classic clock → countdown → 2×3 grid.
