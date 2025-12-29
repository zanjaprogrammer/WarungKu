package com.zanjaprogrammer.warungku.supabase;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.zanjaprogrammer.warungku.data.model.User;
import com.zanjaprogrammer.warungku.data.model.Warung;
import com.zanjaprogrammer.warungku.supabase.api.SupabaseAuthApi;
import com.zanjaprogrammer.warungku.supabase.api.SupabasePostgrestApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Supabase Authentication Manager
 * Menggantikan Firebase AuthManager dengan menggunakan Supabase REST API
 */
public class SupabaseAuthManager {
    private static final String TAG = "SupabaseAuthManager";
    private static final String PREF_NAME = "WarungKuAuth";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_WARUNG_ID = "warung_id";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    private static SupabaseAuthManager instance;
    private final SupabaseClient supabaseClient;
    private final SupabaseAuthApi authApi;
    private final SupabasePostgrestApi postgrestApi;
    private final SharedPreferences prefs;
    
    public User currentUser; // Public untuk akses dari LoginActivity
    public Warung currentWarung; // Public untuk akses dari LoginActivity
    private String accessToken;

    private SupabaseAuthManager(Application application) {
        supabaseClient = SupabaseClient.getInstance(application);
        authApi = supabaseClient.getAuthApi();
        postgrestApi = supabaseClient.getPostgrestApi();
        prefs = application.getSharedPreferences(PREF_NAME, Application.MODE_PRIVATE);
        loadAccessTokenFromCache();
    }

    public static SupabaseAuthManager getInstance(Application application) {
        if (instance == null) {
            instance = new SupabaseAuthManager(application);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return accessToken != null && currentUser != null;
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

    public String getAccessToken() {
        return accessToken;
    }

    private void loadAccessTokenFromCache() {
        accessToken = prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public void loadUserFromCache() {
        String userId = prefs.getString(KEY_USER_ID, null);
        String warungId = prefs.getString(KEY_WARUNG_ID, null);
        String role = prefs.getString(KEY_USER_ROLE, null);
        String name = prefs.getString(KEY_USER_NAME, null);
        String email = prefs.getString(KEY_USER_EMAIL, null);

        if (userId != null && role != null && accessToken != null) {
            currentUser = new User();
            currentUser.userId = userId;
            currentUser.warungId = warungId;
            currentUser.role = role;
            currentUser.name = name;
            currentUser.email = email;

            if (warungId != null) {
                loadWarungFromSupabase(warungId, null);
            }
        }
    }

    /**
     * Register new user
     */
    public void register(String email, String password, String name, RegisterCallback callback) {
        Log.d(TAG, "Registering user: " + email);
        
        SupabaseAuthApi.SignUpRequest request = new SupabaseAuthApi.SignUpRequest(
            email,
            password,
            new SupabaseAuthApi.SignUpData(name)
        );

        Call<SupabaseAuthApi.AuthResponse> call = authApi.signUp(
            supabaseClient.getSupabaseKey(),
            "application/json",
            request
        );

        call.enqueue(new Callback<SupabaseAuthApi.AuthResponse>() {
            @Override
            public void onResponse(Call<SupabaseAuthApi.AuthResponse> call, Response<SupabaseAuthApi.AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SupabaseAuthApi.AuthResponse authResponse = response.body();
                    accessToken = authResponse.access_token;
                    saveAccessTokenToCache(authResponse.access_token, authResponse.refresh_token);
                    
                    if (authResponse.user != null) {
                        String userId = authResponse.user.id;
                        Log.d(TAG, "User registered successfully: " + userId);
                        
                        // Create user and warung documents in Supabase
                        createUserAndWarungInSupabase(userId, email, name, callback);
                    } else if (accessToken != null) {
                        // User object is null but we have access token
                        // This can happen when email confirmation is enabled or user already exists
                        // Try to get user info via getUser API
                        Log.d(TAG, "User object is null, trying to get user info via API");
                        getUserInfoAfterSignup(email, name, callback);
                    } else {
                        // User object is null and no access token
                        // This happens when email confirmation is ON and user needs to confirm email first
                        // Or user already exists but email not confirmed
                        Log.w(TAG, "User object and access token are null - email confirmation may be required");
                        
                        // Check if user already exists (try to login instead)
                        if (callback != null) {
                            callback.onError("Email mungkin sudah terdaftar atau perlu konfirmasi. Silakan coba login atau cek email untuk konfirmasi.");
                        }
                    }
                } else {
                    String errorMsg = "Registration failed";
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            errorMsg = parseErrorMessage(errorBody);
                            Log.e(TAG, "Registration failed: " + errorBody);
                        } catch (Exception e) {
                            errorMsg = "Error code: " + response.code();
                        }
                    }
                    if (callback != null) {
                        callback.onError(errorMsg);
                    }
                }
            }

            @Override
            public void onFailure(Call<SupabaseAuthApi.AuthResponse> call, Throwable t) {
                Log.e(TAG, "Registration network error", t);
                if (callback != null) {
                    callback.onError("Network error: " + t.getMessage());
                }
            }
        });
    }

