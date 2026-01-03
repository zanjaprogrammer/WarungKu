# Firestore Setup - Next Steps
## Langkah-langkah Setelah Memilih Standard Edition

## 📋 Step-by-Step Guide

### Step 1: Database ID & Location ✅ (Sekarang)

**Database ID:**
- Firebase akan generate ID otomatis (contoh: `(default)`)
- **Biarkan default** - tidak perlu diubah
- Klik **"Next"**

**Location:**
- ⚠️ **PENTING**: Pilih location yang tepat!
- **Pilih**: `asia-southeast2 (Jakarta)` 
  - ✅ Terdekat dengan Indonesia
  - ✅ Latency rendah
  - ✅ Cost-effective
- **Alternatif** (jika Jakarta tidak tersedia):
  - `asia-southeast1 (Singapore)` - juga bagus
- **JANGAN pilih**:
  - ❌ `us-central` atau region US (terlalu jauh)
  - ❌ `europe-west` (terlalu jauh)
- Klik **"Next"**

---

### Step 2: Configure (Mode Selection) ⚠️ PENTING!

**Pilih Mode:**
- ⚠️ **PENTING**: Pilih **"Start in production mode"**
  - ✅ Aman untuk production
  - ✅ Security rules strict dari awal
  - ✅ Production-ready
- ❌ **JANGAN pilih** "Start in test mode"
  - Tidak aman untuk aplikasi yang akan di-launch
  - Bisa diakses siapa saja yang tahu project ID

**Apa yang terjadi?**
- Database dibuat dengan security rules yang **DENY semua** secara default
- Kita akan setup security rules sendiri setelah ini (lebih aman)

Klik **"Enable"** atau **"Create"**

---

### Step 3: Setup Security Rules (SEGERA SETELAH DATABASE DIBUAT!)

Setelah database selesai dibuat, **SEGERA** setup security rules:

1. **Buka tab "Rules"** di halaman Firestore
2. **Paste rules production** dari `FIRESTORE_PRODUCTION_SETUP.md`
3. **Klik "Publish"**
4. **Test rules** dengan Rules Playground (optional tapi recommended)

---

## ✅ Checklist

- [ ] Step 1: Pilih **Standard edition** ✅ (Sudah)
- [ ] Step 2: Database ID: Biarkan default
- [ ] Step 3: Location: Pilih **`asia-southeast2 (Jakarta)`**
- [ ] Step 4: Mode: Pilih **"Start in production mode"**
- [ ] Step 5: Klik **"Enable"** / **"Create"**
- [ ] Step 6: Setup Security Rules (lihat `FIRESTORE_PRODUCTION_SETUP.md`)
- [ ] Step 7: Test aplikasi

---

## 🔗 Quick Reference

**Security Rules untuk Production:**
Lihat file: `FIRESTORE_PRODUCTION_SETUP.md`

**Firebase Console Links:**
- Firestore Database: https://console.firebase.google.com/project/warungku-725ec/firestore
- Firestore Rules: https://console.firebase.google.com/project/warungku-725ec/firestore/rules

---

## ⚠️ Important Notes

1. **Location tidak bisa diubah** setelah database dibuat - pilih dengan hati-hati!
2. **Mode tidak bisa diubah** - pastikan pilih production mode
3. **Setup security rules SEBELUM** testing aplikasi
4. **Test rules** dengan simulator untuk memastikan aman

---

## 🎯 Summary

**Next Steps:**
1. ✅ Database ID: Default (biarkan)
2. ✅ Location: `asia-southeast2 (Jakarta)`
3. ✅ Mode: **"Start in production mode"**
4. ✅ Enable/Create database
5. ✅ Setup security rules (SEGERA!)

Setelah semua selesai, aplikasi siap untuk testing! 🚀

