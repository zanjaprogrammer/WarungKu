-- ============================================
-- RECREATE WARUNG FOR USER (AUTO)
-- ============================================
-- Script untuk delete warung lama dan create warung baru secara otomatis
-- Lalu update user dengan warung_id yang baru
-- ============================================

-- Step 1: Delete existing warungs untuk user ini
-- WARNING: Ini akan delete semua warung yang dimiliki user
DELETE FROM warungs 
WHERE owner_id = 'ed2b67be-4d42-4be5-9b97-0a434c811615';

-- Step 2: Create new warung untuk user dan langsung update user
WITH new_warung AS (
    INSERT INTO warungs (id, name, owner_id, created_at, updated_at)
    VALUES (
        gen_random_uuid(),
        'Warung azanjabiil',
        'ed2b67be-4d42-4be5-9b97-0a434c811615',
        NOW(),
        NOW()
    )
    RETURNING id
)
UPDATE users 
SET warung_id = (SELECT id FROM new_warung)
WHERE id = 'ed2b67be-4d42-4be5-9b97-0a434c811615';

-- Step 3: Verify update
SELECT 
    u.id as user_id,
    u.email,
    u.role,
    u.warung_id,
    w.id as warung_id_from_warungs,
    w.name as warung_name,
    w.owner_id
FROM users u
LEFT JOIN warungs w ON w.owner_id = u.id
WHERE u.id = 'ed2b67be-4d42-4be5-9b97-0a434c811615';

-- ============================================
-- SUCCESS MESSAGE
-- ============================================
SELECT 'Warung recreated and user updated successfully!' AS message;

