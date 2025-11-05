package com.example.prm_be.utils;

import android.util.Log;

import com.example.prm_be.data.FirebaseRepo;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Utility class để kiểm tra data đã được seed vào database chưa
 */
public class DataCheckUtils {
    private static final String TAG = "DataCheckUtils";

    /**
     * Kiểm tra và log tất cả data đã được seed
     */
    public static void checkSeededData() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        
        Log.d(TAG, "=== CHECKING SEEDED DATA ===");
        
        // Check Salons
        firestore.collection("salons").get()
            .addOnSuccessListener(querySnapshot -> {
                Log.d(TAG, "SALONS: Found " + querySnapshot.size() + " salons");
                querySnapshot.getDocuments().forEach(doc -> {
                    Log.d(TAG, "  - Salon: " + doc.getId() + " | Name: " + doc.get("name"));
                    
                    // Check Stylists for each salon
                    firestore.collection("salons").document(doc.getId())
                        .collection("stylists").get()
                        .addOnSuccessListener(stylistSnapshot -> {
                            Log.d(TAG, "    Stylists: " + stylistSnapshot.size());
                            stylistSnapshot.getDocuments().forEach(stylistDoc -> {
                                Log.d(TAG, "      - " + stylistDoc.getId() + ": " + stylistDoc.get("name"));
                            });
                        });
                    
                    // Check Services for each salon
                    firestore.collection("salons").document(doc.getId())
                        .collection("services").get()
                        .addOnSuccessListener(serviceSnapshot -> {
                            Log.d(TAG, "    Services: " + serviceSnapshot.size());
                            serviceSnapshot.getDocuments().forEach(serviceDoc -> {
                                Log.d(TAG, "      - " + serviceDoc.getId() + ": " + serviceDoc.get("name") 
                                    + " (" + serviceDoc.get("price") + " VND)");
                            });
                        });
                });
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error checking salons: " + e.getMessage()));
        
        // Check Users with staff role
        firestore.collection("users").whereEqualTo("role", "staff").get()
            .addOnSuccessListener(querySnapshot -> {
                Log.d(TAG, "STAFF ACCOUNTS: Found " + querySnapshot.size() + " staff accounts");
                querySnapshot.getDocuments().forEach(doc -> {
                    String stylistId = (String) doc.get("stylistId");
                    Log.d(TAG, "  - " + doc.getId() + ": " + doc.get("name") 
                        + " | Email: " + doc.get("email") + " | StylistId: " + (stylistId != null ? stylistId : "N/A"));
                });
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error checking staff accounts: " + e.getMessage()));
    }
}

