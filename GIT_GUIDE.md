# 📚 Hướng Dẫn Git - Tạo Nhánh Mới và Push Code Lên GitHub

## 📋 Trạng Thái Hiện Tại

- **Repository**: `https://github.com/ltbao55/PRM_SalonBooking.git`
- **Nhánh hiện tại**: `main`
- **Remote**: `origin` đã được cấu hình

---

## 🚀 Các Bước Tạo Nhánh Mới và Push Code

### **Bước 1: Commit các thay đổi hiện tại trên nhánh main (nếu cần)**

Nếu bạn muốn lưu công việc hiện tại trên nhánh main trước:

```bash
# Xem các file đã thay đổi
git status

# Add tất cả các file (HOẶC add từng file cụ thể)
git add .

# Hoặc add từng file cụ thể:
# git add app/src/main/java/com/example/prm_be/ui/
# git add app/src/main/res/
# git add BUILD_FIX_GUIDE.md
# git add CRASH_FIX.md
# git add UI_LAYOUT_GUIDE.md
# git add UI_UX_COMPLETE.md
# git add GIT_GUIDE.md

# Commit với message mô tả
git commit -m "feat: Thêm UI/UX cơ bản cho toàn bộ ứng dụng

- Tạo color scheme và themes
- Xây dựng UI cho 11 màn hình
- Fix crash khi Firebase chưa init
- Thêm debug navigation buttons
- Cấu hình JDK 21"

# Push lên GitHub nhánh main
git push origin main
```

---

### **Bước 2: Tạo và chuyển sang nhánh mới**

```bash
# Cách 1: Tạo nhánh mới và chuyển sang ngay
git checkout -b feature/ui-ux-implementation

# Hoặc Cách 2: Tạo nhánh mới (nhưng vẫn ở main)
git branch feature/ui-ux-implementation
# Sau đó chuyển sang nhánh mới
git checkout feature/ui-ux-implementation

# Xem nhánh hiện tại
git branch
```

**Lưu ý**: Đặt tên nhánh theo quy ước:

- `feature/` - Tính năng mới (ví dụ: `feature/login-screen`)
- `fix/` - Sửa lỗi (ví dụ: `fix/crash-on-startup`)
- `refactor/` - Refactor code (ví dụ: `refactor/firebase-repo`)
- `ui/` - UI/UX (ví dụ: `ui/material-design`)

---

### **Bước 3: Commit các thay đổi vào nhánh mới**

```bash
# Xem các file thay đổi
git status

# Add các file cần commit
git add .

# Hoặc add từng nhóm file:
# git add app/src/main/java/com/example/prm_be/ui/
# git add app/src/main/res/
# git add *.md

# Commit với message mô tả rõ ràng
git commit -m "feat: Implement UI/UX for all screens

- Add Material Design 3 components
- Create color scheme (Purple/Pink theme)
- Build layouts for 11 screens:
  * Authentication: Splash, Login, Register
  * Discovery: Home, SalonList, SalonDetail
  * Booking: Booking, BookingSuccess
  * Profile: Profile, EditProfile, BookingHistory
- Fix Firebase initialization crash
- Add debug navigation buttons for testing
- Configure JDK 21 in gradle.properties"
```

---

### **Bước 4: Push nhánh mới lên GitHub**

```bash
# Push nhánh mới lên GitHub (lần đầu tiên)
git push -u origin feature/ui-ux-implementation

# Lần sau chỉ cần:
# git push
```

**Giải thích**:

- `-u` (hoặc `--set-upstream`) để set upstream tracking
- `origin` là tên remote (GitHub)
- `feature/ui-ux-implementation` là tên nhánh

---

### **Bước 5: Tạo Pull Request trên GitHub (nếu cần)**

Sau khi push, bạn có thể:

1. Vào GitHub: `https://github.com/ltbao55/PRM_SalonBooking`
2. GitHub sẽ tự động hiện thông báo "Compare & pull request"
3. Click vào đó để tạo Pull Request
4. Điền title và description
5. Chọn reviewer (nếu có)
6. Click "Create Pull Request"

---

## 🔄 Các Lệnh Git Hữu Ích Khác

### **Xem lịch sử commit**

```bash
git log --oneline
git log --graph --oneline --all
```

### **Xem sự khác biệt**

```bash
# Xem diff của các file chưa staged
git diff

# Xem diff của các file đã staged
git diff --staged
```

### **Quay lại nhánh main**

```bash
git checkout main
```

### **Xem tất cả nhánh (local + remote)**

```bash
git branch -a
```

### **Xóa nhánh local**

```bash
git branch -d feature/ui-ux-implementation  # Xóa sau khi merge
git branch -D feature/ui-ux-implementation # Force delete
```

### **Xóa nhánh trên GitHub**

```bash
git push origin --delete feature/ui-ux-implementation
```

### **Lấy code mới nhất từ GitHub**

```bash
git fetch origin
git pull origin main
```

---

## 📝 Quy Ước Commit Message

Sử dụng format chuẩn:

```
<type>: <subject>

<body>
```

**Types:**

- `feat`: Tính năng mới
- `fix`: Sửa lỗi
- `docs`: Tài liệu
- `style`: Formatting (không ảnh hưởng code)
- `refactor`: Refactor code
- `test`: Thêm/sửa tests
- `chore`: Công việc khác (build, config)

**Ví dụ:**

```
feat: Add Material Design UI components

- Implement login screen with TextInputLayout
- Add color scheme (Purple/Pink theme)
- Create reusable button styles
```

---

## ⚠️ Lưu Ý Quan Trọng

### **KHÔNG commit các file sau:**

- `build/` - Build artifacts
- `.idea/` - Android Studio settings
- `local.properties` - Local config
- `*.iml` - IntelliJ module files
- `google-services.json` - Firebase config (nếu project public)

### **File .gitignore đã có sẵn:**

File `.gitignore` đã được tạo và sẽ tự động ignore các file không cần thiết.

---

## 🎯 Ví Dụ Workflow Hoàn Chỉnh

```bash
# 1. Kiểm tra trạng thái
git status

# 2. Tạo nhánh mới
git checkout -b feature/salon-detail-screen

# 3. Làm việc và thay đổi code...

# 4. Add và commit
git add app/src/main/res/layout/activity_salon_detail.xml
git add app/src/main/java/com/example/prm_be/ui/discovery/SalonDetailActivity.java
git commit -m "feat: Implement SalonDetailActivity with CollapsingToolbar"

# 5. Push lên GitHub
git push -u origin feature/salon-detail-screen

# 6. Tạo Pull Request trên GitHub (qua web interface)
```

---

## 📚 Tài Liệu Tham Khảo

- [Git Documentation](https://git-scm.com/doc)
- [GitHub Flow](https://guides.github.com/introduction/flow/)
- [Conventional Commits](https://www.conventionalcommits.org/)

---

**Chúc bạn làm việc với Git vui vẻ! 🚀**
