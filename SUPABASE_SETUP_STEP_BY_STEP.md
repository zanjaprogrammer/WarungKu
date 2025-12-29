# Supabase Setup - Step by Step Guide
## Panduan Lengkap Setup Supabase untuk WarungKu

---

## 📋 Prerequisites

- ✅ Akun GitHub atau Email
- ✅ Akses internet
- ✅ Project WarungKu sudah di branch `supabase-migration`

---

## 🚀 Phase 1: Create Supabase Project

### Step 1.1: Sign Up / Login
1. Buka browser dan kunjungi: **https://supabase.com**
2. Klik **"Start your project"** atau **"Sign In"**
3. Login dengan:
   - **GitHub** (recommended), atau
   - **Email** (Google, dll)

### Step 1.2: Create New Project
1. Setelah login, klik **"New Project"** atau **"Create a new project"**
2. Isi form:
   - **Name:** `WarungKu` (atau nama lain)
   - **Database Password:** 
     - Buat password yang kuat (minimal 12 karakter)
     - ⚠️ **SAVE PASSWORD INI!** (akan dipakai untuk koneksi database)
     - Contoh: `WarungKu2025!Secure`
   - **Region:** 
     - Pilih **"Southeast Asia (Singapore)"** atau **"Southeast Asia (Mumbai)"**
     - Untuk performa terbaik di Indonesia
   - **Pricing Plan:** 
     - Pilih **"Free"** untuk development/testing
     - Bisa upgrade ke Pro nanti jika diperlukan

3. Klik **"Create new project"**
4. ⏳ Tunggu 1-2 menit sampai project selesai dibuat

### Step 1.3: Get Project Credentials
1. Setelah project selesai, klik **"Settings"** di sidebar kiri
2. Di bawah **"PROJECT SETTINGS"**, klik **"API Keys"**
3. Anda akan melihat 2 tabs:
   - **"Publishable and secret API keys"** (tab baru - recommended)
   - **"Legacy anon, service_role API keys"** (untuk backward compatibility)
4. **Untuk setup baru, gunakan tab "Publishable and secret API keys":**
   - **Publishable key:** `sb_publishable_...` (ini yang dipakai di client app)
   - **Secret keys:** Klik **"+ New secret key"** jika belum ada (untuk server-side)
5. **ATAU gunakan tab "Legacy anon, service_role API keys"** (jika lebih familiar):
   - **Project URL:** `https://xxxxx.supabase.co`
   - **anon/public key:** `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
   - **service_role key:** `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` (⚠️ JANGAN SHARE!)
6. **SAVE INFORMASI INI:**
   - **Project URL:** `https://xxxxx.supabase.co`
   - **Publishable key** (atau **anon key** jika pakai legacy)
   - **Secret key** (atau **service_role key** jika pakai legacy)

---

## 🗄️ Phase 2: Database Setup

### Step 2.1: Open SQL Editor
1. Di sidebar kiri, klik **"SQL Editor"**
2. Klik **"New query"**

### Step 2.2: Create Database Schema
1. Copy script SQL di bawah ini
2. Paste ke SQL Editor
3. Klik **"Run"** (atau tekan `Ctrl/Cmd + Enter`)

