# Phase 2: Authentication & Permission Checks
## Update Activities untuk Multi-User Support

## ✅ Status
- [x] Phase 1: Firebase Setup & Authentication (COMPLETE)
- [ ] Phase 2: Authentication & Permission Checks (IN PROGRESS)
- [ ] Phase 3: Firestore Sync Service
- [ ] Phase 4: Invite Employee Flow

---

## 🎯 Tujuan Phase 2

1. **Authentication Checks**: Semua activities harus check apakah user sudah login
2. **Permission Checks**: Hide/disable UI berdasarkan user role
3. **Role-Based UI**: Tampilkan hanya fitur yang sesuai dengan role

---

## 📋 Activities yang Perlu Di-Update

### 1. **MainActivity** ✅ (Already has auth check)
- [x] Check authentication di onCreate
- [ ] Add permission checks untuk fitur tertentu
- [ ] Hide "Tambah Produk" button jika bukan owner/manager

### 2. **SellActivity**
- [ ] Check authentication
- [ ] Check permission: `canSell()` - hanya owner, manager, cashier
- [ ] Hide/disable fitur jika tidak punya permission

### 3. **StockActivity**
- [ ] Check authentication
- [ ] Check permission: `canAccessStock()` - semua role bisa akses
- [ ] Check permission: `canAddEditProduct()` - hanya owner, manager
- [ ] Hide "Tambah Produk" button jika tidak punya permission
- [ ] Hide "Export/Import" menu jika bukan owner/manager

### 4. **HistoryActivity (Money)**
- [ ] Check authentication
- [ ] Check permission: `canAccessMoney()` - semua role bisa akses
- [ ] Hide fitur edit/delete jika bukan owner

### 5. **SummaryActivity**
- [ ] Check authentication
- [ ] Check permission: `canAccessSummary()` - hanya owner, manager
- [ ] Redirect jika tidak punya permission

### 6. **AddProductActivity**
- [ ] Check authentication
- [ ] Check permission: `canAddEditProduct()` - hanya owner, manager
- [ ] Redirect jika tidak punya permission

### 7. **ReportActivity**
- [ ] Check authentication
- [ ] Check permission: `canViewReports()` - hanya owner, manager
- [ ] Redirect jika tidak punya permission

### 8. **ShoppingListActivity**
- [ ] Check authentication
- [ ] Check permission: `canAccessShoppingList()` - semua role bisa akses

---

## 🔧 Implementation Strategy

### Step 1: Create Base Activity (Optional)
Buat `BaseActivity` dengan common authentication check:
```java
public abstract class BaseActivity extends AppCompatActivity {
    protected AuthManager authManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authManager = AuthManager.getInstance(getApplication());
        
        if (!authManager.isLoggedIn()) {
            authManager.loadUserFromCache();
            if (!authManager.isLoggedIn()) {
                redirectToLogin();
                return;
            }
        }
    }
    
    protected void redirectToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
    
    protected boolean hasPermission(String permission) {
        String role = authManager.getCurrentUserRole();
        return PermissionManager.hasPermission(role, permission);
    }
}
```

### Step 2: Update Each Activity
- Add authentication check di `onCreate()`
- Add permission checks untuk specific actions
- Hide/disable UI elements berdasarkan permissions

### Step 3: Add Permission Checks to PermissionManager
Extend `PermissionManager` dengan helper methods:
```java
public static boolean hasPermission(String role, String permission) {
    // Check specific permission
}
```

---

## 🎨 UI Changes Based on Role

### Owner (Full Access)
- ✅ Semua fitur tersedia
- ✅ Semua tombol visible dan enabled

### Manager
- ✅ Bisa lihat laporan
- ✅ Bisa manage produk
- ❌ Tidak bisa hapus data penting
- ❌ Tidak bisa manage employees

### Cashier
- ✅ Bisa jual barang
- ❌ Tidak bisa edit produk/harga
- ❌ Tidak bisa lihat laporan
- ❌ Tidak bisa manage stok

### Staff
- ✅ Bisa lihat stok
- ❌ Tidak bisa jual
- ❌ Tidak bisa edit produk
- ❌ Tidak bisa lihat laporan

---

## 📝 Implementation Order

1. **Update PermissionManager** - Add helper methods
2. **Update MainActivity** - Add permission checks
3. **Update SellActivity** - Add auth & permission checks
4. **Update StockActivity** - Add auth & permission checks
5. **Update HistoryActivity** - Add auth & permission checks
6. **Update SummaryActivity** - Add auth & permission checks
7. **Update AddProductActivity** - Add auth & permission checks
8. **Update ReportActivity** - Add auth & permission checks
9. **Update ShoppingListActivity** - Add auth & permission checks

---

## ✅ Testing Checklist

Setelah implementasi, test dengan:
- [ ] Owner bisa akses semua fitur
- [ ] Manager tidak bisa akses fitur yang dilarang
- [ ] Cashier hanya bisa jual barang
- [ ] Staff hanya bisa lihat stok
- [ ] Unauthenticated user redirect ke LoginActivity

---

**Ready untuk implementasi!** 🚀

