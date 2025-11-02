package com.example.prm_be.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.ui.home.HomeActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;

/**
 * Login Activity - Màn hình đăng nhập
 */
public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin, btnGoogleSignIn;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBar;
    private View rootView;

    private FirebaseRepo firebaseRepo;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo views
        initViews();

        // Khởi tạo FirebaseRepo
        firebaseRepo = FirebaseRepo.getInstance();

        // Khởi tạo Google Sign-In
        setupGoogleSignIn();

        // Xử lý sự kiện
        setupClickListeners();
    }

    private void initViews() {
        rootView = findViewById(android.R.id.content);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Thiết lập Google Sign-In
     */
    private void setupGoogleSignIn() {
        // Lấy Web Client ID từ strings.xml
        String webClientId = getString(R.string.default_web_client_id);
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Khởi tạo ActivityResultLauncher cho Google Sign-In
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleGoogleSignInResult(result.getData());
                    }
                }
        );
    }

    private void setupClickListeners() {
        // Nút Đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Nút Đăng nhập với Google
        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        // Chuyển đến màn hình Đăng ký
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Chuyển đến màn hình Quên mật khẩu
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Xử lý đăng nhập bằng email/password
     */
    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Validation
        if (!validateInput(email, password)) {
            return;
        }

        // Hiển thị ProgressBar
        showLoading(true);

        // Gọi FirebaseRepo để đăng nhập
        firebaseRepo.login(email, password, new FirebaseRepo.FirebaseCallback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser result) {
                // Đăng nhập thành công
                showLoading(false);
                showSuccessMessage("Đăng nhập thành công!");

                // Chuyển đến HomeActivity sau 0.5 giây
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }, 500);
            }

            @Override
            public void onFailure(Exception e) {
                // Đăng nhập thất bại
                showLoading(false);
                String errorMessage = "Đăng nhập thất bại";
                if (e != null && e.getMessage() != null) {
                    if (e.getMessage().contains("user-not-found") || e.getMessage().contains("user")) {
                        errorMessage = "Email không tồn tại";
                    } else if (e.getMessage().contains("wrong-password") || e.getMessage().contains("password")) {
                        errorMessage = "Mật khẩu không đúng";
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
     * Xử lý đăng nhập bằng Google
     */
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    /**
     * Xử lý kết quả Google Sign-In
     */
    private void handleGoogleSignInResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                // Gửi ID Token lên Firebase
                showLoading(true);
                firebaseRepo.signInWithGoogle(account.getIdToken(), new FirebaseRepo.FirebaseCallback<FirebaseUser>() {
                    @Override
                    public void onSuccess(FirebaseUser result) {
                        showLoading(false);
                        showSuccessMessage("Đăng nhập với Google thành công!");

                        // Chuyển đến HomeActivity
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }, 500);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        showLoading(false);
                        String errorMessage = "Đăng nhập với Google thất bại";
                        if (e != null && e.getMessage() != null) {
                            if (e.getMessage().contains("10")) {
                                errorMessage = "Lỗi cấu hình Google Sign-In. Vui lòng kiểm tra SHA-1 fingerprint trong Firebase Console.";
                            } else {
                                errorMessage = "Lỗi: " + e.getMessage();
                            }
                        }
                        showErrorMessage(errorMessage);
                    }
                });
            }
        } catch (ApiException e) {
            showLoading(false);
            String errorMessage = "Đăng nhập với Google thất bại";
            if (e.getStatusCode() == 10) {
                errorMessage = "Lỗi cấu hình Google Sign-In. Vui lòng kiểm tra SHA-1 fingerprint trong Firebase Console.";
            } else {
                errorMessage = "Lỗi: " + e.getMessage();
            }
            showErrorMessage(errorMessage);
        }
    }

    /**
     * Validate input
     */
    private boolean validateInput(String email, String password) {
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

        return true;
    }

    /**
     * Hiển thị/ẩn ProgressBar
     */
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
            btnGoogleSignIn.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            btnGoogleSignIn.setEnabled(true);
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
