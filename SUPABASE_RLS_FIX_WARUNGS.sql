-- ============================================
-- FIX RLS POLICY FOR WARUNGS TABLE
-- ============================================
-- Problem: "new row violates row-level security policy for table warungs"
-- Solution: Fix policy untuk allow owner create warung saat registration
-- ============================================

-- ============================================
-- DROP EXISTING WARUNGS POLICIES
-- ============================================
DROP POLICY IF EXISTS "Users can read own warung" ON warungs;
DROP POLICY IF EXISTS "Owners can create warung" ON warungs;
DROP POLICY IF EXISTS "Owners can update own warung" ON warungs;

-- ============================================
-- CREATE FIXED WARUNGS POLICIES
-- ============================================

-- Users can read their own warung
-- Allow if user is owner of the warung
CREATE POLICY "Users can read own warung"
ON warungs FOR SELECT
USING (
  -- User can read warung if they are the owner
  owner_id = auth.uid()
  OR
  -- User can read warung if they are a member (warung_id matches)
  id IN (
    SELECT warung_id FROM users 
    WHERE id = auth.uid() 
    AND warung_id IS NOT NULL
    LIMIT 1
  )
);

-- Owners can create warung
-- FIXED: Allow if owner_id matches auth.uid() (for registration)
CREATE POLICY "Owners can create warung"
ON warungs FOR INSERT
WITH CHECK (owner_id = auth.uid());

-- Owners can update their own warung
CREATE POLICY "Owners can update own warung"
ON warungs FOR UPDATE
USING (owner_id = auth.uid())
WITH CHECK (owner_id = auth.uid());

-- ============================================
-- VERIFY POLICIES
-- ============================================
SELECT 
  schemaname,
  tablename,
  policyname,
  cmd,
  qual,
  with_check
FROM pg_policies
WHERE tablename = 'warungs'
ORDER BY policyname;

