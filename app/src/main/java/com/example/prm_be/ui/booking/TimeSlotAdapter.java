package com.example.prm_be.ui.booking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị danh sách Time Slot trong BookingActivity
 */
public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {

    private List<TimeSlot> timeSlotList;
    private OnTimeSlotClickListener listener;
    private int selectedPosition = -1;
    private List<String> bookedSlots; // Danh sách các slot đã được đặt

    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(TimeSlot timeSlot, int position);
    }

    public static class TimeSlot {
        private String time;
        private long timestamp;
        private boolean isAvailable;

        public TimeSlot(String time, long timestamp, boolean isAvailable) {
            this.time = time;
            this.timestamp = timestamp;
            this.isAvailable = isAvailable;
        }

        public String getTime() {
            return time;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public boolean isAvailable() {
            return isAvailable;
        }
    }

    public TimeSlotAdapter() {
        this.timeSlotList = new ArrayList<>();
        this.bookedSlots = new ArrayList<>();
    }

    public void setTimeSlotList(List<TimeSlot> timeSlotList) {
        this.timeSlotList = timeSlotList != null ? timeSlotList : new ArrayList<>();
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setBookedSlots(List<String> bookedSlots) {
        this.bookedSlots = bookedSlots != null ? bookedSlots : new ArrayList<>();
        // Update availability
        for (int i = 0; i < timeSlotList.size(); i++) {
            TimeSlot slot = timeSlotList.get(i);
            boolean wasAvailable = slot.isAvailable;
            slot.isAvailable = !this.bookedSlots.contains(slot.getTime());
            if (wasAvailable != slot.isAvailable) {
                notifyItemChanged(i);
            }
        }
    }

    public void setOnTimeSlotClickListener(OnTimeSlotClickListener listener) {
        this.listener = listener;
    }

    public TimeSlot getSelectedTimeSlot() {
        if (selectedPosition >= 0 && selectedPosition < timeSlotList.size()) {
            return timeSlotList.get(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        TimeSlot timeSlot = timeSlotList.get(position);
        holder.bind(timeSlot, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return timeSlotList.size();
    }

    class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView tvTime;

        public TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardTimeSlot);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(TimeSlot timeSlot, boolean isSelected) {
            if (timeSlot != null) {
                tvTime.setText(timeSlot.getTime());

                if (timeSlot.isAvailable()) {
                    // Available slot
                    if (isSelected) {
                        cardView.setStrokeWidth(4);
                        cardView.setStrokeColor(itemView.getContext().getColor(R.color.primary));
                        cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.primary_light));
                        tvTime.setTextColor(itemView.getContext().getColor(R.color.white));
                    } else {
                        cardView.setStrokeWidth(0);
                        cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.surface));
                        tvTime.setTextColor(itemView.getContext().getColor(R.color.text_primary));
                    }
                    cardView.setAlpha(1.0f);
                    itemView.setEnabled(true);
                } else {
                    // Booked slot
                    cardView.setStrokeWidth(0);
                    cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.surface_variant));
                    tvTime.setTextColor(itemView.getContext().getColor(R.color.text_hint));
                    cardView.setAlpha(0.5f);
                    itemView.setEnabled(false);
                }

                itemView.setOnClickListener(v -> {
                    if (!timeSlot.isAvailable()) {
                        return;
                    }
                    
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        int previousSelected = selectedPosition;
                        selectedPosition = position;
                        
                        if (previousSelected != -1 && previousSelected < timeSlotList.size()) {
                            notifyItemChanged(previousSelected);
                        }
                        notifyItemChanged(selectedPosition);
                        
                        if (listener != null) {
                            listener.onTimeSlotClick(timeSlot, position);
                        }
                    }
                });
            }
        }
    }
}

