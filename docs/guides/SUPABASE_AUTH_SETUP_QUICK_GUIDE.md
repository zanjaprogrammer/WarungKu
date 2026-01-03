# Supabase Authentication Setup - Quick Guide
## Phase 4: Setup Authentication untuk WarungKu

---

## 📋 Langkah-langkah

### Step 1: Buka Authentication Settings
1. Di Supabase Dashboard, klik **"Authentication"** di sidebar kiri
2. Klik **"Sign In / Providers"** (di bawah CONFIGURATION)
3. Pastikan tab **"Supabase Auth"** terpilih (bukan "Third-Party Auth")

### Step 2: Configure User Signups
Di section **"User Signups"**, configure:

1. **Allow new users to sign up:**
   - ✅ **ON** (default biasanya sudah ON)
   - Ini memungkinkan user baru untuk registrasi

2. **Confirm email:**
   - ✅ **ON** untuk production/Play Store (user harus confirm email dulu - lebih aman)
   - ❌ **OFF** untuk development/testing (langsung bisa login tanpa confirm email)
   - **Untuk aplikasi yang akan di-upload ke Play Store, set ke ON!**

3. **Allow manual linking:** 
   - Biarkan default (tidak perlu diubah)

4. **Allow anonymous sign-ins:**
   - Biarkan default (OFF untuk sekarang)

### Step 3: (Optional) Email Provider Settings
1. Scroll ke bawah untuk melihat provider settings
2. Pastikan **Email** provider sudah **Enabled**
3. (Optional) Configure email templates di menu **"Email"** (di bawah NOTIFICATIONS)

### Step 4: (Optional) Configure Email Templates
1. Klik **"Email Templates"** di menu Authentication
2. Anda bisa customize:
   - **Confirm signup** - Email untuk konfirmasi registrasi
   - **Magic Link** - Email untuk magic link login
   - **Change Email Address** - Email untuk perubahan email
   - **Reset Password** - Email untuk reset password
3. Untuk sekarang, biarkan default template

---

## ✅ Checklist

- [ ] Authentication → Sign In / Providers dibuka
- [ ] Tab "Supabase Auth" terpilih
- [ ] User Signups configured:
  - [ ] Allow new users to sign up: ON
  - [ ] Confirm email: **ON** (untuk production/Play Store) atau **OFF** (untuk development)
- [ ] Email provider enabled (default sudah ON)
- [ ] (Optional) Email templates reviewed

---

## 🎯 Default Settings (Recommended untuk Development)

### User Signups Settings (Production/Play Store):
- ✅ Allow new users to sign up: **ON**
- ✅ Confirm email: **ON** (untuk production - lebih aman)
- ✅ Allow manual linking: Default
- ✅ Allow anonymous sign-ins: Default (OFF)

### User Signups Settings (Development):
- ✅ Allow new users to sign up: **ON**
- ✅ Confirm email: **OFF** (untuk development/testing)
- ✅ Allow manual linking: Default
- ✅ Allow anonymous sign-ins: Default (OFF)

### Email Provider:
- ✅ Email provider: **Enabled** (default)

---

## 🚨 Important Notes

### Development vs Production:

**Development/Testing:**
- Confirm email: **OFF** (langsung bisa login tanpa confirm email)
- Lebih mudah untuk testing dan development

**Production/Play Store:**
- Confirm email: **ON** (user harus confirm email dulu)
- **Lebih aman** - memastikan email user valid
- **Recommended untuk aplikasi yang akan di-upload ke Play Store**
- User akan menerima email konfirmasi setelah registrasi

### Kenapa Confirm Email Penting untuk Production?

1. **Security:** Memastikan email user valid dan mereka punya akses ke email tersebut
2. **Spam Prevention:** Mencegah registrasi dengan email palsu
3. **Best Practice:** Standar untuk aplikasi production
4. **User Trust:** User lebih percaya aplikasi yang memverifikasi email mereka

### Site URL:
- Default biasanya sudah benar
- Jangan ubah kecuali ada kebutuhan khusus

---

## 📝 Next Steps

Setelah Authentication setup selesai:
1. ✅ Update Android dependencies (Supabase SDK)
2. ✅ Create SupabaseAuthManager class
3. ✅ Update LoginActivity untuk Supabase
4. ✅ Test authentication flow

---

**Status:** Ready untuk implementasi di Android app! 🚀

