package com.zanjaprogrammer.warungku.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.zanjaprogrammer.warungku.R;
import com.zanjaprogrammer.warungku.data.model.User;
import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder> {
    
    private List<User> employees;
    private OnRemoveClickListener onRemoveClickListener;
    
    public interface OnRemoveClickListener {
        void onRemove(User employee);
    }
    
    public EmployeeAdapter(List<User> employees, OnRemoveClickListener listener) {
        this.employees = employees;
        this.onRemoveClickListener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_employee, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User employee = employees.get(position);
        holder.tvName.setText(employee.name);
        holder.tvEmail.setText(employee.email);
        holder.tvRole.setText(getRoleDisplayName(employee.role));
        
        holder.btnRemove.setOnClickListener(v -> {
            if (onRemoveClickListener != null) {
                onRemoveClickListener.onRemove(employee);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return employees.size();
    }
    
    private String getRoleDisplayName(String role) {
        switch (role) {
            case "manager": return "Manager";
            case "cashier": return "Kasir";
            case "staff": return "Staff";
            default: return role;
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole;
        MaterialButton btnRemove;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}

