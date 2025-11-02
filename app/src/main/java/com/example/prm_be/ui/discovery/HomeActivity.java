package com.example.prm_be.ui.discovery;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.models.Salon;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeActivity - Màn hình chính hiển thị danh sách salon
 * Hiện tại chỉ code UI, chưa có logic kết nối BE
 */
public class HomeActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvSalons;
    private TextInputEditText edtSearch;
    private LinearLayout llEmptyState;
    private SalonAdapter salonAdapter;
    private List<Salon> originalSalonList; // Danh sách gốc (để filter)
    private List<Salon> displayedSalonList; // Danh sách hiển thị

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearchView();
        loadMockData(); // Tạm thời dùng mock data để preview UI
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvSalons = findViewById(R.id.rvSalons);
        edtSearch = findViewById(R.id.edtSearch);
        llEmptyState = findViewById(R.id.llEmptyState);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Luxury Salon");
        }
    }

    private void setupRecyclerView() {
        salonAdapter = new SalonAdapter();
        salonAdapter.setOnSalonClickListener(salon -> {
            // Navigate to SalonDetailActivity
            Intent intent = new Intent(HomeActivity.this, SalonDetailActivity.class);
            intent.putExtra(SalonDetailActivity.EXTRA_SALON_ID, salon.getId());
            startActivity(intent);
        });

        rvSalons.setLayoutManager(new LinearLayoutManager(this));
        rvSalons.setAdapter(salonAdapter);
    }

    private void setupSearchView() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Chưa có logic search, sẽ implement sau khi có BE
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Chưa có logic search, sẽ implement sau khi có BE
                // TODO: Filter salon list based on search query
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Chưa có logic search, sẽ implement sau khi có BE
            }
        });
    }

    /**
     * Load mock data để preview UI
     * Sẽ được thay thế bằng FirebaseRepo.getAllSalons() sau khi có BE
     */
    private void loadMockData() {
        originalSalonList = createMockSalons();
        displayedSalonList = new ArrayList<>(originalSalonList);
        salonAdapter.setSalonList(displayedSalonList);
        updateEmptyState();
    }

    /**
     * Tạo mock data để preview
     */
    private List<Salon> createMockSalons() {
        List<Salon> mockSalons = new ArrayList<>();
        
        // Mock Salon 1
        Salon salon1 = new Salon(
            "salon_1",
            "Salon Đẹp - Quận 1",
            "123 Nguyễn Huệ, Quận 1, TP.HCM",
            "https://example.com/salon1.jpg"
        );
        mockSalons.add(salon1);

        // Mock Salon 2
        Salon salon2 = new Salon(
            "salon_2",
            "Beauty House",
            "456 Lê Lợi, Quận 3, TP.HCM",
            "https://example.com/salon2.jpg"
        );
        mockSalons.add(salon2);

        // Mock Salon 3
        Salon salon3 = new Salon(
            "salon_3",
            "Hair Studio Pro",
            "789 Điện Biên Phủ, Bình Thạnh, TP.HCM",
            "https://example.com/salon3.jpg"
        );
        mockSalons.add(salon3);

        // Mock Salon 4
        Salon salon4 = new Salon(
            "salon_4",
            "Style & Cut",
            "321 Hoàng Văn Thụ, Phú Nhuận, TP.HCM",
            "https://example.com/salon4.jpg"
        );
        mockSalons.add(salon4);

        return mockSalons;
    }

    /**
     * Cập nhật trạng thái empty state
     */
    private void updateEmptyState() {
        if (displayedSalonList.isEmpty()) {
            rvSalons.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvSalons.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_profile) {
            startActivity(new Intent(this, com.example.prm_be.ui.profile.ProfileActivity.class));
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}
