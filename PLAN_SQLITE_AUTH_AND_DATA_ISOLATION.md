# Rencana: SQLite Authentication & Data Isolation per Role

## 🎯 Tujuan
- **Hapus Supabase authentication**, ganti dengan **SQLite authentication**
- **Isolasi data per role/user**: setiap role hanya melihat data yang sesuai dengan role mereka
- Fokus ke SQLite (local storage) untuk semua fitur
- Multi user/role tetap berfungsi dengan isolasi data yang benar

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
  - Fields: `id`, `email`, `password` (hashed), `name`, `role`, `warungId`, `createdAt`, `isActive`
  - Table: `local_users`

#### B. **Warung Entity (SQLite)**
- `app/src/main/java/com/zanjaprogrammer/warungku/data/entity/LocalWarung.java`
  - Fields: `id`, `name`, `ownerId`, `createdAt`
  - Table: `local_warungs`

#### C. **User DAO**
- `app/src/main/java/com/zanjaprogrammer/warungku/data/dao/LocalUserDao.java`
  - Methods: `insert`, `update`, `delete`, `getUserByEmail`, `getUserById`, `getAllUsers`, `getUsersByWarungId`

#### D. **Warung DAO**
- `app/src/main/java/com/zanjaprogrammer/warungku/data/dao/LocalWarungDao.java`
  - Methods: `insert`, `update`, `delete`, `getWarungById`, `getWarungByOwnerId`, `getAllWarungs`

#### E. **Local Auth Manager**
- `app/src/main/java/com/zanjaprogrammer/warungku/auth/LocalAuthManager.java`
  - Methods: `login`, `register`, `logout`, `getCurrentUser`, `getCurrentWarung`, `isLoggedIn`, `getCurrentUserRole`
  - Menggunakan SQLite untuk authentication
  - Menggunakan SharedPreferences untuk session

### 3. **File yang Perlu Dimodifikasi**

#### A. **Entity Classes** (Tambahkan user_id/warung_id untuk isolasi)
- `app/src/main/java/com/zanjaprogrammer/warungku/data/entity/Product.java`
  - ❌ Hapus: `synced`, `lastSyncedAt`, `cloudId`
  - ✅ Tambahkan: `warungId` (Integer, nullable) - untuk isolasi data per warung
  - ✅ Tambahkan: `createdBy` (Integer, nullable) - user_id yang membuat produk

- `app/src/main/java/com/zanjaprogrammer/warungku/data/entity/CashFlow.java`
  - ❌ Hapus: `synced`, `lastSyncedAt`, `cloudId`
  - ✅ Tambahkan: `warungId` (Integer, nullable) - untuk isolasi data per warung
  - ✅ Tambahkan: `createdBy` (Integer, nullable) - user_id yang membuat cash flow

#### B. **AppDatabase.java**
- ✅ Tambahkan: `LocalUser` dan `LocalWarung` ke entities
- ✅ Tambahkan: `LocalUserDao` dan `LocalWarungDao` ke abstract methods
- ✅ Update version: dari 4 ke 5
- ✅ Buat migration: `MIGRATION_4_5`
  - Drop sync columns dari `products` dan `cash_flow`
  - Add `warungId` dan `createdBy` ke `products`
  - Add `warungId` dan `createdBy` ke `cash_flow`
  - Create table `local_users`
  - Create table `local_warungs`

#### C. **ProductDao.java**
- ✅ Update semua query untuk filter berdasarkan `warungId` (jika user logged in)
- ✅ Tambahkan method: `getProductsByWarungId(int warungId)`
- ✅ Update: `getAllProducts()` untuk filter berdasarkan current user's warung

#### D. **CashFlowDao.java**
- ✅ Update semua query untuk filter berdasarkan `warungId` (jika user logged in)
- ✅ Tambahkan method: `getCashFlowsByWarungId(int warungId)`
- ✅ Update: `getAllHistory()` untuk filter berdasarkan current user's warung

#### E. **DataRepository.java**
- ❌ Hapus: `import com.zanjaprogrammer.warungku.sync.SyncManager;`
- ❌ Hapus: semua `SyncManager.triggerSync(application);`
- ❌ Hapus: `getUnsyncedProducts()` dan `getUnsyncedCashFlows()`
- ✅ Update: semua insert/update untuk set `warungId` dan `createdBy` dari current user
- ✅ Update: semua query untuk filter berdasarkan current user's warung

#### F. **LoginActivity.java**
- ❌ Hapus: semua import Supabase
- ❌ Hapus: `SupabaseAuthManager`, `SupabasePostgrestApi`
- ✅ Ganti dengan: `LocalAuthManager`
- ✅ Update: `performLogin()` untuk menggunakan SQLite
- ✅ Update: `performRegister()` untuk menggunakan SQLite
- ✅ Update: `registerAsOwner()` untuk create warung di SQLite
- ✅ Update: `registerAsEmployee()` untuk link ke warung yang ada
- ✅ Hapus: `syncLocalDataToSupabase()`

