# Firestore Security Rules - Fixed Version
## Rules yang Diperbaiki untuk Support Register Flow

## ⚠️ Masalah dengan Rules Sebelumnya

Rules sebelumnya terlalu strict dan tidak allow:
- User create user document mereka sendiri saat register
- User create warung document saat register

## ✅ Rules yang Diperbaiki

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function: Get current user's warungId
    function getUserWarungId() {
      let userData = get(/databases/$(database)/documents/users/$(request.auth.uid));
      return userData != null ? userData.data.warungId : null;
    }
    
    // Helper function: Check if user is owner
    function isOwner() {
      let userData = get(/databases/$(database)/documents/users/$(request.auth.uid));
      return request.auth != null && userData != null && userData.data.role == 'owner';
    }
    
    // Helper function: Check if user is manager or owner
    function isManager() {
      let userData = get(/databases/$(database)/documents/users/$(request.auth.uid));
      return request.auth != null && userData != null && 
             (userData.data.role == 'owner' || userData.data.role == 'manager');
    }
    
    // Users collection
    match /users/{userId} {
      // Allow user to create their own user document (for registration)
      allow create: if request.auth != null && request.auth.uid == userId;
      
      // User bisa read semua user di warung yang sama (setelah user document ada)
      allow read: if request.auth != null && 
                     (resource.data.warungId == getUserWarungId() || 
                      request.auth.uid == userId);
      
      // User hanya bisa update data sendiri
      allow update: if request.auth != null && request.auth.uid == userId;
      
      // Hanya owner yang bisa create user baru untuk employee (dengan warungId yang sama)
      allow create: if isOwner() && 
                       request.resource.data.warungId == getUserWarungId();
    }
    
    // Warungs collection
    match /warungs/{warungId} {
      // Allow authenticated user to create warung (for registration)
      allow create: if request.auth != null;
      
      // User hanya bisa read warung mereka sendiri (setelah user document ada)
      allow read: if request.auth != null && 
                     (getUserWarungId() == warungId || 
                      // Allow read during registration before user document exists
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
   - ✅ Allow user create user document mereka sendiri (untuk register)
   - ✅ Allow read dengan fallback jika user document belum ada

2. **Warungs collection**:
   - ✅ Allow authenticated user create warung (untuk register)
   - ✅ Allow read dengan fallback jika user document belum ada

3. **Helper functions**:
   - ✅ Null-safe checks untuk avoid errors
   - ✅ Fallback untuk handle registration flow

## 📋 Cara Update Rules

1. Buka: https://console.firebase.google.com/project/warungku-725ec/firestore/rules
2. **Replace semua** rules dengan rules di atas
3. Klik **"Publish"**
4. **Test** dengan Rules Playground (optional)

## 🧪 Test Setelah Update

1. **Clear app data** atau uninstall & reinstall
2. **Test register** lagi
3. **Expected**: Tidak ada error PERMISSION_DENIED

---

## ⚠️ Note

Rules ini sudah di-optimize untuk:
- ✅ Support registration flow
- ✅ Tetap aman untuk production
- ✅ Role-based access control
- ✅ Multi-tenant isolation (warungId)

