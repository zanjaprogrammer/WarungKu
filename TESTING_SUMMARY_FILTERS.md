# Panduan Testing Filter Waktu di Halaman Ringkasan

## 🎯 Tujuan
Testing filter waktu (Hari Ini, Minggu Ini, Bulan Ini, Tahun Ini) di halaman Ringkasan.

---

## 📅 Cara Testing Manual

### Metode 1: Menggunakan Data Real dengan Waktu Berbeda

#### Step 1: Buat Transaksi untuk Hari Ini
1. Buka aplikasi
2. Catat beberapa transaksi (jual barang atau catat pengeluaran)
3. Semua transaksi ini akan masuk ke filter "Hari Ini"

#### Step 2: Buat Transaksi untuk Minggu Lalu
**Cara 1: Mengubah Waktu Sistem (Tidak Disarankan)**
- Mengubah waktu sistem emulator bisa merusak data
- Tidak disarankan untuk testing

**Cara 2: Menggunakan Helper Function (Disarankan)**
- Gunakan fitur "Generate Test Data" yang akan dibuat
- Atau gunakan adb untuk insert data dengan timestamp manual

#### Step 3: Buat Transaksi untuk Bulan Lalu
- Gunakan helper function atau insert manual

#### Step 4: Buat Transaksi untuk Tahun Lalu
- Gunakan helper function atau insert manual

---

## 🛠️ Cara Testing dengan ADB (Advanced)

### Insert Data dengan Timestamp Manual

```bash
# Insert transaksi untuk hari ini
adb shell "sqlite3 /data/data/com.alkahfprogrammer.warungku/databases/warungku_db.db \"INSERT INTO cash_flow (type, amount, description, timestamp, product_id, profit) VALUES ('IN', 50000, 'Test Hari Ini', $(date +%s)000, NULL, 0);\""

# Insert transaksi untuk 3 hari lalu
adb shell "sqlite3 /data/data/com.alkahfprogrammer.warungku/databases/warungku_db.db \"INSERT INTO cash_flow (type, amount, description, timestamp, product_id, profit) VALUES ('IN', 30000, 'Test 3 Hari Lalu', $(($(date +%s) - 259200))000, NULL, 0);\""

# Insert transaksi untuk 2 minggu lalu
adb shell "sqlite3 /data/data/com.alkahfprogrammer.warungku/databases/warungku_db.db \"INSERT INTO cash_flow (type, amount, description, timestamp, product_id, profit) VALUES ('IN', 40000, 'Test 2 Minggu Lalu', $(($(date +%s) - 1209600))000, NULL, 0);\""

# Insert transaksi untuk 2 bulan lalu
adb shell "sqlite3 /data/data/com.alkahfprogrammer.warungku/databases/warungku_db.db \"INSERT INTO cash_flow (type, amount, description, timestamp, product_id, profit) VALUES ('IN', 60000, 'Test 2 Bulan Lalu', $(($(date +%s) - 5184000))000, NULL, 0);\""

# Insert transaksi untuk tahun lalu
adb shell "sqlite3 /data/data/com.alkahfprogrammer.warungku/databases/warungku_db.db \"INSERT INTO cash_flow (type, amount, description, timestamp, product_id, profit) VALUES ('IN', 70000, 'Test Tahun Lalu', $(($(date +%s) - 31536000))000, NULL, 0);\""
```

**Catatan:** 
- Perlu root access atau aplikasi harus dalam mode debug
- Timestamp dalam milliseconds (Java format)

---

## 🎮 Cara Testing Paling Mudah

### Menggunakan Fitur "Generate Test Data" ✅ SUDAH DIBUAT!

1. Buka aplikasi
2. Pergi ke halaman **Ringkasan**
3. Klik **ikon menu** (3 titik) di toolbar kanan atas
4. Pilih **"Generate Test Data"**
5. Data test akan dibuat dengan timestamp:
   - **Hari ini**: Rp 50,000 (Jual) + Rp 20,000 (Pengeluaran)
   - **2 hari lalu**: Rp 30,000 (Jual)
   - **1 minggu lalu**: Rp 40,000 (Jual) + Rp 15,000 (Pengeluaran)
   - **2 minggu lalu**: Rp 35,000 (Jual)
   - **1 bulan lalu**: Rp 60,000 (Jual)
   - **2 bulan lalu**: Rp 45,000 (Jual)
   - **1 tahun lalu**: Rp 70,000 (Jual)
6. Test filter dengan mengklik chip:
   - **Hari Ini** → hanya data hari ini (Rp 50,000 - Rp 20,000 = Rp 30,000)
   - **Minggu Ini** → data 7 hari terakhir (Hari ini + 2 hari lalu = Rp 80,000 - Rp 20,000 = Rp 60,000)
   - **Bulan Ini** → data 30 hari terakhir (Hari ini + 2 hari + 1 minggu + 2 minggu = Rp 160,000 - Rp 20,000 = Rp 140,000)
   - **Tahun Ini** → data 365 hari terakhir (Semua data = Rp 330,000 - Rp 35,000 = Rp 295,000)

---

## ✅ Checklist Testing

### Filter "Hari Ini"
- [ ] Buat transaksi hari ini
- [ ] Buka halaman Ringkasan
- [ ] Klik chip "Hari Ini"
- [ ] Verifikasi hanya menampilkan transaksi hari ini
- [ ] Verifikasi total income/expense sesuai

### Filter "Minggu Ini"
- [ ] Buat transaksi beberapa hari dalam minggu ini
- [ ] Klik chip "Minggu Ini"
- [ ] Verifikasi menampilkan semua transaksi dalam 7 hari terakhir
- [ ] Verifikasi total income/expense sesuai

### Filter "Bulan Ini"
- [ ] Buat transaksi beberapa hari dalam bulan ini
- [ ] Klik chip "Bulan Ini"
- [ ] Verifikasi menampilkan semua transaksi dalam 30 hari terakhir
- [ ] Verifikasi total income/expense sesuai

### Filter "Tahun Ini"
- [ ] Buat transaksi beberapa bulan dalam tahun ini
- [ ] Klik chip "Tahun Ini"
- [ ] Verifikasi menampilkan semua transaksi dalam 365 hari terakhir
- [ ] Verifikasi total income/expense sesuai

---

## 🔍 Verifikasi Hasil

Setelah memilih filter, pastikan:
1. **Total Income** sesuai dengan transaksi "IN" dalam periode tersebut
2. **Total Expense** sesuai dengan transaksi "OUT" dalam periode tersebut
3. **Net Profit** = Income - Expense
4. **Total Stock Purchase** sesuai dengan transaksi "OUT" untuk stok
5. **Progress Bar Capital Return** update sesuai periode

---

## 💡 Tips

1. **Buat banyak transaksi** dengan jumlah berbeda untuk mudah diidentifikasi
2. **Gunakan deskripsi unik** untuk setiap transaksi (misal: "Test Hari 1", "Test Minggu 1")
3. **Catat jumlah yang diinput** untuk memverifikasi total
4. **Test edge cases**: 
   - Transaksi tepat di tengah malam (00:00)
   - Transaksi di awal bulan/minggu/tahun
   - Tidak ada transaksi dalam periode tertentu

---

**Dibuat oleh:** AI Assistant  
**Tanggal:** 27 Desember 2024

