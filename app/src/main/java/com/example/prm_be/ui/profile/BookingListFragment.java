package com.example.prm_be.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.Booking;
import com.example.prm_be.data.models.Salon;
import com.example.prm_be.data.models.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị danh sách bookings trong BookingHistoryActivity
 * Có thể hiển thị "Sắp tới" hoặc "Đã hoàn thành" dựa trên filterType
 */
public class BookingListFragment extends Fragment {

    private static final String ARG_FILTER_TYPE = "filter_type";

    public static final String FILTER_UPCOMING = "upcoming";
    public static final String FILTER_COMPLETED = "completed";

    private RecyclerView rvBookings;
    private View llEmptyState;
    private BookingAdapter bookingAdapter;
    private String filterType;
    private List<Booking> allBookings;
    private FirebaseRepo repo;

    public static BookingListFragment newInstance(String filterType) {
        BookingListFragment fragment = new BookingListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER_TYPE, filterType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filterType = getArguments().getString(ARG_FILTER_TYPE, FILTER_UPCOMING);
        } else {
            filterType = FILTER_UPCOMING;
        }
        allBookings = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_list, container, false);
        
        rvBookings = view.findViewById(R.id.rvBookings);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        
        setupRecyclerView();
        repo = FirebaseRepo.getInstance();
        
        return view;
    }

    private void setupRecyclerView() {
        bookingAdapter = new BookingAdapter();
        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBookings.setAdapter(bookingAdapter);

        bookingAdapter.setOnBookingLongClickListener(booking -> {
            if (booking == null) return;
            if (!"pending".equalsIgnoreCase(booking.getStatus()) &&
                !"confirmed".equalsIgnoreCase(booking.getStatus())) {
                return; // chỉ cho hủy lịch chưa/đã xác nhận
            }
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Hủy lịch hẹn")
                    .setMessage("Bạn có chắc muốn hủy lịch này?")
                    .setNegativeButton("Không", null)
                    .setPositiveButton("Hủy lịch", (d, which) -> {
                        repo.cancelBooking(booking.getId(), new FirebaseRepo.FirebaseCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                android.widget.Toast.makeText(getContext(), "Đã hủy lịch", android.widget.Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                android.widget.Toast.makeText(getContext(), "Hủy thất bại: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .show();
        });
    }

    public void setBookings(List<Booking> bookings) {
        allBookings = bookings != null ? bookings : new ArrayList<>();
        filterBookings();
    }

    private void filterBookings() {
        if (allBookings == null) {
            allBookings = new ArrayList<>();
        }

        List<Booking> filteredBookings = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        for (Booking booking : allBookings) {
            boolean matches = false;

            if (FILTER_UPCOMING.equals(filterType)) {
                // Sắp tới: pending hoặc confirmed, và timestamp >= hiện tại
                boolean isUpcoming = booking.getTimestamp() >= currentTime;
                boolean isActive = "pending".equals(booking.getStatus()) || 
                                  "confirmed".equals(booking.getStatus());
                matches = isUpcoming && isActive;
            } else if (FILTER_COMPLETED.equals(filterType)) {
                // Đã hoàn thành: completed hoặc cancelled, hoặc timestamp < hiện tại và không phải pending
                boolean isPast = booking.getTimestamp() < currentTime;
                boolean isCompleted = "completed".equals(booking.getStatus()) || 
                                     "cancelled".equals(booking.getStatus()) ||
                                     (isPast && !"pending".equals(booking.getStatus()));
                matches = isCompleted;
            }

            if (matches) {
                filteredBookings.add(booking);
            }
        }

        bookingAdapter.setBookingList(filteredBookings);
        // Load human-friendly names for salons and services
        preloadDisplayNames(filteredBookings);
        
        // Show/hide empty state
        if (filteredBookings.isEmpty()) {
            rvBookings.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvBookings.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    private void preloadDisplayNames(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) return;

        java.util.Set<String> salonIds = new java.util.HashSet<>();
        for (Booking b : bookings) {
            if (b.getSalonId() != null) salonIds.add(b.getSalonId());
        }

        for (String salonId : salonIds) {
            repo.getSalonById(salonId, new FirebaseRepo.FirebaseCallback<Salon>() {
                @Override
                public void onSuccess(Salon salon) {
                    if (salon != null) {
                        bookingAdapter.setSalonName(salon.getId(), salon.getName());
                        // Optionally preload services names for this salon
                        repo.getServicesOfSalon(salon.getId(), new FirebaseRepo.FirebaseCallback<java.util.List<Service>>() {
                            @Override
                            public void onSuccess(java.util.List<Service> services) {
                                if (services != null) {
                                    for (Service s : services) {
                                        bookingAdapter.setServiceName(s.getId(), s.getName());
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Exception e) { /* ignore */ }
                        });
                    }
                }

                @Override
                public void onFailure(Exception e) { /* ignore */ }
            });
        }
    }

}

