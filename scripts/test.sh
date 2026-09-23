#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
readonly JUNIT_VERSION=1.11.4
readonly JUNIT_JAR=".deps/junit-platform-console-standalone-${JUNIT_VERSION}.jar"
readonly JUNIT_SHA256=b016ef6b1c3454d6d7c2c88ce081dabf289699686af6622d6e4e2e1b54b4a2fc
mkdir -p .deps build/test-classes build/test-results
if [[ ! -f "$JUNIT_JAR" ]]; then
    echo "Downloading the pinned JUnit test runner..."
    curl --fail --location --retry 2 --output "${JUNIT_JAR}.tmp" \
        "https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/${JUNIT_VERSION}/junit-platform-console-standalone-${JUNIT_VERSION}.jar"
    mv "${JUNIT_JAR}.tmp" "$JUNIT_JAR"
fi
if command -v shasum >/dev/null 2>&1; then
    actual_checksum=$(shasum -a 256 "$JUNIT_JAR")
else
    actual_checksum=$(sha256sum "$JUNIT_JAR")
fi
if [[ "${actual_checksum%% *}" != "$JUNIT_SHA256" ]]; then
    echo "JUnit checksum mismatch. Remove $JUNIT_JAR and rerun this script." >&2
    exit 1
fi
./scripts/compile.sh
javac --release 11 -encoding UTF-8 -Xlint:all -cp "build/classes:$JUNIT_JAR" \
    -d build/test-classes Pill_Roulette/test/*.java
java -jar "$JUNIT_JAR" execute --class-path build/classes:build/test-classes \
    --scan-class-path --fail-if-no-tests --disable-ansi-colors \
    --reports-dir build/test-results "$@"
