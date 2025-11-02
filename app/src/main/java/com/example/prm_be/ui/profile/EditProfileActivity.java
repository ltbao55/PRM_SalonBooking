package com.example.prm_be.ui.profile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private EditText edtName;
    private Button btnSave;

    private FirebaseRepo repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        repo = FirebaseRepo.getInstance();

        initViews();
        setupClickListeners();
        loadUserData();
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        edtName = findViewById(R.id.edtName);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupClickListeners() {
        imgAvatar.setOnClickListener(v -> {
            // TODO: Open image picker to change avatar
        });

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            if (name.isEmpty()) {
                return;
            }

            // TODO: Update user profile using FirebaseRepo
            // repo.updateUser(user, new FirebaseRepo.FirebaseCallback<Void>() {...});
            finish();
        });
    }

    private void loadUserData() {
        // TODO: Load current user data
    }
}
