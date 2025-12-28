# Test Sync ke Firestore
## Step-by-Step Guide

## 🔍 Current Status

Di Firestore Console, Anda hanya melihat:
- ✅ `users` collection (ada)
- ✅ `warungs` collection (ada)
- ❌ `products` collection (belum ada)
- ❌ `cash_flows` collection (belum ada)

**Ini normal jika:**
- Belum ada produk di aplikasi
- Belum ada transaksi
- Sync belum berjalan

---

## 🧪 Step 1: Check Apakah Ada Produk di Aplikasi

1. **Buka aplikasi** di emulator
2. **Buka halaman "Stok"**
3. **Check**: Apakah ada produk yang sudah dibuat?

**Jika belum ada produk:**
- Tambah produk baru dulu
- Lalu lanjut ke Step 2

**Jika sudah ada produk:**
- Lanjut ke Step 2

---

## 🧪 Step 2: Trigger Sync Manual

Sync otomatis berjalan saat:
- App resume (onResume)
- Setelah insert/update produk

**Untuk trigger manual:**

### Option 1: Restart App
1. **Force close** aplikasi
2. **Buka lagi** aplikasi
3. Sync akan trigger otomatis saat `onResume()`

### Option 2: Tambah/Update Produk
1. **Tambah produk baru** atau **edit produk yang ada**
2. Sync akan trigger otomatis

### Option 3: Check Logcat
```bash
adb logcat | grep -E "FirestoreSyncService|SyncManager"
```

**Expected output:**
```
SyncManager: Triggering sync...
FirestoreSyncService: Starting products sync...
FirestoreSyncService: Syncing X products
FirestoreSyncService: Products sync completed
```

---

## 🧪 Step 3: Verify di Firestore Console

Setelah sync berjalan:

1. **Buka Firestore Console:**
   https://console.firebase.google.com/project/warungku-725ec/firestore/data

2. **Refresh halaman** (F5 atau Cmd+R)

3. **Check collections:**
   - Seharusnya ada `products` collection
   - Klik `products` → Klik `{warungId}` → Seharusnya ada documents

4. **Jika ada transaksi:**
   - Seharusnya ada `cash_flows` collection
   - Klik `cash_flows` → Klik `{warungId}` → Seharusnya ada documents

---

## ⚠️ Troubleshooting

### Sync Tidak Berjalan?

**Check 1: Network**
- Pastikan device/emulator **online**
- Check: Settings → Network

**Check 2: Authentication**
- Pastikan user **sudah login**
- Check: Firebase Console → Authentication → Users

**Check 3: WarungId**
- Pastikan `warungId` tidak null
- Check logcat: `adb logcat | grep "WarungId"`

**Check 4: Logcat Errors**
```bash
adb logcat | grep -E "Error|Exception|FirestoreSync"
```

**Check 5: Security Rules**
- Pastikan rules allow write
- Check: Firebase Console → Firestore → Rules

---

## 🔧 Manual Trigger Sync (Jika Perlu)

Jika sync tidak berjalan otomatis, kita bisa tambahkan button manual sync nanti.

Untuk sekarang, coba:
1. **Tambah produk baru** di aplikasi
2. **Tunggu 5-10 detik**
3. **Refresh Firestore Console**
4. **Check** apakah `products` collection muncul

---

## 📋 Checklist

- [ ] Ada produk di aplikasi?
- [ ] Network online?
- [ ] User sudah login?
- [ ] Sync berjalan (check logcat)?
- [ ] `products` collection muncul di Firestore?
- [ ] `cash_flows` collection muncul di Firestore (jika ada transaksi)?

---

**Silakan coba tambah produk baru di aplikasi, lalu check Firestore Console lagi!** 🚀

