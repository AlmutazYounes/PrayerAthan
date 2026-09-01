# Athan Wall Clock visual spec

This file is the visual contract. Designer agents and Compose agents implement from here plus the two PNGs. If chat history disagrees with this file, this file wins. If a pixel in an approved PNG disagrees with a number here, update this file with Mutaz. Do not freelance a third look.

Product behavior, prayer math, audio, and settings live in `PROJECT.md`. This file is pixels, type, layout, and states.

Keep the screen on with `FLAG_KEEP_SCREEN_ON`. That is a product flag, not a visual.

---

## How agents should use this file

**Designer.** Build the Compose theme and `WallScreen` so a 7-inch and a 10-inch tablet match the two PNGs. Copy the token table. Bind times from the engine. Do not invent colors.

**Code.** Every color on this screen comes from the token table. No `#FFD700`, no `MaterialTheme.colorScheme.primary`, no random `Color.White`. If you need a new color, add a named token here first.

**QA.** Hold the PNG next to the emulator. Same structure, same gold vs ivory jobs, same highlight on the next prayer. Mockup clock times are layout fiction. Real times come from adhan-kotlin.

---

## Source of truth

Approved visuals, and only these:

| File | Axis | Canvas |
| --- | --- | --- |
| `design/athan-wall-horizontal-v2.png` | Landscape | 1536 x 1024, 3:2 |
| `design/athan-wall-vertical.png` | Portrait | 1024 x 1536, 3:2 |

The app supports both orientations. Follow device rotation. Do not lock the activity to portrait. A future settings toggle may pin an axis. Until that exists, `sensor` / full sensor orientation is correct.

Same tokens. Two layouts. Portrait is not a squeezed landscape. Landscape is not a rotated portrait.

## Stacked clocks trial

Live when `UseStackedClocks` is true in `WallScreen.kt`. Code is `ui/StackedClockWall.kt`.

No Jordan clock on the wall. Albany is hours and minutes. Landscape column: header clearance `12`, hero arc `44`, gap `8`, prayer grid `36`. Portrait column: header spacer `14`, hero arc `40`, gap `10`, prayer grid `34` (plus `10dp` top inset on the grid). Arc is ~66% of portrait width or ~38% of landscape width, top-aligned under the header, height-limited.

No standalone countdown in the landscape prayer cells. Landscape top edge holds location + weather (start) and date + settings (end). Hero below matches portrait: Albany clock centered in a horseshoe arc (`nextPrayerRing`); countdown and `NEXT …` sit in the bottom notch. Arc ~38% of width, height-limited, top-aligned under the header. Prayer grid is 3×2 with no cell borders; one hairline between the two rows.

Portrait hero: big Albany clock centered inside a thick horseshoe arc (gap at bottom). Bright gold stroke = time left until next prayer (`nextPrayerRing`); dim track behind it. Countdown (`HH:MM:SS`) and `NEXT …` sit in the bottom gap of the arc, not below the widget. Arc ~66% of width, pinned under the header. No per-prayer weather in the grid. Prayer list is name + time only, 2 columns, hairline between rows. Column weights: header spacer `14`, hero `40`, gap `10`, prayer grid `34`.

Landscape and portrait prayer grids use `HorizontalHairline` between rows only. No vertical hairlines between prayer tiles. No hairline above the first portrait row (Fajr / Sunrise).

No AM/PM beside the Albany clock. Just `H:MM`.

No star watermark behind Albany either (`showStar = false` in `StackedClockColumn`). Bare digits, no octagram behind them.

The PNGs still show a Jordan clock. Ignore that. Mutaz dropped it.

---

## Files in design/

As of this writing the folder contains:

- `design/dark-wall-backdrop.png` (Mecca drone, landscape dark)
- `design/dark-wall-backdrop-portrait.png` (Mecca drone, portrait dark)
- `design/ic_launcher.png`
- `design/ic_launcher_foreground.png`
- `design/WEATHER-ICONS-SOURCE.md`

Live wall fills are the four WebPs in `app/src/main/res/drawable-nodpi/` (`wall_backdrop_{light,dark}` and `_portrait`). Light sources live only as those WebPs. The athan-playing state is specified in this document without a PNG. Do not resurrect deleted mockups or generated_candidates.

---

## Color tokens

