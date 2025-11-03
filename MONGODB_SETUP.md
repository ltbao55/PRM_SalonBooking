# 🍃 Hướng Dẫn Thiết Lập MongoDB

Dự án đã được tích hợp MongoDB để lưu trữ dữ liệu. Tài liệu này hướng dẫn cách sử dụng MongoDB trong ứng dụng.

## 📋 Mục Lục
1. [Cấu Trúc](#cấu-trúc)
2. [Cấu Hình](#cấu-hình)
3. [Sử Dụng](#sử-dụng)
4. [API Reference](#api-reference)

---

## 📁 Cấu Trúc

### Các Class Chính

- **MongoDBConnection**: Quản lý kết nối đến MongoDB cluster
- **MongoDBRepo**: Repository pattern chứa tất cả các CRUD operations

### Location
- `app/src/main/java/com/example/prm_be/data/MongoDBConnection.java`
- `app/src/main/java/com/example/prm_be/data/MongoDBRepo.java`

---

## ⚙️ Cấu Hình

### 1. MongoDB Atlas Setup

1. Tạo MongoDB Atlas cluster tại [mongodb.com/cloud/atlas](https://www.mongodb.com/cloud/atlas)
2. Lấy Connection String từ MongoDB Atlas:
   - Vào **Network Access** → Thêm IP của bạn (hoặc `0.0.0.0/0` cho development)
   - Vào **Database Access** → Tạo user và password
   - Vào **Clusters** → Click **Connect** → Chọn **Connect your application**
   - Copy connection string (format: `mongodb+srv://username:password@cluster.mongodb.net/`)

### 2. Cấu Hình trong Code

**Cách 1: Sử dụng connection string mặc định**

Chỉnh sửa trong `MongoDBConnection.java`:
```java
private static final String CONNECTION_STRING = "mongodb+srv://YOUR_USERNAME:YOUR_PASSWORD@YOUR_CLUSTER.mongodb.net/?retryWrites=true&w=majority";
private static final String DATABASE_NAME = "prm_salon_booking";
```

**Cách 2: Khởi tạo với connection string tùy chỉnh**

```java
MongoDBRepo mongoRepo = MongoDBRepo.getInstance();
mongoRepo.initialize(
    "mongodb+srv://username:password@cluster.mongodb.net/",
    "prm_salon_booking"
);
```

### 3. Network Security Config

Đã tự động cấu hình trong `AndroidManifest.xml`:
- Cho phép kết nối HTTPS đến MongoDB Atlas
- Tắt cleartext traffic (chỉ HTTPS)

---

## 🚀 Sử Dụng

### Khởi Tạo

```java
// Trong Application class hoặc MainActivity onCreate()
MongoDBRepo mongoRepo = MongoDBRepo.getInstance();
mongoRepo.initialize(connectionString, databaseName);
```

### CRUD Operations

#### User Operations

```java
MongoDBRepo mongoRepo = MongoDBRepo.getInstance();

// Tạo User
User user = new User("uid123", "John Doe", "john@example.com", null);
mongoRepo.createUser(user, new MongoDBRepo.MongoDBCallback<String>() {
    @Override
    public void onSuccess(String userId) {
        Log.d("TAG", "User created: " + userId);
    }
    
    @Override
    public void onFailure(Exception e) {
        Log.e("TAG", "Error: " + e.getMessage());
    }
});

// Lấy User
mongoRepo.getUser("uid123", new MongoDBRepo.MongoDBCallback<User>() {
    @Override
    public void onSuccess(User user) {
        // Sử dụng user data
    }
    
    @Override
    public void onFailure(Exception e) {
        // Xử lý lỗi
    }
});

// Cập nhật User
user.setName("Jane Doe");
mongoRepo.updateUser(user, new MongoDBRepo.MongoDBCallback<Void>() {
    @Override
    public void onSuccess(Void result) {
        Log.d("TAG", "User updated");
    }
    
    @Override
    public void onFailure(Exception e) {
        // Xử lý lỗi
    }
});
```

#### Salon Operations

```java
// Lấy tất cả Salon
mongoRepo.getAllSalons(new MongoDBRepo.MongoDBCallback<List<Salon>>() {
    @Override
    public void onSuccess(List<Salon> salons) {
        // Sử dụng danh sách salon
    }
    
    @Override
    public void onFailure(Exception e) {
        // Xử lý lỗi
    }
});

// Lấy Salon theo ID
mongoRepo.getSalonById("salonId123", new MongoDBRepo.MongoDBCallback<Salon>() {
    @Override
    public void onSuccess(Salon salon) {
        // Sử dụng salon data
    }
    
    @Override
    public void onFailure(Exception e) {
        // Xử lý lỗi
    }
});
```

#### Booking Operations

```java
// Tạo Booking
Booking booking = new Booking(
    null, // id sẽ được tự động tạo
    "userId123",
    "salonId123",
    "serviceId123",
    "stylistId123",
    System.currentTimeMillis(),
    "pending",
    System.currentTimeMillis()
);

mongoRepo.createBooking(booking, new MongoDBRepo.MongoDBCallback<String>() {
    @Override
    public void onSuccess(String bookingId) {
        Log.d("TAG", "Booking created: " + bookingId);
    }
    
    @Override
    public void onFailure(Exception e) {
        // Xử lý lỗi
    }
});

// Lấy Booking của User
mongoRepo.getUserBookings("userId123", new MongoDBRepo.MongoDBCallback<List<Booking>>() {
    @Override
    public void onSuccess(List<Booking> bookings) {
        // Hiển thị danh sách booking
    }
    
    @Override
    public void onFailure(Exception e) {
        // Xử lý lỗi
    }
});

// Lấy Booking theo Stylist và ngày (check time slots)
long startOfDay = ...; // Timestamp bắt đầu ngày
long endOfDay = ...; // Timestamp kết thúc ngày

mongoRepo.getBookingsByStylistAndDate(
    "stylistId123",
    "salonId123",
    startOfDay,
    endOfDay,
    new MongoDBRepo.MongoDBCallback<List<Booking>>() {
        @Override
        public void onSuccess(List<Booking> bookings) {
            // Kiểm tra time slots còn trống
        }
        
        @Override
        public void onFailure(Exception e) {
            // Xử lý lỗi
        }
    }
);
```

---

## 📚 API Reference

### MongoDBRepo Methods

#### User Methods
- `createUser(User user, MongoDBCallback<String> callback)`
- `getUser(String uid, MongoDBCallback<User> callback)`
- `updateUser(User user, MongoDBCallback<Void> callback)`

#### Salon Methods
- `getAllSalons(MongoDBCallback<List<Salon>> callback)`
- `getSalonById(String salonId, MongoDBCallback<Salon> callback)`
- `createSalon(Salon salon, MongoDBCallback<String> callback)`

#### Service Methods
- `getServicesOfSalon(String salonId, MongoDBCallback<List<Service>> callback)`

#### Stylist Methods
- `getStylistsOfSalon(String salonId, MongoDBCallback<List<Stylist>> callback)`

#### Booking Methods
- `createBooking(Booking booking, MongoDBCallback<String> callback)`
- `getUserBookings(String userId, MongoDBCallback<List<Booking>> callback)`
- `getBookingsByStylistAndDate(String stylistId, String salonId, long startTimestamp, long endTimestamp, MongoDBCallback<List<Booking>> callback)`
- `updateBookingStatus(String bookingId, String status, MongoDBCallback<Void> callback)`

### MongoDBConnection Methods

- `getInstance()`: Lấy singleton instance
- `connect(String connectionString, String databaseName)`: Khởi tạo kết nối
- `connect()`: Khởi tạo với connection string mặc định
- `getDatabase()`: Lấy MongoDatabase instance
- `isConnected()`: Kiểm tra trạng thái kết nối
- `close()`: Đóng kết nối

---

## ⚠️ Lưu Ý

1. **Thread Safety**: Tất cả operations đều được thực hiện async trên background thread
2. **Error Handling**: Luôn implement `onFailure()` trong callback
3. **Connection Management**: Không cần đóng connection thủ công trong lifecycle của app
4. **Network**: Cần internet connection để kết nối MongoDB Atlas
5. **Security**: Không commit connection string chứa password vào git

---

## 🔧 Troubleshooting

### Lỗi: "MongoDB not connected"
- Đảm bảo đã gọi `initialize()` trước khi sử dụng
- Kiểm tra connection string có đúng không

### Lỗi: "Network error"
- Kiểm tra internet connection
- Kiểm tra IP whitelist trong MongoDB Atlas
- Kiểm tra network security config

### Lỗi: "Authentication failed"
- Kiểm tra username/password trong connection string
- Đảm bảo database user có quyền đọc/ghi

---

## 📝 Collections Structure

### users
```json
{
  "_id": ObjectId,
  "uid": "string",
  "name": "string",
  "email": "string",
  "avatarUrl": "string"
}
```

### salons
```json
{
  "_id": ObjectId,
  "name": "string",
  "address": "string",
  "imageUrl": "string"
}
```

### services
```json
{
  "_id": ObjectId,
  "salonId": "string",
  "name": "string",
  "price": long
}
```

### stylists
```json
{
  "_id": ObjectId,
  "salonId": "string",
  "name": "string",
  "imageUrl": "string",
  "specialization": "string"
}
```

### bookings
```json
{
  "_id": ObjectId,
  "userId": "string",
  "salonId": "string",
  "serviceId": "string",
  "stylistId": "string",
  "timestamp": long,
  "status": "string",
  "createdAt": long
}
```

---

**Lưu ý**: Tài liệu này được tạo tự động. Cập nhật khi có thay đổi trong code.


