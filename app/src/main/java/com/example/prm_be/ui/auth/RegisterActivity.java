package com.example.prm_be.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.google.firebase.auth.FirebaseUser;

/**
 * Register Activity - Màn hình đăng ký
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private View rootView;

    private FirebaseRepo firebaseRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo views
        initViews();

        // Khởi tạo FirebaseRepo
        firebaseRepo = FirebaseRepo.getInstance();

        // Xử lý sự kiện
        setupClickListeners();
    }

    private void initViews() {
        rootView = findViewById(android.R.id.content);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        // Nút Đăng ký
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Chuyển đến màn hình Đăng nhập
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Xử lý đăng ký
     */
    private void registerUser() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Validation
        if (!validateInput(name, email, password, confirmPassword)) {
            return;
        }

        // Hiển thị ProgressBar
        showLoading(true);

        // Gọi FirebaseRepo để đăng ký
        // Hàm register() sẽ tự động tạo User document trong Firestore
        firebaseRepo.register(email, password, name, new FirebaseRepo.FirebaseCallback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser result) {
                // Đăng ký thành công
                showLoading(false);
                showSuccessMessage("Đăng ký thành công! Vui lòng đăng nhập.");
                
                // Chuyển về màn hình Đăng nhập sau 1 giây
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }, 1500);
            }

            @Override
            public void onFailure(Exception e) {
                // Đăng ký thất bại
                showLoading(false);
                String errorMessage = "Đăng ký thất bại";
                if (e != null && e.getMessage() != null) {
                    if (e.getMessage().contains("email-already-in-use") || e.getMessage().contains("email")) {
                        errorMessage = "Email đã được sử dụng";
                    } else if (e.getMessage().contains("weak-password") || e.getMessage().contains("password")) {
                        errorMessage = "Mật khẩu quá yếu. Mật khẩu phải có ít nhất 6 ký tự";
                    } else if (e.getMessage().contains("network")) {
                        errorMessage = "Lỗi kết nối mạng. Vui lòng thử lại";
                    } else if (e.getMessage().contains("invalid-email")) {
                        errorMessage = "Email không hợp lệ";
                    } else {
                        errorMessage = "Lỗi: " + e.getMessage();
                    }
                }
                showErrorMessage(errorMessage);
            }
        });
    }

    /**
     * Validate input
     */
    private boolean validateInput(String name, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(name)) {
            edtName.setError("Vui lòng nhập họ tên");
            edtName.requestFocus();
            return false;
        }
        
        if (name.length() < 2) {
            edtName.setError("Họ tên phải có ít nhất 2 ký tự");
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
        
        if (password.length() > 128) {
            edtPassword.setError("Mật khẩu không được quá 128 ký tự");
            edtPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            edtConfirmPassword.setError("Vui lòng nhập lại mật khẩu");
            edtConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu không khớp");
            edtConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Hiển thị/ẩn ProgressBar
     */
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
        }
    }

    /**
     * Hiển thị thông báo thành công (Snackbar)
     */
    private void showSuccessMessage(String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(getResources().getColor(R.color.success_color, null));
        snackbar.setTextColor(getResources().getColor(android.R.color.white, null));
        snackbar.show();
    }

    /**
     * Hiển thị thông báo lỗi (Snackbar)
     */
    private void showErrorMessage(String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getResources().getColor(R.color.error_color, null));
        snackbar.setTextColor(getResources().getColor(android.R.color.white, null));
        snackbar.show();
    }
}

