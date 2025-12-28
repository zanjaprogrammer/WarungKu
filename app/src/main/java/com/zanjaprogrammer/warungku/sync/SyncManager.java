package com.zanjaprogrammer.warungku.sync;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.zanjaprogrammer.warungku.auth.AuthManager;
import com.zanjaprogrammer.warungku.utils.NetworkUtils;

/**
 * Manager untuk trigger sync operations
 */
public class SyncManager {
    private static final String TAG = "SyncManager";
    
    /**
     * Trigger sync jika user logged in dan network available
     */
    public static void triggerSync(Context context) {
        android.app.Application app = (android.app.Application) context.getApplicationContext();
        AuthManager authManager = AuthManager.getInstance(app);
        
        if (!authManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping sync");
            return;
        }
        
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.d(TAG, "Network not available, skipping sync");
            return;
        }
        
        Log.d(TAG, "Triggering sync...");
        Intent syncIntent = new Intent(context, FirestoreSyncService.class);
        context.startService(syncIntent);
    }
}

