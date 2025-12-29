-- ============================================
-- ROW LEVEL SECURITY (RLS) SETUP (IDEMPOTENT)
-- ============================================
-- Script ini bisa dijalankan berulang kali tanpa error
-- Menggunakan DROP POLICY IF EXISTS sebelum CREATE
-- ============================================

-- ============================================
-- ENABLE ROW LEVEL SECURITY
-- ============================================
ALTER TABLE warungs ENABLE ROW LEVEL SECURITY;
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE cash_flows ENABLE ROW LEVEL SECURITY;
ALTER TABLE invites ENABLE ROW LEVEL SECURITY;

-- ============================================
-- DROP EXISTING POLICIES (IF ANY)
-- ============================================
-- Warungs policies
DROP POLICY IF EXISTS "Users can read own warung" ON warungs;
DROP POLICY IF EXISTS "Owners can create warung" ON warungs;
DROP POLICY IF EXISTS "Owners can update own warung" ON warungs;

-- Users policies
DROP POLICY IF EXISTS "Users can read warung members" ON users;
DROP POLICY IF EXISTS "Users can read own data" ON users;
DROP POLICY IF EXISTS "Users can update own data" ON users;
DROP POLICY IF EXISTS "Owners can create employees" ON users;

-- Products policies
DROP POLICY IF EXISTS "Users can read warung products" ON products;
DROP POLICY IF EXISTS "Authorized users can create products" ON products;
DROP POLICY IF EXISTS "Authorized users can update products" ON products;
DROP POLICY IF EXISTS "Owners and managers can delete products" ON products;

-- Cash flows policies
DROP POLICY IF EXISTS "Users can read warung cash flows" ON cash_flows;
DROP POLICY IF EXISTS "Authorized users can create cash flows" ON cash_flows;
DROP POLICY IF EXISTS "Owners and managers can update cash flows" ON cash_flows;
DROP POLICY IF EXISTS "Only owners can delete cash flows" ON cash_flows;

-- Invites policies
DROP POLICY IF EXISTS "Users can read warung invites" ON invites;
DROP POLICY IF EXISTS "Owners can create invites" ON invites;
DROP POLICY IF EXISTS "Owners can update invites" ON invites;
DROP POLICY IF EXISTS "Users can accept invites" ON invites;

-- ============================================
-- CREATE RLS POLICIES
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
    SELECT 1 FROM users u
    WHERE u.id = auth.uid()
    AND u.role = 'owner'
    AND u.warung_id = (SELECT warung_id FROM users WHERE id = auth.uid() AND role = 'owner')
  )
  AND NEW.warung_id = (SELECT warung_id FROM users WHERE id = auth.uid() AND role = 'owner')
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
    SELECT 1 FROM users u
    WHERE u.id = auth.uid()
    AND u.role = 'owner'
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
USING (
  email = (
    SELECT email::text FROM auth.users 
    WHERE id = auth.uid()
  )
)
WITH CHECK (status = 'accepted');

-- ============================================
-- SUCCESS MESSAGE
-- ============================================
SELECT 'RLS enabled and policies created successfully!' AS message;