Named tokens. Mutaz rejected a white light wall and a crushed-black dark wall. Dark is a night mosque. Light is plaster. Same brass. Landscape v2 still wins for type and layout when the two PNGs drift. Those PNGs do not win for fill. Do not copy their black field.

Use these Compose names. Hex is sRGB. Dark values below. Light sits in the next table.

| Token | Hex | Job |
| --- | --- | --- |
| `ColorBackground` | `#1C1614` | Mid-tone of the wall wash. Umber plaster. Not `#000000`, not `#050403`, not Material `#121212`. |
| `ColorBackgroundLift` | `#3E2C18` | Radial bronze under the clocks. |
| `ColorBackgroundDeep` | `#151328` | Indigo plaster in the corners, vignette, and prayer shelf. Not `#020101`. You should still see color there. |
| `ColorSettingsPanel` | `#2C241A` | Settings sheet fill. Bronze panel. Not the wall mid-tone, not the lift, not the indigo corners. |
| `ColorClock` | `#F5EBDA` | Albany digits and prayer times. Warm cream white, not gold. |
| `ColorGold` | `#C5963A` | Section labels, countdown digits, next-prayer cell text, highlight stroke. Antique brass. |
| `ColorGoldDim` | `#8A6E28` | Unused in idle if past cells use ivory-dim. Keep for a gold label that must recede. |
| `ColorPrayerPast` | `#A89880` | English name and time on a prayer whose time has passed. |
| `ColorHairline` | `#8A7A68` | 1 to 2 px rules between columns, between hero and prayers, and inside the portrait grid. |
| `ColorHighlightStroke` | `#C5963A` | Same as `ColorGold`. Next-prayer rounded-rect outline. |
| `ColorHighlightFill` | `#00C5963A` | Fully transparent. The PNG is an outline, not a filled chip. |
| `ColorDate` | `#EBDDC8` | Weekday and date line. Same as `ColorClock`. |
| `ColorLocation` | `#EBDDC8` | Header `ALBANY, NY`. |
| `ColorLabel` | `#C5963A` | `ALBANY`, `NOW / ALBANY`, `NEXT ASR`, unit captions. |
| `ColorAmPm` | `#EBDDC8` | `AM` / `PM` beside clock digits. |
| `ColorStar` | `#C5963A` at 10% alpha | Rub el Hizb behind Albany only. Stroke, not a filled sticker. |
| `ColorGeometry` | `#C5963A` at 10% alpha | Backdrop eight-point stars, octagons, girih lines. Same pattern as light, lower alpha. |

On the PNGs, some landscape labels sit closer to cream than brass. Implement `ColorLabel` as gold anyway so labels, countdown, and next-prayer share one accent. Do not add a second gold.

The wall is not a flat fill. `WallBackdrop` crops `wall_backdrop_dark` (or `wall_backdrop_dark_portrait`) to fill the screen. Dark is the generated plaster plus girih from `design/`. Palette `backgroundDeep` sits under the bitmap while it loads. Dark must not collapse to total black. Do not replace the plaster walls with a mosque JPEG, marble stock, emoji, or a neon second accent. Do not go back to a Canvas girih unless Mutaz drops the bitmaps.

Do not use `#FFD700`, `#FFC107`, `#FFEB3B`, `#FFFFFF`, `#FFF6E8`, `#000000`, or `#020101`. Digit cores measure about `rgb(197, 150, 58)`. If your gold looks like a warning banner, it is wrong.

Past vs later in the mockups is a small ivory dim, not a different hue. `ColorPrayerPast` is that dim. Later stays `ColorClock`. Next is all `ColorGold`.

## Dark wall tokens

Dark is a night mosque wall: umber, bronze, deep indigo plaster. Not crushed OLED black.

| Token | Dark | Job |
| --- | --- | --- |
| `background` | `#1C1614` | Wall wash mid-tone. Umber plaster. |
| `backgroundLift` | `#3E2C18` | Radial center. Bronze. Not `#FFF6E8`. |
| `backgroundDeep` | `#151328` | Corners, vignette, prayer shelf. Indigo plaster. |
| `settingsPanel` | `#2C241A` | Settings sheet fill. Not any wall-wash stop. |
| `clock` | `#F5EBDA` | Cream white digits and prayer times. |
| `gold` | `#E2B85C` | Arc, labels, countdown, prayer names. |
| `gold` | `#E2B85C` | Labels, countdown, next-prayer. Lighter brass on night plaster. |
| `goldDim` | `#C5963A` | Receding gold. |
| `prayerPast` | `#C8B498` | Past prayer text. |
| `hairline` | `#C4A888` | Rules. |
| `star` | gold 12% | Albany watermark only. |
| `geometry` | gold 10% | Baked into the backdrop bitmaps. Keep the token. |

