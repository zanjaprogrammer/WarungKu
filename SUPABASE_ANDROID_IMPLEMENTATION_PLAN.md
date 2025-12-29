# Supabase Android Implementation Plan
## Migrasi dari Firebase ke Supabase di Android App

---

## ✅ Setup Complete

- [x] Supabase project created
- [x] Database schema created
- [x] RLS policies configured
- [x] Authentication enabled
- [x] Config file created

---

## 📋 Implementation Steps

### Phase 1: Update Dependencies
- [ ] Remove Firebase dependencies
- [ ] Add Supabase dependencies
- [ ] Update `build.gradle`

### Phase 2: Create Supabase Client
- [ ] Create `SupabaseClient` singleton
- [ ] Load config from `supabase_config.properties`
- [ ] Initialize Supabase client

### Phase 3: Create SupabaseAuthManager
- [ ] Replace `AuthManager` (Firebase) dengan `SupabaseAuthManager`
- [ ] Implement login/register methods
- [ ] Implement session management
- [ ] Implement user data sync dengan `users` table

### Phase 4: Update LoginActivity
- [ ] Replace Firebase Auth dengan Supabase Auth
- [ ] Update registration flow
- [ ] Update login flow
- [ ] Handle email confirmation (jika ON)

### Phase 5: Create SupabaseSyncService
- [ ] Replace `FirestoreSyncService` dengan `SupabaseSyncService`
- [ ] Implement sync Products ke Supabase
- [ ] Implement sync CashFlows ke Supabase
- [ ] Use Supabase Realtime untuk cloud → local sync

### Phase 6: Update Activities
- [ ] Update all activities untuk menggunakan SupabaseAuthManager
- [ ] Update permission checks
- [ ] Test authentication flow

### Phase 7: Testing
- [ ] Test registration
- [ ] Test login
- [ ] Test data sync
- [ ] Test permissions
- [ ] Test invite system

---

## 🔧 Technical Details

### Supabase Dependencies

```gradle
dependencies {
    // Supabase
    implementation 'io.github.jan-tennert.supabase:postgrest-kt:2.0.0'
    implementation 'io.github.jan-tennert.supabase:realtime-kt:2.0.0'
    implementation 'io.github.jan-tennert.supabase:storage-kt:2.0.0'
    implementation 'io.github.jan-tennert.supabase:auth-kt:2.0.0'
    implementation 'io.github.jan-tennert.supabase:functions-kt:2.0.0'
    
    // Kotlin Coroutines (required)
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
}
```

### Supabase Client Initialization

```kotlin
// Java equivalent akan dibuat
val supabase = SupabaseClient(
    supabaseUrl = "https://ydwjzbaceheaheaafhoi.supabase.co",
    supabaseKey = "sb_publishable_Ktpd_32xQLRTS2bfwSh66Q_NhJajwkN"
)
```

### Key Changes from Firebase

1. **Authentication:**
   - Firebase Auth → Supabase Auth
   - `FirebaseAuth.getInstance()` → `supabase.auth`
   - `FirebaseUser` → `User` (Supabase)

2. **Database:**
   - Firestore → PostgreSQL (via PostgREST)
   - `Firestore.getInstance()` → `supabase.postgrest`
   - Collections → Tables
   - Documents → Rows

3. **Real-time:**
   - Firestore listeners → Supabase Realtime subscriptions
   - `addSnapshotListener()` → `channel.subscribe()`

---

## 📝 Files to Create/Update

### New Files:
- `SupabaseClient.java` - Singleton Supabase client
- `SupabaseAuthManager.java` - Authentication manager
- `SupabaseSyncService.java` - Data sync service
- `SupabaseConfigLoader.java` - Load config from properties

### Files to Update:
- `app/build.gradle` - Dependencies
- `LoginActivity.java` - Supabase auth
- `MainActivity.java` - Auth checks
- All other activities - Auth checks
- `DataRepository.java` - Trigger sync

---

## 🚨 Important Notes

### Java vs Kotlin:
- Supabase SDK adalah Kotlin-first
- Kita perlu menggunakan Java interop atau convert ke Kotlin
- Atau gunakan Java wrapper jika tersedia

### Migration Strategy:
1. **Gradual Migration:** Bisa run both systems temporarily
2. **Data Migration:** Migrate existing data dari Firebase ke Supabase
3. **Testing:** Test thoroughly sebelum remove Firebase code

---

## 📚 Next Steps

1. Start dengan Phase 1: Update Dependencies
2. Create SupabaseClient
3. Create SupabaseAuthManager
4. Update LoginActivity
5. Test authentication flow

---

**Status:** Ready untuk implementasi! 🚀

