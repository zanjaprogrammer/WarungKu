# Multi-User Implementation Plan
## Sistem Multi-User untuk Warung dengan Karyawan

## 🎯 Tujuan
Mengimplementasikan sistem multi-user yang memungkinkan pemilik warung untuk:
- Menambahkan karyawan sebagai user
- Memberikan akses terbatas sesuai role
- Melacak aktivitas setiap user
- Sinkronisasi data real-time antar device

---

## 📋 Fitur Utama

### 1. **Authentication & User Management**
- Login/Register untuk owner
- Invite karyawan (via email/phone)
- User roles: Owner, Manager, Cashier, Staff
- Profile management
- Logout

### 2. **Role-Based Access Control (RBAC)**
- **Owner**: Full access (semua fitur)
- **Manager**: Bisa lihat laporan, manage produk, tapi tidak bisa hapus data penting
- **Cashier**: Hanya bisa jual barang, tidak bisa edit produk/harga
- **Staff**: Hanya bisa lihat stok, tidak bisa jual

### 3. **Cloud Sync & Real-time Data**
- Sinkronisasi data real-time antar device
- Conflict resolution (last-write-wins atau manual merge)
- Offline-first approach (data tetap tersimpan lokal, sync saat online)

### 4. **Activity Tracking**
- Log setiap transaksi dengan user ID
- History per user (siapa yang jual apa, kapan)
- Audit trail untuk transaksi penting

### 5. **Multi-Device Support**
- Owner bisa akses dari beberapa device
- Karyawan bisa login dari device mereka sendiri
- Data tersinkronisasi otomatis

---

## 🏗️ Arsitektur Teknis

### Backend: Firebase (Recommended)
**Kenapa Firebase?**
- ✅ Gratis untuk penggunaan kecil (Spark Plan cukup untuk warung kecil)
- ✅ Real-time database (Firestore)
- ✅ Authentication built-in
- ✅ Offline persistence
- ✅ Easy setup, no server needed
- ✅ Scalable

### Database Structure

#### Firestore Collections:

```
users/
  {userId}/
    email: string
    name: string
    role: "owner" | "manager" | "cashier" | "staff"
    warungId: string
    createdAt: timestamp
    isActive: boolean

warungs/
  {warungId}/
    name: string
    ownerId: string
    createdAt: timestamp
    settings: {
      currency: string
      timezone: string
    }

products/
  {warungId}/
    {productId}/
      name: string
      sellPrice: number
      buyPrice: number
      currentStock: number
      minStock: number
      barcode: string
      isFavorite: boolean
      salesCount: number
      lastSoldTimestamp: timestamp
      createdBy: userId
      updatedBy: userId
      createdAt: timestamp
      updatedAt: timestamp

transactions/
  {warungId}/
    {transactionId}/
      type: "IN" | "OUT"
      amount: number
      description: string
      timestamp: timestamp
      userId: string  // Siapa yang melakukan transaksi
      productId: number
      profit: number
      paymentMethod: "cash" | "qris"
      items: [
        {
          productId: number
          quantity: number
          price: number
        }
      ]

cash_flows/
  {warungId}/
    {cashFlowId}/
      type: "IN" | "OUT"
      amount: number
      description: string
      timestamp: timestamp
      userId: string
      productId: number
      profit: number

user_activities/
  {warungId}/
    {activityId}/
      userId: string
      action: "login" | "logout" | "sell" | "add_product" | "edit_product" | "delete_product" | "restock"
      details: string
      timestamp: timestamp
```

### Local Database (Room)
Tetap digunakan untuk:
- Offline-first storage
- Fast local queries
- Offline operations

**Sync Strategy:**
1. Data selalu ditulis ke Room database lokal dulu
2. Background sync ke Firestore saat online
3. Pull changes dari Firestore saat app start/resume
4. Conflict resolution: Last-write-wins (dengan timestamp)

---

## 🔐 Authentication Flow

### 1. Owner Registration
```
1. Owner buka app pertama kali
2. Pilih "Daftar sebagai Pemilik"
3. Input: Email, Password, Nama Warung
4. Firebase Auth: createUserWithEmailAndPassword
5. Firestore: Create warung document + user document
6. Set role: "owner"
7. Login otomatis
```

