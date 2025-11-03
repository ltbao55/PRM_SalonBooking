package com.example.prm_be;

import android.app.Application;

import com.google.firebase.FirebaseApp;

/**
 * Application class để khởi tạo Firebase
 */
public class PRMApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Khởi tạo Firebase (Google Services plugin sẽ tự động làm việc này nếu có google-services.json,
        // nhưng thêm code này để đảm bảo)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }
    }
}

