-- ============================================
-- WARUNGKU DATABASE SCHEMA (IDEMPOTENT VERSION)
-- ============================================
-- Script ini bisa dijalankan berulang kali tanpa error
-- Menggunakan IF NOT EXISTS dan DROP IF EXISTS
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

-- Add foreign key constraint for warungs.owner_id (idempotent)
DO $$
BEGIN
  -- Drop constraint jika sudah ada
  IF EXISTS (
    SELECT 1 FROM pg_constraint 
    WHERE conname = 'fk_warungs_owner'
  ) THEN
    ALTER TABLE warungs DROP CONSTRAINT fk_warungs_owner;
  END IF;
  
  -- Create constraint baru
  ALTER TABLE warungs 
  ADD CONSTRAINT fk_warungs_owner 
  FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL;
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

-- Index untuk performa query (idempotent)
DROP INDEX IF EXISTS idx_products_warung_id;
CREATE INDEX idx_products_warung_id ON products(warung_id);

DROP INDEX IF EXISTS idx_products_barcode;
CREATE INDEX idx_products_barcode ON products(barcode);

DROP INDEX IF EXISTS idx_products_synced;
CREATE INDEX idx_products_synced ON products(synced);

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

-- Index untuk performa query (idempotent)
DROP INDEX IF EXISTS idx_cash_flows_warung_id;
CREATE INDEX idx_cash_flows_warung_id ON cash_flows(warung_id);

DROP INDEX IF EXISTS idx_cash_flows_timestamp;
CREATE INDEX idx_cash_flows_timestamp ON cash_flows(timestamp);

DROP INDEX IF EXISTS idx_cash_flows_type;
CREATE INDEX idx_cash_flows_type ON cash_flows(type);

DROP INDEX IF EXISTS idx_cash_flows_synced;
CREATE INDEX idx_cash_flows_synced ON cash_flows(synced);

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

-- Index untuk performa query (idempotent)
DROP INDEX IF EXISTS idx_invites_warung_id;
CREATE INDEX idx_invites_warung_id ON invites(warung_id);

DROP INDEX IF EXISTS idx_invites_email;
CREATE INDEX idx_invites_email ON invites(email);

DROP INDEX IF EXISTS idx_invites_status;
CREATE INDEX idx_invites_status ON invites(status);

-- ============================================
-- 6. TRIGGERS FOR UPDATED_AT (idempotent)
-- ============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop triggers jika sudah ada
DROP TRIGGER IF EXISTS update_warungs_updated_at ON warungs;
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
DROP TRIGGER IF EXISTS update_products_updated_at ON products;
DROP TRIGGER IF EXISTS update_cash_flows_updated_at ON cash_flows;

-- Create triggers
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
SELECT 'Database schema created/updated successfully!' AS message;

