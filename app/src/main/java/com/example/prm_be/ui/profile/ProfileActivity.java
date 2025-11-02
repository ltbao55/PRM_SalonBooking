package com.example.prm_be.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.google.android.material.appbar.MaterialToolbar;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvName;
    private TextView tvEmail;
    private Button btnEditProfile;
    private Button btnBookingHistory;
    private Button btnLogout;

    private FirebaseRepo repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        repo = FirebaseRepo.getInstance();

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();
        setupClickListeners();
        loadUserData();
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnBookingHistory = findViewById(R.id.btnBookingHistory);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        btnBookingHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, BookingHistoryActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            repo.logout();
            // Navigate to Login
            startActivity(new Intent(this, com.example.prm_be.ui.auth.LoginActivity.class));
            finish();
        });
    }

    private void loadUserData() {
        if (!repo.isUserLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = repo.getCurrentUser().getUid();
        repo.getUser(uid, new FirebaseRepo.FirebaseCallback<com.example.prm_be.data.models.User>() {
            @Override
            public void onSuccess(com.example.prm_be.data.models.User user) {
                if (user != null) {
                    tvName.setText(user.getName());
                    tvEmail.setText(user.getEmail());
                    
                    // Load avatar image if available
                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        // TODO: Load image from URL using Glide or Picasso
                        // For now, just show placeholder
                        imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
                    } else {
                        imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ProfileActivity.this, 
                    "Không thể tải thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
