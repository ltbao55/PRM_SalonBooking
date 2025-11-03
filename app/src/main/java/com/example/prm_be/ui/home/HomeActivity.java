package com.example.prm_be.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.User;
import com.example.prm_be.ui.auth.LoginActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;

/**
 * Home Activity - Màn hình chính sau khi đăng nhập thành công
 * Đây là màn hình test để kiểm tra đăng nhập thành công
 */
public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome, tvUserInfo;
    private Button btnLogout;
    private View rootView;
    
    private FirebaseRepo firebaseRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_test);

        rootView = findViewById(android.R.id.content);
        
        // Kiểm tra đăng nhập
        firebaseRepo = FirebaseRepo.getInstance();
        if (!firebaseRepo.isUserLoggedIn()) {
            // Chưa đăng nhập -> Chuyển đến LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        initViews();
        loadUserInfo();
        setupClickListeners();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserInfo = findViewById(R.id.tvUserInfo);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadUserInfo() {
        FirebaseUser firebaseUser = firebaseRepo.getCurrentUser();
        if (firebaseUser != null) {
            // Hiển thị thông tin từ Firebase Auth
            String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "N/A";
            String displayName = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User";
            
            tvWelcome.setText("Chào mừng!");
            tvUserInfo.setText("Email: " + email + "\nTên: " + displayName);
            
            // Thử load thông tin từ Firestore
            firebaseRepo.getUser(firebaseUser.getUid(), new FirebaseRepo.FirebaseCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    // Load thành công từ Firestore
                    tvUserInfo.setText(
                        "UID: " + user.getUid() + "\n" +
                        "Tên: " + user.getName() + "\n" +
                        "Email: " + user.getEmail() + "\n" +
                        "Avatar: " + (user.getAvatarUrl() != null ? "Có" : "Không có")
                    );
                }

                @Override
                public void onFailure(Exception e) {
                    // Không load được từ Firestore, dùng thông tin từ Auth
                    // Đã hiển thị ở trên rồi
                }
            });
        }
    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void logout() {
        firebaseRepo.logout();
        showSuccessMessage("Đã đăng xuất thành công");
        
        // Chuyển đến LoginActivity sau 1 giây
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }

    private void showSuccessMessage(String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(getResources().getColor(R.color.success, null));
        snackbar.setTextColor(getResources().getColor(android.R.color.white, null));
        snackbar.show();
    }
}

