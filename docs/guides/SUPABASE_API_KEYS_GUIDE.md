# Supabase API Keys - Quick Guide
## Cara Menemukan API Keys di Supabase Dashboard

---

## 📍 Lokasi API Keys

### Step 1: Buka Settings
1. Login ke Supabase Dashboard: https://supabase.com/dashboard
2. Pilih project Anda
3. Di **sidebar kiri**, klik **"Settings"** (icon gear ⚙️)

### Step 2: Buka API Keys
1. Di sidebar kiri, scroll ke bawah ke bagian **"PROJECT SETTINGS"**
2. Klik **"API Keys"** (bukan "API Settings")
3. Anda akan melihat 2 tabs:
   - **"Publishable and secret API keys"** ← Tab baru (recommended)
   - **"Legacy anon, service_role API keys"** ← Tab lama (untuk backward compatibility)

---

## 🔑 Format API Keys Baru (Recommended)

### Tab: "Publishable and secret API keys"

#### 1. **Publishable Key**
- **Lokasi:** Section "Publishable key"
- **Format:** `sb_publishable_xxxxx...`
- **Penggunaan:** Client-side (Android app)
- **Keamanan:** ✅ Safe untuk di-expose di client (jika RLS enabled)
- **Cara copy:** Klik icon copy di sebelah key

#### 2. **Secret Keys**
- **Lokasi:** Section "Secret keys"
- **Format:** `sb_secret_xxxxx...`
- **Penggunaan:** Server-side only
- **Keamanan:** ⚠️ JANGAN di-expose di client!
- **Cara buat:** Klik **"+ New secret key"** jika belum ada

---

## 🔑 Format API Keys Lama (Legacy)

### Tab: "Legacy anon, service_role API keys"

#### 1. **Project URL**
- **Format:** `https://xxxxx.supabase.co`
- **Penggunaan:** Base URL untuk semua API calls

#### 2. **anon key**
- **Format:** `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
- **Penggunaan:** Client-side (Android app)
- **Keamanan:** ✅ Safe untuk di-expose di client (jika RLS enabled)

#### 3. **service_role key**
- **Format:** `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
- **Penggunaan:** Server-side only
- **Keamanan:** ⚠️ JANGAN di-expose di client!

---

## 📝 Visual Guide

```
Supabase Dashboard
├── Sidebar Kiri
│   └── Settings ⚙️
│       └── PROJECT SETTINGS
│           └── API Keys ← KLIK DI SINI!
│               ├── Tab: "Publishable and secret API keys"
│               │   ├── Publishable key: sb_publishable_...
│               │   └── Secret keys: sb_secret_...
│               └── Tab: "Legacy anon, service_role API keys"
│                   ├── Project URL: https://xxx.supabase.co
│                   ├── anon key: eyJhbGci...
│                   └── service_role key: eyJhbGci...
```

---

## ✅ Checklist

- [ ] Buka Settings di sidebar kiri
- [ ] Klik "API Keys" di bawah "PROJECT SETTINGS"
- [ ] Pilih tab (Publishable/Secret atau Legacy)
- [ ] Copy Project URL
- [ ] Copy Publishable key (atau anon key)
- [ ] Copy Secret key (atau service_role key) - jika diperlukan
- [ ] Save credentials dengan aman

---

## 🚨 Important Notes

### Publishable Key vs anon Key:
- **Publishable key** = Format baru (recommended)
- **anon key** = Format lama (masih bisa dipakai)
- **Keduanya sama fungsinya** untuk client-side

### Secret Key vs service_role Key:
- **Secret key** = Format baru (recommended)
- **service_role key** = Format lama (masih bisa dipakai)
- **Keduanya sama fungsinya** untuk server-side
- ⚠️ **JANGAN di-expose di client app!**

### Project URL:
- **Sama untuk kedua format**
- Format: `https://xxxxx.supabase.co`
- Dipakai untuk semua API calls

---

## 💡 Tips

1. **Gunakan format baru** (Publishable/Secret) jika memulai project baru
2. **Gunakan format lama** (anon/service_role) jika ingin kompatibel dengan tutorial/examples lama
3. **Keduanya bisa dipakai bersamaan** - pilih yang paling nyaman
4. **Project URL selalu sama** - tidak peduli format mana yang dipakai

---

**Lokasi:** Settings → PROJECT SETTINGS → API Keys ✅