### 2. Owner Login
```
1. Input: Email, Password
2. Firebase Auth: signInWithEmailAndPassword
3. Fetch user data dari Firestore
4. Set local user session
5. Sync data dari Firestore
```

### 3. Invite Employee
```
1. Owner: Settings → Manage Employees → Invite
2. Input: Email/Phone, Role
3. Generate invite link/code
4. Send via SMS/Email (Firebase Cloud Messaging atau manual)
5. Employee: Buka link/input code
6. Employee: Register dengan email/phone
7. Auto-assign ke warung owner
8. Set role sesuai yang di-invite
```

### 4. Employee Login
```
1. Input: Email, Password
2. Firebase Auth: signInWithEmailAndPassword
3. Check: Apakah user punya warungId?
4. Fetch warung data
5. Set local user session dengan role
6. Sync data sesuai permission
```

---

## 🛡️ Permission Matrix

| Fitur | Owner | Manager | Cashier | Staff |
|-------|-------|---------|---------|-------|
| **Jual Barang** | ✅ | ✅ | ✅ | ❌ |
| **Tambah Produk** | ✅ | ✅ | ❌ | ❌ |
| **Edit Produk** | ✅ | ✅ | ❌ | ❌ |
| **Hapus Produk** | ✅ | ❌ | ❌ | ❌ |
| **Restock** | ✅ | ✅ | ❌ | ❌ |
| **Lihat Stok** | ✅ | ✅ | ✅ | ✅ |
| **Lihat Laporan** | ✅ | ✅ | ❌ | ❌ |
| **Export/Import** | ✅ | ✅ | ❌ | ❌ |
| **Manage Karyawan** | ✅ | ❌ | ❌ | ❌ |
| **Ubah Harga** | ✅ | ✅ | ❌ | ❌ |
| **Tambah Pengeluaran** | ✅ | ✅ | ❌ | ❌ |
| **Backup/Restore** | ✅ | ❌ | ❌ | ❌ |
| **Settings** | ✅ | ❌ | ❌ | ❌ |

---

## 📱 UI/UX Changes

### 1. **Login Screen** (New)
- Email/Password input
- "Daftar sebagai Pemilik" button
- "Masuk sebagai Karyawan" button
- Forgot password

### 2. **Settings Screen** (New/Enhanced)
- User profile
- Manage Employees section (Owner only)
  - List karyawan
  - Invite karyawan
  - Edit role
  - Remove karyawan
- Logout

### 3. **Activity Indicator**
- Show current logged-in user di toolbar
- Badge untuk role
- Quick switch user (Owner only)

### 4. **Permission-based UI**
- Hide/show buttons berdasarkan role
- Disable actions yang tidak diizinkan
- Show message jika tidak punya permission

---

## 🔄 Sync Mechanism

### Sync Service Architecture

```java
public class SyncService {
    // Sync local → Cloud
    public void syncToCloud() {
        // 1. Get unsynced data dari Room
        // 2. Upload ke Firestore
        // 3. Mark as synced
    }
    
    // Sync Cloud → Local
    public void syncFromCloud() {
        // 1. Fetch latest data dari Firestore
        // 2. Compare dengan local
        // 3. Merge/update local database
        // 4. Handle conflicts
    }
    
    // Real-time listener
    public void setupRealtimeListener() {
        // Listen to Firestore changes
        // Auto-update local database
    }
}
```

### Conflict Resolution Strategy

**Option 1: Last-Write-Wins (Recommended untuk MVP)**
- Gunakan timestamp untuk menentukan winner
- Simple, no user interaction needed

**Option 2: Manual Merge (Future)**
- Show conflict dialog
- User pilih mana yang benar
- More complex, better for critical data

---

## 📦 Implementation Phases

### Phase 1: Foundation (Week 1-2)
- [ ] Setup Firebase project
- [ ] Add Firebase dependencies
- [ ] Create Firestore database structure
- [ ] Implement Firebase Authentication
- [ ] Create Login/Register screens
- [ ] User session management

### Phase 2: Basic Multi-User (Week 3-4)
- [ ] Role-based access control
- [ ] Permission checks di semua activities
- [ ] User profile management
- [ ] Invite employee flow
- [ ] Activity tracking (basic)

