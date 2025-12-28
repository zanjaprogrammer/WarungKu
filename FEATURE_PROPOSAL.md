# Proposal Fitur WarungKu - Roadmap Pengembangan
## Fokus: Warung/UMKM Kecil

## 🎯 Tujuan
Menambahkan fitur-fitur **sederhana dan praktis** yang benar-benar dibutuhkan oleh pemilik warung/toko kecil untuk mengelola bisnis mereka sehari-hari.

---

## 📊 Fitur yang Sudah Ada
- ✅ Manajemen Stok Barang
- ✅ Sistem Penjualan (POS)
- ✅ Tracking Cash Flow (Pemasukan & Pengeluaran)
- ✅ Ringkasan Keuangan (Laba, ROI, Progress Modal)
- ✅ Favorit & Sorting Produk
- ✅ Search Barang
- ✅ History Transaksi
- ✅ Barcode Scanner (Scan untuk input produk & jual barang)
- ✅ Filter Waktu di Ringkasan (Hari/Minggu/Bulan/Tahun)
- ✅ Total Belanja Stok Tracking
- ✅ Progress Bar Pengembalian Modal
- ✅ Shopping Cart Persisten (terlihat di semua halaman)
- ✅ Export/Import Produk (Excel)
- ✅ Kalkulator Kembalian (Payment Calculator)
- ✅ Laporan Sederhana (Penjualan, Produk Terlaris, Grafik Trend)
- ✅ UI Improvements (Shadows, Gradients, Modern Design)

---

## 🚀 Fitur Baru yang Diusulkan (Fokus Warung Kecil)

### 1. **Barcode Scanner** 📷 ✅ **SUDAH DIIMPLEMENTASI**
**Prioritas: TINGGI** | **Effort: SEDANG** | **Status: SELESAI**

#### Fitur:
- ✅ Scan barcode untuk input produk baru (tidak perlu ketik manual)
- ✅ Scan barcode saat jual (langsung masuk ke keranjang dengan qty 1)
- ✅ Integrasi dengan Open Food Facts API (auto-fill data produk)
- ⏳ Generate barcode untuk produk sendiri (opsional - belum)

#### Kenapa Penting:
- Mempercepat proses input produk
- Mempercepat proses jual (tidak perlu cari manual)
- Standar di aplikasi kasir modern

#### Implementasi:
- ✅ Library ZXing (com.journeyapps:zxing-android-embedded)
- ✅ Camera permission dengan Activity Result API
- ✅ Toolbar menu item untuk scan di halaman jual (pojok kanan atas)
- ✅ Auto-fill form dari API response
- ✅ Hybrid approach: cek lokal dulu, lalu API
- ✅ Auto-add ke cart dengan qty 1 saat scan di halaman jual (tanpa konfirmasi)

---

### 2. **Multi-User & Cloud Sync** 👥☁️
**Prioritas: TINGGI** | **Effort: TINGGI** | **Status: PLANNING**

#### Fitur:
- Login/Register untuk owner dan karyawan
- Role-based access control (Owner, Manager, Cashier, Staff)
- Invite karyawan via email/phone
- Real-time data sync antar device
- Activity tracking (siapa melakukan apa)
- Offline-first dengan auto-sync saat online

#### Kenapa Penting:
- Pemilik warung sering punya beberapa karyawan
- Perlu kontrol akses (karyawan tidak bisa edit harga/hapus data)
- Data perlu tersinkronisasi antar device
- Audit trail untuk transaksi penting

#### Implementasi:
- Firebase Authentication untuk login
- Firebase Firestore untuk cloud database
- Role-based permission checks
- Sync service untuk local ↔ cloud
- See detailed plan: `MULTI_USER_IMPLEMENTATION_PLAN.md`

---

### 3. **Offline Mode** 📴 ✅ **PARTIALLY IMPLEMENTED**
**Prioritas: TINGGI** | **Effort: TINGGI** | **Status: IN PROGRESS**

#### Fitur:
- ✅ Aplikasi tetap bisa dipakai tanpa internet
- ✅ Semua fitur utama tetap berfungsi offline
- ✅ Offline indicator card
- ⏳ Auto sync data saat online kembali (akan diimplementasikan dengan Multi-User)

#### Kenapa Penting:
- Banyak warung di area dengan sinyal internet tidak stabil
- Tidak bisa kehilangan transaksi karena internet mati
- Reliability adalah kunci untuk aplikasi bisnis

