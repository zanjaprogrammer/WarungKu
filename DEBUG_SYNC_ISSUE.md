# Debug Sync Issue
## Document Kosong di Firestore

## 🔍 Current Status

Di Firestore Console, Anda melihat:
- ✅ Collection `products` ada
- ✅ Document `products/{warungId}` ada
- ✅ Subcollection `items` ada
- ❌ Document `products/{warungId}` kosong (tidak ada fields)
- ❌ Subcollection `items` mungkin kosong (tidak ada documents produk)

**Pesan:** "This document does not exist, it will not appear in queries or snapshots"

---

## 🧪 Step 1: Check Apakah Ada Produk di Aplikasi

1. **Buka aplikasi** di emulator
2. **Buka halaman "Stok"**
3. **Check**: Apakah ada produk yang sudah dibuat?

**Jika belum ada produk:**
- Tambah produk baru dulu
- Lalu sync akan trigger otomatis

**Jika sudah ada produk:**
- Lanjut ke Step 2

---

## 🧪 Step 2: Check Logcat untuk Error

Jalankan command ini:
```bash
adb logcat -d | grep -E "FirestoreSyncService|SyncManager|Error|Exception"
```

**Check untuk:**
- ✅ "Starting products sync..."
- ✅ "Syncing X products"
- ✅ "Products sync completed"
- ❌ Error messages

---

## 🧪 Step 3: Trigger Sync Manual

### Option 1: Tambah Produk Baru
1. **Buka aplikasi**
2. **Tambah produk baru** (misal: "Test Product")
3. **Tunggu 5-10 detik**
4. **Check Firestore Console** → `products/{warungId}/items/`

### Option 2: Edit Produk yang Ada
1. **Buka aplikasi**
2. **Edit produk yang sudah ada**
3. **Save**
4. **Tunggu 5-10 detik**
5. **Check Firestore Console**

### Option 3: Restart App
1. **Force close** aplikasi
2. **Buka lagi** aplikasi
3. Sync akan trigger otomatis saat `onResume()`

---

## 🔍 Troubleshooting

### Issue 1: Document Kosong
**Kemungkinan:**
- Sync belum berjalan
- Sync gagal (check logcat)
- Tidak ada produk di aplikasi

**Solution:**
- Tambah produk baru
- Check logcat untuk error
- Pastikan network online

### Issue 2: Subcollection `items` Kosong
**Kemungkinan:**
- Produk belum di-sync
- Sync gagal

**Solution:**
- Check logcat
- Trigger sync manual (tambah/edit produk)
- Check apakah ada produk di aplikasi

### Issue 3: Sync Tidak Berjalan
**Check:**
1. Network online?
2. User sudah login?
3. WarungId tidak null?
4. Security rules sudah benar?

---

## 📋 Checklist

- [ ] Ada produk di aplikasi?
- [ ] Network online?
- [ ] User sudah login?
- [ ] Sync berjalan (check logcat)?
- [ ] Tidak ada error di logcat?
- [ ] Security rules sudah di-publish?
- [ ] Document `products/{warungId}` ada?
- [ ] Subcollection `items` ada?
- [ ] Documents produk muncul di `items`?

---

## 🎯 Expected Result

Setelah sync berhasil:
1. **Document `products/{warungId}`** - Bisa kosong (hanya parent untuk subcollection)
2. **Subcollection `items`** - Harus ada documents produk
3. **Setiap document produk** - Harus ada fields: name, sellPrice, buyPrice, dll

**Cara check:**
1. Klik `products/{warungId}` di Firestore Console
2. Klik subcollection `items`
3. Seharusnya ada documents dengan productId
4. Klik document produk → Seharusnya ada fields

---

**Silakan check logcat dan tambah produk baru untuk trigger sync!** 🚀

