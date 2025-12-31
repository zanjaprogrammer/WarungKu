package com.zanjaprogrammer.warungku.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zanjaprogrammer.warungku.R;
import com.zanjaprogrammer.warungku.data.entity.PaymentProof;
import com.zanjaprogrammer.warungku.databinding.ItemPaymentProofManagementBinding;
import com.zanjaprogrammer.warungku.service.PaymentProofManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for payment proof management grid view
 * Supports selection mode and displays payment proof thumbnails
 */
public class PaymentProofManagementAdapter extends RecyclerView.Adapter<PaymentProofManagementAdapter.ViewHolder> {
    
    private List<PaymentProof> paymentProofs = new ArrayList<>();
    private List<Long> selectedIds = new ArrayList<>();
    private boolean isSelectionMode = false;
    
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnSelectionChangedListener onSelectionChangedListener;
    
    private PaymentProofManager paymentProofManager;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
    
    public interface OnItemClickListener {
        void onItemClick(PaymentProof paymentProof);
    }
    
    public interface OnItemLongClickListener {
        boolean onItemLongClick(PaymentProof paymentProof);
    }
    
    public interface OnSelectionChangedListener {
        void onSelectionChanged(List<Long> selectedIds);
    }
    
    public void setPaymentProofs(List<PaymentProof> paymentProofs) {
        this.paymentProofs = paymentProofs != null ? paymentProofs : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setSelectionMode(boolean selectionMode) {
        this.isSelectionMode = selectionMode;
        if (!selectionMode) {
            selectedIds.clear();
        }
        notifyDataSetChanged();
    }
    
    public void setSelectedIds(List<Long> selectedIds) {
        this.selectedIds = selectedIds != null ? selectedIds : new ArrayList<>();
        notifyDataSetChanged();
        
        if (onSelectionChangedListener != null) {
            onSelectionChangedListener.onSelectionChanged(this.selectedIds);
        }
    }
    
    public void clearSelection() {
        selectedIds.clear();
        notifyDataSetChanged();
        
        if (onSelectionChangedListener != null) {
            onSelectionChangedListener.onSelectionChanged(selectedIds);
        }
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }
    
    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.onSelectionChangedListener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPaymentProofManagementBinding binding = ItemPaymentProofManagementBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        
        // Initialize PaymentProofManager if not already done
        if (paymentProofManager == null) {
            paymentProofManager = new PaymentProofManager(parent.getContext());
        }
        
        return new ViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentProof paymentProof = paymentProofs.get(position);
        holder.bind(paymentProof);
    }
    
