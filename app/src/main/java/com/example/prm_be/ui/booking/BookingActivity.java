package com.example.prm_be.ui.booking;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;

import java.util.Calendar;

public class BookingActivity extends AppCompatActivity {

    public static final String EXTRA_SALON_ID = "salon_id";

    private RecyclerView rvServices;
    private RecyclerView rvStylists;
    private CalendarView calendarView;
    private RecyclerView rvTimeSlots;
    private TextView tvTotalPrice;
    private Button btnConfirm;

    private FirebaseRepo repo;
    private String salonId;
    private long selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        repo = FirebaseRepo.getInstance();
        
        salonId = getIntent().getStringExtra(EXTRA_SALON_ID);
        if (salonId == null) {
            finish();
            return;
        }

        selectedDate = Calendar.getInstance().getTimeInMillis();

        initViews();
        setupClickListeners();
        loadData();
    }

    private void initViews() {
        rvServices = findViewById(R.id.rvServices);
        rvStylists = findViewById(R.id.rvStylists);
        calendarView = findViewById(R.id.calendarView);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnConfirm = findViewById(R.id.btnConfirm);
        
        // TODO: Setup RecyclerView adapters
    }

    private void setupClickListeners() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate = calendar.getTimeInMillis();
            
            // TODO: Reload time slots for selected date
            loadTimeSlots(selectedDate);
        });

        btnConfirm.setOnClickListener(v -> {
            // TODO: Create booking and navigate to success screen
            // repo.createBooking(booking, new FirebaseRepo.FirebaseCallback<String>() {...});
        });
    }

    private void loadData() {
        // TODO: Load services and stylists
        // repo.getServicesOfSalon(salonId, ...);
        // repo.getStylistsOfSalon(salonId, ...);
        loadTimeSlots(selectedDate);
    }

    private void loadTimeSlots(long date) {
        // TODO: Load available time slots for selected date
        // Calculate start and end of day
        // repo.getBookingsByStylistAndDate(stylistId, salonId, startTimestamp, endTimestamp, ...);
    }
}