Provide via `CompositionLocal` with `LocalWallPalette`. `WallBackdrop` uses `design/dark-wall-backdrop.png` (landscape) or `design/dark-wall-backdrop-portrait.png` (portrait). Type, hairlines, and the Albany star use the token table. Geometry in the plaster bitmaps stays behind the clocks.

Settings: Sheet fill is `settingsPanel`, not the wall mid-tone. Gold gear in the header between location and date. Long-press still opens the sheet.

---

---

## Typography

### Family

English and numerals: Inter Variable (`res/font/inter.ttf`, SIL OFL). `EnglishFontFamily` maps Normal/Medium/Bold to weight axis 400/500/650. Tabular lining figures stay on. Oswald and Cinzel are retired. Inter is the modern face: even widths, no condensed squeeze, no engraved small caps.

All English labels are all-caps. No Arabic on the wall. Prayer cells are English name plus time only.

### Numerals

Clocks, countdown, and prayer times use tabular lining figures so `1` and `0` do not shift the layout every second.

Compose:

```kotlin
fontFeatureSettings = "tnum, lnum"
```

Use a `FontFamily` that actually has tabular figures. If Oswald's `tnum` is missing on device, add a `FontVariation` / fallback that keeps digit width constant. Test the countdown at `01:11:11` and `08:08:08`. If the block jumps, tabular is broken.

Do not use a 7-segment LED face.

### Hierarchy

Type is a percent of the slot it sits in, not a global `sp` cap. `fitSp` takes the smaller of width and height so digits fill the box and stop at the edge.

| Role | Size | Color | Notes |
| --- | --- | --- | --- |
| Albany / Jordan | `74%` of the digit slot height, or the width of `12:59` plus AM/PM, whichever is smaller. | `ColorClock` | AM/PM is `28%` of the clock size. Clip. Do not cap at `88.sp`. |
| Countdown | `64%` of the countdown slot height in portrait, `58%` in landscape, or two digits in one third of the width. | `ColorGold` | Digits only (`HH:MM:SS`). No plate behind the digits. No `HRS` / `MIN` / `SEC` captions. |
| Prayer time | Own slot: about `66%` of the cell in portrait, `70%` in landscape. Type is `90%` of that slot height. | state color | Landscape English uses per-name `fitSp` (~`0.78 ×` char count). Portrait English about `28%` of the cell. Two lines. Each line is clipped to its slot so the time cannot cut the hairline. |

---

## Shared layout rules

- Full bleed. No Material top app bar. Hide status and navigation bars if the shell allows it.
- Outer inset about `0.04 * S` from each edge for header text. Clocks may sit closer to the vertical center than the header.
- Header is a overlay on the top of the hero, not a third content region that eats the clocks.
- Location top-left, one line, all-caps.
- Date top-right, two lines, right-aligned. Weekday on top. Date line under it, larger. Format `d MMMM` in English, e.g. `27 August`. No year. No comma in the date line. No Hijri.
- Hairlines are 1.dp on mdpi, 1.5.dp to 2.dp on the mockup density. Never 8.dp "dividers" with padding like a card list.
- Star watermark only behind Albany. Never behind Jordan, never behind the countdown, never as a repeating background.

## Launcher icon

Same mark as the Albany watermark: a Rub el Hizb, two overlapping squares, antique brass `#C5963A` on `#050403`. No mosque, no crescent, no wordmark. Source PNG is `design/ic_launcher.png`. Adaptive layers live in `mipmap`. Notification small icon is a filled white star, `drawable/ic_athan`.

Do not replace this with a Material clock glyph.

Tablets are often 16:10, not the PNG's 3:2. Keep the region percentages. Extra width goes into equal column / cell flex. Portrait clock digits fill the leftover height in their slot after the label. Prayer grid is allowed to take more height than the PNG so the six times read from the sofa.

---

## Landscape

Match `design/athan-wall-horizontal-v2.png`.

### Regions

Percent of screen height, top to bottom:

