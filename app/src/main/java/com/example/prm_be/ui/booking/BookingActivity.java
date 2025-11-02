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
        // Check if Firebase is initialized
        if (repo == null || salonId == null || salonId.isEmpty()) {
            // Show mock data for testing
            loadMockServices();
            return;
        }
        
        repo.getServicesOfSalon(salonId, new FirebaseRepo.FirebaseCallback<List<Service>>() {
            @Override
            public void onSuccess(List<Service> services) {
                serviceList = services != null ? services : new ArrayList<>();
                
                // If no data from Firebase, use mock data for testing
                if (serviceList.isEmpty()) {
                    loadMockServices();
                    return;
                }
                
                serviceAdapter.setServiceList(serviceList);
                
                // Show RecyclerView, hide empty state
                rvServices.setVisibility(View.VISIBLE);
                tvServicesEmpty.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Exception e) {
                // On failure, use mock data for testing UI
                Toast.makeText(BookingActivity.this, 
                    "Không thể tải dịch vụ từ Firebase. Đang dùng dữ liệu mẫu.", Toast.LENGTH_LONG).show();
                loadMockServices();
            }
        });
    }
    
    private void loadMockServices() {
        // Mock data for testing UI
        serviceList = new ArrayList<>();
        serviceList.add(new Service("sv1", "Cắt tóc nam", 100000));
        serviceList.add(new Service("sv2", "Cắt tóc nữ", 150000));
        serviceList.add(new Service("sv3", "Uốn tóc", 300000));
        serviceList.add(new Service("sv4", "Nhuộm tóc", 400000));
        
        serviceAdapter.setServiceList(serviceList);
        rvServices.setVisibility(View.VISIBLE);
        tvServicesEmpty.setVisibility(View.GONE);
    }

    private void loadStylists() {
        // Check if Firebase is initialized
        if (repo == null || salonId == null || salonId.isEmpty()) {
            // Show mock data for testing
            loadMockStylists();
            return;
        }
        
        repo.getStylistsOfSalon(salonId, new FirebaseRepo.FirebaseCallback<List<Stylist>>() {
            @Override
            public void onSuccess(List<Stylist> stylists) {
                stylistList = stylists != null ? stylists : new ArrayList<>();
                
                // If no data from Firebase, use mock data for testing
                if (stylistList.isEmpty()) {
                    loadMockStylists();
                    return;
                }
                
                stylistAdapter.setStylistList(stylistList);
                
                // Show RecyclerView, hide empty state
                rvStylists.setVisibility(View.VISIBLE);
                tvStylistsEmpty.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Exception e) {
                // On failure, use mock data for testing UI
                loadMockStylists();
            }
        });
    }
    
    private void loadMockStylists() {
        // Mock data for testing UI
        stylistList = new ArrayList<>();
        stylistList.add(new Stylist("st1", "Nguyễn Văn A", salonId != null ? salonId : "salon1", "", "Haircut"));
        stylistList.add(new Stylist("st2", "Trần Thị B", salonId != null ? salonId : "salon1", "", "Coloring"));
        stylistList.add(new Stylist("st3", "Lê Văn C", salonId != null ? salonId : "salon1", "", "Styling"));
        
        stylistAdapter.setStylistList(stylistList);
        rvStylists.setVisibility(View.VISIBLE);
        tvStylistsEmpty.setVisibility(View.GONE);
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

        // Always show time slots first (for better UX)
        timeSlotAdapter.setTimeSlotList(timeSlots);

        // Load existing bookings for this date (if Firebase available)
        if (repo != null && salonId != null && !salonId.isEmpty()) {
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
                        
                        if (bookings != null) {
                            for (Booking booking : bookings) {
                                Calendar bookingCal = Calendar.getInstance();
                                bookingCal.setTimeInMillis(booking.getTimestamp());
                                String timeStr = timeFormat.format(bookingCal.getTime());
                                bookedTimes.add(timeStr);
                            }
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
                        // Time slots already set above
                    }
                }
            );
        } else {
            // No Firebase, just show all slots as available
            selectedTimeSlot = null;
        }
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
