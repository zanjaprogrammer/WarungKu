-- ============================================
-- VERIFY RLS POLICY FOR CASH_FLOWS
-- ============================================
-- Script untuk verify policy sudah benar-benar di-update
-- ============================================

-- Check ALL policies untuk cash_flows INSERT
SELECT 
    policyname,
    cmd,
    permissive,
    roles,
    qual,
    with_check
FROM pg_policies 
WHERE tablename = 'cash_flows' 
AND cmd = 'INSERT';

-- Expected result:
-- Policy name: "Authorized users can create cash flows"
-- with_check should contain: EXISTS (SELECT 1 FROM users WHERE id = auth.uid() AND role IN ('owner', 'manager', 'cashier'))
-- Should NOT contain: warung_id IN (SELECT ...)

