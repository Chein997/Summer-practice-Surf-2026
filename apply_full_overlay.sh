#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="${1:-.}"

if [ ! -d "$ROOT_DIR" ]; then
  echo "Target directory does not exist: $ROOT_DIR" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Applying Apex full implementation overlay to: $ROOT_DIR"

cp -R "$SCRIPT_DIR/backend" "$ROOT_DIR/" 2>/dev/null || true
cp -R "$SCRIPT_DIR/client" "$ROOT_DIR/" 2>/dev/null || true
cp -R "$SCRIPT_DIR/docs" "$ROOT_DIR/" 2>/dev/null || true
cp -R "$SCRIPT_DIR/reports" "$ROOT_DIR/" 2>/dev/null || true

if [ -f "$ROOT_DIR/.gitignore" ]; then
  grep -qxF ".DS_Store" "$ROOT_DIR/.gitignore" || echo ".DS_Store" >> "$ROOT_DIR/.gitignore"
else
  echo ".DS_Store" > "$ROOT_DIR/.gitignore"
fi

find "$ROOT_DIR" -name ".DS_Store" -delete

echo "Done."
echo "Next checks:"
echo "  cd $ROOT_DIR/backend && make db-up && make migrate && make test"
echo "  cd $ROOT_DIR/client && ./gradlew :shared:allTests && ./gradlew :androidApp:assembleDebug"
