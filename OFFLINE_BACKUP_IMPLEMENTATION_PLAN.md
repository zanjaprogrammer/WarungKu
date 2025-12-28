# Offline Mode & Backup & Restore - Implementation Plan

## 🎯 Tujuan
Mengimplementasikan Offline Mode dan Backup & Restore Data untuk WarungKu app.

---

## 📋 Analisis Kebutuhan

### Offline Mode
**Status Saat Ini:**
- ✅ Aplikasi sudah menggunakan Room database lokal
- ✅ Semua operasi CRUD sudah offline (tidak perlu internet)
- ⚠️ API calls (Open Food Facts) memerlukan internet
- ⚠️ Tidak ada network detection/indicator

**Yang Perlu Ditambahkan:**
1. Network connectivity detection
2. Graceful handling untuk API calls saat offline
3. User feedback saat offline (opsional - toast/indicator)
4. Skip API lookup jika offline

### Backup & Restore
**Status Saat Ini:**
- ✅ Export/Import produk sudah ada (Excel)
- ❌ Backup database lengkap belum ada
- ❌ Restore database belum ada

**Yang Perlu Ditambahkan:**
1. Export database (SQLite file atau JSON)
2. Import/Restore database dari file
3. UI untuk backup/restore
4. Validasi data saat restore

---

## 🛠️ Implementasi

### Phase 1: Offline Mode

#### 1.1 Network Detection Utility
**File:** `app/src/main/java/com/zanjaprogrammer/warungku/utils/NetworkUtils.java`

**Fitur:**
- Check network connectivity
- Check internet availability (ping test sederhana)
- Static methods untuk digunakan di seluruh app

**Implementasi:**
```java
public class NetworkUtils {
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) 
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
```

#### 1.2 Update API Calls untuk Handle Offline
**Files:**
- `SellActivity.java` - `lookupProductFromApi()`
- `AddProductActivity.java` - `lookupProductByBarcode()`

**Perubahan:**
- Check network sebelum call API
- Skip API call jika offline
- Show toast message yang informatif
- Fallback ke manual input jika offline

#### 1.3 Optional: Network Indicator
- Bisa ditambahkan di toolbar atau status bar
- Low priority untuk sekarang

---

### Phase 2: Backup & Restore

#### 2.1 Backup Database
**File:** `app/src/main/java/com/zanjaprogrammer/warungku/utils/DatabaseBackupUtils.java`

**Fitur:**
- Export database SQLite file
- Export ke JSON (alternatif, lebih readable)
- Save ke Downloads folder atau external storage
- Share via Intent (Google Drive, email, dll)

**Implementasi:**
- Copy SQLite database file dari internal storage
- Atau export ke JSON format (Products, CashFlow)
- Use FileProvider untuk secure sharing
- Intent.ACTION_SEND untuk share

#### 2.2 Restore Database
**File:** `app/src/main/java/com/zanjaprogrammer/warungku/utils/DatabaseRestoreUtils.java`

**Fitur:**
- Import database dari SQLite file
- Import dari JSON file
- Validasi data sebelum restore
- Backup otomatis sebelum restore (safety)
- Confirmation dialog

**Implementasi:**
- Read SQLite file atau JSON
- Validate data structure
- Create backup otomatis sebelum restore
- Replace database dengan data baru
- Show success/error message

#### 2.3 UI untuk Backup & Restore
**Location:** SummaryActivity atau Settings Activity baru

**Options:**
1. **Menu di SummaryActivity toolbar** (simple)
2. **Card di SummaryActivity** (lebih visible)
3. **Settings Activity baru** (lebih organized)

**UI Elements:**
- Button "Backup Data" - export database
- Button "Restore Data" - import database
- Info text tentang backup/restore
- Last backup timestamp (opsional)

---

## 📁 File Structure

```
app/src/main/java/com/zanjaprogrammer/warungku/
├── utils/
│   ├── NetworkUtils.java (NEW)
│   ├── DatabaseBackupUtils.java (NEW)
│   └── DatabaseRestoreUtils.java (NEW)
├── SummaryActivity.java (UPDATE - add backup/restore UI)
└── AddProductActivity.java (UPDATE - handle offline)
└── SellActivity.java (UPDATE - handle offline)
```

---

## 🔒 Security & Safety

### Backup Safety:
- ✅ FileProvider untuk secure file access
- ✅ Validate file sebelum restore
- ✅ Auto backup sebelum restore

### Restore Safety:
- ✅ Confirmation dialog dengan warning
- ✅ Auto backup sebelum restore
- ✅ Validate data structure
- ✅ Rollback jika error

---

## 📱 User Experience

### Backup Flow:
1. User klik "Backup Data"
2. System export database
3. File saved ke Downloads
4. Option untuk share (Google Drive, email, dll)
5. Toast: "Backup berhasil disimpan di Downloads"

### Restore Flow:
1. User klik "Restore Data"
2. File picker muncul
3. User pilih backup file
4. Confirmation dialog: "Ini akan mengganti semua data. Lanjutkan?"
5. Auto backup dibuat
6. Restore dilakukan
7. Toast: "Restore berhasil" atau "Error: ..."

---

## ✅ Checklist Implementasi

### Offline Mode:
- [ ] Create NetworkUtils.java
- [ ] Update SellActivity - handle offline API calls
- [ ] Update AddProductActivity - handle offline API calls
- [ ] Test offline mode (airplane mode)
- [ ] Test online mode (normal)

### Backup & Restore:
- [ ] Create DatabaseBackupUtils.java
- [ ] Create DatabaseRestoreUtils.java
- [ ] Add UI untuk backup/restore (SummaryActivity)
- [ ] Test backup (export file)
- [ ] Test restore (import file)
- [ ] Test error handling
- [ ] Test validation

---

## 🚀 Implementation Order

1. **NetworkUtils** - Utility untuk network detection
2. **Update API calls** - Handle offline gracefully
3. **DatabaseBackupUtils** - Export database
4. **DatabaseRestoreUtils** - Import database
5. **UI di SummaryActivity** - Buttons untuk backup/restore
6. **Testing** - Comprehensive testing

---

## 📝 Notes

- **Offline Mode:** Sebagian besar sudah berfungsi karena Room database lokal. Yang perlu ditambahkan adalah handling untuk API calls.
- **Backup Format:** SQLite file lebih simple dan langsung, tapi JSON lebih readable. Kita bisa support keduanya atau mulai dengan SQLite.
- **Google Drive:** Bisa menggunakan Storage Access Framework atau Intent.ACTION_SEND untuk share ke Google Drive (user pilih sendiri).
- **Auto Backup:** Bisa ditambahkan nanti (scheduled backup), untuk sekarang manual backup cukup.

---

**Dibuat:** 29 Desember 2024  
**Versi:** 1.0

