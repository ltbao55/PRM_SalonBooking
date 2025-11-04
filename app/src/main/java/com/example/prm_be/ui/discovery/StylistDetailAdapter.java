package com.example.prm_be.ui.discovery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.models.Stylist;
import com.google.android.material.card.MaterialCardView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị danh sách Stylist trong SalonDetailActivity
 */
public class StylistDetailAdapter extends RecyclerView.Adapter<StylistDetailAdapter.StylistViewHolder> {

    private List<Stylist> stylistList;

    public StylistDetailAdapter() {
        this.stylistList = new ArrayList<>();
    }

    public void setStylistList(List<Stylist> stylistList) {
        this.stylistList = stylistList != null ? stylistList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stylist_detail, parent, false);
        return new StylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StylistViewHolder holder, int position) {
        Stylist stylist = stylistList.get(position);
        holder.bind(stylist);
    }

    @Override
    public int getItemCount() {
        return stylistList.size();
    }

    class StylistViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardStylist;
        private ImageView imgStylist;
        private TextView tvStylistName;
        private TextView tvSpecialization;

        public StylistViewHolder(@NonNull View itemView) {
            super(itemView);
            cardStylist = itemView.findViewById(R.id.cardStylist);
            imgStylist = itemView.findViewById(R.id.imgStylist);
            tvStylistName = itemView.findViewById(R.id.tvStylistName);
            tvSpecialization = itemView.findViewById(R.id.tvSpecialization);
        }

        public void bind(Stylist stylist) {
            if (stylist != null) {
                tvStylistName.setText(stylist.getName());
                
                if (stylist.getSpecialization() != null && !stylist.getSpecialization().isEmpty()) {
                    tvSpecialization.setText(stylist.getSpecialization());
                    tvSpecialization.setVisibility(View.VISIBLE);
                } else {
                    tvSpecialization.setVisibility(View.GONE);
                }
                
                // Load image with Glide
                if (stylist.getImageUrl() != null && !stylist.getImageUrl().isEmpty()) {
                    Glide.with(imgStylist.getContext())
                            .load(stylist.getImageUrl())
                            .placeholder(android.R.drawable.ic_menu_myplaces)
                            .circleCrop()
                            .into(imgStylist);
                } else {
                    imgStylist.setImageResource(android.R.drawable.ic_menu_myplaces);
                }
            }
        }
    }
}