    @Override
    public int getItemCount() {
        return paymentProofs.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPaymentProofManagementBinding binding;
        
        ViewHolder(ItemPaymentProofManagementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        void bind(PaymentProof paymentProof) {
            // Set date
            binding.tvDate.setText(dateFormat.format(new Date(paymentProof.getCaptureTimestamp())));
            
            // Set file size
            binding.tvFileSize.setText(formatFileSize(paymentProof.getFileSize()));
            
            // Handle selection mode
            if (isSelectionMode) {
                binding.ivSelectionIndicator.setVisibility(View.VISIBLE);
                boolean isSelected = selectedIds.contains(paymentProof.getId());
                binding.overlaySelection.setVisibility(isSelected ? View.VISIBLE : View.GONE);
                
                // Update selection indicator
                if (isSelected) {
                    binding.ivSelectionIndicator.setImageResource(R.drawable.ic_check_circle);
                    binding.ivSelectionIndicator.setColorFilter(androidx.core.content.ContextCompat.getColor(
                        binding.getRoot().getContext(), android.R.color.white));
                    binding.ivSelectionIndicator.setBackgroundResource(R.drawable.bg_button_icon_circle);
                } else {
                    binding.ivSelectionIndicator.setImageResource(R.drawable.ic_radio_button_unchecked);
                    binding.ivSelectionIndicator.setColorFilter(androidx.core.content.ContextCompat.getColor(
                        binding.getRoot().getContext(), android.R.color.white));
                    binding.ivSelectionIndicator.setBackgroundResource(R.drawable.bg_selection_indicator);
                }
            } else {
                binding.ivSelectionIndicator.setVisibility(View.GONE);
                binding.overlaySelection.setVisibility(View.GONE);
            }
            
            // Handle corrupted state
            if (paymentProof.isCorrupted()) {
                binding.ivThumbnail.setImageResource(R.drawable.ic_image_placeholder);
                binding.ivThumbnail.setAlpha(0.5f);
                binding.overlayCorrupted.setVisibility(View.VISIBLE);
                binding.tvCorrupted.setVisibility(View.VISIBLE);
            } else {
                binding.ivThumbnail.setAlpha(1.0f);
                binding.overlayCorrupted.setVisibility(View.GONE);
                binding.tvCorrupted.setVisibility(View.GONE);
                
                // Load thumbnail
                loadThumbnail(paymentProof);
            }
            
            // Set click listeners
            binding.getRoot().setOnClickListener(v -> {
                if (isSelectionMode) {
                    toggleSelection(paymentProof);
                } else if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(paymentProof);
                }
            });
            
            binding.getRoot().setOnLongClickListener(v -> {
                if (onItemLongClickListener != null) {
                    return onItemLongClickListener.onItemLongClick(paymentProof);
                }
                return false;
            });
            
            // Selection indicator click listener (only when visible)
            if (isSelectionMode) {
                binding.ivSelectionIndicator.setOnClickListener(v -> toggleSelection(paymentProof));
            } else {
                binding.ivSelectionIndicator.setOnClickListener(null);
            }
        }
        
        private void loadThumbnail(PaymentProof paymentProof) {
            // Show loading placeholder first
            binding.ivThumbnail.setImageResource(R.drawable.ic_image_placeholder);
            
            // Load actual image asynchronously
            if (paymentProofManager != null) {
                paymentProofManager.loadPaymentProofPhoto(paymentProof)
                    .thenAccept(bitmap -> {
                        // Update UI on main thread
                        binding.getRoot().post(() -> {
                            if (bitmap != null) {
                                // Create thumbnail from bitmap
                                Bitmap thumbnail = createThumbnail(bitmap);
                                binding.ivThumbnail.setImageBitmap(thumbnail);
                                
                                // Clean up original bitmap if it's different from thumbnail
                                if (thumbnail != bitmap) {
                                    bitmap.recycle();
                                }
                            } else {
                                // Failed to load, show placeholder
                                binding.ivThumbnail.setImageResource(R.drawable.ic_image_placeholder);
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        // Error loading image, show placeholder
                        binding.getRoot().post(() -> {
                            binding.ivThumbnail.setImageResource(R.drawable.ic_image_placeholder);
                        });
                        return null;
                    });
            }
        }
        
        private Bitmap createThumbnail(Bitmap original) {
            if (original == null) return null;
            
            // Calculate thumbnail size (square, 200x200 max)
            int size = Math.min(original.getWidth(), original.getHeight());
            int thumbnailSize = Math.min(size, 200);
            
            // Create square thumbnail
            int x = (original.getWidth() - size) / 2;
            int y = (original.getHeight() - size) / 2;
            
            Bitmap cropped = Bitmap.createBitmap(original, x, y, size, size);
            Bitmap thumbnail = Bitmap.createScaledBitmap(cropped, thumbnailSize, thumbnailSize, true);
            
            // Clean up intermediate bitmap if different
            if (cropped != original && cropped != thumbnail) {
                cropped.recycle();
            }
            
            return thumbnail;
        }
        
        private void toggleSelection(PaymentProof paymentProof) {
            if (selectedIds.contains(paymentProof.getId())) {
                selectedIds.remove(paymentProof.getId());
            } else {
                selectedIds.add(paymentProof.getId());
            }
            
            // Update selection indicator state
            boolean isSelected = selectedIds.contains(paymentProof.getId());
            binding.overlaySelection.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            
            // Update selection indicator icon
            if (isSelected) {
                binding.ivSelectionIndicator.setImageResource(R.drawable.ic_check_circle);
                binding.ivSelectionIndicator.setColorFilter(androidx.core.content.ContextCompat.getColor(
                    binding.getRoot().getContext(), android.R.color.white));
                binding.ivSelectionIndicator.setBackgroundResource(R.drawable.bg_button_icon_circle);
            } else {
                binding.ivSelectionIndicator.setImageResource(R.drawable.ic_radio_button_unchecked);
                binding.ivSelectionIndicator.setColorFilter(androidx.core.content.ContextCompat.getColor(
                    binding.getRoot().getContext(), android.R.color.white));
                binding.ivSelectionIndicator.setBackgroundResource(R.drawable.bg_selection_indicator);
            }
            
            // Notify listener
            if (onSelectionChangedListener != null) {
                onSelectionChangedListener.onSelectionChanged(selectedIds);
            }
        }
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0);
        return String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0));
    }
}