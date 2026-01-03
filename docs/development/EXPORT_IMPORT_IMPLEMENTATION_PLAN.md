# Implementation Plan: Export/Import Produk

## 🎯 Tujuan
Memudahkan pemilik warung untuk:
- **Export** daftar produk ke Excel/CSV untuk backup atau analisis
- **Import** banyak produk sekaligus dari Excel/CSV (bulk input)
- Menghemat waktu input produk manual

---

## 📋 Fitur yang Akan Diimplementasi

### 1. **Export Produk** 📤
- Export semua produk ke file Excel (.xlsx) atau CSV (.csv)
- Format kolom: Nama, Harga Beli, Harga Jual, Stok, Barcode, Kategori (jika ada)
- Pilihan format: Excel atau CSV
- Simpan file ke Downloads folder atau pilih lokasi
- Toast notification saat export berhasil

### 2. **Import Produk** 📥
- Import produk dari file Excel (.xlsx) atau CSV (.csv)
- Validasi data sebelum import:
  - Nama produk wajib
  - Harga jual wajib (harga beli opsional)
  - Stok default 0 jika tidak diisi
  - Barcode opsional (skip jika duplikat)
- Preview data sebelum import (opsional)
- Summary hasil import (berhasil X, gagal Y)
- Error handling untuk data yang tidak valid

### 3. **Template Excel** 📄
- Generate template Excel untuk import
- Kolom dengan header yang jelas
- Contoh data untuk panduan
- Format yang mudah diisi

### 4. **UI/UX** 🎨
- Menu Export/Import di halaman Stock
- Dialog untuk pilih format (Excel/CSV)
- Progress indicator saat import/export
- Error messages yang jelas
- Success confirmation

---

## 📊 Struktur Data

### Format Excel/CSV:
```
| Nama Produk | Harga Beli | Harga Jual | Stok | Barcode | Kategori |
|-------------|-----------|------------|------|---------|----------|
| Teh Botol   | 3000      | 5000       | 50   | 899...  | Minuman  |
| Indomie     | 2500      | 3500       | 100  | 899...  | Makanan  |
```

### Field Mapping:
- **Nama Produk** → `Product.name` (required)
- **Harga Beli** → `Product.buyPrice` (optional, default null)
- **Harga Jual** → `Product.sellPrice` (required)
- **Stok** → `Product.currentStock` (optional, default 0)
- **Barcode** → `Product.barcode` (optional)
- **Kategori** → `Product.category` (optional, jika sudah ada field category)

---

## 🛠️ Technical Implementation

### Library yang Akan Digunakan

#### Option 1: Apache POI (Recommended untuk Excel)
```gradle
implementation 'org.apache.poi:poi:5.2.3'
implementation 'org.apache.poi:poi-ooxml:5.2.3'
```
- **Pros:** Full Excel support (.xlsx), formatting, styling
- **Cons:** Larger APK size (~2-3MB)

#### Option 2: OpenCSV (Untuk CSV)
```gradle
implementation 'com.opencsv:opencsv:5.7.1'
```
- **Pros:** Lightweight, simple
- **Cons:** Hanya CSV, tidak support Excel

#### Option 3: Kombinasi (Recommended)
- Apache POI untuk Excel
- OpenCSV untuk CSV
- Atau gunakan Apache POI untuk keduanya (POI bisa handle CSV juga)

### File Structure:
```
app/src/main/java/com/zanjaprogrammer/warungku/
├── utils/
│   ├── ExcelExporter.java      # Export ke Excel
│   ├── ExcelImporter.java      # Import dari Excel
│   ├── CsvExporter.java        # Export ke CSV
│   └── CsvImporter.java        # Import dari CSV
```

---

## 📱 UI Implementation

### 1. **Menu di StockActivity**
- Tambah menu item di toolbar: "Export/Import"
- Atau tambah di bottom sheet saat klik produk

