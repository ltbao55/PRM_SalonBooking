@echo off
REM Script để tạo release keystore và lấy SHA-1

echo ========================================
echo Creating Release Keystore
echo ========================================

REM Tìm keytool trong các vị trí thông thường
set KEYTOOL_PATH=

if exist "C:\Program Files\Java\jdk-21\bin\keytool.exe" (
    set KEYTOOL_PATH=C:\Program Files\Java\jdk-21\bin\keytool.exe
) else if exist "C:\Program Files\Java\jdk-17\bin\keytool.exe" (
    set KEYTOOL_PATH=C:\Program Files\Java\jdk-17\bin\keytool.exe
) else if exist "C:\Program Files\Java\jdk-11\bin\keytool.exe" (
    set KEYTOOL_PATH=C:\Program Files\Java\jdk-11\bin\keytool.exe
) else if exist "%JAVA_HOME%\bin\keytool.exe" (
    set KEYTOOL_PATH=%JAVA_HOME%\bin\keytool.exe
) else (
    echo ERROR: Cannot find keytool. Please ensure JAVA_HOME is set or JDK is installed.
    pause
    exit /b 1
)

echo Using keytool at: %KEYTOOL_PATH%
echo.

REM Tạo keystore nếu chưa tồn tại
if not exist "release.keystore" (
    echo Creating release keystore...
    "%KEYTOOL_PATH%" -genkey -v -keystore release.keystore -alias prm_salon_release -keyalg RSA -keysize 2048 -validity 10000 -storepass prm_salon_2024 -keypass prm_salon_2024 -dname "CN=PRM Salon Booking, OU=Mobile, O=PRM Salon, L=Ho Chi Minh, ST=Ho Chi Minh, C=VN"
    echo.
    echo Keystore created successfully!
) else (
    echo Keystore already exists. Skipping creation.
)

echo.
echo ========================================
echo Getting SHA-1 Fingerprint
echo ========================================
echo.

"%KEYTOOL_PATH%" -list -v -keystore release.keystore -alias prm_salon_release -storepass prm_salon_2024 | findstr /C:"SHA1:"

echo.
echo ========================================
echo Getting SHA-256 Fingerprint (for reference)
echo ========================================
echo.

"%KEYTOOL_PATH%" -list -v -keystore release.keystore -alias prm_salon_release -storepass prm_salon_2024 | findstr /C:"SHA256:"

echo.
echo ========================================
echo Getting SHA-1 (formatted for Firebase)
echo ========================================
echo.

for /f "tokens=2 delims=:" %%a in ('"%KEYTOOL_PATH%" -list -v -keystore release.keystore -alias prm_salon_release -storepass prm_salon_2024 ^| findstr /C:"SHA1:"') do (
    set SHA1=%%a
    set SHA1=!SHA1: =!
    echo SHA-1: !SHA1!
    echo !SHA1! > release_sha1.txt
)

echo.
echo SHA-1 saved to release_sha1.txt
echo.
echo IMPORTANT: Add this SHA-1 to Firebase Console:
echo 1. Go to Firebase Console: https://console.firebase.google.com
echo 2. Select your project: prm-salonbooking
echo 3. Go to Project Settings > Your apps > Android app
echo 4. Click "Add fingerprint" and paste the SHA-1 above
echo 5. Download the updated google-services.json and replace the current one
echo.
pause


