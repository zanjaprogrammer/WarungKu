# Requirements Document

## Introduction

Implementasi monetisasi aplikasi WarungKu menggunakan Google AdMob dengan pendekatan konservatif yang mengutamakan user experience. Strategi ini akan mengintegrasikan banner ads dan interstitial ads secara strategis tanpa mengganggu workflow utama pengguna dalam mengelola warung mereka.

## Glossary

- **AdMob_SDK**: Google AdMob Software Development Kit untuk Android
- **Banner_Ad**: Iklan persegi panjang yang ditampilkan secara persisten di bagian bawah layar
- **Interstitial_Ad**: Iklan fullscreen yang ditampilkan pada transisi tertentu
- **Ad_Unit_ID**: Identifier unik untuk setiap placement iklan
- **Transaction_Counter**: Penghitung jumlah transaksi penjualan yang telah dilakukan
- **Ad_Frequency_Manager**: Komponen yang mengatur frekuensi tampilan iklan
- **Revenue_Analytics**: Sistem pelacakan pendapatan dari iklan

## Requirements

### Requirement 1: AdMob SDK Integration

**User Story:** Sebagai developer, saya ingin mengintegrasikan AdMob SDK ke dalam aplikasi, sehingga aplikasi dapat menampilkan iklan dan menghasilkan revenue.

#### Acceptance Criteria

1. THE AdMob_SDK SHALL be integrated into the Android project with proper dependencies
2. WHEN the application starts, THE AdMob_SDK SHALL initialize successfully with valid App ID
3. THE application SHALL handle AdMob initialization failures gracefully without crashing
4. WHEN network is unavailable, THE application SHALL continue functioning normally without ads
5. THE AdMob_SDK SHALL comply with Google Play Store policies and GDPR requirements

### Requirement 2: Banner Advertisement Display

**User Story:** Sebagai pemilik aplikasi, saya ingin menampilkan banner ads di bagian bawah layar aktivitas utama, sehingga dapat menghasilkan revenue tanpa mengganggu workflow pengguna.

#### Acceptance Criteria

1. WHEN user opens MainActivity, THE system SHALL display a banner ad at the bottom of the screen
2. WHEN user opens SellActivity, THE system SHALL display a banner ad at the bottom of the screen
3. WHEN user opens StockActivity, THE system SHALL display a banner ad at the bottom of the screen
4. WHEN user opens ReportActivity, THE system SHALL display a banner ad at the bottom of the screen
5. THE banner ads SHALL NOT overlap with functional UI elements
6. WHEN banner ad fails to load, THE system SHALL hide the ad container and adjust layout accordingly
7. THE banner ads SHALL refresh automatically every 60 seconds when visible
8. WHEN user navigates between activities, THE banner ads SHALL load independently without blocking UI

### Requirement 3: Strategic Interstitial Advertisement

**User Story:** Sebagai pemilik aplikasi, saya ingin menampilkan interstitial ads pada momen yang tepat, sehingga dapat meningkatkan revenue tanpa mengganggu flow penjualan yang sedang berlangsung.

#### Acceptance Criteria

1. WHEN user navigates from MainActivity to ReportActivity, THE system SHALL display an interstitial ad
2. WHEN user opens StockActivity after being idle for more than 1 hour, THE system SHALL display an interstitial ad
3. WHEN user accesses backup/restore functionality, THE system SHALL display an interstitial ad before the operation
4. THE system SHALL NOT display interstitial ads during active selling process or product scanning
5. WHEN interstitial ad is displayed, THE system SHALL provide a clear close button after 5 seconds
6. THE system SHALL limit interstitial ads to maximum 3 times per hour per user
7. WHEN interstitial ad fails to load, THE system SHALL continue normal app flow without delay

### Requirement 4: Ad Frequency Management

**User Story:** Sebagai pengguna aplikasi, saya ingin iklan ditampilkan dengan frekuensi yang wajar, sehingga tidak mengganggu produktivitas saya dalam mengelola warung.

#### Acceptance Criteria

1. THE Ad_Frequency_Manager SHALL track the number of ads shown per user session
2. WHEN user has seen 3 interstitial ads in one hour, THE system SHALL pause interstitial ads for 30 minutes
3. THE system SHALL reset ad frequency counters every 24 hours
4. WHEN user performs critical operations (active selling, stock management), THE system SHALL suppress interstitial ads
5. THE system SHALL store ad frequency data locally using SharedPreferences
6. WHEN app is restarted, THE system SHALL restore previous ad frequency state

### Requirement 5: Revenue Analytics and Tracking

**User Story:** Sebagai pemilik aplikasi, saya ingin melacak performa iklan dan revenue yang dihasilkan, sehingga dapat mengoptimalkan strategi monetisasi.

#### Acceptance Criteria

1. THE Revenue_Analytics SHALL track banner ad impressions and click-through rates
2. THE system SHALL log interstitial ad completion rates and user interactions
3. WHEN ads generate revenue, THE system SHALL record earnings data locally
4. THE system SHALL provide daily, weekly, and monthly revenue summaries
5. THE analytics data SHALL be exportable in CSV format for external analysis
6. THE system SHALL track ad loading failures and success rates for optimization

### Requirement 6: User Experience Optimization

**User Story:** Sebagai pengguna aplikasi, saya ingin iklan terintegrasi dengan natural ke dalam aplikasi, sehingga tidak mengganggu pengalaman penggunaan aplikasi.

#### Acceptance Criteria

1. THE banner ads SHALL use consistent styling that matches the app's design theme
2. WHEN banner ads are loading, THE system SHALL show a subtle loading indicator
3. THE system SHALL preload interstitial ads in background to minimize loading delays
4. WHEN user is in the middle of a transaction, THE system SHALL defer all interstitial ads
5. THE ad containers SHALL have smooth animations when showing/hiding
6. THE system SHALL provide visual feedback when ads are successfully loaded

### Requirement 7: Error Handling and Fallbacks

**User Story:** Sebagai developer, saya ingin sistem iklan memiliki error handling yang robust, sehingga kegagalan iklan tidak mempengaruhi fungsionalitas utama aplikasi.

#### Acceptance Criteria

1. WHEN AdMob SDK fails to initialize, THE system SHALL log the error and continue without ads
2. WHEN banner ad fails to load, THE system SHALL retry loading after 30 seconds
3. WHEN interstitial ad fails to load, THE system SHALL skip the ad and continue normal flow
4. THE system SHALL implement exponential backoff for repeated ad loading failures
5. WHEN network connectivity is poor, THE system SHALL reduce ad refresh frequency
6. THE error handling SHALL never cause application crashes or ANR (Application Not Responding)

### Requirement 8: Privacy and Compliance

**User Story:** Sebagai pengguna aplikasi, saya ingin data pribadi saya dilindungi sesuai regulasi, sehingga merasa aman menggunakan aplikasi.

#### Acceptance Criteria

1. THE system SHALL implement GDPR consent mechanism for EU users
2. WHEN user is under 13 years old, THE system SHALL use child-safe ad targeting
3. THE system SHALL provide option to opt-out from personalized ads
4. THE ad implementation SHALL comply with Google Play Store advertising policies
5. THE system SHALL not collect or transmit sensitive user data for ad targeting
6. THE privacy policy SHALL clearly explain ad data usage and user rights