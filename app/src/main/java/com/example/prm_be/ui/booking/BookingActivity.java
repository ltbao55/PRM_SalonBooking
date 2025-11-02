package com.example.prm_be.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.Booking;
import com.example.prm_be.data.models.Service;
import com.example.prm_be.data.models.Stylist;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    public static final String EXTRA_SALON_ID = "salon_id";

    private RecyclerView rvServices;
    private RecyclerView rvStylists;
    private CalendarView calendarView;
    private RecyclerView rvTimeSlots;
    private TextView tvTotalPrice;
    private TextView tvServicesEmpty;
    private TextView tvStylistsEmpty;
    private Button btnConfirm;
    private MaterialToolbar toolbar;

    private FirebaseRepo repo;
    private String salonId;
    private long selectedDate;
    private String selectedStylistId;

    // Adapters
    private ServiceBookingAdapter serviceAdapter;
    private StylistBookingAdapter stylistAdapter;
    private TimeSlotAdapter timeSlotAdapter;

    // Selected items
    private Service selectedService;
    private TimeSlotAdapter.TimeSlot selectedTimeSlot;

    // Data lists
    private List<Service> serviceList;
    private List<Stylist> stylistList;

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
        selectedStylistId = null;
        serviceList = new ArrayList<>();
        stylistList = new ArrayList<>();

        initViews();
        
        // Set minimum date to today (after initViews)
        Calendar minDate = Calendar.getInstance();
        minDate.set(Calendar.HOUR_OF_DAY, 0);
        minDate.set(Calendar.MINUTE, 0);
        minDate.set(Calendar.SECOND, 0);
        minDate.set(Calendar.MILLISECOND, 0);
        calendarView.setMinDate(minDate.getTimeInMillis());
        setupAdapters();
        setupClickListeners();
        loadData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        rvServices = findViewById(R.id.rvServices);
        rvStylists = findViewById(R.id.rvStylists);
        calendarView = findViewById(R.id.calendarView);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvServicesEmpty = findViewById(R.id.tvServicesEmpty);
        tvStylistsEmpty = findViewById(R.id.tvStylistsEmpty);
        btnConfirm = findViewById(R.id.btnConfirm);
    }

    private void setupAdapters() {
        // Services adapter
        serviceAdapter = new ServiceBookingAdapter();
        serviceAdapter.setOnServiceClickListener((service, position) -> {
            selectedService = service;
            updateTotalPrice();
        });
        rvServices.setLayoutManager(new LinearLayoutManager(this));
        rvServices.setAdapter(serviceAdapter);

        // Stylists adapter
        stylistAdapter = new StylistBookingAdapter();
        stylistAdapter.setOnStylistClickListener((stylist, position, isNone) -> {
            selectedStylistId = stylist != null ? stylist.getId() : null;
            loadTimeSlots(selectedDate);
        });
        rvStylists.setLayoutManager(new LinearLayoutManager(this));
        rvStylists.setAdapter(stylistAdapter);

        // Time slots adapter
        timeSlotAdapter = new TimeSlotAdapter();
        timeSlotAdapter.setOnTimeSlotClickListener((timeSlot, position) -> {
            selectedTimeSlot = timeSlot;
            updateTotalPrice();
        });
        rvTimeSlots.setLayoutManager(new GridLayoutManager(this, 4));
        rvTimeSlots.setAdapter(timeSlotAdapter);
    }

    private void setupClickListeners() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate = calendar.getTimeInMillis();
            
            loadTimeSlots(selectedDate);
        });

        btnConfirm.setOnClickListener(v -> {
            if (validateBooking()) {
                createBooking();
            }
        });
    }

    private boolean validateBooking() {
        if (selectedService == null) {
            Toast.makeText(this, "Vui lòng chọn dịch vụ", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (selectedTimeSlot == null) {
            Toast.makeText(this, "Vui lòng chọn khung giờ", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!selectedTimeSlot.isAvailable()) {
            Toast.makeText(this, "Khung giờ này đã được đặt", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!repo.isUserLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createBooking() {
        FirebaseUser currentUser = repo.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        long bookingTimestamp = selectedTimeSlot.getTimestamp();
        long createdAt = System.currentTimeMillis();

        Booking booking = new Booking(
            null, // id will be generated by Firebase
            currentUser.getUid(),
            salonId,
            selectedService.getId(),
            selectedStylistId,
            bookingTimestamp,
            "pending",
            createdAt
        );

        btnConfirm.setEnabled(false);
        btnConfirm.setText("Đang xử lý...");

        repo.createBooking(booking, new FirebaseRepo.FirebaseCallback<String>() {
            @Override
            public void onSuccess(String bookingId) {
                // Navigate to success screen
                Intent intent = new Intent(BookingActivity.this, BookingSuccessActivity.class);
                intent.putExtra("booking_id", bookingId);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Xác Nhận Đặt Lịch");
                Toast.makeText(BookingActivity.this, 
                    "Đặt lịch thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        loadServices();
        loadStylists();
        loadTimeSlots(selectedDate);
    }

    private void loadServices() {
        repo.getServicesOfSalon(salonId, new FirebaseRepo.FirebaseCallback<List<Service>>() {
            @Override
            public void onSuccess(List<Service> services) {
                serviceList = services != null ? services : new ArrayList<>();
                serviceAdapter.setServiceList(serviceList);
                
                // Show/hide empty state
                if (serviceList.isEmpty()) {
                    rvServices.setVisibility(View.GONE);
                    tvServicesEmpty.setVisibility(View.VISIBLE);
                    tvServicesEmpty.setText("Chưa có dịch vụ nào cho salon này");
                } else {
                    rvServices.setVisibility(View.VISIBLE);
                    tvServicesEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(BookingActivity.this, 
                    "Không thể tải danh sách dịch vụ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                // Set empty list để adapter hiển thị empty state
                serviceList = new ArrayList<>();
                serviceAdapter.setServiceList(serviceList);
                rvServices.setVisibility(View.GONE);
                tvServicesEmpty.setVisibility(View.VISIBLE);
                tvServicesEmpty.setText("Lỗi khi tải dịch vụ: " + e.getMessage());
            }
        });
    }

    private void loadStylists() {
        repo.getStylistsOfSalon(salonId, new FirebaseRepo.FirebaseCallback<List<Stylist>>() {
            @Override
            public void onSuccess(List<Stylist> stylists) {
                stylistList = stylists != null ? stylists : new ArrayList<>();
                stylistAdapter.setStylistList(stylistList);
                
                // Show/hide empty state
                if (stylistList.isEmpty()) {
                    rvStylists.setVisibility(View.GONE);
                    tvStylistsEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvStylists.setVisibility(View.VISIBLE);
                    tvStylistsEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Stylist is optional, so failure is acceptable
                stylistList = new ArrayList<>();
                stylistAdapter.setStylistList(stylistList);
                rvStylists.setVisibility(View.GONE);
                tvStylistsEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadTimeSlots(long date) {
        // Calculate start and end of day
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTimestamp = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endTimestamp = calendar.getTimeInMillis();

        // Generate time slots (9:00 - 18:00, mỗi slot 60 phút)
        List<TimeSlotAdapter.TimeSlot> timeSlots = generateTimeSlots(date);

        // Load existing bookings for this date
        repo.getBookingsByStylistAndDate(
            selectedStylistId, 
            salonId, 
            startTimestamp, 
            endTimestamp,
            new FirebaseRepo.FirebaseCallback<List<Booking>>() {
                @Override
                public void onSuccess(List<Booking> bookings) {
                    // Mark slots as booked
                    List<String> bookedTimes = new ArrayList<>();
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    
                    for (Booking booking : bookings) {
                        Calendar bookingCal = Calendar.getInstance();
                        bookingCal.setTimeInMillis(booking.getTimestamp());
                        String timeStr = timeFormat.format(bookingCal.getTime());
                        bookedTimes.add(timeStr);
                    }

                    // Update availability
                    for (TimeSlotAdapter.TimeSlot slot : timeSlots) {
                        slot.isAvailable = !bookedTimes.contains(slot.getTime());
                    }

                    timeSlotAdapter.setTimeSlotList(timeSlots);
                    timeSlotAdapter.setBookedSlots(bookedTimes);
                    selectedTimeSlot = null; // Reset selection when date changes
                }

                @Override
                public void onFailure(Exception e) {
                    // Still show time slots, but mark all as available
                    timeSlotAdapter.setTimeSlotList(timeSlots);
                }
            }
        );
    }

    private List<TimeSlotAdapter.TimeSlot> generateTimeSlots(long date) {
        List<TimeSlotAdapter.TimeSlot> slots = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        // Generate slots from 9:00 to 18:00 (every hour)
        int startHour = 9;
        int endHour = 18;

        for (int hour = startHour; hour <= endHour; hour++) {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            long timestamp = calendar.getTimeInMillis();
            String timeStr = timeFormat.format(calendar.getTime());

            // Check if slot is in the past
            boolean isPast = timestamp < System.currentTimeMillis();

            TimeSlotAdapter.TimeSlot slot = new TimeSlotAdapter.TimeSlot(
                timeStr,
                timestamp,
                !isPast // Not available if in the past
            );
            slots.add(slot);
        }

        return slots;
    }

    private void updateTotalPrice() {
        long totalPrice = 0;
        if (selectedService != null) {
            totalPrice = selectedService.getPrice();
        }

        String priceText = String.format(Locale.getDefault(), "%,d đ", totalPrice);
        tvTotalPrice.setText(priceText);
    }
}
