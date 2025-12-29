-- ============================================
-- FIX RLS POLICY FOR CASH_FLOWS TABLE (AGGRESSIVE)
-- ============================================
-- Fix untuk error 403 saat insert cash_flows
-- Versi agresif yang memastikan policy benar-benar di-update
-- ============================================

-- Step 1: Drop ALL existing policies untuk cash_flows INSERT (multiple attempts)
DROP POLICY IF EXISTS "Authorized users can create cash flows" ON cash_flows;
DROP POLICY IF EXISTS "Users can create cash flows" ON cash_flows;
DROP POLICY IF EXISTS "Authorized users can insert cash flows" ON cash_flows;

-- Step 2: Verify no policies exist (optional check)
-- SELECT policyname FROM pg_policies WHERE tablename = 'cash_flows' AND cmd = 'INSERT';

-- Step 3: Create new policy yang permisif (untuk debugging)
CREATE POLICY "Authorized users can create cash flows"
ON cash_flows FOR INSERT
WITH CHECK (
  EXISTS (
    SELECT 1 FROM users 
    WHERE id = auth.uid()
    AND role IN ('owner', 'manager', 'cashier')
  )
);

-- Step 4: Verify policy created
SELECT 
    policyname,
    cmd,
    with_check
FROM pg_policies 
WHERE tablename = 'cash_flows' 
AND cmd = 'INSERT'
AND policyname = 'Authorized users can create cash flows';

-- ============================================
-- SUCCESS MESSAGE
-- ============================================
SELECT 'RLS policy for cash_flows INSERT fixed (aggressive version)!' AS message;

