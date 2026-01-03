# Firestore Console Guide
## Cara Akses dan Check Data Sync

## 🔗 Quick Links

### Firestore Database (Data)
**Link langsung:**
https://console.firebase.google.com/project/warungku-725ec/firestore/data

**Cara akses manual:**
1. Buka: https://console.firebase.google.com/
2. Pilih project: **warungku-725ec**
3. Klik menu **"Firestore Database"** di sidebar kiri
4. Klik tab **"Data"**

---

## 📊 Collections yang Harus Ada

Setelah sync, seharusnya ada collections berikut:

### 1. **products** (Produk)
- Path: `products/{warungId}/{productId}`
- **Cara check:**
  1. Buka Firestore Console
  2. Klik collection **"products"**
  3. Klik subcollection dengan **warungId** Anda
  4. Seharusnya ada documents dengan productId

**Fields yang ada:**
- `name`: Nama produk
- `sellPrice`: Harga jual
- `buyPrice`: Harga beli
- `currentStock`: Stok saat ini
- `minStock`: Stok minimum
- `salesCount`: Jumlah penjualan
- `isFavorite`: Favorite flag
- `lastSoldTimestamp`: Timestamp terakhir dijual
- `barcode`: Barcode (nullable)
- `createdAt`: Timestamp dibuat
- `updatedAt`: Timestamp di-update

### 2. **cash_flows** (Transaksi)
- Path: `cash_flows/{warungId}/{cashFlowId}`
- **Cara check:**
  1. Buka Firestore Console
  2. Klik collection **"cash_flows"**
  3. Klik subcollection dengan **warungId** Anda
  4. Seharusnya ada documents dengan cashFlowId

**Fields yang ada:**
- `type`: "IN" atau "OUT"
- `amount`: Jumlah uang
- `description`: Deskripsi transaksi
- `timestamp`: Timestamp transaksi
- `productId`: ID produk (nullable)
- `profit`: Profit (nullable)
- `userId`: ID user yang melakukan transaksi
- `createdAt`: Timestamp dibuat

### 3. **users** (User Data)
- Path: `users/{userId}`
- **Cara check:**
  1. Buka Firestore Console
  2. Klik collection **"users"**
  3. Seharusnya ada document dengan userId Anda

### 4. **warungs** (Warung Data)
- Path: `warungs/{warungId}`
- **Cara check:**
  1. Buka Firestore Console
  2. Klik collection **"warungs"**
  3. Seharusnya ada document dengan warungId Anda

---

## 🧪 Testing Sync

### Step 1: Tambah Produk Baru
1. Buka aplikasi
2. Tambah produk baru (misal: "Test Product")
3. Tunggu beberapa detik
4. Buka Firestore Console → `products/{warungId}`
5. **Expected**: Ada document baru dengan nama "Test Product"

### Step 2: Update Produk
1. Buka aplikasi
2. Edit produk yang sudah ada
3. Ubah nama atau harga
4. Tunggu beberapa detik
5. Buka Firestore Console → `products/{warungId}/{productId}`
6. **Expected**: Field `updatedAt` berubah

### Step 3: Jual Produk
1. Buka aplikasi
2. Jual produk (checkout)
3. Tunggu beberapa detik
4. Buka Firestore Console → `cash_flows/{warungId}`
5. **Expected**: Ada document baru dengan type "IN"

---

## 🔍 Tips Debugging

### Jika Data Tidak Muncul di Firestore:

1. **Check Logcat:**
   ```bash
   adb logcat | grep -E "FirestoreSyncService|SyncManager"
   ```
   - Look for: "Starting products sync..."
   - Look for: "Syncing X products"
   - Look for: "Products sync completed"

2. **Check Network:**
   - Pastikan device/emulator online
   - Check: Settings → Network

3. **Check Authentication:**
   - Pastikan user sudah login
   - Check: Firebase Console → Authentication → Users

4. **Check WarungId:**
   - Pastikan `warungId` tidak null
   - Check di logcat: "WarungId is null, cannot sync"

5. **Check Security Rules:**
   - Pastikan rules allow write
   - Check: Firebase Console → Firestore → Rules

---

## 📱 Real-time Monitoring

### Firestore Console Features:
- **Real-time updates**: Data update otomatis di console
- **Filter**: Bisa filter berdasarkan field
- **Search**: Bisa search documents
- **Export**: Bisa export data ke JSON/CSV

---

## ⚠️ Troubleshooting

### Error: "Permission denied"
**Solution:**
- Check security rules di Firestore Console
- Pastikan rules allow write untuk authenticated users

### Error: "Network not available"
**Solution:**
- Check internet connection
- Pastikan device/emulator online

### Data tidak sync
**Solution:**
1. Check logcat untuk error
2. Restart app
3. Trigger manual sync (akan ditambahkan nanti)

---

## 🔗 Other Useful Links

- **Authentication Console:**
  https://console.firebase.google.com/project/warungku-725ec/authentication/users

- **Project Overview:**
  https://console.firebase.google.com/project/warungku-725ec/overview

- **Firestore Rules:**
  https://console.firebase.google.com/project/warungku-725ec/firestore/rules

---

**Silakan buka Firestore Console dan check apakah data sudah ter-sync!** 🚀