```sql
-- ============================================
-- WARUNGKU DATABASE SCHEMA
-- ============================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- 1. WARUNGS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS warungs (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name TEXT NOT NULL,
  owner_id UUID,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- 2. USERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  email TEXT UNIQUE NOT NULL,
  name TEXT,
  role TEXT NOT NULL CHECK (role IN ('owner', 'manager', 'cashier', 'staff')),
  warung_id UUID REFERENCES warungs(id) ON DELETE CASCADE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  is_active BOOLEAN DEFAULT TRUE
);

-- Add foreign key constraint for warungs.owner_id (if not exists)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint 
    WHERE conname = 'fk_warungs_owner'
  ) THEN
    ALTER TABLE warungs 
    ADD CONSTRAINT fk_warungs_owner 
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL;
  END IF;
END $$;

-- ============================================
-- 3. PRODUCTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS products (
  id SERIAL PRIMARY KEY,
  warung_id UUID NOT NULL REFERENCES warungs(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  sell_price DECIMAL(15, 2) NOT NULL,
  buy_price DECIMAL(15, 2),
  current_stock INTEGER DEFAULT 0,
  min_stock INTEGER DEFAULT 0,
  sales_count INTEGER DEFAULT 0,
  is_favorite BOOLEAN DEFAULT FALSE,
  last_sold_timestamp BIGINT DEFAULT 0,
  barcode TEXT,
  synced BOOLEAN DEFAULT FALSE,
  last_synced_at BIGINT DEFAULT 0,
  cloud_id TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Index untuk performa query
CREATE INDEX IF NOT EXISTS idx_products_warung_id ON products(warung_id);
CREATE INDEX IF NOT EXISTS idx_products_barcode ON products(barcode);
CREATE INDEX IF NOT EXISTS idx_products_synced ON products(synced);

-- ============================================
-- 4. CASH_FLOWS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS cash_flows (
  id SERIAL PRIMARY KEY,
  warung_id UUID NOT NULL REFERENCES warungs(id) ON DELETE CASCADE,
  type TEXT NOT NULL CHECK (type IN ('IN', 'OUT')),
  amount DECIMAL(15, 2) NOT NULL,
  description TEXT,
  timestamp BIGINT NOT NULL,
  product_id INTEGER REFERENCES products(id) ON DELETE SET NULL,
  profit DECIMAL(15, 2),
  user_id UUID REFERENCES users(id) ON DELETE SET NULL,
  synced BOOLEAN DEFAULT FALSE,
  last_synced_at BIGINT DEFAULT 0,
  cloud_id TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Index untuk performa query
CREATE INDEX IF NOT EXISTS idx_cash_flows_warung_id ON cash_flows(warung_id);
CREATE INDEX IF NOT EXISTS idx_cash_flows_timestamp ON cash_flows(timestamp);
CREATE INDEX IF NOT EXISTS idx_cash_flows_type ON cash_flows(type);
CREATE INDEX IF NOT EXISTS idx_cash_flows_synced ON cash_flows(synced);

-- ============================================
-- 5. INVITES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS invites (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  warung_id UUID NOT NULL REFERENCES warungs(id) ON DELETE CASCADE,
  owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  email TEXT NOT NULL,
  role TEXT NOT NULL CHECK (role IN ('manager', 'cashier', 'staff')),
  status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'accepted', 'expired')),
  created_at BIGINT NOT NULL,
  expires_at BIGINT NOT NULL,
  accepted_at BIGINT,
  accepted_by UUID REFERENCES users(id) ON DELETE SET NULL
);

-- Index untuk performa query
CREATE INDEX IF NOT EXISTS idx_invites_warung_id ON invites(warung_id);
CREATE INDEX IF NOT EXISTS idx_invites_email ON invites(email);
CREATE INDEX IF NOT EXISTS idx_invites_status ON invites(status);

-- ============================================
-- 6. TRIGGERS FOR UPDATED_AT
-- ============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_warungs_updated_at
  BEFORE UPDATE ON warungs
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at
  BEFORE UPDATE ON users
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at
  BEFORE UPDATE ON products
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cash_flows_updated_at
  BEFORE UPDATE ON cash_flows
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- SUCCESS MESSAGE
-- ============================================
SELECT 'Database schema created successfully!' AS message;
```

### Step 2.3: Verify Tables Created
1. Di sidebar kiri, klik **"Table Editor"**
2. Pastikan 5 tables muncul:
   - ✅ `warungs`
   - ✅ `users`
   - ✅ `products`
   - ✅ `cash_flows`
   - ✅ `invites`

---

