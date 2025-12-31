# Payment Denomination Buttons Enhancement Summary

## Fitur Baru
Menambahkan tombol denominasi uang yang lebih lengkap pada dialog pembayaran tunai dengan fungsi akumulatif (menambah nilai ketika ditekan berulang).

## Perubahan yang Dilakukan

### 1. Layout Payment Dialog (`layout_bottom_sheet_payment.xml`)
**Sebelum**: 3 tombol (5rb, 10rb, 100rb) dalam 1 baris
**Sesudah**: 7 tombol + 1 tombol clear dalam 2 baris:

**Baris 1**: 1rb | 2rb | 5rb | 10rb
**Baris 2**: 20rb | 50rb | 100rb | Clear

### 2. Logika Tombol yang Diperbarui
**Sebelum**: Tombol mengganti nilai (replace)
- Tekan 5rb → Input menjadi "5000"
- Tekan 10rb → Input menjadi "10000"

**Sesudah**: Tombol menambah nilai (accumulative)
- Input kosong, tekan 5rb → Input menjadi "5000"
- Tekan 2rb lagi → Input menjadi "7000"
- Tekan 10rb lagi → Input menjadi "17000"

### 3. Tombol Clear
Menambahkan tombol "Clear" berwarna merah untuk mengosongkan input dan memfokuskan kursor.

### 4. Helper Method `addToPaymentAmount()`
Dibuat method helper yang:
- Membaca nilai saat ini dari input field
- Menambahkan nilai denominasi yang dipilih
- Mengupdate input field dengan total baru
- Memposisikan kursor di akhir text

## Files yang Dimodifikasi

### Layout
1. `app/src/main/res/layout/layout_bottom_sheet_payment.xml`
   - Menambahkan tombol 1rb, 2rb, 20rb, 50rb
   - Menambahkan tombol Clear
   - Mengatur layout dalam 2 baris

### Java Activities (5 files)
1. `app/src/main/java/com/zanjaprogrammer/warungku/MainActivity.java`
2. `app/src/main/java/com/zanjaprogrammer/warungku/SellActivity.java`
3. `app/src/main/java/com/zanjaprogrammer/warungku/HistoryActivity.java`
4. `app/src/main/java/com/zanjaprogrammer/warungku/StockActivity.java`
5. `app/src/main/java/com/zanjaprogrammer/warungku/SummaryActivity.java`

**Perubahan di setiap activity**:
- Menambahkan findViewById untuk tombol baru
- Mengubah setOnClickListener untuk menggunakan `addToPaymentAmount()`
- Menambahkan method helper `addToPaymentAmount()`

## Denominasi yang Tersedia
- **1rb** (Rp 1.000) - untuk pembayaran kecil
- **2rb** (Rp 2.000) - denominasi umum
- **5rb** (Rp 5.000) - denominasi umum
- **10rb** (Rp 10.000) - denominasi umum
- **20rb** (Rp 20.000) - denominasi besar
- **50rb** (Rp 50.000) - denominasi besar
- **100rb** (Rp 100.000) - denominasi besar
- **Clear** - mengosongkan input

## Contoh Penggunaan
**Skenario**: Total belanja Rp 27.000

1. User tekan "20rb" → Input: 20000
2. User tekan "5rb" → Input: 25000  
3. User tekan "2rb" → Input: 27000 (pas)

Atau:

1. User tekan "50rb" → Input: 50000
2. Kembalian otomatis terhitung: Rp 23.000

## Manfaat
- **Lebih cepat**: Input denominasi umum dengan 1 tap
- **Akumulatif**: Bisa kombinasi beberapa denominasi
- **Fleksibel**: Bisa clear dan mulai ulang
- **User-friendly**: Sesuai dengan uang fisik Indonesia
- **Mengurangi error**: Tidak perlu ketik manual untuk nilai umum