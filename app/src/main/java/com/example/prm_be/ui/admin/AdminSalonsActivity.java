package com.example.prm_be.ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.Salon;
import com.example.prm_be.utils.RoleGuard;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AdminSalonsActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private RecyclerView rvSalons;
    private LinearLayout llEmptyState;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddSalon;

    private FirebaseRepo repo;
    private AdminSalonsAdapter adapter;
    private List<Salon> allSalons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check admin role
        if (!RoleGuard.checkRoleSync(this, "admin")) {
            return;
        }
        
        setContentView(R.layout.activity_admin_salons);

        repo = FirebaseRepo.getInstance();
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadAllSalons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvSalons = findViewById(R.id.rvSalons);
        llEmptyState = findViewById(R.id.llEmptyState);
        progressBar = findViewById(R.id.progressBar);
        fabAddSalon = findViewById(R.id.fabAddSalon);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý Salon");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AdminSalonsAdapter(
            salon -> showEditSalonDialog(salon),
            salon -> showDeleteConfirmDialog(salon)
        );
        rvSalons.setLayoutManager(new LinearLayoutManager(this));
        rvSalons.setAdapter(adapter);

        fabAddSalon.setOnClickListener(v -> showAddSalonDialog());
    }

    private void loadAllSalons() {
        showLoading(true);
        repo.getAllSalons(new FirebaseRepo.FirebaseCallback<List<Salon>>() {
            @Override
            public void onSuccess(List<Salon> salons) {
                showLoading(false);
                allSalons = salons;
                adapter.setSalons(salons);
                updateEmptyState(salons.isEmpty());
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Toast.makeText(AdminSalonsActivity.this,
                    "Không thể tải danh sách salon: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                    Toast.LENGTH_SHORT).show();
                updateEmptyState(true);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        rvSalons.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            rvSalons.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvSalons.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    private void showAddSalonDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_salon, null);
        EditText etSalonName = dialogView.findViewById(R.id.etSalonName);
        EditText etSalonAddress = dialogView.findViewById(R.id.etSalonAddress);
        EditText etSalonPhone = dialogView.findViewById(R.id.etSalonPhone);
        EditText etSalonImageUrl = dialogView.findViewById(R.id.etSalonImageUrl);
        EditText etSalonDescription = dialogView.findViewById(R.id.etSalonDescription);
        EditText etSalonRating = dialogView.findViewById(R.id.etSalonRating);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Thêm salon mới")
            .setView(dialogView)
            .setPositiveButton("Thêm", (d, w) -> {
                String name = etSalonName.getText().toString().trim();
                String address = etSalonAddress.getText().toString().trim();
                String phone = etSalonPhone.getText().toString().trim();
                String imageUrl = etSalonImageUrl.getText().toString().trim();
                String description = etSalonDescription.getText().toString().trim();
                String ratingStr = etSalonRating.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(this, "Vui lòng nhập tên salon", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(address)) {
                    Toast.makeText(this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
                    return;
                }

                double rating = 0.0;
                try {
                    if (!TextUtils.isEmpty(ratingStr)) {
                        rating = Double.parseDouble(ratingStr);
                    }
                } catch (NumberFormatException e) {
                    // Use default
                }

                Salon newSalon = new Salon(null, name, address, phone, imageUrl, description, rating);
                createSalon(newSalon);
            })
            .setNegativeButton("Hủy", null)
            .create();

        dialog.show();
    }

    private void showEditSalonDialog(Salon salon) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_salon, null);
        EditText etSalonName = dialogView.findViewById(R.id.etSalonName);
        EditText etSalonAddress = dialogView.findViewById(R.id.etSalonAddress);
        EditText etSalonPhone = dialogView.findViewById(R.id.etSalonPhone);
        EditText etSalonImageUrl = dialogView.findViewById(R.id.etSalonImageUrl);
        EditText etSalonDescription = dialogView.findViewById(R.id.etSalonDescription);
        EditText etSalonRating = dialogView.findViewById(R.id.etSalonRating);

        etSalonName.setText(salon.getName());
        etSalonAddress.setText(salon.getAddress());
        etSalonPhone.setText(salon.getPhone());
        etSalonImageUrl.setText(salon.getImageUrl());
        etSalonDescription.setText(salon.getDescription());
        etSalonRating.setText(String.valueOf(salon.getRating()));

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Chỉnh sửa salon")
            .setView(dialogView)
            .setPositiveButton("Lưu", (d, w) -> {
                String name = etSalonName.getText().toString().trim();
                String address = etSalonAddress.getText().toString().trim();
                String phone = etSalonPhone.getText().toString().trim();
                String imageUrl = etSalonImageUrl.getText().toString().trim();
                String description = etSalonDescription.getText().toString().trim();
                String ratingStr = etSalonRating.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(this, "Vui lòng nhập tên salon", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(address)) {
                    Toast.makeText(this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
                    return;
                }

                double rating = salon.getRating();
                try {
                    if (!TextUtils.isEmpty(ratingStr)) {
                        rating = Double.parseDouble(ratingStr);
                    }
                } catch (NumberFormatException e) {
                    // Keep old rating
                }

                salon.setName(name);
                salon.setAddress(address);
                salon.setPhone(phone);
                salon.setImageUrl(imageUrl);
                salon.setDescription(description);
                salon.setRating(rating);

                updateSalon(salon.getId(), salon);
            })
            .setNegativeButton("Hủy", null)
            .create();

        dialog.show();
    }

    private void showDeleteConfirmDialog(Salon salon) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa salon")
            .setMessage("Bạn có chắc chắn muốn xóa salon \"" + salon.getName() + "\"?\n\nLưu ý: Tất cả dịch vụ và stylist của salon này cũng sẽ bị xóa.")
            .setPositiveButton("Xóa", (d, w) -> deleteSalon(salon.getId()))
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void createSalon(Salon salon) {
        repo.createSalon(salon, new FirebaseRepo.FirebaseCallback<String>() {
            @Override
            public void onSuccess(String salonId) {
                Toast.makeText(AdminSalonsActivity.this, "Đã thêm salon thành công", Toast.LENGTH_SHORT).show();
                loadAllSalons();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminSalonsActivity.this,
                    "Không thể thêm salon: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSalon(String salonId, Salon salon) {
        repo.updateSalon(salonId, salon, new FirebaseRepo.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(AdminSalonsActivity.this, "Đã cập nhật salon thành công", Toast.LENGTH_SHORT).show();
                loadAllSalons();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminSalonsActivity.this,
                    "Không thể cập nhật salon: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteSalon(String salonId) {
        repo.deleteSalon(salonId, new FirebaseRepo.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(AdminSalonsActivity.this, "Đã xóa salon thành công", Toast.LENGTH_SHORT).show();
                loadAllSalons();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminSalonsActivity.this,
                    "Không thể xóa salon: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
}

