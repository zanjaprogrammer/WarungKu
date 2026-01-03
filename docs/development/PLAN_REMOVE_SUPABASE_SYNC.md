# Rencana: Hapus Fitur Sinkronisasi Supabase

## 🎯 Tujuan
- Menghapus semua fitur sinkronisasi data ke Supabase
- Fokus ke SQLite (local storage) saja
- Memastikan fitur multi user/role tetap berfungsi (1 device, multiple users)

---

## 📋 Analisis Kode yang Terpengaruh

### 1. **File yang Akan Dihapus**
- ✅ `app/src/main/java/com/zanjaprogrammer/warungku/sync/SyncManager.java`
- ✅ `app/src/main/java/com/zanjaprogrammer/warungku/supabase/SupabaseSyncService.java`

### 2. **File yang Perlu Dimodifikasi**

#### A. **Entity Classes** (Hapus sync fields)
- `app/src/main/java/com/zanjaprogrammer/warungku/data/entity/Product.java`
  - ❌ Hapus: `synced`, `lastSyncedAt`, `cloudId`
  - ✅ Pertahankan: semua field lainnya

- `app/src/main/java/com/zanjaprogrammer/warungku/data/entity/CashFlow.java`
  - ❌ Hapus: `synced`, `lastSyncedAt`, `cloudId`
  - ✅ Pertahankan: semua field lainnya

#### B. **DataRepository.java**
- ❌ Hapus: `import com.zanjaprogrammer.warungku.sync.SyncManager;`
- ❌ Hapus: semua `SyncManager.triggerSync(application);` (6 lokasi)
- ❌ Hapus: `getUnsyncedProducts()` dan `getUnsyncedCashFlows()` methods
- ✅ Pertahankan: semua operasi database lainnya

#### C. **MainActivity.java**
- ❌ Hapus: `SyncManager.triggerSync(this);` di `onResume()`
- ✅ Pertahankan: semua fitur lainnya

#### D. **LoginActivity.java**
- ❌ Hapus: `syncLocalDataToSupabase()` method
- ❌ Hapus: panggilan `syncLocalDataToSupabase()` setelah register
- ✅ Pertahankan: login/register functionality
- ✅ Pertahankan: SupabaseAuthManager untuk authentication (tidak untuk sync)

#### E. **AndroidManifest.xml**
- ❌ Hapus: `<service>` declaration untuk `SupabaseSyncService`
- ✅ Pertahankan: semua lainnya

#### F. **DAO Classes** (Opsional - hapus query unsynced jika ada)
- `app/src/main/java/com/zanjaprogrammer/warungku/data/dao/ProductDao.java`
  - ❌ Hapus: query `getUnsyncedProducts()` jika ada
- `app/src/main/java/com/zanjaprogrammer/warungku/data/dao/CashFlowDao.java`
  - ❌ Hapus: query `getUnsyncedCashFlows()` jika ada

#### G. **Database Migration** (Opsional)
- `app/src/main/java/com/zanjaprogrammer/warungku/data/AppDatabase.java`
  - ⚠️ Pertimbangkan migration untuk drop columns `synced`, `lastSyncedAt`, `cloudId`
  - Atau biarkan columns tetap ada (tidak digunakan)

---

## ✅ Yang Akan Dipertahankan (Multi User/Role)

### 1. **Authentication & User Management**
- ✅ `SupabaseAuthManager.java` - untuk login/register/logout
- ✅ `User.java` model - untuk menyimpan user data
- ✅ `Warung.java` model - untuk menyimpan warung data
- ✅ SharedPreferences - untuk menyimpan session (userId, role, warungId, dll)

### 2. **Role-Based Access Control**
- ✅ `PermissionManager.java` - semua permission checks berdasarkan role
- ✅ Role: `owner`, `manager`, `cashier`, `staff`, `null` (guest)
- ✅ Guest mode tetap berfungsi (role == null)

### 3. **Multi User Features**
- ✅ Login/Register/Logout
- ✅ Switch user (logout → login dengan user lain)
- ✅ Manage Employees (hanya owner)
- ✅ Role-based permissions untuk semua fitur

### 4. **Local Data Storage**
- ✅ SQLite (Room Database) untuk products dan cash_flows
- ✅ SharedPreferences untuk user session
- ✅ Semua data tersimpan lokal (tidak sync ke cloud)

---

## 🔧 Langkah-Langkah Implementasi

