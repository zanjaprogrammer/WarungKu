# Panduan Testing Offline Mode

## Cara Mengaktifkan Airplane Mode di Emulator

### Metode 1: Melalui Settings Emulator
1. Buka aplikasi **Settings** di emulator
2. Scroll ke bawah dan cari **Network & internet** atau **Connections**
3. Aktifkan toggle **Airplane mode** (Mode Pesawat)
4. Atau gunakan quick settings dengan swipe down dari atas layar

### Metode 2: Melalui ADB Command
```bash
# Aktifkan Airplane Mode
adb shell settings put global airplane_mode_on 1
adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true

# Nonaktifkan Airplane Mode
adb shell settings put global airplane_mode_on 0
adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false
```

### Metode 3: Melalui Extended Controls Emulator
1. Klik tombol **...** (tiga titik) di sidebar emulator
2. Pilih **Settings** → **Cellular** atau **Wi-Fi**
3. Matikan **Data connection** atau **Wi-Fi**

## Fitur Offline Mode di WarungKu

### Fitur yang Bekerja Offline:
1. ✅ **Menambah Produk** - Bisa menambah produk baru tanpa internet
2. ✅ **Mengelola Stok** - Restock, adjust, delete produk
3. ✅ **Menjual Produk** - Bisa melakukan transaksi penjualan
4. ✅ **Melihat History** - Semua data lokal tetap bisa diakses
5. ✅ **Ringkasan** - Laporan dan ringkasan tetap berfungsi
6. ✅ **Export/Import Produk** - Bisa export/import Excel tanpa internet

### Fitur yang Membutuhkan Internet:
1. ❌ **Barcode Scanner dengan Auto-fill** - Saat scan barcode, jika produk tidak ada di database lokal, aplikasi akan mencoba mencari di database eksternal (Open Food Facts API)
   - **Behavior Offline**: Jika tidak ada internet, aplikasi akan langsung menampilkan dialog "Produk Tidak Ditemukan" tanpa mencoba API
   - **Behavior Online**: Aplikasi akan mencoba mencari di database eksternal terlebih dahulu

## Skenario Testing

### Test 1: Menambah Produk dengan Barcode Scanner (Offline)
1. Aktifkan Airplane Mode
2. Buka halaman **Stok** → **Tambah Produk**
3. Klik tombol scan barcode
4. Scan barcode produk
5. **Expected**: Dialog "Produk Tidak Ditemukan" muncul langsung (tanpa loading API)
6. Bisa langsung tambah produk manual

### Test 2: Menambah Produk dengan Barcode Scanner (Online)
1. Nonaktifkan Airplane Mode (pastikan ada internet)
2. Buka halaman **Stok** → **Tambah Produk**
3. Klik tombol scan barcode
4. Scan barcode produk populer (misalnya: Teh Botol, Indomie, dll)
5. **Expected**: 
   - Jika produk ada di database lokal → langsung muncul
   - Jika tidak ada di lokal → aplikasi akan mencoba API
   - Jika ditemukan di API → form otomatis terisi
   - Jika tidak ditemukan → dialog "Produk Tidak Ditemukan"

### Test 3: Menjual Produk (Offline)
1. Aktifkan Airplane Mode
2. Buka halaman **Jual**
3. Klik produk untuk menambah ke keranjang
4. Klik **JUAL** di cart summary
5. Input pembayaran dan konfirmasi
6. **Expected**: Transaksi berhasil, data tersimpan lokal

### Test 4: Scan Barcode di Halaman Jual (Offline)
1. Aktifkan Airplane Mode
2. Buka halaman **Jual**
3. Klik tombol barcode scanner di toolbar
4. Scan barcode produk yang sudah ada di database lokal
5. **Expected**: Produk langsung ditambahkan ke keranjang (quantity 1)

### Test 5: Scan Barcode di Halaman Jual (Online - Produk Baru)
1. Nonaktifkan Airplane Mode
2. Buka halaman **Jual**
3. Klik tombol barcode scanner
4. Scan barcode produk yang belum ada di database lokal
5. **Expected**: Dialog muncul menawarkan untuk cek database eksternal atau tambah produk baru

## Verifikasi Offline Mode

### Checklist:
- [ ] Aplikasi tidak crash saat offline
- [ ] Fitur lokal tetap berfungsi (tambah produk, jual, dll)
- [ ] Tidak ada error toast yang muncul saat offline
- [ ] Barcode scanner tidak mencoba API saat offline
- [ ] Data tetap tersimpan dengan benar saat offline
- [ ] Setelah online kembali, aplikasi bisa menggunakan API lagi

## Troubleshooting

### Jika Airplane Mode tidak berfungsi:
1. Restart emulator
2. Gunakan ADB command sebagai alternatif
3. Matikan WiFi/Data secara manual di Settings

### Jika aplikasi masih mencoba API saat offline:
- Pastikan `NetworkUtils.isNetworkAvailable()` bekerja dengan benar
- Check logcat untuk melihat apakah ada error network

