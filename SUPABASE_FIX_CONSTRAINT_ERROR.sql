-- ============================================
-- FIX: Constraint Already Exists Error
-- ============================================
-- Script ini untuk fix error: "constraint fk_warungs_owner already exists"
-- 
-- Jika constraint sudah ada, script ini akan:
-- 1. Drop constraint yang sudah ada (jika ada)
-- 2. Re-create constraint dengan benar
-- 
-- Atau jika constraint belum ada, akan dibuat seperti biasa
-- ============================================

-- Drop constraint jika sudah ada
ALTER TABLE warungs 
DROP CONSTRAINT IF EXISTS fk_warungs_owner;

-- Re-create constraint
ALTER TABLE warungs 
ADD CONSTRAINT fk_warungs_owner 
FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL;

-- Success message
SELECT 'Constraint fk_warungs_owner fixed successfully!' AS message;

