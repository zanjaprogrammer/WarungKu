package com.zanjaprogrammer.warungku.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.zanjaprogrammer.warungku.R;

public class CustomToast {
    
    public static void showSuccess(Context context, String message) {
        showCustomToast(context, message, "success");
    }
    
    public static void showError(Context context, String message) {
        showCustomToast(context, message, "error");
    }
    
    public static void showWarning(Context context, String message) {
        showCustomToast(context, message, "warning");
    }
    
    public static void showInfo(Context context, String message) {
        showCustomToast(context, message, "info");
    }
    
    private static void showCustomToast(Context context, String message, String type) {
        try {
            // Create custom toast
            Toast toast = new Toast(context);
            
            // Inflate custom layout
            LayoutInflater inflater = LayoutInflater.from(context);
            View layout = inflater.inflate(R.layout.custom_toast, null);
            
            // Set message
            TextView textView = layout.findViewById(R.id.toast_message);
            textView.setText(message);
            
            // Set background based on type
            switch (type) {
                case "success":
                    layout.setBackgroundResource(R.drawable.bg_toast_success);
                    break;
                case "error":
                    layout.setBackgroundResource(R.drawable.bg_toast_error);
                    break;
                case "warning":
                    layout.setBackgroundResource(R.drawable.bg_toast_warning);
                    break;
                default:
                    layout.setBackgroundResource(R.drawable.bg_toast_info);
                    break;
            }
            
            toast.setView(layout);
            toast.setDuration(Toast.LENGTH_SHORT);
            
            // Position at top of screen
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
            
            toast.show();
        } catch (Exception e) {
            // Fallback to regular toast if custom fails
            Toast regularToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            regularToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
            regularToast.show();
        }
    }
    
    // Convenience method for regular toast at top
    public static void showTop(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }
    
    // Convenience method for long toast at top
    public static void showTopLong(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }
}