# Rencana: SQLite Authentication & Role-Based Access

## 🎯 Tujuan
- **Hapus Supabase authentication**, ganti dengan **SQLite authentication**
- **1 device = 1 warung** (karena SQLite lokal)
- **Isolasi per ROLE**: setiap role hanya bisa melakukan action sesuai permission mereka
- Semua user di device yang sama melihat data yang sama (karena 1 warung)
- Fokus ke SQLite (local storage) untuk semua fitur

---

## 📋 Analisis & Perubahan

### 1. **File yang Akan Dihapus**
- ❌ `app/src/main/java/com/zanjaprogrammer/warungku/sync/SyncManager.java`
- ❌ `app/src/main/java/com/zanjaprogrammer/warungku/supabase/SupabaseSyncService.java`
- ❌ `app/src/main/java/com/zanjaprogrammer/warungku/supabase/SupabaseAuthManager.java` (ganti dengan SQLite)
- ❌ `app/src/main/java/com/zanjaprogrammer/warungku/supabase/SupabaseClient.java` (tidak diperlukan lagi)
- ❌ `app/src/main/java/com/zanjaprogrammer/warungku/supabase/api/` (semua file API Supabase)

### 2. **File Baru yang Akan Dibuat**

#### A. **User Entity (SQLite)**
- `app/src/main/java/com/zanjaprogrammer/warungku/data/entity/LocalUser.java`
  - Fields: `id`, `email`, `password` (hashed), `name`, `role`, `createdAt`, `isActive`
  - Table: `local_users`
  - **Tidak perlu `warungId`** karena 1 device = 1 warung
  - **Role**: 'owner', 'manager', 'cashier' (staff dihapus)

#### B. **User DAO**
- `app/src/main/java/com/zanjaprogrammer/warungku/data/dao/LocalUserDao.java`
  - Methods: `insert`, `update`, `delete`, `getUserByEmail`, `getUserById`, `getAllUsers`

#### C. **Local Auth Manager**
- `app/src/main/java/com/zanjaprogrammer/warungku/auth/LocalAuthManager.java`
  - Methods: `login`, `register`, `logout`, `getCurrentUser`, `isLoggedIn`, `getCurrentUserRole`
  - Menggunakan SQLite untuk authentication
  - Menggunakan SharedPreferences untuk session

### 3. **File yang Perlu Dimodifikasi**

#### A. **Entity Classes** (Hapus sync fields saja)
- `app/src/main/java/com/zanjaprogrammer/warungku/data/entity/Product.java`
  - ❌ Hapus: `synced`, `lastSyncedAt`, `cloudId`
  - ✅ **TIDAK perlu** tambah `warungId` atau `createdBy` (karena 1 device = 1 warung)

- `app/src/main/java/com/zanjaprogrammer/warungku/data/entity/CashFlow.java`
  - ❌ Hapus: `synced`, `lastSyncedAt`, `cloudId`
  - ✅ **TIDAK perlu** tambah `warungId` atau `createdBy` (karena 1 device = 1 warung)

#### B. **AppDatabase.java**
- ✅ Tambahkan: `LocalUser` ke entities
- ✅ Tambahkan: `LocalUserDao` ke abstract methods
- ✅ Update version: dari 4 ke 5
- ✅ Buat migration: `MIGRATION_4_5`
  - Drop sync columns dari `products` dan `cash_flow`
  - Create table `local_users`

#### C. **ProductDao.java & CashFlowDao.java**
- ✅ **TIDAK perlu** update query untuk filter (karena semua user melihat data yang sama)
- ✅ Tetap menggunakan query yang sama seperti sekarang

#### D. **DataRepository.java**
- ❌ Hapus: `import com.zanjaprogrammer.warungku.sync.SyncManager;`
- ❌ Hapus: semua `SyncManager.triggerSync(application);`
- ❌ Hapus: `getUnsyncedProducts()` dan `getUnsyncedCashFlows()`
- ✅ **TIDAK perlu** update untuk set `warungId` (karena tidak ada)
- ✅ Tetap menggunakan operasi database yang sama

#### E. **LoginActivity.java**
- ❌ Hapus: semua import Supabase
- ❌ Hapus: `SupabaseAuthManager`, `SupabasePostgrestApi`
- ✅ Ganti dengan: `LocalAuthManager`
- ✅ Update: `performLogin()` untuk menggunakan SQLite
- ✅ Update: `performRegister()` untuk menggunakan SQLite
- ✅ Update: `registerAsOwner()` - hanya create user dengan role 'owner'
- ✅ Update: `registerAsEmployee()` - create user dengan role dari invite (jika ada)
- ✅ Hapus: `syncLocalDataToSupabase()`
- ✅ **TIDAK perlu** create warung (karena 1 device = 1 warung)

#### F. **MainActivity.java**
- ❌ Hapus: `SyncManager.triggerSync(this);`
- ✅ Update: ganti `SupabaseAuthManager` dengan `LocalAuthManager`
- ✅ Update: semua reference ke auth manager

