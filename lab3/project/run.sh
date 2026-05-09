#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAVA="${JAVA:-/opt/homebrew/opt/openjdk/bin/java}"

"$PROJECT_DIR/build.sh"
"$JAVA" -cp "$PROJECT_DIR/out" ru.hse.lab3.app.Demo
