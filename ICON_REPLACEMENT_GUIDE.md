# Panduan Mengganti Icon Aplikasi WarungKu

## 📱 Icon Baru
Gambar yang Anda berikan sangat bagus! Desain toko hijau dengan tulisan "WarungKu" dan simbol uang "Rp" sangat cocok untuk aplikasi kasir digital.

## 🚀 Cara Cepat (Otomatis)

### 1. Simpan Gambar Icon
Simpan gambar yang Anda berikan dengan nama `warungku_icon.png` di direktori root project

### 2. Jalankan Script Otomatis
```bash
./replace_app_icon.sh
```

Script ini akan:
- ✅ Membuat semua folder mipmap yang diperlukan
- ✅ Resize gambar ke semua ukuran yang dibutuhkan (jika ImageMagick/sips tersedia)
- ✅ Copy icon ke semua folder
- ✅ Verifikasi hasil

### 3. Build & Test
```bash
./gradlew clean
./gradlew assembleDebug
./quick_build_install.sh
```

## 🔧 Cara Manual (Jika Script Tidak Berfungsi)

### 1. Persiapan Gambar
Simpan gambar yang Anda berikan dengan nama `warungku_icon.png`

### 2. Buat Icon dalam Berbagai Ukuran
Android memerlukan icon dalam berbagai ukuran untuk berbagai density layar:

```
mipmap-mdpi/ic_launcher.png     (48x48 px)
mipmap-hdpi/ic_launcher.png     (72x72 px)  
mipmap-xhdpi/ic_launcher.png    (96x96 px)
mipmap-xxhdpi/ic_launcher.png   (144x144 px)
mipmap-xxxhdpi/ic_launcher.png  (192x192 px)
```

### 3. Cara Mudah Membuat Icon
**Opsi A: Menggunakan Android Studio**
1. Klik kanan pada folder `app/src/main/res`
2. Pilih `New > Image Asset`
3. Pilih `Launcher Icons (Adaptive and Legacy)`
4. Upload gambar WarungKu Anda
5. Android Studio akan otomatis generate semua ukuran

**Opsi B: Online Icon Generator**
1. Buka https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html
2. Upload gambar WarungKu
3. Download hasil generate
4. Extract dan copy ke folder mipmap yang sesuai

**Opsi C: Manual Resize**
Resize gambar ke ukuran-ukuran di atas menggunakan image editor seperti:
- Photoshop
- GIMP (gratis)
- Online resizer

### 4. Struktur Folder yang Sudah Dibuat
```
app/src/main/res/
├── mipmap-mdpi/
├── mipmap-hdpi/
├── mipmap-xhdpi/
├── mipmap-xxhdpi/
├── mipmap-xxxhdpi/
└── mipmap-anydpi-v26/
    └── ic_launcher.xml (adaptive icon)
```

### 5. Copy Icon Files
Copy file `ic_launcher.png` yang sudah diresize ke masing-masing folder:
```
app/src/main/res/mipmap-mdpi/ic_launcher.png     (48x48)
app/src/main/res/mipmap-hdpi/ic_launcher.png     (72x72)
app/src/main/res/mipmap-xhdpi/ic_launcher.png    (96x96)
app/src/main/res/mipmap-xxhdpi/ic_launcher.png   (144x144)
app/src/main/res/mipmap-xxxhdpi/ic_launcher.png  (192x192)
```

## 🎨 Tips untuk Icon yang Bagus

### ✅ Yang Sudah Bagus dari Icon Anda:
- **Warna hijau** - Cocok dengan tema aplikasi (#2E7D32)
- **Simbol toko** - Jelas menggambarkan aplikasi retail
- **Tulisan "WarungKu"** - Branding yang kuat
- **Simbol "Rp"** - Menunjukkan fokus pada transaksi Indonesia
- **Desain clean** - Mudah dikenali di berbagai ukuran

### 💡 Fitur Tambahan yang Sudah Disiapkan:
- **Adaptive Icon** - Support Android 8.0+ dengan background hijau matching
- **Multiple Densities** - Support semua ukuran layar Android
- **Consistent Branding** - Warna background sesuai tema aplikasi

## 🚀 Hasil Akhir
Setelah mengganti icon, aplikasi WarungKu akan memiliki:
- ✅ Icon yang professional dan menarik
- ✅ Branding yang konsisten dengan warna hijau
- ✅ Mudah dikenali di Play Store dan home screen
- ✅ Support adaptive icon untuk Android modern
- ✅ Sesuai dengan identitas aplikasi kasir digital

## 📱 Adaptive Icon (Sudah Dikonfigurasi)
Untuk Android 8.0+, sudah disiapkan:
- ✅ `ic_launcher_background.xml` - Background hijau (#2E7D32)
- ✅ `ic_launcher.xml` - Konfigurasi adaptive icon
- ✅ Support untuk foreground layer

Icon WarungKu yang Anda berikan sudah sangat bagus dan siap digunakan! 🎉

## 🔍 Troubleshooting
Jika icon tidak berubah setelah install:
1. Uninstall aplikasi lama: `adb uninstall com.alkahfprogrammer.warungku`
2. Clean build: `./gradlew clean`
3. Build ulang: `./gradlew assembleDebug`
4. Install fresh: `adb install app/build/outputs/apk/debug/app-debug.apk`