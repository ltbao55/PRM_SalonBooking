package com.example.prm_be.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.ui.discovery.HomeActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private MaterialButton btnLogin;
    private android.widget.TextView tvRegister;

    private FirebaseRepo repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repo = FirebaseRepo.getInstance();

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Implement login logic using FirebaseRepo
            // repo.login(email, password, new FirebaseRepo.FirebaseCallback<FirebaseUser>() {...});
            
            // Tạm thời: Navigate to Home để test
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
        
        // Debug Navigation Buttons (For Testing UI)
        findViewById(R.id.btnDebugHome).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
        });
        
        findViewById(R.id.btnDebugSalonDetail).setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.prm_be.ui.discovery.SalonDetailActivity.class);
            intent.putExtra(com.example.prm_be.ui.discovery.SalonDetailActivity.EXTRA_SALON_ID, "test_salon_id");
            startActivity(intent);
        });
        
        findViewById(R.id.btnDebugBooking).setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.prm_be.ui.booking.BookingActivity.class);
            intent.putExtra(com.example.prm_be.ui.booking.BookingActivity.EXTRA_SALON_ID, "test_salon_id");
            startActivity(intent);
        });
        
        findViewById(R.id.btnDebugProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.prm_be.ui.profile.ProfileActivity.class));
        });
    }
}
