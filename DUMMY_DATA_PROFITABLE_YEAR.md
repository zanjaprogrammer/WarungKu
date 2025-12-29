# Generate Data Tahun Menguntungkan

## Deskripsi
Fitur ini memungkinkan Anda untuk membuat dummy data yang menunjukkan bisnis yang sangat menguntungkan sepanjang tahun ini. Data yang dihasilkan akan menampilkan grafik laporan dengan pendapatan yang jauh lebih tinggi daripada pengeluaran.

## Cara Menggunakan

### 1. Akses Menu
- Buka aplikasi WarungKu
- Masuk ke halaman **Ringkasan** (Summary)
- Tap menu titik tiga (⋮) di pojok kanan atas
- Pilih **"Generate Data Tahun Menguntungkan"**

### 2. Konfirmasi
- Akan muncul dialog konfirmasi dengan detail data yang akan dibuat
- Tap **"Generate"** untuk melanjutkan
- Proses akan berjalan di background (membutuhkan waktu beberapa detik)

### 3. Melihat Hasil
- Setelah selesai, buka halaman **Laporan**
- Pilih filter **"Tahun Ini"**
- Grafik akan menunjukkan trend pendapatan yang tinggi dengan pengeluaran minimal

## Karakteristik Data yang Dihasilkan

### Produk (35 items)
- **Margin keuntungan**: 35-60% (lebih tinggi dari standar 30-50%)
- **Stok**: 20-170 unit per produk
- **Harga beli**: Rp 1.000 - Rp 20.000
- **Variasi produk**: Makanan, minuman, kebutuhan sehari-hari

### Transaksi Penjualan
- **Volume**: 8-20 transaksi per hari
- **Periode**: Dari 1 Januari tahun ini hingga hari ini
- **Jam operasional**: 07:00 - 22:00
- **Kuantitas per transaksi**: 1-10 item
- **Variasi musiman**:
  - Desember & Januari: +40% (musim liburan)
  - Juni & Juli: +20% (pertengahan tahun)
  - April & Mei: +10% (musim semi)
  - Weekend: +30% dari hari biasa

### Pengeluaran (Minimal)
- **Frekuensi**: Hanya 10% hari memiliki pengeluaran
- **Jumlah**: Maksimal 10% dari pendapatan harian
- **Nominal**: Rp 10.000 - Rp 40.000 per transaksi
- **Jenis**: Operasional, listrik, internet, transport

## Hasil yang Diharapkan

### Grafik Laporan Tahunan
- **Pendapatan**: Garis hijau yang tinggi dan konsisten naik
- **Pengeluaran**: Garis merah yang rendah dan jarang muncul
- **Selisih**: Keuntungan bersih yang sangat signifikan

### Ringkasan Keuangan
- **Total Pendapatan**: Jutaan rupiah (tergantung hari dalam tahun)
- **Total Pengeluaran**: Hanya sekitar 5-10% dari pendapatan
- **Keuntungan Bersih**: 85-90% dari total pendapatan

### Produk Terlaris
- Akan menampilkan produk dengan penjualan tertinggi
- Data salesCount akan terupdate sesuai transaksi yang dibuat

## Tips Penggunaan

1. **Gunakan untuk Demo**: Ideal untuk menunjukkan kemampuan aplikasi kepada calon pengguna
2. **Testing Fitur**: Berguna untuk menguji fitur laporan dengan data yang realistis
3. **Analisis Trend**: Melihat bagaimana grafik menampilkan data dalam periode panjang
4. **Backup Data**: Disarankan backup data asli sebelum generate dummy data

## Catatan Penting

- Data dummy akan **ditambahkan** ke data existing, tidak mengganti
- Untuk data bersih, gunakan fitur "Restore" dengan backup kosong
- Proses generate membutuhkan waktu beberapa detik hingga menit tergantung performa device
- Data yang dibuat menggunakan timestamp real dari Januari hingga hari ini

## Troubleshooting

### Grafik Tidak Muncul
- Pastikan sudah memilih filter "Tahun Ini" di halaman Laporan
- Refresh halaman dengan keluar dan masuk kembali ke Laporan

### Proses Lama
- Generate data untuk satu tahun penuh membutuhkan waktu
- Jangan tutup aplikasi selama proses berlangsung
- Tunggu hingga muncul notifikasi "berhasil dibuat"

### Data Tidak Sesuai
- Pastikan tanggal sistem device sudah benar
- Data dibuat berdasarkan tahun sistem saat ini

## Perbandingan dengan Generate Dummy Data Biasa

| Aspek | Dummy Data Biasa | Data Tahun Menguntungkan |
|-------|------------------|--------------------------|
| Periode | 30 hari terakhir | Januari - sekarang |
| Jumlah produk | 30 | 35 |
| Margin keuntungan | 30-50% | 35-60% |
| Transaksi/hari | 1-10 | 8-20 |
| Pengeluaran | 30% dari pendapatan | 10% dari pendapatan |
| Variasi musiman | Tidak ada | Ada (liburan, weekend) |
| Fokus | Testing umum | Menunjukkan profitabilitas |