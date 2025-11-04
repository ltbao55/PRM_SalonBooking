package com.example.prm_be.ui.dev;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;

public class DevToolsActivity extends AppCompatActivity {
    private View root;
    private FirebaseRepo repo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_tools);
        root = findViewById(android.R.id.content);
        repo = FirebaseRepo.getInstance();

        MaterialButton btnCreateStaff = findViewById(R.id.btnCreateStaff);
        MaterialButton btnCreateAdmin = findViewById(R.id.btnCreateAdmin);

        btnCreateStaff.setOnClickListener(v -> createAccount("staff1@lux.com", "123456", "Staff Test", "staff"));
        btnCreateAdmin.setOnClickListener(v -> createAccount("admin1@lux.com", "123456", "Admin Test", "admin"));
    }

    private void createAccount(String email, String password, String name, String role) {
        Snackbar.make(root, "Creating " + role + "...", Snackbar.LENGTH_SHORT).show();
        repo.register(email, password, name, new FirebaseRepo.FirebaseCallback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser user) {
                repo.updateUserRole(user.getUid(), role, new FirebaseRepo.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Snackbar.make(root, "Created " + role + " account: " + email, Snackbar.LENGTH_LONG).show();
                        // Đăng xuất để không giữ phiên test
                        repo.logout();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Snackbar.make(root, "Failed to set role: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Snackbar.make(root, "Create failed: " + (e != null ? e.getMessage() : "unknown"), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}


