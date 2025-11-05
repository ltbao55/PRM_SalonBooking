package com.example.prm_be.ui.booking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.models.Service;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter cho RecyclerView hiển thị danh sách Service trong BookingActivity
 */
public class ServiceBookingAdapter extends RecyclerView.Adapter<ServiceBookingAdapter.ServiceViewHolder> {

    private List<Service> serviceList;
    private OnServiceClickListener listener;
    private Set<String> selectedServiceIds; // Set để lưu các service ID đã chọn

    public interface OnServiceClickListener {
        void onServiceClick(Service service, int position, boolean isSelected);
    }

    public ServiceBookingAdapter() {
        this.serviceList = new ArrayList<>();
        this.selectedServiceIds = new HashSet<>();
    }

    public void setServiceList(List<Service> serviceList) {
        this.serviceList = serviceList != null ? serviceList : new ArrayList<>();
        selectedServiceIds.clear();
        notifyDataSetChanged();
    }

    public void setOnServiceClickListener(OnServiceClickListener listener) {
        this.listener = listener;
    }

    public List<Service> getSelectedServices() {
        List<Service> selected = new ArrayList<>();
        for (Service service : serviceList) {
            if (selectedServiceIds.contains(service.getId())) {
                selected.add(service);
            }
        }
        return selected;
    }

    public boolean isServiceSelected(String serviceId) {
        return selectedServiceIds.contains(serviceId);
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_booking, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);
        boolean isSelected = selectedServiceIds.contains(service.getId());
        holder.bind(service, isSelected);
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private MaterialCheckBox checkBox;
        private TextView tvServiceName;
        private TextView tvPrice;
        private TextView tvDuration;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardService);
            checkBox = itemView.findViewById(R.id.checkBoxService);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }

        public void bind(Service service, boolean isSelected) {
            if (service != null) {
                tvServiceName.setText(service.getName());
                
                // Format price
                long price = service.getPrice();
                String priceText = formatPrice(price);
                tvPrice.setText(priceText);

                // Format duration
                int duration = service.getDurationInMinutes();
                tvDuration.setText(duration + " phút");

                // Set checkbox state
                checkBox.setChecked(isSelected);

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
                    toggleServiceSelection(service);
                });

                checkBox.setOnClickListener(v -> {
                    toggleServiceSelection(service);
                });
            }
        }

        private void toggleServiceSelection(Service service) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            boolean wasSelected = selectedServiceIds.contains(service.getId());
            if (wasSelected) {
                selectedServiceIds.remove(service.getId());
            } else {
                selectedServiceIds.add(service.getId());
            }

            notifyItemChanged(position);

            if (listener != null) {
                listener.onServiceClick(service, position, !wasSelected);
            }
        }

        private String formatPrice(long price) {
            return String.format("%,d đ", price);
        }
    }
}

