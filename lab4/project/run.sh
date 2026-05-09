#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"
mvn -q -DskipTests package
JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home}"
PORT="${PORT:-8080}" "$JAVA_HOME/bin/java" -Dmillionaire.db="${MILLIONAIRE_DB:-data/millionaire.db}" -jar target/millionaire-lab4-1.0.0.jar
