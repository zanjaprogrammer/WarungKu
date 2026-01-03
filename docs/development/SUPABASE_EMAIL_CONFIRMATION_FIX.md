# Fix Email Confirmation Error

## Masalah
Error yang muncul:
- `email_not_confirmed` - Email belum dikonfirmasi
- `over_email_send_rate_limit` - Rate limit email terlampaui

## Solusi

### 1. Pastikan Email Confirmation Disabled di Supabase

1. Buka Supabase Dashboard: https://supabase.com/dashboard
2. Pilih project Anda
3. Pergi ke **Authentication** > **Providers** > **Email**
4. Pastikan **"Confirm email"** setting adalah **OFF** (disabled)
5. Scroll ke bawah dan klik **Save**

### 2. Untuk Development/Testing

Jika masih muncul error `email_not_confirmed`:

**Opsi A: Tunggu beberapa saat**
- Supabase mungkin masih memproses perubahan setting
- Tunggu 1-2 menit dan coba lagi

**Opsi B: Gunakan email yang berbeda**
- Untuk testing, gunakan email yang belum pernah digunakan
- Contoh: `test1@example.com`, `test2@example.com`, dll

**Opsi C: Manual confirm via Supabase Dashboard**
1. Buka **Authentication** > **Users**
2. Cari user yang baru register
3. Klik user tersebut
4. Klik **"Confirm email"** button

### 3. Untuk Rate Limit Error

Jika muncul `over_email_send_rate_limit`:
- **Tunggu 5-10 menit** sebelum mencoba lagi
- Atau gunakan email yang berbeda untuk testing
- Rate limit biasanya reset setiap beberapa menit

## Testing

Setelah fix:
1. Register dengan email baru: `test@example.com`
2. Password: `test123456`
3. Jika berhasil, langsung bisa login tanpa perlu konfirmasi email

## Catatan

- Error handling sudah diperbaiki untuk menampilkan pesan yang lebih user-friendly
- Jika email confirmation benar-benar disabled, user akan langsung confirmed setelah register
- Untuk production (Play Store), pastikan email confirmation **OFF** agar user tidak perlu konfirmasi email

