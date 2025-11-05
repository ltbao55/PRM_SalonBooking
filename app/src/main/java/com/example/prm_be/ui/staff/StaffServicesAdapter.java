package com.example.prm_be.ui.staff;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_be.R;
import com.example.prm_be.data.models.Service;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StaffServicesAdapter extends RecyclerView.Adapter<StaffServicesAdapter.VH> {

    interface ServiceListener {
        void onEdit(Service service);
        void onDelete(Service service);
    }

    private final List<Service> items = new ArrayList<>();
    private final ServiceListener listener;

    public StaffServicesAdapter(ServiceListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Service> services) {
        items.clear();
        if (services != null) items.addAll(services);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff_service, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvDuration;
        ImageButton btnEdit, btnDelete;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvPrice = itemView.findViewById(R.id.tvServicePrice);
            tvDuration = itemView.findViewById(R.id.tvServiceDuration);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(Service s) {
            tvName.setText(s.getName());
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvPrice.setText(nf.format(s.getPrice()));
            tvDuration.setText(s.getDurationInMinutes() + " phút");

            itemView.setOnClickListener(v -> { if (listener != null) listener.onEdit(s); });
            btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(s); });
            btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(s); });
        }
    }
}


