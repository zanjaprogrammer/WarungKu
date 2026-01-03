# Update Firestore Security Rules
## Fix untuk Subcollections

## ⚠️ Masalah

Path Firestore sebelumnya menggunakan 3 segments (tidak valid):
- `products/{warungId}/{productId}` ❌
- `cash_flows/{warungId}/{cashFlowId}` ❌

Firestore memerlukan **jumlah segment genap** (2, 4, 6, dll).

## ✅ Solusi

Gunakan **subcollections**:
- `products/{warungId}/items/{productId}` ✅ (4 segments)
- `cash_flows/{warungId}/transactions/{cashFlowId}` ✅ (4 segments)

---

## 📋 Update Security Rules

**Buka Firebase Console:**
https://console.firebase.google.com/project/warungku-725ec/firestore/rules

**Replace rules untuk `products` dan `cash_flows`:**

```javascript
    // Products collection (with subcollection)
    match /products/{warungId} {
      // Warung document
      allow read: if request.auth != null && getUserWarungId() == warungId;
      
      // Products subcollection: products/{warungId}/items/{productId}
      match /items/{productId} {
        // User bisa read produk di warung mereka
        allow read: if request.auth != null && getUserWarungId() == warungId;
        // Manager dan Owner bisa create/update produk
        allow create, update: if isManager() && getUserWarungId() == warungId;
        // Hanya Owner yang bisa delete produk
        allow delete: if isOwner() && getUserWarungId() == warungId;
      }
    }
    
    // Cash flows collection (with subcollection)
    match /cash_flows/{warungId} {
      // Warung document
      allow read: if request.auth != null && getUserWarungId() == warungId;
      
      // Cash flows subcollection: cash_flows/{warungId}/transactions/{cashFlowId}
      match /transactions/{cashFlowId} {
        // User bisa read cash flow di warung mereka
        allow read: if request.auth != null && getUserWarungId() == warungId;
        // Semua authenticated user bisa create cash flow
        allow create: if request.auth != null && getUserWarungId() == warungId;
        // Hanya Owner yang bisa update/delete cash flow
        allow update, delete: if isOwner() && getUserWarungId() == warungId;
      }
    }
```

---

## 📝 Full Rules (Updated)

**Copy semua rules ini ke Firebase Console:**

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
    
    // Products collection (with subcollection)
    match /products/{warungId} {
      // Warung document
      allow read: if request.auth != null && getUserWarungId() == warungId;
      
      // Products subcollection: products/{warungId}/items/{productId}
      match /items/{productId} {
        // User bisa read produk di warung mereka
        allow read: if request.auth != null && getUserWarungId() == warungId;
        // Manager dan Owner bisa create/update produk
        allow create, update: if isManager() && getUserWarungId() == warungId;
        // Hanya Owner yang bisa delete produk
        allow delete: if isOwner() && getUserWarungId() == warungId;
      }
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
    
    // Cash flows collection (with subcollection)
    match /cash_flows/{warungId} {
      // Warung document
      allow read: if request.auth != null && getUserWarungId() == warungId;
      
      // Cash flows subcollection: cash_flows/{warungId}/transactions/{cashFlowId}
      match /transactions/{cashFlowId} {
        // User bisa read cash flow di warung mereka
        allow read: if request.auth != null && getUserWarungId() == warungId;
        // Semua authenticated user bisa create cash flow
        allow create: if request.auth != null && getUserWarungId() == warungId;
        // Hanya Owner yang bisa update/delete cash flow
        allow update, delete: if isOwner() && getUserWarungId() == warungId;
      }
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

---

## ✅ Setelah Update Rules

1. **Publish rules** di Firebase Console
2. **Restart aplikasi** di emulator
3. **Tambah produk baru** atau **edit produk yang ada**
4. **Check Firestore Console** → `products/{warungId}/items/`
5. **Expected**: Data produk muncul di subcollection `items`

---

**Update rules ini sekarang, lalu test sync lagi!** 🚀

