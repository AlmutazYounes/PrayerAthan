# Handoff: Designer

Status: done

When: 2026-08-31
Agent: designer

## What changed

1. **Light Mode & Auto Removed (Dark Wall Only)**:
   - Removed `ThemeMode` enum, `LightWallPalette`, `paletteFor()`, and light mode theme resolution.
   - Wall is always dark with `DarkWallPalette` and dark backdrop assets (`wall_backdrop_dark` / `wall_backdrop_dark_portrait`).
   - Removed the Theme section from `SettingsSheet` (Light / Dark / Auto chips).
   - Removed `themeMode` plumbing from `WallSettingsStore`, `WallViewModel`, `WallUiState`, `SettingsSheet`, `WallScreen`, and `StackedClockWall`.
   - Updated `PROJECT.md`, `DESIGN.md`, `ops/contracts/ui-api.md`, `ops/STATUS.md`, and `ops/LOG.md`.

2. **Portrait Dark Wall Backdrop Asset**:
   - Replaced old portrait dark fill with Mecca drone dark portrait artwork (`design/dark-wall-backdrop-portrait.png`).
   - Converted to WebP at `app/src/main/res/drawable-nodpi/wall_backdrop_dark_portrait.webp`.
   - Confirmed `WallBackdrop.kt` loads `R.drawable.wall_backdrop_dark_portrait` when portrait.

3. **Weather VectorDrawables**:
   - Added clean, tintable Apache 2.0 / Public Domain VectorDrawables under `app/src/main/res/drawable/`:
     - `ic_weather_clear.xml` (clear/sun)
     - `ic_weather_fair.xml` (fair/sun behind cloud)
     - `ic_weather_cloud.xml` (cloudy)
     - `ic_weather_fog.xml` (fog)
     - `ic_weather_drizzle.xml` (drizzle)
     - `ic_weather_rain.xml` (rain)
     - `ic_weather_snow.xml` (snow)
     - `ic_weather_storm.xml` (storm/thunder)
   - Created `design/WEATHER-ICONS-SOURCE.md` recording sources and license.

4. **Weather condition mapping & UI**:
   - Added `weatherIconRes(condition: String)` in `ui/WeatherIcons.kt` with mapping tests in `WeatherIconsTest.kt`.
   - Exposed `weatherCondition` on `WeatherNow` and `WallUiState`.
   - Updated `Header` in `WallScreen.kt` with a larger `WeatherRow` (gold icon sized ~1.15x line height next to temperature + condition, aligned vertically).

## Verification

Ran `./gradlew test assembleDebug` successfully. All unit tests passed and debug APK built cleanly.


