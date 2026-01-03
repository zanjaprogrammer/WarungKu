-- ============================================
-- CHECK USER DATA FOR DEBUGGING
-- ============================================
-- Script untuk check data user dan verify RLS policy
-- ============================================

-- Check user data (replace dengan user ID yang sesuai)
SELECT 
    id,
    email,
    name,
    role,
    warung_id,
    is_active,
    created_at
FROM users 
WHERE id = 'ed2b67be-4d42-4be5-9b97-0a434c811615';

-- Check warung data
SELECT 
    id,
    name,
    owner_id,
    created_at
FROM warungs 
WHERE id = '36785799-a0ed-46fb-bbbf-baa63a076135';

-- Check RLS policies untuk cash_flows
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
WHERE tablename = 'cash_flows' AND policyname = 'Authorized users can create cash flows';

