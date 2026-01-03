-- ============================================
-- FIX RLS POLICY FOR CASH_FLOWS TABLE
-- ============================================
-- Fix untuk error 403 saat insert cash_flows
-- ============================================

-- Drop existing policy
DROP POLICY IF EXISTS "Authorized users can create cash flows" ON cash_flows;

-- Create new policy yang lebih sederhana dan reliable
-- Policy ini memastikan user memiliki warung_id yang sama dengan yang dikirim
-- Menggunakan pendekatan yang sama seperti policy products INSERT - tanpa NEW
-- PostgreSQL otomatis menggunakan nilai warung_id yang akan di-insert
CREATE POLICY "Authorized users can create cash flows"
ON cash_flows FOR INSERT
WITH CHECK (
  warung_id IN (
    SELECT warung_id FROM users 
    WHERE id = auth.uid()
    AND role IN ('owner', 'manager', 'cashier')
  )
);

-- ============================================
-- SUCCESS MESSAGE
-- ============================================
SELECT 'RLS policy for cash_flows INSERT fixed successfully!' AS message;

