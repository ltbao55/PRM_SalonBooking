# 📋 Phân Chia Task Cho 2 Dev FE - Salon Booking App

## 🎯 Nguyên Tắc Chia Task

- ✅ Mỗi dev làm các màn hình **độc lập**, không chỉnh sửa cùng file
- ✅ Chia theo **luồng nghiệp vụ** để dễ hiểu và test
- ✅ Sử dụng **Feature Branch** để tránh xung đột code
- ✅ Navigation giữa màn hình chỉ dùng Intent (không xung đột)

---

## 👤 DEV 1: Authentication & Discovery Module

### 📱 Màn Hình Phụ Trách (6 màn hình)

#### 🔐 Module Authentication (3 màn hình)
1. **SplashActivity** ✅ (Hoàn thành)
   - File: `ui/auth/SplashActivity.java`
   - Layout: `activity_splash.xml`
   - Logic: Kiểm tra đăng nhập → chuyển Login hoặc Home
   - UI: Luxury salon style với animations

2. **LoginActivity** ✅ (UI hoàn chỉnh, chưa có BE)
   - File: `ui/auth/LoginActivity.java`
   - Layout: `activity_login.xml`
   - Logic: Validate input ✅ (Chưa có Firebase login - TODO)
   - UI: Luxury salon style ✅

3. **RegisterActivity** ✅ (UI hoàn chỉnh, chưa có BE)
   - File: `ui/auth/RegisterActivity.java`
   - Layout: `activity_register.xml`
   - Logic: Validate input ✅ (Chưa có Firebase register - TODO)
   - UI: Luxury salon style ✅

#### 🔍 Module Discovery (3 màn hình)
4. **HomeActivity** ✅ (UI hoàn chỉnh, chưa có BE)
   - File: `ui/discovery/HomeActivity.java`
   - Layout: `activity_home.xml`
   - Logic: 
     - Hiển thị danh sách salon (RecyclerView) ✅
     - Search UI và navigation ✅ (Filter local, chưa có BE search)
     - Navigation đến SalonList, SalonDetail, Profile ✅
   - Adapter: `SalonAdapter.java` ✅
   - **Lưu ý**: Intent đến ProfileActivity (do Dev 2) chỉ cần đúng tên class, không cần chỉnh sửa ProfileActivity

5. **SalonListActivity** ✅ (UI hoàn chỉnh, chưa có BE)
   - File: `ui/discovery/SalonListActivity.java`
   - Layout: `activity_salon_list.xml`
   - Logic:
     - List salon với RecyclerView ✅ (Dùng mock data)
     - Search/Filter salon local ✅ (Chưa có BE search)
     - Navigation đến SalonDetailActivity ✅
   - Adapter: `SalonAdapter.java` ✅ (tái sử dụng từ HomeActivity)

6. **SalonDetailActivity** ✅ (UI hoàn chỉnh, chưa có BE)
   - File: `ui/discovery/SalonDetailActivity.java`
   - Layout: `activity_salon_detail.xml`
   - Logic:
     - Hiển thị thông tin salon (tên, địa chỉ, ảnh) ✅
     - Load services và stylists (RecyclerView) ✅ (Dùng mock data)
     - Button "Đặt lịch" → Intent đến BookingActivity ✅
   - Adapters: `ServiceDetailAdapter.java`, `StylistDetailAdapter.java` ✅
   - **Lưu ý**: Intent đến BookingActivity chỉ cần truyền `EXTRA_SALON_ID`, không chỉnh sửa BookingActivity

### 🎨 Layout Files Cần Tạo/Hoàn Thiện
- `activity_splash.xml` ✅
- `activity_login.xml` ✅
- `activity_register.xml` ✅
- `activity_home.xml` ✅
- `activity_salon_list.xml` ✅
- `activity_salon_detail.xml` ✅
- `item_salon.xml` ✅ (cho RecyclerView)

### 📚 FirebaseRepo Methods Sử Dụng
```java
FirebaseRepo.getInstance().login(email, password, callback)
FirebaseRepo.getInstance().register(email, password, name, callback)
FirebaseRepo.getInstance().isUserLoggedIn()
FirebaseRepo.getInstance().getCurrentUser()
FirebaseRepo.getInstance().getAllSalons(callback)
FirebaseRepo.getInstance().getSalonById(salonId, callback)
FirebaseRepo.getInstance().getServicesOfSalon(salonId, callback)
FirebaseRepo.getInstance().getStylistsOfSalon(salonId, callback)
```

