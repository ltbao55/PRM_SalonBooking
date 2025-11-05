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
import com.example.prm_be.data.models.Booking;
import com.example.prm_be.utils.RoleGuard;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdminReportsActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private TextView tvTotalBookings;
    private TextView tvConfirmedBookings;
    private TextView tvPendingBookings;
    private TextView tvCancelledBookings;
    private ProgressBar progressBar;

    private FirebaseRepo repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check admin role
        if (!RoleGuard.checkRoleSync(this, "admin")) {
            return;
        }
        
        setContentView(R.layout.activity_admin_reports);

        repo = FirebaseRepo.getInstance();
        
        initViews();
        setupToolbar();
        loadReports();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTotalBookings = findViewById(R.id.tvTotalBookings);
        tvConfirmedBookings = findViewById(R.id.tvConfirmedBookings);
        tvPendingBookings = findViewById(R.id.tvPendingBookings);
        tvCancelledBookings = findViewById(R.id.tvCancelledBookings);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Báo cáo");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadReports() {
        progressBar.setVisibility(View.VISIBLE);
        repo.getAllBookings(new FirebaseRepo.FirebaseCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                progressBar.setVisibility(View.GONE);
                
                int total = bookings.size();
                int confirmed = 0;
                int pending = 0;
                int cancelled = 0;
                
                for (Booking booking : bookings) {
                    String status = booking.getStatus();
                    if (status == null) continue;
                    
                    switch (status.toLowerCase()) {
                        case "confirmed":
                            confirmed++;
                            break;
                        case "pending":
                            pending++;
                            break;
                        case "cancelled":
                            cancelled++;
                            break;
                    }
                }
                
                tvTotalBookings.setText(String.valueOf(total));
                tvConfirmedBookings.setText(String.valueOf(confirmed));
                tvPendingBookings.setText(String.valueOf(pending));
                tvCancelledBookings.setText(String.valueOf(cancelled));
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminReportsActivity.this,
                    "Không thể tải báo cáo: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
}