## 🔒 Phase 3: Row Level Security (RLS) Setup

### Step 3.1: Enable RLS
1. Kembali ke **"SQL Editor"**
2. Copy script di bawah ini
3. Paste dan **Run**

```sql
-- ============================================
-- ENABLE ROW LEVEL SECURITY
-- ============================================
ALTER TABLE warungs ENABLE ROW LEVEL SECURITY;
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE cash_flows ENABLE ROW LEVEL SECURITY;
ALTER TABLE invites ENABLE ROW LEVEL SECURITY;

SELECT 'RLS enabled successfully!' AS message;
```

### Step 3.2: Create RLS Policies
1. Copy script di bawah ini
2. Paste dan **Run**

```sql
-- ============================================
-- ROW LEVEL SECURITY POLICIES
-- ============================================

-- ============================================
-- WARUNGS POLICIES
-- ============================================
-- Users can read their own warung
CREATE POLICY "Users can read own warung"
ON warungs FOR SELECT
USING (
  id IN (
    SELECT warung_id FROM users 
    WHERE id = auth.uid()
  )
);

-- Owners can insert their own warung
CREATE POLICY "Owners can create warung"
ON warungs FOR INSERT
WITH CHECK (owner_id = auth.uid());

-- Owners can update their own warung
CREATE POLICY "Owners can update own warung"
ON warungs FOR UPDATE
USING (owner_id = auth.uid())
WITH CHECK (owner_id = auth.uid());

-- ============================================
-- USERS POLICIES
-- ============================================
-- Users can read users in their warung
CREATE POLICY "Users can read warung members"
ON users FOR SELECT
USING (
  warung_id IN (
    SELECT warung_id FROM users 
    WHERE id = auth.uid()
  )
);

-- Users can read their own data
CREATE POLICY "Users can read own data"
ON users FOR SELECT
USING (id = auth.uid());

-- Users can update their own data
CREATE POLICY "Users can update own data"
ON users FOR UPDATE
USING (id = auth.uid())
WITH CHECK (id = auth.uid());

-- Owners can insert new users (employees)
CREATE POLICY "Owners can create employees"
ON users FOR INSERT
WITH CHECK (
  EXISTS (
    SELECT 1 FROM users
    WHERE id = auth.uid()
    AND role = 'owner'
    AND warung_id = NEW.warung_id
  )
);

-- ============================================
-- PRODUCTS POLICIES
-- ============================================
-- Users can read products in their warung
CREATE POLICY "Users can read warung products"
ON products FOR SELECT
USING (
  warung_id IN (
    SELECT warung_id FROM users 
    WHERE id = auth.uid()
  )
);

-- Users with canAddProduct permission can insert
CREATE POLICY "Authorized users can create products"
ON products FOR INSERT
WITH CHECK (
  warung_id IN (
    SELECT warung_id FROM users 
    WHERE id = auth.uid()
    AND role IN ('owner', 'manager', 'cashier')
  )
);

-- Users with canAddProduct permission can update
CREATE POLICY "Authorized users can update products"
ON products FOR UPDATE
USING (
  warung_id IN (
    SELECT warung_id FROM users 
    WHERE id = auth.uid()
    AND role IN ('owner', 'manager', 'cashier')
  )
)
WITH CHECK (
  warung_id IN (
    SELECT warung_id FROM users 
    WHERE id = auth.uid()
    AND role IN ('owner', 'manager', 'cashier')
  )
);

-- Owners and managers can delete products
CREATE POLICY "Owners and managers can delete products"
ON products FOR DELETE
USING (
  warung_id IN (
    SELECT warung_id FROM users 
    WHERE id = auth.uid()
    AND role IN ('owner', 'manager')
  )
);

-- ============================================
-- CASH_FLOWS POLICIES
-- ============================================
-- Users can read cash flows in their warung
CREATE POLICY "Users can read warung cash flows"
ON cash_flows FOR SELECT
USING (
  warung_id IN (
    SELECT warung_id FROM users 
    WHERE id = auth.uid()
  )
);

-- Users with canSell permission can insert sales (IN)
CREATE POLICY "Authorized users can create cash flows"
ON cash_flows FOR INSERT
WITH CHECK (
  warung_id IN (
    SELECT warung_id FROM users 
    WHERE id = auth.uid()
    AND role IN ('owner', 'manager', 'cashier')
  )
);

-- Only owners and managers can update cash flows
CREATE POLICY "Owners and managers can update cash flows"
ON cash_flows FOR UPDATE
USING (
  warung_id IN (
    SELECT warung_id FROM users 
    WHERE id = auth.uid()
    AND role IN ('owner', 'manager')
  )
)
WITH CHECK (
  warung_id IN (
    SELECT warung_id FROM users 
    WHERE id = auth.uid()
    AND role IN ('owner', 'manager')
  )
);

-- Only owners can delete cash flows
CREATE POLICY "Only owners can delete cash flows"
ON cash_flows FOR DELETE
USING (
  warung_id IN (
    SELECT warung_id FROM users 
    WHERE id = auth.uid()
    AND role = 'owner'
  )
);

-- ============================================
-- INVITES POLICIES
-- ============================================
-- Users can read invites for their warung
CREATE POLICY "Users can read warung invites"
ON invites FOR SELECT
USING (
  warung_id IN (
    SELECT warung_id FROM users 
    WHERE id = auth.uid()
  )
);

-- Owners can create invites
CREATE POLICY "Owners can create invites"
ON invites FOR INSERT
WITH CHECK (
  owner_id = auth.uid()
  AND EXISTS (
    SELECT 1 FROM users
    WHERE id = auth.uid()
    AND role = 'owner'
  )
);

-- Owners can update invites
CREATE POLICY "Owners can update invites"
ON invites FOR UPDATE
USING (owner_id = auth.uid())
WITH CHECK (owner_id = auth.uid());

-- Users can update invites when accepting
CREATE POLICY "Users can accept invites"
ON invites FOR UPDATE
USING (email = (SELECT email FROM auth.users WHERE id = auth.uid()))
WITH CHECK (status = 'accepted');

SELECT 'RLS policies created successfully!' AS message;
```