#### G. **MainActivity.java**
- ❌ Hapus: `SyncManager.triggerSync(this);`
- ✅ Update: ganti `SupabaseAuthManager` dengan `LocalAuthManager`
- ✅ Update: semua reference ke auth manager

#### H. **Semua Activities** (SellActivity, StockActivity, dll)
- ✅ Update: ganti `SupabaseAuthManager` dengan `LocalAuthManager`
- ✅ Update: semua reference ke auth manager

#### I. **PermissionManager.java**
- ✅ Tetap sama (tidak perlu perubahan)
- ✅ Menggunakan role dari `LocalAuthManager`

#### J. **ManageEmployeesActivity.java**
- ✅ Update: ganti `SupabaseAuthManager` dengan `LocalAuthManager`
- ✅ Update: query employees dari SQLite (bukan Supabase)
- ✅ Update: create invite di SQLite (atau hapus fitur invite jika tidak diperlukan)

#### K. **AndroidManifest.xml**
- ❌ Hapus: `<service>` untuk `SupabaseSyncService`
- ✅ Pertahankan: semua lainnya

---

## 🔐 Isolasi Data per Role/User

### **Konsep Isolasi:**
1. **Owner**: Melihat semua data warung mereka
2. **Manager**: Melihat semua data warung mereka
3. **Cashier**: Melihat semua data warung mereka (bisa jual)
4. **Staff**: Melihat semua data warung mereka (bisa lihat stok)
5. **Guest**: Melihat data tanpa warung (warungId = null)

### **Implementasi:**
- Setiap `Product` dan `CashFlow` memiliki `warungId`
- Query selalu filter berdasarkan `warungId` dari current user
- Guest mode: `warungId = null` (data shared untuk semua guest)
- Logged in user: `warungId = currentUser.warungId` (hanya melihat data warung mereka)

---

## 🔧 Langkah-Langkah Implementasi

### **Phase 1: Buat Entity & DAO untuk User/Warung**
1. ✅ Buat `LocalUser.java` entity
2. ✅ Buat `LocalWarung.java` entity
3. ✅ Buat `LocalUserDao.java`
4. ✅ Buat `LocalWarungDao.java`
5. ✅ Update `AppDatabase.java` untuk include entities baru

### **Phase 2: Buat LocalAuthManager**
1. ✅ Buat `LocalAuthManager.java`
2. ✅ Implementasi: `login()`, `register()`, `logout()`
3. ✅ Implementasi: `getCurrentUser()`, `getCurrentWarung()`, `isLoggedIn()`
4. ✅ Implementasi: password hashing (BCrypt atau SHA-256)

### **Phase 3: Update Entity untuk Isolasi Data**
1. ✅ Update `Product.java`: hapus sync fields, tambah `warungId` dan `createdBy`
2. ✅ Update `CashFlow.java`: hapus sync fields, tambah `warungId` dan `createdBy`
3. ✅ Update constructor untuk set fields baru

### **Phase 4: Database Migration**
1. ✅ Buat `MIGRATION_4_5` di `AppDatabase.java`
2. ✅ Drop sync columns dari `products` dan `cash_flow`
3. ✅ Add `warungId` dan `createdBy` ke `products` dan `cash_flow`
4. ✅ Create tables `local_users` dan `local_warungs`

### **Phase 5: Update DAO untuk Filter Data**
1. ✅ Update `ProductDao.java`: semua query filter berdasarkan `warungId`
2. ✅ Update `CashFlowDao.java`: semua query filter berdasarkan `warungId`
3. ✅ Tambahkan method untuk query berdasarkan warung

### **Phase 6: Update DataRepository**
1. ✅ Hapus semua sync calls
2. ✅ Update semua insert/update untuk set `warungId` dan `createdBy`
3. ✅ Update semua query untuk filter berdasarkan current user's warung

### **Phase 7: Update LoginActivity**
1. ✅ Ganti `SupabaseAuthManager` dengan `LocalAuthManager`
2. ✅ Update `performLogin()` untuk SQLite
3. ✅ Update `performRegister()` untuk SQLite
4. ✅ Update `registerAsOwner()` untuk create warung di SQLite
5. ✅ Update `registerAsEmployee()` untuk link ke warung

### **Phase 8: Update Semua Activities**
1. ✅ Ganti `SupabaseAuthManager` dengan `LocalAuthManager` di semua activities
2. ✅ Update semua reference ke auth manager

