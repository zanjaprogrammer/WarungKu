# Firebase Setup - Step by Step Guide
## Panduan Lengkap Setup Firebase untuk WarungKu

## 📋 Checklist Setup

- [ ] Step 1: Buat Firebase Project
- [ ] Step 2: Add Android App
- [ ] Step 3: Download google-services.json
- [ ] Step 4: Enable Authentication
- [ ] Step 5: Enable Firestore Database
- [ ] Step 6: Setup Security Rules
- [ ] Step 7: Verify Setup

---

## Step 1: Buat Firebase Project

1. **Buka Browser** dan kunjungi: https://console.firebase.google.com/
2. **Login** dengan Google account Anda
3. Klik tombol **"Add project"** atau **"Create a project"** (tombol besar di tengah)
4. **Masukkan nama project**: 
   - Nama: `WarungKu` (atau nama lain yang Anda suka)
   - Klik **Continue**
5. **Google Analytics** (Optional):
   - Untuk development, Anda bisa **disable** dengan toggle off
   - Atau biarkan enabled (tidak masalah)
   - Klik **Continue**
6. **Review** dan klik **Create project**
7. **Tunggu** sampai project dibuat (sekitar 30 detik)
8. Klik **Continue** setelah project selesai dibuat

✅ **Checkpoint**: Anda sekarang di Firebase Console dashboard

---

## Step 2: Add Android App ke Firebase Project

1. Di Firebase Console dashboard, cari **icon Android** (🟢) atau teks **"Add app"** → **Android**
2. Klik icon/teks tersebut
3. **Masukkan detail app**:
   - **Android package name**: `com.zanjaprogrammer.warungku`
     - ⚠️ **PENTING**: Harus sama persis dengan package name di `app/build.gradle`
   - **App nickname (optional)**: `WarungKu`
   - **Debug signing certificate SHA-1** (optional): Bisa dikosongkan untuk development
4. Klik **Register app**

✅ **Checkpoint**: Anda akan melihat halaman dengan instruksi download `google-services.json`

---

## Step 3: Download google-services.json

1. **Download file** `google-services.json`:
   - Klik tombol **"Download google-services.json"**
   - File akan ter-download ke folder Downloads Anda
2. **Copy file ke project**:
   - Buka file manager
   - Navigate ke: `/Users/ekowibowo/KahfiDev/WarungKu/app/`
   - **HAPUS** file `google-services.json` yang ada (file placeholder)
   - **PASTE** file yang baru di-download ke folder `app/`
   - Pastikan nama file: `google-services.json` (bukan `google-services (1).json`)

✅ **Checkpoint**: File `app/google-services.json` sudah ada dan merupakan file asli dari Firebase

---

## Step 4: Enable Authentication

1. Di Firebase Console, klik menu **"Authentication"** di sidebar kiri
2. Klik tombol **"Get started"** (jika pertama kali)
3. Klik tab **"Sign-in method"** di bagian atas
4. Cari **"Email/Password"** di list
5. Klik **"Email/Password"**
6. **Enable** dengan toggle:
   - Toggle **"Email/Password"** menjadi **ON** (Enabled)
   - Biarkan **"Email link (passwordless sign-in)"** OFF
7. Klik **"Save"**

✅ **Checkpoint**: Email/Password authentication sudah enabled

---

## Step 5: Enable Firestore Database

1. Di Firebase Console, klik menu **"Firestore Database"** di sidebar kiri
2. Klik tombol **"Create database"**
3. **Pilih mode**:
   - Pilih **"Start in test mode"** (untuk development)
   - ⚠️ **Note**: Mode ini hanya untuk development. Untuk production nanti perlu update security rules.
4. Klik **"Next"**
5. **Pilih location**:
   - Pilih: `asia-southeast2 (Jakarta)` atau location terdekat
   - Klik **"Enable"**
6. **Tunggu** sampai database dibuat (sekitar 30 detik)

✅ **Checkpoint**: Firestore Database sudah dibuat dan aktif

---

## Step 6: Setup Security Rules (Development)

1. Di halaman Firestore Database, klik tab **"Rules"** di bagian atas
2. **Replace** semua rules yang ada dengan rules berikut:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Development rules: Allow read/write untuk semua authenticated users
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

3. Klik tombol **"Publish"** di bagian atas
4. **Konfirmasi** jika ada dialog

✅ **Checkpoint**: Security rules sudah di-publish

---

## Step 7: Verify Setup

### Verify di Firebase Console:
- ✅ Authentication → Sign-in method → Email/Password = Enabled
- ✅ Firestore Database → Database created
- ✅ Firestore Database → Rules = Published

### Verify di Project:
1. **Check file**:
   ```bash
   ls -la app/google-services.json
   ```
   - File harus ada dan bukan placeholder

2. **Build project**:
   ```bash
   ./gradlew clean build
   ```
   - Harus build **SUCCESS** tanpa error

3. **Install ke emulator**:
   ```bash
   ./gradlew installDebug
   ```

---

## 🧪 Test Setup

1. **Buka aplikasi** di emulator
2. **Seharusnya muncul** halaman Login (bukan langsung ke MainActivity)
3. **Test Register**:
   - Input email: `test@example.com`
   - Input password: `test123` (minimal 6 karakter)
   - Klik **"Daftar sebagai Pemilik"**
   - Seharusnya:
     - Loading indicator muncul
     - Toast "Registrasi berhasil!"
     - Redirect ke MainActivity

4. **Check Firebase Console**:
   - Buka **Authentication** → **Users**
     - Seharusnya ada 1 user dengan email yang Anda daftarkan
   - Buka **Firestore Database** → **Data**
     - Seharusnya ada collection `users` dan `warungs`

---

## 🆘 Troubleshooting

### Error: "File google-services.json is missing"
**Solution:**
- Pastikan file sudah di-download dari Firebase Console
- Pastikan file ada di `app/google-services.json` (bukan di subfolder)
- Rebuild project: `./gradlew clean build`

### Error: "Authentication not enabled"
**Solution:**
- Pastikan Email/Password sudah di-enable di Firebase Console
- Refresh browser dan cek lagi
- Pastikan internet connection aktif

### Error: "Firestore not initialized"
**Solution:**
- Pastikan Firestore Database sudah dibuat
- Pastikan security rules sudah di-publish
- Check di Firebase Console → Firestore Database → Data

### Error: "Package name mismatch"
**Solution:**
- Pastikan package name di Firebase Console sama dengan di `app/build.gradle`
- Package name harus: `com.zanjaprogrammer.warungku`
- Jika berbeda, hapus app di Firebase Console dan buat ulang dengan package name yang benar

### Build Error: "Google Services Plugin"
**Solution:**
- Pastikan `google-services` plugin sudah ditambahkan di `build.gradle`
- Sync Gradle files di Android Studio
- Atau run: `./gradlew clean build`

---

## 📝 Notes

- File `google-services.json` **JANGAN** di-commit ke public repository (sudah di-ignore)
- Security rules saat ini adalah untuk **development only**
- Untuk production, perlu update rules yang lebih strict (lihat `FIREBASE_SETUP_GUIDE.md`)

---

## ✅ Setup Complete!

Jika semua step sudah selesai dan test berhasil, Firebase setup sudah complete!

**Next Steps:**
- Test login/register flow
- Lanjutkan implementasi Phase 2 (Permission checks di activities)

---

**Last Updated:** 2024-12-28

