package com.example.prm_be.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.Booking;
import com.example.prm_be.data.models.Salon;
import com.example.prm_be.ui.discovery.HomeActivity;
import com.example.prm_be.ui.profile.BookingHistoryActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BookingSuccessActivity extends AppCompatActivity {

    private Button btnBackToHome;
    private Button btnViewHistory;
    private TextView tvServiceName;
    private TextView tvDateTime;
    private TextView tvSalonName;

    private FirebaseRepo repo;
    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is regular user (block staff/admin)
        if (!com.example.prm_be.utils.RoleGuard.checkUserRoleOnlySync(this)) {
            return;
        }
        
        setContentView(R.layout.activity_booking_success);

        repo = FirebaseRepo.getInstance();
        bookingId = getIntent().getStringExtra("booking_id");

        initViews();
        setupClickListeners();
        loadBookingDetails();
        
        // Do async role check
        com.example.prm_be.utils.RoleGuard.checkUserRoleOnly(this);
    }

    private void initViews() {
        btnBackToHome = findViewById(R.id.btnBackToHome);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        tvServiceName = findViewById(R.id.tvServiceName);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvSalonName = findViewById(R.id.tvSalonName);
    }

    private void setupClickListeners() {
        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        if (btnViewHistory != null) {
            btnViewHistory.setOnClickListener(v -> {
                startActivity(new Intent(this, BookingHistoryActivity.class));
                finish();
            });
        }
    }

    private void loadBookingDetails() {
        if (bookingId == null || bookingId.isEmpty()) {
            return;
        }

        repo.getBookingById(bookingId, new FirebaseRepo.FirebaseCallback<Booking>() {
            @Override
            public void onSuccess(Booking booking) {
                if (booking == null) return;

                // Time format
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(booking.getTimestamp());
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
                tvDateTime.setText("Thời gian: " + dateTimeFormat.format(calendar.getTime()));

                // Service (hiển thị ID nếu chưa có name)
                tvServiceName.setText("Dịch vụ: " + (booking.getServiceId() != null ? booking.getServiceId() : "--"));

                // Salon name
                repo.getSalonById(booking.getSalonId(), new FirebaseRepo.FirebaseCallback<Salon>() {
                    @Override
                    public void onSuccess(Salon salon) {
                        if (salon != null) {
                            tvSalonName.setText("Salon: " + salon.getName());
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // ignore, keep default
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                // ignore
            }
        });
    }
}
