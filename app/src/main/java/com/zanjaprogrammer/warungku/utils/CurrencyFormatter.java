package com.zanjaprogrammer.warungku.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class CurrencyFormatter {
    private static final DecimalFormat formatter;
    
    static {
        // Create custom symbols for Indonesian Rupiah format
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(','); // Temporary use comma
        symbols.setDecimalSeparator('.');   
        
        // Pattern tanpa desimal
        formatter = new DecimalFormat("#,##0", symbols);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
    }
    
    public static String format(double amount) {
        String formatted = formatter.format(amount);
        // Replace comma with dot for Indonesian format
        formatted = formatted.replace(',', '.');
        return "Rp " + formatted;
    }
    
    public static String format(int amount) {
        String formatted = formatter.format(amount);
        // Replace comma with dot for Indonesian format  
        formatted = formatted.replace(',', '.');
        return "Rp " + formatted;
    }
    
    public static String formatPlain(double amount) {
        String formatted = formatter.format(amount);
        // Replace comma with dot for Indonesian format
        formatted = formatted.replace(',', '.');
        return formatted;
    }
    
    public static String formatPlain(int amount) {
        String formatted = formatter.format(amount);
        // Replace comma with dot for Indonesian format  
        formatted = formatted.replace(',', '.');
        return formatted;
    }
}