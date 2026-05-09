#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAVAC="${JAVAC:-/opt/homebrew/opt/openjdk/bin/javac}"

rm -rf "$PROJECT_DIR/out"
mkdir -p "$PROJECT_DIR/out"
"$JAVAC" -encoding UTF-8 -d "$PROJECT_DIR/out" $(find "$PROJECT_DIR/src/main/java" -name '*.java' | sort)
