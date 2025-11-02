package com.example.prm_be.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm_be.MainActivity;
import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.ui.home.HomeActivity;

/**
 * Splash Screen - Màn hình đầu tiên khi mở app
 * Kiểm tra trạng thái đăng nhập và điều hướng phù hợp
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Kiểm tra trạng thái đăng nhập
        FirebaseRepo firebaseRepo = FirebaseRepo.getInstance();
        
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (firebaseRepo.isUserLoggedIn()) {
                    // Đã đăng nhập -> Chuyển đến HomeActivity
                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    // Chưa đăng nhập -> Chuyển đến LoginActivity
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                finish(); // Đóng SplashActivity để không quay lại được
            }
        }, SPLASH_DELAY);
    }
}


