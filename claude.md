# Context Dự Án Salon Booking

## 📋 Mô Tả Dự Án

**Tên dự án**: Salon Booking  
**Mục tiêu**: Xây dựng ứng dụng Android native (Java) cho phép người dùng tìm kiếm salon, xem dịch vụ, chọn stylist, và đặt lịch hẹn. Dữ liệu phải được đồng bộ real-time trên cloud.

**Quy mô**: 4 Lập trình viên

**Stack công nghệ**:
- Frontend (Mobile App): Android Native (Java), Kiến trúc MVVM, XML Layouts, Material Design
- Backend (Database & Auth): Firebase (Firestore, Authentication, Storage)

## 🌊 Luồng Chức Năng Chính (4 Luồng - 4 Dev)

### Luồng 1: Xác thực (Authentication) - Dev 1
- Splash Screen -> (Chưa đăng nhập?) -> Login Screen -> (Chưa có tài khoản?) -> Register Screen
- Người dùng đăng ký/đăng nhập thành công -> Home Screen

### Luồng 2: Khám phá & Tìm kiếm (Discovery) - Dev 2
- Từ Home Screen, người dùng thấy danh sách salon, dịch vụ nổi bật
- Người dùng có thể tìm kiếm
- Bấm vào một salon -> Salon Detail Screen

### Luồng 3: Đặt lịch (Booking) - Dev 3
- Từ Salon Detail Screen -> Bấm "Đặt lịch" -> Booking Screen
- Trong Booking Screen:
  - Chọn Dịch vụ (m.u.i, cắt tóc...) -> Tính tổng tiền
  - (Tùy chọn) Chọn Stylist
  - Chọn Ngày (Hiện Calendar)
  - Hiển thị Khung giờ (Time Slots): App phải đọc data từ Firestore xem ngày đó, stylist đó đã có những lịch nào, rồi chỉ hiển thị các slot còn trống
  - Bấm "Xác nhận"
  - Ghi dữ liệu lịch hẹn lên Firestore -> Booking Success Screen

### Luồng 4: Quản lý cá nhân (Profile) - Dev 4
- Từ Home Screen -> Bấm icon Profile -> Profile Screen
- Từ Profile Screen -> Edit Profile Screen (Đổi tên, ảnh đại diện)
- Từ Profile Screen -> Booking History Screen (Xem các lịch đã đặt: Sắp tới / Đã hoàn thành)

## 📦 Phân Chia Công Việc

### 🧑‍💻 Dev 1: "Kiến Trúc Sư" / Firebase Lead (ĐANG THỰC HIỆN)

**Nhiệm vụ**: Xây dựng nền móng, tạo "đường ống" dữ liệu cho 3 dev kia dùng

**Chi tiết**:
- ✅ Tạo Project Android Studio
- ✅ Khởi tạo Git & push lên GitHub (CẦN THỰC HIỆN)
- 🔄 Tạo Project Firebase, kết nối app với Firebase (ĐANG THỰC HIỆN)
- 🔄 Tạo package `com.example.prm_be.data.models` và định nghĩa tất cả các lớp Data Model:
  - User.java (String uid, String name, String email, String avatarUrl)
  - Salon.java (String id, String name, String address, String imageUrl)
  - Service.java (String id, String name, long price)
  - Booking.java (String id, String userId, String salonId, String serviceId, long timestamp, String status)
- 🔄 Viết lớp Singleton `FirebaseRepo.java` quản lý Firebase:
  - login(email, pass, callback)
  - register(email, pass, name, callback)
  - getAllSalons(callback)
  - getServicesOfSalon(salonId, callback)
  - createBooking(Booking booking, callback)
  - getUserBookings(userId, callback)
- 🔄 Thiết lập Firebase Authentication (Email/Password)

**Trạng thái**: ✅ HOÀN THÀNH

**Đã hoàn thành**:
- ✅ Tạo package `com.example.prm_be.data.models` với các Data Models:
  - User.java (String uid, String name, String email, String avatarUrl)
  - Salon.java (String id, String name, String address, String imageUrl)
  - Service.java (String id, String name, long price)
  - Booking.java (String id, String userId, String salonId, String serviceId, String stylistId, long timestamp, String status, long createdAt)
  - Stylist.java (String id, String name, String salonId, String imageUrl, String specialization) - Bonus
- ✅ Tạo FirebaseRepo.java singleton với các method:
  - Authentication: login(), register(), logout(), getCurrentUser(), isUserLoggedIn()
  - User: createUser(), getUser(), updateUser()
  - Salon: getAllSalons(), getSalonById()
  - Service: getServicesOfSalon()
  - Stylist: getStylistsOfSalon()
  - Booking: createBooking(), getUserBookings(), getBookingsByStylistAndDate()
- ✅ Thêm Firebase dependencies vào gradle
- ✅ Cấu hình Firebase trong build.gradle.kts
- ✅ Tạo file hướng dẫn FIREBASE_SETUP.md

**Cần thực hiện tiếp**:
- ⚠️ Tạo Firebase Project trên Firebase Console (xem FIREBASE_SETUP.md)
- ⚠️ Tải file `google-services.json` và đặt vào `app/google-services.json`
- ⚠️ Bật Firebase Authentication (Email/Password)
- ⚠️ Tạo Firestore Database
- ⚠️ (Tùy chọn) Tạo Storage
- ⚠️ Push code lên GitHub và chia sẻ với 3 dev khác

---

## 📝 Ghi Chú Kỹ Thuật

### Cấu Trúc Package
```
com.example.prm_be/
├── data/
│   ├── models/          # Data Models (POJOs)
│   └── FirebaseRepo.java # Singleton Firebase Manager
├── ui/
│   ├── auth/            # Authentication screens (Dev 1)
│   ├── discovery/       # Discovery & Search (Dev 2)
│   ├── booking/         # Booking flow (Dev 3)
│   └── profile/         # Profile management (Dev 4)
└── MainActivity.java
```

### Firebase Collections
- `users` - Thông tin người dùng
- `salons` - Danh sách salon
- `services` - Dịch vụ (subcollection của salon)
- `bookings` - Lịch hẹn
- `stylists` - Danh sách stylist (subcollection của salon)

---

## 🔄 Cập Nhật Gần Nhất
- **Ngày**: Hôm nay
- **Cập nhật bởi**: Dev 1
- **Nội dung**: Bắt đầu thiết lập Firebase và Data Models

