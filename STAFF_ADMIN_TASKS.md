# 📋 Tasks - Màn hình cho Role Staff và Admin

## Tổng quan
- Bổ sung phân quyền (role) vào `User` (vd: `role: "user" | "staff" | "admin"`).
- Sau khi đăng nhập: điều hướng theo role đến Home tương ứng (`StaffHome`, `AdminHome`) hoặc module chung có tab theo role.
- Tất cả màn hình dùng Luxury theme hiện tại; tuân thủ FirebaseRepo + Navigation đã có.

---

## 1) Staff - Xem lịch làm việc của mình

### A. Data & Model
- [ ] Thêm trường role cho `User` (nếu chưa có) và đảm bảo set khi đăng ký/cấp quyền.
- [ ] Bảng/Lưu lịch làm việc staff (gợi ý):
  - Collection: `staffSchedules` hoặc subcollection `salons/{salonId}/staffSchedules`
  - Fields: `staffId`, `date` (yyyy-MM-dd hoặc timestamp), `shifts` (mảng), `bookings` (tham chiếu), `notes`.
- [ ] API qua `FirebaseRepo`:
  - [ ] `getStaffSchedule(staffId, startDate, endDate)` – lấy lịch theo khoảng ngày.
  - [ ] (Tuỳ chọn) `getStaffBookings(staffId, date)` – lấy các booking theo staff + ngày.

### B. UI Screens
- [ ] `StaffHomeActivity` hoặc `StaffScheduleActivity`:
  - [ ] AppBar: tiêu đề “Lịch làm của tôi”, profile icon.
  - [ ] Bộ lọc: chọn tuần/ngày (CalendarView/DateRange picker).
  - [ ] Lịch theo tuần/ngày (RecyclerView dạng lịch, hoặc Grid theo time-slot):
    - [ ] Hiển thị ca (morning/afternoon/evening) hoặc time-slot 30/60 phút.
    - [ ] Gắn nhãn booking đã được đặt (read only) – lấy từ bookings.
  - [ ] Empty state (khi chưa có lịch).
- [ ] `StaffBookingDetailBottomSheet` (tuỳ chọn):
  - [ ] Khi bấm vào slot đã có booking → show chi tiết (khách, dịch vụ, giờ).

### C. Logic
- [ ] Tự động load lịch theo tuần hiện tại khi mở màn hình.
- [ ] Kéo đổi tuần/ngày → reload `getStaffSchedule(...)`.
- [ ] Chỉ hiển thị lịch của `uid` hiện tại (role staff).
- [ ] Bảo vệ route: nếu user không phải staff → điều hướng về Home mặc định.

### D. Chức năng bổ sung cơ bản
- [ ] Cập nhật trạng thái ca làm: đánh dấu "đang làm"/"nghỉ"/"bận" theo time-slot.
- [ ] Xem chi tiết booking của mình: khách hàng, dịch vụ, ghi chú.
- [ ] Xem lịch sử ca làm/booking của bản thân theo ngày/tuần/tháng.
- [ ] Nhận thông báo: booking mới, thay đổi/huỷ lịch (push/in-app).
- [ ] Quản lý thời gian trống (availability): mở/đóng slot cơ bản (nếu được phân quyền).
- [ ] Cập nhật hồ sơ cá nhân: tên, ảnh đại diện, thông tin liên hệ.

---

## 2) Admin - Quản lý lịch & tài khoản

### A. Data & Model
- [ ] Mở rộng `User` với `role` và (tuỳ chọn) `status` (active/disabled).
- [ ] Chỉ admin mới có quyền đọc/ghi mọi dữ liệu – đảm bảo `Security Rules` Firestore phù hợp.

### B. UI Screens
- [ ] `AdminDashboardActivity`:
  - [ ] Cards/tabs: “Tất cả lịch”, “Quản lý tài khoản”, “Báo cáo”.
