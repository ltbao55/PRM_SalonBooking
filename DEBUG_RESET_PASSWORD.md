# 🔍 Debug - Reset Password Email Không Nhận Được

## ✅ Kiểm Tra Các Nguyên Nhân Phổ Biến

### **1. Kiểm Tra Email Có Tồn Tại Trong Firebase Không**

**Cách kiểm tra:**
1. Vào Firebase Console: https://console.firebase.google.com/project/prm-salonbooking
2. **Authentication** → **Users**
3. Tìm email bạn đang test
4. ✅ Nếu KHÔNG có → Email chưa được đăng ký → Phải đăng ký trước!

**Fix:** Đăng ký email đó trước (qua Register screen)

---

### **2. Kiểm Tra Email Có Bị Vào Spam Không**

**Kiểm tra:**
- ✅ Mở **Spam/Junk folder** trong email
- ✅ Tìm email từ: `noreply@prm-salonbooking.firebaseapp.com`
- ✅ Subject: `Reset your password for Salon Booking`

**Fix:** Nếu thấy trong Spam → Đánh dấu "Not Spam" → Thêm vào whitelist

---

### **3. Kiểm Tra Error Message Trong App**

**Sau khi click "Gửi Email":**
- ✅ App có hiện **Snackbar màu đỏ** (lỗi) không?
- ✅ Có thông báo gì không?

**Các lỗi thường gặp:**
- **"Email không tồn tại trong hệ thống"** → Email chưa đăng ký
- **"Lỗi kết nối mạng"** → Kiểm tra internet
- **"Đã gửi quá nhiều email"** → Đợi vài phút rồi thử lại

---

### **4. Kiểm Tra Firebase Authentication Đã Bật Email/Password Chưa**

**Cách kiểm tra:**
1. Firebase Console → **Authentication** → **Sign-in method**
2. Tìm **Email/Password**
3. ✅ Phải có **Enable** toggle = ON
4. ✅ Kiểm tra **Password reset** có được bật không

**Fix:** Nếu chưa bật → Click Enable → Save

---

### **5. Kiểm Tra Logcat (Debug)**

**Cách xem log:**
1. Android Studio → **Logcat** tab (phía dưới)
2. Filter: `ForgotPassword`
3. Chạy lại flow reset password
4. Xem log có error gì không

**Log mong đợi:**
```
D/ForgotPassword: Sending reset email to: your@email.com
D/ForgotPassword: Reset email sent successfully
```

**Nếu có error:**
```
E/ForgotPassword: Error: [error message]
```

---

### **6. Test Với Email Đã Đăng Ký**

**Quy trình test đúng:**
1. ✅ **Đăng ký** email mới (Register screen)
2. ✅ **Đăng nhập** bằng email đó (để verify email tồn tại)
3. ✅ **Đăng xuất**
4. ✅ **Quên mật khẩu** → Nhập email vừa đăng ký
5. ✅ **Kiểm tra email inbox** (và spam)

---

### **7. Kiểm Tra Firebase Email Sending Limits**

Firebase có giới hạn gửi email:
- ✅ Quá nhiều requests trong thời gian ngắn → Bị rate limit
- ✅ Email có thể bị delay vài phút

**Fix:** Đợi 5-10 phút rồi thử lại

---

### **8. Kiểm Tra Email Provider**

**Một số email provider có thể block Firebase emails:**
- ✅ Gmail → Thường OK
- ✅ Outlook/Hotmail → Có thể vào Spam
- ✅ Yahoo → Có thể vào Spam
- ✅ Email công ty → Có thể bị firewall block

**Fix:** Thử với Gmail trước

---

## 🔧 Cách Debug Chi Tiết

### **Bước 1: Kiểm Tra Logcat**

1. Mở Android Studio
2. **Logcat** tab
3. Filter: `ForgotPassword`
4. Chạy lại reset password flow
5. Copy toàn bộ log → Gửi cho Dev 1

### **Bước 2: Kiểm Tra Firebase Console**

1. Vào **Authentication** → **Users**
2. Xem có user với email bạn test không
3. Nếu có → Xem **Email verified** = true/false

### **Bước 3: Test Với Email Khác**

1. Thử với Gmail (khuyến nghị)
2. Nếu Gmail nhận được → Email provider của bạn có vấn đề
3. Nếu Gmail cũng không nhận được → Firebase config có vấn đề

---

## 📋 Checklist Debug

- [ ] Email đã được đăng ký trong Firebase Authentication
- [ ] Email/Password sign-in method đã bật
- [ ] Đã kiểm tra Spam folder
- [ ] App không hiện error message (Snackbar đỏ)
- [ ] Logcat không có error
- [ ] Đã đợi vài phút (email có thể delay)
- [ ] Đã thử với email khác (Gmail)

---

## ⚠️ Lưu Ý Quan Trọng

1. **Firebase chỉ gửi email cho email ĐÃ ĐĂNG KÝ**
   - Không thể gửi reset password cho email chưa tồn tại!

2. **Email có thể mất 1-5 phút mới đến**
   - Không phải real-time!

3. **Firebase có rate limit**
   - Quá nhiều requests → Phải đợi

4. **Email có thể vào Spam**
   - Luôn kiểm tra Spam folder!

---

## 🎯 Test Case Đúng

1. ✅ **Đăng ký** email `test@example.com`
2. ✅ **Đăng nhập** với email đó (verify hoạt động)
3. ✅ **Đăng xuất**
4. ✅ **Quên mật khẩu** → Nhập `test@example.com`
5. ✅ **Đợi 1-2 phút**
6. ✅ **Kiểm tra email inbox** (và spam)
7. ✅ **Click link trong email**
8. ✅ **Nhập mật khẩu mới**

---

**Nếu vẫn không nhận được email sau khi làm tất cả các bước trên → Liên hệ Dev 1 để check Firebase config!**

