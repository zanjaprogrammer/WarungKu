# Supabase Migration Plan
## Migrasi dari Firebase ke Supabase

## 🎯 Tujuan
Migrasi backend dari Firebase ke Supabase untuk:
- ✅ Open-source alternative
- ✅ Self-hosted option (jika diperlukan)
- ✅ PostgreSQL database (lebih powerful dari Firestore)
- ✅ Built-in authentication
- ✅ Real-time subscriptions
- ✅ Storage untuk file

---

## 📋 Current Firebase Implementation

### 1. **Firebase Authentication**
- Email/Password authentication
- User session management
- Auto-complete registration

### 2. **Firestore Database**
- Collections: `users`, `warungs`, `products`, `cash_flows`, `invites`
- Subcollections: `products/{warungId}/items/`, `cash_flows/{warungId}/transactions/`
- Security rules untuk RBAC

### 3. **Firestore Sync Service**
- Background sync service
- Local → Cloud sync
- Batch operations

---

## 🏗️ Supabase Architecture

### 1. **Supabase Auth**
- Email/Password authentication
- Session management
- Row Level Security (RLS) untuk permissions

### 2. **PostgreSQL Database**
- Tables: `users`, `warungs`, `products`, `cash_flows`, `invites`
- Foreign keys & relationships
- Triggers untuk auto-update timestamps

### 3. **Supabase Realtime**
- Real-time subscriptions
- Auto-sync cloud → local
- Conflict resolution

---

## 📝 Migration Steps

### Phase 1: Setup Supabase
- [ ] Create Supabase project
- [ ] Setup PostgreSQL database
- [ ] Create tables (users, warungs, products, cash_flows, invites)
- [ ] Setup Row Level Security (RLS) policies
- [ ] Setup authentication

### Phase 2: Update Dependencies
- [ ] Remove Firebase dependencies
- [ ] Add Supabase dependencies
- [ ] Update `build.gradle`

### Phase 3: Update Code
- [ ] Replace `AuthManager` → `SupabaseAuthManager`
- [ ] Replace `FirestoreSyncService` → `SupabaseSyncService`
- [ ] Update `LoginActivity` untuk Supabase Auth
- [ ] Update `ManageEmployeesActivity` untuk Supabase
- [ ] Update security rules → RLS policies

### Phase 4: Database Migration
- [ ] Create migration scripts
- [ ] Migrate existing data (jika ada)
- [ ] Test data integrity

### Phase 5: Testing
- [ ] Test authentication
- [ ] Test data sync
- [ ] Test permissions
- [ ] Test invite system

---

## 🔧 Supabase Setup

### 1. **Create Supabase Project**
- Go to: https://supabase.com
- Create new project
- Note: Project URL & API Key

### 2. **Database Schema**
```sql
-- Users table
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  email TEXT UNIQUE NOT NULL,
  name TEXT,
  role TEXT NOT NULL CHECK (role IN ('owner', 'manager', 'cashier', 'staff')),
  warung_id UUID REFERENCES warungs(id),
  created_at TIMESTAMP DEFAULT NOW(),
  is_active BOOLEAN DEFAULT TRUE
);

-- Warungs table
CREATE TABLE warungs (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name TEXT NOT NULL,
  owner_id UUID REFERENCES users(id),
  created_at TIMESTAMP DEFAULT NOW()
);

-- Products table
CREATE TABLE products (
  id SERIAL PRIMARY KEY,
  warung_id UUID REFERENCES warungs(id) NOT NULL,
  name TEXT NOT NULL,
  sell_price DECIMAL NOT NULL,
  buy_price DECIMAL,
  current_stock INTEGER DEFAULT 0,
  min_stock INTEGER DEFAULT 0,
  sales_count INTEGER DEFAULT 0,
  is_favorite BOOLEAN DEFAULT FALSE,
  last_sold_timestamp BIGINT DEFAULT 0,
  barcode TEXT,
  synced BOOLEAN DEFAULT FALSE,
  last_synced_at BIGINT DEFAULT 0,
  cloud_id TEXT,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- Cash flows table
CREATE TABLE cash_flows (
  id SERIAL PRIMARY KEY,
  warung_id UUID REFERENCES warungs(id) NOT NULL,
  type TEXT NOT NULL CHECK (type IN ('IN', 'OUT')),
  amount DECIMAL NOT NULL,
  description TEXT,
  timestamp BIGINT NOT NULL,
  product_id INTEGER REFERENCES products(id),
  profit DECIMAL,
  user_id UUID REFERENCES users(id),
  synced BOOLEAN DEFAULT FALSE,
  last_synced_at BIGINT DEFAULT 0,
  cloud_id TEXT,
  created_at TIMESTAMP DEFAULT NOW()
);

-- Invites table
CREATE TABLE invites (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  warung_id UUID REFERENCES warungs(id) NOT NULL,
  owner_id UUID REFERENCES users(id) NOT NULL,
  email TEXT NOT NULL,
  role TEXT NOT NULL CHECK (role IN ('manager', 'cashier', 'staff')),
  status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'accepted', 'expired')),
  created_at BIGINT NOT NULL,
  expires_at BIGINT NOT NULL,
  accepted_at BIGINT,
  accepted_by UUID REFERENCES users(id)
);
```

### 3. **Row Level Security (RLS)**
```sql
-- Enable RLS
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE warungs ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE cash_flows ENABLE ROW LEVEL SECURITY;
ALTER TABLE invites ENABLE ROW LEVEL SECURITY;

-- Policies (examples)
CREATE POLICY "Users can read own data" ON users
  FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Users can read products in their warung" ON products
  FOR SELECT USING (
    warung_id IN (
      SELECT warung_id FROM users WHERE id = auth.uid()
    )
  );
```

---

## 📦 Supabase Dependencies

### Add to `app/build.gradle`:
```gradle
dependencies {
    // Supabase
    implementation 'io.github.jan-tennert.supabase:postgrest-kt:2.0.0'
    implementation 'io.github.jan-tennert.supabase:realtime-kt:2.0.0'
    implementation 'io.github.jan-tennert.supabase:storage-kt:2.0.0'
    implementation 'io.github.jan-tennert.supabase:auth-kt:2.0.0'
    implementation 'io.github.jan-tennert.supabase:functions-kt:2.0.0'
    
    // Kotlin Coroutines (if using Kotlin)
    // Or use Java equivalent
}
```

---

## 🔄 Code Changes Required

### 1. **AuthManager → SupabaseAuthManager**
- Replace Firebase Auth dengan Supabase Auth
- Update session management
- Update user data fetching

### 2. **FirestoreSyncService → SupabaseSyncService**
- Replace Firestore dengan Supabase PostgREST
- Update sync logic
- Use Supabase Realtime untuk cloud → local sync

### 3. **Security Rules → RLS Policies**
- Convert Firestore rules ke PostgreSQL RLS policies
- Test permissions untuk setiap role

---

## ✅ Success Criteria

Migration complete jika:
- ✅ Authentication bekerja dengan Supabase
- ✅ Data sync bekerja (local ↔ Supabase)
- ✅ Permissions bekerja dengan RLS
- ✅ Invite system bekerja
- ✅ Real-time updates bekerja

---

## 📝 Notes

- **Backup Firebase data** sebelum migration
- **Test thoroughly** sebelum production
- **Keep Firebase code** sebagai backup (di branch `v1.0.0-firebase`)
- **Gradual migration** - bisa run both systems temporarily

---

**Current Branch: supabase-migration** 🚀

