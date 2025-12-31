package com.zanjaprogrammer.warungku.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.zanjaprogrammer.warungku.PaymentProofViewActivity;
import com.zanjaprogrammer.warungku.R;
import com.zanjaprogrammer.warungku.data.entity.CashFlow;
import com.zanjaprogrammer.warungku.databinding.ItemHistoryBinding;

import com.zanjaprogrammer.warungku.utils.CurrencyFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<CashFlow> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());

    public void setItems(List<CashFlow> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryBinding binding = ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CashFlow item = items.get(position);
        holder.binding.tvDescription.setText(item.description);
        holder.binding.tvDate.setText(dateFormat.format(new Date(item.timestamp)));

        if ("IN".equals(item.type)) {
            holder.binding.tvAmount.setText("+ " + CurrencyFormatter.format(item.amount));
            holder.binding.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
        } else {
            holder.binding.tvAmount.setText("- " + CurrencyFormatter.format(item.amount));
            holder.binding.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.error));
        }
        
        // Show payment proof indicator for QRIS transactions with payment proof
        ImageView paymentProofIcon = holder.binding.ivPaymentProof;
        if (item.hasPaymentProof && "QRIS".equals(item.paymentMethod)) {
            paymentProofIcon.setVisibility(View.VISIBLE);
            
            // Set click listener to open PaymentProofViewActivity
            holder.itemView.setOnClickListener(v -> {
                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, PaymentProofViewActivity.class);
                intent.putExtra(PaymentProofViewActivity.EXTRA_TRANSACTION_ID, (long) item.id);
                context.startActivity(intent);
            });
            
            // Also set click listener on the icon itself
            paymentProofIcon.setOnClickListener(v -> {
                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, PaymentProofViewActivity.class);
                intent.putExtra(PaymentProofViewActivity.EXTRA_TRANSACTION_ID, (long) item.id);
                context.startActivity(intent);
            });
        } else {
            paymentProofIcon.setVisibility(View.GONE);
            // Remove click listener for transactions without payment proof
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemHistoryBinding binding;

        ViewHolder(ItemHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
