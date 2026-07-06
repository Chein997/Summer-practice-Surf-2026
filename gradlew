#!/usr/bin/env sh
set -e

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

cat <<'EOF'
Gradle is not installed.

Install it first:

  brew install gradle

Then run:

  ./gradlew tasks
  ./gradlew :webApp:wasmJsBrowserDevelopmentRun

After that you can generate the official Gradle Wrapper:

  gradle wrapper --gradle-version 8.10.2
  chmod +x ./gradlew

EOF

exit 127
