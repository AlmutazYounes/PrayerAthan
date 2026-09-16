#!/bin/zsh
# Rebuild adaptive launcher layers from the minaret candidate.
#
# Android adaptive icon spec (API 26+):
#   - Foreground + background layers: 108 x 108 dp each
#   - Safe zone: 66 x 66 dp circle (never clipped by OEM masks)
#   - Outer 18 dp per side reserved for parallax / motion
#   - xxxhdpi foreground: 432 x 432 px
#   - Monochrome: white silhouette on transparent (system tints it)
#
# Play hi-res icon: 512 x 512 PNG (flattened preview, same safe padding).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
# Opaque dark candidate (not the checkerboard-baked export).
SRC="$ROOT/design/candidates/minaret-00-1024.png"
BG="#1C1614"
MASTER=1024
SAFE_PX=$(( MASTER * 66 / 108 )) # 625 @ 1024

if [[ ! -f "$SRC" ]]; then
  echo "missing source: $SRC" >&2
  exit 1
fi

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

# Knock out the dark umber fill → gold stroke on true transparency.
# Fit inside the 66dp safe box, center on a transparent 108dp canvas.
magick "$SRC" -alpha set -channel RGBA -fuzz 18% -fill none \
  -opaque "srgb(21,16,14)" +channel \
  -trim +repage \
  -resize "${SAFE_PX}x${SAFE_PX}>" \
  -background none -gravity center -extent "${MASTER}x${MASTER}" \
  "$WORK/foreground.png"

magick -size "${MASTER}x${MASTER}" "xc:$BG" \
  "$WORK/foreground.png" -gravity center -compose over -composite \
  "$ROOT/design/ic_launcher.png"

cp "$WORK/foreground.png" "$ROOT/design/ic_launcher_foreground.png"
cp "$WORK/foreground.png" "$ROOT/design/ic_launcher_minaret_foreground.png"
magick "$ROOT/design/ic_launcher.png" -resize 512x512 "$ROOT/store/listing/icon-512.png"

# White monochrome silhouette, transparent outside the stroke.
magick "$WORK/foreground.png" \
  \( +clone -alpha extract \) \
  \( -clone 0 -fill white -colorize 100 \) \
  -delete 0 \
  -compose CopyOpacity -composite \
  PNG32:"$ROOT/app/src/main/res/drawable-nodpi/ic_launcher_monochrome.png"

declare -A LEGACY=(mdpi 48 hdpi 72 xhdpi 96 xxhdpi 144 xxxhdpi 192)
declare -A FG=(mdpi 108 hdpi 162 xhdpi 216 xxhdpi 324 xxxhdpi 432)

for d in mdpi hdpi xhdpi xxhdpi xxxhdpi; do
  out="$ROOT/app/src/main/res/mipmap-$d"
  magick "$WORK/foreground.png" -resize "${FG[$d]}x${FG[$d]}" "$out/ic_launcher_foreground.png"
  magick "$ROOT/design/ic_launcher.png" -resize "${LEGACY[$d]}x${LEGACY[$d]}" "$out/ic_launcher.png"
  cp "$out/ic_launcher.png" "$out/ic_launcher_round.png"
done

echo "built launcher icons (safe zone ${SAFE_PX}px @ ${MASTER}px, bg $BG)"
echo "fg alpha_mean=$(magick "$WORK/foreground.png" -format '%[fx:mean.a]' info:)"
echo "mono alpha_mean=$(magick "$ROOT/app/src/main/res/drawable-nodpi/ic_launcher_monochrome.png" -format '%[fx:mean.a]' info:)"
