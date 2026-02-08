@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   GuardianAC Build Script for Windows 11
echo ============================================
echo.

:: Set working directory to where this script lives
cd /d "%~dp0"

:: Check Java is available
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Java not found! Install JDK 17 from:
    echo         https://adoptium.net/temurin/releases/?version=17
    echo         Download the Windows x64 .msi installer.
    pause
    exit /b 1
)

echo [OK] Java found:
java -version 2>&1 | findstr /i "version"
echo.

:: Set up portable Maven
set "MAVEN_DIR=%cd%\.maven"
set "MAVEN_ZIP=%MAVEN_DIR%\maven.zip"

:: Find existing mvn.cmd if already downloaded
set "MVN="
if exist "%MAVEN_DIR%" (
    for /r "%MAVEN_DIR%" %%f in (mvn.cmd) do (
        if exist "%%f" set "MVN=%%f"
    )
)

if defined MVN (
    echo [OK] Maven already downloaded: !MVN!
) else (
    echo [INFO] Downloading Maven 3.9.6 portable...
    if not exist "%MAVEN_DIR%" mkdir "%MAVEN_DIR%"

    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%MAVEN_ZIP%'"

    if not exist "%MAVEN_ZIP%" (
        echo [WARN] Primary download failed, trying mirror...
        powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%MAVEN_ZIP%'"
    )

    if not exist "%MAVEN_ZIP%" (
        echo [ERROR] Download failed. Please download Maven manually:
        echo         https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip
        echo         Extract it into: %MAVEN_DIR%\
        pause
        exit /b 1
    )

    echo [INFO] Extracting Maven...
    powershell -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%MAVEN_DIR%' -Force"
    del "%MAVEN_ZIP%" 2>nul

    :: Find mvn.cmd in whatever folder got extracted
    for /r "%MAVEN_DIR%" %%f in (mvn.cmd) do (
        if exist "%%f" set "MVN=%%f"
    )

    if not defined MVN (
        echo [ERROR] Maven extracted but mvn.cmd not found!
        echo         Check contents of: %MAVEN_DIR%
        dir /s /b "%MAVEN_DIR%"
        pause
        exit /b 1
    )

    echo [OK] Maven ready: !MVN!
)

echo.
echo [INFO] Building GuardianAC...
echo ============================================
echo.

:: Debug: show what we're running
echo [DEBUG] Running: "!MVN!" clean package
echo [DEBUG] Working dir: %cd%
echo.

call "!MVN!" clean package

if %errorlevel% neq 0 (
    echo.
    echo ============================================
    echo [FAILED] Build failed! Check errors above.
    echo ============================================
    pause
    exit /b 1
)

echo.
echo ============================================
echo [SUCCESS] Build complete!
echo.
echo   JAR file: %~dp0target\GuardianAC-1.0.0-SNAPSHOT.jar
echo.
echo   Copy this file to your server's plugins\ folder
echo   and restart your Paper 1.21.1 server.
echo ============================================
echo.

:: Ask if user wants to copy to server
set /p COPY_JAR="Copy JAR to a server plugins folder? (y/n): "
if /i "%COPY_JAR%"=="y" (
    set /p SERVER_PATH="Enter your server plugins folder path (e.g. C:\server\plugins): "
    if defined SERVER_PATH (
        copy "%~dp0target\GuardianAC-1.0.0-SNAPSHOT.jar" "%SERVER_PATH%\" /Y
        echo [OK] Copied to %SERVER_PATH%
    )
)

pause
