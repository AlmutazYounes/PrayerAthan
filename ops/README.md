# Control plane

Product: `PROJECT.md`. Pixels: `DESIGN.md`. Orchestrator: `AGENTS.md`. Subagents: `.cursor/agents/`. Memory: this folder.

## Layout

| Path | What |
| --- | --- |
| `ops/PLAN.md` | Sequence, expected output, review bar |
| `ops/STATUS.md` | Now / next / blockers |
| `ops/LOG.md` | Dated one-liners |
| `ops/handoffs/` | Last report per job |
| `ops/contracts/` | Shared Kotlin APIs |
| `.cursor/agents/` | Cursor subagent prompts |
| `.cursor/commands/` | Slash commands: `/next`, `/prayer-engine`, … |
| `.cursor/rules/` | Short glob / always-on rules |
| `store/` | Play Console state and listing. Not the product spec. |

## Session loop

1. Read `AGENTS.md` and `ops/STATUS.md`.
2. Spawn `subagent_type` matching **Next**.
3. Review the diff. Read the handoff.
4. Update `STATUS.md`. Append `LOG.md`.
5. Stop unless Mutaz said keep going.

## Spawn

One builder at a time, except engine and shell. Edit `.cursor/agents/<name>.md` if the job changed, then spawn. Do not invent a parallel prompt in `ops/`.
