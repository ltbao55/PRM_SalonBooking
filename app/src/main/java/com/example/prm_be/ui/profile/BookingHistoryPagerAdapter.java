package com.example.prm_be.ui.profile;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * ViewPager2 Adapter cho BookingHistoryActivity
 * Hiển thị 2 tabs: "Sắp tới" và "Đã hoàn thành"
 */
public class BookingHistoryPagerAdapter extends FragmentStateAdapter {

    public BookingHistoryPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            // Tab 1: Sắp tới
            return BookingListFragment.newInstance(BookingListFragment.FILTER_UPCOMING);
        } else {
            // Tab 2: Đã hoàn thành
            return BookingListFragment.newInstance(BookingListFragment.FILTER_COMPLETED);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // 2 tabs
    }
}

