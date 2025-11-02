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

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị danh sách Service trong BookingActivity
 */
public class ServiceBookingAdapter extends RecyclerView.Adapter<ServiceBookingAdapter.ServiceViewHolder> {

    private List<Service> serviceList;
    private OnServiceClickListener listener;
    private int selectedPosition = -1;

    public interface OnServiceClickListener {
        void onServiceClick(Service service, int position);
    }

    public ServiceBookingAdapter() {
        this.serviceList = new ArrayList<>();
    }

    public void setServiceList(List<Service> serviceList) {
        this.serviceList = serviceList != null ? serviceList : new ArrayList<>();
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setOnServiceClickListener(OnServiceClickListener listener) {
        this.listener = listener;
    }

    public Service getSelectedService() {
        if (selectedPosition >= 0 && selectedPosition < serviceList.size()) {
            return serviceList.get(selectedPosition);
        }
        return null;
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
        holder.bind(service, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView tvServiceName;
        private TextView tvPrice;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardService);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }

        public void bind(Service service, boolean isSelected) {
            if (service != null) {
                tvServiceName.setText(service.getName());
                
                // Format price
                long price = service.getPrice();
                String priceText = formatPrice(price);
                tvPrice.setText(priceText);

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
                        
                        if (previousSelected != -1) {
                            notifyItemChanged(previousSelected);
                        }
                        notifyItemChanged(selectedPosition);
                        
                        if (listener != null) {
                            listener.onServiceClick(service, position);
                        }
                    }
                });
            }
        }

        private String formatPrice(long price) {
            return String.format("%,d đ", price);
        }
    }
}

