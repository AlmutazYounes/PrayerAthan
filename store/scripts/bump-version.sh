#!/bin/zsh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
# shellcheck source=versions.sh
source "$SCRIPT_DIR/versions.sh"
cd "$ROOT"

NAME_OVERRIDE=""
FORCE=0
while [[ $# -gt 0 ]]; do
  case "$1" in
    --name)
      NAME_OVERRIDE="$2"
      shift 2
      ;;
    --force)
      FORCE=1
      shift
      ;;
    *)
      echo "usage: bump-version.sh [--name 0.8.0] [--force]" >&2
      exit 2
      ;;
  esac
done

code="$(gradle_version_code)"
name="$(gradle_version_name)"
live="$(readme_live_code)"

if [[ -z "$code" ]]; then
  echo "could not read versionCode from app/build.gradle.kts" >&2
  exit 1
fi

if [[ -n "$live" && "$code" -gt "$live" && "$FORCE" -ne 1 ]]; then
  echo "Gradle already ahead of README ($code > $live). No bump."
  echo "CODE=$code"
  echo "NAME=$name"
  exit 0
fi

if [[ -n "$live" ]]; then
  new_code=$((live + 1))
else
  new_code=$((code + 1))
fi
new_name="${NAME_OVERRIDE:-0.${new_code}.0}"

perl -i -pe "s/^(\\s*versionCode\\s*=\\s*)\\d+/\$1${new_code}/" "$ROOT/app/build.gradle.kts"
perl -i -pe "s/^(\\s*versionName\\s*=\\s*)\"[^\"]+\"/\$1\"${new_name}\"/" "$ROOT/app/build.gradle.kts"

echo "bumped $code ($name) -> $new_code ($new_name)"
echo "CODE=$new_code"
echo "NAME=$new_name"
