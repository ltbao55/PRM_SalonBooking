package com.example.prm_be.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Màn hình hồ sơ dành cho Staff: cho phép đổi tên (qua màn hình chỉnh sửa)
 * và đổi mật khẩu (bottom sheet). Ẩn lịch sử đặt lịch.
 */
public class StaffProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvName;
    private TextView tvEmail;
    private Button btnEditProfile;
    private Button btnChangePassword;
    private Button btnBookingHistory;
    private Button btnLogout;

    private FirebaseRepo repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chỉ cho phép staff
        if (!com.example.prm_be.utils.RoleGuard.checkRoleSync(this, "staff")) {
            return;
        }

        setContentView(R.layout.activity_profile);

        repo = FirebaseRepo.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        initViews();
        setupClickListeners();
        loadUserData();
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnBookingHistory = findViewById(R.id.btnBookingHistory);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, StaffEditProfileActivity.class));
        });

        btnChangePassword.setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        // Staff không có lịch sử đặt lịch
        btnBookingHistory.setVisibility(View.GONE);

        btnLogout.setOnClickListener(v -> {
            repo.logout();
            startActivity(new Intent(this, com.example.prm_be.ui.auth.LoginActivity.class));
            finish();
        });
    }

    private void loadUserData() {
        if (!repo.isUserLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = repo.getCurrentUser().getUid();
        repo.getUser(uid, new FirebaseRepo.FirebaseCallback<com.example.prm_be.data.models.User>() {
            @Override
            public void onSuccess(com.example.prm_be.data.models.User user) {
                if (user != null) {
                    tvName.setText(user.getName());
                    tvEmail.setText(user.getEmail());

                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
                    } else {
                        imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(StaffProfileActivity.this,
                    "Không thể tải thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePasswordDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        dialog.setContentView(dialogView);

        TextInputEditText edtCurrentPassword = dialogView.findViewById(R.id.edtCurrentPassword);
        TextInputEditText edtNewPassword = dialogView.findViewById(R.id.edtNewPassword);
        TextInputEditText edtConfirmPassword = dialogView.findViewById(R.id.edtConfirmPassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String currentPassword = edtCurrentPassword.getText() != null ? edtCurrentPassword.getText().toString() : "";
            String newPassword = edtNewPassword.getText() != null ? edtNewPassword.getText().toString() : "";
            String confirmPassword = edtConfirmPassword.getText() != null ? edtConfirmPassword.getText().toString() : "";

            if (TextUtils.isEmpty(currentPassword)) {
                edtCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
                edtCurrentPassword.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(newPassword)) {
                edtNewPassword.setError("Vui lòng nhập mật khẩu mới");
                edtNewPassword.requestFocus();
                return;
            }
            if (newPassword.length() < 6) {
                edtNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                edtNewPassword.requestFocus();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
                edtConfirmPassword.requestFocus();
                return;
            }
            if (newPassword.equals(currentPassword)) {
                edtNewPassword.setError("Mật khẩu mới phải khác mật khẩu hiện tại");
                edtNewPassword.requestFocus();
                return;
            }

            btnConfirm.setEnabled(false);
            btnConfirm.setText("Đang xử lý...");

            String userEmail = repo.getCurrentUser() != null ? repo.getCurrentUser().getEmail() : "";
            repo.changePassword(userEmail, currentPassword, newPassword, new FirebaseRepo.FirebaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(StaffProfileActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Exception e) {
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText("Xác nhận");
                    String errorMsg = "Đổi mật khẩu thất bại";
                    if (e != null && e.getMessage() != null) {
                        if (e.getMessage().contains("wrong-password")) {
                            errorMsg = "Mật khẩu hiện tại không chính xác";
                            edtCurrentPassword.setError("Mật khẩu không chính xác");
                            edtCurrentPassword.requestFocus();
                        } else {
                            errorMsg = "Đổi mật khẩu thất bại: " + e.getMessage();
                        }
                    }
                    Toast.makeText(StaffProfileActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}


