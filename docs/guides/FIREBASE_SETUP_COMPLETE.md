# Firebase Setup Complete - Next Steps
## Testing & Verification

## ✅ Setup Checklist (Verify)

Pastikan semua sudah selesai:

- [x] ✅ `google-services.json` sudah di-copy
- [x] ✅ Authentication (Email/Password) enabled
- [x] ✅ Firestore Database created
- [x] ✅ Security Rules published
- [ ] ⏳ Test aplikasi

---

## 🧪 Step 1: Build & Install Aplikasi

```bash
./gradlew clean build
./gradlew installDebug
```

**Expected:**
- Build SUCCESS
- Aplikasi terinstall di emulator

---

## 🧪 Step 2: Test Register Owner

1. **Buka aplikasi** di emulator
2. **Seharusnya muncul** halaman Login (bukan langsung ke MainActivity)
3. **Input data**:
   - Email: `test@example.com` (atau email Anda)
   - Password: `test123` (minimal 6 karakter)
4. **Klik** "Daftar sebagai Pemilik"
5. **Expected**:
   - Loading indicator muncul
   - Toast "Registrasi berhasil!"
   - Redirect ke MainActivity
   - Tidak ada error

---

## 🧪 Step 3: Verify di Firebase Console

### Authentication
1. Buka: https://console.firebase.google.com/project/warungku-725ec/authentication/users
2. **Seharusnya ada**:
   - 1 user dengan email yang Anda daftarkan
   - Status: Enabled

### Firestore Database
1. Buka: https://console.firebase.google.com/project/warungku-725ec/firestore/data
2. **Seharusnya ada collections**:
   - `users` → 1 document (user yang baru register)
   - `warungs` → 1 document (warung baru)

**Check data:**
- `users/{userId}`:
  - `email`: email yang Anda input
  - `role`: "owner"
  - `warungId`: ID warung yang dibuat
  - `name`: nama dari email
  - `isActive`: true

- `warungs/{warungId}`:
  - `name`: "Warung {email_username}"
  - `ownerId`: userId dari user
  - `createdAt`: timestamp

---

## 🧪 Step 4: Test Login

1. **Logout** (akan ditambahkan nanti, untuk sekarang restart app)
2. **Input** email & password yang sama
3. **Klik** "Masuk"
4. **Expected**:
   - Loading indicator muncul
   - Redirect ke MainActivity
   - Data user ter-load

---

## 🧪 Step 5: Test Aplikasi Normal

Setelah login berhasil, test fitur normal:

1. **Home page** - seharusnya muncul normal
2. **Tambah produk** - test apakah bisa
3. **Jual barang** - test apakah bisa
4. **Lihat stok** - test apakah bisa

**Expected**: Semua fitur berfungsi normal seperti sebelum ada authentication

---

## ⚠️ Troubleshooting

### Error: "Permission denied" saat register/login
**Solution:**
- Check security rules sudah di-publish
- Pastikan rules menggunakan `request.auth != null`
- Test dengan Rules Playground

### Error: "User not found" setelah login
**Solution:**
- Check apakah user document sudah dibuat di Firestore
- Check `users` collection di Firebase Console
- Pastikan `warungId` sudah di-set

### Error: "Network error" atau timeout
**Solution:**
- Check internet connection
- Check Firebase Console apakah service aktif
- Pastikan `google-services.json` sudah benar

### Aplikasi langsung ke MainActivity (tidak muncul Login)
**Solution:**
- Check apakah user sudah login sebelumnya (cache)
- Clear app data: Settings → Apps → WarungKu → Clear Data
- Atau uninstall & reinstall

---

## ✅ Setup Complete!

Jika semua test berhasil, Firebase setup sudah **COMPLETE**! 🎉

---

## 🚀 Next: Phase 2 Implementation

Setelah Firebase setup selesai, kita bisa lanjutkan ke:

### Phase 2: Permission Checks di Activities
- Update semua activities untuk check permissions
- Hide/disable UI berdasarkan role
- Test dengan user berbeda role

### Phase 3: Firestore Sync Service
- Sync local → cloud
- Sync cloud → local
- Real-time listeners

### Phase 4: Invite Employee Flow
- Owner bisa invite karyawan
- Employee registration flow

---

**Status**: Ready untuk testing! 🚀