| Region | Height | Content |
| --- | --- | --- |
| Header overlay | sits in the top `12%` | Location left, date right. Hero clocks occupy the space under and between them. |
| Hero | top `0%` to `50%` | Clock and weather. |
| Hairline | at `50%` | Full-width `ColorHairline`. |
| Prayer block | bottom `36%` column weight | Two rows of three tiles, no cell borders. One hairline between the rows. Countdown lives in the arc notch above. |

Percent of screen width: clock and location+countdown split the hero. Current weather is a top-right one-liner. Prayer tiles share three equal slots per row.

### Hero columns

Left to right, equal width.

1. Label `ALBANY`. Clock Albany local time. AM/PM. Star watermark centered on the digits.
2. Label `JORDAN`. Clock Amman time. AM/PM. No star.
3. Label `NEXT {PRAYER}` e.g. `NEXT ASR`. Gold countdown `HH:MM:SS`. `HRS`, `MIN`, `SEC` under the three pairs.

Vertical hairlines at `33.3%` and `66.7%` width, running through the hero only, not through the header date and not through the prayer row. Column content is centered in its third.

Albany and Jordan clocks are ivory and large. Countdown is gold and the same visual weight. On the PNG the three digit rows share one baseline band around `36%` to `54%` of height.

### Prayer block

Two rows of three equal-weight slots (`chunked(3)`). Row 1: Fajr, Dhuhr, Asr. Row 2: Maghrib, Isha, empty `Box` spacer so the two cards keep the same width as the top row. No Sunrise tile.

Each tile is open on the backdrop. Landscape: prayer name and that prayer's weather share one top line; prayer time stays large below. Next tile also shows the live countdown under the time. No plaque border or fill. One hairline between the two rows. Portrait keeps name, weather, then time stacked.

---

## Portrait

Match `design/athan-wall-vertical.png`.

### Regions

Percent of screen height:

| Region | Height | Content |
| --- | --- | --- |
| Header overlay | top `10%` | Same location / date as landscape. |
| Albany block | about `10%` to `44%` | Large ivory clock. |
| Countdown | about `45%` to `61%` | Standalone `CountdownBlock` under the clock. |
| Hairline | about `62%` | Full-width rule. |
| Prayer grid | `63%` to `100%` | 2 columns, 3 equal rows. No expanded next cell. |

All hero blocks are horizontally centered.

Portrait Albany is the hero. Jordan and the countdown are clearly smaller. If you draw three equal portrait clocks, you are looking at the landscape file.

### Prayer grid

Two columns, three rows:

| | Left | Right |
| --- | --- | --- |
| Row 1 | Fajr | Sunrise |
| Row 2 | Dhuhr | Asr |
| Row 3 | Maghrib | Isha |

Thin horizontal rules at the top of the grid and between rows. One vertical rule at `50%` width through the grid only. Same `ColorHairline`.

Each cell uses the same two-line stack as landscape. Next-prayer pill insets inside the cell. On the PNG the Asr pill is a wide rounded rectangle, about 421 x 149 px on a 1024-wide canvas, not a circle.

---

## Components

Five composables. Naming is a suggestion. Structure is not.

### Header

```
Header(location: String, weekday: String, dateLine: String)
```

Location top-start, two lines: city name then gold weather with condition icon (`22°C  CLEAR`) from Open-Meteo, Celsius. Gold weather icon sits beside the weather line. Never lat/long on the wall. Date column top-end, one line: weekday then calendar date (`Thursday  27 August`). No year. In landscape, location and weather sit top-start; date and settings top-end. Countdown is in the arc notch, not a separate hero column. No Hijri slot.

### ClockBlock

```
ClockBlock(
  label: String,
  hourMinute: String,  // "3:17"
  amPm: String,        // "PM"
  emphasis: ClockEmphasis,  // Local, World
  showStar: Boolean
)
```

`Local` is Albany. Largest in portrait. Star only when `showStar` is true, which is Albany only.

`World` is Jordan. No star. Smaller in portrait.

AM/PM sits to the right of the minute digits. Optically center it on the cap height of the digits. It is not a second line under the clock.

### CountdownBlock

```
CountdownBlock(
  label: String,       // "NEXT ASR"
  hours: String,       // "01"
  minutes: String,     // "24"
  seconds: String,     // "18"
  playingPrayerName: String?  // null when idle
)
```