### **Phase 1: Hapus Sync Service & Manager**
1. ❌ Delete `SyncManager.java`
2. ❌ Delete `SupabaseSyncService.java`
3. ❌ Hapus service declaration di `AndroidManifest.xml`

### **Phase 2: Hapus Sync Calls**
1. ❌ Hapus semua `SyncManager.triggerSync()` di:
   - `DataRepository.java` (6 lokasi)
   - `MainActivity.java` (1 lokasi)
2. ❌ Hapus `syncLocalDataToSupabase()` di `LoginActivity.java`

### **Phase 3: Hapus Sync Fields dari Entity**
1. ❌ Hapus `synced`, `lastSyncedAt`, `cloudId` dari `Product.java`
2. ❌ Hapus `synced`, `lastSyncedAt`, `cloudId` dari `CashFlow.java`
3. ❌ Update constructor untuk tidak set fields tersebut

### **Phase 4: Hapus Unsynced Methods**
1. ❌ Hapus `getUnsyncedProducts()` dari `DataRepository.java`
2. ❌ Hapus `getUnsyncedCashFlows()` dari `DataRepository.java`
3. ❌ Hapus query methods dari DAO jika ada

### **Phase 5: Database Migration (Opsional)**
1. ⚠️ Buat migration untuk drop columns (jika ingin clean database)
2. Atau biarkan columns tetap ada (tidak digunakan, tidak masalah)

### **Phase 6: Testing**
1. ✅ Test login/register/logout
2. ✅ Test switch user (logout → login user lain)
3. ✅ Test role-based permissions
4. ✅ Test semua fitur CRUD (create, read, update, delete)
5. ✅ Test guest mode
6. ✅ Test manage employees (hanya owner)

---

## ⚠️ Catatan Penting

### **Multi User/Role di SQLite (1 Device)**
- ✅ User session disimpan di **SharedPreferences** (bukan SQLite)
- ✅ Setiap user bisa login/logout
- ✅ Role disimpan di SharedPreferences via `SupabaseAuthManager`
- ✅ Permission checks menggunakan `PermissionManager` dengan role dari SharedPreferences
- ✅ Data products dan cash_flows di SQLite **shared** untuk semua user di device yang sama
- ⚠️ **Tidak ada isolasi data per user** - semua user di device yang sama melihat data yang sama

### **Jika Ingin Isolasi Data Per User (Opsional - Future)**
- Tambahkan `userId` atau `warungId` ke `Product` dan `CashFlow` entity
- Filter query berdasarkan `userId` atau `warungId`
- Setiap user hanya melihat data mereka sendiri

### **Supabase Auth vs Sync**
- ✅ **Pertahankan**: SupabaseAuthManager untuk authentication (login/register)
- ❌ **Hapus**: SupabaseSyncService untuk data synchronization
- ✅ **Hasil**: User bisa login/register, tapi data tidak sync ke Supabase

---

## 📊 Dampak Perubahan

### **Yang Berubah**
- ❌ Data tidak lagi sync ke Supabase
- ❌ Tidak ada background sync service
- ❌ Tidak ada sync status tracking
- ❌ Field `synced`, `lastSyncedAt`, `cloudId` tidak digunakan

### **Yang Tetap Sama**
- ✅ Login/Register/Logout tetap berfungsi
- ✅ Multi user/role tetap berfungsi
- ✅ Role-based permissions tetap berfungsi
- ✅ Guest mode tetap berfungsi
- ✅ Semua fitur CRUD tetap berfungsi
- ✅ Data tersimpan di SQLite (local)

---

## ✅ Checklist Implementasi

- [ ] Phase 1: Hapus Sync Service & Manager
- [ ] Phase 2: Hapus Sync Calls
- [ ] Phase 3: Hapus Sync Fields dari Entity
- [ ] Phase 4: Hapus Unsynced Methods
- [ ] Phase 5: Database Migration (opsional)
- [ ] Phase 6: Testing
  - [ ] Login/Register/Logout
  - [ ] Switch user
  - [ ] Role-based permissions
  - [ ] Guest mode
  - [ ] Manage employees
  - [ ] CRUD operations

---

## 🎯 Hasil Akhir

Setelah implementasi:
- ✅ Aplikasi fokus ke SQLite (local storage)
- ✅ Multi user/role tetap berfungsi (1 device)
- ✅ Tidak ada sinkronisasi ke Supabase
- ✅ Authentication tetap menggunakan Supabase (opsional, bisa dihapus juga nanti)
- ✅ Semua fitur tetap berfungsi normal

