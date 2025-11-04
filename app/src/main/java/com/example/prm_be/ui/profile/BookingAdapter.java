package com.example.prm_be.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.models.Booking;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView hiển thị danh sách Booking trong BookingHistoryActivity
 */
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookingList;
    private OnBookingClickListener listener;
    private java.util.Map<String, String> salonIdToName = new java.util.HashMap<>();
    private java.util.Map<String, String> serviceIdToName = new java.util.HashMap<>();

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    public BookingAdapter() {
        this.bookingList = new ArrayList<>();
        this.salonIdToName = new java.util.HashMap<>();
        this.serviceIdToName = new java.util.HashMap<>();
    }

    public void setBookingList(List<Booking> bookingList) {
        this.bookingList = bookingList != null ? bookingList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnBookingClickListener(OnBookingClickListener listener) {
        this.listener = listener;
    }

    public interface OnBookingLongClickListener {
        void onBookingLongClick(Booking booking);
    }

    private OnBookingLongClickListener longClickListener;

    public void setOnBookingLongClickListener(OnBookingLongClickListener listener) {
        this.longClickListener = listener;
    }

    // Helper methods to cache names
    public void setSalonName(String salonId, String salonName) {
        if (salonId != null && salonName != null) {
            salonIdToName.put(salonId, salonName);
            notifyDataSetChanged();
        }
    }

    public void setServiceName(String serviceId, String serviceName) {
        if (serviceId != null && serviceName != null) {
            serviceIdToName.put(serviceId, serviceName);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSalonName;
        private TextView tvServiceName;
        private TextView tvDateTime;
        private Chip chipStatus;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSalonName = itemView.findViewById(R.id.tvSalonName);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }

        public void bind(Booking booking) {
            if (booking != null) {
                // Salon name from cache
                String salonName = salonIdToName.get(booking.getSalonId());
                tvSalonName.setText(salonName != null ? salonName : ("Salon ID: " + booking.getSalonId()));
                
                // Service name from cache
                String serviceName = serviceIdToName.get(booking.getServiceId());
                tvServiceName.setText(serviceName != null ? serviceName : ("Dịch vụ ID: " + booking.getServiceId()));

                // Format date and time
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(booking.getTimestamp());
                
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
                String dateTimeStr = dateTimeFormat.format(calendar.getTime());
                tvDateTime.setText(dateTimeStr);

                // Set status chip
                String status = booking.getStatus();
                chipStatus.setText(getStatusText(status));
                
                // Set status color
                int statusColor = getStatusColor(status);
                chipStatus.setChipBackgroundColorResource(statusColor);

                // Set click listener
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onBookingClick(bookingList.get(position));
                    }
                });

                itemView.setOnLongClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                        longClickListener.onBookingLongClick(bookingList.get(position));
                        return true;
                    }
                    return false;
                });
            }
        }

        private String getStatusText(String status) {
            if (status == null) return "Chưa xác định";
            
            switch (status.toLowerCase()) {
                case "pending":
                    return "Chờ xác nhận";
                case "confirmed":
                    return "Đã xác nhận";
                case "completed":
                    return "Đã hoàn thành";
                case "cancelled":
                    return "Đã hủy";
                default:
                    return status;
            }
        }

        private int getStatusColor(String status) {
            if (status == null) return R.color.text_hint;
            
            switch (status.toLowerCase()) {
                case "pending":
                    return R.color.warning;
                case "confirmed":
                    return R.color.success;
                case "completed":
                    return R.color.info;
                case "cancelled":
                    return R.color.error;
                default:
                    return R.color.text_hint;
            }
        }
    }
}

