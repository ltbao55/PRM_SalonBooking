# 📱 Hướng Dẫn Khung Layout - Salon Booking App

## 📋 Tổng Quan

Đã tạo xong **khung layout đơn giản** cho tất cả các màn hình. 2 FE developer có thể bắt đầu chia việc để code UI/UX chi tiết.

## 🗂️ Cấu Trúc Đã Tạo

### 📁 Package Structure

```
app/src/main/java/com/example/prm_be/ui/
├── auth/          # Authentication flow (3 màn hình)
├── discovery/     # Discovery & Search flow (3 màn hình)
├── booking/       # Booking flow (2 màn hình)
└── profile/       # Profile management (3 màn hình)
```

### 📄 Layout Files

```
app/src/main/res/layout/
├── activity_splash.xml
├── activity_login.xml
├── activity_register.xml
├── activity_home.xml
├── activity_salon_list.xml
├── activity_salon_detail.xml
├── activity_booking.xml
├── activity_booking_success.xml
├── activity_profile.xml
├── activity_edit_profile.xml
└── activity_booking_history.xml
```

## 🎯 Danh Sách Màn Hình Cần Code UI/UX

### 1️⃣ Authentication Flow (3 màn hình)

#### ✨ SplashActivity

- **File**: `ui/auth/SplashActivity.java` + `layout/activity_splash.xml`
- **Mô tả**: Màn hình chào mừng khi mở app
- **Layout hiện tại**: Chỉ có TextView "Salon Booking"
- **TODO UI/UX**:
  - [ ] Thêm logo/icon app
  - [ ] Animation loading hoặc splash screen đẹp
  - [ ] Gradient background hoặc hình ảnh nền

#### 🔐 LoginActivity

- **File**: `ui/auth/LoginActivity.java` + `layout/activity_login.xml`
- **Mô tả**: Đăng nhập với email/password
- **Layout hiện tại**: EditText email, password, Button login, TextView đăng ký
- **TODO UI/UX**:
  - [ ] Styling đẹp cho EditText (Material Design)
  - [ ] Icon trong input fields
  - [ ] Password visibility toggle
  - [ ] Button styling với ripple effect
  - [ ] Error messages đẹp
  - [ ] Loading indicator khi đăng nhập

#### 📝 RegisterActivity

- **File**: `ui/auth/RegisterActivity.java` + `layout/activity_register.xml`
- **Mô tả**: Đăng ký tài khoản mới
- **Layout hiện tại**: EditText name, email, password, Button register, TextView đăng nhập
- **TODO UI/UX**:
  - [ ] Tương tự LoginActivity
  - [ ] Validation UI (checkmark khi nhập đúng)
  - [ ] Password strength indicator

---

### 2️⃣ Discovery Flow (3 màn hình)

#### 🏠 HomeActivity

- **File**: `ui/discovery/HomeActivity.java` + `layout/activity_home.xml`
- **Mô tả**: Màn hình chính hiển thị danh sách salon
- **Layout hiện tại**: Toolbar + RecyclerView đơn giản
- **TODO UI/UX**:
  - [ ] Search bar ở đầu trang
  - [ ] RecyclerView với CardView cho salon items
  - [ ] Grid layout hoặc List layout
  - [ ] Pull-to-refresh
  - [ ] Empty state khi không có salon
  - [ ] Bottom navigation (nếu cần)

#### 🔍 SalonListActivity

- **File**: `ui/discovery/SalonListActivity.java` + `layout/activity_salon_list.xml`
- **Mô tả**: Danh sách salon với search
- **Layout hiện tại**: AppBar + SearchView + RecyclerView
- **TODO UI/UX**:
  - [ ] SearchView styling
  - [ ] Filter options (lọc theo vị trí, giá, rating...)
  - [ ] RecyclerView với CardView đẹp
  - [ ] Image loading với Glide/Picasso
  - [ ] Rating stars
  - [ ] Distance/Address display

#### 📄 SalonDetailActivity

- **File**: `ui/discovery/SalonDetailActivity.java` + `layout/activity_salon_detail.xml`
- **Mô tả**: Chi tiết salon, dịch vụ, stylist
- **Layout hiện tại**: ImageView, TextView, 2 RecyclerView, Button đặt lịch
- **TODO UI/UX**:
  - [ ] Image carousel hoặc zoomable image
  - [ ] Collapsing toolbar với parallax effect
  - [ ] Tabs hoặc sections: Dịch vụ, Stylist, Đánh giá
  - [ ] RecyclerView dịch vụ với checkboxes
  - [ ] RecyclerView stylist với avatar, specialization
  - [ ] Floating action button hoặc bottom bar cho "Đặt lịch"
  - [ ] Map view (nếu có địa chỉ)

---

### 3️⃣ Booking Flow (2 màn hình)

#### 📅 BookingActivity

