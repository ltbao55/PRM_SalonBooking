# 🚀 Hướng Dẫn Setup Cho Team - Salon Booking App

## 👋 Chào Mừng Đến Với Dự Án!

File này là **hướng dẫn duy nhất** để setup môi trường development cho dự án **Salon Booking App**.

---

## ✅ Yêu Cầu Hệ Thống

- **Android Studio:** Arctic Fox trở lên (khuyến nghị: Latest version)
- **JDK:** 11 hoặc cao hơn
- **Android SDK:** API 33+ (compileSdk 36)
- **Git:** Để clone repository

---

## 📥 Bước 1: Clone Repository

```bash
git clone <repository-url>
cd PRM_SalonBooking
```

**Lưu ý:** [Cần thêm GitHub repository URL]

---

## 🔥 Bước 2: Thiết Lập Firebase

### **2.1. Lấy File `google-services.json`**

**Option A: Copy từ Dev 1 (Nhanh nhất)**
1. Dev 1 sẽ share file `google-services.json` (qua Git, email, hoặc chat)
2. Copy file vào thư mục: `app/google-services.json`
3. Đảm bảo file nằm cùng cấp với `app/build.gradle.kts`

**Option B: Tải từ Firebase Console**
1. Truy cập: https://console.firebase.google.com/project/prm-salonbooking
2. Vào **⚙️ Project Settings** → **Your apps** → Android app
3. Click **"Download google-services.json"**
4. Đặt file vào: `app/google-services.json`

### **2.2. Kiểm Tra Firebase Services**

Đảm bảo các services sau đã được bật (Dev 1 đã setup):
- ✅ **Authentication** → Email/Password: Đã bật
- ✅ **Authentication** → Google: Đã bật
- ✅ **Firestore Database**: Đã tạo
- ✅ **Storage**: Đã bật

**Kiểm tra:**
- Vào Firebase Console → Authentication → Sign-in method
- Xem Email/Password và Google đã bật chưa

---

## 📋 Thông Tin Firebase Configuration

### **Firebase Project Info**
- **Project ID**: `prm-salonbooking`
- **Project Number**: `407661589941`
- **Package Name**: `com.example.prm_be`
- **Web Client ID**: `407661589941-cp64ed9d0vqghvrj75ap4ohd0bhpelpr.apps.googleusercontent.com`

### **SHA-1 Fingerprint (Debug Keystore)**
```
C5:4D:C5:BB:27:2E:BA:20:56:38:17:54:22:54:1C:8F:20:24:61:71
```

**Lưu ý về SHA-1:**
- Debug SHA-1 **GIỐNG NHAU** trên mọi máy (mặc định)
- Chỉ cần **1 người add vào Firebase** → Tất cả team dùng được
- Nếu Dev 1 đã add rồi → Bạn không cần làm gì thêm!
- **AN TOÀN** để share SHA-1 debug với team (không nguy hiểm)

### **Firebase Console Links**
- **Dashboard**: https://console.firebase.google.com/project/prm-salonbooking
- **Authentication**: https://console.firebase.google.com/project/prm-salonbooking/authentication
- **Firestore**: https://console.firebase.google.com/project/prm-salonbooking/firestore
- **Storage**: https://console.firebase.google.com/project/prm-salonbooking/storage

### **Firebase Collections Structure**
- `users` - Thông tin người dùng
  - Document fields: `uid`, `name`, `email`, `avatarUrl`
- `salons` - Danh sách salon
  - Document fields: `id`, `name`, `address`, `imageUrl`
- `salons/{salonId}/services` - Dịch vụ của salon (subcollection)
  - Document fields: `id`, `name`, `price`
- `salons/{salonId}/stylists` - Stylist của salon (subcollection)
  - Document fields: `id`, `name`, `salonId`, `imageUrl`, `specialization`
- `bookings` - Lịch hẹn
  - Document fields: `id`, `userId`, `salonId`, `serviceId`, `stylistId`, `timestamp`, `status`, `createdAt`

---

## 📦 Bước 3: Sync Gradle

1. **Mở Android Studio**
2. **Open Project:** Chọn thư mục `PRM_SalonBooking`
3. **Sync Gradle:**
   - Android Studio sẽ tự động phát hiện `google-services.json`
   - Click **"Sync Now"** nếu có thông báo
   - Hoặc: **File** → **Sync Project with Gradle Files**
