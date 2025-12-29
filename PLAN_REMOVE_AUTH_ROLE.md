# Rencana: Hapus Fitur Authentication & Role-Based Access

## Overview
Menghapus semua fitur authentication (login/register), multi-role system, dan manage employees karena tidak berguna tanpa Supabase sync. Aplikasi akan berjalan dalam mode guest-only (tanpa login).

---

## Phase 1: Hapus Authentication Files

### Files yang akan dihapus:
1. `app/src/main/java/com/zanjaprogrammer/warungku/auth/LocalAuthManager.java`
2. `app/src/main/java/com/zanjaprogrammer/warungku/auth/PermissionManager.java`
3. `app/src/main/java/com/zanjaprogrammer/warungku/LoginActivity.java`
4. `app/src/main/java/com/zanjaprogrammer/warungku/ManageEmployeesActivity.java`
5. `app/src/main/java/com/zanjaprogrammer/warungku/data/entity/LocalUser.java`
6. `app/src/main/java/com/zanjaprogrammer/warungku/data/dao/LocalUserDao.java`
7. `app/src/main/res/layout/activity_login.xml`
8. `app/src/main/res/layout/activity_manage_employees.xml`
9. `app/src/main/res/layout/dialog_invite_employee.xml`
10. `app/src/main/res/layout/item_employee.xml`
11. `app/src/main/java/com/zanjaprogrammer/warungku/adapters/EmployeeAdapter.java`

### Action:
- Delete semua file di atas
- Hapus reference dari `AndroidManifest.xml`

---

## Phase 2: Update Database Schema

### File: `app/src/main/java/com/zanjaprogrammer/warungku/data/AppDatabase.java`

**Changes:**
1. Hapus `LocalUser.class` dari `entities` array
2. Hapus `LocalUserDao` abstract method
3. Buat migration baru (version 5 → 6):
   - Drop table `local_users` jika ada
   - Tidak perlu migrasi data karena tidak ada sync

**Migration Script:**
```java
static final Migration MIGRATION_5_6 = new Migration(5, 6) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Drop local_users table (no longer needed)
        database.execSQL("DROP TABLE IF EXISTS local_users");
    }
};
```

---

## Phase 3: Update Activities - Hapus Auth Checks

### Files yang perlu diupdate:

#### 3.1 MainActivity.java
- **Hapus:**
  - Import `LocalAuthManager`
  - `authManager.loadUserFromCache()` calls
  - `updateAuthButton()` method
  - Button login/logout di UI
- **Keep:**
  - Semua fitur lainnya tetap sama

#### 3.2 SellActivity.java
- **Hapus:**
  - Import `LocalAuthManager`
  - Auth checks di `onCreate()`
  - `authManager.loadUserFromCache()` calls
- **Keep:**
  - Semua fitur jual tetap sama

#### 3.3 StockActivity.java
- **Hapus:**
  - Import `LocalAuthManager`
  - Auth checks di `onCreate()`
  - Permission checks untuk export/import (semua bisa akses)
- **Keep:**
  - Semua fitur stok tetap sama

#### 3.4 AddProductActivity.java
- **Hapus:**
  - Import `LocalAuthManager`
  - Auth checks di `onCreate()`
- **Keep:**
  - Semua fitur tambah/edit produk tetap sama

#### 3.5 HistoryActivity.java
- **Hapus:**
  - Import `LocalAuthManager`
  - Auth checks di `onCreate()`
- **Keep:**
  - Semua fitur history tetap sama

#### 3.6 ReportActivity.java
- **Hapus:**
  - Import `LocalAuthManager`
  - Auth checks di `onCreate()`
- **Keep:**
  - Semua fitur laporan tetap sama

#### 3.7 SummaryActivity.java
- **Hapus:**
  - Import `LocalAuthManager`
  - Auth checks di `onCreate()`
  - Permission checks (semua bisa akses)
- **Keep:**
  - Semua fitur ringkasan tetap sama

#### 3.8 ShoppingListActivity.java
- **Hapus:**
  - Import `LocalAuthManager`
  - Auth checks di `onCreate()`
- **Keep:**
  - Semua fitur shopping list tetap sama

---

## Phase 4: Update PermissionManager (Optional)

### Option A: Hapus PermissionManager sepenuhnya
- Hapus file `PermissionManager.java`
- Hapus semua import dan calls ke `PermissionManager`
- Semua fitur bisa diakses tanpa check

### Option B: Simplify PermissionManager (Recommended)
- Buat semua method return `true` (semua fitur bisa diakses)
- Hapus semua logic role-based
- Keep file untuk backward compatibility jika ada reference

**Recommended: Option B** - Lebih aman, tidak perlu cari semua reference

