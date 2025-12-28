package com.zanjaprogrammer.warungku.auth;

import com.zanjaprogrammer.warungku.data.model.User;

public class PermissionManager {
    
    // Helper method untuk check role string
    private static boolean isOwner(String role) {
        return "owner".equals(role);
    }
    
    private static boolean isManager(String role) {
        return "manager".equals(role);
    }
    
    private static boolean isCashier(String role) {
        return "cashier".equals(role);
    }
    
    private static boolean isStaff(String role) {
        return "staff".equals(role);
    }
    
    // Methods dengan User object (backward compatibility)
    public static boolean canSell(User user) {
        if (user == null) return false;
        return canSell(user.role);
    }

    public static boolean canAddProduct(User user) {
        if (user == null) return false;
        return canAddProduct(user.role);
    }

    public static boolean canEditProduct(User user) {
        if (user == null) return false;
        return canEditProduct(user.role);
    }

    public static boolean canDeleteProduct(User user) {
        if (user == null) return false;
        return canDeleteProduct(user.role);
    }

    public static boolean canRestock(User user) {
        if (user == null) return false;
        return canRestock(user.role);
    }

    public static boolean canViewStock(User user) {
        if (user == null) return false;
        return canViewStock(user.role);
    }

    public static boolean canViewReports(User user) {
        if (user == null) return false;
        return canViewReports(user.role);
    }

    public static boolean canExportImport(User user) {
        if (user == null) return false;
        return canExportImport(user.role);
    }

    public static boolean canManageEmployees(User user) {
        if (user == null) return false;
        return canManageEmployees(user.role);
    }

    public static boolean canChangePrice(User user) {
        if (user == null) return false;
        return canChangePrice(user.role);
    }

    public static boolean canAddExpense(User user) {
        if (user == null) return false;
        return canAddExpense(user.role);
    }

    public static boolean canBackupRestore(User user) {
        if (user == null) return false;
        return canBackupRestore(user.role);
    }

    public static boolean canAccessSettings(User user) {
        if (user == null) return false;
        return canAccessSettings(user.role);
    }
    
    // Methods dengan role string (new, preferred)
    public static boolean canSell(String role) {
        if (role == null) return false;
        return isOwner(role) || isManager(role) || isCashier(role);
    }

    public static boolean canAddProduct(String role) {
        if (role == null) return false;
        return isOwner(role) || isManager(role);
    }

    public static boolean canEditProduct(String role) {
        if (role == null) return false;
        return isOwner(role) || isManager(role);
    }

    public static boolean canDeleteProduct(String role) {
        if (role == null) return false;
        return isOwner(role);
    }

    public static boolean canRestock(String role) {
        if (role == null) return false;
        return isOwner(role) || isManager(role);
    }

    public static boolean canViewStock(String role) {
        if (role == null) return false;
        return true; // Semua role bisa lihat stok
    }

    public static boolean canViewReports(String role) {
        if (role == null) return false;
        return isOwner(role) || isManager(role);
    }

    public static boolean canExportImport(String role) {
        if (role == null) return false;
        return isOwner(role) || isManager(role);
    }

    public static boolean canManageEmployees(String role) {
        if (role == null) return false;
        return isOwner(role);
    }

    public static boolean canChangePrice(String role) {
        if (role == null) return false;
        return isOwner(role) || isManager(role);
    }

    public static boolean canAddExpense(String role) {
        if (role == null) return false;
        return isOwner(role) || isManager(role);
    }

    public static boolean canBackupRestore(String role) {
        if (role == null) return false;
        return isOwner(role);
    }

    public static boolean canAccessSettings(String role) {
        if (role == null) return false;
        return isOwner(role);
    }
    
    // Additional permission checks
    public static boolean canAccessMoney(String role) {
        if (role == null) return false;
        return true; // Semua role bisa akses halaman uang
    }
    
    public static boolean canAccessSummary(String role) {
        if (role == null) return false;
        return isOwner(role) || isManager(role);
    }
    
    public static boolean canAccessShoppingList(String role) {
        if (role == null) return false;
        return true; // Semua role bisa akses shopping list
    }
}

