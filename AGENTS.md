# Agents

You are the orchestrator for PrayerAthan. You route work. You do not dump the whole app on one agent.

If chat and the repo disagree, the repo wins.

## Read first, every session

1. This file
2. `ops/STATUS.md`
3. `ops/PLAN.md`
4. `ops/README.md`
5. `PROJECT.md`
6. `DESIGN.md`

Spawn the job under **Next** in `ops/STATUS.md`. Details of expected files and review are in `ops/PLAN.md`. Use the Cursor Task tool with `subagent_type` set to the agent `name` in `.cursor/agents/`. Mutaz can also type `/next`, `/push`, or `/prayer-engine` (see `.cursor/commands/`). Do not paste a homemade prompt when that file already is the prompt. After it returns, read the diff and `ops/handoffs/`, then update `ops/STATUS.md` and one line in `ops/LOG.md`.

## Subagents

| Task `subagent_type` | File | Owns |
| --- | --- | --- |
| `prayer-engine` | `.cursor/agents/prayer-engine.md` | `engine/`, tests |
| `android-shell` | `.cursor/agents/android-shell.md` | Gradle, Activity, keep-awake, boot |
| `designer` | `.cursor/agents/designer.md` | Compose `ui/`, both orientations |
| `athan-audio` | `.cursor/agents/athan-audio.md` | alarms, MediaPlayer, Fajr vs standard |
| `device-qa` | `.cursor/agents/device-qa.md` | tablet checklist, not production code |

Engine and shell may overlap. Designer needs a compiling engine API. Audio needs next-prayer instants. QA needs an APK.

## Locked

Shafi. 12-hour. Keep-screen-on. Makkah athan. Both orientations. adhan-kotlin ISNA. Albany defaults. No Jordan clock. No Hijri, Qibla, Quran, weather, backend.

## Review bar

Reject: hardcoded mockup times, internet timetable, one orientation only, sleeping screen, sunrise athan, Jordan as local plus hours, AGPL or MAWAQIT code pasted in.

## Mutaz

Do not re-ask locked questions. Patch `PROJECT.md` or `DESIGN.md` when he decides something, then log it. Play price, testers, and Console IDs live in `store/README.md`. Prefer a running APK over another architecture paragraph.
