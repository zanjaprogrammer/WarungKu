# Setup API Database Produk Eksternal

## 🎯 Tujuan
Mengkonfigurasi API untuk lookup produk berdasarkan barcode dari database eksternal (seperti GS1, Open Product Data, dll).

---

## 📡 API yang Bisa Digunakan

### 1. **Open Product Data (OPD)** - Recommended
- **URL:** `https://opendata.gs1.org/`
- **Format:** REST API, JSON
- **Gratis:** Ya (dengan batasan)
- **Dokumentasi:** https://opendata.gs1.org/

### 2. **Barcode Spider API**
- **URL:** `https://api.barcodespider.com/v1/`
- **Format:** REST API, JSON
- **Gratis:** Ya (terbatas)
- **Dokumentasi:** https://barcodespider.com/api

### 3. **CheckBarcode.com**
- **URL:** `https://api.checkbarcode.com/`
- **Format:** REST API, JSON
- **Gratis:** Ya (terbatas)

### 4. **GS1 Indonesia**
- **URL:** Tergantung registrasi
- **Format:** REST API
- **Gratis:** Perlu registrasi
- **Website:** https://www.gs1.org/id

---

## ⚙️ Cara Setup

### Step 1: Pilih API yang Akan Digunakan

Pilih salah satu API di atas sesuai kebutuhan. Untuk warung kecil, **Open Product Data** atau **Barcode Spider** direkomendasikan karena gratis.

### Step 2: Update Base URL di ProductApiClient

Buka file: `app/src/main/java/com/zanjaprogrammer/warungku/api/ProductApiClient.java`

Ganti `BASE_URL` dengan URL API yang dipilih:

```java
// Contoh untuk Open Product Data
private static final String BASE_URL = "https://opendata.gs1.org/";

// Atau untuk Barcode Spider
private static final String BASE_URL = "https://api.barcodespider.com/v1/";
```

### Step 3: Update Endpoint di ProductApiService

Buka file: `app/src/main/java/com/zanjaprogrammer/warungku/api/ProductApiService.java`

Sesuaikan endpoint sesuai dokumentasi API yang dipilih:

```java
// Contoh untuk Open Product Data
@GET("api/products/{barcode}")
Call<ProductApiResponse> lookupProduct(@Path("barcode") String barcode);

// Atau untuk Barcode Spider
@GET("lookup")
Call<ProductApiResponse> lookupProduct(@Query("upc") String barcode);
```

### Step 4: Update ProductApiResponse (jika perlu)

Sesuaikan field di `ProductApiResponse.java` dengan response format dari API yang dipilih.

### Step 5: Tambahkan API Key (jika diperlukan)

Beberapa API memerlukan API key. Tambahkan di header request:

```java
// Di ProductApiClient.java
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(chain -> {
        Request original = chain.request();
        Request.Builder requestBuilder = original.newBuilder()
            .header("Authorization", "Bearer YOUR_API_KEY")
            .header("Content-Type", "application/json");
        Request request = requestBuilder.build();
        return chain.proceed(request);
    })
    .build();
```

---

## 🧪 Testing

### Test dengan Barcode Real:

1. **Teh Botol Sosro:**
   - Barcode: `8996001600106`
   - Test scan dan lihat apakah data muncul

2. **Indomie:**
   - Barcode: `8992702000009`
   - Test scan dan lihat apakah data muncul

3. **Aqua:**
   - Barcode: `8999999400014`
   - Test scan dan lihat apakah data muncul

### Test Flow:

1. Buka aplikasi
2. Pergi ke halaman **Tambah Produk**
3. Scan barcode produk populer
4. Lihat apakah data otomatis terisi
5. Jika tidak, cek logcat untuk error

---

## 🔧 Troubleshooting

### API tidak bekerja:
1. **Cek internet connection**
2. **Cek BASE_URL sudah benar**
3. **Cek endpoint sesuai dokumentasi API**
4. **Cek logcat untuk error detail**

### Data tidak muncul:
1. **Cek response format dari API**
2. **Update ProductApiResponse sesuai format API**
3. **Cek apakah barcode valid**
4. **Cek apakah produk ada di database API**

### Error Network:
1. **Cek permission INTERNET di AndroidManifest**
2. **Cek koneksi internet**
3. **Cek firewall/security settings**

---

## 📝 Contoh Response Format

### Open Product Data:
```json
{
  "barcode": "8996001600106",
  "name": "Teh Botol Sosro",
  "brand": "Sosro",
  "category": "Minuman",
  "price": 5000,
  "currency": "IDR"
}
```

### Barcode Spider:
```json
{
  "status": "success",
  "item_attributes": {
    "title": "Teh Botol Sosro",
    "brand": "Sosro",
    "category": "Beverages",
    "price": "5000"
  }
}
```

---

## 🎯 Next Steps

1. **Pilih API yang sesuai**
2. **Update BASE_URL dan endpoint**
3. **Test dengan barcode real**
4. **Implementasi caching untuk offline use**
5. **Tambahkan error handling yang lebih baik**

---

**Dibuat oleh:** AI Assistant  
**Tanggal:** 26 Desember 2024

