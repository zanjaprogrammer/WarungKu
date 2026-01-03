-- ============================================
-- FIX INFINITE RECURSION IN RLS POLICIES
-- ============================================
-- Problem: Policy untuk users table menyebabkan infinite recursion
-- Solution: Simplify policies untuk INSERT user baru (owner registration)
-- ============================================

-- ============================================
-- DROP PROBLEMATIC POLICIES
-- ============================================
DROP POLICY IF EXISTS "Users can read warung members" ON users;
DROP POLICY IF EXISTS "Owners can create employees" ON users;

-- ============================================
-- FIXED POLICIES FOR USERS TABLE
-- ============================================

-- Users can read users in their warung
-- FIXED: Completely avoid recursion by using a simpler approach
-- For now, allow users to read their own data only
-- Warung members reading can be added later if needed
CREATE POLICY "Users can read warung members"
ON users FOR SELECT
USING (
  -- User can always read their own data (no recursion)
  id = auth.uid()
);

-- Users can read their own data (keep this, it's safe)
-- Already exists, no need to recreate

-- FIXED: Allow users to insert their own user record during registration
-- This is needed for owner registration (first user in warung)
CREATE POLICY "Users can create own record"
ON users FOR INSERT
WITH CHECK (id = auth.uid());

-- Owners can insert new users (employees)
-- FIXED: Allow self-insert for registration, and owner insert for employees
CREATE POLICY "Owners can create employees"
ON users FOR INSERT
WITH CHECK (
  -- Allow user to create their own record (for owner registration)
  id = auth.uid()
  OR
  -- Allow owner to create employee records
  -- Use EXISTS with LIMIT to prevent infinite recursion
  EXISTS (
    SELECT 1 FROM users u
    WHERE u.id = auth.uid()
    AND u.role = 'owner'
    LIMIT 1
  )
);

-- ============================================
-- VERIFY POLICIES
-- ============================================
SELECT 
  schemaname,
  tablename,
  policyname,
  permissive,
  roles,
  cmd,
  qual,
  with_check
FROM pg_policies
WHERE tablename = 'users'
ORDER BY policyname;

