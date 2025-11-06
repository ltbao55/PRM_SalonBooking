package com.example.prm_be;

import android.app.Application;

import com.example.prm_be.utils.SeedDataUtils;
import com.example.prm_be.utils.DataCheckUtils;
import com.google.firebase.FirebaseApp;

/**
 * Application class để khởi tạo Firebase và tự động seed data
 */
public class    PRMApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Khởi tạo Firebase (Google Services plugin sẽ tự động làm việc này nếu có google-services.json,
        // nhưng thêm code này để đảm bảo)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }
        
        // Tự động seed data nếu chưa có
        // Delay một chút để đảm bảo Firebase đã sẵn sàng
        new android.os.Handler().postDelayed(() -> {
            SeedDataUtils.autoSeedData();
            
            // Kiểm tra data sau khi seed (delay thêm để đảm bảo seed đã hoàn thành)
            new android.os.Handler().postDelayed(() -> {
                DataCheckUtils.checkSeededData();
            }, 5000); // Check sau 5 giây
        }, 2000); // Delay 2 giây
    }
}

