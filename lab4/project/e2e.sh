#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LAB_DIR="$(cd "$PROJECT_DIR/.." && pwd)"
AUDIT_DIR="$LAB_DIR/audit"
mkdir -p "$AUDIT_DIR"

cd "$PROJECT_DIR"
mvn -q -DskipTests package

PORT="$(python3 - <<'PY'
import socket
with socket.socket() as s:
    s.bind(("127.0.0.1", 0))
    print(s.getsockname()[1])
PY
)"

DB_PATH="$AUDIT_DIR/e2e.db"
SERVER_LOG="$AUDIT_DIR/e2e-server.log"
RESULT_LOG="$AUDIT_DIR/e2e-output.txt"
rm -f "$DB_PATH" "$SERVER_LOG" "$RESULT_LOG"

JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home}"
PORT="$PORT" "$JAVA_HOME/bin/java" -Dmillionaire.db="$DB_PATH" -jar target/millionaire-lab4-1.0.0.jar >"$SERVER_LOG" 2>&1 &
SERVER_PID="$!"
cleanup() {
    kill "$SERVER_PID" >/dev/null 2>&1 || true
}
trap cleanup EXIT

for _ in $(seq 1 40); do
    if curl -fs "http://127.0.0.1:$PORT/" >/dev/null; then
        break
    fi
    sleep 0.25
done

PAGE="$(curl -fsS "http://127.0.0.1:$PORT/")"
START_JSON="$(curl -fsS "http://127.0.0.1:$PORT/api/game/start" \
    -H 'Content-Type: application/json' \
    -d '{"playerName":"E2E","safeLevel":5}')"

GAME_ID="$(python3 - "$START_JSON" <<'PY'
import json
import sys
print(json.loads(sys.argv[1])["gameId"])
PY
)"

HINT_JSON="$(curl -fsS "http://127.0.0.1:$PORT/api/game/$GAME_ID/hint/fifty-fifty" \
    -H 'Content-Type: application/json' \
    -d '{}')"
RECORDS_JSON="$(curl -fsS "http://127.0.0.1:$PORT/api/records")"

python3 - "$PAGE" "$START_JSON" "$HINT_JSON" "$RECORDS_JSON" "$RESULT_LOG" <<'PY'
import json
import sys
from pathlib import Path

page, start_raw, hint_raw, records_raw, result_path = sys.argv[1:]
start = json.loads(start_raw)
hint = json.loads(hint_raw)
records = json.loads(records_raw)

assert "Кто хочет стать миллионером?" in page
assert start["status"] == "ACTIVE"
assert start["level"] == 1
assert len(start["question"]["answers"]) == 4
assert hint["hintsUsed"] == 1
assert len(hint["disabledAnswers"]) == 2
assert isinstance(records, list)

Path(result_path).write_text(
    "E2E OK\n"
    f"gameId={start['gameId']}\n"
    f"questionLevel={start['question']['level']}\n"
    f"disabledAfterFifty={hint['disabledAnswers']}\n",
    encoding="utf-8",
)
PY

echo "E2E OK: http://127.0.0.1:$PORT"
echo "Audit: $RESULT_LOG"