- **File**: `ui/booking/BookingActivity.java` + `layout/activity_booking.xml`
- **Mô tả**: Chọn dịch vụ, stylist, ngày, giờ
- **Layout hiện tại**: 3 RecyclerView, CalendarView, TextView tổng tiền, Button xác nhận
- **TODO UI/UX**:
  - [ ] RecyclerView dịch vụ với checkboxes và giá
  - [ ] RecyclerView stylist với radio buttons
  - [ ] CalendarView custom styling
  - [ ] Time slots grid/card layout
  - [ ] Tính tổng tiền real-time
  - [ ] Stepper/progress indicator
  - [ ] Bottom sheet cho summary

#### ✅ BookingSuccessActivity

- **File**: `ui/booking/BookingSuccessActivity.java` + `layout/activity_booking_success.xml`
- **Mô tả**: Xác nhận đặt lịch thành công
- **Layout hiện tại**: ImageView, TextView, Button về trang chủ
- **TODO UI/UX**:
  - [ ] Success animation (checkmark, confetti...)
  - [ ] Booking details summary
  - [ ] Icon đẹp thay vì drawable mặc định
  - [ ] Button styling

---

### 4️⃣ Profile Flow (3 màn hình)

#### 👤 ProfileActivity

- **File**: `ui/profile/ProfileActivity.java` + `layout/activity_profile.xml`
- **Mô tả**: Thông tin cá nhân, menu profile
- **Layout hiện tại**: ImageView avatar, TextView name/email, 3 Buttons
- **TODO UI/UX**:
  - [ ] Circular avatar với border
  - [ ] CardView layout cho profile info
  - [ ] List items đẹp cho các actions
  - [ ] Material icons
  - [ ] Logout confirmation dialog

#### ✏️ EditProfileActivity

- **File**: `ui/profile/EditProfileActivity.java` + `layout/activity_edit_profile.xml`
- **Mô tả**: Chỉnh sửa thông tin và avatar
- **Layout hiện tại**: ImageView, EditText name, Button save
- **TODO UI/UX**:
  - [ ] Image picker với crop
  - [ ] Circular avatar với edit icon overlay
  - [ ] TextInputLayout cho EditText
  - [ ] Save button với loading state

#### 📜 BookingHistoryActivity

- **File**: `ui/profile/BookingHistoryActivity.java` + `layout/activity_booking_history.xml`
- **Mô tả**: Lịch sử đặt lịch (Sắp tới / Đã hoàn thành)
- **Layout hiện tại**: 2 RecyclerView
- **TODO UI/UX**:
  - [ ] Tabs hoặc sections cho "Sắp tới" và "Đã hoàn thành"
  - [ ] CardView layout cho booking items
  - [ ] Status badges (pending, confirmed, completed)
  - [ ] Date formatting đẹp
  - [ ] Empty state
  - [ ] Action buttons (cancel, reschedule...)

---

## 🎨 Gợi Ý Chia Việc Cho 2 FE Developer

### Option 1: Theo Flow

- **FE Dev A**: Authentication + Discovery (6 màn hình)
- **FE Dev B**: Booking + Profile (5 màn hình)

### Option 2: Theo Chức Năng

- **FE Dev A**:
  - Authentication (3 màn hình)
  - Home + SalonList (2 màn hình)
  - BookingSuccess (1 màn hình)
- **FE Dev B**:
  - SalonDetail (1 màn hình)
  - Booking (1 màn hình)
  - Profile (3 màn hình)

### Option 3: Theo Độ Phức Tạp

- **FE Dev A** (Phức tạp hơn):
  - SalonDetailActivity (nhiều components)
  - BookingActivity (logic phức tạp)
  - HomeActivity (RecyclerView + search)
  - BookingHistoryActivity (2 lists)
- **FE Dev B** (Đơn giản hơn):
  - SplashActivity
  - LoginActivity
  - RegisterActivity
  - ProfileActivity
  - EditProfileActivity
  - BookingSuccessActivity

## 🛠️ Công Cụ & Thư Viện Đề Xuất

### Image Loading

- Glide hoặc Picasso để load ảnh từ URL

### RecyclerView

- ViewBinding để bind views
- DiffUtil cho efficient updates

### Material Components

- Material Design 3 components
- Bottom Navigation
- Tabs
- Cards
- Floating Action Button

### Form Validation

- TextInputLayout với error messages

## 📝 Lưu Ý

1. **Tất cả Activities đã có sẵn navigation logic** - chỉ cần implement UI/UX
2. **FirebaseRepo đã sẵn sàng** - có thể gọi methods từ activities
3. **Layout XML hiện tại rất đơn giản** - cần thay thế bằng Material Design components
4. **Các TODO comments** trong code chỉ ra chỗ cần implement logic

## ✅ Checklist Trước Khi Bắt Đầu

- [ ] Sync Gradle project
- [ ] Test build để đảm bảo không có lỗi compile
- [ ] Setup Firebase `google-services.json` (nếu chưa có)
- [ ] Thống nhất design system (colors, typography, spacing)
- [ ] Chia việc rõ ràng giữa 2 FE developer

---

**Chúc code vui vẻ! 🚀**
