#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
./scripts/compile.sh
exec java -cp build/classes Main "$@"
