# 🔥 Hướng Dẫn Thiết Lập Firebase

## Bước 1: Tạo Firebase Project

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" hoặc chọn project có sẵn
3. Đặt tên project: **Salon Booking** (hoặc tên bạn muốn)
4. Bật **Google Analytics** (tùy chọn)
5. Click "Create project"

## Bước 2: Thêm Android App vào Firebase Project

1. Trong Firebase Console, click vào biểu tượng **Android** (hoặc "Add app")
2. Nhập **Android package name**: `com.example.prm_be`
   - Lấy từ file `app/build.gradle.kts` → `applicationId`
3. Nhập **App nickname**: Salon Booking (tùy chọn)
4. **SHA-1**: Bỏ qua (không bắt buộc cho dev, nhưng cần cho Production)
5. Click "Register app"

## Bước 3: Tải file `google-services.json`

1. Firebase sẽ hiển thị file `google-services.json`
2. Tải file này về máy
3. **QUAN TRỌNG**: Copy file `google-services.json` vào thư mục:
   ```
   app/
   └── google-services.json
   ```
   - File phải nằm ở `app/google-services.json` (cùng cấp với `build.gradle.kts`)

## Bước 4: Bật Firebase Services

### 4.1. Firebase Authentication
1. Vào **Authentication** trong Firebase Console
2. Click "Get started"
3. Vào tab **Sign-in method**
4. Bật **Email/Password**
   - Click vào "Email/Password"
   - Bật toggle "Enable"
   - Click "Save"

### 4.2. Firestore Database
1. Vào **Firestore Database** trong Firebase Console
2. Click "Create database"
3. Chọn **Start in test mode** (cho development)
   - ⚠️ Lưu ý: Test mode cho phép đọc/ghi không giới hạn trong 30 ngày
   - Sau đó bạn nên thiết lập Security Rules
4. Chọn **Location** (ví dụ: `us-central` hoặc `asia-southeast1` cho Việt Nam)
5. Click "Enable"

### 4.3. Firebase Storage (Tùy chọn - cho upload ảnh)
1. Vào **Storage** trong Firebase Console
2. Click "Get started"
3. Chọn "Start in test mode"
4. Chọn Location
5. Click "Done"

## Bước 5: Thiết Lập Security Rules (QUAN TRỌNG - Sau khi hoàn thành dev)

### Firestore Rules (tạm thời - test mode)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true; // TẠM THỜI - CHỈ CHO TEST
    }
  }
}
```

⚠️ **Lưu ý**: Rules trên cho phép mọi người đọc/ghi. Sau khi deploy, bạn PHẢI thay đổi rules phù hợp!

### Storage Rules (tạm thời - test mode)
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null; // Chỉ user đã đăng nhập mới upload được
    }
  }
}
```

## Bước 6: Tạo Dữ Liệu Mẫu (Sample Data)

Sau khi thiết lập xong, bạn có thể tạo dữ liệu mẫu trong Firestore:

### Collection: `salons`
Document ID: `salon1`
```json
{
  "id": "salon1",
  "name": "Salon Đẹp",
  "address": "123 Đường ABC, Quận 1, TP.HCM",
  "imageUrl": "https://example.com/salon1.jpg"
}
```

### Subcollection: `salons/salon1/services`
Document ID: `service1`
```json
{
  "id": "service1",
  "name": "Cắt tóc",
  "price": 100000
}
```

Document ID: `service2`
```json
{
  "id": "service2",
  "name": "M.u.i",
  "price": 200000
}
```

### Subcollection: `salons/salon1/stylists` (Tùy chọn)
Document ID: `stylist1`
```json
{
  "id": "stylist1",
  "name": "Nguyễn Văn A",
  "salonId": "salon1",
  "imageUrl": "https://example.com/stylist1.jpg",
  "specialization": "Haircut"
}
```

## Bước 7: Sync Gradle

Sau khi thêm `google-services.json`, bạn cần:
1. Sync Gradle Files (Click "Sync Now" nếu Android Studio hỏi)
2. Hoặc chạy: `./gradlew build`

## ✅ Kiểm Tra

Sau khi hoàn thành, bạn có thể test bằng cách:
1. Build project (không bị lỗi)
2. Chạy app và test các method trong `FirebaseRepo`
3. Kiểm tra Firebase Console xem dữ liệu đã được tạo chưa

## 📝 Lưu Ý Quan Trọng

1. **KHÔNG commit `google-services.json` vào Git** (nếu project public)
   - Thêm vào `.gitignore`: `app/google-services.json`
   - Mỗi dev sẽ tải file riêng của mình

2. **Mỗi dev cần tải `google-services.json` riêng** hoặc dùng chung file (nếu cùng Firebase project)

3. **Security Rules**: Sau khi hoàn thành dev, BẮT BUỘC phải thiết lập Security Rules phù hợp!

---

**Chúc bạn thành công! 🚀**

