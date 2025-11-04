package com.example.prm_be.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.prm_be.data.models.Booking;
import com.example.prm_be.data.models.Salon;
import com.example.prm_be.data.models.Service;
import com.example.prm_be.data.models.Stylist;
import com.example.prm_be.data.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton class quản lý tất cả các thao tác với Firebase
 * Đây là "đường ống" dữ liệu cho toàn bộ dự án
 * Tất cả các dev sẽ sử dụng lớp này để tương tác với Firebase
 */
public class FirebaseRepo {
    private static final String TAG = "FirebaseRepo";
    private static FirebaseRepo instance;
    
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private com.google.firebase.storage.FirebaseStorage storage;
    
    // Collection names
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_SALONS = "salons";
    private static final String COLLECTION_BOOKINGS = "bookings";
    
    // Subcollection names
    private static final String SUBCOLLECTION_SERVICES = "services";
    private static final String SUBCOLLECTION_STYLISTS = "stylists";
    
    /**
     * Constructor private để đảm bảo Singleton pattern
     */
    private FirebaseRepo() {
        try {
            auth = FirebaseAuth.getInstance();
            firestore = FirebaseFirestore.getInstance();
            storage = com.google.firebase.storage.FirebaseStorage.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed. Make sure google-services.json is added.", e);
            // Firebase chưa được setup - sẽ trả về null khi gọi methods
            auth = null;
            firestore = null;
            storage = null;
        }
    }
    
    /**
     * Lấy instance duy nhất của FirebaseRepo
     */
    public static synchronized FirebaseRepo getInstance() {
        if (instance == null) {
            instance = new FirebaseRepo();
        }
        return instance;
    }
    
    // ========== AUTHENTICATION METHODS ==========
    