### Step 3.3: Verify RLS
1. Kembali ke **"Table Editor"**
2. Klik salah satu table (misalnya `products`)
3. Scroll ke bawah, pastikan ada section **"Policies"**
4. Pastikan policies sudah terdaftar

---

## 🔐 Phase 4: Authentication Setup

### Step 4.1: Enable Email Auth
1. Di sidebar kiri, klik **"Authentication"**
2. Klik **"Providers"**
3. Pastikan **"Email"** provider sudah **Enabled**
4. (Optional) Configure email templates:
   - **Confirm signup**
   - **Magic link**
   - **Change email address**
   - **Reset password**

### Step 4.2: Configure Auth Settings
1. Di **"Authentication"**, klik **"Settings"**
2. Configure:
   - **Site URL:** `https://your-project.supabase.co` (default)
   - **Redirect URLs:** Tambahkan jika diperlukan
   - **Enable email confirmations:** 
     - ✅ **ON** untuk production
     - ❌ **OFF** untuk development/testing

---

## 📝 Phase 5: Create Config File

### Step 5.1: Create `supabase_config.properties`
1. Di project Android, buat file baru: `app/src/main/res/raw/supabase_config.properties`
2. Isi dengan (pilih salah satu format):

**Format 1: Menggunakan Publishable & Secret Keys (Baru):**
```properties
# Supabase Configuration
# DO NOT COMMIT THIS FILE TO GIT!

SUPABASE_URL=https://xxxxx.supabase.co
SUPABASE_PUBLISHABLE_KEY=sb_publishable_xxxxx...
SUPABASE_SECRET_KEY=sb_secret_xxxxx...
```

