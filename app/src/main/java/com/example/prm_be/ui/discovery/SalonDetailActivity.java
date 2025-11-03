package com.example.prm_be.ui.discovery;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.Salon;
import com.example.prm_be.data.models.Service;
import com.example.prm_be.data.models.Stylist;
import com.example.prm_be.ui.booking.BookingActivity;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * SalonDetailActivity - Màn hình chi tiết salon
 * Hiện tại chỉ code UI, chưa có logic kết nối BE
 */
public class SalonDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SALON_ID = "salon_id";

    private ImageView imgSalon;
    private TextView tvSalonName;
    private TextView tvSalonAddress;
    private RecyclerView rvServices;
    private RecyclerView rvStylists;
    private ExtendedFloatingActionButton btnBook;

    private ServiceDetailAdapter serviceAdapter;
    private StylistDetailAdapter stylistAdapter;
    private FirebaseRepo repo;
    private String salonId;
    private Salon currentSalon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salon_detail);

        repo = FirebaseRepo.getInstance();
        
        salonId = getIntent().getStringExtra(EXTRA_SALON_ID);
        if (salonId == null) {
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupAdapters();
        setupClickListeners();
        loadMockData(); // Tạm thời dùng mock data để preview UI
    }

    private void initViews() {
        imgSalon = findViewById(R.id.imgSalon);
        tvSalonName = findViewById(R.id.tvSalonName);
        tvSalonAddress = findViewById(R.id.tvSalonAddress);
        rvServices = findViewById(R.id.rvServices);
        rvStylists = findViewById(R.id.rvStylists);
        btnBook = findViewById(R.id.btnBook);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Setup CollapsingToolbarLayout
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
        if (collapsingToolbar != null) {
            collapsingToolbar.setTitle("Chi tiết Salon");
            ColorStateList goldColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gold));
            collapsingToolbar.setExpandedTitleTextColor(goldColor);
            collapsingToolbar.setCollapsedTitleTextColor(goldColor);
        }
    }

    private void setupAdapters() {
        // Services adapter
        serviceAdapter = new ServiceDetailAdapter();
        rvServices.setLayoutManager(new LinearLayoutManager(this));
        rvServices.setAdapter(serviceAdapter);

        // Stylists adapter
        stylistAdapter = new StylistDetailAdapter();
        rvStylists.setLayoutManager(new LinearLayoutManager(this));
        rvStylists.setAdapter(stylistAdapter);
    }

    private void setupClickListeners() {
        btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra(BookingActivity.EXTRA_SALON_ID, salonId);
            startActivity(intent);
        });
    }

    /**
     * Load mock data để preview UI
     * Sẽ được thay thế bằng FirebaseRepo sau khi có BE
     */
    private void loadMockData() {
        // Mock salon data
        currentSalon = new Salon(
            salonId,
            "Salon Luxury Premium",
            "123 Nguyễn Huệ, Quận 1, TP.HCM",
            "https://example.com/salon1.jpg"
        );

        // Update UI
        if (currentSalon != null) {
            tvSalonName.setText(currentSalon.getName());
            tvSalonAddress.setText(currentSalon.getAddress());
            
            // Update CollapsingToolbar title
            CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
            if (collapsingToolbar != null) {
                collapsingToolbar.setTitle(currentSalon.getName());
            }
            
            // Placeholder image
            imgSalon.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Mock services
        List<Service> mockServices = createMockServices();
        serviceAdapter.setServiceList(mockServices);

        // Mock stylists
        List<Stylist> mockStylists = createMockStylists();
        stylistAdapter.setStylistList(mockStylists);
    }

    private List<Service> createMockServices() {
        List<Service> services = new ArrayList<>();
        services.add(new Service("sv1", "Cắt tóc nam", 100000));
        services.add(new Service("sv2", "Cắt tóc nữ", 150000));
        services.add(new Service("sv3", "Uốn tóc", 300000));
        services.add(new Service("sv4", "Nhuộm tóc", 400000));
        services.add(new Service("sv5", "Phục hồi tóc", 250000));
        services.add(new Service("sv6", "Tạo kiểu", 200000));
        return services;
    }

    private List<Stylist> createMockStylists() {
        List<Stylist> stylists = new ArrayList<>();
        stylists.add(new Stylist("st1", "Nguyễn Văn A", salonId, "", "Chuyên cắt tóc nam"));
        stylists.add(new Stylist("st2", "Trần Thị B", salonId, "", "Chuyên nhuộm tóc"));
        stylists.add(new Stylist("st3", "Lê Văn C", salonId, "", "Chuyên tạo kiểu"));
        stylists.add(new Stylist("st4", "Phạm Thị D", salonId, "", "Chuyên uốn tóc"));
        return stylists;
    }
}
