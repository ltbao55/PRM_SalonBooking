Mô Tả Dự Án Chi Tiết: Ứng Dụng Đặt Lịch Salon (Hair Salon Booking)

1. Tên Dự Án
SalonBooking
2. Mục Tiêu
Xây dựng một ứng dụng Android native (Java) cho phép khách hàng duyệt, tìm kiếm và đặt lịch hẹn tại các salon tóc. Hệ thống phải sử dụng cơ sở dữ liệu cloud (Firebase) để đảm bảo dữ liệu (lịch hẹn, trạng thái slot) được đồng bộ theo thời gian thực trên mọi thiết bị.
3. Đối Tượng Người Dùng
Khách hàng (Người dùng cuối): Người tải ứng dụng để đặt lịch.
(Ngoài phạm vi V1): Chủ Salon, Stylist (Sẽ cần một app riêng hoặc giao diện web admin).
4. Yêu Cầu Công Nghệ
Frontend: Android Native (Java), XML Layouts, Kiến trúc MVVM.
Backend (BaaS): Firebase (Firestore, Authentication, Storage).

5. Mô Tả Chức Năng Chi Tiết (Theo Module)
Đây là 4 module chính, được phân chia để 4 lập trình viên có thể phát triển song song.
MODULE 1: NỀN TẢNG & LÕI (CORE & REPOSITORY)
Mô tả: Module này không có giao diện người dùng. Đây là "trái tim" của ứng dụng, cung cấp nền tảng và các phương thức truy cập dữ liệu cho 3 module còn lại. Module này phải được hoàn thành đầu tiên.
Chức năng 1.1: Thiết Lập Firebase
Khởi tạo dự án Firebase.
Kết nối app Android với Firebase (thêm file google-services.json).
Thêm các dependency (SDK) của Firebase: firestore, auth, storage.
Thiết lập Quy tắc Bảo mật (Security Rules) cho Firestore (ví dụ: user chỉ được đọc/ghi booking của chính mình).
Chức năng 1.2: Định Nghĩa Data Models (POJO)
Đây là "Hợp đồng dữ liệu" cho toàn team.
User.java: (uid, name, email, avatarUrl)
Salon.java: (salonId, name, address, imageUrl, phone, listServices)
Service.java: (serviceId, name, price, durationInMinutes)
Booking.java: (bookingId, userId, salonId, serviceId, stylistId (nếu có), bookingTime (Timestamp), status)
Chức năng 1.3: Xây Dựng Lớp Repository (Singleton Pattern)
Tạo lớp FirebaseRepository.java (Singleton).
Lớp này đóng gói (encapsulate) MỌI lời gọi đến Firebase. 3 dev còn lại sẽ KHÔNG gọi Firebase trực tiếp, mà chỉ gọi qua Repository này.
Chức năng 1.4: Triển Khai Các Phương Thức Truy Cập Dữ Liệu
Xác thực: login(email, pass, callback), register(email, pass, name, callback), signOut(), getCurrentUser().
Người dùng: getUserData(uid, callback<User>), updateUserData(uid, User data, callback).
Salon/Dịch vụ: getAllSalons(callback<List<Salon>>), getServicesForSalon(salonId, callback<List<Service>>)
Booking: createBooking(Booking booking, callback), cancelBooking(bookingId, callback).
Real-time (Quan trọng): getUserBookingsListener(userId, callback<List<Booking>>) (Sử dụng addSnapshotListener của Firestore để tự động cập nhật khi có thay đổi).
Logic Slot: getAvailableTimeSlots(date, salonId, serviceDuration, callback<List<TimeSlot>>) (Hàm này đọc tất cả bookings của ngày đó để tính toán các slot còn trống).
Storage: uploadProfileImage(Uri imageUri, uid, callback<String_url>).

MODULE 2: XÁC THỰC NGƯỜI DÙNG (AUTHENTICATION)
Mô tả: Chịu trách nhiệm cho toàn bộ luồng đăng nhập, đăng ký và quản lý phiên làm việc của người dùng.
Chức năng 2.1: Màn hình Chào (Splash Screen)
Giao diện: Logo ứng dụng.
Logic: Kiểm tra FirebaseRepository.getCurrentUser().
Nếu null -> Chuyển đến LoginActivity.
Nếu khác null -> Chuyển đến HomeActivity.
Chức năng 2.2: Màn hình Đăng Nhập (Login Screen)
Giao diện: EditText (Email), EditText (Password - ẩn), Button ("Đăng nhập"), TextView ("Quên mật khẩu?"), TextView ("Chưa có tài khoản? Đăng ký").
Logic:
Validate input (không trống, đúng định dạng email).
Gọi FirebaseRepository.getInstance().login(...) khi bấm nút.
Hiển thị ProgressBar khi đang gọi.
Xử lý callback: Thành công -> Chuyển HomeActivity. Thất bại -> Hiển thị Toast/Snackbar (ví dụ: "Sai email hoặc mật khẩu").
Chức năng 2.3: Màn hình Đăng Ký (Register Screen)
Giao diện: EditText (Họ tên), EditText (Email), EditText (Password), EditText (Nhập lại Password).
Logic:
Validate input (không trống, password khớp, email hợp lệ).
Gọi FirebaseRepository.getInstance().register(...).
Quan trọng: Hàm register trong Repository phải thực hiện 2 việc: 1. Tạo user trong Firebase Auth. 2. Lấy uid và tạo một document mới trong collection Users trên Firestore.
Thông báo thành công và điều hướng về LoginActivity.