### Phase 3: Cloud Sync (Week 5-6)
- [ ] Sync service implementation
- [ ] Local → Cloud sync
- [ ] Cloud → Local sync
- [ ] Real-time listeners
- [ ] Conflict resolution
- [ ] Offline queue management

### Phase 4: Advanced Features (Week 7-8)
- [ ] Activity log UI
- [ ] User management UI
- [ ] Multi-device testing
- [ ] Performance optimization
- [ ] Error handling & retry logic

---

## 🔧 Technical Details

### Dependencies to Add

```gradle
// Firebase
implementation platform('com.google.firebase:firebase-bom:32.7.0')
implementation 'com.google.firebase:firebase-firestore'
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-analytics'

// Optional: Firebase Cloud Messaging untuk notifications
implementation 'com.google.firebase:firebase-messaging'
```

### New Classes/Components

1. **AuthManager.java**
   - Handle login/logout
   - User session management
   - Token refresh

2. **FirestoreSyncService.java**
   - Sync local → cloud
   - Sync cloud → local
   - Real-time listeners

3. **PermissionManager.java**
   - Check user permissions
   - Role validation

4. **UserRepository.java**
   - User data management
   - Warung data management

5. **LoginActivity.java**
   - Login/Register UI

6. **SettingsActivity.java** (Enhanced)
   - User management
   - Employee management

### Database Schema Changes

**Local (Room):**
- Add `userId` field to all entities
- Add `warungId` field to all entities
- Add `synced` boolean field (untuk track sync status)
- Add `lastSyncedAt` timestamp

**Cloud (Firestore):**
- Collections structure seperti di atas

---

## 🚨 Challenges & Solutions

### Challenge 1: Data Migration
**Problem:** Existing users punya data lokal, perlu migrate ke cloud
**Solution:**
- One-time migration script
- Upload existing data saat first login
- Show progress indicator

### Challenge 2: Offline Operations
**Problem:** Karyawan perlu bisa jual meski offline
**Solution:**
- Offline-first approach (sudah ada)
- Queue untuk sync saat online
- Show sync status indicator

### Challenge 3: Conflict Resolution
**Problem:** Multiple users edit same data
**Solution:**
- Last-write-wins untuk MVP
- Timestamp-based resolution
- Future: Manual merge untuk critical data

### Challenge 4: Security
**Problem:** Prevent unauthorized access
**Solution:**
- Firestore Security Rules
- Role-based access di Firestore
- Validate permissions di app level juga

---

## 📊 Cost Estimation

### Firebase Pricing (Spark Plan - Free)
- **Authentication**: 50,000 MAU free
- **Firestore**: 
  - 1 GB storage free
  - 50K reads/day free
  - 20K writes/day free
  - 20K deletes/day free

**Untuk warung kecil dengan 5-10 karyawan:**
- Estimated: < 10K reads/day
- Estimated: < 5K writes/day
- **Total: FREE** ✅

### Jika melebihi (Blaze Plan - Pay as you go):
- $0.06 per 100K document reads
- $0.18 per 100K document writes
- Very affordable untuk warung kecil

---

## ✅ Success Criteria

1. Owner bisa invite karyawan
2. Karyawan bisa login dan akses sesuai role
3. Data tersinkronisasi real-time antar device
4. Offline operations tetap berfungsi
5. Activity tracking berfungsi
6. No data loss saat sync
7. Performance tetap baik (< 2s untuk sync)

---

## 🎯 Next Steps

1. **Review & Approval**: Review plan ini dengan stakeholder
2. **Firebase Setup**: Create Firebase project, enable services
3. **Phase 1 Start**: Implement authentication & basic structure
4. **Testing**: Test dengan multiple users/devices
5. **Iterate**: Improve based on feedback

---

## 📝 Notes

- **MVP Approach**: Start dengan basic features, iterate
- **Backward Compatibility**: Existing users perlu migrate data
- **User Education**: Perlu tutorial untuk owner tentang cara invite karyawan
- **Support**: Consider adding in-app help/FAQ

---

**Last Updated:** 2024-12-28
**Status:** Planning Phase
**Estimated Timeline:** 8 weeks untuk full implementation