#### Implementasi:
- ✅ Local database sudah ada (Room)
- ✅ Offline detection dengan NetworkUtils
- ✅ Offline indicator UI
- ⏳ Sync mechanism akan diimplementasikan dengan Multi-User feature

---

### 4. **Backup & Restore Data** ☁️
**Prioritas: TINGGI** | **Effort: SEDANG**

#### Fitur:
- Backup data ke Google Drive (gratis)
- Restore data dari backup
- Backup otomatis harian (opsional)

#### Kenapa Penting:
- Keamanan data (jika HP hilang/rusak)
- Bisa pindah ke HP baru dengan mudah
- Peace of mind untuk pemilik warung

#### Implementasi:
- Google Drive API
- Export/import database file
- Simple UI untuk backup/restore

---

### 5. **Laporan Sederhana** 📊 ✅ **SUDAH DIIMPLEMENTASI**
**Prioritas: SEDANG** | **Effort: SEDANG** | **Status: SELESAI**

#### Fitur:
- ✅ Laporan penjualan harian/mingguan/bulanan dengan filter waktu
- ✅ Produk terlaris (top 10) berdasarkan jumlah penjualan
- ✅ Produk yang tidak laku (belum pernah dijual) untuk evaluasi
- ✅ Grafik sederhana (line chart untuk trend penjualan menggunakan MPAndroidChart)
- ✅ Ringkasan keuangan (total pemasukan, pengeluaran, laba)
- ✅ Card besar di halaman ringkasan untuk akses cepat ke laporan

#### Kenapa Penting:
- Pemilik warung perlu tahu produk mana yang laku
- Membantu keputusan restock
- Tidak perlu analytics kompleks, cukup yang praktis

#### Implementasi:
- ✅ Query dari database yang sudah ada (DataRepository)
- ✅ Library chart sederhana (MPAndroidChart)
- ✅ ReportActivity dengan filter waktu (Hari/Minggu/Bulan)
- ✅ ProductReportAdapter untuk menampilkan produk terlaris dan tidak laku
- ✅ Real-time data update

---

### 5. **Struk Digital & Print** 🧾
**Prioritas: SEDANG** | **Effort: SEDANG**

#### Fitur:
- Generate struk setelah transaksi
- Print struk via Bluetooth printer (untuk printer murah)
- Simpan struk di history (bisa lihat ulang)
- Template struk sederhana

#### Kenapa Penting:
- Profesionalitas
- Bukti transaksi untuk pelanggan
- Banyak warung sudah pakai printer Bluetooth

#### Implementasi:
- Library untuk Bluetooth printing
- Template struk sederhana
- Save struk sebagai image/PDF

---

### 7. **Notifikasi Stok** 🔔
**Prioritas: SEDANG** | **Effort: RENDAH**

#### Fitur:
- Notifikasi saat stok hampir habis (berdasarkan minStock)
- Notifikasi saat stok habis
- Reminder untuk restock (opsional)

#### Kenapa Penting:
- Tidak melewatkan stok yang harus di-restock
- Mencegah kehilangan penjualan karena stok habis
- Simple tapi sangat membantu

#### Implementasi:
- Android Notification API
- Check saat buka app atau saat stok berubah
- Simple notification dengan action

---

### 8. **Kategori Produk** 📦
**Prioritas: RENDAH** | **Effort: RENDAH**

#### Fitur:
- Tambah kategori untuk produk (Makanan, Minuman, Snack, dll)
- Filter produk per kategori
- Group produk di halaman jual berdasarkan kategori

#### Kenapa Penting:
- Organisasi produk lebih baik
- Lebih mudah cari produk saat jual
- Cocok untuk warung dengan banyak produk

#### Implementasi:
- Tambah field `category` di Product entity
- Filter di adapter
- UI sederhana untuk pilih kategori

---

### 9. **Export/Import Produk** 📤📥 ✅ **SUDAH DIIMPLEMENTASI**
**Prioritas: RENDAH** | **Effort: SEDANG** | **Status: SELESAI**

#### Fitur:
- ✅ Export daftar produk ke Excel (.xlsx)
- ✅ Import produk dari Excel (.xlsx) dengan update produk existing
- ✅ Template Excel untuk import
- ✅ Validasi data saat import (name & sell price required)
- ✅ Graceful handling untuk kolom opsional yang kosong

