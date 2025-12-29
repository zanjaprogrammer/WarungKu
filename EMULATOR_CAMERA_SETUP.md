# Setup Kamera untuk Android Emulator

## Cara Setup Kamera di Android Emulator

### Metode 1: Menggunakan Webcam Laptop (Recommended)

1. **Buka Android Studio**
2. **Buka AVD Manager** (Tools > Device Manager atau ikon di toolbar)
3. **Edit Emulator** yang akan digunakan (klik ikon pensil/edit)
4. **Pilih "Show Advanced Settings"**
5. **Di bagian "Camera"**, pilih:
   - **Front Camera**: `Webcam0` (atau nama webcam laptop Anda)
   - **Back Camera**: `Webcam0` (atau nama webcam laptop Anda)
6. **Klik "Finish"** untuk menyimpan

### Metode 2: Menggunakan Command Line (Alternatif)

Jika emulator sudah berjalan, Anda bisa menggunakan command line:

```bash
# List emulator yang sedang berjalan
adb devices

# Set camera untuk emulator (ganti emulator-5554 dengan ID emulator Anda)
adb -s emulator-5554 emu camera webcam0
```

### Metode 3: Menggunakan Extended Controls

1. **Buka Extended Controls** di emulator (klik ikon "..." di sidebar)
2. **Pilih "Camera"**
3. **Pilih "Webcam0"** untuk Front dan Back Camera
4. **Klik "Done"**

## Verifikasi Setup

Setelah setup, coba buka aplikasi kamera di emulator atau test barcode scanner di aplikasi WarungKu untuk memastikan kamera berfungsi.

## Troubleshooting

### Kamera tidak muncul di dropdown
- Pastikan webcam laptop aktif dan tidak digunakan aplikasi lain
- Restart Android Studio
- Restart emulator

### Kamera tidak berfungsi
- Pastikan permission CAMERA sudah diberikan di aplikasi
- Coba restart emulator
- Coba gunakan metode command line

### Error "Camera not available"
- Pastikan emulator menggunakan system image yang support camera
- Coba gunakan emulator dengan API level yang lebih tinggi (API 28+)

## Catatan

- **Webcam0** biasanya adalah webcam built-in laptop
- Jika ada beberapa webcam, bisa coba **Webcam1**, **Webcam2**, dll
- Untuk testing barcode, pastikan barcode terlihat jelas di depan webcam

