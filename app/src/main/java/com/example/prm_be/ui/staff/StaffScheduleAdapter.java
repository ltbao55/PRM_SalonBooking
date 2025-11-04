package com.example.prm_be.ui.staff;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StaffScheduleAdapter extends RecyclerView.Adapter<StaffScheduleAdapter.ViewHolder> {
    private List<Booking> bookings;
    private OnBookingClickListener listener;
    private SimpleDateFormat dateTimeFormat;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    public StaffScheduleAdapter(OnBookingClickListener listener) {
        this.bookings = new ArrayList<>();
        this.listener = listener;
        this.dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings != null ? bookings : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSalonName;
        private TextView tvServiceName;
        private TextView tvCustomerName;
        private TextView tvDateTime;
        private Chip chipStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSalonName = itemView.findViewById(R.id.tvSalonName);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            chipStatus = itemView.findViewById(R.id.chipStatus);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onBookingClick(bookings.get(getAdapterPosition()));
                }
            });
        }

        void bind(Booking booking) {
            // Format date/time
            String dateTime = dateTimeFormat.format(new Date(booking.getTimestamp()));
            tvDateTime.setText(dateTime);

            // Status chip
            String status = booking.getStatus();
            chipStatus.setText(getStatusText(status));
            chipStatus.setChipBackgroundColorResource(getStatusColor(status));

            // Placeholder texts (will be loaded from Firestore)
            tvSalonName.setText("Đang tải...");
            tvServiceName.setText("Đang tải...");
            tvCustomerName.setText("Đang tải...");

            // TODO: Load salon name, service name, customer name from Firestore
            // This will be done in StaffScheduleActivity when we have the booking data
        }

        private String getStatusText(String status) {
            if (status == null) return "Chưa xác nhận";
            switch (status.toLowerCase()) {
                case "confirmed":
                    return "Đã xác nhận";
                case "pending":
                    return "Chờ xác nhận";
                case "completed":
                    return "Đã hoàn thành";
                case "cancelled":
                    return "Đã hủy";
                default:
                    return "Chưa xác nhận";
            }
        }

        private int getStatusColor(String status) {
            if (status == null) return R.color.warning;
            switch (status.toLowerCase()) {
                case "confirmed":
                    return R.color.success;
                case "pending":
                    return R.color.warning;
                case "completed":
                    return R.color.info;
                case "cancelled":
                    return R.color.error;
                default:
                    return R.color.warning;
            }
        }
    }
}

