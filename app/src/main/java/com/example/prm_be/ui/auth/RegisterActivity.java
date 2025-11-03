package com.example.prm_be.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.ui.discovery.HomeActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtName;
    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private View rootView;

    private FirebaseRepo repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        repo = FirebaseRepo.getInstance();
        rootView = findViewById(android.R.id.content);

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
        progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (!validateInput(name, email, password)) {
                return;
            }

            performRegister(name, email, password);
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    /**
     * Validate input
     */
    private boolean validateInput(String name, String email, String password) {
        if (TextUtils.isEmpty(name)) {
            edtName.setError("Vui lòng nhập tên");
            edtName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            edtPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Perform register với Firebase
     */
    private void performRegister(String name, String email, String password) {
        showLoading(true);

        repo.register(email, password, name, new FirebaseRepo.FirebaseCallback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser user) {
                showLoading(false);
                Log.d("RegisterActivity", "Register successful: " + user.getEmail());
                
                showSuccessMessage("Đăng ký thành công!");
                
                // Navigate to HomeActivity sau 1 giây
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }, 1500);
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                String errorMessage = "Đăng ký thất bại";
                
                if (e != null && e.getMessage() != null) {
                    String errorMsg = e.getMessage();
                    Log.e("RegisterActivity", "Register error: " + errorMsg, e);
                    
                    if (errorMsg.contains("email-already-in-use")) {
                        errorMessage = "Email này đã được sử dụng. Vui lòng đăng nhập hoặc dùng email khác";
                    } else if (errorMsg.contains("invalid-email")) {
                        errorMessage = "Email không hợp lệ";
                    } else if (errorMsg.contains("weak-password")) {
                        errorMessage = "Mật khẩu quá yếu. Vui lòng chọn mật khẩu mạnh hơn";
                    } else if (errorMsg.contains("network") || errorMsg.contains("network_error")) {
                        errorMessage = "Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại";
                    } else {
                        errorMessage = "Đăng ký thất bại: " + errorMsg;
                    }
                } else if (e != null) {
                    Log.e("RegisterActivity", "Register error (no message): " + e.getClass().getName(), e);
                    errorMessage = "Đã xảy ra lỗi. Vui lòng thử lại sau";
                }
                
                showErrorMessage(errorMessage);
            }
        });
    }

    /**
     * Hiển thị/ẩn ProgressBar
     */
    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnRegister.setEnabled(!isLoading);
    }

    /**
     * Hiển thị thông báo thành công
     */
    private void showSuccessMessage(String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(getResources().getColor(R.color.success, null));
        snackbar.setTextColor(getResources().getColor(android.R.color.white, null));
        snackbar.show();
    }

    /**
     * Hiển thị thông báo lỗi
     */
    private void showErrorMessage(String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getResources().getColor(R.color.error, null));
        snackbar.setTextColor(getResources().getColor(android.R.color.white, null));
        snackbar.show();
    }
}
