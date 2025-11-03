package com.example.prm_be.ui.discovery;

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
import java.util.Locale;

/**
 * Adapter cho RecyclerView hiển thị danh sách Service trong SalonDetailActivity
 */
public class ServiceDetailAdapter extends RecyclerView.Adapter<ServiceDetailAdapter.ServiceViewHolder> {

    private List<Service> serviceList;

    public ServiceDetailAdapter() {
        this.serviceList = new ArrayList<>();
    }

    public void setServiceList(List<Service> serviceList) {
        this.serviceList = serviceList != null ? serviceList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_detail, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);
        holder.bind(service);
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardService;
        private TextView tvServiceName;
        private TextView tvPrice;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardService = itemView.findViewById(R.id.cardService);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }

        public void bind(Service service) {
            if (service != null) {
                tvServiceName.setText(service.getName());
                
                // Format price
                String priceText = String.format(Locale.getDefault(), "%,d đ", service.getPrice());
                tvPrice.setText(priceText);
            }
        }
    }
}

