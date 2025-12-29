# Supabase Testing Guide
## Panduan Testing Authentication Flow

---

## 🧪 Testing Checklist

### 1. **Registration (Owner)**
- [ ] Buka aplikasi
- [ ] Klik "Daftar" atau "Register"
- [ ] Input email baru (contoh: `test@example.com`)
- [ ] Input password (minimal 6 karakter)
- [ ] Klik "Daftar"
- [ ] **Expected:** 
  - Jika "Confirm email" ON: User harus check email dan klik link konfirmasi
  - Jika "Confirm email" OFF: Langsung masuk ke MainActivity
  - User otomatis menjadi "owner"
  - Warung otomatis dibuat

### 2. **Login**
- [ ] Logout dari aplikasi (jika sudah login)
- [ ] Input email yang sudah terdaftar
- [ ] Input password
- [ ] Klik "Login"
- [ ] **Expected:** 
  - Berhasil login
  - Masuk ke MainActivity
  - User data ter-load dengan benar

### 3. **Email Confirmation (jika ON)**
- [ ] Register dengan email baru
- [ ] Check email inbox
- [ ] Klik link konfirmasi di email
- [ ] **Expected:** 
  - Email terkonfirmasi
  - Bisa login setelah konfirmasi

### 4. **Session Persistence**
- [ ] Login ke aplikasi
- [ ] Close aplikasi (force stop)
- [ ] Buka aplikasi lagi
- [ ] **Expected:** 
  - Masih logged in (tidak perlu login lagi)
  - User data ter-load dari cache

### 5. **Logout**
- [ ] Login ke aplikasi
- [ ] Klik logout (jika ada menu)
- [ ] **Expected:** 
  - Session cleared
  - Redirect ke LoginActivity

---

## 🔍 Debugging Tips

### Check Logcat:
```bash
adb logcat | grep -E "SupabaseAuthManager|LoginActivity|Supabase"
```

### Common Issues:

1. **"Config not loaded" error:**
   - Check: `app/src/main/res/raw/supabase_config.properties` exists
   - Check: File contains correct Project URL and keys

2. **"Network error" atau "Connection failed":**
   - Check: Internet connection
   - Check: Supabase project URL is correct
   - Check: API key is correct

3. **"User not found" setelah register:**
   - Check: User document created di Supabase dashboard
   - Check: Warung document created
   - Check: Logcat untuk error details

4. **"Permission denied" error:**
   - Check: RLS policies di Supabase
   - Check: User authenticated (access token valid)

---

## 📊 Verify di Supabase Dashboard

### After Registration:
1. Buka Supabase Dashboard → Authentication → Users
2. Pastikan user baru muncul dengan email yang didaftarkan
3. Buka Table Editor → `users` table
4. Pastikan user document ada dengan:
   - `id` = user ID dari auth
   - `email` = email yang didaftarkan
   - `role` = "owner"
   - `warung_id` = UUID warung yang dibuat
5. Buka Table Editor → `warungs` table
6. Pastikan warung document ada dengan:
   - `id` = warung_id dari user
   - `name` = "Warung {email_username}"
   - `owner_id` = user ID

### After Login:
1. Check Authentication → Users
2. Pastikan user status = "Confirmed" (jika email confirmation ON)
3. Check Table Editor → `users` table
4. Pastikan user data ter-load dengan benar

---

## 🚨 Known Issues & Workarounds

### Issue 1: Email Confirmation Required
**Jika "Confirm email" ON:**
- User harus check email dan klik link konfirmasi sebelum bisa login
- Untuk testing, bisa set "Confirm email" OFF di Supabase Dashboard

### Issue 2: RLS Policies Blocking
**Jika mendapat "Permission denied":**
- Check RLS policies di Supabase
- Pastikan policies allow authenticated users to read/write their own data

### Issue 3: Access Token Expired
**Jika session hilang setelah beberapa waktu:**
- Implement refresh token logic (akan ditambahkan nanti)
- Untuk sekarang, user perlu login ulang

---

## ✅ Success Criteria

Authentication flow berhasil jika:
- ✅ User bisa register sebagai owner
- ✅ User bisa login setelah register
- ✅ User data ter-load dengan benar
- ✅ Warung otomatis dibuat saat register
- ✅ Session persist setelah close app
- ✅ User bisa logout (jika ada menu)

---

**Status:** Ready untuk testing! 🚀