**Format 2: Menggunakan Legacy Keys (Anon & Service Role):**
```properties
# Supabase Configuration
# DO NOT COMMIT THIS FILE TO GIT!

SUPABASE_URL=https://xxxxx.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
SUPABASE_SERVICE_ROLE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Note:** Kedua format bisa digunakan, tapi **Publishable/Secret keys** adalah format baru yang recommended.

3. **⚠️ IMPORTANT:** Tambahkan ke `.gitignore`:
   ```
   app/src/main/res/raw/supabase_config.properties
   ```

### Step 5.2: Alternative - Use BuildConfig
Atau gunakan `BuildConfig` dengan menambahkan ke `app/build.gradle`:

**Format 1: Publishable & Secret Keys (Baru):**
```gradle
android {
    buildTypes {
        debug {
            buildConfigField "String", "SUPABASE_URL", "\"https://xxxxx.supabase.co\""
            buildConfigField "String", "SUPABASE_PUBLISHABLE_KEY", "\"sb_publishable_xxxxx...\""
        }
        release {
            buildConfigField "String", "SUPABASE_URL", "\"https://xxxxx.supabase.co\""
            buildConfigField "String", "SUPABASE_PUBLISHABLE_KEY", "\"sb_publishable_xxxxx...\""
        }
    }
}
```

**Format 2: Legacy Anon & Service Role Keys:**
```gradle
android {
    buildTypes {
        debug {
            buildConfigField "String", "SUPABASE_URL", "\"https://xxxxx.supabase.co\""
            buildConfigField "String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\""
        }
        release {
            buildConfigField "String", "SUPABASE_URL", "\"https://xxxxx.supabase.co\""
            buildConfigField "String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\""
        }
    }
}
```

---

## ✅ Phase 6: Verification Checklist

### Checklist Setup:
- [ ] Supabase project created
- [ ] Project URL & API keys saved
- [ ] Database password saved
- [ ] All 5 tables created (`warungs`, `users`, `products`, `cash_flows`, `invites`)
- [ ] RLS enabled on all tables
- [ ] RLS policies created
- [ ] Authentication enabled (Email)
- [ ] Config file created (dengan credentials)

### Test Connection:
1. Buka **"Table Editor"** di Supabase dashboard
2. Coba insert test data manual
3. Verify data muncul di table

---

## 🚨 Important Notes

### Security:
- ⚠️ **JANGAN commit** `supabase_config.properties` atau API keys ke Git
- ⚠️ **JANGAN share** `service_role` key (punya full access)
- ✅ Gunakan `anon` key untuk client-side
- ✅ `service_role` key hanya untuk server-side (jika ada)

### Database Password:
- ⚠️ **SAVE** database password yang dibuat saat project creation
- Password ini dipakai untuk koneksi langsung ke database (jika diperlukan)

### Region:
- Pilih region terdekat untuk performa terbaik
- Singapore/Mumbai recommended untuk Indonesia

---

## 📚 Next Steps

Setelah setup selesai:
1. ✅ Update Android dependencies (Supabase SDK)
2. ✅ Create `SupabaseAuthManager` class
3. ✅ Create `SupabaseSyncService` class
4. ✅ Update `LoginActivity` untuk Supabase
5. ✅ Test authentication flow
6. ✅ Test data sync

---

## 🆘 Troubleshooting

### Issue: "Table not found"
- **Solution:** Pastikan semua SQL scripts sudah di-run
- Check di "Table Editor" apakah tables sudah muncul

### Issue: "Permission denied"
- **Solution:** Pastikan RLS policies sudah dibuat
- Check apakah user sudah authenticated

### Issue: "Connection failed"
- **Solution:** 
  - Check Project URL
  - Check API key
  - Check internet connection

---

**Setup Complete!** 🎉

Siap untuk implementasi Supabase di Android app!

