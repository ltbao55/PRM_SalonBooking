# Hướng Dẫn Sửa Lỗi Google Sign-In cho Release APK

## Vấn Đề
Google Sign-In chỉ hoạt động trên debug build nhưng không hoạt động trên release APK. Điều này xảy ra vì:
- SHA-1 fingerprint của debug keystore và release keystore khác nhau
- Firebase Console chỉ có SHA-1 của debug keystore
- Cần thêm SHA-1 của release keystore vào Firebase Console

## Giải Pháp Đã Thực Hiện

### ✅ 1. Tạo Release Keystore
Đã tạo release keystore tại: `app/release.keystore`
- Alias: `prm_salon_release`
- Password: `prm_salon_2024`
- Validity: 10,000 days

### ✅ 2. Cấu Hình Signing
Đã cập nhật `app/build.gradle.kts` để tự động sử dụng release keystore khi build release APK.

### ✅ 3. SHA-1 Fingerprint
**SHA-1 Release Keystore:**
```
86:72:A6:8A:2A:2D:36:C4:A7:4A:06:59:65:D0:3A:BA:0A:12:1F:F2
```

**SHA-256 (để tham khảo):**
```
AA:E6:59:8D:B1:33:7F:44:34:B8:08:55:C3:3D:D8:54:71:B1:EF:3B:F0:58:B4:D5:11:9E:BB:BB:88:89:9E:6D
```

## Bước Tiếp Theo - Thêm SHA-1 vào Firebase Console

### Cách 1: Thêm Thủ Công (Khuyến nghị)

1. **Truy cập Firebase Console**
   - Vào: https://console.firebase.google.com
   - Đăng nhập và chọn project: **prm-salonbooking**

2. **Vào Project Settings**
   - Click vào biểu tượng ⚙️ (Settings) ở góc trên bên trái
   - Chọn "Project settings"

3. **Thêm SHA-1 Fingerprint**
   - Scroll xuống phần "Your apps"
   - Tìm Android app với package name: `com.example.prm_be`
   - Click vào "Add fingerprint"
   - Paste SHA-1 release: `86:72:A6:8A:2A:2D:36:C4:A7:4A:06:59:65:D0:3A:BA:0A:12:1F:F2`
   - Click "Save"

4. **Tải lại google-services.json**
   - Sau khi thêm SHA-1, Firebase sẽ tự động tạo OAuth client mới
   - Click vào "google-services.json" để tải file mới
   - Hoặc download từ phần "Your apps" > Android app > "Download google-services.json"
   - **Thay thế** file `app/google-services.json` hiện tại bằng file mới

5. **Rebuild và Test**
   - Build lại release APK: `./gradlew assembleRelease`
   - Hoặc trong Android Studio: Build > Generate Signed Bundle / APK
   - Test Google Sign-In trên release APK

### Cách 2: Sử dụng Firebase CLI (Nâng cao)

Nếu bạn có Firebase CLI được cài đặt và đã login:

```bash
# Lấy SHA-1 từ keystore (nếu cần)
cd app
keytool -list -v -keystore release.keystore -alias prm_salon_release -storepass prm_salon_2024 | findstr SHA1

# Thêm SHA-1 vào Firebase (yêu cầu Firebase CLI)
firebase projects:list
firebase apps:android:sha:create prm-salonbooking --sha 86:72:A6:8A:2A:2D:36:C4:A7:4A:06:59:65:D0:3A:BA:0A:12:1F:F2
```

## Lấy Lại SHA-1 Nếu Cần

Nếu bạn cần lấy lại SHA-1, chạy script:

**Windows (PowerShell):**
```powershell
cd app
.\create-release-keystore.ps1
```

**Windows (CMD):**
```cmd
cd app
create-release-keystore.bat
```

SHA-1 sẽ được lưu trong file `app/release_sha1.txt`

## Kiểm Tra SHA-1 Thủ Công

Nếu script không hoạt động, bạn có thể lấy SHA-1 thủ công:

```bash
keytool -list -v -keystore app/release.keystore -alias prm_salon_release -storepass prm_salon_2024
```

Tìm dòng có "SHA1:" và copy giá trị.

## Lưu Ý Quan Trọng

1. **Giữ bảo mật keystore**
   - File `app/release.keystore` và `app/key.properties` đã được thêm vào `.gitignore`
   - **KHÔNG BAO GIỜ** commit các file này lên Git
   - Backup keystore ở nơi an toàn vì mất keystore = không thể update app lên Play Store

2. **Thông tin keystore**
   - Keystore file: `app/release.keystore`
   - Alias: `prm_salon_release`
   - Password: `prm_salon_2024` (cả store và key đều dùng password này)
   - Lưu thông tin này ở nơi an toàn!

3. **Sau khi thêm SHA-1 vào Firebase**
   - Có thể mất vài phút để Firebase cập nhật
   - Đảm bảo tải lại `google-services.json` sau khi thêm SHA-1
   - Build lại APK sau khi thay `google-services.json`

## Troubleshooting

### Google Sign-In vẫn không hoạt động sau khi thêm SHA-1

1. **Kiểm tra google-services.json**
   - Đảm bảo file mới được tải từ Firebase Console
   - Kiểm tra trong file có entry với `certificate_hash` mới chưa

2. **Kiểm tra SHA-1 đã được thêm**
   - Vào Firebase Console > Project Settings > Your apps
   - Xem danh sách SHA-1 fingerprints
   - Đảm bảo SHA-1 release có trong danh sách

3. **Clear cache và rebuild**
   ```bash
   ./gradlew clean
   ./gradlew assembleRelease
   ```

4. **Kiểm tra Logcat**
   - Xem log khi click Google Sign-In
   - Tìm lỗi liên quan đến "SHA-1" hoặc "OAuth client"

### Lỗi "Keystore file not found"

- Đảm bảo file `app/release.keystore` tồn tại
- Nếu chưa có, chạy lại script `create-release-keystore.ps1` hoặc `.bat`

## Script Tự Động

Các script đã được tạo:
- `app/create-release-keystore.ps1` - PowerShell script (Windows)
- `app/create-release-keystore.bat` - Batch script (Windows)

Các script này sẽ:
1. Tự động tìm keytool trong máy
2. Tạo keystore nếu chưa có
3. Lấy SHA-1 và SHA-256 fingerprints
4. Lưu SHA-1 vào `release_sha1.txt`

## Tóm Tắt

✅ **Đã hoàn thành:**
- Tạo release keystore
- Cấu hình signing trong build.gradle.kts
- Lấy SHA-1 fingerprint: `86:72:A6:8A:2A:2D:36:C4:A7:4A:06:59:65:D0:3A:BA:0A:12:1F:F2`

⏳ **Cần thực hiện:**
1. Thêm SHA-1 vào Firebase Console (theo hướng dẫn trên)
2. Tải lại `google-services.json` từ Firebase
3. Build lại release APK và test

Sau khi hoàn thành các bước trên, Google Sign-In sẽ hoạt động trên release APK! 🎉


