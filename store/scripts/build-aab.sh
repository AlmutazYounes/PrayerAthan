#!/bin/zsh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
# shellcheck source=versions.sh
source "$SCRIPT_DIR/versions.sh"
cd "$ROOT"

if [[ ! -f "$ROOT/keystore/upload.jks" || ! -f "$ROOT/keystore/keystore.properties" ]]; then
  echo "missing keystore/upload.jks or keystore/keystore.properties" >&2
  exit 1
fi

./gradlew bundleRelease

AAB="$ROOT/app/build/outputs/bundle/release/app-release.aab"
if [[ ! -f "$AAB" ]]; then
  echo "bundleRelease did not write $AAB" >&2
  exit 1
fi

stale="$(find "$ROOT/app/src/main" -type f \( -name '*.kt' -o -name '*.xml' -o -name '*.webp' \) -newer "$AAB" | head)"
if [[ -n "$stale" ]]; then
  echo "source newer than AAB, rebuilding" >&2
  ./gradlew bundleRelease
fi

ls -lh "$AAB"
echo "AAB=$AAB"
echo "CODE=$(gradle_version_code)"
echo "NAME=$(gradle_version_name)"
