package com.example.prm_be;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.ui.auth.LoginActivity;

/**
 * MainActivity - Entry point của app
 * Chức năng: Kiểm tra đăng nhập và điều hướng đến màn hình phù hợp
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Kiểm tra đăng nhập - nếu chưa đăng nhập thì chuyển đến LoginActivity
        FirebaseRepo firebaseRepo = FirebaseRepo.getInstance();
        if (!firebaseRepo.isUserLoggedIn()) {
            // Chưa đăng nhập -> Chuyển đến LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        // Đã đăng nhập -> Hiển thị màn hình chính
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}