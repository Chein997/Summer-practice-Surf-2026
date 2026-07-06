@echo off
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  gradle %*
  exit /b %ERRORLEVEL%
)

echo Gradle is not installed.
echo Install Gradle, then run:
echo   gradle tasks
exit /b 127
