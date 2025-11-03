package com.example.prm_be.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtName;
    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;

    private FirebaseRepo repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        repo = FirebaseRepo.getInstance();

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            repo.register(email, password, name, new FirebaseRepo.FirebaseCallback<FirebaseUser>() {
                @Override
                public void onSuccess(FirebaseUser firebaseUser) {
                    // Create user document in Firestore
                    if (firebaseUser != null && firebaseUser.getUid() != null) {
                        User user = new User(firebaseUser.getUid(), name, email, null);
                        repo.createUser(user, new FirebaseRepo.FirebaseCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                finish();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                finish();
                            }
                        });
                    } else {
                        finish();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
