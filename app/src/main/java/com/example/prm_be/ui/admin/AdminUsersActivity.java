package com.example.prm_be.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.User;
import com.example.prm_be.utils.RoleGuard;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private RecyclerView rvUsers;
    private LinearLayout llEmptyState;
    private ProgressBar progressBar;
    private TextView tvSearch;

    private FirebaseRepo repo;
    private AdminUsersAdapter adapter;
    private List<User> allUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check admin role
        if (!RoleGuard.checkRoleSync(this, "admin")) {
            return;
        }
        
        setContentView(R.layout.activity_admin_users);

        repo = FirebaseRepo.getInstance();
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadAllUsers();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvUsers = findViewById(R.id.rvUsers);
        llEmptyState = findViewById(R.id.llEmptyState);
        progressBar = findViewById(R.id.progressBar);
        tvSearch = findViewById(R.id.tvSearch);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý tài khoản");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AdminUsersAdapter(user -> showUserDetail(user));
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }

    private void loadAllUsers() {
        showLoading(true);
        repo.getAllUsers(new FirebaseRepo.FirebaseCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                showLoading(false);
                allUsers = users;
                adapter.setUsers(users);
                updateEmptyState(users.isEmpty());
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Toast.makeText(AdminUsersActivity.this,
                    "Không thể tải danh sách tài khoản: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                    Toast.LENGTH_SHORT).show();
                updateEmptyState(true);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        rvUsers.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            rvUsers.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvUsers.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    private void showUserDetail(User user) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_user, null);
        bottomSheet.setContentView(bottomSheetView);

        TextView tvEditUserName = bottomSheetView.findViewById(R.id.tvEditUserName);
        TextView tvEditUserEmail = bottomSheetView.findViewById(R.id.tvEditUserEmail);
        MaterialButton btnRoleUser = bottomSheetView.findViewById(R.id.btnRoleUser);
        MaterialButton btnRoleStaff = bottomSheetView.findViewById(R.id.btnRoleStaff);
        MaterialButton btnRoleAdmin = bottomSheetView.findViewById(R.id.btnRoleAdmin);
        MaterialButton btnStatusActive = bottomSheetView.findViewById(R.id.btnStatusActive);
        MaterialButton btnStatusDisabled = bottomSheetView.findViewById(R.id.btnStatusDisabled);
        MaterialButton btnSaveEdit = bottomSheetView.findViewById(R.id.btnSaveEdit);
        MaterialButton btnCancelEdit = bottomSheetView.findViewById(R.id.btnCancelEdit);

        // Set current values
        tvEditUserName.setText(user.getName());
        tvEditUserEmail.setText(user.getEmail());

        // Current role
        String currentRole = user.getRole();
        if (currentRole == null || currentRole.isEmpty()) {
            currentRole = "user";
        }
        updateRoleButtons(btnRoleUser, btnRoleStaff, btnRoleAdmin, currentRole);

        // Current status
        String currentStatus = user.getStatus();
        if (currentStatus == null || currentStatus.isEmpty()) {
            currentStatus = "active";
        }
        updateStatusButtons(btnStatusActive, btnStatusDisabled, currentStatus);

        // Save button - use final array to store selected values
        final String[] selectedRole = {currentRole};
        final String[] selectedStatus = {currentStatus};

        // Disable admin button - không cho phép đổi vai trò thành admin
        // Nếu user hiện tại là admin, vẫn hiển thị nhưng không cho phép thay đổi
        btnRoleAdmin.setEnabled(false);
        btnRoleAdmin.setAlpha(0.5f); // Làm mờ button
        btnRoleAdmin.setOnClickListener(v -> {
            Toast.makeText(AdminUsersActivity.this, 
                "Không được phép đổi vai trò thành admin", 
                Toast.LENGTH_SHORT).show();
        });

        // Role selection listeners
        btnRoleUser.setOnClickListener(v -> {
            selectedRole[0] = "user";
            updateRoleButtons(btnRoleUser, btnRoleStaff, btnRoleAdmin, "user");
        });
        btnRoleStaff.setOnClickListener(v -> {
            selectedRole[0] = "staff";
            updateRoleButtons(btnRoleUser, btnRoleStaff, btnRoleAdmin, "staff");
        });

        // Status selection listeners
        btnStatusActive.setOnClickListener(v -> {
            selectedStatus[0] = "active";
            updateStatusButtons(btnStatusActive, btnStatusDisabled, "active");
        });
        btnStatusDisabled.setOnClickListener(v -> {
            selectedStatus[0] = "disabled";
            updateStatusButtons(btnStatusActive, btnStatusDisabled, "disabled");
        });

        btnSaveEdit.setOnClickListener(v -> {
            saveUserChanges(user.getUid(), selectedRole[0], selectedStatus[0], bottomSheet);
        });

        btnCancelEdit.setOnClickListener(v -> bottomSheet.dismiss());

        bottomSheet.show();
    }

    private void updateRoleButtons(MaterialButton btnUser, MaterialButton btnStaff, MaterialButton btnAdmin, String selectedRole) {
        // Reset all buttons
        btnUser.setBackgroundTintList(null);
        btnStaff.setBackgroundTintList(null);
        btnAdmin.setBackgroundTintList(null);
        btnUser.setTextColor(getResources().getColor(R.color.gold, getTheme()));
        btnStaff.setTextColor(getResources().getColor(R.color.gold, getTheme()));
        btnAdmin.setTextColor(getResources().getColor(R.color.gold, getTheme()));

        // Highlight selected button
        switch (selectedRole.toLowerCase()) {
            case "user":
                btnUser.setBackgroundTintList(getResources().getColorStateList(R.color.gold, getTheme()));
                btnUser.setTextColor(getResources().getColor(R.color.white, getTheme()));
                break;
            case "staff":
                btnStaff.setBackgroundTintList(getResources().getColorStateList(R.color.gold, getTheme()));
                btnStaff.setTextColor(getResources().getColor(R.color.white, getTheme()));
                break;
            case "admin":
                btnAdmin.setBackgroundTintList(getResources().getColorStateList(R.color.gold, getTheme()));
                btnAdmin.setTextColor(getResources().getColor(R.color.white, getTheme()));
                break;
        }
    }

    private void updateStatusButtons(MaterialButton btnActive, MaterialButton btnDisabled, String selectedStatus) {
        // Reset all buttons
        btnActive.setBackgroundTintList(null);
        btnDisabled.setBackgroundTintList(null);
        btnActive.setTextColor(getResources().getColor(R.color.success, getTheme()));
        btnDisabled.setTextColor(getResources().getColor(R.color.error, getTheme()));

        // Highlight selected button
        if ("active".equals(selectedStatus.toLowerCase())) {
            btnActive.setBackgroundTintList(getResources().getColorStateList(R.color.success, getTheme()));
            btnActive.setTextColor(getResources().getColor(R.color.white, getTheme()));
        } else {
            btnDisabled.setBackgroundTintList(getResources().getColorStateList(R.color.error, getTheme()));
            btnDisabled.setTextColor(getResources().getColor(R.color.white, getTheme()));
        }
    }

    private void saveUserChanges(String userId, String newRole, String newStatus, BottomSheetDialog bottomSheet) {
        // Validation: không cho phép đổi vai trò thành admin
        if ("admin".equalsIgnoreCase(newRole)) {
            Toast.makeText(AdminUsersActivity.this, 
                "Không được phép đổi vai trò thành admin", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Update role
        repo.updateUserRole(userId, newRole, new FirebaseRepo.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Update status
                repo.updateUserStatus(userId, newStatus, new FirebaseRepo.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(AdminUsersActivity.this, "Đã cập nhật thành công", Toast.LENGTH_SHORT).show();
                        bottomSheet.dismiss();
                        loadAllUsers(); // Reload list
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(AdminUsersActivity.this,
                            "Không thể cập nhật trạng thái: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                            Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminUsersActivity.this,
                    "Không thể cập nhật vai trò: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
}
