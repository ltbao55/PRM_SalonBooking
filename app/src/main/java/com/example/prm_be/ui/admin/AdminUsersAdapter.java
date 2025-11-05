package com.example.prm_be.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.models.User;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersAdapter extends RecyclerView.Adapter<AdminUsersAdapter.ViewHolder> {
    private List<User> users = new ArrayList<>();
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public AdminUsersAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.users = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName;
        private TextView tvUserEmail;
        private TextView tvUserRole;
        private Chip chipUserStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            chipUserStatus = itemView.findViewById(R.id.chipUserStatus);
        }

        void bind(User user) {
            tvUserName.setText(user.getName());
            tvUserEmail.setText(user.getEmail());
            
            String role = user.getRole();
            if (role == null || role.isEmpty()) {
                role = "user";
            }
            tvUserRole.setText(getRoleText(role));
            
            String status = user.getStatus();
            if (status == null || status.isEmpty()) {
                status = "active";
            }
            chipUserStatus.setText(getStatusText(status));
            chipUserStatus.setChipBackgroundColorResource(getStatusColor(status));
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(user);
                }
            });
        }

        private String getRoleText(String role) {
            if (role == null) return "Người dùng";
            switch (role.toLowerCase()) {
                case "admin":
                    return "Quản trị viên";
                case "staff":
                    return "Nhân viên";
                case "user":
                default:
                    return "Người dùng";
            }
        }

        private String getStatusText(String status) {
            if (status == null) return "Hoạt động";
            switch (status.toLowerCase()) {
                case "active":
                    return "Hoạt động";
                case "disabled":
                    return "Vô hiệu hóa";
                default:
                    return "Hoạt động";
            }
        }

        private int getStatusColor(String status) {
            if (status == null) return R.color.success;
            switch (status.toLowerCase()) {
                case "active":
                    return R.color.success;
                case "disabled":
                    return R.color.error;
                default:
                    return R.color.success;
            }
        }
    }
}

