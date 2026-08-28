#!/usr/bin/env python3
"""Patch live version strings after a successful internal-testing publish."""

from __future__ import annotations

import argparse
import datetime
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]


def sub_file(path: pathlib.Path, pattern: str, repl: str, flags: int = 0) -> int:
    text = path.read_text()
    new, n = re.subn(pattern, repl, text, flags=flags)
    if n:
        path.write_text(new)
    return n


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument("--code", type=int, required=True)
    p.add_argument("--name", required=True)
    p.add_argument("--notes", default="")
    args = p.parse_args()
    label = f"{args.code} ({args.name})"
    today = datetime.date.today().isoformat()

    n = 0
    n += sub_file(
        ROOT / "store" / "README.md",
        r"Internal testing \| Live\. Release \d+ \([^)]+\)\.",
        f"Internal testing | Live. Release {label}.",
    )
    n += sub_file(
        ROOT / "ops" / "STATUS.md",
        r"Internal testing is \*\*\d+ \([^)]+\)\*\*",
        f"Internal testing is **{label}**",
    )
    n += sub_file(
        ROOT / "store" / "CHECKLIST.md",
        r"Signed AAB on internal testing\. \d+ \([^)]+\)\.",
        f"Signed AAB on internal testing. {label}.",
    )
    n += sub_file(
        ROOT / "store" / "PLAN.md",
        r"Signed AAB on internal testing, version \d+ / [^\s.]+",
        f"Signed AAB on internal testing, version {args.code} / {args.name}",
    )
    n += sub_file(
        ROOT / "store" / "APP-GAPS.md",
        r"versionCode \d+, versionName `[^`]+`",
        f"versionCode {args.code}, versionName `{args.name}`",
    )
    n += sub_file(
        ROOT / "store" / "APP-GAPS.md",
        r"Internal test is `[^`]+`",
        f"Internal test is `{args.name}`",
    )
    n += sub_file(
        ROOT / "store" / "PLAY-CONSOLE.md",
        r"bundleRelease \d+ \([^)]+\)",
        f"bundleRelease {label}",
    )
    n += sub_file(
        ROOT / "ops" / "handoffs" / "store.md",
        r"Internal testing live, \d+ \([^)]+\)",
        f"Internal testing live, {label}",
    )

    log = ROOT / "ops" / "LOG.md"
    body = log.read_text()
    note = args.notes.strip() or "app update"
    line = f"- {today} Mutaz: internal testing {label}. {note}. Not production.\n"
    marker = "Newest at the top.\n\n"
    if marker not in body:
        print("ops/LOG.md missing expected header", file=sys.stderr)
        return 1
    if f"internal testing {label}" not in body.split("\n")[4:8]:
        log.write_text(body.replace(marker, marker + line, 1))
        n += 1

    print(f"patched {n} live-version sites to {label}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
