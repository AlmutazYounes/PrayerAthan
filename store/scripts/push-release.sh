#!/bin/zsh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
source "$SCRIPT_DIR/versions.sh"
cd "$ROOT"

NO_BUMP=0
NOTES=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-bump)
      NO_BUMP=1
      shift
      ;;
    --build-only)
      BUILD_ONLY=1
      shift
      ;;
    -h|--help)
      cat <<EOF
Usage: ./store/scripts/push-release.sh [--no-bump] [--build-only] "what changed"

Bumps versionCode if Gradle is not ahead of store/README.md, builds a signed
AAB, publishes it to Play internal testing, patches store docs.

Requires: keystore/, ego-browser, Chrome signed into Play Console as
mutazyounes@gmail.com.
EOF
      exit 0
      ;;
    *)
      NOTES="$1"
      shift
      ;;
  esac
done

BUILD_ONLY="${BUILD_ONLY:-0}"
NOTES="${NOTES:-Internal test update.}"

if [[ "$NO_BUMP" -ne 1 ]]; then
  "$SCRIPT_DIR/bump-version.sh"
fi

"$SCRIPT_DIR/build-aab.sh"

CODE="$(gradle_version_code)"
NAME="$(gradle_version_name)"
LABEL="${CODE} (${NAME})"
AAB="$ROOT/app/build/outputs/bundle/release/app-release.aab"

if [[ "$BUILD_ONLY" -eq 1 ]]; then
  echo "AAB ready: $AAB ($LABEL)"
  echo "skipping Console (--build-only)"
  exit 0
fi

if ! command -v ego-browser >/dev/null; then
  echo "ego-browser not on PATH. Upload $AAB in Console, then:" >&2
  echo "  python3 store/scripts/patch-docs.py --code $CODE --name $NAME --notes $(printf %q "$NOTES")" >&2
  exit 1
fi

export PLAY_AAB="$AAB"
export PLAY_LABEL="$LABEL"
export RELEASE_NOTES="$NOTES"

ego-browser nodejs < "$SCRIPT_DIR/console-publish.js"

python3 "$SCRIPT_DIR/patch-docs.py" --code "$CODE" --name "$NAME" --notes "$NOTES"

echo
echo "Live on internal testing: $LABEL"
echo "Update tablets: https://play.google.com/apps/internaltest/4701680380313434468"
echo "Play can sit on the old build for a few minutes. Uninstall debug first if Update never shows."
