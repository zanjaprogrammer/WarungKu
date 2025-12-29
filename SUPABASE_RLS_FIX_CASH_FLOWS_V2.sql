-- ============================================
-- FIX RLS POLICY FOR CASH_FLOWS TABLE (V2)
-- ============================================
-- Fix untuk error 403 saat insert cash_flows
-- Versi lebih permisif untuk debugging
-- ============================================

-- Drop existing policy
DROP POLICY IF EXISTS "Authorized users can create cash flows" ON cash_flows;

-- Create new policy yang lebih permisif
-- Allow insert jika user memiliki role yang sesuai, tanpa check warung_id match
-- (untuk debugging - akan di-restrict lagi setelah fix)
CREATE POLICY "Authorized users can create cash flows"
ON cash_flows FOR INSERT
WITH CHECK (
  EXISTS (
    SELECT 1 FROM users 
    WHERE id = auth.uid()
    AND role IN ('owner', 'manager', 'cashier')
  )
);

-- ============================================
-- SUCCESS MESSAGE
-- ============================================
SELECT 'RLS policy for cash_flows INSERT fixed (permissive version)!' AS message;