---

## Phase 5: Update AndroidManifest.xml

### Changes:
1. **Hapus Activity declarations:**
   - `<activity>` untuk `LoginActivity`
   - `<activity>` untuk `ManageEmployeesActivity`

2. **Update MainActivity:**
   - Pastikan `MainActivity` tetap sebagai `LAUNCHER`
   - Hapus `android:exported` jika tidak diperlukan

---

## Phase 6: Update UI - Hapus Login/Logout Button

### Files yang perlu diupdate:

#### 6.1 activity_main.xml
- **Hapus:**
  - Button login/logout di hero card
  - Semua reference ke auth button

#### 6.2 activity_summary.xml (jika ada)
- **Hapus:**
  - Button login/logout jika ada
  - Menu manage employees jika ada

---

## Phase 7: Update Navigation - Hapus Manage Employees

### Files yang perlu diupdate:

#### 7.1 MainActivity.java
- **Hapus:**
  - Card "Manage Employees" dari UI
  - Click listener untuk manage employees
  - Intent ke `ManageEmployeesActivity`

#### 7.2 Bottom Navigation (jika ada)
- **Hapus:**
  - Menu item untuk manage employees (jika ada)

---

## Phase 8: Cleanup Data Models

### Files yang bisa dihapus (optional):
1. `app/src/main/java/com/zanjaprogrammer/warungku/data/model/User.java` (jika tidak digunakan lagi)
2. `app/src/main/java/com/zanjaprogrammer/warungku/data/model/Invite.java` (jika tidak digunakan lagi)

### Check:
- Cari semua reference ke `User` dan `Invite` model
- Hapus jika tidak digunakan lagi

---

## Phase 9: Update SharedPreferences

### Cleanup:
- Hapus semua key terkait auth dari SharedPreferences:
  - `WarungKuAuth` preferences (user_id, user_email, user_name, user_role)
  - Bisa dilakukan via migration atau clear di first launch

---

## Phase 10: Testing & Verification

### Checklist:
- [ ] Aplikasi bisa launch tanpa crash
- [ ] Semua fitur utama bisa diakses (Jual, Stok, History, Laporan, Ringkasan, Shopping List)
- [ ] Tidak ada error terkait authentication
- [ ] UI tidak menampilkan button login/logout
- [ ] Tidak ada menu manage employees
- [ ] Database migration berjalan dengan baik
- [ ] Tidak ada reference ke file yang sudah dihapus

---

## Implementation Order

1. **Phase 1**: Hapus authentication files
2. **Phase 2**: Update database schema & migration
3. **Phase 3**: Update semua activities (hapus auth checks)
4. **Phase 4**: Update PermissionManager (simplify)
5. **Phase 5**: Update AndroidManifest.xml
6. **Phase 6**: Update UI (hapus login/logout button)
7. **Phase 7**: Update navigation (hapus manage employees)
8. **Phase 8**: Cleanup data models (optional)
9. **Phase 9**: Cleanup SharedPreferences (optional)
10. **Phase 10**: Testing & verification

---

## Notes

- **Guest Mode**: Aplikasi akan berjalan dalam mode guest-only (tanpa login)
- **No Data Isolation**: Semua data di SQLite bisa diakses oleh siapa saja yang menggunakan device
- **Backward Compatibility**: Pastikan data existing tidak hilang saat migration
- **Error Handling**: Pastikan tidak ada crash jika ada reference ke file yang sudah dihapus

---

## Files Summary

### Files to DELETE:
- `LocalAuthManager.java`
- `PermissionManager.java` (atau simplify)
- `LoginActivity.java`
- `ManageEmployeesActivity.java`
- `LocalUser.java`
- `LocalUserDao.java`
- `EmployeeAdapter.java`
- Layout files untuk login & manage employees

### Files to UPDATE:
- `AppDatabase.java` (migration & remove LocalUser)
- Semua Activities (hapus auth checks)
- `AndroidManifest.xml` (hapus activity declarations)
- Layout files (hapus login/logout button)
- `MainActivity.java` (hapus manage employees card)

### Files to KEEP (no changes):
- `DataRepository.java`
- `Product.java`, `CashFlow.java` entities
- `ProductDao.java`, `CashFlowDao.java`
- Semua ViewModels
- Semua utils & helpers
- Semua fitur utama (Jual, Stok, History, dll)

---

## Estimated Impact

- **Lines of Code Removed**: ~2000-3000 lines
- **Files Deleted**: ~10-15 files
- **Files Modified**: ~10-15 files
- **Risk Level**: Medium (perlu testing menyeluruh)
- **Time Estimate**: 2-3 hours implementation + testing