Idle. Gold `HH:MM:SS` with colons in the same face and color. Under the pairs, three captions `HRS`, `MIN`, `SEC`, each centered on its two digits. Use a subcompose or a 3-column `Row` so the captions stay glued to the pairs when the string width changes.

Playing. `playingPrayerName != null`. Hide the timer and the unit captions. Stack, centered, gold:

1. `NOW` in the section-label size
2. Prayer English name in the countdown size, all-caps
3. `Adhan is playing` in the unit-caption size, or slightly larger so it reads at 8 feet

Do not open another activity. Swap the body of this block.

### PrayerCell

```
PrayerCell(
  cell: PrayerCellState,
  type: TypeScale,
  portrait: Boolean,
  countdown: String,
  athanPlaying: Boolean,
  playingName: String?
)
```

| State | Name Text | Time Text | Fill | Stroke |
| --- | --- | --- | --- | --- |
| Past | `ColorGold` | `ColorPrayerPast` | none | none |
| Next | `ColorGold` | `ColorClock` | none | none (countdown in cell) |
| Later | `ColorGold` | `ColorClock` | none | none |

No cell corner radius, border, or fill. Landscape and portrait row separators are `HorizontalHairline` (`HairlineWidth`, `palette.hairline`).
Normal tiles have width weight `1.0f` in landscape and standard 2-column pairing in portrait.
Next tile expands with width weight `1.35f` in landscape only. Portrait next cell stays equal width.

Landscape next cell includes a live gold countdown `HH:MM:SS` (tabular lining figures, size `type.cellCountdown`) below the prayer time, or "Adhan is playing" when active. Portrait keeps that countdown in the hero `CountdownBlock`.

### Star watermark

Eight-pointed star, Rub el Hizb / octagram. Thin stroke. `ColorStar` which is gold at 10% alpha. Diameter about `0.55` of the Albany column width in landscape, about `0.70` of the clock digit width in portrait. Centered on the Albany digits. Drawn behind the numerals, above the background. Vector or a single composable `Canvas`. Not an emoji. Not a filled gold badge.

---

## States

### Idle

Default wall. Countdown runs to the next athan prayer. Next cell is gold with the outline. Past cells use `ColorPrayerPast`. Later cells use `ColorClock`. Sunrise can be Past or Later. It is never Next, because sunrise has no athan. After Fajr the next highlight is Dhuhr even if sunrise is still in the future. Sunrise still goes Past once its time has passed.

### Adhan playing

No remaining PNG for this. Implement from this spec.

- Albany clock stays.
- Header stays.
- Prayer grid stays. The prayer that just started is `Next` (highlighted). Other cells follow past / later as usual.
- `CountdownBlock` switches to NOW + prayer name + `Adhan is playing`.
- When the file ends, or when Mutaz taps to stop, return to idle with the new next prayer.

Tap to stop is a distinct gesture from long-press settings. Visual of the tap is none. No snackbar, no pause button.

### Settings

Small gold gear in the header, between location and date. Long-press anywhere is the backup. Not a FAB. Not on the prayer grid.

The sheet is a bottom panel in `settingsPanel`. SETTINGS and CLOSE stay on one header row. Gold section titles with a thin rule: Location, Prayer athans, Athan, Hourly athkar, Night blackout. Country and city are bordered search fields. GPS is a quiet action under location. No Default Albany button. Prayer athans has five chips (Fajr, Dhuhr, Asr, Maghrib, Isha) to mute or unmute individual athans. Athan is a compact list with a gold tick on the selected file and PLAY on the right. Athkar is On / Off, no clip demos. Subtitle is `8 AM to 10 PM · athan wins`. Night blackout is On / Off with subtitle `11 PM to 4 AM · tap to wake`. Landscape splits location on the left and prayer athans+athan+athkar+blackout on the right. Same Inter and gold. No Material You cards. Scroll inside a column if the 7-inch cannot fit.

---

## Content rules

