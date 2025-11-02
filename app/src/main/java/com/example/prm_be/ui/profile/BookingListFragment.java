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
import com.example.prm_be.data.models.Booking;

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
        
        return view;
    }

    private void setupRecyclerView() {
        bookingAdapter = new BookingAdapter();
        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBookings.setAdapter(bookingAdapter);
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
        
        // Show/hide empty state
        if (filteredBookings.isEmpty()) {
            rvBookings.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvBookings.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

}

