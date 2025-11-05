package com.example.prm_be.ui.staff;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseUser;

/**
 * Màn hình chỉnh sửa hồ sơ cho Staff: cho phép đổi tên (và ảnh sau này).
 */
public class StaffEditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imgAvatar;
    private EditText edtName;
    private Button btnSave;
    private MaterialToolbar toolbar;

    private FirebaseRepo repo;
    private User currentUser;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!com.example.prm_be.utils.RoleGuard.checkRoleSync(this, "staff")) {
            return;
        }

        setContentView(R.layout.activity_edit_profile);

        repo = FirebaseRepo.getInstance();

        toolbar = findViewById(R.id.toolbar);
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
        edtName = findViewById(R.id.edtName);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupClickListeners() {
        imgAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentUser == null) {
                Toast.makeText(this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                return;
            }

            currentUser.setName(name);

            if (selectedImageUri != null) {
                FirebaseUser firebaseUser = repo.getCurrentUser();
                if (firebaseUser == null) {
                    Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                    return;
                }
                btnSave.setEnabled(false);
                btnSave.setText("Đang lưu...");
                repo.uploadProfileImage(selectedImageUri, firebaseUser.getUid(), new FirebaseRepo.FirebaseCallback<String>() {
                    @Override
                    public void onSuccess(String url) {
                        currentUser.setAvatarUrl(url);
                        updateUserProfile();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        btnSave.setEnabled(true);
                        btnSave.setText("Lưu");
                        Toast.makeText(StaffEditProfileActivity.this, "Upload ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                updateUserProfile();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                imgAvatar.setImageURI(selectedImageUri);
                Toast.makeText(this, "Ảnh đã được chọn.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadUserData() {
        if (!repo.isUserLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = repo.getCurrentUser().getUid();
        repo.getUser(uid, new FirebaseRepo.FirebaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                if (user != null) {
                    edtName.setText(user.getName());
                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
                    } else {
                        imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(StaffEditProfileActivity.this,
                    "Không thể tải thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserProfile() {
        repo.updateUser(currentUser, new FirebaseRepo.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(StaffEditProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(StaffEditProfileActivity.this,
                    "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}