    /**
     * Đăng nhập với email và password
     * @param email Email người dùng
     * @param password Mật khẩu
     * @param callback Callback để xử lý kết quả
     */
    public void login(String email, String password, FirebaseCallback<FirebaseUser> callback) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            callback.onSuccess(user);
                        } else {
                            callback.onFailure(new Exception("User is null"));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                }
            });
    }
    
    /**
     * Đăng ký tài khoản mới
     * @param email Email người dùng
     * @param password Mật khẩu
     * @param name Tên người dùng
     * @param callback Callback để xử lý kết quả
     */
    public void register(String email, String password, String name, FirebaseCallback<FirebaseUser> callback) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Tạo User document trong Firestore
                            User user = new User(firebaseUser.getUid(), name, email, null, "user");
                            createUser(user, new FirebaseCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    callback.onSuccess(firebaseUser);
                                }
                                
                                @Override
                                public void onFailure(Exception e) {
                                    // Vẫn trả về success vì user đã được tạo trong Auth
                                    callback.onSuccess(firebaseUser);
                                }
                            });
                        } else {
                            callback.onFailure(new Exception("User is null"));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                }
            });
    }
    
    /**
     * Đăng nhập với Google
     * @param idToken ID token từ Google Sign-In
     * @param callback Callback để xử lý kết quả
     */
    public void signInWithGoogle(String idToken, FirebaseCallback<FirebaseUser> callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Kiểm tra xem user đã có trong Firestore chưa
                            getUser(firebaseUser.getUid(), new FirebaseCallback<User>() {
                                @Override
                                public void onSuccess(User user) {
                                    // User đã tồn tại trong Firestore
                                    callback.onSuccess(firebaseUser);
                                }
                                
                                @Override
                                public void onFailure(Exception e) {
                                    // User chưa có trong Firestore, tạo mới
                                    String name = firebaseUser.getDisplayName() != null 
                                            ? firebaseUser.getDisplayName() 
                                            : "User";
                                    String email = firebaseUser.getEmail() != null 
                                            ? firebaseUser.getEmail() 
                                            : "";
                                    String avatarUrl = firebaseUser.getPhotoUrl() != null 
                                            ? firebaseUser.getPhotoUrl().toString() 
                                            : null;
                                    
                                    User newUser = new User(firebaseUser.getUid(), name, email, avatarUrl, "user");
                                    createUser(newUser, new FirebaseCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void result) {
                                            callback.onSuccess(firebaseUser);
                                        }
                                        
                                        @Override
                                        public void onFailure(Exception e) {
                                            Log.e(TAG, "Error creating user document", e);
                                            // Vẫn trả về success vì user đã được tạo trong Auth
                                            callback.onSuccess(firebaseUser);
                                        }
                                    });
                                }
                            });
                        } else {
                            callback.onFailure(new Exception("User is null"));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                }
            });
    }
    
    /**
     * Gửi email reset mật khẩu
     * @param email Email người dùng
     * @param callback Callback để xử lý kết quả
     */
    public void sendPasswordResetEmail(String email, FirebaseCallback<Void> callback) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onFailure(task.getException());
                    }
                }
            });
    }
    
    /**
     * Đăng xuất
     */
    public void logout() {
        if (auth != null) {
            auth.signOut();
        }
    }
    
    /**
     * Lấy user hiện tại đã đăng nhập
     */
    public FirebaseUser getCurrentUser() {
        if (auth == null) {
            return null; // Firebase chưa được setup
        }
        return auth.getCurrentUser();
    }
    
    /**
     * Kiểm tra xem có user đang đăng nhập không
     */
    public boolean isUserLoggedIn() {
        if (auth == null) {
            return false; // Firebase chưa được setup
        }
        return auth.getCurrentUser() != null;
    }
    
    // ========== USER METHODS ==========
    
    /**
     * Tạo User document trong Firestore
     */
    public void createUser(User user, FirebaseCallback<Void> callback) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", user.getUid());
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        userMap.put("role", user.getRole() != null ? user.getRole() : "user");
        
        firestore.collection(COLLECTION_USERS)
            .document(user.getUid())
            .set(userMap)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    callback.onSuccess(aVoid);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onFailure(e);
                }
            });
    }
    
    /**
     * Lấy thông tin User từ Firestore
     */
    public void getUser(String uid, FirebaseCallback<User> callback) {
        firestore.collection(COLLECTION_USERS)
            .document(uid)
            .get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setUid(documentSnapshot.getId());
                            callback.onSuccess(user);
                        } else {
                            callback.onFailure(new Exception("Failed to parse user"));
                        }
                    } else {
                        callback.onFailure(new Exception("User not found"));
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onFailure(e);
                }
            });
    }
    
    /**
     * Cập nhật thông tin User
     */
    public void updateUser(User user, FirebaseCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        if (user.getName() != null) updates.put("name", user.getName());
        if (user.getEmail() != null) updates.put("email", user.getEmail());
        if (user.getAvatarUrl() != null) updates.put("avatarUrl", user.getAvatarUrl());
        if (user.getRole() != null) updates.put("role", user.getRole());
        
        firestore.collection(COLLECTION_USERS)
            .document(user.getUid())
            .update(updates)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    callback.onSuccess(aVoid);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onFailure(e);
                }
            });
    }

    /**
     * Cập nhật role cho User (user/staff/admin)
     */
    public void updateUserRole(@NonNull String uid, @NonNull String role, FirebaseCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("role", role);

        firestore.collection(COLLECTION_USERS)
                .document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Upload ảnh đại diện lên Firebase Storage và trả về URL
     */
    public void uploadProfileImage(@NonNull Uri imageUri, @NonNull String uid, FirebaseCallback<String> callback) {
        if (storage == null) {
            callback.onFailure(new IllegalStateException("Firebase Storage is not initialized"));
            return;
        }

        com.google.firebase.storage.StorageReference avatarsRef = storage.getReference()
                .child("avatars/").child(uid + ".jpg");

        avatarsRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> avatarsRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                        .addOnFailureListener(callback::onFailure))
                .addOnFailureListener(callback::onFailure);
    }
    
    // ========== SALON METHODS ==========
    
    /**
     * Lấy tất cả salon
     * @param callback Callback trả về danh sách salon
     */
    public void getAllSalons(FirebaseCallback<List<Salon>> callback) {
        firestore.collection(COLLECTION_SALONS)
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot querySnapshot) {
                    List<Salon> salons = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Salon salon = document.toObject(Salon.class);
                        salon.setId(document.getId());
                        salons.add(salon);
                    }
                    callback.onSuccess(salons);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onFailure(e);
                }
            });
    }
    
    /**
     * Lấy thông tin salon theo ID
     */
    public void getSalonById(String salonId, FirebaseCallback<Salon> callback) {
        firestore.collection(COLLECTION_SALONS)
            .document(salonId)
            .get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        Salon salon = documentSnapshot.toObject(Salon.class);
                        if (salon != null) {
                            salon.setId(documentSnapshot.getId());
                            callback.onSuccess(salon);
                        } else {
                            callback.onFailure(new Exception("Failed to parse salon"));
                        }
                    } else {
                        callback.onFailure(new Exception("Salon not found"));
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onFailure(e);
                }
            });
    }
    
    // ========== SERVICE METHODS ==========
    
    /**
     * Lấy tất cả dịch vụ của một salon
     * @param salonId ID của salon
     * @param callback Callback trả về danh sách dịch vụ
     */
    public void getServicesOfSalon(String salonId, FirebaseCallback<List<Service>> callback) {
        firestore.collection(COLLECTION_SALONS)
            .document(salonId)
            .collection(SUBCOLLECTION_SERVICES)
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot querySnapshot) {
                    List<Service> services = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Service service = document.toObject(Service.class);
                        service.setId(document.getId());
                        services.add(service);
                    }
                    callback.onSuccess(services);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onFailure(e);
                }
            });
    }
    
    // ========== STYLIST METHODS ==========
    
    /**
     * Lấy tất cả stylist của một salon
     */
    public void getStylistsOfSalon(String salonId, FirebaseCallback<List<Stylist>> callback) {
        firestore.collection(COLLECTION_SALONS)
            .document(salonId)
            .collection(SUBCOLLECTION_STYLISTS)
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot querySnapshot) {
                    List<Stylist> stylists = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Stylist stylist = document.toObject(Stylist.class);
                        stylist.setId(document.getId());
                        stylists.add(stylist);
                    }
                    callback.onSuccess(stylists);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onFailure(e);
                }
            });
    }
    
    // ========== BOOKING METHODS ==========
    
    /**
     * Tạo booking mới
     * @param booking Booking object
     * @param callback Callback để xử lý kết quả
     */
    public void createBooking(Booking booking, FirebaseCallback<String> callback) {
        Map<String, Object> bookingMap = new HashMap<>();
        bookingMap.put("userId", booking.getUserId());
        bookingMap.put("salonId", booking.getSalonId());
        bookingMap.put("serviceId", booking.getServiceId());
        bookingMap.put("stylistId", booking.getStylistId() != null ? booking.getStylistId() : "");
        bookingMap.put("timestamp", booking.getTimestamp());
        bookingMap.put("status", booking.getStatus());
        bookingMap.put("createdAt", booking.getCreatedAt());
        
        firestore.collection(COLLECTION_BOOKINGS)
            .add(bookingMap)
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    callback.onSuccess(documentReference.getId());
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onFailure(e);
                }
            });
    }

    /**
     * Lấy booking theo ID
     */
    public void getBookingById(String bookingId, FirebaseCallback<Booking> callback) {
        firestore.collection(COLLECTION_BOOKINGS)
                .document(bookingId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Booking booking = documentSnapshot.toObject(Booking.class);
                        if (booking != null) {
                            booking.setId(documentSnapshot.getId());
                            callback.onSuccess(booking);
                        } else {
                            callback.onFailure(new Exception("Failed to parse booking"));
                        }
                    } else {
                        callback.onFailure(new Exception("Booking not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
    
    /**
     * Lấy tất cả booking của một user
     * @param userId ID của user
     * @param callback Callback trả về danh sách booking
     */
    public void getUserBookings(String userId, FirebaseCallback<List<Booking>> callback) {
        firestore.collection(COLLECTION_BOOKINGS)
            .whereEqualTo("userId", userId)
            // Tránh yêu cầu composite index bằng cách không dùng orderBy trên server
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot querySnapshot) {
                    List<Booking> bookings = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Booking booking = document.toObject(Booking.class);
                        booking.setId(document.getId());
                        bookings.add(booking);
                    }
                    // Sắp xếp client-side theo timestamp giảm dần
                    java.util.Collections.sort(bookings, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    callback.onSuccess(bookings);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onFailure(e);
                }
            });
    }

    /**
     * Lắng nghe realtime danh sách bookings của user
     */
    public ListenerRegistration getUserBookingsListener(String userId, FirebaseCallback<List<Booking>> callback) {
        return firestore.collection(COLLECTION_BOOKINGS)
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error);
                        return;
                    }
                    List<Booking> bookings = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            Booking booking = document.toObject(Booking.class);
                            booking.setId(document.getId());
                            bookings.add(booking);
                        }
                    }
                    java.util.Collections.sort(bookings, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    callback.onSuccess(bookings);
                });
    }

    /**
     * Hủy booking (đổi status thành "cancelled")
     */
    public void cancelBooking(String bookingId, FirebaseCallback<Void> callback) {
        firestore.collection(COLLECTION_BOOKINGS)
                .document(bookingId)
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
    
    /**
     * Lấy tất cả booking của một stylist trong một khoảng thời gian
     * Dùng để check time slots còn trống khi booking
     * @param stylistId ID của stylist (có thể null nếu không chọn stylist)
     * @param salonId ID của salon
     * @param startTimestamp Timestamp bắt đầu của ngày
     * @param endTimestamp Timestamp kết thúc của ngày
     * @param callback Callback trả về danh sách booking
     */
    public void getBookingsByStylistAndDate(
            @Nullable String stylistId, 
            String salonId, 
            long startTimestamp, 
            long endTimestamp, 
            FirebaseCallback<List<Booking>> callback) {
        
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        Query query = firestore.collection(COLLECTION_BOOKINGS)
            .whereEqualTo("salonId", salonId)
            .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
            .whereLessThanOrEqualTo("timestamp", endTimestamp);
        
        // Nếu có stylistId, filter thêm theo stylistId
        // Nếu không có stylistId, lấy tất cả bookings của salon (bao gồm cả bookings không có stylist)
        if (stylistId != null && !stylistId.isEmpty()) {
            query = query.whereEqualTo("stylistId", stylistId);
        }
        // Nếu stylistId null hoặc empty, không filter theo stylist - lấy tất cả
        
        query.get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot querySnapshot) {
                    List<Booking> bookings = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Booking booking = document.toObject(Booking.class);
                        if (booking != null) {
                            booking.setId(document.getId());
                            
                            // Chỉ lấy các booking confirmed hoặc pending (không lấy cancelled)
                            String status = booking.getStatus();
                            if (status != null && ("confirmed".equals(status) || "pending".equals(status))) {
                                // Nếu đã chọn stylist, chỉ lấy bookings của stylist đó
                                // Nếu chưa chọn stylist, lấy tất cả bookings (bao gồm cả bookings không có stylist)
                                if (stylistId != null && !stylistId.isEmpty()) {
                                    // Đã filter trong query rồi, chỉ cần add
                                    bookings.add(booking);
                                } else {
                                    // Chưa chọn stylist: lấy tất cả bookings (có stylist và không có stylist)
                                    bookings.add(booking);
                                }
                            }
                        }
                    }
                    callback.onSuccess(bookings);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error getting bookings by stylist and date", e);
                    callback.onFailure(e);
                }
            });
    }
    
    /**
     * Lấy lịch làm việc của staff trong khoảng thời gian
     * @param staffId ID của staff (stylistId)
     * @param startDate Timestamp bắt đầu (start of week/day)
     * @param endDate Timestamp kết thúc (end of week/day)
     * @param callback Callback trả về danh sách booking
     */
    public void getStaffSchedule(String staffId, long startDate, long endDate, FirebaseCallback<List<Booking>> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        // Query chỉ filter theo stylistId (không cần composite index)
        // Filter timestamp sẽ làm ở client-side để tránh lỗi FAILED_PRECONDITION
        firestore.collection(COLLECTION_BOOKINGS)
            .whereEqualTo("stylistId", staffId)
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot querySnapshot) {
                    List<Booking> bookings = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Booking booking = document.toObject(Booking.class);
                        booking.setId(document.getId());
                        
                        // Filter timestamp ở client-side
                        long timestamp = booking.getTimestamp();
                        if (timestamp >= startDate && timestamp <= endDate) {
                            bookings.add(booking);
                        }
                    }
                    // Sắp xếp theo timestamp tăng dần
                    java.util.Collections.sort(bookings, (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
                    callback.onSuccess(bookings);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onFailure(e);
                }
            });
    }

    /**
     * Interface callback cho các thao tác Firebase
     */
    public interface FirebaseCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
}

