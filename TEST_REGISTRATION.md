# Test Registration & Login
## Step-by-Step Testing Guide

## ✅ Pre-Checklist

- [x] Security rules sudah di-update di Firebase Console
- [x] Aplikasi sudah di-reinstall di emulator
- [x] Logcat sudah di-clear

---

## 🧪 Test 1: Register New User

### Steps:
1. **Buka aplikasi** di emulator
   - Seharusnya muncul halaman **Login** (bukan langsung ke MainActivity)

2. **Input data registrasi**:
   - Email: `test@example.com` (atau email Anda)
   - Password: `test123` (minimal 6 karakter)

3. **Klik** "Daftar sebagai Pemilik"

4. **Expected Result**:
   - ✅ Loading indicator muncul
   - ✅ Toast "Registrasi berhasil!"
   - ✅ Redirect ke MainActivity (halaman home)
   - ✅ Tidak ada error PERMISSION_DENIED
   - ✅ Tidak ada crash

5. **Jika ada error**, check:
   - Logcat untuk error message
   - Firebase Console → Firestore → Data (apakah collections terbuat?)

---

## 🧪 Test 2: Verify di Firebase Console

### Authentication:
1. Buka: https://console.firebase.google.com/project/warungku-725ec/authentication/users
2. **Seharusnya ada**:
   - 1 user dengan email yang Anda daftarkan
   - Status: **Enabled**

### Firestore Database:
1. Buka: https://console.firebase.google.com/project/warungku-725ec/firestore/data
2. **Seharusnya ada collections**:
   - `users` → 1 document
   - `warungs` → 1 document

3. **Check user document** (`users/{userId}`):
   - `email`: email yang Anda input
   - `role`: "owner"
   - `warungId`: ID warung yang dibuat
   - `name`: nama dari email
   - `userId`: sama dengan Firebase Auth UID

4. **Check warung document** (`warungs/{warungId}`):
   - `name`: "Warung {email_username}"
   - `ownerId`: userId dari user
   - `warungId`: ID warung

---

## 🧪 Test 3: Test Login

### Steps:
1. **Restart aplikasi** (atau clear app data)
   - Settings → Apps → WarungKu → Force Stop
   - Atau uninstall & reinstall

2. **Input** email & password yang sama

3. **Klik** "Masuk"

4. **Expected Result**:
   - ✅ Loading indicator muncul
   - ✅ Toast "Login berhasil!" (atau tidak ada error)
   - ✅ Redirect ke MainActivity
   - ✅ Data user ter-load

---

## 🧪 Test 4: Test Fitur Normal

Setelah login berhasil, test fitur normal:

1. **Home page** - seharusnya muncul normal
2. **Tambah produk** - test apakah bisa
3. **Jual barang** - test apakah bisa
4. **Lihat stok** - test apakah bisa
5. **Lihat ringkasan** - test apakah bisa

**Expected**: Semua fitur berfungsi normal seperti sebelum ada authentication

---

## ⚠️ Troubleshooting

### Error: "PERMISSION_DENIED" masih muncul
**Solution:**
- Pastikan rules sudah di-publish (tunggu 10-15 detik setelah publish)
- Check apakah rules yang di-paste sudah benar (copy semua, termasuk `rules_version = '2';`)
- Clear app data dan test lagi

### Error: "User not found" setelah login
**Solution:**
- Check apakah user document sudah dibuat di Firestore
- Check `users` collection di Firebase Console
- Pastikan `warungId` sudah di-set

### Error: "Network error" atau timeout
**Solution:**
- Check internet connection emulator
- Check Firebase Console apakah service aktif
- Pastikan `google-services.json` sudah benar

### Aplikasi langsung ke MainActivity (tidak muncul Login)
**Solution:**
- User sudah login sebelumnya (cache)
- Clear app data: Settings → Apps → WarungKu → Clear Data
- Atau uninstall & reinstall

### Crash saat register/login
**Solution:**
- Check logcat untuk error stack trace
- Pastikan semua dependencies sudah ter-install
- Rebuild aplikasi: `./gradlew clean build`

---

## ✅ Success Criteria

Registration & Login berhasil jika:
- ✅ Tidak ada error PERMISSION_DENIED
- ✅ User document terbuat di Firestore
- ✅ Warung document terbuat di Firestore
- ✅ User bisa login setelah register
- ✅ Semua fitur normal berfungsi

---

**Silakan test sekarang dan beri tahu hasilnya!** 🚀

