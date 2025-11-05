package com.example.prm_be.ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.Salon;
import com.example.prm_be.data.models.WorkingHours;
import com.example.prm_be.utils.RoleGuard;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminWorkingHoursActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private Spinner spinnerSalons;
    private EditText etOpenTime;
    private EditText etCloseTime;
    private EditText etSlotDuration;
    private LinearLayout llDaysOfWeek;
    private MaterialButton btnSave;
    private ProgressBar progressBar;

    private FirebaseRepo repo;
    private List<Salon> allSalons = new ArrayList<>();
    private Salon selectedSalon;
    private List<CheckBox> dayCheckBoxes = new ArrayList<>();
    private final String[] dayLabels = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
    private final String[] dayValues = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check admin role
        if (!RoleGuard.checkRoleSync(this, "admin")) {
            return;
        }
        
        setContentView(R.layout.activity_admin_working_hours);

        repo = FirebaseRepo.getInstance();
        
        initViews();
        setupToolbar();
        loadSalons();
        setupDaysCheckBoxes();
        setupSaveButton();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        spinnerSalons = findViewById(R.id.spinnerSalons);
        etOpenTime = findViewById(R.id.etOpenTime);
        etCloseTime = findViewById(R.id.etCloseTime);
        etSlotDuration = findViewById(R.id.etSlotDuration);
        llDaysOfWeek = findViewById(R.id.llDaysOfWeek);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cấu hình giờ làm việc");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDaysCheckBoxes() {
        for (int i = 0; i < dayLabels.length; i++) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(dayLabels[i]);
            checkBox.setTag(dayValues[i]);
            checkBox.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
            llDaysOfWeek.addView(checkBox);
            dayCheckBoxes.add(checkBox);
        }
    }

    private void loadSalons() {
        showLoading(true);
        repo.getAllSalons(new FirebaseRepo.FirebaseCallback<List<Salon>>() {
            @Override
            public void onSuccess(List<Salon> salons) {
                showLoading(false);
                allSalons = salons;
                setupSalonSpinner();
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Toast.makeText(AdminWorkingHoursActivity.this,
                    "Không thể tải danh sách salon: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSalonSpinner() {
        if (allSalons.isEmpty()) {
            Toast.makeText(this, "Chưa có salon nào", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> salonNames = new ArrayList<>();
        for (Salon salon : allSalons) {
            salonNames.add(salon.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, salonNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSalons.setAdapter(adapter);

        spinnerSalons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSalon = allSalons.get(position);
                loadWorkingHours(selectedSalon.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Select first salon by default
        if (!allSalons.isEmpty()) {
            selectedSalon = allSalons.get(0);
            loadWorkingHours(selectedSalon.getId());
        }
    }

    private void loadWorkingHours(String salonId) {
        showLoading(true);
        repo.getWorkingHours(salonId, new FirebaseRepo.FirebaseCallback<WorkingHours>() {
            @Override
            public void onSuccess(WorkingHours workingHours) {
                showLoading(false);
                if (workingHours != null) {
                    etOpenTime.setText(workingHours.getOpenTime());
                    etCloseTime.setText(workingHours.getCloseTime());
                    etSlotDuration.setText(String.valueOf(workingHours.getSlotDuration()));

                    // Set days checkboxes
                    List<String> selectedDays = workingHours.getDaysOfWeek();
                    for (CheckBox checkBox : dayCheckBoxes) {
                        String dayValue = (String) checkBox.getTag();
                        checkBox.setChecked(selectedDays != null && selectedDays.contains(dayValue));
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                // Use defaults
                etOpenTime.setText("09:00");
                etCloseTime.setText("18:00");
                etSlotDuration.setText("30");
            }
        });
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            if (selectedSalon == null) {
                Toast.makeText(this, "Vui lòng chọn salon", Toast.LENGTH_SHORT).show();
                return;
            }

            String openTime = etOpenTime.getText().toString().trim();
            String closeTime = etCloseTime.getText().toString().trim();
            String slotDurationStr = etSlotDuration.getText().toString().trim();

            if (TextUtils.isEmpty(openTime) || !isValidTime(openTime)) {
                Toast.makeText(this, "Giờ mở cửa không hợp lệ (định dạng: HH:mm)", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(closeTime) || !isValidTime(closeTime)) {
                Toast.makeText(this, "Giờ đóng cửa không hợp lệ (định dạng: HH:mm)", Toast.LENGTH_SHORT).show();
                return;
            }

            int slotDuration = 30;
            try {
                slotDuration = Integer.parseInt(slotDurationStr);
                if (slotDuration <= 0) {
                    slotDuration = 30;
                }
            } catch (NumberFormatException e) {
                // Use default
            }

            // Get selected days
            List<String> selectedDays = new ArrayList<>();
            for (CheckBox checkBox : dayCheckBoxes) {
                if (checkBox.isChecked()) {
                    selectedDays.add((String) checkBox.getTag());
                }
            }

            if (selectedDays.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất một ngày làm việc", Toast.LENGTH_SHORT).show();
                return;
            }

            WorkingHours workingHours = new WorkingHours(selectedSalon.getId(), openTime, closeTime, selectedDays, slotDuration);
            saveWorkingHours(workingHours);
        });
    }

    private boolean isValidTime(String time) {
        return time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    private void saveWorkingHours(WorkingHours workingHours) {
        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");
        showLoading(true);

        repo.saveWorkingHours(workingHours, new FirebaseRepo.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                showLoading(false);
                btnSave.setEnabled(true);
                btnSave.setText("Lưu");
                Toast.makeText(AdminWorkingHoursActivity.this, "Đã lưu cấu hình giờ làm việc thành công", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                btnSave.setEnabled(true);
                btnSave.setText("Lưu");
                Toast.makeText(AdminWorkingHoursActivity.this,
                    "Không thể lưu cấu hình: " + (e != null ? e.getMessage() : "Lỗi không xác định"),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}