#### Kenapa Penting:
- Memudahkan input banyak produk sekaligus
- Backup manual produk
- Cocok untuk warung yang punya banyak produk

#### Implementasi:
- ✅ Apache POI untuk read/write Excel
- ✅ Menu di toolbar StockActivity (overflow menu)
- ✅ FileProvider untuk secure file sharing
- ✅ Intent.ACTION_SEND untuk share file

---

### 10. **Pelanggan Sederhana** 👥
**Prioritas: RENDAH** | **Effort: SEDANG**

#### Fitur:
- Simpan data pelanggan (nama, nomor HP)
- History pembelian per pelanggan
- Total belanja per pelanggan
- Opsional: Kirim struk via WhatsApp

#### Kenapa Penting:
- Membangun hubungan dengan pelanggan
- Bisa follow up pelanggan
- Simple CRM untuk warung kecil

#### Implementasi:
- Entity Customer baru
- Relasi dengan transaksi
- Simple UI untuk manage pelanggan

---

### 11. **Kalkulator Kembalian** 💰 ✅ **SUDAH DIIMPLEMENTASI**
**Prioritas: TINGGI** | **Effort: RENDAH** | **Status: SELESAI**

#### Fitur:
- ✅ Bottom sheet pembayaran saat checkout
- ✅ Input uang bayar dengan validasi real-time
- ✅ Kalkulator kembalian otomatis
- ✅ Peringatan jika uang bayar kurang (dengan jumlah kekurangan)
- ✅ Auto-focus dan keyboard otomatis muncul
- ✅ Integrasi di semua halaman (Home, Sell, Stock, History, Summary)

#### Kenapa Penting:
- Mempercepat proses checkout
- Mengurangi kesalahan hitung kembalian
- Standar di aplikasi kasir modern
- User experience yang lebih baik

#### Implementasi:
- ✅ BottomSheetDialog dengan layout khusus
- ✅ TextWatcher untuk real-time calculation
- ✅ Validasi input dan enable/disable tombol konfirmasi
- ✅ Toast notification dengan informasi kembalian

---


## ❌ Fitur yang TIDAK Akan Dibuat (Terlalu Kompleks untuk Warung Kecil)

### Fitur Enterprise (Tidak Perlu):
- ❌ Multi User & Role Management (kebanyakan warung kecil 1-2 orang)
- ❌ Multi Cabang (warung kecil biasanya 1 lokasi)
- ❌ Supplier Management Kompleks (cukup simpan kontak supplier)
- ❌ Purchase Order System (terlalu formal untuk warung kecil)
- ❌ Membership System Kompleks (poin, tier, dll - terlalu rumit)
- ❌ Multi Currency (warung kecil pakai Rupiah saja)

### Fitur Advanced (Tidak Prioritas):
- ❌ AI Prediction (terlalu advanced, belum perlu)
- ❌ Analytics Kompleks (heatmap, prediksi, dll)
- ❌ Social Features (share, review, dll)
- ❌ Gamification (tidak relevan untuk warung kecil)
- ❌ Voice Commands (nice to have, tapi tidak prioritas)
- ❌ Dark Mode (tidak urgent)

---

## 🎯 Prioritas Implementasi (Simplified)

### Phase 1 - Must Have (1-2 bulan)
**Fitur yang benar-benar dibutuhkan warung kecil:**

1. **Barcode Scanner** ⭐⭐⭐
   - Impact: Sangat tinggi
   - Effort: Sedang
   - **Alasan:** Mempercepat proses jual dan input produk

2. **Offline Mode** ⭐⭐⭐
   - Impact: Sangat tinggi
   - Effort: Tinggi
   - **Alasan:** Reliability, banyak area sinyal tidak stabil

3. **Backup & Restore** ⭐⭐⭐
   - Impact: Tinggi
   - Effort: Sedang
   - **Alasan:** Keamanan data, peace of mind

### Phase 2 - Should Have (2-3 bulan)
**Fitur yang sangat membantu:**

4. **Laporan Sederhana** ⭐⭐
   - Impact: Tinggi
   - Effort: Sedang
   - **Alasan:** Membantu keputusan bisnis

5. **Struk Digital & Print** ⭐⭐
   - Impact: Sedang-Tinggi
   - Effort: Sedang
   - **Alasan:** Profesionalitas, banyak yang sudah pakai printer

