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
import com.example.prm_be.data.models.Service;
import com.example.prm_be.utils.RoleGuard;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminServicesActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private RecyclerView rvServices;
    private LinearLayout llEmptyState;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddService;

    private FirebaseRepo repo;
    private AdminServicesAdapter adapter;
    private List<Service> allServices = new ArrayList<>();
    private List<Salon> allSalons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check admin role
        if (!RoleGuard.checkRoleSync(this, "admin")) {
            return;
        }
        
        setContentView(R.layout.activity_admin_services);

        repo = FirebaseRepo.getInstance();
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadSalons();
        loadAllServices();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvServices = findViewById(R.id.rvServices);
        llEmptyState = findViewById(R.id.llEmptyState);
        progressBar = findViewById(R.id.progressBar);
        fabAddService = findViewById(R.id.fabAddService);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý dịch vụ");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AdminServicesAdapter(
            service -> showEditServiceDialog(service),
            service -> showDeleteConfirmDialog(service)
        );
        rvServices.setLayoutManager(new LinearLayoutManager(this));
        rvServices.setAdapter(adapter);

        fabAddService.setOnClickListener(v -> showAddServiceDialog());
    }

    private void loadSalons() {
        repo.getAllSalons(new FirebaseRepo.FirebaseCallback<List<Salon>>() {
            @Override
            public void onSuccess(List<Salon> salons) {
                allSalons = salons;
            }

            @Override
            public void onFailure(Exception e) {
                // Continue anyway
            }
        });
    }

    private void loadAllServices() {
        showLoading(true);
        repo.getAllServices(new FirebaseRepo.FirebaseCallback<List<Service>>() {
            @Override
            public void onSuccess(List<Service> services) {
                showLoading(false);
                allServices = services;
                adapter.setServices(services);
                updateEmptyState(services.isEmpty());
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Toast.makeText(AdminServicesActivity.this,
                    "Không thể tải danh sách dịch vụ: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                    Toast.LENGTH_SHORT).show();
                updateEmptyState(true);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        rvServices.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            rvServices.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvServices.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    private void showAddServiceDialog() {
        if (allSalons.isEmpty()) {
            Toast.makeText(this, "Chưa có salon nào. Vui lòng tạo salon trước.", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_service, null);
        EditText etServiceName = dialogView.findViewById(R.id.etServiceName);
        EditText etServicePrice = dialogView.findViewById(R.id.etServicePrice);
        EditText etServiceDuration = dialogView.findViewById(R.id.etServiceDuration);
        TextView tvSalonName = dialogView.findViewById(R.id.tvSalonName);

        // Show salon selection (simplified - just show first salon)
        // In production, you'd want a spinner/dropdown
        String salonId = allSalons.get(0).getId();
        String salonName = allSalons.get(0).getName();
        tvSalonName.setText("Salon: " + salonName);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Thêm dịch vụ mới")
            .setView(dialogView)
            .setPositiveButton("Thêm", (d, w) -> {
                String name = etServiceName.getText().toString().trim();
                String priceStr = etServicePrice.getText().toString().trim();
                String durationStr = etServiceDuration.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(this, "Vui lòng nhập tên dịch vụ", Toast.LENGTH_SHORT).show();
                    return;
                }

                long price = 0;
                try {
                    price = Long.parseLong(priceStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                int duration = 60;
                try {
                    duration = Integer.parseInt(durationStr);
                } catch (NumberFormatException e) {
                    // Use default
                }

                Service newService = new Service(null, name, price, duration);
                createService(salonId, newService);
            })
            .setNegativeButton("Hủy", null)
            .create();

        dialog.show();
    }

    private void showEditServiceDialog(Service service) {
        // For edit, we need to know which salon this service belongs to
        // This is simplified - in production, store salonId with service
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_service, null);
        EditText etServiceName = dialogView.findViewById(R.id.etServiceName);
        EditText etServicePrice = dialogView.findViewById(R.id.etServicePrice);
        EditText etServiceDuration = dialogView.findViewById(R.id.etServiceDuration);
        TextView tvSalonName = dialogView.findViewById(R.id.tvSalonName);

        etServiceName.setText(service.getName());
        etServicePrice.setText(String.valueOf(service.getPrice()));
        etServiceDuration.setText(String.valueOf(service.getDurationInMinutes()));
        tvSalonName.setText("(Chỉnh sửa dịch vụ)");

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Chỉnh sửa dịch vụ")
            .setView(dialogView)
            .setPositiveButton("Lưu", (d, w) -> {
                String name = etServiceName.getText().toString().trim();
                String priceStr = etServicePrice.getText().toString().trim();
                String durationStr = etServiceDuration.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(this, "Vui lòng nhập tên dịch vụ", Toast.LENGTH_SHORT).show();
                    return;
                }

                long price = 0;
                try {
                    price = Long.parseLong(priceStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                int duration = 60;
                try {
                    duration = Integer.parseInt(durationStr);
                } catch (NumberFormatException e) {
                    // Use default
                }

                service.setName(name);
                service.setPrice(price);
                service.setDurationInMinutes(duration);

                // Find salon for this service (simplified - use first salon)
                // In production, store salonId with service
                String salonId = allSalons.get(0).getId();
                updateService(salonId, service.getId(), service);
            })
            .setNegativeButton("Hủy", null)
            .create();

        dialog.show();
    }

    private void showDeleteConfirmDialog(Service service) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa dịch vụ")
            .setMessage("Bạn có chắc chắn muốn xóa dịch vụ \"" + service.getName() + "\"?")
            .setPositiveButton("Xóa", (d, w) -> {
                // Find salon for this service (simplified)
                String salonId = allSalons.get(0).getId();
                deleteService(salonId, service.getId());
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void createService(String salonId, Service service) {
        repo.createService(salonId, service, new FirebaseRepo.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(AdminServicesActivity.this, "Đã thêm dịch vụ thành công", Toast.LENGTH_SHORT).show();
                loadAllServices();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminServicesActivity.this,
                    "Không thể thêm dịch vụ: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateService(String salonId, String serviceId, Service service) {
        repo.updateService(salonId, serviceId, service, new FirebaseRepo.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(AdminServicesActivity.this, "Đã cập nhật dịch vụ thành công", Toast.LENGTH_SHORT).show();
                loadAllServices();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminServicesActivity.this,
                    "Không thể cập nhật dịch vụ: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteService(String salonId, String serviceId) {
        repo.deleteService(salonId, serviceId, new FirebaseRepo.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(AdminServicesActivity.this, "Đã xóa dịch vụ thành công", Toast.LENGTH_SHORT).show();
                loadAllServices();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminServicesActivity.this,
                    "Không thể xóa dịch vụ: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
}

