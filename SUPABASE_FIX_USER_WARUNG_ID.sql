-- ============================================
-- FIX USER WARUNG_ID
-- ============================================
-- Script untuk fix user warung_id jika null atau tidak match
-- ============================================

-- Check user data
SELECT 
    id,
    email,
    name,
    role,
    warung_id,
    is_active
FROM users 
WHERE id = 'ed2b67be-4d42-4be5-9b97-0a434c811615';

-- Check warungs yang dimiliki oleh user ini (sebagai owner)
SELECT 
    id,
    name,
    owner_id,
    created_at
FROM warungs 
WHERE owner_id = 'ed2b67be-4d42-4be5-9b97-0a434c811615'
ORDER BY created_at DESC;

-- Update user warung_id jika null
-- Menggunakan warung_id dari warung yang dimiliki user (sebagai owner)
UPDATE users 
SET warung_id = (
    SELECT id FROM warungs 
    WHERE owner_id = 'ed2b67be-4d42-4be5-9b97-0a434c811615'
    ORDER BY created_at DESC
    LIMIT 1
)
WHERE id = 'ed2b67be-4d42-4be5-9b97-0a434c811615'
AND warung_id IS NULL;

-- Verify update
SELECT 
    id,
    email,
    role,
    warung_id
FROM users 
WHERE id = 'ed2b67be-4d42-4be5-9b97-0a434c811615';

