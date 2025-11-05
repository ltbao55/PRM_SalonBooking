package com.example.prm_be.ui.dev;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.Stylist;
import com.example.prm_be.utils.SeedDataUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DevToolsActivity extends AppCompatActivity {
    private View root;
    private FirebaseRepo repo;
    private MaterialButton btnLoadStylists;
    private MaterialButton btnForceSeedData;
    private MaterialButton btnLinkAllStaffAccounts;
    private TextView tvStatus;
    private RecyclerView rvStylists;
    private View llEmptyState;
    private StylistAccountAdapter adapter;
    private List<Stylist> stylists = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_tools);
        root = findViewById(android.R.id.content);
        repo = FirebaseRepo.getInstance();

        MaterialButton btnCreateStaff = findViewById(R.id.btnCreateStaff);
        MaterialButton btnCreateAdmin = findViewById(R.id.btnCreateAdmin);
        btnForceSeedData = findViewById(R.id.btnForceSeedData);
        btnLoadStylists = findViewById(R.id.btnLoadStylists);
        btnLinkAllStaffAccounts = findViewById(R.id.btnLinkAllStaffAccounts);
        tvStatus = findViewById(R.id.tvStatus);
        rvStylists = findViewById(R.id.rvStylists);
        llEmptyState = findViewById(R.id.llEmptyState);

        adapter = new StylistAccountAdapter();
        rvStylists.setLayoutManager(new LinearLayoutManager(this));
        rvStylists.setAdapter(adapter);

        btnCreateStaff.setOnClickListener(v -> createAccount("staff1@lux.com", "123456", "Staff Test", "staff"));
        btnCreateAdmin.setOnClickListener(v -> createAccount("admin1@lux.com", "123456", "Admin Test", "admin"));
        btnForceSeedData.setOnClickListener(v -> forceSeedAllData());
        btnLoadStylists.setOnClickListener(v -> loadStylists());
        btnLinkAllStaffAccounts.setOnClickListener(v -> linkAllStaffAccountsWithStylists());
    }

    private void createAccount(String email, String password, String name, String role) {
        Snackbar.make(root, "Creating " + role + "...", Snackbar.LENGTH_SHORT).show();
        repo.register(email, password, name, new FirebaseRepo.FirebaseCallback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser user) {
                repo.updateUserRole(user.getUid(), role, new FirebaseRepo.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Snackbar.make(root, "Created " + role + " account: " + email, Snackbar.LENGTH_LONG).show();
                        repo.logout();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Snackbar.make(root, "Failed to set role: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Snackbar.make(root, "Create failed: " + (e != null ? e.getMessage() : "unknown"), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void forceSeedAllData() {
        btnForceSeedData.setEnabled(false);
        Snackbar.make(root, "Force seeding data... This may take a few minutes.", Snackbar.LENGTH_LONG).show();
        
        SeedDataUtils.forceSeedData();
        
        // Re-enable button after 10 seconds
        new android.os.Handler().postDelayed(() -> {
            btnForceSeedData.setEnabled(true);
            Snackbar.make(root, "Data seeding completed! Check logs for details.", Snackbar.LENGTH_LONG).show();
        }, 10000);
    }

    private void loadStylists() {
        btnLoadStylists.setEnabled(false);
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("Loading stylists...");
        rvStylists.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);

        repo.getAllStylists(new FirebaseRepo.FirebaseCallback<List<Stylist>>() {
            @Override
            public void onSuccess(List<Stylist> stylistsList) {
                btnLoadStylists.setEnabled(true);
                
                if (stylistsList == null || stylistsList.isEmpty()) {
                    tvStatus.setText("No stylists found in database");
                    llEmptyState.setVisibility(View.VISIBLE);
                    return;
                }

                stylists = stylistsList;
                adapter.setStylists(stylists);
                tvStatus.setText("Found " + stylists.size() + " stylists");
                rvStylists.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Exception e) {
                btnLoadStylists.setEnabled(true);
                String errorMsg = e != null && e.getMessage() != null ? e.getMessage() : "unknown";
                tvStatus.setText("Error: " + errorMsg);
                
                if (errorMsg.contains("PERMISSION_DENIED")) {
                    tvStatus.setText("Permission denied. Please login first or check Firestore rules.");
                    Snackbar.make(root, "Permission denied. Login may be required.", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(root, "Error loading stylists: " + errorMsg, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void createAccountForStylist(Stylist stylist, String email, String password, String name) {
        Snackbar.make(root, "Creating account for " + name + "...", Snackbar.LENGTH_SHORT).show();
        
        repo.register(email, password, name, new FirebaseRepo.FirebaseCallback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser user) {
                repo.updateUserRole(user.getUid(), "staff", new FirebaseRepo.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Cập nhật stylistId vào User document để liên kết với stylist
                        repo.updateUserStylistId(user.getUid(), stylist.getId(), new FirebaseRepo.FirebaseCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                Snackbar.make(root, "Created account: " + email, Snackbar.LENGTH_LONG).show();
                                repo.logout();
                                // Update adapter to disable button
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                // Role đã set thành công, stylistId có thể set sau
                                Log.w("DevTools", "Failed to set stylistId for " + email + ", but role is set", e);
                                Snackbar.make(root, "Created account (stylistId not set): " + email, Snackbar.LENGTH_LONG).show();
                                repo.logout();
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Snackbar.make(root, "Failed to set role: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                String errorMsg = e != null && e.getMessage() != null ? e.getMessage() : "unknown";
                if (errorMsg.contains("email-already-in-use")) {
                    Snackbar.make(root, "Email already exists: " + email, Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(root, "Create failed: " + errorMsg, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private String generateEmailFromStylist(Stylist stylist) {
        String name = stylist.getName();
        if (TextUtils.isEmpty(name)) {
            return "stylist_" + stylist.getId() + "@lux.com";
        }
        
        String email = name.toLowerCase()
            .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
            .replaceAll("[èéẹẻẽêềếệểễ]", "e")
            .replaceAll("[ìíịỉĩ]", "i")
            .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
            .replaceAll("[ùúụủũưừứựửữ]", "u")
            .replaceAll("[ỳýỵỷỹ]", "y")
            .replaceAll("[đ]", "d")
            .replaceAll("[^a-z0-9]", "")
            .replaceAll("\\s+", "");
        
        if (email.isEmpty()) {
            email = "stylist_" + stylist.getId();
        }
        
        return email + "@lux.com";
    }

    private void linkAllStaffAccountsWithStylists() {
        btnLinkAllStaffAccounts.setEnabled(false);
        Snackbar.make(root, "Đang liên kết tất cả staff accounts với stylists...", Snackbar.LENGTH_LONG).show();
        
        // Load tất cả users có role = "staff"
        repo.getAllUsers(new FirebaseRepo.FirebaseCallback<List<com.example.prm_be.data.models.User>>() {
            @Override
            public void onSuccess(List<com.example.prm_be.data.models.User> users) {
                // Load tất cả stylists
                repo.getAllStylists(new FirebaseRepo.FirebaseCallback<List<Stylist>>() {
                    @Override
                    public void onSuccess(List<Stylist> stylistsList) {
                        if (users == null || users.isEmpty()) {
                            btnLinkAllStaffAccounts.setEnabled(true);
                            Snackbar.make(root, "Không có staff accounts nào", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        
                        if (stylistsList == null || stylistsList.isEmpty()) {
                            btnLinkAllStaffAccounts.setEnabled(true);
                            Snackbar.make(root, "Không có stylists nào", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // Lọc chỉ staff accounts chưa có stylistId
                        List<com.example.prm_be.data.models.User> staffWithoutStylist = new ArrayList<>();
                        for (com.example.prm_be.data.models.User user : users) {
                            if ("staff".equalsIgnoreCase(user.getRole()) && 
                                (user.getStylistId() == null || user.getStylistId().isEmpty())) {
                                staffWithoutStylist.add(user);
                            }
                        }
                        
                        if (staffWithoutStylist.isEmpty()) {
                            btnLinkAllStaffAccounts.setEnabled(true);
                            Snackbar.make(root, "Tất cả staff accounts đã được liên kết", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // Tạo map email -> stylist
                        java.util.Map<String, Stylist> emailToStylistMap = new java.util.HashMap<>();
                        for (Stylist stylist : stylistsList) {
                            String email = generateEmailFromStylist(stylist);
                            emailToStylistMap.put(email.toLowerCase(), stylist);
                        }
                        
                        // Liên kết từng staff account với stylist tương ứng
                        final int[] linked = {0};
                        final int[] total = {staffWithoutStylist.size()};
                        
                        for (com.example.prm_be.data.models.User staff : staffWithoutStylist) {
                            String staffEmail = staff.getEmail();
                            if (staffEmail != null) {
                                Stylist matchingStylist = emailToStylistMap.get(staffEmail.toLowerCase());
                                if (matchingStylist != null) {
                                    // Liên kết staff với stylist
                                    repo.updateUserStylistId(staff.getUid(), matchingStylist.getId(), 
                                        new FirebaseRepo.FirebaseCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void result) {
                                                linked[0]++;
                                                if (linked[0] == total[0]) {
                                                    btnLinkAllStaffAccounts.setEnabled(true);
                                                    Snackbar.make(root, 
                                                        "Đã liên kết " + linked[0] + "/" + total[0] + " staff accounts", 
                                                        Snackbar.LENGTH_LONG).show();
                                                }
                                            }
                                            
                                            @Override
                                            public void onFailure(Exception e) {
                                                Log.e("DevTools", "Failed to link " + staffEmail, e);
                                                linked[0]++;
                                                if (linked[0] == total[0]) {
                                                    btnLinkAllStaffAccounts.setEnabled(true);
                                                    Snackbar.make(root, 
                                                        "Hoàn thành (một số có thể thất bại): " + linked[0] + "/" + total[0], 
                                                        Snackbar.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                } else {
                                    // Không tìm thấy stylist phù hợp
                                    linked[0]++;
                                    if (linked[0] == total[0]) {
                                        btnLinkAllStaffAccounts.setEnabled(true);
                                        Snackbar.make(root, 
                                            "Hoàn thành (một số không tìm thấy stylist phù hợp): " + linked[0] + "/" + total[0], 
                                            Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            } else {
                                linked[0]++;
                                if (linked[0] == total[0]) {
                                    btnLinkAllStaffAccounts.setEnabled(true);
                                    Snackbar.make(root, 
                                        "Hoàn thành (một số không có email): " + linked[0] + "/" + total[0], 
                                        Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        btnLinkAllStaffAccounts.setEnabled(true);
                        Snackbar.make(root, "Lỗi khi load stylists: " + (e != null ? e.getMessage() : "unknown"), 
                            Snackbar.LENGTH_LONG).show();
                    }
                });
            }
            
            @Override
            public void onFailure(Exception e) {
                btnLinkAllStaffAccounts.setEnabled(true);
                Snackbar.make(root, "Lỗi khi load users: " + (e != null ? e.getMessage() : "unknown"), 
                    Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private class StylistAccountAdapter extends RecyclerView.Adapter<StylistAccountAdapter.ViewHolder> {
        private List<Stylist> adapterStylists = new ArrayList<>();
        
        public void setStylists(List<Stylist> stylists) {
            adapterStylists = stylists != null ? stylists : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_stylist_account, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Stylist stylist = adapterStylists.get(position);
            holder.bind(stylist, position);
        }

        @Override
        public int getItemCount() {
            return adapterStylists.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvStylistName;
            private TextView tvStylistEmail;
            private MaterialButton btnCreateAccount;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvStylistName = itemView.findViewById(R.id.tvStylistName);
                tvStylistEmail = itemView.findViewById(R.id.tvStylistEmail);
                btnCreateAccount = itemView.findViewById(R.id.btnCreateAccount);
            }

            void bind(Stylist stylist, int position) {
                tvStylistName.setText(stylist.getName());
                String email = generateEmailFromStylist(stylist);
                tvStylistEmail.setText("Email: " + email);
                
                btnCreateAccount.setOnClickListener(v -> {
                    String password = "123456";
                    createAccountForStylist(stylist, email, password, stylist.getName());
                });
            }
        }
    }
}