6. **Notifikasi Stok** ⭐⭐
   - Impact: Sedang
   - Effort: Rendah
   - **Alasan:** Simple tapi sangat membantu

### Phase 3 - Nice to Have (3+ bulan)
**Fitur tambahan yang bisa ditambah:**

7. **Kategori Produk** ⭐
   - Impact: Sedang
   - Effort: Rendah
   - **Alasan:** Organisasi produk lebih baik

8. **Export/Import Produk** ⭐
   - Impact: Sedang
   - Effort: Sedang
   - **Alasan:** Memudahkan bulk input

9. **Pelanggan Sederhana** ⭐
   - Impact: Sedang
   - Effort: Sedang
   - **Alasan:** CRM sederhana untuk warung kecil

---

## 💡 Fitur Unik Sederhana (Differentiator)

### 1. **WhatsApp Integration Sederhana** 💬
- Kirim struk otomatis via WhatsApp ke pelanggan
- Notifikasi stok via WhatsApp (opsional)
- **Simple tapi powerful** untuk warung kecil

### 2. **Target Penjualan Sederhana** 🎯
- Set target penjualan harian
- Progress bar sederhana
- Notifikasi saat target tercapai
- **Motivasi sederhana untuk pemilik warung**

### 3. **Mode Kasir Cepat** ⚡
- Mode khusus untuk jual cepat (minimal UI)
- Fokus pada speed
- **Untuk jam-jam ramai**

---

## 📊 Kompetitor Analysis (Fokus Warung Kecil)

### Aplikasi Serupa:
1. **BukuWarung** - Simple, fokus cash flow
2. **Kasir Pintar** - POS dengan fitur lengkap (tapi kompleks)
3. **Moka POS** - Enterprise level (terlalu mahal untuk warung kecil)

### Positioning WarungKu:
- ✅ **Lebih simple** dari Kasir Pintar
- ✅ **Lebih lengkap** dari BukuWarung
- ✅ **Gratis** (tidak seperti Moka yang berbayar)
- ✅ **Fokus warung kecil** (bukan enterprise)

### Kelebihan WarungKu:
- ✅ UI yang clean dan mudah digunakan
- ✅ Tidak overwhelming dengan fitur
- ✅ Fokus pada kebutuhan warung kecil
- ✅ Offline-first (setelah implementasi)

---

## 🎨 UX Improvements Sederhana

### 1. **Onboarding Sederhana**
- Tutorial singkat pertama kali buka app
- Setup wizard untuk modal awal
- Tips sederhana

### 2. **Quick Actions**
- Swipe untuk aksi cepat di list produk
- Shortcut untuk fitur yang sering dipakai

### 3. **Font Size Adjustment**
- Bisa ubah ukuran font (untuk yang matanya kurang jelas)

---

## 🔒 Security Sederhana

### 1. **Password Protection**
- Password untuk buka app (opsional)
- Biometric (fingerprint/face) untuk unlock cepat

### 2. **Data Encryption**
- Encrypt database lokal
- Secure backup

---

## 📱 Technical Improvements

### 1. **Performance**
- Optimasi untuk list produk besar
- Lazy loading
- Image optimization

### 2. **Stability**
- Better error handling
- Crash reporting
- Auto recovery

---

## 📝 Prinsip Pengembangan

1. **Keep It Simple** - Fitur harus sederhana dan mudah digunakan
2. **Warung Kecil First** - Semua fitur harus relevan untuk warung kecil
3. **No Over-Engineering** - Tidak perlu fitur yang terlalu kompleks
4. **Practical Over Fancy** - Fitur praktis lebih penting dari fitur keren
5. **Gradual Enhancement** - Tambah fitur bertahap, jangan sekaligus

---

## 🤔 Questions untuk Review

1. **Fitur Phase 1** (Barcode, Offline, Backup) - Apakah setuju dengan prioritas ini?
2. **Fitur Phase 2** - Apakah ada yang perlu ditambah/kurang?
3. **Fitur yang dihapus** - Apakah ada yang sebenarnya masih perlu?
4. **Timeline** - Berapa lama untuk Phase 1?
5. **Budget/Resource** - Apakah ada constraint?

---

## ✅ Checklist Review

- [ ] Setuju dengan fitur Phase 1?
- [ ] Setuju dengan fitur Phase 2?
- [ ] Setuju menghapus fitur kompleks?
- [ ] Ada fitur lain yang perlu ditambah?
- [ ] Prioritas sudah sesuai?
- [ ] Siap untuk mulai implementasi?

