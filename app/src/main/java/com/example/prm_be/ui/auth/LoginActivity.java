package com.example.prm_be.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.User;
import com.example.prm_be.ui.discovery.HomeActivity;
import com.example.prm_be.ui.staff.StaffHomeActivity;
import com.example.prm_be.ui.admin.AdminDashboardActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnGoogleSignIn;
    private android.widget.TextView tvRegister;
    private ProgressBar progressBar;
    private View rootView;

    private FirebaseRepo repo;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repo = FirebaseRepo.getInstance();
        rootView = findViewById(android.R.id.content);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();
        initGoogleSignIn();
        setupClickListeners();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (!validateInput(email, password)) {
                return;
            }

            performLogin(email, password);
        });

        if (btnGoogleSignIn != null) {
            btnGoogleSignIn.setOnClickListener(v -> startGoogleSignIn());
        }

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        return status == ConnectionResult.SUCCESS;
    }

    private void startGoogleSignIn() {
        if (!isGooglePlayServicesAvailable()) {
            showErrorMessage("Thiết bị/emulator không có Google Play services. Vui lòng dùng emulator có Play Store hoặc thiết bị thật có GMS.");
            return;
        }
        showLoading(true);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                if (account != null && account.getIdToken() != null) {
                    String idToken = account.getIdToken();
                    repo.signInWithGoogle(idToken, new FirebaseRepo.FirebaseCallback<FirebaseUser>() {
                        @Override
                        public void onSuccess(FirebaseUser user) {
                            showLoading(false);
                            Log.d("LoginActivity", "Google sign-in success: " + user.getEmail());
                            fetchRoleAndNavigate(user.getUid());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            showLoading(false);
                            String msg = e != null && e.getMessage() != null ? e.getMessage() : "Google sign-in failed";
                            showErrorMessage("Đăng nhập Google thất bại: " + msg);
                        }
                    });
                } else {
                    showLoading(false);
                    showErrorMessage("Không lấy được token Google");
                }
            } catch (ApiException e) {
                showLoading(false);
                Log.e("LoginActivity", "Google sign-in ApiException", e);
                showErrorMessage("Google sign-in lỗi: " + e.getStatusCode());
            }
        }
    }

    private void navigateByRole(String role) {
        Class<?> destination;
        if ("admin".equalsIgnoreCase(role)) {
            destination = AdminDashboardActivity.class;
        } else if ("staff".equalsIgnoreCase(role)) {
            destination = StaffHomeActivity.class;
        } else {
            destination = HomeActivity.class;
        }
        Intent intent = new Intent(LoginActivity.this, destination);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void fetchRoleAndNavigate(String uid) {
        repo.getUser(uid, new FirebaseRepo.FirebaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                // Kiểm tra status của user
                String status = user.getStatus() != null ? user.getStatus() : "active";
                
                if ("disabled".equalsIgnoreCase(status)) {
                    // Account bị ban/disabled, đăng xuất và hiển thị thông báo
                    repo.logout();
                    showErrorMessage("Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ admin để được hỗ trợ.");
                    return;
                }
                
                // Account active, tiếp tục đăng nhập
                String role = user.getRole() != null ? user.getRole() : "user";
                navigateByRole(role);
            }

            @Override
            public void onFailure(Exception e) {
                // Nếu không lấy được user profile, fallback về Home của user thường
                // (có thể là user mới chưa có document trong Firestore)
                navigateByRole("user");
            }
        });
    }

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

        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            edtPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void performLogin(String email, String password) {
        showLoading(true);

        repo.login(email, password, new FirebaseRepo.FirebaseCallback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser user) {
                showLoading(false);
                Log.d("LoginActivity", "Login successful: " + user.getEmail());
                fetchRoleAndNavigate(user.getUid());
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                String errorMessage = "Đăng nhập thất bại";

                if (e != null) {
                    Log.e("LoginActivity", "Login error", e);

                    // Kiểm tra nếu là FirebaseAuthException để lấy error code chính xác
                    if (e instanceof FirebaseAuthException) {
                        FirebaseAuthException authException = (FirebaseAuthException) e;
                        String errorCode = authException.getErrorCode();
                        Log.d("LoginActivity", "FirebaseAuthException error code: " + errorCode);
                        
                        // Firebase error codes có thể có format khác nhau, kiểm tra cả với và không có prefix ERROR_
                        switch (errorCode) {
                            case "ERROR_USER_NOT_FOUND":
                            case "ERROR_USER_DOES_NOT_EXIST":
                            case "user-not-found":
                                errorMessage = "Email không tồn tại trong hệ thống. Vui lòng kiểm tra lại email hoặc đăng ký tài khoản mới.";
                                break;
                            case "ERROR_WRONG_PASSWORD":
                            case "ERROR_INVALID_CREDENTIAL":
                            case "wrong-password":
                            case "invalid-credential":
                                errorMessage = "Mật khẩu không chính xác. Vui lòng kiểm tra lại mật khẩu.";
                                break;
                            case "ERROR_INVALID_EMAIL":
                            case "invalid-email":
                                errorMessage = "Email không hợp lệ. Vui lòng nhập đúng định dạng email.";
                                break;
                            case "ERROR_USER_DISABLED":
                            case "user-disabled":
                                errorMessage = "Tài khoản đã bị vô hiệu hóa. Vui lòng liên hệ admin để được hỗ trợ.";
                                break;
                            case "ERROR_TOO_MANY_REQUESTS":
                            case "too-many-requests":
                                errorMessage = "Quá nhiều lần thử đăng nhập. Vui lòng thử lại sau vài phút.";
                                break;
                            case "ERROR_NETWORK_REQUEST_FAILED":
                            case "ERROR_INTERNAL_ERROR":
                            case "network-request-failed":
                                errorMessage = "Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại.";
                                break;
                            default:
                                errorMessage = "Đăng nhập thất bại. Vui lòng thử lại sau.";
                                Log.e("LoginActivity", "Unknown error code: " + errorCode);
                        }
                    } else if (e.getMessage() != null) {
                        String errorMsg = e.getMessage().toLowerCase();
                        
                        // Fallback: kiểm tra message nếu không phải FirebaseAuthException
                        if (errorMsg.contains("user-not-found") || errorMsg.contains("there is no user record")) {
                            errorMessage = "Email không tồn tại trong hệ thống. Vui lòng kiểm tra lại email hoặc đăng ký tài khoản mới.";
                        } else if (errorMsg.contains("wrong-password") || errorMsg.contains("password is invalid") || errorMsg.contains("invalid password")) {
                            errorMessage = "Mật khẩu không chính xác. Vui lòng kiểm tra lại mật khẩu.";
                        } else if (errorMsg.contains("invalid-email") || errorMsg.contains("invalid email")) {
                            errorMessage = "Email không hợp lệ. Vui lòng nhập đúng định dạng email.";
                        } else if (errorMsg.contains("user-disabled") || errorMsg.contains("user account has been disabled")) {
                            errorMessage = "Tài khoản đã bị vô hiệu hóa. Vui lòng liên hệ admin để được hỗ trợ.";
                        } else if (errorMsg.contains("network") || errorMsg.contains("network_error") || errorMsg.contains("connection")) {
                            errorMessage = "Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại.";
                        } else {
                            errorMessage = "Đăng nhập thất bại: " + e.getMessage();
                        }
                    } else {
                        errorMessage = "Đã xảy ra lỗi. Vui lòng thử lại sau";
                    }
                }

                showErrorMessage(errorMessage);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnLogin.setEnabled(!isLoading);
        if (btnGoogleSignIn != null) {
            btnGoogleSignIn.setEnabled(!isLoading);
        }
    }

    private void showErrorMessage(String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getResources().getColor(R.color.error, null));
        snackbar.setTextColor(getResources().getColor(android.R.color.white, null));
        snackbar.show();
    }
}
