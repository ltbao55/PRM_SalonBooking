package com.example.prm_be.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import com.example.prm_be.ui.auth.LoginActivity;
import com.example.prm_be.ui.profile.ProfileActivity;
import com.example.prm_be.utils.RoleGuard;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
    
    // For loading schedule
    private String currentStylistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check staff role
        if (!RoleGuard.checkRoleSync(this, "staff")) {
            return;
        }
        
        setContentView(R.layout.activity_staff_schedule);

        repo = FirebaseRepo.getInstance();
        currentWeek = Calendar.getInstance(Locale.getDefault());
        weekFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        initViews();
        setupRecyclerView();
        setupToolbar();
        setupWeekNavigation();
        setupLogoutButton();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_staff, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            startActivity(new android.content.Intent(this, StaffProfileActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_services) {
            startActivity(new android.content.Intent(this, StaffServicesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupLogoutButton() {
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                FirebaseRepo.getInstance().logout();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
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

        // Lấy User document để lấy stylistId
        repo.getUser(currentUser.getUid(), new FirebaseRepo.FirebaseCallback<com.example.prm_be.data.models.User>() {
            @Override
            public void onSuccess(com.example.prm_be.data.models.User user) {
                String stylistId = user.getStylistId();
                if (stylistId == null || stylistId.isEmpty()) {
                    Toast.makeText(StaffScheduleActivity.this, 
                        "Tài khoản staff chưa được liên kết với stylist. Vui lòng liên hệ admin.", 
                        Toast.LENGTH_LONG).show();
                    updateEmptyState(true);
                    return;
                }
                
                currentStylistId = stylistId;
                loadScheduleByStylistId(stylistId);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(StaffScheduleActivity.this, 
                    "Không thể lấy thông tin user: " + (e != null ? e.getMessage() : "unknown"), 
                    Toast.LENGTH_SHORT).show();
                updateEmptyState(true);
            }
        });
    }

    private void loadScheduleByStylistId(String stylistId) {
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
        repo.getStaffSchedule(stylistId, startTimestamp, endTimestamp, new FirebaseRepo.FirebaseCallback<List<Booking>>() {
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
        LinearLayout llActionButtons = bottomSheetView.findViewById(R.id.llActionButtons);
        MaterialButton btnStartBooking = bottomSheetView.findViewById(R.id.btnStartBooking);
        MaterialButton btnCompleteBooking = bottomSheetView.findViewById(R.id.btnCompleteBooking);
        MaterialButton btnCancelBooking = bottomSheetView.findViewById(R.id.btnCancelBooking);

        // Format date/time
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
        String dateTime = dateTimeFormat.format(new java.util.Date(booking.getTimestamp()));
        tvDetailDateTime.setText(dateTime);

        // Status
        String status = booking.getStatus();
        if (status == null) {
            status = "pending";
        }
        chipDetailStatus.setText(getStatusText(status));
        chipDetailStatus.setChipBackgroundColorResource(getStatusColor(status));

        // Show/hide action buttons based on status
        llActionButtons.setVisibility(View.VISIBLE);
        btnStartBooking.setVisibility(View.GONE);
        btnCompleteBooking.setVisibility(View.GONE);
        btnCancelBooking.setVisibility(View.GONE);

        // Action buttons logic
        switch (status.toLowerCase()) {
            case "pending":
                // Pending: Can confirm (start) or cancel
                btnStartBooking.setVisibility(View.VISIBLE);
                btnStartBooking.setText("Xác nhận");
                btnCancelBooking.setVisibility(View.VISIBLE);
                break;
            case "confirmed":
                // Confirmed: Can complete or cancel
                btnCompleteBooking.setVisibility(View.VISIBLE);
                btnCancelBooking.setVisibility(View.VISIBLE);
                break;
            case "completed":
            case "cancelled":
                // Completed/Cancelled: No actions available
                llActionButtons.setVisibility(View.GONE);
                break;
        }

        // Button listeners
        btnStartBooking.setOnClickListener(v -> {
            updateBookingStatus(booking.getId(), "confirmed", bottomSheet);
        });

        btnCompleteBooking.setOnClickListener(v -> {
            updateBookingStatus(booking.getId(), "completed", bottomSheet);
        });

        btnCancelBooking.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hủy lịch hẹn")
                .setMessage("Bạn có chắc chắn muốn hủy lịch hẹn này?")
                .setPositiveButton("Hủy", (d, w) -> {
                    updateBookingStatus(booking.getId(), "cancelled", bottomSheet);
                })
                .setNegativeButton("Không", null)
                .show();
        });

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

    private void updateBookingStatus(String bookingId, String newStatus, BottomSheetDialog bottomSheet) {
        repo.updateBookingStatus(bookingId, newStatus, new FirebaseRepo.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(StaffScheduleActivity.this, "Đã cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
                bottomSheet.dismiss();
                loadSchedule(); // Reload schedule
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(StaffScheduleActivity.this,
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

