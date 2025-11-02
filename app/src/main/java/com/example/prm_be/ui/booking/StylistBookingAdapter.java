package com.example.prm_be.ui.booking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.models.Stylist;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị danh sách Stylist trong BookingActivity
 */
public class StylistBookingAdapter extends RecyclerView.Adapter<StylistBookingAdapter.StylistViewHolder> {

    private List<Stylist> stylistList;
    private OnStylistClickListener listener;
    private int selectedPosition = -1;
    private boolean allowNone = true; // Cho phép không chọn stylist

    public interface OnStylistClickListener {
        void onStylistClick(Stylist stylist, int position, boolean isNone);
    }

    public StylistBookingAdapter() {
        this.stylistList = new ArrayList<>();
    }

    public void setStylistList(List<Stylist> stylistList) {
        this.stylistList = stylistList != null ? stylistList : new ArrayList<>();
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setOnStylistClickListener(OnStylistClickListener listener) {
        this.listener = listener;
    }

    public Stylist getSelectedStylist() {
        if (selectedPosition >= 0 && selectedPosition < stylistList.size()) {
            return stylistList.get(selectedPosition);
        }
        return null;
    }

    public boolean isNoneSelected() {
        return selectedPosition == -1;
    }

    @NonNull
    @Override
    public StylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stylist_booking, parent, false);
        return new StylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StylistViewHolder holder, int position) {
        Stylist stylist = stylistList.get(position);
        holder.bind(stylist, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return stylistList.size();
    }

    class StylistViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView tvStylistName;
        private TextView tvSpecialization;

        public StylistViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardStylist);
            tvStylistName = itemView.findViewById(R.id.tvStylistName);
            tvSpecialization = itemView.findViewById(R.id.tvSpecialization);
        }

        public void bind(Stylist stylist, boolean isSelected) {
            if (stylist != null) {
                tvStylistName.setText(stylist.getName());
                
                if (stylist.getSpecialization() != null && !stylist.getSpecialization().isEmpty()) {
                    tvSpecialization.setText(stylist.getSpecialization());
                    tvSpecialization.setVisibility(View.VISIBLE);
                } else {
                    tvSpecialization.setVisibility(View.GONE);
                }

                // Highlight selected item
                if (isSelected) {
                    cardView.setStrokeWidth(4);
                    cardView.setStrokeColor(itemView.getContext().getColor(R.color.primary));
                    cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.primary_light));
                } else {
                    cardView.setStrokeWidth(0);
                    cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.surface));
                }

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        int previousSelected = selectedPosition;
                        selectedPosition = position;
                        
                        if (previousSelected != -1 && previousSelected < stylistList.size()) {
                            notifyItemChanged(previousSelected);
                        }
                        notifyItemChanged(selectedPosition);
                        
                        if (listener != null) {
                            listener.onStylistClick(stylist, position, false);
                        }
                    }
                });
            }
        }
    }
}

