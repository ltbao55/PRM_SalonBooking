package com.example.prm_be.ui.discovery;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.Salon;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * SalonListActivity - Màn hình danh sách salon với tìm kiếm
 * Hiện tại chỉ code UI, chưa có logic kết nối BE
 */
public class SalonListActivity extends AppCompatActivity {

    private RecyclerView rvSalons;
    private TextInputEditText edtSearch;
    private TextView tvResultsCount;
    private LinearLayout llEmptyState;
    private MaterialToolbar toolbar;
    
    private SalonAdapter salonAdapter;
    private FirebaseRepo repo;
    
    private List<Salon> originalSalonList; // Danh sách gốc
    private List<Salon> displayedSalonList; // Danh sách hiển thị (sau khi filter)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salon_list);

        repo = FirebaseRepo.getInstance();

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
        tvResultsCount = findViewById(R.id.tvResultsCount);
        llEmptyState = findViewById(R.id.llEmptyState);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tìm Salon");
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        salonAdapter = new SalonAdapter();
        salonAdapter.setOnSalonClickListener(salon -> {
            // Navigate to SalonDetailActivity
            Intent intent = new Intent(SalonListActivity.this, SalonDetailActivity.class);
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
                // Filter salon list based on search query
                filterSalons(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Chưa có logic search, sẽ implement sau khi có BE
            }
        });
    }

    /**
     * Filter salon list based on search query
     * Hiện tại chỉ filter local, sẽ kết nối với BE sau
     */
    private void filterSalons(String query) {
        if (query == null || query.trim().isEmpty()) {
            displayedSalonList = new ArrayList<>(originalSalonList);
        } else {
            displayedSalonList = new ArrayList<>();
            String searchQuery = query.toLowerCase().trim();
            
            for (Salon salon : originalSalonList) {
                if (salon.getName() != null && salon.getName().toLowerCase().contains(searchQuery) ||
                    salon.getAddress() != null && salon.getAddress().toLowerCase().contains(searchQuery)) {
                    displayedSalonList.add(salon);
                }
            }
        }
        
        salonAdapter.setSalonList(displayedSalonList);
        updateResultsCount();
        updateEmptyState();
    }

    /**
     * Load mock data để preview UI
     * Sẽ được thay thế bằng FirebaseRepo.getAllSalons() sau khi có BE
     */
    private void loadMockData() {
        originalSalonList = createMockSalons();
        displayedSalonList = new ArrayList<>(originalSalonList);
        salonAdapter.setSalonList(displayedSalonList);
        updateResultsCount();
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

        // Mock Salon 5
        Salon salon5 = new Salon(
            "salon_5",
            "Premium Hair Salon",
            "555 Nguyễn Trãi, Quận 5, TP.HCM",
            "https://example.com/salon5.jpg"
        );
        mockSalons.add(salon5);

        // Mock Salon 6
        Salon salon6 = new Salon(
            "salon_6",
            "Elite Beauty Center",
            "888 Võ Văn Tần, Quận 10, TP.HCM",
            "https://example.com/salon6.jpg"
        );
        mockSalons.add(salon6);

        return mockSalons;
    }

    /**
     * Cập nhật số lượng kết quả
     */
    private void updateResultsCount() {
        if (tvResultsCount != null) {
            int count = displayedSalonList.size();
            if (count > 0) {
                tvResultsCount.setText(String.format("Tìm thấy %d salon", count));
            } else {
                tvResultsCount.setText("Kết quả tìm kiếm");
            }
        }
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
}