    /**
     * Login user
     */
    public void login(String email, String password, LoginCallback callback) {
        Log.d(TAG, "Logging in user: " + email);
        
        SupabaseAuthApi.SignInRequest request = new SupabaseAuthApi.SignInRequest(email, password);

        Call<SupabaseAuthApi.AuthResponse> call = authApi.signIn(
            supabaseClient.getSupabaseKey(),
            "application/json",
            request
        );

        call.enqueue(new Callback<SupabaseAuthApi.AuthResponse>() {
            @Override
            public void onResponse(Call<SupabaseAuthApi.AuthResponse> call, Response<SupabaseAuthApi.AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SupabaseAuthApi.AuthResponse authResponse = response.body();
                    accessToken = authResponse.access_token;
                    saveAccessTokenToCache(authResponse.access_token, authResponse.refresh_token);
                    
                    if (authResponse.user != null) {
                        String userId = authResponse.user.id;
                        Log.d(TAG, "User logged in successfully: " + userId);
                        
                        // Load user data from Supabase
                        loadUserFromSupabase(userId, new LoadUserCallback() {
                            @Override
                            public void onSuccess() {
                                if (callback != null) {
                                    callback.onSuccess();
                                }
                            }

                            @Override
                            public void onError(String error) {
                                if (callback != null) {
                                    callback.onError(error);
                                }
                            }
                        });
                    } else {
                        Log.e(TAG, "User object is null in response");
                        if (callback != null) {
                            callback.onError("Login failed: User object is null");
                        }
                    }
                } else {
                    String errorMsg = "Login failed";
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            errorMsg = parseErrorMessage(errorBody);
                            Log.e(TAG, "Login failed: " + errorBody);
                        } catch (Exception e) {
                            errorMsg = "Error code: " + response.code();
                        }
                    }
                    if (callback != null) {
                        callback.onError(errorMsg);
                    }
                }
            }

