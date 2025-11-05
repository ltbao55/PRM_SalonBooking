package com.example.prm_be.ui.discovery;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private TextView tvViewAll;
    private SalonAdapter salonAdapter;
    private List<Salon> originalSalonList; // Danh sách gốc (để filter)
    private List<Salon> displayedSalonList; // Danh sách hiển thị

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is regular user (block staff/admin)
        if (!com.example.prm_be.utils.RoleGuard.checkUserRoleOnlySync(this)) {
            return;
        }
        
        setContentView(R.layout.activity_home);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearchView();
        setupViewAllButton();
        loadSalonsFromFirebase();
        
        // Do async role check
        com.example.prm_be.utils.RoleGuard.checkUserRoleOnly(this);
    }
    
    private void setupViewAllButton() {
        // Setup click listener cho "Xem tất cả"
        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, SalonListActivity.class);
                startActivity(intent);
            });
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvSalons = findViewById(R.id.rvSalons);
        edtSearch = findViewById(R.id.edtSearch);
        llEmptyState = findViewById(R.id.llEmptyState);
        tvViewAll = findViewById(R.id.tvViewAll);
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
        // Click vào search box để navigate đến SalonListActivity
        edtSearch.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SalonListActivity.class);
            startActivity(intent);
        });
        
        // Focus vào search box cũng navigate
        edtSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Intent intent = new Intent(HomeActivity.this, SalonListActivity.class);
                startActivity(intent);
                edtSearch.clearFocus();
            }
        });
        
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
    private void loadSalonsFromFirebase() {
        com.example.prm_be.data.FirebaseRepo repo = com.example.prm_be.data.FirebaseRepo.getInstance();
        repo.getAllSalons(new com.example.prm_be.data.FirebaseRepo.FirebaseCallback<List<Salon>>() {
            @Override
            public void onSuccess(List<Salon> salons) {
                originalSalonList = salons != null ? salons : new ArrayList<>();
                // Fallback demo data để preview UI nếu Firestore chưa có dữ liệu
                if (originalSalonList.isEmpty()) {
                    originalSalonList = createDemoSalons();
                }
                displayedSalonList = new ArrayList<>(originalSalonList);
                salonAdapter.setSalonList(displayedSalonList);
                updateEmptyState();
            }

            @Override
            public void onFailure(Exception e) {
                originalSalonList = new ArrayList<>();
                displayedSalonList = new ArrayList<>();
                salonAdapter.setSalonList(displayedSalonList);
                updateEmptyState();
            }
        });
    }

    // Demo data dùng khi Firestore trống để người dùng có trải nghiệm UI
    private List<Salon> createDemoSalons() {
        List<Salon> mockSalons = new ArrayList<>();
        mockSalons.add(new Salon("demo_1", "Salon Đẹp - Quận 1", "123 Nguyễn Huệ, Quận 1, TP.HCM", ""));
        mockSalons.add(new Salon("demo_2", "Beauty House", "456 Lê Lợi, Quận 3, TP.HCM", ""));
        mockSalons.add(new Salon("demo_3", "Hair Studio Pro", "789 Điện Biên Phủ, Bình Thạnh, TP.HCM", ""));
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
