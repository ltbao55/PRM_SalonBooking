package com.example.prm_be.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.FirebaseRepo;
import com.example.prm_be.data.models.Booking;
import com.example.prm_be.data.models.Salon;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminScheduleCalendarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_DATE_HEADER = 0;
    private static final int TYPE_BOOKING = 1;

    private List<Object> items = new ArrayList<>(); // Mix of DateHeader and Booking
    private OnBookingClickListener listener;
    private SimpleDateFormat dateTimeFormat;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    private FirebaseRepo repo;
    private List<Salon> salons;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    public AdminScheduleCalendarAdapter(OnBookingClickListener listener) {
        this.listener = listener;
        this.dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
        this.dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.repo = FirebaseRepo.getInstance();
    }

    public void setBookings(List<Booking> bookings, List<Salon> salons) {
        this.salons = salons != null ? salons : new ArrayList<>();
        this.items.clear();
        
        if (bookings == null || bookings.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        // Group bookings by date
        Map<String, List<Booking>> bookingsByDate = new HashMap<>();
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        
        for (Booking booking : bookings) {
            cal.setTimeInMillis(booking.getTimestamp());
            // Use date as key (yyyy-MM-dd format for sorting)
            String dateKey = String.format(Locale.getDefault(), "%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), 
                cal.get(Calendar.MONTH) + 1, 
                cal.get(Calendar.DAY_OF_MONTH));
            
            if (!bookingsByDate.containsKey(dateKey)) {
                bookingsByDate.put(dateKey, new ArrayList<>());
            }
            bookingsByDate.get(dateKey).add(booking);
        }

        // Sort dates and create items list
        List<String> sortedDates = new ArrayList<>(bookingsByDate.keySet());
        java.util.Collections.sort(sortedDates);

        for (String dateKey : sortedDates) {
            // Add date header
            items.add(new DateHeader(dateKey));
            
            // Add bookings for this date (already sorted by timestamp from activity)
            List<Booking> dayBookings = bookingsByDate.get(dateKey);
            items.addAll(dayBookings);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof DateHeader) {
            return TYPE_DATE_HEADER;
        } else {
            return TYPE_BOOKING;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_DATE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_calendar_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_staff_schedule, parent, false);
            return new BookingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DateHeaderViewHolder) {
            DateHeader header = (DateHeader) items.get(position);
            ((DateHeaderViewHolder) holder).bind(header);
        } else if (holder instanceof BookingViewHolder) {
            Booking booking = (Booking) items.get(position);
            ((BookingViewHolder) holder).bind(booking);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Date Header class
    private static class DateHeader {
        String dateKey; // yyyy-MM-dd format

        DateHeader(String dateKey) {
            this.dateKey = dateKey;
        }
    }

    // Date Header ViewHolder
    class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate;

        DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        void bind(DateHeader header) {
            try {
                // Parse date from yyyy-MM-dd
                String[] parts = header.dateKey.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1; // Calendar month is 0-based
                int day = Integer.parseInt(parts[2]);
                
                Calendar cal = Calendar.getInstance(Locale.getDefault());
                cal.set(year, month, day);
                
                String formattedDate = dateFormat.format(cal.getTime());
                // Capitalize first letter
                if (!formattedDate.isEmpty()) {
                    formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
                }
                tvDate.setText(formattedDate);
            } catch (Exception e) {
                tvDate.setText(header.dateKey);
            }
        }
    }

    // Booking ViewHolder
    class BookingViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSalonName;
        private TextView tvServiceName;
        private TextView tvCustomerName;
        private TextView tvDateTime;
        private Chip chipStatus;

        BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSalonName = itemView.findViewById(R.id.tvSalonName);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            chipStatus = itemView.findViewById(R.id.chipStatus);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    Object item = items.get(getAdapterPosition());
                    if (item instanceof Booking) {
                        listener.onBookingClick((Booking) item);
                    }
                }
            });
        }

        void bind(Booking booking) {
            // Format time only (date is shown in header)
            String time = timeFormat.format(new java.util.Date(booking.getTimestamp()));
            tvDateTime.setText(time);

            // Status chip
            String status = booking.getStatus();
            chipStatus.setText(getStatusText(status));
            chipStatus.setChipBackgroundColorResource(getStatusColor(status));

            // Load salon name from cache or Firestore
            tvSalonName.setText("Đang tải...");
            Salon salon = findSalonById(booking.getSalonId());
            if (salon != null) {
                tvSalonName.setText(salon.getName());
            } else {
                repo.getSalonById(booking.getSalonId(), new FirebaseRepo.FirebaseCallback<Salon>() {
                    @Override
                    public void onSuccess(Salon salon) {
                        if (salon != null) {
                            tvSalonName.setText(salon.getName());
                        } else {
                            tvSalonName.setText("Không tìm thấy salon");
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        tvSalonName.setText("Không tìm thấy salon");
                    }
                });
            }

            // Load service name
            tvServiceName.setText("Đang tải...");
            repo.getServicesOfSalon(booking.getSalonId(), new FirebaseRepo.FirebaseCallback<List<com.example.prm_be.data.models.Service>>() {
                @Override
                public void onSuccess(List<com.example.prm_be.data.models.Service> services) {
                    if (services != null) {
                        for (com.example.prm_be.data.models.Service service : services) {
                            if (service.getId().equals(booking.getServiceId())) {
                                tvServiceName.setText("Dịch vụ: " + service.getName());
                                return;
                            }
                        }
                    }
                    tvServiceName.setText("Không tìm thấy dịch vụ");
                }

                @Override
                public void onFailure(Exception e) {
                    tvServiceName.setText("Không tìm thấy dịch vụ");
                }
            });

            // Load customer name
            tvCustomerName.setText("Đang tải...");
            repo.getUser(booking.getUserId(), new FirebaseRepo.FirebaseCallback<com.example.prm_be.data.models.User>() {
                @Override
                public void onSuccess(com.example.prm_be.data.models.User user) {
                    if (user != null) {
                        tvCustomerName.setText("Khách hàng: " + user.getName());
                    } else {
                        tvCustomerName.setText("Không tìm thấy khách hàng");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    tvCustomerName.setText("Không tìm thấy khách hàng");
                }
            });
        }

        private Salon findSalonById(String salonId) {
            if (salons == null || salonId == null) return null;
            for (Salon salon : salons) {
                if (salonId.equals(salon.getId())) {
                    return salon;
                }
            }
            return null;
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

