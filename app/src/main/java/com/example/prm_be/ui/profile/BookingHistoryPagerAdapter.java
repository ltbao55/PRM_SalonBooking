package com.example.prm_be.ui.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * ViewPager2 Adapter cho BookingHistoryActivity
 * Hiển thị 2 tabs: "Sắp tới" và "Đã hoàn thành"
 */
public class BookingHistoryPagerAdapter extends FragmentStateAdapter {

    // Lưu reference fragment theo position để Activity có thể cập nhật dữ liệu chắc chắn
    private final java.util.Map<Integer, Fragment> positionToFragment = new java.util.HashMap<>();

    public BookingHistoryPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        if (position == 0) {
            fragment = BookingListFragment.newInstance(BookingListFragment.FILTER_UPCOMING);
        } else {
            fragment = BookingListFragment.newInstance(BookingListFragment.FILTER_COMPLETED);
        }
        positionToFragment.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2; // 2 tabs
    }

    @Nullable
    public Fragment getFragmentAt(int position) {
        return positionToFragment.get(position);
    }
}

