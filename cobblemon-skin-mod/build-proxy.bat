@echo off
echo [CobblemonSkin] Building with proxy 127.0.0.1:7897 ...

set GRADLE_OPTS=-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7897 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7897 -Dhttp.nonProxyHosts=

if "%~1"=="" (
    call .\gradlew.bat build
) else (
    call .\gradlew.bat %*
)

echo.
if %ERRORLEVEL% == 0 (
    echo [CobblemonSkin] SUCCESS.
    dir /b build\libs\cobblemon-skin-mod-*.jar 2>nul
) else (
    echo [CobblemonSkin] FAILED. See output above.
)

echo.
echo [CobblemonSkin] Stopping Gradle daemon to free memory...
call .\gradlew.bat --stop >nul 2>&1
