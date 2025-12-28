# Multi-User Setup Instructions
## Langkah-langkah Setup Firebase

## ⚠️ PENTING: Setup Firebase Terlebih Dahulu

Sebelum aplikasi bisa digunakan dengan fitur multi-user, Anda **HARUS** setup Firebase terlebih dahulu.

### Step 1: Buat Firebase Project

1. Buka [Firebase Console](https://console.firebase.google.com/)
2. Klik **"Add project"** atau **"Create a project"**
3. Masukkan nama project: `WarungKu` (atau nama lain)
4. (Optional) Disable Google Analytics untuk simplicity
5. Klik **"Create project"**

### Step 2: Add Android App

1. Di Firebase Console, klik icon **Android** (🟢)
2. Masukkan:
   - **Package name**: `com.zanjaprogrammer.warungku`
   - **App nickname**: `WarungKu` (optional)
3. Klik **"Register app"**

### Step 3: Download google-services.json

1. Download file `google-services.json`
2. **HAPUS** file placeholder yang ada di `app/google-services.json`
3. **PASTE** file asli dari Firebase ke `app/google-services.json`

### Step 4: Enable Firebase Services

#### Authentication
1. Buka **Authentication** → **Get started**
2. Klik tab **Sign-in method**
3. Enable **Email/Password**
4. Klik **Save**

#### Firestore Database
1. Buka **Firestore Database** → **Create database**
2. Pilih **Start in test mode** (untuk development)
3. Pilih location: `asia-southeast2` (Jakarta) atau terdekat
4. Klik **Enable**

### Step 5: Firestore Security Rules (Development)

Untuk development, gunakan rules ini:

1. Buka **Firestore Database** → **Rules**
2. Paste rules berikut:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read/write untuk semua authenticated users (development only)
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

3. Klik **Publish**

**⚠️ WARNING**: Rules ini hanya untuk development! Untuk production, gunakan rules yang lebih strict (lihat `FIREBASE_SETUP_GUIDE.md`).

### Step 6: Rebuild & Test

1. Rebuild project:
   ```bash
   ./gradlew clean build
   ```

2. Install ke emulator:
   ```bash
   ./gradlew installDebug
   ```

3. Test:
   - Buka aplikasi
   - Seharusnya muncul halaman Login
   - Klik **"Daftar sebagai Pemilik"**
   - Input email & password
   - Registrasi akan membuat warung baru dan user dengan role "owner"

## ✅ Yang Sudah Diimplementasikan

### Phase 1: Foundation ✅
- ✅ Firebase dependencies ditambahkan
- ✅ User model class
- ✅ Warung model class
- ✅ AuthManager (singleton untuk manage authentication)
- ✅ PermissionManager (role-based permission checks)
- ✅ LoginActivity dengan UI modern
- ✅ Register flow untuk owner
- ✅ Login flow
- ✅ Auto-redirect ke Login jika belum login
- ✅ Cache user data untuk offline access

### Yang Belum (Next Phase)
- ⏳ Update existing activities untuk check permissions
- ⏳ Hide/disable UI berdasarkan role
- ⏳ Firestore sync service
- ⏳ Invite employee flow
- ⏳ Activity tracking

## 🧪 Testing

### Test Register Owner:
1. Buka aplikasi
2. Input email & password di form
3. Klik **"Daftar sebagai Pemilik"**
4. Seharusnya:
   - Loading indicator muncul
   - Toast "Registrasi berhasil!"
   - Redirect ke MainActivity
   - User terdaftar sebagai "owner"
   - Warung baru dibuat

### Test Login:
1. Setelah register, logout (akan ditambahkan nanti)
2. Input email & password yang sama
3. Klik **"Masuk"**
4. Seharusnya:
   - Loading indicator muncul
   - Redirect ke MainActivity
   - Data user ter-load dari Firestore

## 📝 Notes

- File `app/google-services.json` adalah placeholder
- **HARUS** diganti dengan file asli dari Firebase Console
- File ini sudah di-ignore di `.gitignore` untuk security
- Untuk production, update Firestore security rules

## 🆘 Troubleshooting

### Error: "File google-services.json is missing"
- Pastikan file sudah di-download dari Firebase Console
- Pastikan file ada di `app/google-services.json`

### Error: "Authentication not enabled"
- Pastikan Email/Password sudah di-enable di Firebase Console
- Refresh Firebase Console

### Error: "Firestore not initialized"
- Pastikan Firestore Database sudah dibuat
- Pastikan security rules sudah di-publish

### Build Error: "Google Services Plugin"
- Pastikan `google-services` plugin sudah ditambahkan di `app/build.gradle`
- Sync Gradle files

## 📚 Dokumentasi Lengkap

Lihat `FIREBASE_SETUP_GUIDE.md` untuk detail lengkap setup Firebase.