#### G. **Semua Activities** (SellActivity, StockActivity, dll)
- ✅ Update: ganti `SupabaseAuthManager` dengan `LocalAuthManager`
- ✅ Update: semua reference ke auth manager
- ✅ **Permission checks tetap sama** (menggunakan PermissionManager)

#### H. **PermissionManager.java**
- ❌ Hapus: method `isStaff(String role)` (tidak digunakan lagi)
- ✅ Tetap sama untuk role lainnya (tidak perlu perubahan)
- ✅ Menggunakan role dari `LocalAuthManager`
- ✅ Setiap role hanya bisa melakukan action sesuai permission mereka

#### I. **ManageEmployeesActivity.java**
- ✅ Update: ganti `SupabaseAuthManager` dengan `LocalAuthManager`
- ✅ Update: query employees dari SQLite (bukan Supabase)
- ✅ Update: create user dengan role tertentu (manager, cashier) - **staff dihapus**
- ✅ **TIDAK perlu** invite system (atau bisa tetap ada untuk future)

#### J. **AndroidManifest.xml**
- ❌ Hapus: `<service>` untuk `SupabaseSyncService`
- ✅ Pertahankan: semua lainnya

---

## 🔐 Role-Based Access (Bukan Isolasi Data)

### **Konsep:**
- **1 device = 1 warung** (karena SQLite lokal)
- **Semua user melihat data yang sama** (karena 1 warung)
- **Isolasi per ROLE**: setiap role hanya bisa melakukan action sesuai permission mereka

### **Role Permissions (dari PermissionManager):**
- **Owner**: Semua akses (manage employees, delete product, dll)
- **Manager**: Bisa jual, tambah/edit produk, restock, lihat laporan, export/import
- **Cashier**: Bisa jual, lihat stok
- **Guest**: Bisa semua kecuali manage employees
- **Staff**: ❌ **DIHAPUS** - tidak digunakan lagi

### **Contoh:**
- Owner dan Manager melihat **data yang sama** (semua produk, semua cash flow)
- Tapi Owner bisa **delete product**, Manager **tidak bisa**
- Cashier bisa **jual produk**, Staff **tidak bisa**
- Semua melihat **data yang sama**, tapi **action yang berbeda**

---

## 🔧 Langkah-Langkah Implementasi

### **Phase 1: Buat Entity & DAO untuk User**
1. ✅ Buat `LocalUser.java` entity
2. ✅ Buat `LocalUserDao.java`
3. ✅ Update `AppDatabase.java` untuk include entity baru

### **Phase 2: Buat LocalAuthManager**
1. ✅ Buat `LocalAuthManager.java`
2. ✅ Implementasi: `login()`, `register()`, `logout()`
3. ✅ Implementasi: `getCurrentUser()`, `isLoggedIn()`, `getCurrentUserRole()`
4. ✅ Implementasi: password hashing (BCrypt atau SHA-256)

### **Phase 3: Update Entity (Hapus Sync Fields)**
1. ✅ Update `Product.java`: hapus `synced`, `lastSyncedAt`, `cloudId`
2. ✅ Update `CashFlow.java`: hapus `synced`, `lastSyncedAt`, `cloudId`
3. ✅ Update constructor untuk tidak set fields tersebut

### **Phase 4: Database Migration**
1. ✅ Buat `MIGRATION_4_5` di `AppDatabase.java`
2. ✅ Drop sync columns dari `products` dan `cash_flow`
3. ✅ Create table `local_users`

### **Phase 5: Update DataRepository**
1. ✅ Hapus semua sync calls
2. ✅ Hapus `getUnsyncedProducts()` dan `getUnsyncedCashFlows()`
3. ✅ **TIDAK perlu** update untuk set `warungId` (karena tidak ada)

### **Phase 6: Update LoginActivity**
1. ✅ Ganti `SupabaseAuthManager` dengan `LocalAuthManager`
2. ✅ Update `performLogin()` untuk SQLite
3. ✅ Update `performRegister()` untuk SQLite
4. ✅ Update `registerAsOwner()` - hanya create user dengan role 'owner'
5. ✅ Update `registerAsEmployee()` - create user dengan role tertentu
6. ✅ Hapus: `syncLocalDataToSupabase()`

### **Phase 7: Update Semua Activities**
1. ✅ Ganti `SupabaseAuthManager` dengan `LocalAuthManager` di semua activities
2. ✅ Update semua reference ke auth manager

### **Phase 8: Hapus Supabase Files**
1. ❌ Delete `SupabaseAuthManager.java`
2. ❌ Delete `SupabaseSyncService.java`
3. ❌ Delete `SyncManager.java`
4. ❌ Delete `SupabaseClient.java`
5. ❌ Delete semua file di `supabase/api/`
6. ❌ Hapus service dari `AndroidManifest.xml`

