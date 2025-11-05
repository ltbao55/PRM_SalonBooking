# 🧪 Testing Guide - Hướng dẫn kiểm thử toàn bộ ứng dụng

## Mục đích
Tài liệu này cung cấp các bước kiểm thử chi tiết cho tất cả các chức năng của ứng dụng Salon Booking, bao gồm:
- **User (Khách hàng)**: Đăng ký, đăng nhập, tìm salon, đặt lịch, xem lịch sử
- **Staff (Nhân viên)**: Xem lịch làm việc, quản lý booking, quản lý thời gian trống
- **Admin (Quản trị viên)**: Quản lý toàn bộ hệ thống

---

## 📋 Mục lục
1. [Chuẩn bị Test Data](#1-chuẩn-bị-test-data)
2. [Testing cho User (Khách hàng)](#2-testing-cho-user-khách-hàng)
3. [Testing cho Staff (Nhân viên)](#3-testing-cho-staff-nhân-viên)
4. [Testing cho Admin (Quản trị viên)](#4-testing-cho-admin-quản-trị-viên)
5. [Testing Security & Role Guard](#5-testing-security--role-guard)
6. [Testing Cross-Role Access](#6-testing-cross-role-access)

---

## 1. Chuẩn bị Test Data

### 1.1. Tạo tài khoản test

**Admin Account:**
- Email: `admin1@lux.com`
- Password: `123456`
- Role: `admin`

**Staff Account:**
- Email: `staff1@lux.com` (hoặc tạo từ Dev Tools)
- Password: `123456`
- Role: `staff`
- StylistId: Phải được liên kết với một stylist trong Firestore

**User Account:**
- Email: `user1@test.com`
- Password: `123456`
- Role: `user` (mặc định)

### 1.2. Seed Data (nếu cần)
- Mở Dev Tools từ Admin Dashboard
- Chọn "Force Seed Data" để tạo dữ liệu mẫu (salons, services, stylists, bookings)

---

## 2. Testing cho User (Khách hàng)

### 2.1. Authentication

#### Test Case 2.1.1: Đăng ký tài khoản mới
**Bước thực hiện:**
1. Mở app → Chọn "Đăng ký"
2. Nhập thông tin:
   - Tên: "Test User"
   - Email: `newuser@test.com`
   - Password: `123456`
   - Confirm Password: `123456`
3. Nhấn "Đăng ký"

**Kết quả mong đợi:**
- ✅ Đăng ký thành công
- ✅ Tự động đăng nhập
- ✅ Điều hướng đến HomeActivity
- ✅ User document trong Firestore có `role: "user"` (mặc định)

#### Test Case 2.1.2: Đăng nhập với email/password
**Bước thực hiện:**
1. Nhập email: `user1@test.com`
2. Nhập password: `123456`
3. Nhấn "Đăng nhập"

**Kết quả mong đợi:**
- ✅ Đăng nhập thành công
- ✅ Điều hướng đến HomeActivity
- ✅ Hiển thị danh sách salon

#### Test Case 2.1.3: Đăng nhập với Google
**Bước thực hiện:**
1. Nhấn "Đăng nhập với Google"
2. Chọn tài khoản Google
3. Xác nhận quyền truy cập

**Kết quả mong đợi:**
- ✅ Đăng nhập thành công
- ✅ Tạo User document trong Firestore nếu chưa có
- ✅ Điều hướng đến HomeActivity

#### Test Case 2.1.4: Đăng nhập sai mật khẩu
**Bước thực hiện:**
1. Nhập email hợp lệ
2. Nhập password sai
3. Nhấn "Đăng nhập"

**Kết quả mong đợi:**
- ❌ Hiển thị thông báo lỗi: "Mật khẩu không chính xác"
- ❌ Vẫn ở màn hình Login

---

### 2.2. Discovery & Salon Browsing

#### Test Case 2.2.1: Xem danh sách salon trên Home
**Bước thực hiện:**
1. Đăng nhập với tài khoản user
2. Xem màn hình Home

**Kết quả mong đợi:**
- ✅ Hiển thị danh sách salon (tối đa một số salon đầu tiên)
- ✅ Mỗi salon hiển thị: tên, địa chỉ, ảnh (nếu có)
- ✅ Có nút "Xem tất cả"

#### Test Case 2.2.2: Tìm kiếm salon
**Bước thực hiện:**
1. Ở màn hình Home, nhấn vào ô tìm kiếm
2. Hoặc nhấn "Xem tất cả"
3. Nhập từ khóa tìm kiếm (ví dụ: "Quận 1")

**Kết quả mong đợi:**
- ✅ Điều hướng đến SalonListActivity
- ✅ Hiển thị danh sách salon đầy đủ
- ✅ Kết quả được lọc theo từ khóa (nếu có)

#### Test Case 2.2.3: Xem chi tiết salon
**Bước thực hiện:**
1. Từ danh sách salon, nhấn vào một salon
2. Xem màn hình chi tiết

**Kết quả mong đợi:**
- ✅ Hiển thị thông tin salon: tên, địa chỉ, mô tả, rating
- ✅ Hiển thị danh sách dịch vụ
- ✅ Hiển thị danh sách stylist
- ✅ Có nút "Đặt lịch"

---

### 2.3. Booking (Đặt lịch)

#### Test Case 2.3.1: Tạo booking mới
**Bước thực hiện:**
1. Vào SalonDetailActivity
2. Nhấn "Đặt lịch"
3. Chọn dịch vụ
4. Chọn stylist
5. Chọn ngày (CalendarView)
6. Chọn giờ (time slot)
7. Nhấn "Xác nhận"

**Kết quả mong đợi:**
- ✅ Booking được tạo thành công
- ✅ Điều hướng đến BookingSuccessActivity
- ✅ Hiển thị thông tin booking vừa tạo
- ✅ Booking document được lưu trong Firestore với:
  - `userId`: ID của user hiện tại
  - `salonId`: ID salon đã chọn
  - `serviceId`: ID dịch vụ đã chọn
  - `stylistId`: ID stylist đã chọn
  - `timestamp`: Timestamp của booking
  - `status`: "pending" (mặc định)

#### Test Case 2.3.2: Chọn time slot không khả dụng
**Bước thực hiện:**
1. Vào BookingActivity
2. Chọn dịch vụ và stylist
3. Chọn ngày có slot đã được đặt
4. Thử chọn slot đã được đặt

**Kết quả mong đợi:**
- ✅ Slot đã được đặt hiển thị mờ (disabled)
- ✅ Không thể chọn slot đó
- ✅ Chỉ có thể chọn slot còn trống

#### Test Case 2.3.3: Booking với availability của staff
**Bước thực hiện:**
1. Staff đánh dấu một slot là không khả dụng (unavailable)
2. User thử đặt lịch vào slot đó

**Kết quả mong đợi:**
- ✅ Slot đó không hiển thị trong danh sách slot khả dụng
- ✅ User không thể đặt lịch vào slot đó

---

### 2.4. Profile & Booking History

#### Test Case 2.4.1: Xem profile
**Bước thực hiện:**
1. Từ HomeActivity, nhấn menu → Profile
2. Xem thông tin profile

**Kết quả mong đợi:**
- ✅ Hiển thị tên, email, avatar
- ✅ Có nút "Chỉnh sửa hồ sơ"
- ✅ Có nút "Lịch sử đặt lịch"
- ✅ Có nút "Đăng xuất"

#### Test Case 2.4.2: Chỉnh sửa profile
**Bước thực hiện:**
1. Vào ProfileActivity
2. Nhấn "Chỉnh sửa hồ sơ"
3. Sửa tên
4. (Tùy chọn) Chọn ảnh đại diện
5. Nhấn "Lưu"

**Kết quả mong đợi:**
- ✅ Thông tin được cập nhật trong Firestore
- ✅ UI hiển thị thông tin mới
- ✅ Thông báo "Đã cập nhật thành công"

#### Test Case 2.4.3: Xem lịch sử đặt lịch
**Bước thực hiện:**
1. Vào ProfileActivity
2. Nhấn "Lịch sử đặt lịch"
3. Xem danh sách booking

**Kết quả mong đợi:**
- ✅ Hiển thị tất cả booking của user hiện tại
- ✅ Có tab "Tất cả", "Đã xác nhận", "Đã hoàn thành", "Đã hủy"
- ✅ Mỗi booking hiển thị: salon, dịch vụ, ngày giờ, trạng thái
- ✅ Nhấn vào booking → xem chi tiết

#### Test Case 2.4.4: Đăng xuất
**Bước thực hiện:**
1. Vào ProfileActivity
2. Nhấn "Đăng xuất"

**Kết quả mong đợi:**
- ✅ Đăng xuất thành công
- ✅ Điều hướng về LoginActivity
- ✅ Không thể quay lại màn hình user mà không đăng nhập lại

---

## 3. Testing cho Staff (Nhân viên)

### 3.1. Authentication & Navigation

#### Test Case 3.1.1: Đăng nhập với tài khoản staff
**Bước thực hiện:**
1. Đăng nhập với email: `staff1@lux.com`
2. Nhập password

**Kết quả mong đợi:**
- ✅ Đăng nhập thành công
- ✅ Điều hướng đến StaffScheduleActivity (không phải HomeActivity)
- ✅ Không thể truy cập các màn hình user (Home, Booking, etc.)

#### Test Case 3.1.2: Kiểm tra Role Guard cho staff
**Bước thực hiện:**
1. Đăng nhập với tài khoản staff
2. Thử truy cập HomeActivity (qua deep link hoặc Intent)

**Kết quả mong đợi:**
- ✅ Tự động redirect về StaffScheduleActivity
- ✅ Không thể truy cập HomeActivity

---

### 3.2. Staff Schedule (Lịch làm việc)

#### Test Case 3.2.1: Xem lịch làm việc theo tuần
**Bước thực hiện:**
1. Vào StaffScheduleActivity
2. Xem lịch tuần hiện tại

**Kết quả mong đợi:**
- ✅ Hiển thị lịch làm việc của staff hiện tại
- ✅ Hiển thị các booking được gán cho staff
- ✅ Mỗi booking hiển thị: salon, dịch vụ, khách hàng, ngày giờ, trạng thái
- ✅ Có nút "Tuần trước" và "Tuần sau"

#### Test Case 3.2.2: Điều hướng tuần
**Bước thực hiện:**
1. Ở StaffScheduleActivity
2. Nhấn "Tuần trước" hoặc "Tuần sau"
3. Xem lịch mới

**Kết quả mong đợi:**
- ✅ Hiển thị phạm vi tuần mới (dd/MM/yyyy - dd/MM/yyyy)
- ✅ Lịch được reload với booking của tuần đó
- ✅ Empty state nếu không có booking

#### Test Case 3.2.3: Xem chi tiết booking
**Bước thực hiện:**
1. Ở StaffScheduleActivity
2. Nhấn vào một booking trong danh sách
3. Xem BottomSheet chi tiết

**Kết quả mong đợi:**
- ✅ Hiển thị thông tin đầy đủ:
  - Tên salon
  - Tên dịch vụ
  - Tên khách hàng
  - Ngày giờ
  - Trạng thái (pending/confirmed/completed/cancelled)
- ✅ Có các nút action dựa trên trạng thái

---

### 3.3. Quản lý trạng thái booking

#### Test Case 3.3.1: Xác nhận booking (pending → confirmed)
**Bước thực hiện:**
1. Xem chi tiết booking có trạng thái "pending"
2. Nhấn "Xác nhận"

**Kết quả mong đợi:**
- ✅ Trạng thái được cập nhật thành "confirmed"
- ✅ BottomSheet đóng
- ✅ Danh sách được reload
- ✅ Booking trong Firestore có `status: "confirmed"`

#### Test Case 3.3.2: Hoàn thành booking (confirmed → completed)
**Bước thực hiện:**
1. Xem chi tiết booking có trạng thái "confirmed"
2. Nhấn "Hoàn thành"

**Kết quả mong đợi:**
- ✅ Trạng thái được cập nhật thành "completed"
- ✅ BottomSheet đóng
- ✅ Danh sách được reload

#### Test Case 3.3.3: Hủy booking
**Bước thực hiện:**
1. Xem chi tiết booking có trạng thái "pending" hoặc "confirmed"
2. Nhấn "Hủy"
3. Xác nhận trong dialog

**Kết quả mong đợi:**
- ✅ Hiển thị dialog xác nhận
- ✅ Trạng thái được cập nhật thành "cancelled"
- ✅ Danh sách được reload

---

### 3.4. Quản lý thời gian trống (Availability)

#### Test Case 3.4.1: Mở dialog quản lý availability
**Bước thực hiện:**
1. Ở StaffScheduleActivity
2. Nhấn menu → "Thời gian trống"

**Kết quả mong đợi:**
- ✅ Mở BottomSheet quản lý availability
- ✅ Hiển thị date picker
- ✅ Hiển thị danh sách time slots

#### Test Case 3.4.2: Chọn ngày và xem slots
**Bước thực hiện:**
1. Mở dialog availability
2. Nhấn "Chọn ngày"
3. Chọn một ngày trong tương lai
4. Xem danh sách slots

**Kết quả mong đợi:**
- ✅ Hiển thị các time slots dựa trên working hours của salon
- ✅ Mỗi slot có switch (ON = available, OFF = unavailable)
- ✅ Slots hiện tại được load từ Firestore (nếu có)

#### Test Case 3.4.3: Đánh dấu slot không khả dụng
**Bước thực hiện:**
1. Chọn ngày
2. Tắt switch của một số slot (OFF)
3. Nhập lý do (tùy chọn)
4. Nhấn "Lưu"

**Kết quả mong đợi:**
- ✅ Availability được lưu vào Firestore
- ✅ Collection `availability` có document với:
  - `staffId`: ID của staff
  - `salonId`: ID của salon
  - `date`: Timestamp của ngày (00:00:00)
  - `unavailableSlots`: Danh sách các slot không khả dụng
  - `reason`: Lý do (nếu có)
- ✅ Thông báo "Đã lưu thời gian trống"

#### Test Case 3.4.4: Mở lại slot đã đóng
**Bước thực hiện:**
1. Chọn ngày đã có availability
2. Bật switch của slot đã đóng (ON)
3. Nhấn "Lưu"

**Kết quả mong đợi:**
- ✅ Slot được xóa khỏi `unavailableSlots`
- ✅ Slot lại khả dụng cho booking

---

### 3.5. Profile của Staff

#### Test Case 3.5.1: Xem profile staff
**Bước thực hiện:**
1. Ở StaffScheduleActivity
2. Nhấn menu → "Hồ sơ"

**Kết quả mong đợi:**
- ✅ Điều hướng đến ProfileActivity
- ✅ Hiển thị thông tin: tên, email, avatar
- ✅ **KHÔNG** hiển thị nút "Lịch sử đặt lịch" (vì staff không có booking history như user)
- ✅ Có nút "Chỉnh sửa hồ sơ" và "Đăng xuất"

---

## 4. Testing cho Admin (Quản trị viên)

### 4.1. Authentication & Navigation

#### Test Case 4.1.1: Đăng nhập với tài khoản admin
**Bước thực hiện:**
1. Đăng nhập với email: `admin1@lux.com`
2. Nhập password

**Kết quả mong đợi:**
- ✅ Đăng nhập thành công
- ✅ Điều hướng đến AdminDashboardActivity
- ✅ Không thể truy cập các màn hình user hoặc staff

#### Test Case 4.1.2: Xem Admin Dashboard
**Bước thực hiện:**
1. Đăng nhập với tài khoản admin
2. Xem AdminDashboardActivity

**Kết quả mong đợi:**
- ✅ Hiển thị các card menu:
  - Tất cả lịch
  - Quản lý tài khoản
  - Quản lý dịch vụ
  - Quản lý salon
  - Cấu hình giờ làm việc
  - Báo cáo
  - Developer Tools

---

### 4.2. Quản lý lịch (All Schedules)

#### Test Case 4.2.1: Xem tất cả booking
**Bước thực hiện:**
1. Từ AdminDashboard, nhấn "Tất cả lịch"
2. Xem danh sách booking

**Kết quả mong đợi:**
- ✅ Hiển thị tất cả booking trong hệ thống
- ✅ Mỗi booking hiển thị: salon, staff, dịch vụ, khách hàng, ngày giờ, trạng thái
- ✅ Có empty state nếu không có booking

#### Test Case 4.2.2: Xem chi tiết booking và đổi trạng thái
**Bước thực hiện:**
1. Nhấn vào một booking
2. Xem BottomSheet chi tiết
3. Nhấn "Xác nhận" hoặc "Hủy"

**Kết quả mong đợi:**
- ✅ Hiển thị thông tin đầy đủ booking
- ✅ Có thể đổi trạng thái
- ✅ Trạng thái được cập nhật trong Firestore
- ✅ Danh sách được reload

---

### 4.3. Quản lý tài khoản (Users)

#### Test Case 4.3.1: Xem danh sách users
**Bước thực hiện:**
1. Từ AdminDashboard, nhấn "Quản lý tài khoản"
2. Xem danh sách users

**Kết quả mong đợi:**
- ✅ Hiển thị tất cả users trong hệ thống
- ✅ Mỗi user hiển thị: tên, email, role, status
- ✅ Có thể tìm kiếm user

#### Test Case 4.3.2: Đổi role của user
**Bước thực hiện:**
1. Nhấn vào một user
2. Xem BottomSheet edit user
3. Chọn role mới (user/staff/admin)
4. Nhấn "Lưu"

**Kết quả mong đợi:**
- ✅ Role được cập nhật trong Firestore
- ✅ Thông báo "Đã cập nhật thành công"
- ✅ User phải đăng nhập lại để thấy thay đổi role

#### Test Case 4.3.3: Đổi status của user (active/disabled)
**Bước thực hiện:**
1. Nhấn vào một user
2. Chọn status mới (active/disabled)
3. Nhấn "Lưu"

**Kết quả mong đợi:**
- ✅ Status được cập nhật trong Firestore
- ✅ User bị disabled không thể đăng nhập
- ✅ User active có thể đăng nhập bình thường

---

### 4.4. Quản lý dịch vụ (Services)

#### Test Case 4.4.1: Xem danh sách dịch vụ
**Bước thực hiện:**
1. Từ AdminDashboard, nhấn "Quản lý dịch vụ"
2. Xem danh sách dịch vụ

**Kết quả mong đợi:**
- ✅ Hiển thị tất cả dịch vụ từ tất cả salon
- ✅ Mỗi dịch vụ hiển thị: tên, giá, thời lượng, salon
- ✅ Có FAB để thêm dịch vụ mới

#### Test Case 4.4.2: Thêm dịch vụ mới
**Bước thực hiện:**
1. Nhấn FAB "Thêm"
2. Chọn salon
3. Nhập tên dịch vụ
4. Nhập giá
5. Nhập thời lượng (phút)
6. Nhấn "Lưu"

**Kết quả mong đợi:**
- ✅ Dịch vụ được tạo trong Firestore
- ✅ Hiển thị trong danh sách
- ✅ Có thể sử dụng trong BookingActivity

#### Test Case 4.4.3: Sửa dịch vụ
**Bước thực hiện:**
1. Nhấn vào một dịch vụ
2. Sửa thông tin
3. Nhấn "Lưu"

**Kết quả mong đợi:**
- ✅ Dịch vụ được cập nhật trong Firestore
- ✅ Thông tin mới hiển thị trong UI

#### Test Case 4.4.4: Xóa dịch vụ
**Bước thực hiện:**
1. Nhấn vào một dịch vụ
2. Nhấn "Xóa"
3. Xác nhận xóa

**Kết quả mong đợi:**
- ✅ Dịch vụ được xóa khỏi Firestore
- ✅ Không còn hiển thị trong danh sách
- ✅ Booking cũ vẫn giữ nguyên serviceId

---

### 4.5. Quản lý Salon

#### Test Case 4.5.1: Xem danh sách salon
**Bước thực hiện:**
1. Từ AdminDashboard, nhấn "Quản lý salon"
2. Xem danh sách salon

**Kết quả mong đợi:**
- ✅ Hiển thị tất cả salon
- ✅ Mỗi salon hiển thị: tên, địa chỉ, phone, rating
- ✅ Có FAB để thêm salon mới

#### Test Case 4.5.2: Thêm salon mới
**Bước thực hiện:**
1. Nhấn FAB "Thêm"
2. Nhập tên salon
3. Nhập địa chỉ
4. Nhập số điện thoại
5. Nhập mô tả
6. Nhập URL ảnh (tùy chọn)
7. Nhập rating (mặc định 0.0)
8. Nhấn "Lưu"

**Kết quả mong đợi:**
- ✅ Salon được tạo trong Firestore
- ✅ Hiển thị trong danh sách
- ✅ Có thể được chọn trong BookingActivity

#### Test Case 4.5.3: Sửa salon
**Bước thực hiện:**
1. Nhấn vào một salon
2. Sửa thông tin
3. Nhấn "Lưu"

**Kết quả mong đợi:**
- ✅ Salon được cập nhật trong Firestore
- ✅ Thông tin mới hiển thị trong UI

#### Test Case 4.5.4: Xóa salon
**Bước thực hiện:**
1. Nhấn vào một salon
2. Nhấn "Xóa"
3. Xác nhận xóa

**Kết quả mong đợi:**
- ✅ Salon được xóa khỏi Firestore
- ✅ Không còn hiển thị trong danh sách
- ⚠️ Lưu ý: Các booking và services liên quan vẫn tồn tại

---

### 4.6. Cấu hình giờ làm việc (Working Hours)

#### Test Case 4.6.1: Xem cấu hình giờ làm việc
**Bước thực hiện:**
1. Từ AdminDashboard, nhấn "Cấu hình giờ làm việc"
2. Chọn một salon từ Spinner

**Kết quả mong đợi:**
- ✅ Hiển thị working hours hiện tại của salon (hoặc default)
- ✅ Hiển thị: giờ mở cửa, giờ đóng cửa, slot duration, các ngày làm việc

#### Test Case 4.6.2: Cấu hình giờ làm việc mới
**Bước thực hiện:**
1. Chọn salon
2. Chọn giờ mở cửa (ví dụ: 09:00)
3. Chọn giờ đóng cửa (ví dụ: 18:00)
4. Nhập slot duration (ví dụ: 30 phút)
5. Chọn các ngày làm việc (checkboxes)
6. Nhấn "Lưu"

**Kết quả mong đợi:**
- ✅ Working hours được lưu vào Firestore
- ✅ Collection `workingHours` có document với:
  - `salonId`: ID salon
  - `openTime`: "09:00"
  - `closeTime`: "18:00"
  - `slotDuration`: 30
  - `daysOfWeek`: ["MON", "TUE", ...]
- ✅ Thông báo "Đã lưu cấu hình giờ làm việc"

#### Test Case 4.6.3: Sử dụng working hours trong booking
**Bước thực hiện:**
1. Cấu hình working hours cho salon
2. Đăng nhập với user, thử đặt lịch tại salon đó
3. Xem danh sách time slots

**Kết quả mong đợi:**
- ✅ Time slots được generate dựa trên working hours
- ✅ Slots từ 09:00 đến 18:00, mỗi slot 30 phút
- ✅ Chỉ hiển thị slots trong các ngày làm việc đã chọn

---

### 4.7. Báo cáo (Reports)

#### Test Case 4.7.1: Xem báo cáo
**Bước thực hiện:**
1. Từ AdminDashboard, nhấn "Báo cáo"
2. Xem thống kê

**Kết quả mong đợi:**
- ✅ Hiển thị các thống kê:
  - Tổng số booking theo ngày/tuần
  - Top dịch vụ
  - Doanh thu (nếu có)
- ✅ Có thể export CSV

---

### 4.8. Developer Tools

#### Test Case 4.8.1: Truy cập Dev Tools
**Bước thực hiện:**
1. Từ AdminDashboard, nhấn "Developer Tools"
2. Xem các công cụ

**Kết quả mong đợi:**
- ✅ Chỉ admin mới truy cập được
- ✅ Có các chức năng:
  - Tạo tài khoản staff/admin
  - Seed data (tạo dữ liệu mẫu)
  - Force seed data

---

## 5. Testing Security & Role Guard

### 5.1. Role Guard cho User

#### Test Case 5.1.1: User không thể truy cập admin dashboard
**Bước thực hiện:**
1. Đăng nhập với tài khoản user
2. Thử truy cập AdminDashboardActivity (qua Intent hoặc deep link)

**Kết quả mong đợi:**
- ✅ Tự động redirect về HomeActivity
- ✅ Không thể truy cập admin dashboard

#### Test Case 5.1.2: User không thể truy cập staff schedule
**Bước thực hiện:**
1. Đăng nhập với tài khoản user
2. Thử truy cập StaffScheduleActivity

**Kết quả mong đợi:**
- ✅ Tự động redirect về HomeActivity
- ✅ Không thể truy cập staff schedule

---

### 5.2. Role Guard cho Staff

#### Test Case 5.2.1: Staff không thể truy cập user UI
**Bước thực hiện:**
1. Đăng nhập với tài khoản staff
2. Thử truy cập HomeActivity, BookingActivity, SalonDetailActivity

**Kết quả mong đợi:**
- ✅ Tự động redirect về StaffScheduleActivity
- ✅ Không thể truy cập các màn hình user

#### Test Case 5.2.2: Staff không thể truy cập admin dashboard
**Bước thực hiện:**
1. Đăng nhập với tài khoản staff
2. Thử truy cập AdminDashboardActivity

**Kết quả mong đợi:**
- ✅ Tự động redirect về StaffScheduleActivity
- ✅ Không thể truy cập admin dashboard

---

### 5.3. Role Guard cho Admin

#### Test Case 5.3.1: Admin không thể truy cập user UI
**Bước thực hiện:**
1. Đăng nhập với tài khoản admin
2. Thử truy cập HomeActivity, BookingActivity

**Kết quả mong đợi:**
- ✅ Tự động redirect về AdminDashboardActivity
- ✅ Không thể truy cập các màn hình user

#### Test Case 5.3.2: Admin không thể truy cập staff schedule
**Bước thực hiện:**
1. Đăng nhập với tài khoản admin
2. Thử truy cập StaffScheduleActivity

**Kết quả mong đợi:**
- ✅ Tự động redirect về AdminDashboardActivity
- ✅ Không thể truy cập staff schedule

---

## 6. Testing Cross-Role Access

### 6.1. Testing User Disabled

#### Test Case 6.1.1: User bị disabled không thể đăng nhập
**Bước thực hiện:**
1. Admin đổi status của một user thành "disabled"
2. User đó thử đăng nhập

**Kết quả mong đợi:**
- ❌ Không thể đăng nhập
- ✅ Hiển thị thông báo lỗi
- ✅ User bị logout nếu đang đăng nhập

---

### 6.2. Testing Role Change

#### Test Case 6.2.1: Đổi role của user từ user → staff
**Bước thực hiện:**
1. Admin đổi role của user từ "user" → "staff"
2. User đó đăng xuất và đăng nhập lại

**Kết quả mong đợi:**
- ✅ Đăng nhập thành công
- ✅ Điều hướng đến StaffScheduleActivity (không phải HomeActivity)
- ✅ User không thể truy cập HomeActivity nữa

#### Test Case 6.2.2: Đổi role của staff từ staff → admin
**Bước thực hiện:**
1. Admin đổi role của staff từ "staff" → "admin"
2. Staff đó đăng xuất và đăng nhập lại

**Kết quả mong đợi:**
- ✅ Đăng nhập thành công
- ✅ Điều hướng đến AdminDashboardActivity
- ✅ Có thể truy cập tất cả chức năng admin

---

## 7. Testing Edge Cases & Error Handling

### 7.1. Network Error

#### Test Case 7.1.1: Mất kết nối mạng khi đăng nhập
**Bước thực hiện:**
1. Tắt WiFi/Data
2. Thử đăng nhập

**Kết quả mong đợi:**
- ❌ Hiển thị thông báo lỗi: "Lỗi kết nối mạng"
- ✅ Không crash app

#### Test Case 7.1.2: Mất kết nối khi load data
**Bước thực hiện:**
1. Đăng nhập thành công
2. Tắt WiFi/Data
3. Thử load danh sách salon/bookings

**Kết quả mong đợi:**
- ❌ Hiển thị thông báo lỗi
- ✅ Hiển thị empty state
- ✅ Không crash app

---

### 7.2. Empty State

#### Test Case 7.2.1: Không có salon
**Bước thực hiện:**
1. Xóa tất cả salon trong Firestore
2. Đăng nhập với user
3. Xem HomeActivity

**Kết quả mong đợi:**
- ✅ Hiển thị empty state
- ✅ Có thông báo "Chưa có salon nào"

#### Test Case 7.2.2: Staff không có booking
**Bước thực hiện:**
1. Đăng nhập với staff chưa có booking
2. Xem StaffScheduleActivity

**Kết quả mong đợi:**
- ✅ Hiển thị empty state
- ✅ Có thông báo "Chưa có lịch làm việc"

---

### 7.3. Data Validation

#### Test Case 7.3.1: Đăng ký với email không hợp lệ
**Bước thực hiện:**
1. Nhập email không hợp lệ (ví dụ: "test@")
2. Nhập password
3. Nhấn "Đăng ký"

**Kết quả mong đợi:**
- ❌ Hiển thị lỗi: "Email không hợp lệ"
- ✅ Không thể đăng ký

#### Test Case 7.3.2: Đăng ký với password quá ngắn
**Bước thực hiện:**
1. Nhập email hợp lệ
2. Nhập password < 6 ký tự
3. Nhấn "Đăng ký"

**Kết quả mong đợi:**
- ❌ Hiển thị lỗi: "Mật khẩu phải có ít nhất 6 ký tự"
- ✅ Không thể đăng ký

---

## 8. Checklist Testing Tổng hợp

### 8.1. User Flow Hoàn chỉnh
- [ ] Đăng ký tài khoản mới
- [ ] Đăng nhập
- [ ] Xem danh sách salon
- [ ] Tìm kiếm salon
- [ ] Xem chi tiết salon
- [ ] Đặt lịch (chọn dịch vụ, stylist, ngày, giờ)
- [ ] Xem booking thành công
- [ ] Xem lịch sử đặt lịch
- [ ] Chỉnh sửa profile
- [ ] Đăng xuất

### 8.2. Staff Flow Hoàn chỉnh
- [ ] Đăng nhập với tài khoản staff
- [ ] Xem lịch làm việc tuần hiện tại
- [ ] Điều hướng tuần (trước/sau)
- [ ] Xem chi tiết booking
- [ ] Xác nhận booking (pending → confirmed)
- [ ] Hoàn thành booking (confirmed → completed)
- [ ] Hủy booking
- [ ] Quản lý thời gian trống (đánh dấu slot không khả dụng)
- [ ] Mở lại slot đã đóng
- [ ] Xem profile
- [ ] Đăng xuất

### 8.3. Admin Flow Hoàn chỉnh
- [ ] Đăng nhập với tài khoản admin
- [ ] Xem dashboard
- [ ] Xem tất cả booking
- [ ] Đổi trạng thái booking
- [ ] Xem danh sách users
- [ ] Đổi role của user
- [ ] Đổi status của user (active/disabled)
- [ ] Thêm dịch vụ mới
- [ ] Sửa dịch vụ
- [ ] Xóa dịch vụ
- [ ] Thêm salon mới
- [ ] Sửa salon
- [ ] Xóa salon
- [ ] Cấu hình giờ làm việc
- [ ] Xem báo cáo
- [ ] Truy cập Dev Tools
- [ ] Đăng xuất

### 8.4. Security & Role Guard
- [ ] User không thể truy cập admin dashboard
- [ ] User không thể truy cập staff schedule
- [ ] Staff không thể truy cập user UI
- [ ] Staff không thể truy cập admin dashboard
- [ ] Admin không thể truy cập user UI
- [ ] Admin không thể truy cập staff schedule
- [ ] User disabled không thể đăng nhập
- [ ] Đổi role và verify navigation đúng

---

## 9. Ghi chú Testing

### 9.1. Test Accounts
- **Admin**: `admin1@lux.com` / `123456`
- **Staff**: `staff1@lux.com` / `123456` (hoặc tạo từ Dev Tools)
- **User**: Tạo mới hoặc `user1@test.com` / `123456`

### 9.2. Test Data
- Sử dụng Dev Tools để seed data nếu cần
- Có thể tạo booking test bằng cách đặt lịch từ user account

### 9.3. Firestore Collections
- `users`: Thông tin người dùng
- `salons`: Danh sách salon
- `services`: Dịch vụ (subcollection trong `salons/{salonId}/services`)
- `stylists`: Stylist (subcollection trong `salons/{salonId}/stylists`)
- `bookings`: Lịch đặt hẹn
- `workingHours`: Cấu hình giờ làm việc
- `availability`: Thời gian trống của staff

### 9.4. Common Issues
- **Lỗi không tìm thấy stylist**: Đảm bảo staff account có `stylistId` được liên kết
- **Lỗi không có slot**: Kiểm tra working hours của salon đã được cấu hình chưa
- **Lỗi permission denied**: Kiểm tra Firestore Security Rules (nếu có)

---

## 10. Kết luận

Sau khi hoàn thành tất cả các test case trên, ứng dụng sẽ được đánh giá là:
- ✅ **Functional**: Tất cả chức năng hoạt động đúng
- ✅ **Secure**: Role guard hoạt động đúng, không có truy cập trái phép
- ✅ **User-friendly**: UI/UX tốt, có empty state và error handling
- ✅ **Reliable**: Không crash, xử lý lỗi tốt

**Người test**: ___________________

**Ngày test**: ___________________

**Kết quả**: ☐ Pass  ☐ Fail (ghi chú: ___________________)