            @Override
            public void onFailure(Call<SupabaseAuthApi.AuthResponse> call, Throwable t) {
                Log.e(TAG, "Login network error", t);
                if (callback != null) {
                    callback.onError("Network error: " + t.getMessage());
                }
            }
        });
    }

    /**
     * Create user and warung documents in Supabase after registration
     * FIXED: Create user FIRST, then warung, then update user with warung_id
     * This avoids foreign key constraint violation (warung.owner_id references users.id)
     */
    private void createUserAndWarungInSupabase(String userId, String email, String name, RegisterCallback callback) {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("/Users/ekowibowo/KahfiDev/WarungKu/.cursor/debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\",\"location\":\"SupabaseAuthManager.java:270\",\"message\":\"createUserAndWarungInSupabase ENTRY\",\"data\":{\"userId\":\"" + userId + "\",\"email\":\"" + email + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        
        // Check if user already exists before inserting
        String authHeader = "Bearer " + accessToken;
        Call<List<Map<String, Object>>> checkUserCall = postgrestApi.getUsers(
            supabaseClient.getSupabaseKey(),
            authHeader,
            "eq." + userId,
            null,
            "id,warung_id"
        );
        
        checkUserCall.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("/Users/ekowibowo/KahfiDev/WarungKu/.cursor/debug.log", true);
                    String errorBodyStr = null;
                    if (!response.isSuccessful() && response.errorBody() != null) {
                        try {
                            errorBodyStr = response.errorBody().string();
                        } catch (Exception e) {}
                    }
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\",\"location\":\"SupabaseAuthManager.java:checkUser\",\"message\":\"Check user exists response\",\"data\":{\"code\":" + response.code() + ",\"success\":" + response.isSuccessful() + ",\"hasBody\":" + (response.body() != null) + ",\"bodySize\":" + (response.body() != null ? response.body().size() : 0) + ",\"errorBody\":" + (errorBodyStr != null ? "\"" + errorBodyStr.replace("\"", "\\\"") + "\"" : "null") + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion
                
                boolean userExists = response.isSuccessful() && response.body() != null && !response.body().isEmpty();
                String existingWarungId = null;
                if (userExists) {
                    Map<String, Object> existingUser = response.body().get(0);
                    existingWarungId = existingUser.get("warung_id") != null ? existingUser.get("warung_id").toString() : null;
                }
                
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("/Users/ekowibowo/KahfiDev/WarungKu/.cursor/debug.log", true);
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\",\"location\":\"SupabaseAuthManager.java:checkUser\",\"message\":\"User exists check result\",\"data\":{\"userExists\":" + userExists + ",\"existingWarungId\":" + (existingWarungId != null ? "\"" + existingWarungId + "\"" : "null") + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion
                
                if (userExists && existingWarungId != null && !existingWarungId.isEmpty()) {
                    // User already exists with warung_id - just load it
                    Log.d(TAG, "User already exists with warung_id: " + existingWarungId);
                    currentUser = new User(userId, email, name, "owner", existingWarungId);
                    loadWarungFromSupabase(existingWarungId, new LoadUserCallback() {
                        @Override
                        public void onSuccess() {
                            saveUserToCache();
                            if (callback != null) {
                                callback.onSuccess();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            if (callback != null) {
                                callback.onError(error);
                            }
                        }
                    });
                    return;
                }
                
                // User doesn't exist or doesn't have warung_id - proceed with creation
                if (userExists) {
                    // User exists but no warung_id - just create warung and update user
                    createWarungAndUpdateUser(userId, email, name, callback);
                } else {
                    // User doesn't exist - create user first, then warung
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", userId);
                    user.put("email", email);
                    user.put("name", name);
                    user.put("role", "owner");
                    user.put("warung_id", null); // Will be updated after warung is created
                    user.put("is_active", true);
                    
                    Call<List<Map<String, Object>>> userCall = postgrestApi.insertUser(
                        supabaseClient.getSupabaseKey(),
                        authHeader,
                        "return=representation",
                        user
                    );
                    
                    // #region agent log
                    try {
                        java.io.FileWriter fw = new java.io.FileWriter("/Users/ekowibowo/KahfiDev/WarungKu/.cursor/debug.log", true);
                        fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\",\"location\":\"SupabaseAuthManager.java:insertUser\",\"message\":\"Attempting INSERT user\",\"data\":{\"userId\":\"" + userId + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                        fw.close();
                    } catch (Exception e) {}
                    // #endregion
                    
                    userCall.enqueue(new Callback<List<Map<String, Object>>>() {
                        @Override
                        public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                            // #region agent log
                            try {
                                java.io.FileWriter fw = new java.io.FileWriter("/Users/ekowibowo/KahfiDev/WarungKu/.cursor/debug.log", true);
                                String errorBody = null;
                                if (response.errorBody() != null) {
                                    try {
                                        errorBody = response.errorBody().string();
                                    } catch (Exception e) {}
                                }
                                fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\",\"location\":\"SupabaseAuthManager.java:insertUser\",\"message\":\"INSERT user response\",\"data\":{\"success\":" + response.isSuccessful() + ",\"code\":" + response.code() + ",\"errorBody\":" + (errorBody != null ? "\"" + errorBody.replace("\"", "\\\"") + "\"" : "null") + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                                fw.close();
                            } catch (Exception e) {}
                            // #endregion
                            
                            if (response.isSuccessful()) {
                                Log.d(TAG, "User document created successfully: " + userId);
                                createWarungAndUpdateUser(userId, email, name, callback);
                            } else {
                                String errorMsg = "Failed to create user document";
                                if (response.errorBody() != null) {
                                    try {
                                        errorMsg = response.errorBody().string();
                                    } catch (Exception e) {
                                        errorMsg = "Error code: " + response.code();
                                    }
                                }
                                Log.e(TAG, "Failed to create user document: " + errorMsg);
                                // If duplicate key error, user might have been created between check and insert
                                if (errorMsg.contains("23505") || errorMsg.contains("duplicate")) {
                                    // Try to create warung anyway
                                    createWarungAndUpdateUser(userId, email, name, callback);
                                } else if (callback != null) {
                                    callback.onError(errorMsg);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                            Log.e(TAG, "Network error creating user document", t);
                            if (callback != null) {
                                callback.onError("Network error: " + t.getMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("/Users/ekowibowo/KahfiDev/WarungKu/.cursor/debug.log", true);
                    String errorMsg = t.getMessage() != null ? t.getMessage().replace("\"", "\\\"") : "null";
                    String errorClass = t.getClass().getName();
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\",\"location\":\"SupabaseAuthManager.java:checkUser\",\"message\":\"Check user FAILED\",\"data\":{\"error\":\"" + errorMsg + "\",\"errorClass\":\"" + errorClass + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion
                
                Log.e(TAG, "Error checking if user exists", t);
                // Continue with creation attempt anyway
                Map<String, Object> user = new HashMap<>();
                user.put("id", userId);
                user.put("email", email);
                user.put("name", name);
                user.put("role", "owner");
                user.put("warung_id", null);
                user.put("is_active", true);
                
                Call<List<Map<String, Object>>> userCall = postgrestApi.insertUser(
                    supabaseClient.getSupabaseKey(),
                    authHeader,
                    "return=representation",
                    user
                );
                
                userCall.enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Log.d(TAG, "User document created successfully: " + userId);
                            createWarungAndUpdateUser(userId, email, name, callback);
                        } else {
                            String errorMsg = "Failed to create user document";
                            if (response.errorBody() != null) {
                                try {
                                    errorMsg = response.errorBody().string();
                                } catch (Exception e) {
                                    errorMsg = "Error code: " + response.code();
                                }
                            }
                            Log.e(TAG, "Failed to create user document: " + errorMsg);
                            if (errorMsg.contains("23505") || errorMsg.contains("duplicate")) {
                                createWarungAndUpdateUser(userId, email, name, callback);
                            } else if (callback != null) {
                                callback.onError(errorMsg);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "Network error creating user document", t);
                        if (callback != null) {
                            callback.onError("Network error: " + t.getMessage());
                        }
                    }
                });
            }
        });
    }
    
    /**
     * Helper method to create warung and update user with warung_id
     */
    private void createWarungAndUpdateUser(String userId, String email, String name, RegisterCallback callback) {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("/Users/ekowibowo/KahfiDev/WarungKu/.cursor/debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"SupabaseAuthManager.java:createWarungAndUpdateUser\",\"message\":\"createWarungAndUpdateUser ENTRY\",\"data\":{\"userId\":\"" + userId + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        
        String authHeader = "Bearer " + accessToken;
        String warungId = UUID.randomUUID().toString();
        String warungName = "Warung " + email.split("@")[0];
        
        Map<String, Object> warung = new HashMap<>();
        warung.put("id", warungId);
        warung.put("name", warungName);
        warung.put("owner_id", userId);
        
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("/Users/ekowibowo/KahfiDev/WarungKu/.cursor/debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"SupabaseAuthManager.java:insertWarung\",\"message\":\"Attempting INSERT warung\",\"data\":{\"warungId\":\"" + warungId + "\",\"ownerId\":\"" + userId + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        
        Call<List<Map<String, Object>>> warungCall = postgrestApi.insertWarung(
            supabaseClient.getSupabaseKey(),
            authHeader,
            "return=representation",
            warung
        );
        
        warungCall.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("/Users/ekowibowo/KahfiDev/WarungKu/.cursor/debug.log", true);
                    String errorBody = null;
                    if (response.errorBody() != null) {
                        try {
                            errorBody = response.errorBody().string();
                        } catch (Exception e) {}
                    }
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"SupabaseAuthManager.java:insertWarung\",\"message\":\"INSERT warung response\",\"data\":{\"success\":" + response.isSuccessful() + ",\"code\":" + response.code() + ",\"errorBody\":" + (errorBody != null ? "\"" + errorBody.replace("\"", "\\\"") + "\"" : "null") + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion
                
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Log.d(TAG, "Warung created successfully: " + warungId);
                    
                    // Update user with warung_id
                    Map<String, Object> userUpdate = new HashMap<>();
                    userUpdate.put("warung_id", warungId);
                    
                    // #region agent log
                    try {
                        java.io.FileWriter fw = new java.io.FileWriter("/Users/ekowibowo/KahfiDev/WarungKu/.cursor/debug.log", true);
                        fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"D\",\"location\":\"SupabaseAuthManager.java:updateUser\",\"message\":\"Attempting UPDATE user with warung_id\",\"data\":{\"userId\":\"" + userId + "\",\"warungId\":\"" + warungId + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                        fw.close();
                    } catch (Exception e) {}
                    // #endregion
                    
                    Call<List<Map<String, Object>>> updateCall = postgrestApi.updateUser(
                        supabaseClient.getSupabaseKey(),
                        authHeader,
                        "return=representation",
                        "eq." + userId,
                        userUpdate
                    );
                    
                    updateCall.enqueue(new Callback<List<Map<String, Object>>>() {
                        @Override
                        public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                            // #region agent log
                            try {
                                java.io.FileWriter fw = new java.io.FileWriter("/Users/ekowibowo/KahfiDev/WarungKu/.cursor/debug.log", true);
                                String errorBody = null;
                                if (response.errorBody() != null) {
                                    try {
                                        errorBody = response.errorBody().string();
                                    } catch (Exception e) {}
                                }
                                fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"D\",\"location\":\"SupabaseAuthManager.java:updateUser\",\"message\":\"UPDATE user response\",\"data\":{\"success\":" + response.isSuccessful() + ",\"code\":" + response.code() + ",\"errorBody\":" + (errorBody != null ? "\"" + errorBody.replace("\"", "\\\"") + "\"" : "null") + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                                fw.close();
                            } catch (Exception e) {}
                            // #endregion
                            
                            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                Log.d(TAG, "User updated with warung_id: " + warungId);
                                
                                // Set current user and warung
                                currentUser = new User(userId, email, name, "owner", warungId);
                                currentWarung = new Warung(warungId, warungName, userId);
                                saveUserToCache();
                                
                                if (callback != null) {
                                    callback.onSuccess();
                                }
                            } else {
                                String errorMsg = "Failed to update user with warung_id";
                                if (response.errorBody() != null) {
                                    try {
                                        errorMsg = response.errorBody().string();
                                    } catch (Exception e) {
                                        errorMsg = "Error code: " + response.code();
                                    }
                                }
                                Log.e(TAG, "Failed to update user: " + errorMsg);
                                // Still consider it success since user and warung are created
                                currentUser = new User(userId, email, name, "owner", warungId);
                                currentWarung = new Warung(warungId, warungName, userId);
                                saveUserToCache();
                                if (callback != null) {
                                    callback.onSuccess();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                            Log.e(TAG, "Network error updating user", t);
                            // Still consider it success since user and warung are created
                            currentUser = new User(userId, email, name, "owner", warungId);
                            currentWarung = new Warung(warungId, warungName, userId);
                            saveUserToCache();
                            if (callback != null) {
                                callback.onSuccess();
                            }
                        }
                    });
                } else {
                    String errorMsg = "Failed to create warung";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            errorMsg = "Error code: " + response.code();
                        }
                    }
                    Log.e(TAG, "Failed to create warung: " + errorMsg);
                    // #region agent log
                    try {
                        java.io.FileWriter fw = new java.io.FileWriter("/Users/ekowibowo/KahfiDev/WarungKu/.cursor/debug.log", true);
                        fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"SupabaseAuthManager.java:insertWarung\",\"message\":\"INSERT warung FAILED\",\"data\":{\"errorMsg\":\"" + errorMsg.replace("\"", "\\\"") + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                        fw.close();
                    } catch (Exception e) {}
                    // #endregion
                    
                    if (callback != null) {
                        callback.onError(errorMsg);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "Network error creating warung", t);
                if (callback != null) {
                    callback.onError("Network error: " + t.getMessage());
                }
            }
        });
    }

    /**
     * Load user data from Supabase
     */
    public void loadUserFromSupabase(String userId, LoadUserCallback callback) {
        Log.d(TAG, "Loading user from Supabase: " + userId);
        
        if (accessToken == null) {
            Log.e(TAG, "Access token is null");
            if (callback != null) {
                callback.onError("Not authenticated");
            }
            return;
        }
        
        String authHeader = "Bearer " + accessToken;
        Call<List<Map<String, Object>>> call = postgrestApi.getUsers(
            supabaseClient.getSupabaseKey(),
            authHeader,
            "eq." + userId, // id filter
            null, // warung_id filter
            "*"
        );
        
        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Map<String, Object> userData = response.body().get(0);
                    Log.d(TAG, "User data loaded successfully");
                    
                    currentUser = new User();
                    currentUser.userId = (String) userData.get("id");
                    currentUser.email = (String) userData.get("email");
                    currentUser.name = (String) userData.get("name");
                    currentUser.role = (String) userData.get("role");
                    currentUser.warungId = userData.get("warung_id") != null ? 
                        userData.get("warung_id").toString() : null;
                    
                    saveUserToCache();
                    
                    // Load warung data
                    if (currentUser.warungId != null && !currentUser.warungId.isEmpty()) {
                        Log.d(TAG, "Loading warung: " + currentUser.warungId);
                        loadWarungFromSupabase(currentUser.warungId, callback);
                    } else {
                        Log.w(TAG, "User has no warungId - triggering auto-complete registration");
                        // User doesn't have warung_id - try to auto-complete registration
                        // This can happen if user was created before warung creation was fixed
                        autoCompleteRegistration(userId, callback);
                    }
                } else {
                    Log.w(TAG, "User document does not exist in Supabase: " + userId);
                    // Try auto-complete registration
                    autoCompleteRegistration(userId, callback);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("/Users/ekowibowo/KahfiDev/WarungKu/.cursor/debug.log", true);
                    String errorMsg = t.getMessage() != null ? t.getMessage().replace("\"", "\\\"") : "null";
                    String errorClass = t.getClass().getName();
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"F\",\"location\":\"SupabaseAuthManager.java:loadUserFromSupabase\",\"message\":\"loadUserFromSupabase FAILED\",\"data\":{\"error\":\"" + errorMsg + "\",\"errorClass\":\"" + errorClass + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion
                
                Log.e(TAG, "Error loading user from Supabase", t);
                if (callback != null) {
                    callback.onError(t.getMessage());
                }
            }
        });
    }

    /**
     * Get user info after signup when user object is null in response
     */
    private void getUserInfoAfterSignup(String email, String name, RegisterCallback callback) {
        if (accessToken == null) {
            if (callback != null) {
                callback.onError("Access token is null");
            }
            return;
        }
        
        String authHeader = "Bearer " + accessToken;
        Call<SupabaseAuthApi.UserResponse> call = authApi.getUser(
            supabaseClient.getSupabaseKey(),
            authHeader
        );
        
        call.enqueue(new Callback<SupabaseAuthApi.UserResponse>() {
            @Override
            public void onResponse(Call<SupabaseAuthApi.UserResponse> call, Response<SupabaseAuthApi.UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SupabaseAuthApi.UserResponse userResponse = response.body();
                    String userId = userResponse.id;
                    Log.d(TAG, "Got user info after signup: " + userId);
                    
                    // Check if user already has user document in database
                    loadUserFromSupabase(userId, new LoadUserCallback() {
                        @Override
                        public void onSuccess() {
                            // User already has document, registration complete
                            if (callback != null) {
                                callback.onSuccess();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            // User document doesn't exist, create it
                            Log.d(TAG, "User document not found, creating new one");
                            createUserAndWarungInSupabase(userId, email, name, callback);
                        }
                    });
                } else {
                    Log.e(TAG, "Failed to get user info after signup");
                    if (callback != null) {
                        callback.onError("Failed to get user information after registration");
                    }
                }
            }

            @Override
            public void onFailure(Call<SupabaseAuthApi.UserResponse> call, Throwable t) {
                Log.e(TAG, "Network error getting user info after signup", t);
                if (callback != null) {
                    callback.onError("Network error: " + t.getMessage());
                }
            }
        });
    }
    
    /**
     * Auto-complete registration untuk user yang sudah ada di Supabase Auth
     * tapi belum ada user document di database
     */
    private void autoCompleteRegistration(String userId, LoadUserCallback callback) {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("/Users/ekowibowo/KahfiDev/WarungKu/.cursor/debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"E\",\"location\":\"SupabaseAuthManager.java:555\",\"message\":\"autoCompleteRegistration ENTRY\",\"data\":{\"userId\":\"" + userId + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        
        // Get user email from auth API
        String authHeader = "Bearer " + accessToken;
        Call<SupabaseAuthApi.UserResponse> call = authApi.getUser(
            supabaseClient.getSupabaseKey(),
            authHeader
        );
        
        call.enqueue(new Callback<SupabaseAuthApi.UserResponse>() {
            @Override
            public void onResponse(Call<SupabaseAuthApi.UserResponse> call, Response<SupabaseAuthApi.UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SupabaseAuthApi.UserResponse userResponse = response.body();
                    String email = userResponse.email;
                    String name = userResponse.user_metadata != null ? 
                        userResponse.user_metadata.name : email.split("@")[0];
                    
                    Log.d(TAG, "Auto-completing registration for: " + email);
                    
                    // Create user and warung (same as registration)
                    createUserAndWarungInSupabase(userId, email, name, new RegisterCallback() {
                        @Override
                        public void onSuccess() {
                            if (callback != null) {
                                callback.onSuccess();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            if (callback != null) {
                                callback.onError(error);
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "Failed to get user from auth API");
                    if (callback != null) {
                        callback.onError("User not found");
                    }
                }
            }

            @Override
            public void onFailure(Call<SupabaseAuthApi.UserResponse> call, Throwable t) {
                Log.e(TAG, "Network error getting user from auth API", t);
                if (callback != null) {
                    callback.onError("Network error: " + t.getMessage());
                }
            }
        });
    }

    private void loadWarungFromSupabase(String warungId, LoadUserCallback callback) {
        if (accessToken == null) {
            if (callback != null) {
                callback.onError("Not authenticated");
            }
            return;
        }
        
        String authHeader = "Bearer " + accessToken;
        Call<List<Map<String, Object>>> call = postgrestApi.getWarungs(
            supabaseClient.getSupabaseKey(),
            authHeader,
            "eq." + warungId,
            "*"
        );
        
        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("/Users/ekowibowo/KahfiDev/WarungKu/.cursor/debug.log", true);
                    String errorBodyStr = null;
                    if (!response.isSuccessful() && response.errorBody() != null) {
                        try {
                            errorBodyStr = response.errorBody().string();
                        } catch (Exception e) {}
                    }
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"G\",\"location\":\"SupabaseAuthManager.java:loadWarungFromSupabase\",\"message\":\"loadWarungFromSupabase response\",\"data\":{\"code\":" + response.code() + ",\"success\":" + response.isSuccessful() + ",\"hasBody\":" + (response.body() != null) + ",\"bodySize\":" + (response.body() != null ? response.body().size() : 0) + ",\"errorBody\":" + (errorBodyStr != null ? "\"" + errorBodyStr.replace("\"", "\\\"") + "\"" : "null") + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion
                
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Map<String, Object> warungData = response.body().get(0);
                    currentWarung = new Warung();
                    currentWarung.warungId = (String) warungData.get("id");
                    currentWarung.name = (String) warungData.get("name");
                    currentWarung.ownerId = warungData.get("owner_id") != null ? 
                        warungData.get("owner_id").toString() : null;
                    
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else if (callback != null) {
                    callback.onError("Warung not found");
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("/Users/ekowibowo/KahfiDev/WarungKu/.cursor/debug.log", true);
                    String errorMsg = t.getMessage() != null ? t.getMessage().replace("\"", "\\\"") : "null";
                    String errorClass = t.getClass().getName();
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"G\",\"location\":\"SupabaseAuthManager.java:loadWarungFromSupabase\",\"message\":\"loadWarungFromSupabase FAILED\",\"data\":{\"error\":\"" + errorMsg + "\",\"errorClass\":\"" + errorClass + "\",\"warungId\":\"" + warungId + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion
                
                Log.e(TAG, "Error loading warung", t);
                if (callback != null) {
                    callback.onError(t.getMessage());
                }
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

    private void saveAccessTokenToCache(String accessToken, String refreshToken) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply();
    }

    public void logout() {
        if (accessToken != null) {
            String authHeader = "Bearer " + accessToken;
            Call<Void> call = authApi.signOut(
                supabaseClient.getSupabaseKey(),
                authHeader
            );
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d(TAG, "Logged out successfully");
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Error logging out", t);
                }
            });
        }
        
        accessToken = null;
        currentUser = null;
        currentWarung = null;
        prefs.edit().clear().apply();
    }

    public interface RegisterCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface LoginCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface LoadUserCallback {
        void onSuccess();
        void onError(String error);
    }
    
    /**
     * Parse error message from Supabase response
     */
    private String parseErrorMessage(String errorBody) {
        try {
            // Try to parse JSON error
            if (errorBody.contains("email_not_confirmed")) {
                return "Email belum dikonfirmasi. Silakan cek email Anda untuk konfirmasi, atau tunggu beberapa saat dan coba lagi.";
            } else if (errorBody.contains("over_email_send_rate_limit")) {
                return "Terlalu banyak percobaan. Silakan tunggu beberapa menit sebelum mencoba lagi, atau gunakan email yang berbeda.";
            } else if (errorBody.contains("Invalid login")) {
                return "Email atau password salah. Silakan coba lagi.";
            } else if (errorBody.contains("User already registered")) {
                return "Email sudah terdaftar. Silakan login atau gunakan email lain.";
            } else if (errorBody.contains("Password")) {
                return "Password terlalu lemah. Minimal 6 karakter.";
            }
            
            // Try to extract message from JSON
            if (errorBody.contains("\"msg\"")) {
                int msgStart = errorBody.indexOf("\"msg\"");
                int colonIndex = errorBody.indexOf(":", msgStart);
                int quoteStart = errorBody.indexOf("\"", colonIndex) + 1;
                int quoteEnd = errorBody.indexOf("\"", quoteStart);
                if (quoteEnd > quoteStart) {
                    return errorBody.substring(quoteStart, quoteEnd);
                }
            }
            
            return errorBody;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error message", e);
            return errorBody;
        }
    }
}

