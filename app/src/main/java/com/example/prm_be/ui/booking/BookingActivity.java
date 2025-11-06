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
import android.util.Log;

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
    private List<Service> selectedServices; // Danh sách dịch vụ đã chọn
    private TimeSlotAdapter.TimeSlot selectedTimeSlot;

    // Data lists
    private List<Service> serviceList;
    private List<Stylist> stylistList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is regular user (block staff/admin)
        if (!com.example.prm_be.utils.RoleGuard.checkUserRoleOnlySync(this)) {
            return;
        }
        
        setContentView(R.layout.activity_booking);
        
        // Do async role check
        com.example.prm_be.utils.RoleGuard.checkUserRoleOnly(this);

        repo = FirebaseRepo.getInstance();
        
        salonId = getIntent().getStringExtra(EXTRA_SALON_ID);
        if (salonId == null) {
            finish();
            return;
        }

        selectedDate = Calendar.getInstance().getTimeInMillis();
        selectedStylistId = null;
        selectedServices = new ArrayList<>();
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
        serviceAdapter.setOnServiceClickListener((service, position, isSelected) -> {
            selectedServices = serviceAdapter.getSelectedServices();
            updateTotalPrice();
            // Reload time slots vì duration có thể thay đổi
            loadTimeSlots(selectedDate);
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

    /**
     * Kiểm tra tính hợp lệ của booking trước khi tạo
     * - Phải chọn ít nhất 1 dịch vụ
     * - Phải chọn khung giờ
     * - Khung giờ phải còn trống
     * - Không cho đặt lịch ở quá khứ
     * - Khung giờ phải đủ thời gian cho tất cả dịch vụ
     */
    private boolean validateBooking() {
        if (selectedServices == null || selectedServices.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một dịch vụ", Toast.LENGTH_SHORT).show();
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

        // Không cho đặt lịch ở quá khứ
        long now = System.currentTimeMillis();
        if (selectedTimeSlot.getTimestamp() < now) {
            Toast.makeText(this, "Không thể đặt lịch ở thời điểm đã qua", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Kiểm tra khung giờ có đủ thời gian cho tất cả dịch vụ không
        int totalDuration = calculateTotalDuration();
        if (!hasEnoughSlots(selectedTimeSlot, totalDuration)) {
            Toast.makeText(this, "Khung giờ này không đủ thời gian cho các dịch vụ đã chọn", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!repo.isUserLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private int calculateTotalDuration() {
        int total = 0;
        for (Service service : selectedServices) {
            total += service.getDurationInMinutes();
        }
        return total;
    }

    private boolean hasEnoughSlots(TimeSlotAdapter.TimeSlot startSlot, int durationMinutes) {
        // Check if there are enough consecutive available slots
        long startTime = startSlot.getTimestamp();
        long endTime = startTime + (durationMinutes * 60 * 1000L);
        
        // Check if all slots from start to end are available
        for (TimeSlotAdapter.TimeSlot slot : timeSlotAdapter.getTimeSlotList()) {
            long slotStart = slot.getTimestamp();
            long slotEnd = slotStart + (getSlotDuration() * 60 * 1000L);
            
            // If slot overlaps with booking time range
            if (slotStart < endTime && slotEnd > startTime) {
                if (!slot.isAvailable()) {
                    return false;
                }
            }
        }
        return true;
    }

    private int getSlotDuration() {
        // Slot duration cố định là 60 phút (1 giờ - step của time slots)
        return 60;
    }

    /**
     * Tạo booking mới trong Firebase
     * - Tính tổng thời gian và thời gian kết thúc
     * - Tạo Booking object với status "confirmed"
     * - Lưu vào Firebase và chuyển đến màn hình thành công
     */
    private void createBooking() {
        FirebaseUser currentUser = repo.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        long bookingTimestamp = selectedTimeSlot.getTimestamp();
        long createdAt = System.currentTimeMillis();
        
        // Tính tổng thời gian và thời gian kết thúc
        int totalDuration = calculateTotalDuration();
        long endTime = bookingTimestamp + (totalDuration * 60 * 1000L);
        
        // Lấy danh sách ID của các dịch vụ đã chọn
        List<String> serviceIds = new ArrayList<>();
        for (Service service : selectedServices) {
            serviceIds.add(service.getId());
        }

        // Tạo Booking object - status mặc định là "confirmed"
        Booking booking = new Booking(
            null, // id sẽ được Firebase tự động tạo
            currentUser.getUid(),
            salonId,
            serviceIds,
            selectedStylistId,
            bookingTimestamp,
            endTime,
            "confirmed", // Mặc định là confirmed khi user đặt lịch
            createdAt
        );

        btnConfirm.setEnabled(false);
        btnConfirm.setText("Đang xử lý...");

        // Lưu booking vào Firebase
        repo.createBooking(booking, new FirebaseRepo.FirebaseCallback<String>() {
            @Override
            public void onSuccess(String bookingId) {
                // Đặt lịch thành công -> Chuyển đến màn hình thành công
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
        Log.d("BookingDebug", "loadTimeSlots called - salonId=" + salonId + 
                ", stylistId=" + (selectedStylistId == null ? "<none>" : selectedStylistId));
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

        // Generate time slots (9:00 - 18:00) với step 1 giờ
        // Booking có thể chiếm nhiều slot liên tiếp nếu duration dài
        List<TimeSlotAdapter.TimeSlot> timeSlots = generateTimeSlots(date, 60);
        // Debug: log all slot times generated
        SimpleDateFormat dbgFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm z", Locale.getDefault());
        for (TimeSlotAdapter.TimeSlot s : timeSlots) {
            Log.d("BookingDebug", "Slot -> " + s.getTime() + " | ts=" + s.getTimestamp() + " | " + dbgFormat.format(new java.util.Date(s.getTimestamp())));
        }

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
                        Log.d("BookingDebug", "Bookings fetched: count=" + (bookings == null ? 0 : bookings.size()));
                        if (bookings != null) {
                            for (Booking b : bookings) {
                                Log.d("BookingDebug", "Booking -> id=" + b.getId() + 
                                        ", stylistId=" + b.getStylistId() +
                                        ", ts=" + b.getTimestamp() + 
                                        ", time=" + dbgFormat.format(new java.util.Date(b.getTimestamp())) +
                                        ", status=" + b.getStatus());
                            }
                        }
                        // Đánh dấu slot đã đặt - check overlap với bookings
                        // Nếu đã chọn stylist, chỉ check bookings của stylist đó
                        // Nếu chưa chọn stylist, check tất cả bookings của salon
                        // Slot duration là 60 phút (1 giờ)
                        markBookedSlots(timeSlots, bookings, 60);
                        
                        timeSlotAdapter.setTimeSlotList(timeSlots);
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

    /**
     * Đánh dấu các slot đã được đặt dựa trên danh sách bookings
     * Logic: Một slot được coi là đã đặt nếu có booking overlap với slot đó
     * - Overlap: booking [startTime, endTime) overlap với slot [slotStart, slotStart + duration)
     * - Slot ở quá khứ luôn unavailable
     */
    private void markBookedSlots(List<TimeSlotAdapter.TimeSlot> timeSlots, List<Booking> bookings, int slotDurationMinutes) {
        if (bookings == null || bookings.isEmpty()) {
            // Không có booking nào, tất cả slot đều available
            Log.d("BookingDebug", "No bookings found, all slots available");
            for (TimeSlotAdapter.TimeSlot slot : timeSlots) {
                slot.setAvailable(true);
            }
            return;
        }

        Log.d("BookingDebug", "Marking booked slots - total slots: " + timeSlots.size() + ", total bookings: " + bookings.size());
        
        // Với mỗi slot, check xem có booking nào overlap không
        long now = System.currentTimeMillis();
        for (TimeSlotAdapter.TimeSlot slot : timeSlots) {
            long slotStartTime = slot.getTimestamp();
            long slotEndTime = slotStartTime + (slotDurationMinutes * 60 * 1000L); // Convert minutes to milliseconds
            
            // Giữ nguyên trạng thái unavailable cho các slot ở quá khứ
            if (slotStartTime < now) {
                slot.setAvailable(false);
                continue;
            }

            boolean isBooked = false;
            Booking matchedBooking = null;
            
            for (Booking booking : bookings) {
                long bookingStartTime = booking.getTimestamp();
                long bookingEndTime = booking.getEndTime(); // Sử dụng endTime từ booking
                
                // Check overlap: booking [start, end) overlap với slot [slotStart, slotEnd)
                if (isTimeSlotOverlap(slotStartTime, slotEndTime, bookingStartTime, bookingEndTime)) {
                    isBooked = true;
                    matchedBooking = booking;
                    break;
                }
            }
            
            slot.setAvailable(!isBooked);
            
            // Log để debug
            if (isBooked && matchedBooking != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Log.d("BookingDebug", "Slot " + slot.getTime() + " marked as BOOKED by booking " + matchedBooking.getId() + 
                    " (booking: " + timeFormat.format(new java.util.Date(matchedBooking.getTimestamp())) + 
                    " - " + timeFormat.format(new java.util.Date(matchedBooking.getEndTime())) + ")");
            }
        }
        
        // Log summary
        int bookedCount = 0;
        for (TimeSlotAdapter.TimeSlot slot : timeSlots) {
            if (!slot.isAvailable()) bookedCount++;
        }
        Log.d("BookingDebug", "Summary: " + bookedCount + " slots marked as booked out of " + timeSlots.size());
    }

    /**
     * Kiểm tra xem booking có overlap với slot không
     * Slot: [slotStart, slotEnd)
     * Booking: [bookingStart, bookingEnd)
     * Overlap nếu: hai khoảng thời gian có phần chung
     */
    private boolean isTimeSlotOverlap(long slotStartTime, long slotEndTime, long bookingStartTime, long bookingEndTime) {
        // Overlap nếu: bookingStart < slotEnd && bookingEnd > slotStart
        return bookingStartTime < slotEndTime && bookingEndTime > slotStartTime;
    }

    private List<TimeSlotAdapter.TimeSlot> generateTimeSlots(long date, int slotStepMinutes) {
        List<TimeSlotAdapter.TimeSlot> slots = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        // Generate slots from 9:00 to 18:00 với step 1 giờ
        int startHour = 9;
        int endHour = 18;
        int stepMinutes = 60; // Step 1 giờ để tạo các slot

        calendar.set(Calendar.HOUR_OF_DAY, startHour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(date);
        endCal.set(Calendar.HOUR_OF_DAY, endHour);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);

        while (calendar.before(endCal) || calendar.equals(endCal)) {
            long timestamp = calendar.getTimeInMillis();
            String timeStr = timeFormat.format(calendar.getTime());
            boolean isPast = timestamp < System.currentTimeMillis();

            slots.add(new TimeSlotAdapter.TimeSlot(timeStr, timestamp, !isPast));

            calendar.add(Calendar.MINUTE, stepMinutes);
        }

        return slots;
    }

    private void updateTotalPrice() {
        long totalPrice = 0;
        int totalDuration = 0;
        
        for (Service service : selectedServices) {
            totalPrice += service.getPrice();
            totalDuration += service.getDurationInMinutes();
        }

        // Format price
        String priceText = String.format(Locale.getDefault(), "%,d đ", totalPrice);
        
        // Add duration info if services selected
        if (totalDuration > 0) {
            int hours = totalDuration / 60;
            int minutes = totalDuration % 60;
            String durationText = "";
            if (hours > 0) {
                durationText = hours + " giờ ";
            }
            if (minutes > 0) {
                durationText += minutes + " phút";
            }
            priceText += " (" + durationText.trim() + ")";
        }
        
        tvTotalPrice.setText(priceText);
        
        // Show end time if time slot selected
        if (selectedTimeSlot != null && totalDuration > 0) {
            long endTime = selectedTimeSlot.getTimestamp() + (totalDuration * 60 * 1000L);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String endTimeText = timeFormat.format(new java.util.Date(endTime));
            // Could add a TextView to show end time if needed
        }
    }
}
