-- ============================================
-- COMPLETE FIX FOR INFINITE RECURSION
-- ============================================
-- This script fixes ALL infinite recursion issues in RLS policies
-- Run this script in Supabase SQL Editor
-- ============================================

-- ============================================
-- STEP 1: DROP ALL EXISTING POLICIES ON USERS TABLE
-- ============================================
-- Drop all policies to ensure clean state
DROP POLICY IF EXISTS "Users can read warung members" ON users;
DROP POLICY IF EXISTS "Users can read own data" ON users;
DROP POLICY IF EXISTS "Owners can create employees" ON users;
DROP POLICY IF EXISTS "Users can create own record" ON users;
DROP POLICY IF EXISTS "Users can update own data" ON users;

-- Drop any other policies that might exist from previous setup
DROP POLICY IF EXISTS "Users can read own data" ON users;
DROP POLICY IF EXISTS "Users can update own data" ON users;

-- ============================================
-- STEP 2: CREATE SIMPLIFIED POLICIES (NO RECURSION)
-- ============================================

-- Policy 1: Users can read their own data
-- This is safe - no recursion because it only checks auth.uid()
CREATE POLICY "Users can read own data"
ON users FOR SELECT
USING (id = auth.uid());

-- Policy 2: Users can read other users in same warung
-- SIMPLIFIED: Only allow reading own data to avoid recursion and type issues
-- Warung members reading can be added later via a database function
CREATE POLICY "Users can read warung members"
ON users FOR SELECT
USING (
  -- For now, only allow reading own data to avoid recursion
  -- Warung members can be read via a separate API endpoint or function
  id = auth.uid()
);

-- Policy 3: Users can insert their own record (for registration)
CREATE POLICY "Users can create own record"
ON users FOR INSERT
WITH CHECK (id = auth.uid());

-- Policy 4: Users can update their own data
CREATE POLICY "Users can update own data"
ON users FOR UPDATE
USING (id = auth.uid())
WITH CHECK (id = auth.uid());

-- Policy 5: Owners can create employees
-- This is tricky - we need to check if user is owner without recursion
-- Solution: Use a function or check via auth.users metadata
-- For now, allow if user is creating their own record OR
-- if we can verify owner status without querying users table
CREATE POLICY "Owners can create employees"
ON users FOR INSERT
WITH CHECK (
  -- Allow self-insert (for registration)
  id = auth.uid()
  -- Note: Employee creation by owner will need to be handled
  -- via a database function or service role key
);

-- ============================================
-- STEP 3: VERIFY POLICIES
-- ============================================
SELECT 
  schemaname,
  tablename,
  policyname,
  cmd,
  qual,
  with_check
FROM pg_policies
WHERE tablename = 'users'
ORDER BY policyname;

-- ============================================
-- NOTES
-- ============================================
-- 1. "Users can read warung members" is simplified to only allow reading own data
--    This prevents infinite recursion
-- 2. To read warung members, you can:
--    a) Use a database function with SECURITY DEFINER
--    b) Use service role key for admin operations
--    c) Add warung_id to auth.users metadata and query from there
-- 3. For employee creation by owner, consider using a database function
--    or service role key instead of RLS policy