### 2. **Export Dialog**
```
┌─────────────────────────────┐
│ Export Produk               │
├─────────────────────────────┤
│ Pilih Format:               │
│ ○ Excel (.xlsx)             │
│ ○ CSV (.csv)                │
│                             │
│ [Batal]  [Export]           │
└─────────────────────────────┘
```

### 3. **Import Dialog**
```
┌─────────────────────────────┐
│ Import Produk               │
├─────────────────────────────┤
│ Pilih File:                 │
│ [Pilih File...]             │
│                             │
│ Format: Excel/CSV           │
│                             │
│ [Batal]  [Import]           │
└─────────────────────────────┘
```

### 4. **Progress Dialog**
```
┌─────────────────────────────┐
│ Import Produk               │
├─────────────────────────────┤
│ Memproses...                │
│ ████████░░ 80%              │
│                             │
│ [Batal]                     │
└─────────────────────────────┘
```

### 5. **Result Dialog**
```
┌─────────────────────────────┐
│ Import Selesai              │
├─────────────────────────────┤
│ ✅ Berhasil: 45 produk      │
│ ❌ Gagal: 5 produk          │
│                             │
│ Detail error:               │
│ - Baris 3: Nama kosong      │
│ - Baris 7: Harga jual kosong│
│                             │
│ [OK]                        │
└─────────────────────────────┘
```

---

## 🔄 Flow Implementation

### Export Flow:
1. User klik "Export" di StockActivity
2. Dialog muncul: pilih format (Excel/CSV)
3. User klik "Export"
4. Generate file dengan semua produk
5. Save ke Downloads folder
6. Show toast: "File tersimpan di Downloads"
7. Option: Share file via Intent

### Import Flow:
1. User klik "Import" di StockActivity
2. Dialog muncul: pilih file
3. User pilih file dari file picker
4. Parse file (Excel/CSV)
5. Validasi setiap row
6. Show progress dialog
7. Insert valid products ke database
8. Show result dialog dengan summary
9. Refresh product list

---

## ✅ Validasi Data

### Required Fields:
- ✅ **Nama Produk**: Tidak boleh kosong
- ✅ **Harga Jual**: Harus > 0

### Optional Fields:
- **Harga Beli**: Jika kosong, set null
- **Stok**: Jika kosong, set 0
- **Barcode**: Jika kosong, set null. Jika duplikat, skip atau update?
- **Kategori**: Jika kosong, set null (jika field category sudah ada)

### Validation Rules:
1. Nama produk harus unik (cek duplikat di database)
2. Harga jual harus > 0
3. Harga beli harus > 0 (jika diisi)
4. Stok harus >= 0
5. Barcode harus unik (jika diisi)

### Error Handling:
- Skip row jika data tidak valid
- Log error untuk setiap row yang gagal
- Tampilkan summary di result dialog

---

## 📂 File Location

### Export:
- Default: `/storage/emulated/0/Download/WarungKu_Products_[timestamp].xlsx`
- Atau: `/storage/emulated/0/Download/WarungKu_Products_[timestamp].csv`

### Import:
- User pilih file dari file picker
- Support: Downloads, Documents, atau storage lainnya

---

## 🔐 Permissions

### Required Permissions:
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### Android 10+ (Scoped Storage):
- Gunakan `MediaStore` API untuk Android 10+
- Atau gunakan `SAF` (Storage Access Framework) untuk file picker
- Untuk Android 11+, mungkin perlu `MANAGE_EXTERNAL_STORAGE` (tapi tidak disarankan)

### Alternative (Recommended):
- Gunakan `ActivityResultContracts.GetContent()` untuk file picker (tidak perlu permission)
- Untuk export, gunakan `ActivityResultContracts.CreateDocument()` untuk save file

---

## 🧪 Testing Plan

### Manual Testing:
1. **Export Test:**
   - Export dengan 0 produk
   - Export dengan banyak produk
   - Export ke Excel
   - Export ke CSV
   - Buka file hasil export di Excel/Google Sheets

