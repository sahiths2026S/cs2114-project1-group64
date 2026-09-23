#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/classes
javac --release 11 -encoding UTF-8 -Xlint:all -d build/classes Pill_Roulette/src/*.java
