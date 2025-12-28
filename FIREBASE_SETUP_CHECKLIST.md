# Firebase Setup Checklist
## Checklist untuk memastikan Firebase sudah setup dengan benar

## ✅ File Configuration
- [x] ✅ `google-services.json` sudah di-copy ke `app/google-services.json`
- [x] ✅ File bukan placeholder (sudah verified)
- [x] ✅ Package name match: `com.zanjaprogrammer.warungku`
- [x] ✅ Build project berhasil

## ⚠️ Firebase Console Setup (PENTING!)

### Authentication
- [ ] **Enable Email/Password Authentication**
  1. Buka: https://console.firebase.google.com/project/warungku-725ec/authentication/providers
  2. Klik **"Email/Password"**
  3. Toggle **"Enable"** menjadi ON
  4. Klik **"Save"**

### Firestore Database
- [ ] **Create Firestore Database**
  1. Buka: https://console.firebase.google.com/project/warungku-725ec/firestore
  2. Klik **"Create database"** (jika belum ada)
  3. ⚠️ **PENTING**: Pilih **"Start in production mode"** (JANGAN test mode!)
     - Karena aplikasi akan di-launch di Play Store, kita perlu production-ready dari awal
  4. Pilih location: **`asia-southeast2 (Jakarta)`** (recommended untuk Indonesia)
     - Alternatif: `asia-southeast1 (Singapore)` jika Jakarta tidak tersedia
  5. Klik **"Enable"**

- [ ] **Setup Security Rules (PRODUCTION)**
  1. Di halaman Firestore, klik tab **"Rules"**
  2. ⚠️ **PENTING**: Karena aplikasi akan di-launch, gunakan **production rules**
  3. Paste rules dari `FIRESTORE_PRODUCTION_SETUP.md` (rules yang lebih strict dan aman)
  4. Klik **"Publish"**
  5. **Test rules** dengan Rules Playground/Simulator

## 🧪 Testing

Setelah semua checklist di atas selesai:

1. **Install aplikasi**:
   ```bash
   ./gradlew installDebug
   ```

2. **Buka aplikasi** di emulator

3. **Test Register**:
   - Seharusnya muncul halaman Login
   - Input email: `test@example.com`
   - Input password: `test123`
   - Klik **"Daftar sebagai Pemilik"**
   - Seharusnya:
     - Loading indicator muncul
     - Toast "Registrasi berhasil!"
     - Redirect ke MainActivity

4. **Verify di Firebase Console**:
   - **Authentication** → **Users**: Seharusnya ada 1 user
   - **Firestore Database** → **Data**: Seharusnya ada collections `users` dan `warungs`

## 🔗 Quick Links

- **Firebase Console**: https://console.firebase.google.com/project/warungku-725ec
- **Authentication**: https://console.firebase.google.com/project/warungku-725ec/authentication/providers
- **Firestore Database**: https://console.firebase.google.com/project/warungku-725ec/firestore

## ✅ Setup Complete!

Jika semua checklist sudah ✅ dan test berhasil, Firebase setup sudah complete!

