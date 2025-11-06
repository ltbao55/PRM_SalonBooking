# PowerShell script để tạo release keystore và lấy SHA-1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Creating Release Keystore" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Tìm keytool
$keytoolPath = $null

$possiblePaths = @(
    "C:\Program Files\Java\jdk-21\bin\keytool.exe",
    "C:\Program Files\Java\jdk-17\bin\keytool.exe",
    "C:\Program Files\Java\jdk-11\bin\keytool.exe",
    "$env:JAVA_HOME\bin\keytool.exe"
)

foreach ($path in $possiblePaths) {
    if (Test-Path $path) {
        $keytoolPath = $path
        break
    }
}

if (-not $keytoolPath) {
    # Thử tìm trong PATH
    $keytool = Get-Command keytool -ErrorAction SilentlyContinue
    if ($keytool) {
        $keytoolPath = $keytool.Path
    }
}

if (-not $keytoolPath) {
    Write-Host "ERROR: Cannot find keytool. Please ensure JAVA_HOME is set or JDK is installed." -ForegroundColor Red
    Write-Host "Trying to find Java installation..." -ForegroundColor Yellow
    
    # Tìm trong Program Files
    $javaPaths = Get-ChildItem "C:\Program Files\Java" -ErrorAction SilentlyContinue | Where-Object { $_.PSIsContainer -and $_.Name -like "jdk*" }
    foreach ($javaPath in $javaPaths) {
        $testPath = Join-Path $javaPath.FullName "bin\keytool.exe"
        if (Test-Path $testPath) {
            $keytoolPath = $testPath
            Write-Host "Found keytool at: $keytoolPath" -ForegroundColor Green
            break
        }
    }
}

if (-not $keytoolPath) {
    Write-Host "ERROR: Cannot find keytool. Please install JDK or set JAVA_HOME." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Using keytool at: $keytoolPath" -ForegroundColor Green
Write-Host ""

# Tạo keystore nếu chưa tồn tại
if (-not (Test-Path "release.keystore")) {
    Write-Host "Creating release keystore..." -ForegroundColor Yellow
    & $keytoolPath -genkey -v -keystore release.keystore -alias prm_salon_release -keyalg RSA -keysize 2048 -validity 10000 -storepass prm_salon_2024 -keypass prm_salon_2024 -dname "CN=PRM Salon Booking, OU=Mobile, O=PRM Salon, L=Ho Chi Minh, ST=Ho Chi Minh, C=VN"
    Write-Host ""
    Write-Host "Keystore created successfully!" -ForegroundColor Green
} else {
    Write-Host "Keystore already exists. Skipping creation." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Getting SHA-1 Fingerprint" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Lấy SHA-1
$output = & $keytoolPath -list -v -keystore release.keystore -alias prm_salon_release -storepass prm_salon_2024 2>&1

$sha1Line = $output | Select-String "SHA1:" | Select-Object -First 1
if ($sha1Line) {
    $sha1 = ($sha1Line -split "SHA1:")[1].Trim()
    Write-Host "SHA-1: $sha1" -ForegroundColor Green
    $sha1 | Out-File -FilePath "release_sha1.txt" -Encoding UTF8
    Write-Host ""
    Write-Host "SHA-1 saved to release_sha1.txt" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Getting SHA-256 Fingerprint (for reference)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$sha256Line = $output | Select-String "SHA256:" | Select-Object -First 1
if ($sha256Line) {
    $sha256 = ($sha256Line -split "SHA256:")[1].Trim()
    Write-Host "SHA-256: $sha256" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IMPORTANT: Add SHA-1 to Firebase Console" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Go to Firebase Console: https://console.firebase.google.com" -ForegroundColor White
Write-Host "2. Select your project: prm-salonbooking" -ForegroundColor White
Write-Host "3. Go to Project Settings > Your apps > Android app" -ForegroundColor White
Write-Host "4. Click 'Add fingerprint' and paste the SHA-1 above" -ForegroundColor White
Write-Host "5. Download the updated google-services.json and replace the current one" -ForegroundColor White
Write-Host ""
Read-Host "Press Enter to exit"


