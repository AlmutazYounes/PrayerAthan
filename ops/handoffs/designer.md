# Handoff: Designer

Status: done

When: 2026-10-05
Agent: designer
Issue: #18

Portrait header stack: weather → location → larger battery.

## What changed

1. **`ui/WallScreen.kt` — `Header`**
   - Portrait left column order: weather, location, battery.
   - Battery diameter 54dp (was 44).

2. **`DESIGN.md`**
   - Header note matches the stack order.

## Not touched

Athan, engine, landscape, low-battery beep logic.

## Verify

`./gradlew test assembleDebug`
