# Setup Webcam untuk Android Emulator

## 🎯 Tujuan
Menggunakan webcam laptop (bukan camera dummy) di Android Emulator untuk testing barcode scanner.

## ✅ Permission Sudah Terpasang
- ✅ `CAMERA` permission sudah ada di `AndroidManifest.xml`
- ✅ Aplikasi akan request permission saat pertama kali menggunakan camera

## 🔧 Cara Setup Webcam

### Metode 1: Edit AVD Configuration (Paling Mudah - Recommended)

1. **Buka Android Studio**
2. **Tools > Device Manager** (atau klik icon Device Manager di toolbar)
3. **Klik icon pensil (Edit)** pada AVD yang ingin di-edit
4. **Klik "Show Advanced Settings"** di bagian bawah
5. **Scroll ke bagian "Camera"**
6. **Set Camera Configuration:**
   - **Back Camera:** Pilih `Webcam0` (atau nama webcam laptop Anda)
   - **Front Camera:** Pilih `Webcam0` (atau nama webcam laptop Anda)
7. **Klik "Finish"** untuk menyimpan
8. **Restart emulator** agar perubahan berlaku

### Metode 2: Menggunakan Script

```bash
# Launch emulator dengan webcam
./launch_emulator_with_webcam.sh [AVD_NAME]

# Atau jika AVD_NAME tidak di-specify, akan menggunakan AVD pertama
./launch_emulator_with_webcam.sh
```

### Metode 3: Manual Command Line

```bash
# Cari path emulator (biasanya di Android SDK)
export ANDROID_HOME=$HOME/Library/Android/sdk
$ANDROID_HOME/emulator/emulator -avd <AVD_NAME> \
    -camera-back webcam0 \
    -camera-front webcam0
```

## 🔍 Troubleshooting

### Webcam tidak terdeteksi
- Pastikan webcam laptop tidak digunakan aplikasi lain (Zoom, Teams, dll)
- Tutup aplikasi yang menggunakan webcam
- Restart emulator

### Permission denied
- Pastikan aplikasi sudah request CAMERA permission
- Cek Settings > Apps > WarungKu > Permissions > Camera (harus Allow)

### Camera masih dummy
- Pastikan sudah restart emulator setelah mengubah konfigurasi
- Cek di AVD Manager bahwa camera sudah di-set ke Webcam0
- Coba metode lain (script atau command line)

## 📝 Catatan

- **macOS:** Webcam biasanya terdeteksi sebagai `webcam0`
- **Windows:** Mungkin perlu nama webcam yang spesifik (cek di Device Manager)
- **Linux:** Mungkin perlu install `v4l2loopback` atau menggunakan `v4l2`

## ✅ Verifikasi

Setelah setup:
1. Launch emulator
2. Buka aplikasi WarungKu
3. Buka barcode scanner (di halaman Jual atau Tambah Produk)
4. Webcam laptop seharusnya aktif dan menampilkan preview camera

