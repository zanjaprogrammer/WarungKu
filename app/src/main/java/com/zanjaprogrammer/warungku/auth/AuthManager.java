package com.zanjaprogrammer.warungku.auth;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.zanjaprogrammer.warungku.data.model.User;
import com.zanjaprogrammer.warungku.data.model.Warung;

public class AuthManager {
    private static final String TAG = "AuthManager";
    private static final String PREF_NAME = "WarungKuAuth";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_WARUNG_ID = "warung_id";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    private static AuthManager instance;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final SharedPreferences prefs;
    public User currentUser; // Public untuk akses dari LoginActivity
    public Warung currentWarung; // Public untuk akses dari LoginActivity

    private AuthManager(Application application) {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        prefs = application.getSharedPreferences(PREF_NAME, Application.MODE_PRIVATE);
    }

    public static AuthManager getInstance(Application application) {
        if (instance == null) {
            instance = new AuthManager(application);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return firebaseAuth.getCurrentUser() != null && currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Warung getCurrentWarung() {
        return currentWarung;
    }

    public String getCurrentUserId() {
        return currentUser != null ? currentUser.userId : null;
    }

    public String getCurrentWarungId() {
        return currentWarung != null ? currentWarung.warungId : null;
    }

    public String getCurrentUserRole() {
        return currentUser != null ? currentUser.role : null;
    }

    public void loadUserFromCache() {
        String userId = prefs.getString(KEY_USER_ID, null);
        String warungId = prefs.getString(KEY_WARUNG_ID, null);
        String role = prefs.getString(KEY_USER_ROLE, null);
        String name = prefs.getString(KEY_USER_NAME, null);
        String email = prefs.getString(KEY_USER_EMAIL, null);

        if (userId != null && role != null) {
            currentUser = new User();
            currentUser.userId = userId;
            currentUser.warungId = warungId;
            currentUser.role = role;
            currentUser.name = name;
            currentUser.email = email;

            if (warungId != null) {
                loadWarungFromFirestore(warungId, null);
            }
        }
    }

    public void loadUserFromFirestore(String userId, LoadUserCallback callback) {
        Log.d(TAG, "Loading user from Firestore: " + userId);
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "User document found in Firestore");
                    currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null) {
                        Log.d(TAG, "User parsed successfully: " + currentUser.email + ", Role: " + currentUser.role);
                        saveUserToCache();
                        
                        // Load warung data
                        if (currentUser.warungId != null) {
                            Log.d(TAG, "Loading warung: " + currentUser.warungId);
                            loadWarungFromFirestore(currentUser.warungId, callback);
                        } else if (callback != null) {
                            Log.w(TAG, "User has no warungId");
                            callback.onSuccess();
                        }
                    } else if (callback != null) {
                        Log.e(TAG, "Failed to parse user data from document");
                        callback.onError("Failed to parse user data");
                    }
                } else {
                    // User document tidak ada - coba auto-complete registration
                    Log.w(TAG, "User document does not exist in Firestore: " + userId);
                    Log.d(TAG, "Attempting to auto-complete registration...");
                    autoCompleteRegistration(userId, callback);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading user from Firestore", e);
                Log.e(TAG, "Error details: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }
    
    /**
     * Auto-complete registration untuk user yang sudah ada di Firebase Auth
     * tapi belum ada user document di Firestore
     */
    private void autoCompleteRegistration(String userId, LoadUserCallback callback) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null || !firebaseUser.getUid().equals(userId)) {
            Log.e(TAG, "Firebase Auth user not found or UID mismatch");
            if (callback != null) {
                callback.onError("User not found");
            }
            return;
        }
        
        String email = firebaseUser.getEmail();
        if (email == null || email.isEmpty()) {
            Log.e(TAG, "User email is null or empty");
            if (callback != null) {
                callback.onError("User email not found");
            }
            return;
        }
        
        Log.d(TAG, "Auto-completing registration for: " + email);
        
        // Create warung first
        String warungId = java.util.UUID.randomUUID().toString();
        String warungName = "Warung " + email.split("@")[0];
        
        Warung warung = new Warung(warungId, warungName, userId);
        
        // Save warung to Firestore
        firestore.collection("warungs").document(warungId)
            .set(warung)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Warung document created successfully: " + warungId);
                
                // Create user document
                User user = new User(
                    userId,
                    email,
                    email.split("@")[0],
                    "owner",
                    warungId
                );
                
                // Save user to Firestore
                firestore.collection("users").document(userId)
                    .set(user)
                    .addOnSuccessListener(aVoid2 -> {
                        Log.d(TAG, "User document created successfully: " + userId);
                        
                        // Set current user and warung
                        currentUser = user;
                        currentWarung = warung;
                        saveUserToCache();
                        
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error creating user document during auto-complete", e);
                        if (callback != null) {
                            callback.onError("Failed to create user document: " + e.getMessage());
                        }
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating warung document during auto-complete", e);
                if (callback != null) {
                    callback.onError("Failed to create warung document: " + e.getMessage());
                }
            });
    }

    private void loadWarungFromFirestore(String warungId, LoadUserCallback callback) {
        firestore.collection("warungs").document(warungId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentWarung = documentSnapshot.toObject(Warung.class);
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else if (callback != null) {
                    callback.onError("Warung not found");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading warung", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }

    public void saveUserToCache() {
        if (currentUser != null) {
            prefs.edit()
                .putString(KEY_USER_ID, currentUser.userId)
                .putString(KEY_WARUNG_ID, currentUser.warungId)
                .putString(KEY_USER_ROLE, currentUser.role)
                .putString(KEY_USER_NAME, currentUser.name)
                .putString(KEY_USER_EMAIL, currentUser.email)
                .apply();
        }
    }

    public void logout() {
        firebaseAuth.signOut();
        currentUser = null;
        currentWarung = null;
        prefs.edit().clear().apply();
    }

    public interface LoadUserCallback {
        void onSuccess();
        void onError(String error);
    }
}

