# 📋 Checklist - Files Cần Gửi Cho Team Dev

## ✅ Files Cần Gửi Cho Mỗi Dev

### **1. `google-services.json` (BẮT BUỘC) 🔥**

**File:** `app/google-services.json`

**Cách lấy:**
- Vào Firebase Console: https://console.firebase.google.com/project/prm-salonbooking
- **Project Settings** → **Your apps** → Android app
- Click **"Download google-services.json"**

**Cách gửi:**
- ✅ Qua **Git** (private repo) - Tốt nhất
- ✅ Qua **Email** 
- ✅ Qua **Chat/Team chat** (Slack, Discord, etc)
- ✅ Qua **Google Drive/OneDrive** (share link)

**Lưu ý:**
- ⚠️ File này **KHÔNG được commit** vào Git (đã có trong `.gitignore`)
- ⚠️ Mỗi dev cần file này để app kết nối với Firebase
- ⚠️ File này **KHÔNG chứa secret keys nguy hiểm** (chỉ là config public)

---

### **2. Thông Tin Firebase Config (TRONG SETUP_GUIDE.md) 📝**

**File:** `SETUP_GUIDE.md` (đã có sẵn trong repo)

**Thông tin đã bao gồm:**
- ✅ Firebase Project ID: `prm-salonbooking`
- ✅ Project Number: `407661589941`
- ✅ Package Name: `com.example.prm_be`
- ✅ Web Client ID: `407661589941-cp64ed9d0vqghvrj75ap4ohd0bhpelpr.apps.googleusercontent.com`
- ✅ SHA-1 Fingerprint: `C5:4D:C5:BB:27:2E:BA:20:56:38:17:54:22:54:1C:8F:20:24:61:71`
- ✅ Firebase Console links
- ✅ Firebase Collections structure

**→ Team chỉ cần đọc `SETUP_GUIDE.md` là đủ!**

---

## 📦 Files KHÔNG Cần Gửi (Đã Có Trong Repo)

### **✅ Đã có sẵn trong Git:**
- ✅ Source code (Java files)
- ✅ Layout files (XML)
- ✅ Resource files (strings, colors, themes)
- ✅ Gradle config files
- ✅ `README.md`
- ✅ `SETUP_GUIDE.md`
- ✅ `instrucition.md`

### **❌ KHÔNG cần gửi:**
- ❌ `local.properties` (mỗi dev có SDK path riêng)
- ❌ Build files (`build/`, `.gradle/`)
- ❌ IDE config (`.idea/`)
- ❌ APK files

---

## 🔐 Security Check - Không Có Secret Keys

### **✅ Public IDs (An toàn để share):**
- ✅ Web Client ID trong `strings.xml` → **Public**, không nguy hiểm
- ✅ Firebase Project ID → **Public**, không nguy hiểm
- ✅ Package Name → **Public**, không nguy hiểm

### **⚠️ Không có:**
- ❌ API Keys hardcoded
- ❌ Secret keys trong code
- ❌ Database passwords
- ❌ Private keys

**→ Project an toàn để share với team!**

---

## 📋 Checklist Cho Dev Mới

### **Bước 1: Clone Repository**
```bash
git clone <repository-url>
cd PRM_SalonBooking
```

### **Bước 2: Nhận `google-services.json`**
- ✅ Lấy từ Dev 1 (qua Git, email, hoặc chat)
- ✅ Copy vào `app/google-services.json`

### **Bước 3: Đọc Hướng Dẫn**
- ✅ Đọc `SETUP_GUIDE.md`
- ✅ Đọc `README.md`

### **Bước 4: Sync & Build**
- ✅ Android Studio → Sync Gradle
- ✅ Build → Make Project
- ✅ Run app

---

## 🚀 Cách Gửi `google-services.json` Cho Team

### **Option 1: Qua Git (Khuyến nghị - Private Repo)**
1. Tạo **private GitHub/GitLab repository**
2. Dev 1 commit `google-services.json` vào repo (hoặc dùng Git LFS)
3. Team clone repo → File tự động có

**Lưu ý:** Nếu repo **public**, KHÔNG commit `google-services.json`!

### **Option 2: Qua Email/Drive**
1. Dev 1 tải `google-services.json` từ Firebase
2. Gửi file qua email/Drive cho team
3. Mỗi dev copy vào `app/google-services.json`

### **Option 3: Qua Team Chat**
1. Upload `google-services.json` lên Slack/Discord
2. Team download về
3. Copy vào project

### **Option 4: Từng Dev Tự Tải**
1. Mỗi dev vào Firebase Console
2. Tự tải `google-services.json`
3. Copy vào `app/google-services.json`

**Lưu ý:** Option này cần mỗi dev có quyền truy cập Firebase Console!

---

## ✅ Final Checklist Trước Khi Share Project

### **Code Review:**
- [x] ✅ `.gitignore` đã có `app/google-services.json`
- [x] ✅ Không có hardcoded secret keys
- [x] ✅ Không có personal info trong code
- [x] ✅ `README.md` đầy đủ thông tin
- [x] ✅ `SETUP_GUIDE.md` có đầy đủ hướng dẫn

### **Documentation:**
- [x] ✅ `SETUP_GUIDE.md` - Hướng dẫn setup đầy đủ
- [x] ✅ `README.md` - Tổng quan dự án
- [x] ✅ `instrucition.md` - Yêu cầu và phân công
- [x] ✅ `DEBUG_RESET_PASSWORD.md` - Debug guide
- [x] ✅ `CUSTOMIZE_EMAIL_TEMPLATE.md` - Email template guide

### **Files Cần Gửi:**
- [ ] ⚠️ `google-services.json` → Gửi riêng (không qua Git)
- [x] ✅ Repository URL → Share với team
- [x] ✅ Firebase Console access (nếu cần) → Share credentials

---

## 📞 Support

**Nếu team gặp vấn đề:**
1. Đọc `SETUP_GUIDE.md`
2. Kiểm tra `DEBUG_RESET_PASSWORD.md` (nếu lỗi reset password)
3. Liên hệ **Dev 1 (Firebase Lead)**

---

**Tóm lại: Team chỉ cần `google-services.json` + Clone repo → Xong! 🚀**