MODULE 3: KHÁM PHÁ & ĐẶT LỊCH (DISCOVERY & BOOKING)
Mô tả: Đây là luồng nghiệp vụ cốt lõi. Từ việc xem salon đến việc hoàn tất một lịch hẹn.
Chức năng 3.1: Màn hình Chính (Home Activity & Home Fragment)
Giao diện: Chứa BottomNavigationView (3 tab: Home, History, Profile) và FragmentContainerView.
Giao diện (Home Fragment):
Thanh tìm kiếm (Search Bar).
(Tùy chọn) Banner/Carousel quảng cáo.
RecyclerView "Salon Nổi Bật" (Hiển thị ngang).
RecyclerView "Dịch Vụ Hot" (Hiển thị ngang).
Logic: Gọi FirebaseRepository.getInstance().getAllSalons(...) để đổ dữ liệu vào RecyclerView.
Chức năng 3.2: Màn hình Chi Tiết Salon (Salon Detail Activity)
Giao diện: Ảnh bìa salon, Tên, Địa chỉ, Đánh giá, Giờ mở cửa.
RecyclerView "Danh sách Dịch vụ": Mỗi item hiển thị (Tên dịch vụ, Giá, Thời gian thực hiện (duration)).
Button "Đặt lịch".
Logic: Nhận salonId từ Intent. Gọi FirebaseRepository.getInstance().getServicesForSalon(salonId) để lấy danh sách dịch vụ.
Chức năng 3.3: Màn hình Đặt Lịch (Booking Activity)
Giao diện: Đây là màn hình phức tạp, có thể chia theo bước (steps).
Step 1: Hiển thị dịch vụ đã chọn (từ màn hình trước), cho phép chọn thêm (nếu có).
Step 2: Hiển thị CalendarView (Lịch) để chọn ngày. Chỉ cho phép chọn từ ngày hiện tại trở đi.
Step 3: Hiển thị RecyclerView (dạng Grid) các khung giờ (Time Slots).
Logic (Quan trọng):
Khi người dùng chọn một ngày (Step 2), ứng dụng gọi FirebaseRepository.getInstance().getAvailableTimeSlots(...) với (date, salonId, serviceDuration).
Repository sẽ tính toán và trả về danh sách các slot còn trống.
Hiển thị các slot lên UI (Step 3). Các slot đã bị book phải bị vô hiệu hóa (disabled).
Logic (Xác nhận):
Khi người dùng chọn 1 slot và bấm "Xác nhận".
Tạo đối tượng Booking mới (với userId, salonId, serviceId, bookingTime, status = "upcoming").
Gọi FirebaseRepository.getInstance().createBooking(...).
Chức năng 3.4: Màn hình Đặt Lịch Thành Công (Booking Success Activity)
Giao diện: Icon checkmark, text "Đặt lịch thành công!", tóm tắt thông tin lịch hẹn (dịch vụ, thời gian, địa chỉ salon).
Button "Về Trang chủ" hoặc "Xem Lịch sử".

MODULE 4: QUẢN LÝ CÁ NHÂN & LỊCH SỬ (PROFILE & HISTORY)
Mô tả: Quản lý các thông tin cá nhân của người dùng và lịch sử các phiên đặt lịch của họ.
Chức năng 4.1: Màn hình Lịch Sử (Booking History Fragment)
Giao diện: Tab "Sắp tới" (Upcoming) và Tab "Đã qua" (Completed/Cancelled).
Mỗi tab là một RecyclerView hiển thị các lịch hẹn.
Logic (Quan trọng):
Gọi FirebaseRepository.getInstance().getUserBookingsListener(userId, ...)
Đây là một listener real-time. Bất cứ khi nào có booking mới (từ Module 3) hoặc booking bị hủy, danh sách này sẽ tự động cập nhật mà không cần kéo để làm mới (pull-to-refresh).
Phân loại danh sách booking nhận về vào 2 tab dựa trên status và bookingTime.
Chức năng 4.2: Màn hình Chi Tiết Lịch Hẹn (Booking Detail Activity)
Giao diện: Hiển thị chi tiết một lịch hẹn (khi bấm vào item ở 4.1). Gồm: Tên salon, địa chỉ, dịch vụ, thời gian, tổng tiền.
Button "Hủy lịch" (Chỉ hiển thị nếu lịch đó là "upcoming").
Logic: Khi bấm "Hủy lịch", gọi FirebaseRepository.getInstance().cancelBooking(bookingId). (Hàm này sẽ đổi status thành "cancelled"). Màn hình 4.1 sẽ tự động cập nhật.
Chức năng 4.3: Màn hình Hồ Sơ (Profile Fragment)
Giao diện: Ảnh đại diện (ImageView), Tên (TextView), Email (TextView).
Các tùy chọn: "Chỉnh sửa hồ sơ", "Đăng xuất".
Logic:
Gọi FirebaseRepository.getInstance().getUserData(...) để hiển thị thông tin.
Nút "Đăng xuất": Gọi FirebaseRepository.getInstance().signOut() và điều hướng về LoginActivity.
Chức năng 4.4: Màn hình Chỉnh Sửa Hồ Sơ (Edit Profile Activity)
Giao diện: ImageView (cho phép chọn ảnh mới), EditText (Họ tên - có sẵn data cũ).
Logic:
Chọn ảnh: Mở Thư viện/Camera (Image Picker).
Lưu:
Nếu có ảnh mới, gọi FirebaseRepository.getInstance().uploadProfileImage(...) để tải ảnh lên Firebase Storage.
Nhận về URL của ảnh.
Gọi FirebaseRepository.getInstance().updateUserData(...) với (tên mới) và (URL ảnh mới).
Thông báo thành công và quay lại.



