package com.example.prm_be.ui.profile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.viewpager2.widget.ViewPager2;

public class BookingHistoryActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    private FirebaseRepo repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        repo = FirebaseRepo.getInstance();

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
        
        // TODO: Setup ViewPager2 with adapter
        // viewPager.setAdapter(new BookingHistoryPagerAdapter(this));
    }

    private void setupTabs() {
        // TODO: Setup TabLayout with ViewPager2
        // new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
        //     tab.setText(position == 0 ? "Sắp tới" : "Đã hoàn thành");
        // }).attach();
    }

    private void loadBookingHistory() {
        // TODO: Load user bookings from FirebaseRepo
        // if (repo.isUserLoggedIn()) {
        //     String uid = repo.getCurrentUser().getUid();
        //     repo.getUserBookings(uid, new FirebaseRepo.FirebaseCallback<List<Booking>>() {...});
        // }
    }
}