package com.example.prm_be.ui.profile;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.Booking;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseUser;

import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class BookingHistoryActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private BookingHistoryPagerAdapter pagerAdapter;

    private FirebaseRepo repo;
    private List<Booking> allBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is regular user (block staff/admin)
        if (!com.example.prm_be.utils.RoleGuard.checkUserRoleOnlySync(this)) {
            return;
        }
        
        setContentView(R.layout.activity_booking_history);

        repo = FirebaseRepo.getInstance();
        
        // Do async role check
        com.example.prm_be.utils.RoleGuard.checkUserRoleOnly(this);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        initViews();
        setupTabs();
        loadBookingHistory();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        
        // Setup ViewPager2 with adapter
        pagerAdapter = new BookingHistoryPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
    }

    private void setupTabs() {
        // Setup TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Sắp tới" : "Đã hoàn thành");
        }).attach();
    }

    private com.google.firebase.firestore.ListenerRegistration listenerRegistration;

    private void loadBookingHistory() {
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

        String uid = currentUser.getUid();
        // Đăng ký lắng nghe realtime
        if (listenerRegistration != null) listenerRegistration.remove();
        listenerRegistration = repo.getUserBookingsListener(uid, new FirebaseRepo.FirebaseCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                allBookings = bookings;
                updateFragmentsWithBookings(bookings);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(BookingHistoryActivity.this, 
                    "Không thể tải lịch sử đặt lịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) listenerRegistration.remove();
    }

    private void updateFragmentsWithBookings(List<Booking> bookings) {
        // Update fragments through adapter
        // Get current fragments from ViewPager2
        int currentItem = viewPager.getCurrentItem();
        
        // Update fragment at position 0 (Upcoming)
        updateFragmentAtPosition(0, bookings);
        
        // Update fragment at position 1 (Completed)
        updateFragmentAtPosition(1, bookings);
    }

    private void updateFragmentAtPosition(int position, List<Booking> bookings) {
        // Get fragment at specific position using ViewPager2
        Fragment fragment = null;
        try {
            long itemId = pagerAdapter.getItemId(position);
            String tag = "f" + itemId;
            fragment = getSupportFragmentManager().findFragmentByTag(tag);
        } catch (Exception e) {
            // Fallback: try to find by position
            if (position < getSupportFragmentManager().getFragments().size()) {
                fragment = getSupportFragmentManager().getFragments().get(position);
            }
        }

        if (fragment instanceof BookingListFragment) {
            ((BookingListFragment) fragment).setBookings(bookings);
        }
    }
}