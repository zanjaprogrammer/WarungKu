# Phase 3: Firestore Sync Service
## Cloud Sync untuk Multi-Device Support

## ✅ Status
- [x] Phase 1: Firebase Setup & Authentication (COMPLETE)
- [x] Phase 2: Authentication & Permission Checks (COMPLETE)
- [ ] Phase 3: Firestore Sync Service (IN PROGRESS)
- [ ] Phase 4: Invite Employee Flow

---

## 🎯 Tujuan Phase 3

1. **Sync Local → Cloud**: Upload data lokal ke Firestore
2. **Sync Cloud → Local**: Download data dari Firestore ke lokal
3. **Real-time Listeners**: Update otomatis saat data berubah di cloud
4. **Conflict Resolution**: Handle konflik saat sync
5. **Offline Support**: Data tetap tersimpan lokal, sync saat online

---

## 📋 Data yang Perlu Di-Sync

### 1. **Products** (Produk)
- Collection: `products/{warungId}/{productId}`
- Fields: name, sellPrice, buyPrice, currentStock, minStock, barcode, isFavorite, salesCount, lastSoldTimestamp
- Sync direction: Bidirectional (local ↔ cloud)

### 2. **Cash Flows** (Transaksi)
- Collection: `cash_flows/{warungId}/{cashFlowId}`
- Fields: type, amount, description, timestamp, productId, profit
- Sync direction: Local → Cloud (append-only, tidak bisa edit/delete)

### 3. **User Activities** (Activity Log)
- Collection: `user_activities/{warungId}/{activityId}`
- Fields: userId, action, description, timestamp
- Sync direction: Local → Cloud (append-only)

---

## 🏗️ Arsitektur Sync

### Strategy: Offline-First dengan Background Sync

```
┌─────────────────┐
│   Local Room    │ ← Primary source of truth
│    Database     │
└────────┬────────┘
         │
         │ (Background Sync)
         ▼
┌─────────────────┐
│  FirestoreSync  │ ← Sync service
│     Service     │
└────────┬────────┘
         │
         │ (Real-time)
         ▼
┌─────────────────┐
│   Firestore     │ ← Cloud backup
│    Database     │
└─────────────────┘
```

### Sync Flow:
1. **Write Operations**: Local first → Background sync to cloud
2. **Read Operations**: Local first → Check cloud for updates
3. **Real-time Updates**: Listen to cloud changes → Update local

---

## 🔧 Implementation Components

### 1. **FirestoreSyncService**
- Background service untuk sync
- Handle sync queue
- Retry logic untuk failed syncs
- Conflict resolution

### 2. **SyncManager**
- Singleton untuk manage sync state
- Track sync status (syncing, synced, error)
- Queue operations untuk offline mode

### 3. **Data Mappers**
- Convert Room entities → Firestore documents
- Convert Firestore documents → Room entities
- Handle field mapping

### 4. **Sync Listeners**
- Real-time listeners untuk cloud changes
- Update local database saat cloud berubah
- Handle conflicts

---

## 📝 Implementation Steps

### Step 1: Update Database Entities
- Add `synced` field (boolean) untuk track sync status
- Add `lastSyncedAt` field (timestamp)
- Add `cloudId` field (Firestore document ID)

### Step 2: Create FirestoreSyncService
- Background service untuk sync
- Sync products, cash flows, activities
- Handle errors dan retries

### Step 3: Create SyncManager
- Manage sync state
- Queue operations
- Trigger sync on network available

### Step 4: Update DataRepository
- Add sync methods
- Auto-sync setelah insert/update/delete
- Check sync status

### Step 5: Add Real-time Listeners
- Listen to Firestore changes
- Update local database
- Handle conflicts

### Step 6: Add UI Indicators
- Show sync status
- Show sync progress
- Show sync errors

---

## 🔄 Sync Strategy Details

### Products Sync:
- **Create**: Local → Cloud (create document)
- **Update**: Local → Cloud (update document)
- **Delete**: Local → Cloud (delete document)
- **Read**: Cloud → Local (if local not found or outdated)

### Cash Flows Sync:
- **Create**: Local → Cloud (append, no update/delete)
- **Read**: Cloud → Local (for initial sync)

### User Activities Sync:
- **Create**: Local → Cloud (append, no update/delete)
- **Read**: Cloud → Local (for initial sync)

---

## ⚠️ Conflict Resolution

### Strategy: Last-Write-Wins (with timestamp)
1. Compare `lastSyncedAt` timestamps
2. Keep version with latest timestamp
3. Log conflict for manual review (optional)

### Alternative: Manual Merge (for future)
- Show conflict dialog
- Let user choose which version to keep
- Merge changes if possible

---

## 🧪 Testing Strategy

1. **Test Offline Mode**:
   - Create/update/delete products offline
   - Verify data saved locally
   - Go online → verify sync to cloud

2. **Test Real-time Updates**:
   - Change data in Firestore Console
   - Verify local database updated automatically

3. **Test Conflict Resolution**:
   - Modify same product on 2 devices
   - Verify last-write-wins works

4. **Test Network Issues**:
   - Simulate network errors
   - Verify retry logic works
   - Verify queue operations

---

## 📋 Implementation Order

1. **Update Product Entity** - Add sync fields
2. **Update CashFlow Entity** - Add sync fields
3. **Create FirestoreSyncService** - Background sync service
4. **Create SyncManager** - Manage sync state
5. **Update DataRepository** - Add sync methods
6. **Add Real-time Listeners** - Listen to cloud changes
7. **Add UI Indicators** - Show sync status

---

## ✅ Success Criteria

Phase 3 complete jika:
- ✅ Products sync local ↔ cloud
- ✅ Cash flows sync local → cloud
- ✅ Real-time updates work
- ✅ Offline mode works (queue operations)
- ✅ Conflict resolution works
- ✅ UI shows sync status

---

**Ready untuk implementasi!** 🚀