4. **Chờ sync hoàn tất** (30-60 giây)

---

## 🧪 Bước 4: Test Setup

### **4.1. Build Project**
- **Build** → **Make Project** (hoặc `Ctrl + F9`)
- ✅ Nếu build thành công → Setup đúng!

### **4.2. Chạy App**
- Click **Run** (▶️) hoặc `Shift + F10`
- Chọn emulator hoặc device
- ✅ App chạy → OK!

### **4.3. Test Authentication**
1. **Test Đăng Ký:**
   - Mở app → Splash Screen → Login Screen
   - Click "Đăng ký"
   - Nhập: Name, Email, Password
   - Click "Đăng Ký"
   - ✅ Thành công → User được tạo trong Firebase

2. **Test Đăng Nhập:**
   - Email và password vừa đăng ký
   - Click "Đăng Nhập"
   - ✅ Thành công → Chuyển đến HomeActivity

3. **Test Google Sign-In:**
   - Click "Đăng nhập với Google"
   - Chọn Google account
   - ✅ Nếu thành công → OK
   - ⚠️ Nếu lỗi Error 10 → Cần thêm SHA-1 (xem bên dưới)

---

## 🔐 Bước 5: Setup Google Sign-In (Nếu Cần)

### **Nếu Google Sign-In Báo Lỗi Error 10:**

**Nguyên nhân:** SHA-1 fingerprint chưa được thêm vào Firebase

**Cách fix:**

1. **Lấy SHA-1 Fingerprint:**

   **Cách 1: Dùng Keytool (Nhanh nhất - Không cần Gradle)**
   - Mở **Command Prompt** (CMD)
   - Chạy:
     ```cmd
     keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
     ```
   - Tìm dòng **SHA1:** và copy toàn bộ
   
   **Cách 2: Dùng Gradle Tab (Trong Android Studio)**
   - Mở **Gradle** tab (bên phải)
   - `PRM_SalonBooking` → `Tasks` → `android` → `signingReport`
   - Double-click `signingReport`
   - Xem SHA-1 trong **Build Output** tab

2. **Thêm SHA-1 vào Firebase:**
   - Vào Firebase Console: https://console.firebase.google.com/project/prm-salonbooking
   - **Project Settings** → **Your apps** → Android app (`com.example.prm_be`)
   - Scroll xuống phần **"SHA certificate fingerprints"**
   - Click **"Add fingerprint"** (hoặc ➕)
   - Paste SHA-1: `C5:4D:C5:BB:27:2E:BA:20:56:38:17:54:22:54:1C:8F:20:24:61:71`
   - Click **"Save"**

3. **Tải lại google-services.json (QUAN TRỌNG!):**
   - ⚠️ **SAU KHI THÊM SHA-1, PHẢI TẢI LẠI FILE NGAY!**
   - Trong cùng màn hình Firebase Console
   - Scroll xuống → Click **"Download google-services.json"**
   - **Thay thế file cũ** trong `app/google-services.json`
   - File mới sẽ có `oauth_client` với `client_type: 1` (Android client)

4. **Sync & Rebuild:**
   - Android Studio → **File** → **Sync Project with Gradle Files**
   - **Build** → **Rebuild Project**
   - Test lại Google Sign-In → ✅ Hoạt động!

**Kiểm tra sau khi add SHA-1:**
- Firebase Console → Project Settings → Your apps → Android app
- Xem phần **SHA certificate fingerprints** → ✅ Nếu thấy SHA-1 → Đã add đúng!
- Mở `app/google-services.json` → Tìm `"oauth_client"` → Nếu có `"client_type": 1` → File đã được cập nhật! ✅

---

## 🔍 Troubleshooting

### **Lỗi: "Could not find google-services.json"**
- ✅ Kiểm tra file có ở `app/google-services.json` không
- ✅ Tên file chính xác: `google-services.json` (không có .txt)
- ✅ File nằm cùng cấp với `app/build.gradle.kts`

### **Lỗi: "Package name mismatch"**
- ✅ Kiểm tra `app/build.gradle.kts` → `applicationId = "com.example.prm_be"`
- ✅ Kiểm tra `google-services.json` → `package_name = "com.example.prm_be"`

