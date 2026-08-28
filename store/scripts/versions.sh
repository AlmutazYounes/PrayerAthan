# shellcheck shell=zsh
# Expects ROOT to be the repo root. Sourced, not executed.

gradle_version_code() {
  perl -ne 'print $1 if /^\s*versionCode\s*=\s*(\d+)/' "$ROOT/app/build.gradle.kts"
}

gradle_version_name() {
  perl -ne 'print $1 if /^\s*versionName\s*=\s*"([^"]+)"/' "$ROOT/app/build.gradle.kts"
}

readme_live_code() {
  perl -ne 'print $1 if /Internal testing \| Live\. Release (\d+)/' "$ROOT/store/README.md"
}
