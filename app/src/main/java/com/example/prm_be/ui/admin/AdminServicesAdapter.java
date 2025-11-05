package com.example.prm_be.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.models.Service;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminServicesAdapter extends RecyclerView.Adapter<AdminServicesAdapter.ViewHolder> {
    private List<Service> services = new ArrayList<>();
    private OnServiceEditListener editListener;
    private OnServiceDeleteListener deleteListener;

    public interface OnServiceEditListener {
        void onEdit(Service service);
    }

    public interface OnServiceDeleteListener {
        void onDelete(Service service);
    }

    public AdminServicesAdapter(OnServiceEditListener editListener, OnServiceDeleteListener deleteListener) {
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    public void setServices(List<Service> services) {
        this.services = services != null ? services : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Service service = services.get(position);
        holder.bind(service);
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvServiceName;
        private TextView tvServicePrice;
        private TextView tvServiceDuration;
        private MaterialButton btnEditService;
        private MaterialButton btnDeleteService;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            tvServiceDuration = itemView.findViewById(R.id.tvServiceDuration);
            btnEditService = itemView.findViewById(R.id.btnEditService);
            btnDeleteService = itemView.findViewById(R.id.btnDeleteService);
        }

        void bind(Service service) {
            tvServiceName.setText(service.getName());
            
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            tvServicePrice.setText(formatter.format(service.getPrice()) + " VND");
            
            tvServiceDuration.setText(service.getDurationInMinutes() + " phút");

            btnEditService.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onEdit(service);
                }
            });

            btnDeleteService.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(service);
                }
            });
        }
    }
}

