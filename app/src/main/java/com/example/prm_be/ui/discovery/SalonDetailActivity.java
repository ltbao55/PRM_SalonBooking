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
 * Chức năng:
 * - Load thông tin salon từ Firebase
 * - Hiển thị danh sách services và stylists của salon
 * - Chuyển đến BookingActivity để đặt lịch
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
        
        // Check if user is regular user (block staff/admin)
        if (!com.example.prm_be.utils.RoleGuard.checkUserRoleOnlySync(this)) {
            return;
        }
        
        setContentView(R.layout.activity_salon_detail);

        repo = FirebaseRepo.getInstance();
        
        // Do async role check
        com.example.prm_be.utils.RoleGuard.checkUserRoleOnly(this);
        
        salonId = getIntent().getStringExtra(EXTRA_SALON_ID);
        if (salonId == null || salonId.isEmpty()) {
            android.widget.Toast.makeText(this, "Thiếu salonId", android.widget.Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupAdapters();
        setupClickListeners();
        loadDataFromFirebase();
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

    private void loadDataFromFirebase() {
        // Load salon meta
        repo.getSalonById(salonId, new FirebaseRepo.FirebaseCallback<Salon>() {
            @Override
            public void onSuccess(Salon salon) {
                currentSalon = salon;
                if (salon != null) {
                    tvSalonName.setText(salon.getName());
                    tvSalonAddress.setText(salon.getAddress());
                    CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
                    if (collapsingToolbar != null) {
                        collapsingToolbar.setTitle(salon.getName());
                    }
                    // Load image (use Glide if available)
                    if (salon.getImageUrl() != null && !salon.getImageUrl().isEmpty()) {
                        try {
                            com.bumptech.glide.Glide.with(SalonDetailActivity.this)
                                    .load(salon.getImageUrl())
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .centerCrop()
                                    .into(imgSalon);
                        } catch (Throwable t) {
                            imgSalon.setImageResource(android.R.drawable.ic_menu_gallery);
                        }
                    } else {
                        imgSalon.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) { /* ignore */ }
        });

        // Load services
        repo.getServicesOfSalon(salonId, new FirebaseRepo.FirebaseCallback<List<Service>>() {
            @Override
            public void onSuccess(List<Service> services) {
                serviceAdapter.setServiceList(services);
            }

            @Override
            public void onFailure(Exception e) { /* ignore */ }
        });

        // Load stylists
        repo.getStylistsOfSalon(salonId, new FirebaseRepo.FirebaseCallback<List<Stylist>>() {
            @Override
            public void onSuccess(List<Stylist> stylists) {
                stylistAdapter.setStylistList(stylists);
            }

            @Override
            public void onFailure(Exception e) { /* ignore */ }
        });
    }
}
