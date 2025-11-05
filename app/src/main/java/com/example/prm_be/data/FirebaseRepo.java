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
import com.google.firebase.auth.EmailAuthProvider;
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
                                    // User chưa có trong Firestore, tạo mới với thông tin từ Auth
                                    String name = firebaseUser.getDisplayName() != null 
                                            ? firebaseUser.getDisplayName() 
                                            : (firebaseUser.getEmail() != null ? firebaseUser.getEmail().split("@")[0] : "User");
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
    
    /**
     * Đổi mật khẩu cho user hiện tại
     * @param email Email của user
     * @param currentPassword Mật khẩu hiện tại
     * @param newPassword Mật khẩu mới
     * @param callback Callback để xử lý kết quả
     */
    public void changePassword(String email, String currentPassword, String newPassword, FirebaseCallback<Void> callback) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        // Reauthenticate với mật khẩu hiện tại
        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
        firebaseUser.reauthenticate(credential)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Reauthentication thành công, đổi mật khẩu
                        firebaseUser.updatePassword(newPassword)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> updateTask) {
                                    if (updateTask.isSuccessful()) {
                                        Log.d(TAG, "Password updated successfully");
                                        callback.onSuccess(null);
                                    } else {
                                        Log.e(TAG, "Error updating password", updateTask.getException());
                                        callback.onFailure(updateTask.getException());
                                    }
                                }
                            });
                    } else {
                        Log.e(TAG, "Error reauthenticating", task.getException());
                        callback.onFailure(task.getException());
                    }
                }
            });
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
        userMap.put("status", user.getStatus() != null ? user.getStatus() : "active");
        if (user.getStylistId() != null) {
            userMap.put("stylistId", user.getStylistId());
        }
        
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
        if (user.getStylistId() != null) updates.put("stylistId", user.getStylistId());
        
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
     * Cập nhật stylistId cho User (dùng khi tạo staff account từ stylist)
     */
    public void updateUserStylistId(@NonNull String uid, @NonNull String stylistId, FirebaseCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("stylistId", stylistId);

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

    /**
     * Tìm stylist theo ID khi không biết salonId (duyệt qua tất cả salon)
     */
    public void getStylistById(String stylistId, FirebaseCallback<Stylist> callback) {
        getAllSalons(new FirebaseCallback<List<Salon>>() {
            @Override
            public void onSuccess(List<Salon> salons) {
                if (salons == null || salons.isEmpty()) {
                    callback.onFailure(new Exception("No salons found"));
                    return;
                }

                final boolean[] found = {false};
                final int total = salons.size();
                final int[] done = {0};

                for (Salon salon : salons) {
                    firestore.collection(COLLECTION_SALONS)
                            .document(salon.getId())
                            .collection(SUBCOLLECTION_STYLISTS)
                            .document(stylistId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                done[0]++;
                                if (documentSnapshot.exists() && !found[0]) {
                                    Stylist stylist = documentSnapshot.toObject(Stylist.class);
                                    if (stylist != null) {
                                        stylist.setId(documentSnapshot.getId());
                                        stylist.setSalonId(salon.getId());
                                        found[0] = true;
                                        callback.onSuccess(stylist);
                                    }
                                }
                                if (done[0] == total && !found[0]) {
                                    callback.onFailure(new Exception("Stylist not found"));
                                }
                            })
                            .addOnFailureListener(e -> {
                                done[0]++;
                                if (done[0] == total && !found[0]) {
                                    callback.onFailure(e);
                                }
                            });
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Lấy tất cả stylist từ tất cả salon (dùng cho DevTools)
     */
    public void getAllStylists(FirebaseCallback<List<Stylist>> callback) {
        getAllSalons(new FirebaseCallback<List<Salon>>() {
            @Override
            public void onSuccess(List<Salon> salons) {
                final List<Stylist> allStylists = new ArrayList<>();
                final int[] completed = {0};
                final int totalSalons = salons.size();
                
                if (totalSalons == 0) {
                    callback.onSuccess(allStylists);
                    return;
                }
                
                for (Salon salon : salons) {
                    getStylistsOfSalon(salon.getId(), new FirebaseCallback<List<Stylist>>() {
                        @Override
                        public void onSuccess(List<Stylist> stylists) {
                            allStylists.addAll(stylists);
                            completed[0]++;
                            if (completed[0] == totalSalons) {
                                callback.onSuccess(allStylists);
                            }
                        }
                        
                        @Override
                        public void onFailure(Exception e) {
                            completed[0]++;
                            if (completed[0] == totalSalons) {
                                callback.onSuccess(allStylists);
                            }
                        }
                    });
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Lấy tất cả dịch vụ từ tất cả salon (cho admin)
     */
    public void getAllServices(FirebaseCallback<List<Service>> callback) {
        getAllSalons(new FirebaseCallback<List<Salon>>() {
            @Override
            public void onSuccess(List<Salon> salons) {
                final List<Service> allServices = new ArrayList<>();
                final int[] completed = {0};
                final int totalSalons = salons.size();
                
                if (totalSalons == 0) {
                    callback.onSuccess(allServices);
                    return;
                }
                
                for (Salon salon : salons) {
                    getServicesOfSalon(salon.getId(), new FirebaseCallback<List<Service>>() {
                        @Override
                        public void onSuccess(List<Service> services) {
                            // Add salonId to each service for reference
                            for (Service service : services) {
                                allServices.add(service);
                            }
                            completed[0]++;
                            if (completed[0] == totalSalons) {
                                callback.onSuccess(allServices);
                            }
                        }
                        
                        @Override
                        public void onFailure(Exception e) {
                            completed[0]++;
                            if (completed[0] == totalSalons) {
                                callback.onSuccess(allServices);
                            }
                        }
                    });
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Tạo dịch vụ mới cho salon
     */
    public void createService(String salonId, Service service, FirebaseCallback<Void> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put("name", service.getName());
        serviceMap.put("price", service.getPrice());
        serviceMap.put("duration", service.getDurationInMinutes());
        serviceMap.put("description", service.getName()); // Default description
        
        firestore.collection(COLLECTION_SALONS)
            .document(salonId)
            .collection(SUBCOLLECTION_SERVICES)
            .add(serviceMap)
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(callback::onFailure);
    }

    /**
     * Cập nhật dịch vụ
     */
    public void updateService(String salonId, String serviceId, Service service, FirebaseCallback<Void> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", service.getName());
        updates.put("price", service.getPrice());
        updates.put("duration", service.getDurationInMinutes());
        
        firestore.collection(COLLECTION_SALONS)
            .document(salonId)
            .collection(SUBCOLLECTION_SERVICES)
            .document(serviceId)
            .update(updates)
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(callback::onFailure);
    }

    /**
     * Xóa dịch vụ
     */
    public void deleteService(String salonId, String serviceId, FirebaseCallback<Void> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        firestore.collection(COLLECTION_SALONS)
            .document(salonId)
            .collection(SUBCOLLECTION_SERVICES)
            .document(serviceId)
            .delete()
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(callback::onFailure);
    }

    /**
     * Tạo salon mới
     */
    public void createSalon(Salon salon, FirebaseCallback<String> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        Map<String, Object> salonMap = new HashMap<>();
        salonMap.put("name", salon.getName());
        salonMap.put("address", salon.getAddress());
        salonMap.put("phone", salon.getPhone() != null ? salon.getPhone() : "");
        salonMap.put("imageUrl", salon.getImageUrl() != null ? salon.getImageUrl() : "");
        salonMap.put("description", salon.getDescription() != null ? salon.getDescription() : "");
        salonMap.put("rating", salon.getRating());
        
        firestore.collection(COLLECTION_SALONS)
            .add(salonMap)
            .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
            .addOnFailureListener(callback::onFailure);
    }

    /**
     * Cập nhật salon
     */
    public void updateSalon(String salonId, Salon salon, FirebaseCallback<Void> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", salon.getName());
        updates.put("address", salon.getAddress());
        updates.put("phone", salon.getPhone() != null ? salon.getPhone() : "");
        updates.put("imageUrl", salon.getImageUrl() != null ? salon.getImageUrl() : "");
        updates.put("description", salon.getDescription() != null ? salon.getDescription() : "");
        updates.put("rating", salon.getRating());
        
        firestore.collection(COLLECTION_SALONS)
            .document(salonId)
            .update(updates)
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(callback::onFailure);
    }

    /**
     * Xóa salon
     */
    public void deleteSalon(String salonId, FirebaseCallback<Void> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        firestore.collection(COLLECTION_SALONS)
            .document(salonId)
            .delete()
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(callback::onFailure);
    }

    // ========== WORKING HOURS METHODS ==========
    
    private static final String COLLECTION_WORKING_HOURS = "workingHours";

    /**
     * Lấy working hours của salon
     */
    public void getWorkingHours(String salonId, FirebaseCallback<com.example.prm_be.data.models.WorkingHours> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        firestore.collection(COLLECTION_WORKING_HOURS)
            .whereEqualTo("salonId", salonId)
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    com.example.prm_be.data.models.WorkingHours hours = querySnapshot.getDocuments().get(0).toObject(com.example.prm_be.data.models.WorkingHours.class);
                    callback.onSuccess(hours);
                } else {
                    // Return default working hours
                    callback.onSuccess(new com.example.prm_be.data.models.WorkingHours(salonId, "09:00", "18:00", 
                        java.util.Arrays.asList("MON", "TUE", "WED", "THU", "FRI", "SAT"), 30));
                }
            })
            .addOnFailureListener(callback::onFailure);
    }

    /**
     * Lưu/cập nhật working hours
     */
    public void saveWorkingHours(com.example.prm_be.data.models.WorkingHours workingHours, FirebaseCallback<Void> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        // Check if exists
        firestore.collection(COLLECTION_WORKING_HOURS)
            .whereEqualTo("salonId", workingHours.getSalonId())
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                Map<String, Object> hoursMap = new HashMap<>();
                hoursMap.put("salonId", workingHours.getSalonId());
                hoursMap.put("openTime", workingHours.getOpenTime());
                hoursMap.put("closeTime", workingHours.getCloseTime());
                hoursMap.put("daysOfWeek", workingHours.getDaysOfWeek());
                hoursMap.put("slotDuration", workingHours.getSlotDuration());
                
                if (!querySnapshot.isEmpty()) {
                    // Update existing
                    String docId = querySnapshot.getDocuments().get(0).getId();
                    firestore.collection(COLLECTION_WORKING_HOURS)
                        .document(docId)
                        .set(hoursMap)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                        .addOnFailureListener(callback::onFailure);
                } else {
                    // Create new
                    firestore.collection(COLLECTION_WORKING_HOURS)
                        .add(hoursMap)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                        .addOnFailureListener(callback::onFailure);
                }
            })
            .addOnFailureListener(callback::onFailure);
    }

    // ========== AVAILABILITY METHODS ==========
    
    private static final String COLLECTION_AVAILABILITY = "availability";

    /**
     * Lấy availability của staff cho một ngày
     */
    public void getStaffAvailability(String staffId, long dateTimestamp, FirebaseCallback<com.example.prm_be.data.models.Availability> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        firestore.collection(COLLECTION_AVAILABILITY)
            .whereEqualTo("staffId", staffId)
            .whereEqualTo("date", dateTimestamp)
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    com.example.prm_be.data.models.Availability availability = querySnapshot.getDocuments().get(0).toObject(com.example.prm_be.data.models.Availability.class);
                    if (availability != null) {
                        availability.setId(querySnapshot.getDocuments().get(0).getId());
                    }
                    callback.onSuccess(availability);
                } else {
                    // Return empty availability (all slots available)
                    callback.onSuccess(null);
                }
            })
            .addOnFailureListener(callback::onFailure);
    }

    /**
     * Lấy availability của staff cho một khoảng ngày
     */
    public void getStaffAvailabilityRange(String staffId, long startDate, long endDate, FirebaseCallback<List<com.example.prm_be.data.models.Availability>> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        firestore.collection(COLLECTION_AVAILABILITY)
            .whereEqualTo("staffId", staffId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<com.example.prm_be.data.models.Availability> availabilities = new ArrayList<>();
                for (QueryDocumentSnapshot document : querySnapshot) {
                    com.example.prm_be.data.models.Availability availability = document.toObject(com.example.prm_be.data.models.Availability.class);
                    availability.setId(document.getId());
                    availabilities.add(availability);
                }
                callback.onSuccess(availabilities);
            })
            .addOnFailureListener(callback::onFailure);
    }

    /**
     * Lưu/cập nhật availability
     */
    public void saveAvailability(com.example.prm_be.data.models.Availability availability, FirebaseCallback<Void> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        Map<String, Object> availabilityMap = new HashMap<>();
        availabilityMap.put("staffId", availability.getStaffId());
        availabilityMap.put("salonId", availability.getSalonId());
        availabilityMap.put("date", availability.getDate());
        availabilityMap.put("unavailableSlots", availability.getUnavailableSlots());
        if (availability.getReason() != null) {
            availabilityMap.put("reason", availability.getReason());
        }
        
        // Check if exists
        firestore.collection(COLLECTION_AVAILABILITY)
            .whereEqualTo("staffId", availability.getStaffId())
            .whereEqualTo("date", availability.getDate())
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    // Update existing
                    String docId = querySnapshot.getDocuments().get(0).getId();
                    firestore.collection(COLLECTION_AVAILABILITY)
                        .document(docId)
                        .set(availabilityMap)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                        .addOnFailureListener(callback::onFailure);
                } else {
                    // Create new
                    firestore.collection(COLLECTION_AVAILABILITY)
                        .add(availabilityMap)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                        .addOnFailureListener(callback::onFailure);
                }
            })
            .addOnFailureListener(callback::onFailure);
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
        bookingMap.put("serviceId", booking.getServiceId()); // Giữ lại cho tương thích
        bookingMap.put("serviceIds", booking.getServiceIds() != null ? booking.getServiceIds() : new ArrayList<>());
        bookingMap.put("stylistId", booking.getStylistId() != null ? booking.getStylistId() : "");
        bookingMap.put("timestamp", booking.getTimestamp());
        bookingMap.put("endTime", booking.getEndTime());
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
     * Normalize booking data để đảm bảo serviceIds và endTime được set đúng
     */
    private void normalizeBooking(Booking booking) {
        if (booking == null) return;
        
        // Ensure serviceIds is set (for backward compatibility)
        if (booking.getServiceIds().isEmpty() && booking.getServiceId() != null) {
            List<String> serviceIds = new ArrayList<>();
            serviceIds.add(booking.getServiceId());
            booking.setServiceIds(serviceIds);
        }
        
        // Ensure endTime is set (default to timestamp + 60 minutes if not set)
        if (booking.getEndTime() == booking.getTimestamp() && booking.getTimestamp() > 0) {
            booking.setEndTime(booking.getTimestamp() + (60 * 60 * 1000L));
        }
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
                            normalizeBooking(booking);
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
                        normalizeBooking(booking);
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
                            normalizeBooking(booking);
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
     * Xóa booking vĩnh viễn (chỉ admin mới có quyền)
     * @param bookingId ID của booking cần xóa
     * @param callback Callback để xử lý kết quả
     */
    public void deleteBooking(String bookingId, FirebaseCallback<Void> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        firestore.collection(COLLECTION_BOOKINGS)
                .document(bookingId)
                .delete()
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
                            normalizeBooking(booking);
                            
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
                        normalizeBooking(booking);
                        
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
     * Lấy tất cả bookings (cho admin)
     * @param callback Callback trả về danh sách booking
     */
    public void getAllBookings(FirebaseCallback<List<Booking>> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        firestore.collection(COLLECTION_BOOKINGS)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Booking> bookings = new ArrayList<>();
                for (QueryDocumentSnapshot document : querySnapshot) {
                    Booking booking = document.toObject(Booking.class);
                    booking.setId(document.getId());
                    normalizeBooking(booking);
                    bookings.add(booking);
                }
                // Sắp xếp theo timestamp giảm dần (mới nhất trước)
                java.util.Collections.sort(bookings, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                callback.onSuccess(bookings);
            })
            .addOnFailureListener(callback::onFailure);
    }

    /**
     * Cập nhật trạng thái booking
     * @param bookingId ID của booking
     * @param status Trạng thái mới (confirmed, pending, cancelled, completed)
     * @param callback Callback
     */
    public void updateBookingStatus(String bookingId, String status, FirebaseCallback<Void> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        firestore.collection(COLLECTION_BOOKINGS)
            .document(bookingId)
            .update("status", status)
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(callback::onFailure);
    }

    /**
     * Lấy tất cả users (cho admin)
     * @param callback Callback trả về danh sách users
     */
    public void getAllUsers(FirebaseCallback<List<User>> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        firestore.collection(COLLECTION_USERS)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<User> users = new ArrayList<>();
                for (QueryDocumentSnapshot document : querySnapshot) {
                    User user = document.toObject(User.class);
                    user.setUid(document.getId());
                    users.add(user);
                }
                callback.onSuccess(users);
            })
            .addOnFailureListener(callback::onFailure);
    }

    /**
     * Cập nhật status của user (active/disabled)
     * @param userId ID của user
     * @param status Trạng thái mới
     * @param callback Callback
     */
    public void updateUserStatus(String userId, String status, FirebaseCallback<Void> callback) {
        if (firestore == null) {
            callback.onFailure(new IllegalStateException("Firestore is not initialized"));
            return;
        }
        
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .update("status", status)
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(callback::onFailure);
    }

    /**
     * Interface callback cho các thao tác Firebase
     */
    public interface FirebaseCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
}

