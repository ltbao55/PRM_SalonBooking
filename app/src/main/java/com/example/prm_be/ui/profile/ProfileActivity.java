package com.example.prm_be.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
        // TODO: Load user data from FirebaseRepo
        // if (repo.isUserLoggedIn()) {
        //     String uid = repo.getCurrentUser().getUid();
        //     repo.getUser(uid, new FirebaseRepo.FirebaseCallback<User>() {...});
        // }
    }
}
