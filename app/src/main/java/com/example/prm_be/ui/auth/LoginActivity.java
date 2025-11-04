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
                            navigateToHome();
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

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
                navigateToHome();
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                String errorMessage = "Đăng nhập thất bại";

                if (e != null && e.getMessage() != null) {
                    String errorMsg = e.getMessage();
                    Log.e("LoginActivity", "Login error: " + errorMsg, e);

                    if (errorMsg.contains("user-not-found")) {
                        errorMessage = "Email không tồn tại trong hệ thống";
                    } else if (errorMsg.contains("wrong-password")) {
                        errorMessage = "Mật khẩu không chính xác";
                    } else if (errorMsg.contains("invalid-email")) {
                        errorMessage = "Email không hợp lệ";
                    } else if (errorMsg.contains("user-disabled")) {
                        errorMessage = "Tài khoản đã bị vô hiệu hóa";
                    } else if (errorMsg.contains("network") || errorMsg.contains("network_error")) {
                        errorMessage = "Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại";
                    } else {
                        errorMessage = "Đăng nhập thất bại: " + errorMsg;
                    }
                } else if (e != null) {
                    Log.e("LoginActivity", "Login error (no message): " + e.getClass().getName(), e);
                    errorMessage = "Đã xảy ra lỗi. Vui lòng thử lại sau";
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
