# Supabase Setup Checklist
## Quick Checklist untuk Setup Supabase

---

## ✅ Phase 1: Project Creation
- [ ] Sign up/Login ke Supabase
- [ ] Create new project
- [ ] Set project name: `WarungKu`
- [ ] Set database password (SAVE PASSWORD!)
- [ ] Select region: **Southeast Asia (Singapore/Mumbai)**
- [ ] Wait for project creation (1-2 minutes)
- [ ] Get Project URL dari Settings → API
- [ ] Get `anon` key dari Settings → API
- [ ] Get `service_role` key dari Settings → API (⚠️ JANGAN SHARE!)

---

## ✅ Phase 2: Database Schema
- [x] Open SQL Editor
- [x] Run database schema script (`SUPABASE_SCHEMA_IDEMPOTENT.sql`)
- [ ] Verify 5 tables created:
  - [ ] `warungs`
  - [ ] `users`
  - [ ] `products`
  - [ ] `cash_flows`
  - [ ] `invites`
- [ ] Check indexes created
- [ ] Check triggers created

---

## ✅ Phase 3: Row Level Security (RLS)
- [x] Run RLS setup script (`SUPABASE_RLS_SETUP_FIXED.sql`)
- [ ] Verify RLS enabled di Table Editor
- [ ] Verify policies created untuk setiap table:
  - [ ] Warungs: 3 policies
  - [ ] Users: 4 policies
  - [ ] Products: 4 policies
  - [ ] Cash flows: 4 policies
  - [ ] Invites: 4 policies

---

## ✅ Phase 4: Authentication
- [ ] Open Authentication → Providers
- [ ] Enable Email provider
- [ ] Configure email templates (optional)
- [ ] Set Authentication → Settings:
  - [ ] Site URL configured
  - [ ] Email confirmations: ON/OFF (sesuai kebutuhan)

---

## ✅ Phase 5: Configuration File
- [x] Create `app/src/main/res/raw/supabase_config.properties`
- [x] Add Project URL: `https://ydwjzbaceheaheaafhoi.supabase.co`
- [x] Add Publishable key: `sb_publishable_Ktpd_32xQLRTS2bfwSh66Q_NhJajwkN`
- [x] Add Secret key: `sb_secret_Ay6CTE6xa1M1IsT7bIMnsw_zqwV2VRf`
- [x] Verify file added to `.gitignore`

---

## ✅ Phase 6: Verification
- [ ] Test connection ke Supabase dashboard
- [ ] Test insert data manual di Table Editor
- [ ] Verify data muncul di table
- [ ] Test authentication (jika sudah implement)

---

## 📝 Credentials to Save

### Project Info:
```
Project Name: WarungKu
Project URL: https://ydwjzbaceheaheaafhoi.supabase.co
Database Password: _______________ (SAVE SECURELY!)
Region: _______________
```

### API Keys:
```
Publishable Key: sb_publishable_Ktpd_32xQLRTS2bfwSh66Q_NhJajwkN
Secret Key: sb_secret_Ay6CTE6xa1M1IsT7bIMnsw_zqwV2VRf
```

---

## 🚨 Security Reminders
- [ ] Database password saved securely
- [ ] API keys saved securely
- [ ] `supabase_config.properties` added to `.gitignore`
- [ ] `service_role` key NOT shared publicly
- [ ] Only `anon` key used in client app

---

## 📚 Next Steps After Setup
- [ ] Update Android dependencies (Supabase SDK)
- [ ] Create SupabaseAuthManager
- [ ] Create SupabaseSyncService
- [ ] Update LoginActivity
- [ ] Test authentication
- [ ] Test data sync

---

**Status:** ⏳ In Progress / ✅ Complete

**Date Completed:** _______________

