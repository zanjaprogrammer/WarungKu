// Template untuk menambahkan Banner Ad ke Activity

// 1. Import yang diperlukan:
import com.zanjaprogrammer.warungku.ads.AdManager;
import android.widget.FrameLayout;
import android.util.Log;

// 2. Tambahkan field di class:
private AdManager adManager;

// 3. Panggil di onCreate() setelah setup lainnya:
initializeAdManager();

// 4. Tambahkan method ini di akhir class:
private void initializeAdManager() {
    try {
        adManager = AdManager.getInstance(this);
        adManager.initialize();
        
        // Load banner ad for [ActivityName]
        FrameLayout bannerContainer = findViewById(R.id.bannerAdContainer);
        if (adManager.isInitialized() && bannerContainer != null) {
            adManager.getBannerAdController().loadBannerAd(bannerContainer, "[ActivityName]");
        }
    } catch (Exception e) {
        Log.e("[ActivityName]", "Failed to initialize AdManager", e);
    }
}

// 5. Optional - Tambahkan lifecycle methods jika diperlukan:
@Override
protected void onDestroy() {
    super.onDestroy();
    
    // Destroy banner ads to free resources
    if (adManager != null && adManager.isInitialized()) {
        adManager.getBannerAdController().destroyBannerAd(findViewById(R.id.bannerAdContainer));
    }
}