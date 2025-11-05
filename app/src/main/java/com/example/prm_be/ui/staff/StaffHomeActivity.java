package com.example.prm_be.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.prm_be.R;
import com.example.prm_be.utils.RoleGuard;

public class StaffHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check staff role
        if (!RoleGuard.checkRoleSync(this, "staff")) {
            return;
        }
        
        setContentView(R.layout.activity_staff_home);
        
        // Navigate to schedule immediately
        startActivity(new Intent(this, StaffScheduleActivity.class));
        finish();
    }
}


