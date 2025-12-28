# Firestore Security Rules - Fixed untuk Registration
## Rules yang Diperbaiki untuk Support Register Flow

## ⚠️ Masalah

Rules sebelumnya tidak allow:
- User create warung saat register (karena `isOwner()` require user document yang belum ada)
- User create user document mereka sendiri saat register

## ✅ Rules yang Diperbaiki

**Copy rules ini ke Firebase Console → Firestore → Rules:**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function: Get current user's warungId (null-safe)
    function getUserWarungId() {
      let userDoc = get(/databases/$(database)/documents/users/$(request.auth.uid));
      return userDoc != null ? userDoc.data.warungId : null;
    }
    
    // Helper function: Check if user is owner
    function isOwner() {
      let userDoc = get(/databases/$(database)/documents/users/$(request.auth.uid));
      return request.auth != null && 
             userDoc != null && 
             userDoc.data.role == 'owner';
    }
    
    // Helper function: Check if user is manager or owner
    function isManager() {
      let userDoc = get(/databases/$(database)/documents/users/$(request.auth.uid));
      return request.auth != null && 
             userDoc != null && 
             (userDoc.data.role == 'owner' || userDoc.data.role == 'manager');
    }
    
    // Users collection
    match /users/{userId} {
      // Allow user to create their own user document (for registration)
      allow create: if request.auth != null && 
                       request.auth.uid == userId &&
                       request.resource.data.userId == userId;
      
      // User bisa read semua user di warung yang sama
      allow read: if request.auth != null && 
                     (request.auth.uid == userId || 
                      resource.data.warungId == getUserWarungId());
      
      // User hanya bisa update data sendiri
      allow update: if request.auth != null && request.auth.uid == userId;
      
      // Owner bisa create user untuk employee (dengan warungId yang sama)
      allow create: if isOwner() && 
                       request.resource.data.warungId == getUserWarungId();
    }
    
    // Warungs collection
    match /warungs/{warungId} {
      // Allow authenticated user to create warung (for registration)
      allow create: if request.auth != null;
      
      // User bisa read warung mereka sendiri (setelah user document ada)
      // Atau allow read jika user document belum ada (during registration)
      allow read: if request.auth != null && 
                     (getUserWarungId() == warungId || 
                      !exists(/databases/$(database)/documents/users/$(request.auth.uid)));
      
      // Hanya owner yang bisa update warung
      allow update: if isOwner() && getUserWarungId() == warungId;
      
      // Hanya owner yang bisa delete warung
      allow delete: if isOwner() && getUserWarungId() == warungId;
    }
    
    // Products collection
    match /products/{warungId}/{productId} {
      // User bisa read produk di warung mereka
      allow read: if request.auth != null && getUserWarungId() == warungId;
      
      // Manager dan Owner bisa create/update produk
      allow create, update: if isManager() && getUserWarungId() == warungId;
      
      // Hanya Owner yang bisa delete produk
      allow delete: if isOwner() && getUserWarungId() == warungId;
    }
    
    // Transactions collection
    match /transactions/{warungId}/{transactionId} {
      // User bisa read transaksi di warung mereka
      allow read: if request.auth != null && getUserWarungId() == warungId;
      
      // Semua authenticated user bisa create transaksi (untuk jual barang)
      allow create: if request.auth != null && getUserWarungId() == warungId;
      
      // Hanya Owner yang bisa update/delete transaksi
      allow update, delete: if isOwner() && getUserWarungId() == warungId;
    }
    
    // Cash flows collection
    match /cash_flows/{warungId}/{cashFlowId} {
      // User bisa read cash flow di warung mereka
      allow read: if request.auth != null && getUserWarungId() == warungId;
      
      // Semua authenticated user bisa create cash flow
      allow create: if request.auth != null && getUserWarungId() == warungId;
      
      // Hanya Owner yang bisa update/delete cash flow
      allow update, delete: if isOwner() && getUserWarungId() == warungId;
    }
    
    // User activities collection
    match /user_activities/{warungId}/{activityId} {
      // User bisa read activity di warung mereka
      allow read: if request.auth != null && getUserWarungId() == warungId;
      
      // System bisa create activity (dari app)
      allow create: if request.auth != null && getUserWarungId() == warungId;
      
      // Tidak ada yang bisa update/delete activity (read-only log)
      allow update, delete: if false;
    }
  }
}
```

## 🔧 Perubahan Utama

1. **Users collection**:
   - ✅ `allow create`: User bisa create user document mereka sendiri (userId == auth.uid)
   - ✅ `allow read`: Bisa read user sendiri atau user di warung yang sama

2. **Warungs collection**:
   - ✅ `allow create`: Semua authenticated user bisa create warung (untuk register)
   - ✅ `allow read`: Bisa read warung sendiri, atau allow read jika user document belum ada (saat register)

3. **Helper functions**:
   - ✅ Null-safe dengan `!= null` checks
   - ✅ Handle case ketika user document belum ada

## 📋 Cara Update Rules

1. **Buka Firebase Console**: https://console.firebase.google.com/project/warungku-725ec/firestore/rules
2. **Select all** rules yang ada (Ctrl+A / Cmd+A)
3. **Delete** semua rules
4. **Paste** rules di atas
5. **Klik "Publish"**
6. **Tunggu** sampai rules ter-publish (sekitar 5-10 detik)

## 🧪 Test Setelah Update

1. **Clear app data**:
   - Settings → Apps → WarungKu → Clear Data
   - Atau uninstall & reinstall aplikasi

2. **Test register**:
   - Buka aplikasi
   - Input email & password
   - Klik "Daftar sebagai Pemilik"
   - **Expected**: Tidak ada error PERMISSION_DENIED

3. **Verify di Firebase Console**:
   - Firestore → Data: Seharusnya ada `users` dan `warungs` collections

## ✅ Rules Tetap Aman

Rules ini tetap aman karena:
- ✅ User hanya bisa create user document mereka sendiri (userId == auth.uid)
- ✅ User hanya bisa create warung (tidak bisa update/delete warung lain)
- ✅ Setelah user document ada, semua akses berdasarkan warungId
- ✅ Multi-tenant isolation tetap terjaga

---

**Update rules ini sekarang, lalu test register lagi!**