---

## 👤 DEV 2: Booking & Profile Module

### 📱 Màn Hình Phụ Trách (5 màn hình)

#### 📅 Module Booking (2 màn hình)
1. **BookingActivity** ⚠️ (Cần hoàn thiện)
   - File: `ui/booking/BookingActivity.java`
   - Layout: `activity_booking.xml`
   - Logic:
     - Nhận `EXTRA_SALON_ID` từ SalonDetailActivity
     - Load services và stylists (RecyclerView)
     - CalendarView để chọn ngày
     - Hiển thị Time Slots còn trống (logic phức tạp nhất)
     - Tính tổng tiền
     - Tạo booking → Intent đến BookingSuccessActivity
   - Adapters: `ServiceBookingAdapter.java`, `StylistBookingAdapter.java`, `TimeSlotAdapter.java`

2. **BookingSuccessActivity** ✅ (Cơ bản có, cần hoàn thiện)
   - File: `ui/booking/BookingSuccessActivity.java`
   - Layout: `activity_booking_success.xml`
   - Logic: Hiển thị thông báo thành công, button quay về Home

#### 👤 Module Profile (3 màn hình)
3. **ProfileActivity** ⚠️ (Cần hoàn thiện)
   - File: `ui/profile/ProfileActivity.java`
   - Layout: `activity_profile.xml`
   - Logic:
     - Load thông tin user từ FirebaseRepo
     - Hiển thị avatar, tên, email
     - Button Edit Profile → EditProfileActivity
     - Button Booking History → BookingHistoryActivity
     - Button Logout

4. **EditProfileActivity** ⚠️ (Cần hoàn thiện)
   - File: `ui/profile/EditProfileActivity.java`
   - Layout: `activity_edit_profile.xml`
   - Logic:
     - Load user data hiện tại
     - Chỉnh sửa tên, upload avatar (Firebase Storage)
     - Update profile qua FirebaseRepo

5. **BookingHistoryActivity** ⚠️ (Cần hoàn thiện)
   - File: `ui/profile/BookingHistoryActivity.java`
   - Layout: `activity_booking_history.xml`
   - Logic:
     - Load bookings của user từ FirebaseRepo
     - TabLayout: "Sắp tới" / "Đã hoàn thành"
     - Hiển thị danh sách booking (RecyclerView)
   - Adapter: `BookingAdapter.java`
   - ViewPager2: `BookingHistoryPagerAdapter.java` (nếu cần)

### 🎨 Layout Files Cần Tạo/Hoàn Thiện
- `activity_booking.xml` ✅
- `activity_booking_success.xml` ✅
- `activity_profile.xml` ✅
- `activity_edit_profile.xml` ✅
- `activity_booking_history.xml` ✅
- `fragment_booking_list.xml` ✅ (cho ViewPager2)
- `item_booking.xml` ✅ (cho RecyclerView)

### 📚 FirebaseRepo Methods Sử Dụng
```java
FirebaseRepo.getInstance().getServicesOfSalon(salonId, callback)
FirebaseRepo.getInstance().getStylistsOfSalon(salonId, callback)
FirebaseRepo.getInstance().getBookingsByStylistAndDate(stylistId, salonId, start, end, callback)
FirebaseRepo.getInstance().createBooking(booking, callback)
FirebaseRepo.getInstance().getUser(userId, callback)
FirebaseRepo.getInstance().updateUser(user, callback)
FirebaseRepo.getInstance().getUserBookings(userId, callback)
FirebaseRepo.getInstance().logout()
```

---

## 🐙 Quy Trình Làm Việc Trên GitHub

### Bước 1: Setup (Lần đầu)
```bash
# Cả 2 dev cùng làm
git checkout main
git pull origin main
```

### Bước 2: Tạo Feature Branch
```bash
# DEV 1
git checkout -b feature/dev1-auth-discovery

# DEV 2
git checkout -b feature/dev2-booking-profile
```

### Bước 3: Code
- Dev 1 code các màn hình trong `ui/auth/` và `ui/discovery/`
- Dev 2 code các màn hình trong `ui/booking/` và `ui/profile/`
- **Không xung đột** vì làm file khác nhau

