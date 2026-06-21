#!/bin/bash
set -e

cd "$(dirname "$0")/.."

echo "Building iOS framework..."
./gradlew :composeApp:compileKotlinIosSimulatorArm64

echo "To build IPA, run Archive in Xcode."
