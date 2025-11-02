# 📧 Customize Email Template - Password Reset

## ✅ Chức Năng Reset Password Đã Sẵn Sàng!

Chức năng reset password đã được implement đầy đủ:
- ✅ `ForgotPasswordActivity` - Màn hình quên mật khẩu
- ✅ Link từ Login screen: "Quên mật khẩu?"
- ✅ Firebase integration - Gửi email reset tự động

---

## 🎨 Customize Email Template trong Firebase

### **Bước 1: Truy Cập Email Templates**

1. Vào Firebase Console: https://console.firebase.google.com/project/prm-salonbooking
2. Vào **Authentication** → **Templates** (hoặc **Email templates**)
3. Click vào **"Password reset"**

### **Bước 2: Customize Email Template**

#### **2.1. Thay Đổi Subject (Tiêu đề email)**

- **Hiện tại**: `Reset your password for Salon Booking`
- **Có thể đổi thành**: `Đặt lại mật khẩu Salon Booking` hoặc `Reset Password - Salon Booking`

#### **2.2. Thay Đổi Email Body (Nội dung email)**

**Email mặc định:**
```
Hello,

Follow this link to reset your %APP_NAME% password for your %EMAIL% account.

https://prm-salonbooking.firebaseapp.com/__/auth/action?mode=action&oobCode=code

If you didn't ask to reset your password, you can ignore this email.

Thanks,
Your %APP_NAME% team
```

**Email tiếng Việt đề xuất:**
```
Xin chào,

Vui lòng click vào liên kết bên dưới để đặt lại mật khẩu cho tài khoản %EMAIL% của bạn trên %APP_NAME%.

https://prm-salonbooking.firebaseapp.com/__/auth/action?mode=action&oobCode=code

Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.

Trân trọng,
Đội ngũ %APP_NAME%
```

#### **2.3. Customize Sender Name**

- **Sender name**: Có thể đổi thành `Salon Booking Team` hoặc `Salon Booking App`
- **From**: `noreply@prm-salonbooking.firebaseapp.com` (không thể đổi)

### **Bước 3: Customize Action URL (Tùy chọn)**

Nếu bạn muốn redirect sau khi user click link reset, có thể setup:
1. Vào **Authentication** → **Settings** → **Authorized domains**
2. Thêm domain của bạn (nếu có custom domain)

### **Bước 4: Customize Language**

1. Ở phần **Template language** (góc dưới bên trái)
2. Chọn ngôn ngữ: **Vietnamese (Tiếng Việt)**
3. Sau đó customize template cho ngôn ngữ đó

---

## 🔗 Placeholders Có Sẵn

Trong email template, bạn có thể dùng các biến:
- `%APP_NAME%` - Tên ứng dụng (tự động lấy từ Firebase project)
- `%EMAIL%` - Email của người dùng
- `%LINK%` - Link reset password (tự động generate)

---

## 🧪 Test Reset Password

### **Cách Test:**

1. **Chạy app**
2. **Login Screen** → Click **"Quên mật khẩu?"**
3. **Forgot Password Screen** → Nhập email đã đăng ký
4. **Click "Gửi Email Đặt Lại Mật Khẩu"**
5. **Kiểm tra email inbox** (hoặc spam folder)
6. **Click link trong email** → Mở browser
7. **Nhập mật khẩu mới** → Xong!

### **Lưu Ý:**
- Email có thể mất vài giây để gửi đến
- Kiểm tra cả **spam folder** nếu không thấy
- Link reset có thời hạn (mặc định 1 giờ)

---

## 📝 Các Email Templates Khác

Firebase cũng có các email templates khác bạn có thể customize:
- ✅ **Email address verification** - Xác thực email
- ✅ **Email address change** - Thay đổi email
- ✅ **Multi-factor enrollment** - Xác thực 2 lớp

---

## 🎯 Gợi Ý Customize

### **Tiếng Việt:**
```html
Subject: Đặt lại mật khẩu Salon Booking

Body:
Chào bạn,

Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản %EMAIL% trên ứng dụng Salon Booking.

Vui lòng click vào liên kết sau để tạo mật khẩu mới:

%LINK%

Liên kết này sẽ hết hạn sau 1 giờ.

Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này và giữ nguyên mật khẩu hiện tại.

Cảm ơn bạn đã sử dụng Salon Booking!

Trân trọng,
Đội ngũ Salon Booking
```

---

**Lưu ý:** Sau khi customize, click **"Save"** để lưu thay đổi. Email template mới sẽ được áp dụng cho các email reset password tiếp theo.

