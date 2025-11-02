# 🔧 Hướng Dẫn Sửa Lỗi Build

## ❌ Lỗi Hiện Tại

```
Error: Dependency requires at least JVM runtime version 11. This build uses a Java 8 JVM.
```

**Nguyên nhân**: Máy đang dùng Java 8, nhưng dự án yêu cầu Java 11+.

## ✅ Giải Pháp

### Cách 1: Sử dụng Java từ Android Studio (Khuyến nghị)

1. **Mở Android Studio**
2. **File → Project Structure** (hoặc `Ctrl+Alt+Shift+S`)
3. **SDK Location tab**:
   - Xem đường dẫn **JDK location** (thường là `C:\Program Files\Android\Android Studio\jbr`)
   - Android Studio đi kèm JDK 11+ (JBR - JetBrains Runtime)
4. **Settings → Build, Execution, Deployment → Build Tools → Gradle**:
   - **Gradle JDK**: Chọn **"Embedded JDK"** hoặc **"jbr-11"** (JDK 11)
   - Click **Apply** → **OK**
5. **Sync Project với Gradle Files**:
   - Click **Sync Now** hoặc **File → Sync Project with Gradle Files**

### Cách 2: Cài Java 11+ và Cấu Hình

1. **Tải Java 11 hoặc Java 17**:

   - Oracle JDK: https://www.oracle.com/java/technologies/downloads/
   - OpenJDK: https://adoptium.net/ (khuyến nghị - miễn phí)

2. **Cài đặt Java**:

   - Giải nén hoặc cài đặt vào thư mục (ví dụ: `C:\Program Files\Java\jdk-11`)

3. **Thiết lập JAVA_HOME**:

   - **System Properties → Environment Variables**
   - Thêm **JAVA_HOME** = đường dẫn đến JDK (ví dụ: `C:\Program Files\Java\jdk-11`)
   - Thêm vào **Path**: `%JAVA_HOME%\bin`

4. **Cấu hình trong Android Studio**:
   - **Settings → Build, Execution, Deployment → Build Tools → Gradle**
   - **Gradle JDK**: Chọn JDK 11+ vừa cài
   - **Sync Project**

### Cách 3: Cấu Hình Trong gradle.properties (Tạm thời)

Thêm vào file `gradle.properties`:

```
org.gradle.java.home=C:\\Program Files\\Android\\Android Studio\\jbr
```

(Lưu ý: Đường dẫn có thể khác tùy cài đặt Android Studio)

## 🧹 Sau Khi Cấu Hình Java

1. **Clean Project**:

   ```
   Build → Clean Project
   ```

2. **Invalidate Caches** (nếu vẫn lỗi):

   ```
   File → Invalidate Caches → Invalidate and Restart
   ```

3. **Rebuild Project**:
   ```
   Build → Rebuild Project
   ```

## ✅ Kiểm Tra

Sau khi cấu hình xong, kiểm tra:

```bash
java -version
```

Phải hiển thị Java 11 trở lên.

## 📝 Lưu Ý

- **AGP 8.13.0** yêu cầu **Java 11+** (không thể downgrade)
- **Android Studio** thường đi kèm JDK 11 (JBR), không cần cài thêm
- Nếu vẫn lỗi, kiểm tra **Gradle JDK** trong **Settings → Gradle**

---

**Sau khi fix, project sẽ build thành công! 🎉**