- [ ] `AdminAllSchedulesActivity`:
  - [ ] Bộ lọc: salon, staff, ngày/tuần, trạng thái (pending/confirmed/...)
  - [ ] Danh sách lịch (RecyclerView): salon, staff, dịch vụ, giờ, trạng thái.
  - [ ] Xem chi tiết booking (BottomSheet/Dialog): đổi trạng thái (confirm/cancel), ghi chú.
- [ ] `AdminUsersActivity` (Quản lý tài khoản):
  - [ ] Danh sách users (name, email, role, status).
  - [ ] Thêm/sửa/xoá user (chỉ admin):
    - [ ] Set role (user/staff/admin), set status (active/disabled).
  - [ ] Tìm kiếm/lọc theo role/status.
- [ ] (Tuỳ chọn) `AdminReportsActivity`:
  - [ ] Thống kê số booking theo ngày/tuần/staff, doanh thu theo dịch vụ.

### C. FirebaseRepo APIs (đề xuất)
- [ ] Schedules/Bookings:
  - [ ] `getAllBookings(filters)` – lọc theo salon, staff, date range, status.
  - [ ] `updateBookingStatus(bookingId, status)` – admin đổi trạng thái.
- [ ] Users:
  - [ ] `getAllUsers(filters)` – lọc theo role/status.
  - [ ] `updateUserRole(uid, role)` – set role.
  - [ ] `updateUserStatus(uid, status)` – vô hiệu hóa/kích hoạt.

### D. Logic & Bảo mật
- [ ] Điều hướng: nếu `role != admin` → chặn vào màn hình admin, điều hướng về Home.
- [ ] Firestore Security Rules (sau khi xác định cấu trúc collections):
  - [ ] Staff chỉ đọc dữ liệu lịch của chính mình.
  - [ ] Admin có quyền đọc/ghi mọi lịch và người dùng.
  - [ ] Người dùng thường chỉ có quyền đọc/ghi bookings của chính họ.

### E. Chức năng bổ sung cơ bản
- [ ] Duyệt/điều phối lịch: gán booking cho staff, chuyển staff, đổi khung giờ cơ bản.
- [ ] Quản lý danh mục dịch vụ: thêm/sửa/xoá dịch vụ, giá, thời lượng.
- [ ] Quản lý salon/chi nhánh: thông tin cơ bản (tên, địa chỉ, giờ mở cửa).
- [ ] Quản lý thông báo: gửi thông báo tới staff hoặc toàn bộ hệ thống.
- [ ] Quản lý phân quyền: gán role (user/staff/admin), khoá/mở tài khoản.
- [ ] Xem báo cáo nhanh: tổng số booking theo ngày/tuần, top dịch vụ, xuất CSV nhẹ.
- [ ] Cấu hình khung giờ làm việc: ca sáng/chiều/tối mặc định theo ngày trong tuần.

---

## 3) Điều hướng theo Role sau đăng nhập
- [ ] Sau khi login, lấy `User` từ Firestore để biết `role`.
  - [ ] Nếu `staff` → `StaffHomeActivity` (hoặc tab Staff trong Home chung).
  - [ ] Nếu `admin` → `AdminDashboardActivity`.
  - [ ] Nếu `user` → `HomeActivity` (hiện tại).
- [ ] Lưu role vào Session/SharedPreferences để tối ưu lần mở sau.

---

## 4) Hạng mục kỹ thuật & UX bổ sung
- [ ] Loading/Empty/Error state thống nhất (Snackbar/Toast + shimmer/placeholder).
- [ ] Bộ lọc ngày/tuần thân thiện (Material Date/Range pickers).
- [ ] Pagination/limit khi tải danh sách lớn (bookings/users).
- [ ] Kiểm tra quyền (guard) ở đầu mỗi Activity/Fragment theo role.
- [ ] Unit test cơ bản cho `FirebaseRepo` methods mới.

---

## 5) Ưu tiên triển khai (gợi ý)
1. Model & API FirebaseRepo (role, schedules, admin users).
2. StaffSchedule UI + logic (đọc lịch của `uid`).
3. AdminAllSchedules UI + update status.
4. AdminUsers UI + set role/status.
5. Điều hướng theo role & Security Rules.
