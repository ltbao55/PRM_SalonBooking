package com.example.prm_be.ui.staff;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.Service;
import com.example.prm_be.data.models.User;
import com.example.prm_be.data.models.Stylist;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class StaffServicesActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvServices;
    private ProgressBar progressBar;
    private FloatingActionButton fabAdd;

    private FirebaseRepo repo;
    private StaffServicesAdapter adapter;
    private String salonId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!com.example.prm_be.utils.RoleGuard.checkRoleSync(this, "staff")) {
            return;
        }

        setContentView(R.layout.activity_staff_services);

        repo = FirebaseRepo.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Dịch vụ của salon");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        rvServices = findViewById(R.id.rvServices);
        progressBar = findViewById(R.id.progressBar);
        fabAdd = findViewById(R.id.fabAdd);

        adapter = new StaffServicesAdapter(new StaffServicesAdapter.ServiceListener() {
            @Override
            public void onEdit(Service service) {
                showEditServiceBottomSheet(service);
            }

            @Override
            public void onDelete(Service service) {
                deleteService(service);
            }
        });
        rvServices.setLayoutManager(new LinearLayoutManager(this));
        rvServices.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showEditServiceBottomSheet(null));

        resolveSalonAndLoad();
    }

    private void resolveSalonAndLoad() {
        if (!repo.isUserLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String uid = repo.getCurrentUser().getUid();
        repo.getUser(uid, new FirebaseRepo.FirebaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null || TextUtils.isEmpty(user.getStylistId())) {
                    Toast.makeText(StaffServicesActivity.this, "Tài khoản staff chưa liên kết stylist", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                repo.getStylistById(user.getStylistId(), new FirebaseRepo.FirebaseCallback<Stylist>() {
                    @Override
                    public void onSuccess(Stylist stylist) {
                        salonId = stylist.getSalonId();
                        loadServices();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(StaffServicesActivity.this, "Không tìm thấy stylist: " + (e!=null?e.getMessage():""), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(StaffServicesActivity.this, "Không thể lấy user: " + (e!=null?e.getMessage():""), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadServices() {
        showLoading(true);
        repo.getServicesOfSalon(salonId, new FirebaseRepo.FirebaseCallback<List<Service>>() {
            @Override
            public void onSuccess(List<Service> services) {
                showLoading(false);
                adapter.setItems(services != null ? services : new ArrayList<>());
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Toast.makeText(StaffServicesActivity.this, "Không thể tải dịch vụ: " + (e!=null?e.getMessage():""), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditServiceBottomSheet(@Nullable Service service) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_edit_service, null);
        dialog.setContentView(view);

        EditText edtName = view.findViewById(R.id.edtServiceName);
        EditText edtPrice = view.findViewById(R.id.edtServicePrice);
        EditText edtDuration = view.findViewById(R.id.edtServiceDuration);
        View btnCancel = view.findViewById(R.id.btnCancel);
        View btnSave = view.findViewById(R.id.btnSave);

        if (service != null) {
            edtName.setText(service.getName());
            edtPrice.setText(String.valueOf(service.getPrice()));
            edtDuration.setText(String.valueOf(service.getDurationInMinutes()));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String priceStr = edtPrice.getText().toString().trim();
            String durationStr = edtDuration.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                edtName.setError("Nhập tên dịch vụ");
                return;
            }
            long price = 0;
            int duration = 60;
            try { price = Long.parseLong(priceStr); } catch (Exception ignored) {}
            try { duration = Integer.parseInt(durationStr); } catch (Exception ignored) {}
            if (duration <= 0) duration = 60;

            if (service == null) {
                Service newService = new Service(null, name, price, duration);
                repo.createService(salonId, newService, new FirebaseRepo.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(StaffServicesActivity.this, "Đã thêm dịch vụ", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadServices();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(StaffServicesActivity.this, "Thêm thất bại: " + (e!=null?e.getMessage():""), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                service.setName(name);
                service.setPrice(price);
                service.setDurationInMinutes(duration);
                repo.updateService(salonId, service.getId(), service, new FirebaseRepo.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(StaffServicesActivity.this, "Đã cập nhật dịch vụ", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadServices();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(StaffServicesActivity.this, "Cập nhật thất bại: " + (e!=null?e.getMessage():""), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.show();
    }

    private void deleteService(Service service) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa dịch vụ")
                .setMessage("Bạn chắc chắn muốn xóa dịch vụ này?")
                .setPositiveButton("Xóa", (d,w) -> {
                    repo.deleteService(salonId, service.getId(), new FirebaseRepo.FirebaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(StaffServicesActivity.this, "Đã xóa dịch vụ", Toast.LENGTH_SHORT).show();
                            loadServices();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(StaffServicesActivity.this, "Xóa thất bại: " + (e!=null?e.getMessage():""), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        rvServices.setVisibility(loading ? View.GONE : View.VISIBLE);
    }
}


