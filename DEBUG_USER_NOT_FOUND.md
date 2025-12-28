# Debug: "User not found" Error
## Troubleshooting Guide

## 🔍 Kemungkinan Penyebab

Error "User not found" bisa terjadi karena:

1. **Registrasi tidak selesai** - User document tidak terbuat di Firestore
2. **Security rules masih block** - Rules belum ter-update dengan benar
3. **Document ID tidak match** - User document dibuat dengan ID yang berbeda

---

## ✅ Step 1: Check Firebase Console

### Check Authentication:
1. Buka: https://console.firebase.google.com/project/warungku-725ec/authentication/users
2. **Apakah ada user?**
   - ✅ Ada → Lanjut ke Step 2
   - ❌ Tidak ada → Registrasi gagal di Firebase Auth

### Check Firestore:
1. Buka: https://console.firebase.google.com/project/warungku-725ec/firestore/data
2. **Check collections:**
   - `users` → Apakah ada document?
   - `warungs` → Apakah ada document?

3. **Jika `users` collection kosong:**
   - ❌ User document tidak terbuat saat registrasi
   - Kemungkinan: Security rules block create, atau error saat create

4. **Jika `users` collection ada document:**
   - ✅ Check document ID: Apakah sama dengan Firebase Auth UID?
   - ✅ Check fields: `userId`, `email`, `role`, `warungId` ada?

---

## ✅ Step 2: Check Logcat

Saya sudah menambahkan logging detail. Check logcat untuk:

### Saat Registrasi:
```
LoginActivity: Creating warung: {warungId}
LoginActivity: Warung document created successfully: {warungId}
LoginActivity: User document created successfully: {userId}
LoginActivity: User data: {userId}, {email}, {role}, {warungId}
```

### Saat Login:
```
LoginActivity: Loading user from Firestore: {userId}
AuthManager: Loading user from Firestore: {userId}
AuthManager: User document found in Firestore
AuthManager: User parsed successfully: {email}, Role: {role}
```

### Jika Error:
```
AuthManager: User document does not exist in Firestore: {userId}
```

**Cara check logcat:**
```bash
adb logcat | grep -E "LoginActivity|AuthManager|Firebase|PERMISSION"
```

---

## ✅ Step 3: Test Registrasi Lagi

1. **Clear app data** atau uninstall & reinstall
2. **Test registrasi** dengan email baru
3. **Monitor logcat** saat registrasi
4. **Check Firebase Console** setelah registrasi

---

## ✅ Step 4: Verify Security Rules

Pastikan rules sudah benar di Firebase Console:

1. Buka: https://console.firebase.google.com/project/warungku-725ec/firestore/rules
2. **Check rules untuk `users` collection:**
   ```javascript
   match /users/{userId} {
     // Harus ada ini:
     allow create: if request.auth != null && 
                      request.auth.uid == userId &&
                      request.resource.data.userId == userId;
   }
   ```

3. **Check rules untuk `warungs` collection:**
   ```javascript
   match /warungs/{warungId} {
     // Harus ada ini:
     allow create: if request.auth != null;
   }
   ```

4. **Pastikan rules sudah di-publish** (ada tombol "Publish" di atas)

---

## ✅ Step 5: Manual Check di Firebase Console

### Test dengan Rules Playground:
1. Buka: https://console.firebase.google.com/project/warungku-725ec/firestore/rules
2. Klik **"Rules Playground"** (di kanan atas)
3. **Test scenario:**
   - Location: `users/{userId}`
   - Authenticated: Yes
   - User ID: `test-user-id`
   - Operation: `create`
   - Data: `{userId: "test-user-id", email: "test@example.com", role: "owner", warungId: "test-warung-id"}`
4. **Klik "Run"**
5. **Expected:** ✅ Allowed

---

## 🔧 Quick Fix: Re-register

Jika user document tidak terbuat:

1. **Delete user di Firebase Console:**
   - Authentication → Users → Delete user yang error

2. **Clear app data:**
   - Settings → Apps → WarungKu → Clear Data

3. **Test registrasi lagi** dengan email yang sama atau email baru

---

## 📋 Checklist Debug

- [ ] Check Firebase Console → Authentication → Users (ada user?)
- [ ] Check Firebase Console → Firestore → Data (ada `users` collection?)
- [ ] Check logcat untuk error detail
- [ ] Verify security rules sudah benar dan ter-publish
- [ ] Test registrasi lagi dengan logging enabled

---

**Silakan check Firebase Console dan logcat, lalu beri tahu hasilnya!**

