#!/bin/zsh
# Fast Tablet_10in emulator for PrayerAthan UI work. Leaves it running.
set -euo pipefail

export ANDROID_HOME="${ANDROID_HOME:-/Volumes/SamsungT7/Android/LibraryAndroid/sdk}"
export ANDROID_AVD_HOME="${ANDROID_AVD_HOME:-/Volumes/SamsungT7/Android/.android/avd}"
export PATH="$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$PATH"

AVD="${1:-Tablet_10in}"

if adb devices 2>/dev/null | rg -q '^emulator-[0-9]+\s+device$'; then
  echo "Emulator already up:"
  adb devices
  exit 0
fi

# Quick boot from snapshot, host GPU on Apple Silicon. No -no-snapshot-load.
nohup emulator -avd "$AVD" -gpu host -no-boot-anim -no-snapshot-save >/tmp/prayerathan-emulator.log 2>&1 &
disown

echo "Starting $AVD (quick boot, host GPU). Log: /tmp/prayerathan-emulator.log"
echo "Waiting for boot..."
for _ in {1..90}; do
  if adb wait-for-device 2>/dev/null; then
    boot="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')"
    if [[ "$boot" == "1" ]]; then
      adb shell settings put global window_animation_scale 0 >/dev/null 2>&1 || true
      adb shell settings put global transition_animation_scale 0 >/dev/null 2>&1 || true
      adb shell settings put global animator_duration_scale 0 >/dev/null 2>&1 || true
      echo "Ready: $(adb devices | rg emulator)"
      exit 0
    fi
  fi
  sleep 2
done

echo "Emulator process started but boot did not finish in 3 minutes. Check /tmp/prayerathan-emulator.log" >&2
exit 1
