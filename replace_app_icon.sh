#!/bin/bash

# Script untuk mengganti icon aplikasi WarungKu
# Pastikan Anda sudah menyimpan gambar icon sebagai 'warungku_icon.png' di direktori ini

echo "🎨 Mengganti Icon Aplikasi WarungKu..."
echo "======================================="

# Cek apakah file icon ada
if [ ! -f "warungku_icon.png" ]; then
    echo "❌ File 'warungku_icon.png' tidak ditemukan!"
    echo "💡 Silakan simpan gambar icon Anda dengan nama 'warungku_icon.png' di direktori ini"
    exit 1
fi

# Buat direktori mipmap jika belum ada
echo "📁 Membuat direktori mipmap..."
mkdir -p app/src/main/res/mipmap-mdpi
mkdir -p app/src/main/res/mipmap-hdpi  
mkdir -p app/src/main/res/mipmap-xhdpi
mkdir -p app/src/main/res/mipmap-xxhdpi
mkdir -p app/src/main/res/mipmap-xxxhdpi

# Cek apakah ImageMagick tersedia untuk resize
if command -v convert &> /dev/null; then
    echo "🔧 Menggunakan ImageMagick untuk resize..."
    
    # Resize ke berbagai ukuran
    convert warungku_icon.png -resize 48x48 app/src/main/res/mipmap-mdpi/ic_launcher.png
    convert warungku_icon.png -resize 72x72 app/src/main/res/mipmap-hdpi/ic_launcher.png
    convert warungku_icon.png -resize 96x96 app/src/main/res/mipmap-xhdpi/ic_launcher.png
    convert warungku_icon.png -resize 144x144 app/src/main/res/mipmap-xxhdpi/ic_launcher.png
    convert warungku_icon.png -resize 192x192 app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
    
    echo "✅ Icon berhasil diresize ke semua ukuran!"
    
elif command -v sips &> /dev/null; then
    echo "🔧 Menggunakan sips (macOS) untuk resize..."
    
    # Resize menggunakan sips (macOS)
    sips -z 48 48 warungku_icon.png --out app/src/main/res/mipmap-mdpi/ic_launcher.png
    sips -z 72 72 warungku_icon.png --out app/src/main/res/mipmap-hdpi/ic_launcher.png
    sips -z 96 96 warungku_icon.png --out app/src/main/res/mipmap-xhdpi/ic_launcher.png
    sips -z 144 144 warungku_icon.png --out app/src/main/res/mipmap-xxhdpi/ic_launcher.png
    sips -z 192 192 warungku_icon.png --out app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
    
    echo "✅ Icon berhasil diresize ke semua ukuran!"
    
else
    echo "⚠️  ImageMagick atau sips tidak tersedia"
    echo "📋 Silakan resize manual ke ukuran berikut:"
    echo "   - mipmap-mdpi: 48x48 px"
    echo "   - mipmap-hdpi: 72x72 px"
    echo "   - mipmap-xhdpi: 96x96 px"
    echo "   - mipmap-xxhdpi: 144x144 px"
    echo "   - mipmap-xxxhdpi: 192x192 px"
    
    # Copy file asli ke semua folder (user perlu resize manual)
    cp warungku_icon.png app/src/main/res/mipmap-mdpi/ic_launcher.png
    cp warungku_icon.png app/src/main/res/mipmap-hdpi/ic_launcher.png
    cp warungku_icon.png app/src/main/res/mipmap-xhdpi/ic_launcher.png
    cp warungku_icon.png app/src/main/res/mipmap-xxhdpi/ic_launcher.png
    cp warungku_icon.png app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
    
    echo "📁 File icon telah dicopy ke semua folder mipmap"
    echo "⚠️  Anda perlu resize manual sesuai ukuran di atas"
fi

# Verifikasi hasil
echo ""
echo "📊 Verifikasi hasil:"
echo "==================="
for dir in app/src/main/res/mipmap-*/; do
    if [ -f "${dir}ic_launcher.png" ]; then
        echo "✅ ${dir}ic_launcher.png"
    else
        echo "❌ ${dir}ic_launcher.png"
    fi
done

echo ""
echo "🚀 Langkah selanjutnya:"
echo "======================"
echo "1. Clean & rebuild aplikasi:"
echo "   ./gradlew clean"
echo "   ./gradlew assembleDebug"
echo ""
echo "2. Install aplikasi untuk melihat icon baru:"
echo "   ./quick_build_install.sh"
echo ""
echo "✨ Icon aplikasi WarungKu siap digunakan!"