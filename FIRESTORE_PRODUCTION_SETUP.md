    # Firestore Database Setup untuk Production
## Konfigurasi yang Tepat untuk Aplikasi yang akan di-Launch di Play Store

## ⚠️ PENTING: Production-Ready Setup

Karena aplikasi akan di-launch di Play Store, kita perlu setup yang **aman dan production-ready** dari awal.

---

## 📍 Step 1: Pilih Location

### Rekomendasi: `asia-southeast2 (Jakarta)`

**Kenapa Jakarta?**
- ✅ Terdekat dengan Indonesia (target market utama)
- ✅ Latency rendah untuk user Indonesia
- ✅ Cost-effective
- ✅ Support untuk semua fitur Firestore

**Alternatif (jika Jakarta tidak tersedia):**
- `asia-southeast1` (Singapore) - juga bagus untuk Indonesia
- `asia-south1` (Mumbai) - lebih jauh tapi masih acceptable

**JANGAN pilih:**
- ❌ `us-central` atau region US (latency tinggi untuk Indonesia)
- ❌ `europe-west` (terlalu jauh)

---

## 🔒 Step 2: Pilih Mode

### ⚠️ JANGAN Pilih "Start in test mode"

**Kenapa?**
- Test mode = **TIDAK AMAN** untuk production
- Semua data bisa diakses siapa saja (jika tahu project ID)
- Tidak cocok untuk aplikasi yang akan di-launch

### ✅ Pilih "Start in production mode"

**Kenapa?**
- ✅ **AMAN** - hanya bisa diakses dengan security rules
- ✅ Production-ready dari awal
- ✅ Tidak perlu migrate nanti
- ✅ Best practice untuk aplikasi yang akan di-launch

**Apa yang terjadi?**
- Database dibuat dengan security rules yang **DENY semua** secara default
- Kita akan setup security rules sendiri (lebih aman)

---

## 🛡️ Step 3: Security Rules (PRODUCTION)

Setelah database dibuat, **SEGERA** setup security rules yang aman:

### Rules untuk Production (Recommended):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function: Get current user's warungId
    function getUserWarungId() {
      return get(/databases/$(database)/documents/users/$(request.auth.uid)).data.warungId;
    }
    
    // Helper function: Check if user is owner
    function isOwner() {
      return request.auth != null && 
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'owner';
    }
    
    // Helper function: Check if user is manager or owner
    function isManager() {
      return request.auth != null && 
             (get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'owner' ||
              get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'manager');
    }
    
    // Users collection
    match /users/{userId} {
      // User bisa read semua user di warung yang sama
      allow read: if request.auth != null && 
                     get(/databases/$(database)/documents/users/$(request.auth.uid)).data.warungId == 
                     resource.data.warungId;
      // User hanya bisa update data sendiri
      allow update: if request.auth != null && request.auth.uid == userId;
      // Hanya owner yang bisa create user baru (untuk invite employee)
      allow create: if isOwner();
    }
    
    // Warungs collection
    match /warungs/{warungId} {
      // User hanya bisa read warung mereka sendiri
      allow read: if request.auth != null && getUserWarungId() == warungId;
      // Hanya owner yang bisa update warung
      allow write: if isOwner() && getUserWarungId() == warungId;
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
    
    // Invites collection
    match /invites/{inviteId} {
      // Owner bisa read invites untuk warung mereka
      allow read: if request.auth != null && 
                     (resource.data.ownerId == request.auth.uid || 
                      resource.data.email == get(/databases/$(database)/documents/users/$(request.auth.uid)).data.email);
      // Owner bisa create invite untuk warung mereka
      allow create: if request.auth != null && 
                       isOwner() && 
                       request.resource.data.ownerId == request.auth.uid &&
                       request.resource.data.warungId == getUserWarungId();
      // Owner bisa update invite (untuk accept/expire)
      allow update: if request.auth != null && 
                       (isOwner() && resource.data.ownerId == request.auth.uid ||
                        resource.data.email == get(/databases/$(database)/documents/users/$(request.auth.uid)).data.email);
      // Owner bisa delete invite
      allow delete: if request.auth != null && 
                       isOwner() && 
                       resource.data.ownerId == request.auth.uid;
    }
  }
}
```

---

## 📋 Checklist Setup Firestore untuk Production

- [ ] **Location**: Pilih `asia-southeast2 (Jakarta)` atau `asia-southeast1 (Singapore)`
- [ ] **Mode**: Pilih **"Start in production mode"** (JANGAN test mode!)
- [ ] **Security Rules**: Setup rules di atas segera setelah database dibuat
- [ ] **Test Rules**: Test dengan Firebase Console Simulator
- [ ] **Verify**: Pastikan rules sudah di-publish

---

## 🧪 Testing Security Rules

Setelah setup rules, test dengan Firebase Console:

1. Buka **Firestore Database** → **Rules**
2. Klik **"Rules Playground"** atau **"Simulator"**
3. Test scenarios:
   - User read products → Should ALLOW
   - User create product tanpa permission → Should DENY
   - Owner delete product → Should ALLOW
   - Staff delete product → Should DENY

---

## 💰 Cost Estimation untuk Production

### Firestore Pricing (Blaze Plan - Pay as you go):

**Free Tier (Spark Plan):**
- 1 GB storage free
- 50K reads/day free
- 20K writes/day free
- 20K deletes/day free

**Untuk warung kecil dengan 5-10 karyawan:**
- Estimated: < 10K reads/day
- Estimated: < 5K writes/day
- **Total: FREE** ✅

**Jika melebihi (Blaze Plan):**
- Storage: $0.18/GB/month
- Reads: $0.06 per 100K
- Writes: $0.18 per 100K
- Deletes: $0.02 per 100K

**Very affordable untuk warung kecil!**

---

## ⚠️ Important Notes

1. **JANGAN gunakan test mode** untuk production
2. **Setup security rules SEBELUM launch** ke Play Store
3. **Test rules** dengan simulator
4. **Monitor usage** di Firebase Console
5. **Backup rules** (copy ke file terpisah)

---

## 🔄 Migration Path (Jika sudah pakai test mode)

Jika sudah terlanjur pakai test mode:

1. **Export semua data** dari Firestore
2. **Update security rules** ke production rules
3. **Test** dengan user yang berbeda
4. **Monitor** untuk beberapa hari
5. **Jika aman**, lanjutkan ke production

---

## ✅ Summary

**Untuk aplikasi yang akan di-launch di Play Store:**

1. ✅ **Location**: `asia-southeast2 (Jakarta)`
2. ✅ **Mode**: **"Start in production mode"**
3. ✅ **Security Rules**: Setup rules yang strict (lihat di atas)
4. ✅ **Test**: Test rules sebelum launch

**JANGAN:**
- ❌ Test mode
- ❌ Location yang jauh (US, Europe)
- ❌ Rules yang terlalu permissive

---

**Last Updated:** 2024-12-28

