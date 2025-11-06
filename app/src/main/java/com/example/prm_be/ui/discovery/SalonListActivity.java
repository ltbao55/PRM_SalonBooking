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
 * Chức năng:
 * - Load danh sách salon từ Firebase
 * - Tìm kiếm salon theo tên/địa chỉ (filter local)
 * - Click salon để xem chi tiết
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
        
        // Check if user is regular user (block staff/admin)
        if (!com.example.prm_be.utils.RoleGuard.checkUserRoleOnlySync(this)) {
            return;
        }
        
        setContentView(R.layout.activity_salon_list);

        repo = FirebaseRepo.getInstance();

        initViews();
        setupToolbar();
        
        // Do async role check
        com.example.prm_be.utils.RoleGuard.checkUserRoleOnly(this);
        setupRecyclerView();
        setupSearchView();
        loadFromFirebase();
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
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter salon list theo query tìm kiếm
                filterSalons(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý
            }
        });
    }

    /**
     * Lọc danh sách salon theo query tìm kiếm
     * Filter local theo tên và địa chỉ salon
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
     * Load danh sách salon từ Firebase
     * Nếu không có dữ liệu thì dùng demo data để preview UI
     */
    private void loadFromFirebase() {
        repo.getAllSalons(new FirebaseRepo.FirebaseCallback<List<Salon>>() {
            @Override
            public void onSuccess(List<Salon> salons) {
                originalSalonList = salons != null ? salons : new ArrayList<>();
                if (originalSalonList.isEmpty()) {
                    originalSalonList = createDemoSalons();
                }
                displayedSalonList = new ArrayList<>(originalSalonList);
                salonAdapter.setSalonList(displayedSalonList);
                updateResultsCount();
                updateEmptyState();
            }

            @Override
            public void onFailure(Exception e) {
                originalSalonList = new ArrayList<>();
                displayedSalonList = new ArrayList<>();
                salonAdapter.setSalonList(displayedSalonList);
                updateResultsCount();
                updateEmptyState();
            }
        });
    }

    private List<Salon> createDemoSalons() {
        List<Salon> mockSalons = new ArrayList<>();
        mockSalons.add(new Salon("demo_1", "Salon Đẹp - Quận 1", "123 Nguyễn Huệ, Quận 1, TP.HCM", ""));
        mockSalons.add(new Salon("demo_2", "Beauty House", "456 Lê Lợi, Quận 3, TP.HCM", ""));
        mockSalons.add(new Salon("demo_3", "Hair Studio Pro", "789 Điện Biên Phủ, Bình Thạnh, TP.HCM", ""));
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
