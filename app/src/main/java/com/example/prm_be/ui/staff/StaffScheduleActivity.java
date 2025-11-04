package com.example.prm_be.ui.staff;

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
import com.example.prm_be.data.models.Booking;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.widget.TextView;

public class StaffScheduleActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private MaterialButton btnPrevWeek;
    private MaterialButton btnNextWeek;
    private TextView tvWeekRange;
    private RecyclerView rvSchedule;
    private LinearLayout llEmptyState;
    private ProgressBar progressBar;

    private FirebaseRepo repo;
    private StaffScheduleAdapter adapter;
    private Calendar currentWeek;
    private SimpleDateFormat weekFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_schedule);

        repo = FirebaseRepo.getInstance();
        currentWeek = Calendar.getInstance(Locale.getDefault());
        weekFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        initViews();
        setupRecyclerView();
        setupToolbar();
        setupWeekNavigation();
        loadSchedule();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        btnPrevWeek = findViewById(R.id.btnPrevWeek);
        btnNextWeek = findViewById(R.id.btnNextWeek);
        tvWeekRange = findViewById(R.id.tvWeekRange);
        rvSchedule = findViewById(R.id.rvSchedule);
        llEmptyState = findViewById(R.id.llEmptyState);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new StaffScheduleAdapter(booking -> {
            // TODO: Show booking detail bottom sheet
            showBookingDetail(booking);
        });
        rvSchedule.setLayoutManager(new LinearLayoutManager(this));
        rvSchedule.setAdapter(adapter);
    }

    private void setupWeekNavigation() {
        btnPrevWeek.setOnClickListener(v -> {
            currentWeek.add(Calendar.WEEK_OF_YEAR, -1);
            updateWeekDisplay();
            loadSchedule();
        });

        btnNextWeek.setOnClickListener(v -> {
            currentWeek.add(Calendar.WEEK_OF_YEAR, 1);
            updateWeekDisplay();
            loadSchedule();
        });

        updateWeekDisplay();
    }

    private void updateWeekDisplay() {
        Calendar startOfWeek = (Calendar) currentWeek.clone();
        startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.getFirstDayOfWeek());
        
        Calendar endOfWeek = (Calendar) startOfWeek.clone();
        endOfWeek.add(Calendar.DAY_OF_WEEK, 6);

        String weekText = String.format(Locale.getDefault(), 
            "%s - %s", 
            weekFormat.format(startOfWeek.getTime()),
            weekFormat.format(endOfWeek.getTime()));
        tvWeekRange.setText(weekText);
    }

    private void loadSchedule() {
        if (!repo.isUserLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseUser currentUser = repo.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String staffId = currentUser.getUid(); // Assume staffId = userId for now
        
        // Calculate week start and end timestamps
        Calendar startOfWeek = (Calendar) currentWeek.clone();
        startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.getFirstDayOfWeek());
        startOfWeek.set(Calendar.HOUR_OF_DAY, 0);
        startOfWeek.set(Calendar.MINUTE, 0);
        startOfWeek.set(Calendar.SECOND, 0);
        startOfWeek.set(Calendar.MILLISECOND, 0);

        Calendar endOfWeek = (Calendar) startOfWeek.clone();
        endOfWeek.add(Calendar.DAY_OF_WEEK, 6);
        endOfWeek.set(Calendar.HOUR_OF_DAY, 23);
        endOfWeek.set(Calendar.MINUTE, 59);
        endOfWeek.set(Calendar.SECOND, 59);
        endOfWeek.set(Calendar.MILLISECOND, 999);

        long startTimestamp = startOfWeek.getTimeInMillis();
        long endTimestamp = endOfWeek.getTimeInMillis();

        showLoading(true);
        repo.getStaffSchedule(staffId, startTimestamp, endTimestamp, new FirebaseRepo.FirebaseCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                showLoading(false);
                adapter.setBookings(bookings);
                updateEmptyState(bookings.isEmpty());
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Toast.makeText(StaffScheduleActivity.this, 
                    "Không thể tải lịch làm việc: " + (e != null ? e.getMessage() : "Lỗi không xác định"), 
                    Toast.LENGTH_SHORT).show();
                updateEmptyState(true);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        rvSchedule.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            rvSchedule.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvSchedule.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    private void showBookingDetail(Booking booking) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_booking_detail, null);
        bottomSheet.setContentView(bottomSheetView);

        TextView tvDetailSalonName = bottomSheetView.findViewById(R.id.tvDetailSalonName);
        TextView tvDetailServiceName = bottomSheetView.findViewById(R.id.tvDetailServiceName);
        TextView tvDetailCustomerName = bottomSheetView.findViewById(R.id.tvDetailCustomerName);
        TextView tvDetailDateTime = bottomSheetView.findViewById(R.id.tvDetailDateTime);
        Chip chipDetailStatus = bottomSheetView.findViewById(R.id.chipDetailStatus);

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

        bottomSheet.show();
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

