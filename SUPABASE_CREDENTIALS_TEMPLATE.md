# Supabase Credentials Template
## Template untuk menyimpan credentials Supabase

---

## ✅ API Keys Sudah Didapat

### Publishable Key:
```
sb_publishable_Ktpd_32xQLRTS2bfwSh66Q_NhJajwkN
```

### Secret Key:
```
sb_secret_Ay6CTE6xa1M1IsT7bIMnsw_zqwV2VRf
```

---

## 📝 Project URL

**⚠️ PERLU:** Project URL dari Supabase Dashboard

### Cara mendapatkan Project URL:
1. Buka Supabase Dashboard
2. Pilih project Anda
3. Di **Settings → API Keys**, scroll ke bawah
4. Atau di **Settings → General**, lihat **"Reference ID"**
5. Format: `https://xxxxx.supabase.co`

### Atau:
- Project URL biasanya terlihat di bagian atas dashboard
- Atau di **Settings → General → Reference ID**

---

## 🔧 Update Config File

Setelah mendapatkan Project URL, update file:
`app/src/main/res/raw/supabase_config.properties`

Ganti:
```properties
SUPABASE_URL=https://YOUR_PROJECT_ID.supabase.co
```

Dengan Project URL yang sebenarnya, contoh:
```properties
SUPABASE_URL=https://abcdefghijklmnop.supabase.co
```

---

## ✅ Checklist

- [x] Publishable key didapat
- [x] Secret key didapat
- [ ] Project URL didapat
- [ ] Config file di-update dengan Project URL
- [ ] File sudah di-verify tidak ter-commit ke Git

---

## 🚨 Security Reminder

- ✅ File `supabase_config.properties` sudah di `.gitignore`
- ⚠️ **JANGAN commit** file ini ke Git
- ⚠️ **JANGAN share** Secret key secara publik
- ✅ Publishable key safe untuk di-expose di client app

---

**Status:** Menunggu Project URL ⏳

