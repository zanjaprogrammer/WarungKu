package com.zanjaprogrammer.warungku;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.zanjaprogrammer.warungku.supabase.SupabaseAuthManager;
import com.zanjaprogrammer.warungku.supabase.SupabaseClient;
import com.zanjaprogrammer.warungku.supabase.api.SupabasePostgrestApi;
import com.zanjaprogrammer.warungku.data.model.User;
import com.zanjaprogrammer.warungku.data.model.Warung;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnRegister;
    private ProgressBar progressBar;
    private SupabaseAuthManager authManager;
    private SupabasePostgrestApi postgrestApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = SupabaseAuthManager.getInstance(getApplication());
        SupabaseClient supabaseClient = SupabaseClient.getInstance(getApplication());
        postgrestApi = supabaseClient.getPostgrestApi();

        initViews();
        setupListeners();

        // Check if already logged in
        if (authManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        // Try load from cache
        authManager.loadUserFromCache();
        if (authManager.isLoggedIn()) {
            // Load fresh data from Supabase
            authManager.loadUserFromSupabase(authManager.getCurrentUserId(), new SupabaseAuthManager.LoadUserCallback() {
                @Override
                public void onSuccess() {
                    navigateToMain();
                }

                @Override
                public void onError(String error) {
                    // Cache invalid, show login screen
                }
            });
        }
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> {
            Toast.makeText(this, "Fitur lupa password akan segera hadir", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        btnRegister.setOnClickListener(v -> performRegister());
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email tidak boleh kosong");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password tidak boleh kosong");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            return;
        }

        showLoading(true);
        
        authManager.login(email, password, new SupabaseAuthManager.LoginCallback() {
            @Override
            public void onSuccess() {
                showLoading(false);
                Log.d(TAG, "User logged in successfully");
                navigateToMain();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                // Error message sudah di-parse oleh SupabaseAuthManager
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Login error: " + error);
            }
        });
    }

    private void performRegister() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email tidak boleh kosong");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password tidak boleh kosong");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            return;
        }

        showLoading(true);

        // Check if email has pending invite via Supabase
        checkInviteAndRegister(email, password);
    }
    
    private void checkInviteAndRegister(String email, String password) {
        // Check invite via Supabase PostgREST API
        // Note: We need to use anon key for this check since user is not authenticated yet
        SupabaseClient supabaseClient = SupabaseClient.getInstance(getApplication());
        String apiKey = supabaseClient.getSupabaseKey();
        
        Call<List<Map<String, Object>>> call = postgrestApi.getInvites(
            apiKey,
            "Bearer " + apiKey, // Use anon key for unauthenticated access
            "eq." + email,
            "eq.pending",
            "*"
        );
        
        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Email has pending invite - register as employee
                    Map<String, Object> inviteData = response.body().get(0);
                    com.zanjaprogrammer.warungku.data.model.Invite invite = parseInvite(inviteData);
                    
                    if (invite != null && invite.isPending() && !invite.isExpired()) {
                        registerAsEmployee(email, password, invite);
                    } else if (invite != null && invite.isExpired()) {
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, "Invite sudah kadaluarsa. Silakan minta invite baru.", Toast.LENGTH_LONG).show();
                    } else {
                        // No valid invite - register as owner
                        registerAsOwner(email, password);
                    }
                } else {
                    // No invite found - register as owner
                    registerAsOwner(email, password);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "Error checking invite", t);
                // Continue with owner registration if invite check fails
                registerAsOwner(email, password);
            }
        });
    }
    
    private com.zanjaprogrammer.warungku.data.model.Invite parseInvite(Map<String, Object> data) {
        try {
            com.zanjaprogrammer.warungku.data.model.Invite invite = new com.zanjaprogrammer.warungku.data.model.Invite();
            invite.inviteId = (String) data.get("id");
            invite.warungId = data.get("warung_id") != null ? data.get("warung_id").toString() : null;
            invite.ownerId = data.get("owner_id") != null ? data.get("owner_id").toString() : null;
            invite.email = (String) data.get("email");
            invite.role = (String) data.get("role");
            invite.status = (String) data.get("status");
            
            Object createdAt = data.get("created_at");
            if (createdAt instanceof Number) {
                invite.createdAt = ((Number) createdAt).longValue();
            } else if (createdAt instanceof String) {
                // Try to parse if it's a string
                try {
                    invite.createdAt = Long.parseLong((String) createdAt);
                } catch (NumberFormatException e) {
                    invite.createdAt = System.currentTimeMillis();
                }
            } else {
                invite.createdAt = System.currentTimeMillis();
            }
            
            Object expiresAt = data.get("expires_at");
            if (expiresAt instanceof Number) {
                invite.expiresAt = ((Number) expiresAt).longValue();
            } else if (expiresAt instanceof String) {
                try {
                    invite.expiresAt = Long.parseLong((String) expiresAt);
                } catch (NumberFormatException e) {
                    invite.expiresAt = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000); // 7 days default
                }
            } else {
                invite.expiresAt = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000);
            }
            
            return invite;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing invite", e);
            return null;
        }
    }
    
    private void registerAsOwner(String email, String password) {
        String name = email.split("@")[0]; // Default name from email
        
        authManager.register(email, password, name, new SupabaseAuthManager.RegisterCallback() {
            @Override
            public void onSuccess() {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Registrasi berhasil! Menyinkronkan data lokal ke Supabase...", Toast.LENGTH_SHORT).show();
                
                // Sync data lokal ke Supabase setelah register berhasil
                syncLocalDataToSupabase();
                
                navigateToMain();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                // Error message sudah di-parse oleh SupabaseAuthManager
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Register error: " + error);
            }
        });
    }
    
    /**
     * Sync data lokal (SQLite) ke Supabase setelah user register
     */
    private void syncLocalDataToSupabase() {
        Log.d(TAG, "Starting sync of local data to Supabase after registration...");
        
        // Trigger sync service untuk sync semua data lokal ke Supabase
        com.zanjaprogrammer.warungku.sync.SyncManager.triggerSync(getApplication());
        
        Toast.makeText(this, "Data lokal sedang disinkronkan ke Supabase", Toast.LENGTH_SHORT).show();
    }
    
    private void registerAsEmployee(String email, String password, com.zanjaprogrammer.warungku.data.model.Invite invite) {
        String name = email.split("@")[0]; // Default name from email
        
        // Register with Supabase Auth
        authManager.register(email, password, name, new SupabaseAuthManager.RegisterCallback() {
            @Override
            public void onSuccess() {
                // After successful registration, update user role and warung_id
                String userId = authManager.getCurrentUserId();
                if (userId != null) {
                    updateUserAsEmployee(userId, invite);
                } else {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Error: User ID tidak ditemukan", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                // Error message sudah di-parse oleh SupabaseAuthManager
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Register error: " + error);
            }
        });
    }
    
    private void updateUserAsEmployee(String userId, com.zanjaprogrammer.warungku.data.model.Invite invite) {
        // Get warung data first
        String authHeader = "Bearer " + authManager.getAccessToken();
        SupabaseClient supabaseClient = SupabaseClient.getInstance(getApplication());
        
        Call<List<Map<String, Object>>> warungCall = postgrestApi.getWarungs(
            supabaseClient.getSupabaseKey(),
            authHeader,
            "eq." + invite.warungId,
            "*"
        );
        
        warungCall.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Map<String, Object> warungData = response.body().get(0);
                    Warung warung = new Warung();
                    warung.warungId = (String) warungData.get("id");
                    warung.name = (String) warungData.get("name");
                    warung.ownerId = warungData.get("owner_id") != null ? 
                        warungData.get("owner_id").toString() : null;
                    
                    // Update user with invite role and warung_id
                    Map<String, Object> userUpdate = new HashMap<>();
                    userUpdate.put("role", invite.role);
                    userUpdate.put("warung_id", invite.warungId);
                    
                    Call<List<Map<String, Object>>> userCall = postgrestApi.updateUser(
                        supabaseClient.getSupabaseKey(),
                        authHeader,
                        "return=representation",
                        "eq." + userId,
                        userUpdate
                    );
                    
                    userCall.enqueue(new Callback<List<Map<String, Object>>>() {
                        @Override
                        public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                // Update invite status
                                Map<String, Object> inviteUpdate = new HashMap<>();
                                inviteUpdate.put("status", "accepted");
                                inviteUpdate.put("accepted_at", System.currentTimeMillis());
                                inviteUpdate.put("accepted_by", userId);
                                
                                Call<List<Map<String, Object>>> inviteCall = postgrestApi.updateInvite(
                                    supabaseClient.getSupabaseKey(),
                                    authHeader,
                                    "return=representation",
                                    "eq." + invite.inviteId,
                                    inviteUpdate
                                );
                                
                                inviteCall.enqueue(new Callback<List<Map<String, Object>>>() {
                                    @Override
                                    public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                                        // Reload user data
                                        authManager.loadUserFromSupabase(userId, new SupabaseAuthManager.LoadUserCallback() {
                                            @Override
                                            public void onSuccess() {
                                                authManager.currentWarung = warung;
                                                authManager.saveUserToCache();
                                                
                                                showLoading(false);
                                                Toast.makeText(LoginActivity.this, "Registrasi berhasil! Selamat bergabung!", Toast.LENGTH_SHORT).show();
                                                navigateToMain();
                                            }

                                            @Override
                                            public void onError(String error) {
                                                showLoading(false);
                                                Toast.makeText(LoginActivity.this, "Error memuat data user: " + error, Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                                        Log.e(TAG, "Error updating invite status", t);
                                        // Continue anyway - user is registered
                                        authManager.currentWarung = warung;
                                        authManager.saveUserToCache();
                                        
                                        showLoading(false);
                                        Toast.makeText(LoginActivity.this, "Registrasi berhasil! Selamat bergabung!", Toast.LENGTH_SHORT).show();
                                        navigateToMain();
                                    }
                                });
                            } else {
                                showLoading(false);
                                Toast.makeText(LoginActivity.this, "Error mengupdate data user: " + response.code(), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                            showLoading(false);
                            Toast.makeText(LoginActivity.this, "Error mengupdate data user: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error updating user", t);
                        }
                    });
                } else {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Data warung tidak ditemukan", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Error memuat data warung: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error loading warung", t);
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnRegister.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

