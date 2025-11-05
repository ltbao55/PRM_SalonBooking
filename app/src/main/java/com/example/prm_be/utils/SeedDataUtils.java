package com.example.prm_be.utils;

import android.util.Log;

import com.example.prm_be.data.FirebaseRepo;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class để tự động seed data vào Firestore khi app khởi động
 */
public class SeedDataUtils {
    private static final String TAG = "SeedDataUtils";

    /**
     * Tự động seed data nếu chưa có
     */
    public static void autoSeedData() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        
        // Kiểm tra xem đã seed chưa bằng cách check collection salons
        firestore.collection("salons").limit(1).get()
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    Log.d(TAG, "No salons found, starting seed data...");
                    seedAllData();
                } else {
                    Log.d(TAG, "Data already exists, skipping seed");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error checking existing data: " + e.getMessage());
            });
    }

    /**
     * Force seed data - luôn seed data mới (ghi đè lên data cũ)
     */
    public static void forceSeedData() {
        Log.d(TAG, "Force seeding data (will overwrite existing data)...");
        seedAllData();
    }

    private static void seedAllData() {
        Log.d(TAG, "Seeding data...");
        seedSalonsAndStylists();
    }

    private static void seedSalonsAndStylists() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        
        // Salon 1: Golden Hair Studio
        Map<String, Object> salon1 = new HashMap<>();
        salon1.put("name", "Golden Hair Studio");
        salon1.put("address", "123 Nguyễn Huệ, Quận 1, TP.HCM");
        salon1.put("phone", "0901234567");
        salon1.put("imageUrl", "https://images.unsplash.com/photo-1596462502278-27bfad6e9e67?w=800&q=80&auto=format&fit=crop");
        salon1.put("description", "Salon cao cấp với đội ngũ stylist chuyên nghiệp, chuyên về cắt tóc và nhuộm màu");
        salon1.put("rating", 4.8);
        
        firestore.collection("salons").document("salon1").set(salon1)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Salon 1 created");
                
                // Stylists cho salon 1
                createStylist(firestore, "salon1", "stylist1", "Nguyễn Minh Tuấn", "Cắt tóc nam cao cấp");
                createStylist(firestore, "salon1", "stylist2", "Trần Thị Lan Anh", "Cắt tóc nữ thời trang");
                createStylist(firestore, "salon1", "stylist3", "Lê Văn Đức", "Nhuộm tóc chuyên nghiệp");
                
                // Services cho salon 1
                createService(firestore, "salon1", "service1", "Cắt tóc nam", 150000, 30);
                createService(firestore, "salon1", "service2", "Cắt tóc nữ", 200000, 45);
                createService(firestore, "salon1", "service3", "Nhuộm tóc toàn đầu", 500000, 120);
                createService(firestore, "salon1", "service4", "Nhuộm tóc highlights", 800000, 180);
                createService(firestore, "salon1", "service5", "Uốn tóc", 600000, 150);
                createService(firestore, "salon1", "service6", "Gội đầu + Massage", 100000, 30);
                createService(firestore, "salon1", "service7", "Tạo kiểu tóc", 250000, 60);
                
                seedSalon2(firestore);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error creating salon 1", e));
    }

    private static void seedSalon2(FirebaseFirestore firestore) {
        // Salon 2: Premium Beauty Salon
        Map<String, Object> salon2 = new HashMap<>();
        salon2.put("name", "Premium Beauty Salon");
        salon2.put("address", "456 Lê Lợi, Quận 3, TP.HCM");
        salon2.put("phone", "0907654321");
        salon2.put("imageUrl", "https://images.unsplash.com/photo-1560066984-138dadb4c035?w=800&q=80");
        salon2.put("description", "Salon chuyên về làm đẹp và chăm sóc tóc cao cấp, phong cách luxury");
        salon2.put("rating", 4.9);
        
        firestore.collection("salons").document("salon2").set(salon2)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Salon 2 created");
                
                // Stylists cho salon 2
                createStylist(firestore, "salon2", "stylist4", "Phạm Thị Hương", "Cắt tóc nữ thời trang");
                createStylist(firestore, "salon2", "stylist5", "Hoàng Văn Khánh", "Nhuộm & Uốn tóc chuyên nghiệp");
                createStylist(firestore, "salon2", "stylist6", "Võ Thị Mai", "Tạo kiểu tóc cưới hỏi");
                
                // Services cho salon 2
                createService(firestore, "salon2", "service8", "Cắt tóc nữ cao cấp", 300000, 60);
                createService(firestore, "salon2", "service9", "Nhuộm tóc ombre", 1200000, 180);
                createService(firestore, "salon2", "service10", "Uốn tóc sóng nước", 900000, 200);
                createService(firestore, "salon2", "service11", "Phục hồi tóc hư tổn", 400000, 90);
                createService(firestore, "salon2", "service12", "Tạo kiểu tóc cưới", 800000, 150);
                createService(firestore, "salon2", "service13", "Duỗi tóc", 700000, 120);
                createService(firestore, "salon2", "service14", "Gội đầu + Điều trị", 150000, 45);
                
                seedSalon3(firestore);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error creating salon 2", e));
    }

    private static void seedSalon3(FirebaseFirestore firestore) {
        // Salon 3: Elite Hair Design
        Map<String, Object> salon3 = new HashMap<>();
        salon3.put("name", "Elite Hair Design");
        salon3.put("address", "789 Điện Biên Phủ, Quận Bình Thạnh, TP.HCM");
        salon3.put("phone", "0909876543");
        salon3.put("imageUrl", "https://images.unsplash.com/photo-1516975080664-ed2fc6a32937?w=800&q=80");
        salon3.put("description", "Salon thiết kế tóc chuyên nghiệp, đội ngũ stylist giàu kinh nghiệm");
        salon3.put("rating", 4.7);
        
        firestore.collection("salons").document("salon3").set(salon3)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Salon 3 created");
                
                // Stylists cho salon 3
                createStylist(firestore, "salon3", "stylist7", "Nguyễn Thị Thanh Hương", "Cắt tóc nữ hiện đại");
                createStylist(firestore, "salon3", "stylist8", "Đặng Văn Hùng", "Cắt tóc nam kiểu Hàn Quốc");
                createStylist(firestore, "salon3", "stylist9", "Bùi Thị Linh", "Nhuộm tóc balayage");
                
                // Services cho salon 3
                createService(firestore, "salon3", "service15", "Cắt tóc nam kiểu Hàn", 200000, 40);
                createService(firestore, "salon3", "service16", "Cắt tóc nữ layer", 250000, 50);
                createService(firestore, "salon3", "service17", "Nhuộm tóc balayage", 1500000, 200);
                createService(firestore, "salon3", "service18", "Nhuộm tóc babylights", 1200000, 180);
                createService(firestore, "salon3", "service19", "Uốn tóc perm", 800000, 150);
                createService(firestore, "salon3", "service20", "Tẩy tóc", 600000, 120);
                createService(firestore, "salon3", "service21", "Gội đầu + Điều trị dưỡng", 120000, 40);
                
                seedSalon4(firestore);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error creating salon 3", e));
    }

    private static void seedSalon4(FirebaseFirestore firestore) {
        // Salon 4: Royal Hair Spa
        Map<String, Object> salon4 = new HashMap<>();
        salon4.put("name", "Royal Hair Spa");
        salon4.put("address", "321 Nguyễn Trãi, Quận 5, TP.HCM");
        salon4.put("phone", "0905556667");
        salon4.put("imageUrl", "https://images.unsplash.com/photo-1517838277536-f5f99be501b0?w=800&q=80&auto=format&fit=crop");
        salon4.put("description", "Spa chăm sóc tóc và làm đẹp cao cấp, không gian sang trọng");
        salon4.put("rating", 4.9);
        
        firestore.collection("salons").document("salon4").set(salon4)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Salon 4 created");
                
                // Stylists cho salon 4
                createStylist(firestore, "salon4", "stylist10", "Lương Thị Kim Oanh", "Chăm sóc tóc spa");
                createStylist(firestore, "salon4", "stylist11", "Trịnh Văn Long", "Cắt tóc nam classic");
                createStylist(firestore, "salon4", "stylist12", "Đỗ Thị Hồng Nhung", "Nhuộm tóc màu tự nhiên");
                
                // Services cho salon 4
                createService(firestore, "salon4", "service22", "Cắt tóc nam classic", 180000, 35);
                createService(firestore, "salon4", "service23", "Cắt tóc nữ", 280000, 55);
                createService(firestore, "salon4", "service24", "Nhuộm tóc màu tự nhiên", 600000, 120);
                createService(firestore, "salon4", "service25", "Spa chăm sóc tóc", 500000, 90);
                createService(firestore, "salon4", "service26", "Điều trị tóc hư tổn", 800000, 120);
                createService(firestore, "salon4", "service27", "Uốn tóc tự nhiên", 750000, 140);
                createService(firestore, "salon4", "service28", "Gội đầu + Massage thư giãn", 200000, 50);
                
                seedSalon5(firestore);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error creating salon 4", e));
    }

    private static void seedSalon5(FirebaseFirestore firestore) {
        // Salon 5: Modern Hair Studio
        Map<String, Object> salon5 = new HashMap<>();
        salon5.put("name", "Modern Hair Studio");
        salon5.put("address", "555 Võ Văn Tần, Quận 10, TP.HCM");
        salon5.put("phone", "0908889990");
        salon5.put("imageUrl", "https://images.unsplash.com/photo-1502767089025-6572583495a3?w=800&q=80&auto=format&fit=crop");
        salon5.put("description", "Studio tóc hiện đại với phong cách trẻ trung, năng động");
        salon5.put("rating", 4.6);
        
        firestore.collection("salons").document("salon5").set(salon5)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Salon 5 created");
                
                // Stylists cho salon 5
                createStylist(firestore, "salon5", "stylist13", "Nguyễn Thị Minh Thư", "Cắt tóc nữ trendy");
                createStylist(firestore, "salon5", "stylist14", "Phan Văn Quang", "Cắt tóc nam undercut");
                createStylist(firestore, "salon5", "stylist15", "Lê Thị Bích Ngọc", "Nhuộm tóc màu sáng");
                
                // Services cho salon 5
                createService(firestore, "salon5", "service29", "Cắt tóc nam undercut", 220000, 45);
                createService(firestore, "salon5", "service30", "Cắt tóc nữ trendy", 270000, 50);
                createService(firestore, "salon5", "service31", "Nhuộm tóc màu sáng", 900000, 150);
                createService(firestore, "salon5", "service32", "Nhuộm tóc màu pastel", 1100000, 180);
                createService(firestore, "salon5", "service33", "Uốn tóc lọn", 650000, 130);
                createService(firestore, "salon5", "service34", "Tạo kiểu tóc event", 350000, 70);
                createService(firestore, "salon5", "service35", "Gội đầu + Tạo kiểu", 150000, 40);
                
                // Sau khi seed xong, tạo staff accounts (delay để đảm bảo stylists đã được tạo)
                new android.os.Handler().postDelayed(() -> {
                    createStaffAccounts();
                }, 2000);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error creating salon 5", e));
    }

    private static void createStylist(FirebaseFirestore firestore, String salonId, 
                                     String stylistId, String name, String specialization) {
        Map<String, Object> stylist = new HashMap<>();
        stylist.put("name", name);
        stylist.put("salonId", salonId);
        stylist.put("imageUrl", "");
        stylist.put("specialization", specialization);
        
        firestore.collection("salons").document(salonId)
            .collection("stylists").document(stylistId).set(stylist)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "Stylist created: " + name))
            .addOnFailureListener(e -> Log.e(TAG, "Error creating stylist: " + name, e));
    }

    private static void createService(FirebaseFirestore firestore, String salonId,
                                     String serviceId, String name, long price, int duration) {
        Map<String, Object> service = new HashMap<>();
        service.put("name", name);
        service.put("price", price);
        service.put("duration", duration);
        service.put("description", "Dịch vụ " + name);
        
        firestore.collection("salons").document(salonId)
            .collection("services").document(serviceId).set(service)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "Service created: " + name))
            .addOnFailureListener(e -> Log.e(TAG, "Error creating service: " + name, e));
    }

    private static void createStaffAccounts() {
        FirebaseRepo repo = FirebaseRepo.getInstance();
        
        // Danh sách stylist và thông tin account (tên đầy đủ)
        String[][] stylists = {
            {"stylist1", "Nguyễn Minh Tuấn", "nguyenminhtuan@lux.com"},
            {"stylist2", "Trần Thị Lan Anh", "tranthilananh@lux.com"},
            {"stylist3", "Lê Văn Đức", "levanduc@lux.com"},
            {"stylist4", "Phạm Thị Hương", "phamthihuong@lux.com"},
            {"stylist5", "Hoàng Văn Khánh", "hoangvankhanh@lux.com"},
            {"stylist6", "Võ Thị Mai", "vothimai@lux.com"},
            {"stylist7", "Nguyễn Thị Thanh Hương", "nguyenthithanhhuong@lux.com"},
            {"stylist8", "Đặng Văn Hùng", "dangvanhung@lux.com"},
            {"stylist9", "Bùi Thị Linh", "buithilinh@lux.com"},
            {"stylist10", "Lương Thị Kim Oanh", "luongthikimoanh@lux.com"},
            {"stylist11", "Trịnh Văn Long", "trinhvanlong@lux.com"},
            {"stylist12", "Đỗ Thị Hồng Nhung", "dothihongnhung@lux.com"},
            {"stylist13", "Nguyễn Thị Minh Thư", "nguyenthiminhthu@lux.com"},
            {"stylist14", "Phan Văn Quang", "phanvanquang@lux.com"},
            {"stylist15", "Lê Thị Bích Ngọc", "lethibichngoc@lux.com"}
        };
        
        createStaffAccountRecursive(repo, stylists, 0);
    }

    private static void createStaffAccountRecursive(FirebaseRepo repo, String[][] stylists, int index) {
        if (index >= stylists.length) {
            Log.d(TAG, "All staff accounts processed");
            return;
        }
        
        String stylistId = stylists[index][0];
        String name = stylists[index][1];
        String email = stylists[index][2];
        String password = "123456";
        
        // Tạo account
        repo.register(email, password, name, new FirebaseRepo.FirebaseCallback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Set role và stylistId
                repo.updateUserRole(user.getUid(), "staff", new FirebaseRepo.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        repo.updateUserStylistId(user.getUid(), stylistId, new FirebaseRepo.FirebaseCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                Log.d(TAG, "Staff account created: " + email + " (stylistId: " + stylistId + ")");
                                repo.logout(); // Logout để không giữ session
                                createStaffAccountRecursive(repo, stylists, index + 1);
                            }
                            
                            @Override
                            public void onFailure(Exception e) {
                                Log.w(TAG, "Failed to set stylistId for " + email, e);
                                repo.logout();
                                createStaffAccountRecursive(repo, stylists, index + 1);
                            }
                        });
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to set role for " + email, e);
                        repo.logout();
                        createStaffAccountRecursive(repo, stylists, index + 1);
                    }
                });
            }
            
            @Override
            public void onFailure(Exception e) {
                String errorMsg = e != null && e.getMessage() != null ? e.getMessage() : "unknown";
                if (errorMsg.contains("email-already-in-use")) {
                    Log.d(TAG, "Account already exists: " + email + " - skipping");
                } else {
                    Log.e(TAG, "Failed to create account for " + email, e);
                }
                createStaffAccountRecursive(repo, stylists, index + 1);
            }
        });
    }
}