### Bước 4: Commit & Push
```bash
# DEV 1
git add app/src/main/java/com/example/prm_be/ui/auth/
git add app/src/main/java/com/example/prm_be/ui/discovery/
git add app/src/main/res/layout/activity_*.xml  # chỉ các file liên quan
git commit -m "Dev 1: Hoàn thành Auth và Discovery module"
git push -u origin feature/dev1-auth-discovery

# DEV 2
git add app/src/main/java/com/example/prm_be/ui/booking/
git add app/src/main/java/com/example/prm_be/ui/profile/
git add app/src/main/res/layout/activity_*.xml  # chỉ các file liên quan
git commit -m "Dev 2: Hoàn thành Booking và Profile module"
git push -u origin feature/dev2-booking-profile
```

### Bước 5: Tạo Pull Request
- Dev 1: Tạo PR từ `feature/dev1-auth-discovery` → `main`
- Dev 2: Tạo PR từ `feature/dev2-booking-profile` → `main`
- Review và merge lần lượt

### Bước 6: Lấy Code Mới (Sau khi merge)
```bash
# Cả 2 dev
git checkout main
git pull origin main
# Bắt đầu task tiếp theo
```

---

## ⚠️ Lưu Ý Quan Trọng - Tránh Xung Đột

### ✅ AN TOÀN - Không Xung Đột
1. **Intent Navigation**: 
   - Dev 1 dùng Intent đến ProfileActivity → Chỉ cần đúng tên class, không chỉnh file của Dev 2
   - Dev 2 dùng Intent đến HomeActivity → Chỉ cần đúng tên class, không chỉnh file của Dev 1

2. **EXTRA Constants**:
   - Dev 1: `SalonDetailActivity.EXTRA_SALON_ID` → Dev 2 chỉ cần đọc, không chỉnh
   - Dev 2: `BookingActivity.EXTRA_SALON_ID` → Dev 1 chỉ cần truyền, không chỉnh

3. **FirebaseRepo**: 
   - Cả 2 dev chỉ **sử dụng**, không chỉnh sửa file này (do Dev Backend làm)

### ⚠️ CẦN THẬN TRỌNG
1. **AndroidManifest.xml**: 
   - Khi thêm Activity mới, cần thêm vào Manifest
   - Có thể xung đột nếu cả 2 cùng commit Manifest → Nên thông báo trước khi merge

2. **strings.xml / colors.xml**:
   - Nếu thêm string/color mới → Có thể xung đột → Nên thống nhất trước hoặc merge cẩn thận

3. **Menu Files**:
   - `menu_home.xml` do Dev 1 làm, Dev 2 không chỉnh

---

## 📊 Checklist Hoàn Thành

### Dev 1 Checklist
- [x] SplashActivity: Logic check đăng nhập ✅ (Đã hoàn thành với UI luxury + animations)
- [x] LoginActivity: Validate input, UI hoàn chỉnh ✅ (Chưa có BE - cần implement Firebase login sau)
- [x] RegisterActivity: Validate input, UI hoàn chỉnh ✅ (Chưa có BE - cần implement Firebase register sau)
- [x] HomeActivity: RecyclerView salon, search UI, navigation ✅ (Dùng mock data, search local filter)
- [x] SalonListActivity: List salon, search/filter local ✅ (Dùng mock data, filter local)
- [x] SalonDetailActivity: Hiển thị salon, services, stylists ✅ (Dùng mock data, có adapters)
- [x] Layouts: Tất cả XML layouts cho 6 màn hình ✅ (Tất cả layout đã có với luxury style)
- [x] Adapters: SalonAdapter, ServiceDetailAdapter, StylistDetailAdapter ✅
- [x] Style: Luxury salon theme (vàng ánh kim, trắng kem, nâu nhạt) ✅

### Dev 2 Checklist
- [ ] BookingActivity: Chọn service, stylist, ngày, time slot, tạo booking
- [ ] BookingSuccessActivity: Hiển thị success, navigation
- [ ] ProfileActivity: Load user info, navigation buttons
- [ ] EditProfileActivity: Edit name, upload avatar
- [ ] BookingHistoryActivity: TabLayout, ViewPager2, list bookings
- [ ] Layouts: Tất cả XML layouts cho 5 màn hình

---

## 🎯 Kết Quả Mong Đợi

Sau khi 2 dev hoàn thành:
- ✅ 11 màn hình hoạt động độc lập
- ✅ Navigation giữa các màn hình smooth
- ✅ Không có conflict khi merge
- ✅ Code sạch, dễ maintain

---

**Chúc team code vui vẻ! 🚀**

