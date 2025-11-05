package com.example.prm_be.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.example.prm_be.data.models.Booking;
import com.example.prm_be.utils.RoleGuard;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminAllSchedulesActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private AutoCompleteTextView spinnerSalon;
    private RecyclerView rvBookings;
    private LinearLayout llEmptyState;
    private ProgressBar progressBar;

    private FirebaseRepo repo;
    private AdminScheduleCalendarAdapter adapter;
    private List<Booking> allBookings = new ArrayList<>();
    private List<com.example.prm_be.data.models.Salon> allSalons = new ArrayList<>();
    private String selectedSalonId = null; // null = "Tất cả salon"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check admin role
        if (!RoleGuard.checkRoleSync(this, "admin")) {
            return;
        }
        
        setContentView(R.layout.activity_admin_all_schedules);

        repo = FirebaseRepo.getInstance();
        
        initViews();
        setupToolbar();
        setupSalonSpinner();
        setupRecyclerView();
        loadSalons();
        loadAllBookings();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        spinnerSalon = findViewById(R.id.spinnerSalon);
        rvBookings = findViewById(R.id.rvBookings);
        llEmptyState = findViewById(R.id.llEmptyState);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tất cả lịch");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSalonSpinner() {
        spinnerSalon.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                selectedSalonId = null; // "Tất cả salon"
            } else {
                com.example.prm_be.data.models.Salon salon = allSalons.get(position - 1);
                selectedSalonId = salon.getId();
            }
            filterAndDisplayBookings();
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminScheduleCalendarAdapter(booking -> {
            showBookingDetail(booking);
        });
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        rvBookings.setAdapter(adapter);
    }

    private void loadSalons() {
        repo.getAllSalons(new FirebaseRepo.FirebaseCallback<List<com.example.prm_be.data.models.Salon>>() {
            @Override
            public void onSuccess(List<com.example.prm_be.data.models.Salon> salons) {
                allSalons = salons != null ? salons : new ArrayList<>();
                updateSalonSpinner();
            }

            @Override
            public void onFailure(Exception e) {
                allSalons = new ArrayList<>();
            }
        });
    }

    private void updateSalonSpinner() {
        List<String> salonNames = new ArrayList<>();
        salonNames.add("Tất cả salon");
        for (com.example.prm_be.data.models.Salon salon : allSalons) {
            salonNames.add(salon.getName());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, salonNames);
        spinnerSalon.setAdapter(spinnerAdapter);
        spinnerSalon.setText("Tất cả salon", false);
    }

    private void loadAllBookings() {
        showLoading(true);
        repo.getAllBookings(new FirebaseRepo.FirebaseCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                showLoading(false);
                allBookings = bookings != null ? bookings : new ArrayList<>();
                filterAndDisplayBookings();
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Toast.makeText(AdminAllSchedulesActivity.this,
                    "Không thể tải danh sách lịch: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                    Toast.LENGTH_SHORT).show();
                updateEmptyState(true);
            }
        });
    }

    private void filterAndDisplayBookings() {
        // Filter by salon
        List<Booking> filteredBookings = new ArrayList<>();
        for (Booking booking : allBookings) {
            if (selectedSalonId == null || selectedSalonId.equals(booking.getSalonId())) {
                filteredBookings.add(booking);
            }
        }

        // Sort by timestamp
        Collections.sort(filteredBookings, new Comparator<Booking>() {
            @Override
            public int compare(Booking b1, Booking b2) {
                return Long.compare(b1.getTimestamp(), b2.getTimestamp());
            }
        });

        // Group by date and pass to adapter
        adapter.setBookings(filteredBookings, allSalons);
        updateEmptyState(filteredBookings.isEmpty());
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        rvBookings.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            rvBookings.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvBookings.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    private void showBookingDetail(Booking booking) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_booking_detail_admin, null);
        bottomSheet.setContentView(bottomSheetView);

        TextView tvDetailSalonName = bottomSheetView.findViewById(R.id.tvDetailSalonName);
        TextView tvDetailServiceName = bottomSheetView.findViewById(R.id.tvDetailServiceName);
        TextView tvDetailCustomerName = bottomSheetView.findViewById(R.id.tvDetailCustomerName);
        TextView tvDetailDateTime = bottomSheetView.findViewById(R.id.tvDetailDateTime);
        Chip chipDetailStatus = bottomSheetView.findViewById(R.id.chipDetailStatus);
        MaterialButton btnConfirm = bottomSheetView.findViewById(R.id.btnConfirm);
        MaterialButton btnCancel = bottomSheetView.findViewById(R.id.btnCancel);
        MaterialButton btnDelete = bottomSheetView.findViewById(R.id.btnDelete);

        // Format date/time
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
        String dateTime = dateTimeFormat.format(new java.util.Date(booking.getTimestamp()));
        tvDetailDateTime.setText(dateTime);

        // Status
        String status = booking.getStatus();
        chipDetailStatus.setText(getStatusText(status));
        chipDetailStatus.setChipBackgroundColorResource(getStatusColor(status));

        // Load salon name, service name, customer name
        tvDetailSalonName.setText("Đang tải...");
        tvDetailServiceName.setText("Đang tải...");
        tvDetailCustomerName.setText("Đang tải...");

        // Load salon name
        repo.getSalonById(booking.getSalonId(), new FirebaseRepo.FirebaseCallback<com.example.prm_be.data.models.Salon>() {
            @Override
            public void onSuccess(com.example.prm_be.data.models.Salon salon) {
                tvDetailSalonName.setText(salon.getName());
            }

            @Override
            public void onFailure(Exception e) {
                tvDetailSalonName.setText("Không tìm thấy salon");
            }
        });

        // Load service name
        repo.getServicesOfSalon(booking.getSalonId(), new FirebaseRepo.FirebaseCallback<List<com.example.prm_be.data.models.Service>>() {
            @Override
            public void onSuccess(List<com.example.prm_be.data.models.Service> services) {
                for (com.example.prm_be.data.models.Service service : services) {
                    if (service.getId().equals(booking.getServiceId())) {
                        tvDetailServiceName.setText(service.getName());
                        return;
                    }
                }
                tvDetailServiceName.setText("Không tìm thấy dịch vụ");
            }

            @Override
            public void onFailure(Exception e) {
                tvDetailServiceName.setText("Không tìm thấy dịch vụ");
            }
        });

        // Load customer name
        repo.getUser(booking.getUserId(), new FirebaseRepo.FirebaseCallback<com.example.prm_be.data.models.User>() {
            @Override
            public void onSuccess(com.example.prm_be.data.models.User user) {
                tvDetailCustomerName.setText(user.getName());
            }

            @Override
            public void onFailure(Exception e) {
                tvDetailCustomerName.setText("Không tìm thấy khách hàng");
            }
        });

        // Update status buttons
        btnConfirm.setOnClickListener(v -> {
            updateBookingStatus(booking.getId(), "confirmed", bottomSheet);
        });

        btnCancel.setOnClickListener(v -> {
            updateBookingStatus(booking.getId(), "cancelled", bottomSheet);
        });

        btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog(booking.getId(), bottomSheet);
        });

        bottomSheet.show();
    }

    private void showDeleteConfirmationDialog(String bookingId, BottomSheetDialog bottomSheet) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa lịch")
                .setMessage("Bạn có chắc chắn muốn xóa lịch hẹn này? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteBooking(bookingId, bottomSheet);
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }

    private void deleteBooking(String bookingId, BottomSheetDialog bottomSheet) {
        repo.deleteBooking(bookingId, new FirebaseRepo.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(AdminAllSchedulesActivity.this, "Đã xóa lịch hẹn thành công", Toast.LENGTH_SHORT).show();
                bottomSheet.dismiss();
                loadAllBookings(); // Reload list
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminAllSchedulesActivity.this,
                    "Không thể xóa lịch hẹn: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBookingStatus(String bookingId, String status, BottomSheetDialog bottomSheet) {
        repo.updateBookingStatus(bookingId, status, new FirebaseRepo.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(AdminAllSchedulesActivity.this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                bottomSheet.dismiss();
                loadAllBookings();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminAllSchedulesActivity.this,
                    "Không thể cập nhật trạng thái: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStatusText(String status) {
        if (status == null) return "Chưa xác nhận";
        switch (status.toLowerCase()) {
            case "confirmed":
                return "Đã xác nhận";
            case "pending":
                return "Chờ xác nhận";
            case "completed":
                return "Đã hoàn thành";
            case "cancelled":
                return "Đã hủy";
            default:
                return "Chưa xác nhận";
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return R.color.warning;
        switch (status.toLowerCase()) {
            case "confirmed":
                return R.color.success;
            case "pending":
                return R.color.warning;
            case "completed":
                return R.color.info;
            case "cancelled":
                return R.color.error;
            default:
                return R.color.warning;
        }
    }
}
