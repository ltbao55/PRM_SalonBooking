package com.example.prm_be.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.ui.auth.LoginActivity;
import com.example.prm_be.utils.RoleGuard;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class AdminDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check admin role
        if (!RoleGuard.checkRoleSync(this, "admin")) {
            return; // Will redirect to login/home
        }
        
        setContentView(R.layout.activity_admin_dashboard);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // All Schedules Card
        MaterialCardView cardAllSchedules = findViewById(R.id.cardAllSchedules);
        if (cardAllSchedules != null) {
            cardAllSchedules.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminAllSchedulesActivity.class));
            });
        }

        // Manage Users Card
        MaterialCardView cardManageUsers = findViewById(R.id.cardManageUsers);
        if (cardManageUsers != null) {
            cardManageUsers.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminUsersActivity.class));
            });
        }

        // Manage Services Card
        MaterialCardView cardManageServices = findViewById(R.id.cardManageServices);
        if (cardManageServices != null) {
            cardManageServices.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminServicesActivity.class));
            });
        }

        // Manage Salons Card
        MaterialCardView cardManageSalons = findViewById(R.id.cardManageSalons);
        if (cardManageSalons != null) {
            cardManageSalons.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminSalonsActivity.class));
            });
        }

        // Working Hours Card
        MaterialCardView cardWorkingHours = findViewById(R.id.cardWorkingHours);
        if (cardWorkingHours != null) {
            cardWorkingHours.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminWorkingHoursActivity.class));
            });
        }

        // Reports Card
        MaterialCardView cardReports = findViewById(R.id.cardReports);
        if (cardReports != null) {
            cardReports.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminReportsActivity.class));
            });
        }

        // Dev Tools removed from Dashboard

        // Logout Button
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                FirebaseRepo.getInstance().logout();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}


