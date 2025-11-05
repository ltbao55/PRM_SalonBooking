# Firestore Security Rules - Recommended

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Salon & Stylists - chỉ đọc khi đã đăng nhập
    match /salons/{salonId} {
      allow read: if request.auth != null;
      match /stylists/{stylistId} {
        allow read: if request.auth != null;
      }
      match /services/{serviceId} {
        allow read: if request.auth != null;
      }
    }
    
    // Users collection
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
      // Admin có thể update role
      allow update: if request.auth != null && 
        (request.auth.uid == userId || 
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin');
    }
    
    // Bookings
    match /bookings/{bookingId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && 
        (resource.data.userId == request.auth.uid ||
         resource.data.stylistId == request.auth.uid ||
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin');
    }
  }
}
```

## Cách áp dụng:
1. Mở Firebase Console → Firestore Database → Rules
2. Copy rules trên vào
3. Click "Publish"
4. Đợi vài giây để rules được áp dụng

## Giải thích:
- `request.auth != null`: Yêu cầu đã đăng nhập
- `request.auth.uid == userId`: Chỉ user đó mới có thể đọc/ghi dữ liệu của mình
- `get(...).data.role == 'admin'`: Admin có quyền cao hơn