### **Lỗi: "Gradle sync failed"**
- ✅ Kiểm tra internet connection
- ✅ File → Invalidate Caches → Invalidate and Restart
- ✅ Sync lại

### **Lỗi Google Sign-In Error 10**
- ✅ Thêm SHA-1 vào Firebase (xem Bước 5)
- ✅ Đảm bảo Google Sign-In đã bật trong Firebase Console
- ✅ **QUAN TRỌNG**: Phải tải lại `google-services.json` sau khi add SHA-1!

### **Lỗi: "Build failed"**
- ✅ Sync Gradle lại
- ✅ Clean Project: **Build** → **Clean Project**
- ✅ Rebuild: **Build** → **Rebuild Project**

### **Lỗi: "Cannot find symbol class LoginActivity"**
- ✅ Sync Gradle: **File** → **Sync Project with Gradle Files**
- ✅ Clean & Rebuild project
- ✅ Nếu vẫn lỗi, kiểm tra file `LoginActivity.java` có tồn tại không

---

## ✅ Checklist Hoàn Thành Setup

Sau khi setup, đảm bảo:

- [ ] Repository đã được clone
- [ ] File `google-services.json` đã có trong `app/`
- [ ] Gradle đã sync thành công
- [ ] Build project thành công (không có lỗi)
- [ ] App chạy được trên emulator/device
- [ ] Test đăng ký thành công
- [ ] Test đăng nhập thành công
- [ ] (Optional) Test Google Sign-In thành công
- [ ] (Nếu Google Sign-In lỗi) Đã thêm SHA-1 và tải lại `google-services.json`

---

## 📚 Cấu Trúc Dự Án

```
app/src/main/java/com/example/prm_be/
├── data/
│   ├── models/          # Data Models (POJOs)
│   │   ├── User.java
│   │   ├── Salon.java
│   │   ├── Service.java
│   │   ├── Booking.java
│   │   └── Stylist.java
│   └── FirebaseRepo.java # Singleton Firebase Manager
├── ui/
│   ├── auth/            # Authentication screens (Dev 1) ✅
│   │   ├── SplashActivity.java
│   │   ├── LoginActivity.java
│   │   ├── RegisterActivity.java
│   │   └── ForgotPasswordActivity.java
│   ├── home/            # Home screen (Dev 1) ✅
│   │   └── HomeActivity.java
│   ├── discovery/       # Discovery & Search (Dev 2)
│   ├── booking/         # Booking flow (Dev 3)
│   └── profile/         # Profile management (Dev 4)
└── MainActivity.java
```

---

## 👥 Phân Công Team

### Dev 1: Kiến Trúc Sư / Firebase Lead ✅
- ✅ Thiết lập Firebase
- ✅ Tạo Data Models
- ✅ Tạo FirebaseRepo
- ✅ Hoàn thành Authentication Module

### Dev 2: Discovery & Search
- Home Screen
- Salon List
- Salon Detail
- Search functionality

### Dev 3: Booking Flow
- Booking Screen
- Calendar
- Time Slots
- Booking Success

### Dev 4: Profile Management
- Profile Screen
- Edit Profile
- Booking History

---

## 📝 Lưu Ý Quan Trọng

1. **KHÔNG commit `google-services.json` vào Git** (đã thêm vào `.gitignore`)
   - Mỗi dev cần tải file riêng từ Firebase Console
   - Hoặc share qua chat/email với team

2. **SHA-1 Debug Keystore:**
   - Debug SHA-1 giống nhau trên mọi máy (mặc định)
   - Chỉ cần 1 người add vào Firebase → Tất cả team dùng được
   - AN TOÀN để share với team

3. **Firebase Security Rules:**
   - Hiện tại đang ở **test mode** (cho phép đọc/ghi không giới hạn)
   - Sau khi hoàn thành dev, BẮT BUỘC phải thiết lập Security Rules phù hợp!

---

## 📞 Hỗ Trợ

Nếu gặp vấn đề:
1. Đọc lại file hướng dẫn này
2. Check **Troubleshooting** section
3. Liên hệ **Dev 1 (Firebase Lead)**
4. Tạo issue trên GitHub

---

**Chúc bạn code vui vẻ! 🚀**

*Cập nhật bởi: Dev 1 - Firebase Lead*

