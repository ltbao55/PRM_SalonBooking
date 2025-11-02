# 🎨 Salon Booking - Android App

Ứng dụng Android native (Java) cho phép người dùng tìm kiếm salon, xem dịch vụ, chọn stylist, và đặt lịch hẹn. Dữ liệu được đồng bộ real-time trên Firebase.

## 📋 Thông Tin Dự Án

- **Framework**: Android Native (Java)
- **Kiến trúc**: MVVM
- **UI**: XML Layouts, Material Design
- **Backend**: Firebase (Firestore, Authentication, Storage)
- **Team**: 4 Lập trình viên

## 🌊 Luồng Chức Năng Chính

### Luồng 1: Xác thực (Authentication) - Dev 1 ✅
- Splash Screen -> Login Screen -> Register Screen -> Home Screen

### Luồng 2: Khám phá & Tìm kiếm (Discovery) - Dev 2
- Home Screen -> Danh sách salon -> Salon Detail Screen

### Luồng 3: Đặt lịch (Booking) - Dev 3
- Salon Detail Screen -> Booking Screen -> Chọn dịch vụ, stylist, ngày, giờ -> Booking Success

### Luồng 4: Quản lý cá nhân (Profile) - Dev 4
- Profile Screen -> Edit Profile / Booking History

## 🚀 Bắt Đầu

### ⭐ **Cho Dev Mới: Đọc [SETUP_GUIDE.md](SETUP_GUIDE.md) Trước!**

### Yêu cầu
- Android Studio (Arctic Fox trở lên)
- JDK 11
- Android SDK 33+

### Cài đặt nhanh

1. **Clone repository**
```bash
git clone <repository-url>
cd PRM_SalonBooking
```

2. **Thiết lập Firebase** 
   - Xem chi tiết: **[SETUP_GUIDE.md](SETUP_GUIDE.md)**
   - Tải file `google-services.json` và đặt vào `app/google-services.json`
   - Firebase đã được setup sẵn bởi Dev 1

3. **Sync Gradle & Build**
   - File → Sync Project with Gradle Files
   - Build → Make Project
   - Run app

## 📦 Cấu Trúc Dự Án

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
│   ├── auth/            # Authentication screens (Dev 1)
│   ├── discovery/       # Discovery & Search (Dev 2)
│   ├── booking/         # Booking flow (Dev 3)
│   └── profile/         # Profile management (Dev 4)
└── MainActivity.java
```

## 🔥 Firebase Collections

- `users` - Thông tin người dùng
- `salons` - Danh sách salon
- `salons/{salonId}/services` - Dịch vụ của salon
- `salons/{salonId}/stylists` - Stylist của salon
- `bookings` - Lịch hẹn

## 👥 Phân Công

### Dev 1: Kiến Trúc Sư / Firebase Lead ✅
- Thiết lập Firebase
- Tạo Data Models
- Tạo FirebaseRepo

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

## 📝 Lưu Ý

- **KHÔNG commit `google-services.json`** vào Git (đã thêm vào .gitignore)
- Mỗi dev cần tải `google-services.json` riêng từ Firebase Console

## 📚 Tài Liệu

- **[SETUP_GUIDE.md](SETUP_GUIDE.md)** - ⭐ **Hướng dẫn setup cho team (ĐỌC FILE NÀY ĐẦU TIÊN)**
- [instrucition.md](instrucition.md) - Yêu cầu và phân công công việc

---

**Developed with ❤️ by Team Salon Booking**