---

**Dibuat oleh:** AI Assistant  
**Tanggal:** 26 Desember 2024  
**Versi:** 2.3 (Updated Progress - 29 Desember 2024)  
**Target User:** Pemilik Warung/UMKM Kecil

---

## 📈 Progress Update (29 Desember 2024)

### ✅ Fitur yang Baru Selesai:
1. **Barcode Scanner** - Implementasi lengkap dengan:
   - Scan untuk input produk baru
   - Scan untuk jual barang (auto-add ke cart dengan qty 1)
   - Integrasi Open Food Facts API untuk auto-fill data
   - Toolbar menu item di pojok kanan atas halaman jual (dengan tooltip jelas)
   - Camera permission handling

2. **Filter Waktu di Ringkasan** - Implementasi lengkap dengan:
   - Filter Hari Ini, Minggu Ini, Bulan Ini, Tahun Ini
   - Generate Test Data untuk testing
   - Update otomatis semua perhitungan sesuai periode

3. **Total Belanja Stok Tracking** - Implementasi lengkap dengan:
   - Tracking semua pengeluaran untuk beli stok
   - Tampil di halaman ringkasan
   - Membantu analisis profit vs modal

4. **Progress Bar Pengembalian Modal** - Implementasi lengkap dengan:
   - Progress bar visual untuk tracking ROI
   - Announcement card saat mencapai 100% (balik modal)
   - Update otomatis berdasarkan filter waktu
   - Opsi untuk hide progress bar setelah balik modal
   - Menampilkan sisa modal yang dibutuhkan untuk balik modal

5. **Shopping Cart Persisten** - Implementasi lengkap dengan:
   - Cart terlihat di semua halaman (Home, Stock, Sell, History, Summary)
   - Menampilkan total quantity (bukan jumlah tipe produk)
   - Singleton ViewModel untuk persist cart state

6. **Export/Import Produk** - Implementasi lengkap dengan:
   - Export produk ke Excel (.xlsx) dengan semua field
   - Import produk dari Excel dengan update produk existing
   - Generate template Excel untuk import
   - Validasi data (name & sell price required)
   - Graceful handling untuk kolom opsional kosong
   - Menu di toolbar StockActivity (overflow menu)
   - FileProvider untuk secure file sharing

7. **Kalkulator Kembalian** - Implementasi lengkap dengan:
   - Bottom sheet pembayaran saat klik tombol "JUAL"
   - Input uang bayar dengan validasi real-time
   - Kalkulator kembalian otomatis (update saat user mengetik)
   - Peringatan visual jika uang bayar kurang (dengan jumlah kekurangan)
   - Auto-focus dan keyboard otomatis muncul
   - Tombol konfirmasi hanya aktif jika uang cukup
   - Toast notification dengan informasi kembalian setelah transaksi
   - Integrasi di semua halaman yang memiliki cart (Home, Sell, Stock, History, Summary)
   - Metode pembayaran: Tunai dan QRIS
   - Tombol cepat untuk input uang (5rb, 10rb, 100rb)

8. **Laporan Sederhana** - Implementasi lengkap dengan:
   - ReportActivity dengan filter waktu (Hari/Minggu/Bulan)
   - Ringkasan keuangan (total pemasukan, pengeluaran, laba)
   - Grafik trend penjualan (line chart menggunakan MPAndroidChart)
   - Top 10 produk terlaris berdasarkan jumlah penjualan
   - Daftar produk yang tidak laku (belum pernah dijual)
   - Card besar di halaman ringkasan untuk akses cepat
   - Real-time data update

9. **UI Improvements** - Implementasi lengkap dengan:
   - Custom drawable gradients untuk tombol utama (primary buttons)
   - Custom drawable gradients untuk hero cards (Uang Sekarang, Laba Bersih, Laporan)
   - Enhanced shadows dan elevation untuk depth
   - Gradien untuk FAB (FloatingActionButton)
   - Subtle gradients untuk tonal/outlined buttons
   - Progress bar dengan gradien
   - Konsistensi visual di seluruh aplikasi
   - Modern, clean design dengan depth yang lebih baik

### 🔄 Fitur yang Sedang Dikembangkan:
- Tidak ada

### ⏳ Fitur Selanjutnya (Phase 1):
- Offline Mode
- Backup & Restore Data


