package com.example.prm_be.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.google.android.material.snackbar.Snackbar;

/**
 * Forgot Password Activity - Màn hình quên mật khẩu
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtEmail;
    private Button btnSendResetEmail;
    private TextView tvBackToLogin;
    private ProgressBar progressBar;
    private View rootView;

    private FirebaseRepo firebaseRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Khởi tạo views
        initViews();

        // Khởi tạo FirebaseRepo
        firebaseRepo = FirebaseRepo.getInstance();

        // Xử lý sự kiện
        setupClickListeners();
    }

    private void initViews() {
        rootView = findViewById(android.R.id.content);
        edtEmail = findViewById(R.id.edtEmail);
        btnSendResetEmail = findViewById(R.id.btnSendResetEmail);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        // Nút Gửi email reset
        btnSendResetEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResetEmail();
            }
        });

        // Quay lại Login
        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Gửi email reset mật khẩu
     */
    private void sendResetEmail() {
        String email = edtEmail.getText().toString().trim();

        // Validation
        if (!validateEmail(email)) {
            return;
        }

        // Hiển thị ProgressBar
        showLoading(true);

        // Gọi FirebaseRepo để gửi email reset
        android.util.Log.d("ForgotPassword", "Sending reset email to: " + email);
        firebaseRepo.sendPasswordResetEmail(email, new FirebaseRepo.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Gửi email thành công
                showLoading(false);
                android.util.Log.d("ForgotPassword", "Reset email sent successfully");
                showSuccessMessage("Email đặt lại mật khẩu đã được gửi đến: " + email + "\nVui lòng kiểm tra hộp thư (kể cả thư mục Spam).");
                
                // Đóng activity sau 2 giây
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 2000);
            }

            @Override
            public void onFailure(Exception e) {
                // Gửi email thất bại
                showLoading(false);
                String errorMessage = "Không thể gửi email reset mật khẩu";
                if (e != null && e.getMessage() != null) {
                    String errorMsg = e.getMessage();
                    // Log error để debug
                    android.util.Log.e("ForgotPassword", "Error: " + errorMsg, e);
                    
                    if (errorMsg.contains("user-not-found") || errorMsg.contains("user")) {
                        errorMessage = "Email không tồn tại trong hệ thống. Vui lòng kiểm tra lại email.";
                    } else if (errorMsg.contains("invalid-email")) {
                        errorMessage = "Email không hợp lệ";
                    } else if (errorMsg.contains("network") || errorMsg.contains("network_error")) {
                        errorMessage = "Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại";
                    } else if (errorMsg.contains("too-many-requests")) {
                        errorMessage = "Đã gửi quá nhiều email. Vui lòng đợi vài phút rồi thử lại";
                    } else {
                        errorMessage = "Lỗi: " + errorMsg + "\nVui lòng thử lại sau";
                    }
                } else if (e != null) {
                    android.util.Log.e("ForgotPassword", "Error (no message): " + e.getClass().getName(), e);
                    errorMessage = "Đã xảy ra lỗi. Vui lòng thử lại sau";
                }
                showErrorMessage(errorMessage);
            }
        });
    }

    /**
     * Validate email
     */
    private boolean validateEmail(String email) {
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

        return true;
    }

    /**
     * Hiển thị/ẩn ProgressBar
     */
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSendResetEmail.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSendResetEmail.setEnabled(true);
        }
    }

    /**
     * Hiển thị thông báo thành công (Snackbar)
     */
    private void showSuccessMessage(String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getResources().getColor(R.color.success, null));
        snackbar.setTextColor(getResources().getColor(android.R.color.white, null));
        snackbar.show();
    }

    /**
     * Hiển thị thông báo lỗi (Snackbar)
     */
    private void showErrorMessage(String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getResources().getColor(R.color.error, null));
        snackbar.setTextColor(getResources().getColor(android.R.color.white, null));
        snackbar.show();
    }
}

