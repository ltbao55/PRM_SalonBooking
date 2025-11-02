package com.example.prm_be.ui.discovery;

import android.os.Bundle;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;

public class SalonListActivity extends AppCompatActivity {

    private RecyclerView rvSalons;
    private SearchView searchView;
    
    private FirebaseRepo repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salon_list);

        repo = FirebaseRepo.getInstance();

        initViews();
        setupSearchView();
        loadSalons();
    }

    private void initViews() {
        rvSalons = findViewById(R.id.rvSalons);
        searchView = findViewById(R.id.searchView);
        
        // TODO: Setup RecyclerView adapter
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO: Implement search
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO: Implement filter as user types
                return false;
            }
        });
    }

    private void loadSalons() {
        // TODO: Load salons from FirebaseRepo
        // repo.getAllSalons(new FirebaseRepo.FirebaseCallback<List<Salon>>() {...});
    }
}
