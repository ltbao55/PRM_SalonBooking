package com.example.prm_be.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.models.Salon;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminSalonsAdapter extends RecyclerView.Adapter<AdminSalonsAdapter.ViewHolder> {
    private List<Salon> salons = new ArrayList<>();
    private OnSalonEditListener editListener;
    private OnSalonDeleteListener deleteListener;

    public interface OnSalonEditListener {
        void onEdit(Salon salon);
    }

    public interface OnSalonDeleteListener {
        void onDelete(Salon salon);
    }

    public AdminSalonsAdapter(OnSalonEditListener editListener, OnSalonDeleteListener deleteListener) {
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    public void setSalons(List<Salon> salons) {
        this.salons = salons != null ? salons : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_salon, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Salon salon = salons.get(position);
        holder.bind(salon);
    }

    @Override
    public int getItemCount() {
        return salons.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSalonName;
        private TextView tvSalonAddress;
        private TextView tvSalonPhone;
        private TextView tvSalonRating;
        private MaterialButton btnEditSalon;
        private MaterialButton btnDeleteSalon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSalonName = itemView.findViewById(R.id.tvSalonName);
            tvSalonAddress = itemView.findViewById(R.id.tvSalonAddress);
            tvSalonPhone = itemView.findViewById(R.id.tvSalonPhone);
            tvSalonRating = itemView.findViewById(R.id.tvSalonRating);
            btnEditSalon = itemView.findViewById(R.id.btnEditSalon);
            btnDeleteSalon = itemView.findViewById(R.id.btnDeleteSalon);
        }

        void bind(Salon salon) {
            tvSalonName.setText(salon.getName());
            tvSalonAddress.setText(salon.getAddress());
            
            String phone = salon.getPhone();
            if (phone == null || phone.isEmpty()) {
                tvSalonPhone.setText("Chưa có số điện thoại");
            } else {
                tvSalonPhone.setText("📞 " + phone);
            }
            
            tvSalonRating.setText("⭐ " + String.format("%.1f", salon.getRating()));

            btnEditSalon.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onEdit(salon);
                }
            });

            btnDeleteSalon.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(salon);
                }
            });
        }
    }
}

