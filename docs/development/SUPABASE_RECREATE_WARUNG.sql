-- ============================================
-- RECREATE WARUNG FOR USER
-- ============================================
-- Script untuk delete warung lama dan create warung baru
-- Lalu update user dengan warung_id yang baru
-- ============================================

-- Step 1: Check user data
SELECT 
    id,
    email,
    name,
    role,
    warung_id,
    is_active
FROM users 
WHERE id = 'ed2b67be-4d42-4be5-9b97-0a434c811615';

-- Step 2: Check existing warungs untuk user ini
SELECT 
    id,
    name,
    owner_id,
    created_at
FROM warungs 
WHERE owner_id = 'ed2b67be-4d42-4be5-9b97-0a434c811615'
ORDER BY created_at DESC;

-- Step 3: Delete existing warungs untuk user ini (optional - uncomment jika perlu)
-- WARNING: Ini akan delete semua warung yang dimiliki user
-- DELETE FROM warungs 
-- WHERE owner_id = 'ed2b67be-4d42-4be5-9b97-0a434c811615';

-- Step 4: Create new warung untuk user
-- Generate UUID baru untuk warung_id
INSERT INTO warungs (id, name, owner_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'Warung azanjabiil',
    'ed2b67be-4d42-4be5-9b97-0a434c811615',
    NOW(),
    NOW()
)
RETURNING id, name, owner_id;

-- Step 5: Update user dengan warung_id yang baru
-- (Ganti 'WARUNG_ID_DARI_STEP_4' dengan id yang di-return dari INSERT di atas)
-- UPDATE users 
-- SET warung_id = 'WARUNG_ID_DARI_STEP_4'
-- WHERE id = 'ed2b67be-4d42-4be5-9b97-0a434c811615';

-- Step 6: Verify update
SELECT 
    id,
    email,
    role,
    warung_id
FROM users 
WHERE id = 'ed2b67be-4d42-4be5-9b97-0a434c811615';

SELECT 
    id,
    name,
    owner_id
FROM warungs 
WHERE owner_id = 'ed2b67be-4d42-4be5-9b97-0a434c811615';