### **Phase 9: Hapus Reference ke Role 'staff'**
1. ❌ Hapus method `isStaff(String role)` dari `PermissionManager.java`
2. ❌ Hapus method `isStaff()` dari `User.java` (jika ada)
3. ❌ Hapus option 'staff' dari UI (jika ada di ManageEmployeesActivity atau tempat lain)
4. ✅ Update dokumentasi/comments yang menyebutkan role 'staff'

### **Phase 10: Testing**
1. ✅ Test register owner
2. ✅ Test register employee (dengan role tertentu: manager, cashier)
3. ✅ Test login/logout
4. ✅ Test switch user (logout → login user lain)
5. ✅ Test role-based permissions (owner vs manager vs cashier)
6. ✅ Test guest mode
7. ✅ Test manage employees (hanya owner)
8. ✅ Test CRUD operations dengan permission checks

---

## 📊 Struktur Database SQLite

### **Table: local_users**
```sql
CREATE TABLE local_users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,  -- hashed
    name TEXT,
    role TEXT NOT NULL,  -- 'owner', 'manager', 'cashier' (staff dihapus)
    createdAt INTEGER NOT NULL,
    isActive INTEGER NOT NULL DEFAULT 1
);
```

### **Table: products** (updated)
```sql
-- Remove: synced, lastSyncedAt, cloudId
-- TIDAK perlu: warungId, createdBy (karena 1 device = 1 warung)
CREATE TABLE products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    sellPrice REAL NOT NULL,
    buyPrice REAL,
    currentStock INTEGER NOT NULL,
    minStock INTEGER NOT NULL,
    salesCount INTEGER NOT NULL DEFAULT 0,
    isFavorite INTEGER NOT NULL DEFAULT 0,
    lastSoldTimestamp INTEGER NOT NULL DEFAULT 0,
    barcode TEXT
);
```

### **Table: cash_flow** (updated)
```sql
-- Remove: synced, lastSyncedAt, cloudId
-- TIDAK perlu: warungId, createdBy (karena 1 device = 1 warung)
CREATE TABLE cash_flow (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL,  -- 'IN' or 'OUT'
    amount REAL NOT NULL,
    description TEXT,
    timestamp INTEGER NOT NULL,
    productId INTEGER,
    profit REAL,
    FOREIGN KEY(productId) REFERENCES products(id)
);
```

---

## 🔐 Password Hashing

### **Opsi 1: BCrypt (Recommended)**
- Library: `org.mindrot:jbcrypt:0.4`
- Secure, slow (good for password hashing)

### **Opsi 2: SHA-256 + Salt**
- Built-in Java
- Faster, but less secure than BCrypt

### **Rekomendasi:**
- Gunakan BCrypt untuk production
- Atau SHA-256 + salt untuk simplicity

---

## ⚠️ Catatan Penting

### **1 Device = 1 Warung:**
- ✅ Semua user di device yang sama melihat **data yang sama**
- ✅ Tidak perlu isolasi data per warung (karena hanya ada 1 warung)
- ✅ Tidak perlu field `warungId` di Product dan CashFlow

### **Isolasi per ROLE (Permission-Based):**
- ✅ Setiap role hanya bisa melakukan **action** sesuai permission mereka
- ✅ Semua role melihat **data yang sama**, tapi **action yang berbeda**
- ✅ Permission checks menggunakan `PermissionManager` (sudah ada)

### **Guest Mode:**
- ✅ Guest tidak perlu login
- ✅ Guest melihat semua data (karena guest mode)
- ✅ Guest bisa melakukan semua action kecuali manage employees

### **Multi User:**
- ✅ Bisa ada multiple users di 1 device (owner, manager, cashier, staff)
- ✅ Setiap user bisa login/logout
- ✅ Setiap user melihat data yang sama, tapi action berbeda sesuai role

---

## ✅ Checklist Implementasi

- [ ] Phase 1: Buat Entity & DAO untuk User
- [ ] Phase 2: Buat LocalAuthManager
- [ ] Phase 3: Update Entity (Hapus Sync Fields)
- [ ] Phase 4: Database Migration
- [ ] Phase 5: Update DataRepository
- [ ] Phase 6: Update LoginActivity
- [ ] Phase 7: Update Semua Activities
- [ ] Phase 8: Hapus Supabase Files
- [ ] Phase 9: Hapus Reference ke Role 'staff'
- [ ] Phase 10: Testing
  - [ ] Register owner
  - [ ] Register employee
  - [ ] Login/logout
  - [ ] Switch user
  - [ ] Role-based permissions
  - [ ] Guest mode
  - [ ] Manage employees
  - [ ] CRUD operations

---

## 🎯 Hasil Akhir

Setelah implementasi:
- ✅ Authentication 100% SQLite (tidak ada Supabase)
- ✅ 1 device = 1 warung (semua user melihat data yang sama)
- ✅ Isolasi per ROLE (setiap role hanya bisa action sesuai permission)
- ✅ Multi user/role tetap berfungsi
- ✅ Guest mode tetap berfungsi
- ✅ Tidak ada sinkronisasi ke cloud
- ✅ Semua data tersimpan lokal di SQLite

