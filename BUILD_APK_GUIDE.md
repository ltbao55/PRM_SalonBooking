# Hướng Dẫn Build APK Release

## Vấn Đề Hiện Tại
Gradle/Kotlin compiler không hỗ trợ Java 25. Cần JDK 17 hoặc 21 để build.

## Giải Pháp

### Cách 1: Build bằng Android Studio (Khuyến nghị - Đơn giản nhất)

1. Mở project trong Android Studio
2. Vào menu: **Build > Build Bundle(s) / APK(s) > Build APK(s)**
3. Chọn **release** variant
4. Android Studio sẽ tự động dùng JDK tương thích
5. APK sẽ được tạo tại: `app/build/outputs/apk/release/app-release.apk`

### Cách 2: Cài JDK 17 và Build từ Command Line

#### Bước 1: Tải và cài JDK 17
- Tải JDK 17 từ: https://adoptium.net/temurin/releases/?version=17
- Cài đặt vào: `C:\Program Files\Java\jdk-17`

#### Bước 2: Cập nhật gradle.properties
Mở file `gradle.properties` và thêm/sửa:
```properties
org.gradle.java.home=C:\\Program Files\\Java\\jdk-17
```

#### Bước 3: Build APK
```bash
.\gradlew.bat assembleRelease
```

APK sẽ được tạo tại: `app\build\outputs\apk\release\app-release.apk`

### Cách 3: Dùng JDK 21 (nếu có)
Nếu bạn đã có JDK 21, cập nhật `gradle.properties`:
```properties
org.gradle.java.home=C:\\Program Files\\Java\\jdk-21
```

## Kiểm Tra APK Sau Khi Build

Sau khi build thành công, APK sẽ nằm tại:
```
app\build\outputs\apk\release\app-release.apk
```

APK này đã được ký bằng release keystore và sẵn sàng để:
- Test trên thiết bị thật
- Upload lên Google Play Store
- Phân phối cho người dùng

## Lưu Ý

1. **Release keystore** đã được cấu hình tự động
2. **SHA-1** đã được lấy: `86:72:A6:8A:2A:2D:36:C4:A7:4A:06:59:65:D0:3A:BA:0A:12:1F:F2`
3. **Nhớ thêm SHA-1 vào Firebase Console** nếu chưa làm (xem GOOGLE_SIGNIN_FIX.md)

## Troubleshooting

### Lỗi "Unresolved reference: util" hoặc "Unresolved reference: io"
- Đã sửa trong build.gradle.kts, không cần lo

### Lỗi "Java 25 not supported"
- Dùng Android Studio để build (Cách 1)
- Hoặc cài JDK 17/21 (Cách 2/3)

### Lỗi "Keystore not found"
- Đảm bảo file `app/release.keystore` tồn tại
- Nếu chưa có, chạy: `app\create-release-keystore.ps1`


