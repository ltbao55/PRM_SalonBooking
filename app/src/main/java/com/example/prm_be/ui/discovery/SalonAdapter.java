package com.example.prm_be.ui.discovery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.models.Salon;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị danh sách Salon
 * Hiện tại chỉ hiển thị UI, chưa có logic load data từ Firebase
 */
public class SalonAdapter extends RecyclerView.Adapter<SalonAdapter.SalonViewHolder> {

    private List<Salon> salonList;
    private OnSalonClickListener listener;

    public interface OnSalonClickListener {
        void onSalonClick(Salon salon);
    }

    public SalonAdapter() {
        this.salonList = new ArrayList<>();
    }

    public void setSalonList(List<Salon> salonList) {
        this.salonList = salonList != null ? salonList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnSalonClickListener(OnSalonClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SalonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_salon, parent, false);
        return new SalonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalonViewHolder holder, int position) {
        Salon salon = salonList.get(position);
        holder.bind(salon);
    }

    @Override
    public int getItemCount() {
        return salonList.size();
    }

    class SalonViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgSalon;
        private TextView tvSalonName;
        private TextView tvAddress;
        private TextView tvServiceCount;

        public SalonViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSalon = itemView.findViewById(R.id.imgSalon);
            tvSalonName = itemView.findViewById(R.id.tvSalonName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvServiceCount = itemView.findViewById(R.id.tvServiceCount);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSalonClick(salonList.get(position));
                }
            });
        }

        public void bind(Salon salon) {
            if (salon != null) {
                tvSalonName.setText(salon.getName());
                tvAddress.setText(salon.getAddress());
                
                // Hiển thị placeholder image (sẽ load từ URL sau khi có BE)
                imgSalon.setImageResource(android.R.drawable.ic_menu_gallery);
                
                // Hiện tại không có service count từ model, để mặc định
                tvServiceCount.setText("Xem chi tiết");
            }
        }
    }
}

