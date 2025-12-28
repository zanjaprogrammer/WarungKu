# Firebase Setup Guide
## Setup Firebase untuk Multi-User Feature

## 📋 Prerequisites
- Google account
- Firebase Console access

## 🔧 Setup Steps

### 1. Create Firebase Project

1. Buka [Firebase Console](https://console.firebase.google.com/)
2. Klik "Add project" atau "Create a project"
3. Masukkan nama project: `WarungKu` (atau nama lain)
4. Disable Google Analytics (optional, untuk simplicity)
5. Klik "Create project"

### 2. Add Android App to Firebase

1. Di Firebase Console, klik icon Android
2. Masukkan:
   - **Package name**: `com.zanjaprogrammer.warungku`
   - **App nickname**: `WarungKu` (optional)
   - **Debug signing certificate SHA-1**: (optional untuk development)
3. Klik "Register app"

### 3. Download google-services.json

1. Download file `google-services.json`
2. Copy file ke: `app/google-services.json`
   ```
   WarungKu/
   └── app/
       └── google-services.json  ← Paste di sini
   ```

### 4. Enable Firebase Services

Di Firebase Console, enable services berikut:

#### Authentication
1. Buka **Authentication** → **Get started**
2. Enable **Email/Password** sign-in method
3. Klik "Save"

#### Firestore Database
1. Buka **Firestore Database** → **Create database**
2. Pilih **Start in test mode** (untuk development)
3. Pilih location: `asia-southeast2` (Jakarta) atau terdekat
4. Klik "Enable"

### 5. Firestore Security Rules (Development)

Untuk development, gunakan rules ini (temporary, akan diupdate nanti):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read/write untuk semua (development only)
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

**⚠️ WARNING**: Rules ini hanya untuk development! Untuk production, perlu rules yang lebih strict.

### 6. Verify Setup

1. Build project: `./gradlew build`
2. Jika tidak ada error, setup berhasil!

## 🔐 Production Security Rules (Future)

Setelah development selesai, update Firestore rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Helper functions
    function isOwner() {
      return request.auth != null && 
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'owner';
    }
    
    function isManager() {
      return request.auth != null && 
             (get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'owner' ||
              get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'manager');
    }
    
    function getUserWarungId() {
      return get(/databases/$(database)/documents/users/$(request.auth.uid)).data.warungId;
    }
    
    // Users collection
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Warungs collection
    match /warungs/{warungId} {
      allow read: if request.auth != null && getUserWarungId() == warungId;
      allow write: if isOwner() && getUserWarungId() == warungId;
    }
    
    // Products collection
    match /products/{warungId}/{productId} {
      allow read: if request.auth != null && getUserWarungId() == warungId;
      allow create: if isManager() && getUserWarungId() == warungId;
      allow update: if isManager() && getUserWarungId() == warungId;
      allow delete: if isOwner() && getUserWarungId() == warungId;
    }
    
    // Transactions collection
    match /transactions/{warungId}/{transactionId} {
      allow read: if request.auth != null && getUserWarungId() == warungId;
      allow create: if request.auth != null && getUserWarungId() == warungId;
      allow update, delete: if isOwner() && getUserWarungId() == warungId;
    }
    
    // Cash flows collection
    match /cash_flows/{warungId}/{cashFlowId} {
      allow read: if request.auth != null && getUserWarungId() == warungId;
      allow create: if request.auth != null && getUserWarungId() == warungId;
      allow update, delete: if isOwner() && getUserWarungId() == warungId;
    }
  }
}
```

## 📝 Notes

- File `google-services.json` JANGAN di-commit ke public repository
- Tambahkan ke `.gitignore` jika belum ada
- Untuk production, gunakan proper security rules
- Monitor Firebase usage di Firebase Console

## 🆘 Troubleshooting

### Error: "File google-services.json is missing"
- Pastikan file sudah di-copy ke `app/google-services.json`
- Rebuild project

### Error: "Default FirebaseApp is not initialized"
- Pastikan `google-services` plugin sudah ditambahkan di `build.gradle`
- Sync Gradle files

### Authentication not working
- Pastikan Email/Password sudah di-enable di Firebase Console
- Check internet connection