- Clocks are 12-hour with `AM` / `PM` by default. `PROJECT.md` allows a 24-hour settings flag. When that flag is on, drop AM/PM and keep tabular digits.
- Prayer grid times are hour and minute only. No `AM` / `PM` on Fajr through Isha.
- Countdown is always `HH:MM:SS` with leading zeros, even for `00:04:09`.
- Header location string is `ALBANY, NY` until the saved label changes. Still all-caps in the header. Gold weather line under it when Open-Meteo answers. No `NOW / ALBANY` on the clock.
- No Jordan clock. No Jordan label.
- Next label is `NEXT` plus the English prayer name, all-caps: `NEXT ASR`.
- Prayer order is always Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha. This spelling.
- English names only on the prayer cells. No Arabic labels.
- No Hijri.
- No settings chrome on the wall.
- Mockup times (Fajr `5:09 AM`, Asr `4:42 PM`, clocks `3:17` / `10:17`, countdown `01:24:18`) are drawn for Thursday 27 August 2026. They are not constants. Layout against live engine values.

---

## Do and don't

Do:

- Use the token table.
- Hide system bars when the shell can.
- Keep hairlines thin and warm.
- Tick layout every second. Tabular figures exist so that tick does not shove neighbors.
- Verify both orientations on 7-inch and 10-inch.

Don't:

- Material You cards, tonal surfaces, FABs, nav bars, scrims, ripple-heavy lists.
- A hamburger, FAB, or Qibla compass on this screen.
- Weather, Hijri, Quran, mosque name, or a second accent color.
- 7-segment or calculator fonts.
- Neon gold.
- Hardcode the PNG's `5:09 AM` as Fajr.
- Draw the star on Jordan or on the countdown.
- Add a third breakpoint layout. Two axes, same tokens.
- Put Chrome / browser QA in the loop. This is an Android wall activity.
- Swap the wall bitmaps for a mosque photo, marble stock, or a Canvas girih.

---

## Compose token copy

Canonical values. Put them in `WallTheme.kt` or equivalent. Do not scatter literals.

```kotlin
val DarkWallPalette = WallPalette(
    background = Color(0xFF1C1614),
    backgroundLift = Color(0xFF3E2C18),
    backgroundDeep = Color(0xFF151328),
    settingsPanel = Color(0xFF2C241A),
    clock = Color(0xFFF5EBDA),
    gold = Color(0xFFE2B85C),
    goldDim = Color(0xFFC5963A),
    prayerPast = Color(0xFFC8B498),
    hairline = Color(0xFFC4A888),
    star = Color(0xFFE2B85C).copy(alpha = 0.12f),
    geometry = Color(0xFFE2B85C).copy(alpha = 0.10f),
)

val HairlineWidth = 1.5.dp
val HighlightStrokeWidth = 1.5.dp
val HighlightCornerRadius = 16.dp
val CellCornerRadius = 8.dp
val ScreenInset = 16.dp // floor. Prefer 0.04 * shortest side when window size is known.

val ColorCellFill = Color(0xFF0A0A0A).copy(alpha = 0.38f)
val ColorCellFillNext = Color(0xFF0A0A0A).copy(alpha = 0.48f)
val ColorCellStroke = Color(0xFFD4C4A8)
val ColorCellStrokeNext = Color(0xFFE8D5B5)
val CellStrokeNormal = HairlineWidth
val CellStrokeNext = HighlightStrokeWidth * 2
val NextCellWeight = 1.5f
val NormalCellWeight = 1.0f

fun typeScale(shortestSideDp: Float) = object {
    val albanyLandscape = (shortestSideDp * 0.26f).sp
    val albanyPortrait = (shortestSideDp * 0.28f).sp
    val jordanLandscape = (shortestSideDp * 0.24f).sp
    val jordanPortrait = (shortestSideDp * 0.17f).sp
    val countdownLandscape = (shortestSideDp * 0.22f).sp
    val countdownPortrait = (shortestSideDp * 0.17f).sp
    val dateLine = (shortestSideDp * 0.050f).sp
    val weekday = (shortestSideDp * 0.034f).sp
    val location = (shortestSideDp * 0.022f).sp
    val label = (shortestSideDp * 0.022f).sp
    val units = (shortestSideDp * 0.014f).sp
    val prayerEn = (shortestSideDp * 0.024f).sp
    val prayerAr = (shortestSideDp * 0.020f).sp
    val prayerTime = (shortestSideDp * 0.022f).sp
    val cellCountdown = (shortestSideDp * 0.026f).sp
}
```

English `FontFamily`: Oswald or Roboto Condensed, `FontWeight.Medium` for clocks, `FontWeight.Normal` or `Medium` for labels.
