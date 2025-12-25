Aplikasi dengan java mobile tentukan versi yg populer yang akan dibuat adalah aplikasi untuk mengelola warung. Aplikasi ini akan memiliki fitur-fitur berikut:

KEY FEATURES: 
    1. Penjualan Cepat (CORE)

        Jual barang dengan 1 tap

        Long press untuk pilih jumlah

        Otomatis:

            stok berkurang

            uang bertambah

            profit dihitung (jika ada harga beli)

    2. Manajemen Stok Sederhana

        Tambah stok barang

        Penyesuaian stok (hilang / rusak / salah hitung)

        Alert stok hampir habis

    3. Pencatatan Uang (Cash)

        Catat uang masuk (penjualan)

        Catat uang keluar (belanja, listrik, dll)

        Tampilkan saldo kas saat ini

    4. Daftar Belanja Otomatis

        Barang yang stoknya hampir habis

        Urut berdasarkan yang paling sering terjual

        Bisa dicentang setelah dibeli

HALAMAN / SCREEN YANG DIBUTUHKAN:
    1. Home / Hari Ini

        Fungsi:
        Ringkasan kondisi warung hari ini.

        Isi:

            Total penjualan hari ini

            Total pengeluaran hari ini

            Saldo kas saat ini

            Jumlah barang hampir habis

        Aksi cepat:

            Jual

            Tambah stok

            Catat pengeluaran

    2. Jual Barang

        Fungsi:
            Mencatat penjualan secepat mungkin.

        Isi:

            Grid daftar produk

        Interaksi:

            Tap → jual 1

            Long press → pilih jumlah

    3. Stok Barang

        Fungsi:
            Melihat kondisi stok.

        Isi:

            Nama barang

            Sisa stok

            Status (aman / hampir habis)

        Aksi:

            Tambah stok

            Penyesuaian stok

    4. Tambah / Edit Barang

        Fungsi:
            Mendaftarkan barang baru.

        Field minimal:

            Nama barang

            Harga jual

            Harga beli (opsional)

            Stok awal

            Minimum stok

    5. Uang (Cash)

        Fungsi:
            Menjawab pertanyaan “uang saya sekarang ada berapa?”

        Isi:

            Saldo kas

            Riwayat uang masuk & keluar

        Aksi:

            Catat pengeluaran manual

    6. Daftar Belanja

        Fungsi:
            Memandu belanja stok.

        Isi:

            Daftar barang hampir habis

            Urutan berdasarkan frekuensi jual

            Checklist


RINGKASAN STRUKTUR LAYAR:
    Home (Hari Ini)
     ├─ Jual Barang
     ├─ Stok Barang
     │   └─ Tambah / Edit Barang
     ├─ Uang (Cash)
     └─ Daftar Belanja

ATURAN EMAS (WAJIB)

    1. Semua aksi penting maksimal 2 tap

    2. Tidak ada istilah teknis

    3. Tidak ada grafik di MVP

    4. Tidak ada fitur “jarang dipakai”

UI/UX:
    🏠 Home (Hari Ini)

        Desain:

            3–4 card besar vertikal

            Angka bold

            Contoh isi card:

            💰 Penjualan Hari Ini

            💼 Uang Sekarang

            📉 Pengeluaran

            📦 Stok Hampir Habis

        Bottom bar:

            Home

            Jual

            Stok

            Uang

    🛒 Jual Barang

        Desain wajib:

            Grid 2–3 kolom

            Tombol produk besar

            Warna netral

        Interaksi:

            Tap → langsung jual

            Long press → qty

        ❌ Jangan popup konfirmasi tiap jual.

    📦 Stok Barang

        List simpel:

            Nama kiri

            Stok kanan (bold)

            Badge kecil: Hampir habis

        Aksi:

            Floating button “Tambah stok”

    💰 Uang (Cash)

        Fokus:

            Saldo kas besar di atas

            Riwayat di bawah

        Warna:

            Hijau = masuk

            Merah = keluar

    

    🧾 Daftar Belanja

        Checklist:

            Nama barang

            Sisa stok

            Checkbox

        Ini layar action, bukan laporan.