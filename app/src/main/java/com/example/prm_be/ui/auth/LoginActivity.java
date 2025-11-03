package com.example.prm_be.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.User;
import com.example.prm_be.ui.discovery.HomeActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnGoogleSignIn;
    private android.widget.TextView tvRegister;

    private FirebaseRepo repo;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repo = FirebaseRepo.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        setupGoogleSignIn();

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
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
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

            repo.login(email, password, new FirebaseRepo.FirebaseCallback<FirebaseUser>() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    // Create/Update user in Firestore
                    syncUserToFirestore(user.getUid(), user.getDisplayName(), user.getEmail(), user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        btnGoogleSignIn.setOnClickListener(v -> {
            // Force fresh token to avoid expired/malformed credential
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
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

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result == null || result.getResultCode() != RESULT_OK || result.getData() == null) {
                        Toast.makeText(this, "Google sign-in cancelled", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData()).getResult(ApiException.class);
                        if (account != null) {
                            String idToken = account.getIdToken();
                            if (idToken == null || idToken.isEmpty()) {
                                Toast.makeText(this, "ID token is null. Check default_web_client_id/SHA-1.", Toast.LENGTH_LONG).show();
                                return;
                            }
                            firebaseAuthWithGoogle(idToken);
                        } else {
                            Toast.makeText(this, "No Google account returned", Toast.LENGTH_SHORT).show();
                        }
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google sign-in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            syncUserToFirestore(user.getUid(), user.getDisplayName(), user.getEmail(), user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
                        }
                    } else {
                        Toast.makeText(this, "Firebase auth failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void syncUserToFirestore(String uid, String name, String email, String avatarUrl) {
        User userModel = new User(uid, name != null ? name : "", email != null ? email : "", avatarUrl);
        repo.createUser(userModel, new FirebaseRepo.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(LoginActivity.this, "Save profile failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            }
        });
    }
}
