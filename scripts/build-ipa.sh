#!/usr/bin/env bash
#
# Archive the iOS app and export a signed .ipa into iCloud Drive → Downloads.
#
# Requirements:
#   - Xcode signed in to the Apple ID for team Z5UZYST2WL
#   - A provisioning profile covering this app (development, or distribution if
#     you switch exportOptions.plist `method`)
#
# Usage:  ./scripts/build-ipa.sh
set -euo pipefail

cd "$(dirname "$0")/.."  # repo root (panashe-bible-app)

SCHEME="Panashe Bible"
PROJECT="iosApp/iosApp.xcodeproj"
CONFIG="Release"
ARCHIVE="build/PanasheBible.xcarchive"
OPTS="iosApp/exportOptions.plist"
EXPORT_DIR="$HOME/Library/Mobile Documents/com~apple~CloudDocs/Downloads"

mkdir -p "$EXPORT_DIR"

echo "▶ Archiving (generic iOS device / arm64, $CONFIG)…"
xcodebuild archive \
  -project "$PROJECT" \
  -scheme "$SCHEME" \
  -configuration "$CONFIG" \
  -destination 'generic/platform=iOS' \
  -archivePath "$ARCHIVE" \
  -allowProvisioningUpdates

echo "▶ Exporting .ipa → $EXPORT_DIR …"
xcodebuild -exportArchive \
  -archivePath "$ARCHIVE" \
  -exportOptionsPlist "$OPTS" \
  -exportPath "$EXPORT_DIR" \
  -allowProvisioningUpdates

echo "✅ Done. .ipa(s) in iCloud Drive → Downloads:"
ls -lh "$EXPORT_DIR"/*.ipa 2>/dev/null || echo "(no .ipa found — check export log above)"