### **Phase 9: Hapus Supabase Files**
1. ❌ Delete `SupabaseAuthManager.java`
2. ❌ Delete `SupabaseSyncService.java`
3. ❌ Delete `SyncManager.java`
4. ❌ Delete `SupabaseClient.java`
5. ❌ Delete semua file di `supabase/api/`
6. ❌ Hapus service dari `AndroidManifest.xml`

### **Phase 10: Testing**
1. ✅ Test register owner (create warung)
2. ✅ Test register employee (link ke warung)
3. ✅ Test login/logout
4. ✅ Test switch user (logout → login user lain)
5. ✅ Test isolasi data: owner hanya melihat data warung mereka
6. ✅ Test isolasi data: employee hanya melihat data warung mereka
7. ✅ Test guest mode (warungId = null)
8. ✅ Test role-based permissions
9. ✅ Test CRUD operations dengan isolasi data

---

## 📊 Struktur Database SQLite

### **Table: local_users**
```sql
CREATE TABLE local_users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,  -- hashed
    name TEXT,
    role TEXT NOT NULL,  -- 'owner', 'manager', 'cashier', 'staff'
    warungId INTEGER,
    createdAt INTEGER NOT NULL,
    isActive INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY(warungId) REFERENCES local_warungs(id)
);
```

### **Table: local_warungs**
```sql
CREATE TABLE local_warungs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    ownerId INTEGER NOT NULL,
    createdAt INTEGER NOT NULL,
    FOREIGN KEY(ownerId) REFERENCES local_users(id)
);
```

### **Table: products** (updated)
```sql
-- Remove: synced, lastSyncedAt, cloudId
-- Add: warungId, createdBy
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
    barcode TEXT,
    warungId INTEGER,  -- NEW: untuk isolasi data
    createdBy INTEGER,  -- NEW: user_id yang membuat
    FOREIGN KEY(warungId) REFERENCES local_warungs(id),
    FOREIGN KEY(createdBy) REFERENCES local_users(id)
);
```

### **Table: cash_flow** (updated)
```sql
-- Remove: synced, lastSyncedAt, cloudId
-- Add: warungId, createdBy
CREATE TABLE cash_flow (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL,  -- 'IN' or 'OUT'
    amount REAL NOT NULL,
    description TEXT,
    timestamp INTEGER NOT NULL,
    productId INTEGER,
    profit REAL,
    warungId INTEGER,  -- NEW: untuk isolasi data
    createdBy INTEGER,  -- NEW: user_id yang membuat
    FOREIGN KEY(warungId) REFERENCES local_warungs(id),
    FOREIGN KEY(createdBy) REFERENCES local_users(id),
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

### **Isolasi Data:**
- ✅ Setiap user hanya melihat data warung mereka (`warungId` match)
- ✅ Guest mode: melihat data dengan `warungId = null` (shared untuk semua guest)
- ✅ Owner/Manager/Cashier/Staff: melihat data dengan `warungId = currentUser.warungId`
- ⚠️ **Tidak ada isolasi per user individual** - semua user dalam warung yang sama melihat data yang sama

### **Multi Warung (Future Enhancement):**
- Jika user ingin support multiple warung, bisa tambahkan `user_warungs` junction table
- Atau allow user untuk switch warung (logout → login dengan warung lain)

### **Guest Mode:**
- Guest tidak punya `warungId`
- Data guest disimpan dengan `warungId = null`
- Semua guest melihat data yang sama (shared)

---

## ✅ Checklist Implementasi

- [ ] Phase 1: Buat Entity & DAO untuk User/Warung
- [ ] Phase 2: Buat LocalAuthManager
- [ ] Phase 3: Update Entity untuk Isolasi Data
- [ ] Phase 4: Database Migration
- [ ] Phase 5: Update DAO untuk Filter Data
- [ ] Phase 6: Update DataRepository
- [ ] Phase 7: Update LoginActivity
- [ ] Phase 8: Update Semua Activities
- [ ] Phase 9: Hapus Supabase Files
- [ ] Phase 10: Testing
  - [ ] Register owner
  - [ ] Register employee
  - [ ] Login/logout
  - [ ] Switch user
  - [ ] Isolasi data per warung
  - [ ] Guest mode
  - [ ] Role-based permissions
  - [ ] CRUD operations

---

## 🎯 Hasil Akhir

Setelah implementasi:
- ✅ Authentication 100% SQLite (tidak ada Supabase)
- ✅ Isolasi data per warung (setiap role hanya melihat data warung mereka)
- ✅ Multi user/role tetap berfungsi
- ✅ Guest mode tetap berfungsi (data shared dengan warungId = null)
- ✅ Tidak ada sinkronisasi ke cloud
- ✅ Semua data tersimpan lokal di SQLite

