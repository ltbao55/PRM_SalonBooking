package com.example.prm_be.ui.discovery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.ui.booking.BookingActivity;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class SalonDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SALON_ID = "salon_id";

    private ImageView imgSalon;
    private TextView tvSalonName;
    private TextView tvSalonAddress;
    private RecyclerView rvServices;
    private RecyclerView rvStylists;
    private ExtendedFloatingActionButton btnBook;

    private FirebaseRepo repo;
    private String salonId;

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

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Setup CollapsingToolbarLayout
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
        if (collapsingToolbar != null) {
            collapsingToolbar.setTitle("Chi tiết Salon");
            collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.white, getTheme()));
            collapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(android.R.color.white, getTheme()));
        }

        initViews();
        setupClickListeners();
        loadSalonData();
    }

    private void initViews() {
        imgSalon = findViewById(R.id.imgSalon);
        tvSalonName = findViewById(R.id.tvSalonName);
        tvSalonAddress = findViewById(R.id.tvSalonAddress);
        rvServices = findViewById(R.id.rvServices);
        rvStylists = findViewById(R.id.rvStylists);
        btnBook = findViewById(R.id.btnBook);
        
        // TODO: Setup RecyclerView adapters
    }

    private void setupClickListeners() {
        btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra(BookingActivity.EXTRA_SALON_ID, salonId);
            startActivity(intent);
        });
    }

    private void loadSalonData() {
        // TODO: Load salon, services, and stylists from FirebaseRepo
        // repo.getSalonById(salonId, ...);
        // repo.getServicesOfSalon(salonId, ...);
        // repo.getStylistsOfSalon(salonId, ...);
    }
}