2. **Import Test:**
   - Import file Excel valid
   - Import file CSV valid
   - Import file dengan data tidak valid
   - Import file dengan duplikat nama
   - Import file dengan duplikat barcode
   - Import file kosong
   - Import file dengan format salah

3. **Edge Cases:**
   - File sangat besar (1000+ produk)
   - Karakter khusus dalam nama produk
   - Nama produk sangat panjang
   - Harga dengan format berbeda (Rp 5.000 vs 5000)

---

## 📝 Implementation Steps

### Phase 1: Setup & Export (Prioritas 1)
1. ✅ Add dependencies (Apache POI)
2. ✅ Create `ExcelExporter.java`
3. ✅ Create `CsvExporter.java`
4. ✅ Add Export menu di StockActivity
5. ✅ Implement export flow
6. ✅ Test export functionality

### Phase 2: Import (Prioritas 2)
1. ✅ Add file picker permission handling
2. ✅ Create `ExcelImporter.java`
3. ✅ Create `CsvImporter.java`
4. ✅ Add Import menu di StockActivity
5. ✅ Implement import flow dengan validasi
6. ✅ Add progress dialog
7. ✅ Add result dialog
8. ✅ Test import functionality

### Phase 3: Template & Polish (Prioritas 3)
1. ✅ Create template generator
2. ✅ Add template download option
3. ✅ Improve error messages
4. ✅ Add retry mechanism untuk failed rows
5. ✅ UI/UX improvements

---

## 🎨 UI/UX Considerations

### Design Principles:
1. **Simple**: Minimal steps untuk export/import
2. **Clear**: Error messages yang jelas
3. **Fast**: Progress indicator untuk operasi lama
4. **Safe**: Confirmation sebelum import (overwrite warning)

### Accessibility:
- Large touch targets
- Clear labels
- Error messages yang mudah dibaca

---

## ⚠️ Potential Issues & Solutions

### Issue 1: Large File Size
- **Problem**: File Excel besar bisa lambat
- **Solution**: 
  - Limit export (misal: max 1000 produk per export)
  - Atau gunakan streaming untuk file besar
  - Show progress untuk export juga

### Issue 2: Duplicate Products
- **Problem**: Import produk yang sudah ada
- **Solution**: 
  - Skip jika nama sama
  - Atau update produk yang sudah ada
  - Atau tanya user: Skip/Update/Cancel

### Issue 3: Format Compatibility
- **Problem**: Excel versi berbeda
- **Solution**: 
  - Gunakan format .xlsx (Excel 2007+)
  - Test dengan Google Sheets juga
  - Support CSV sebagai fallback

### Issue 4: Permission Issues
- **Problem**: Android 11+ storage permission
- **Solution**: 
  - Gunakan SAF (Storage Access Framework)
  - Atau gunakan app-specific directory
  - Request permission dengan jelas

---

## 📚 Resources

### Documentation:
- [Apache POI Documentation](https://poi.apache.org/)
- [OpenCSV Documentation](http://opencsv.sourceforge.net/)
- [Android Storage Guide](https://developer.android.com/training/data-storage)

### Example Code:
- Excel export/import examples
- CSV parsing examples
- File picker implementation

---

## ✅ Checklist

### Pre-Implementation:
- [ ] Review implementation plan
- [ ] Decide library (Apache POI vs OpenCSV vs both)
- [ ] Design UI mockup
- [ ] Plan error handling strategy

### Implementation:
- [ ] Add dependencies
- [ ] Create exporter classes
- [ ] Create importer classes
- [ ] Add UI components
- [ ] Implement export flow
- [ ] Implement import flow
- [ ] Add validations
- [ ] Add error handling
- [ ] Test with sample data

### Post-Implementation:
- [ ] Test dengan data real
- [ ] Performance testing
- [ ] UI/UX review
- [ ] Documentation update
- [ ] User testing (jika ada)

---

**Dibuat oleh:** AI Assistant  
**Tanggal:** 27 Desember 2024  
**Status:** Ready for Implementation

