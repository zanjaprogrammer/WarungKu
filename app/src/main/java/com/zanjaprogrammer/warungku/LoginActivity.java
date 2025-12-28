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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zanjaprogrammer.warungku.auth.AuthManager;
import com.zanjaprogrammer.warungku.data.model.User;
import com.zanjaprogrammer.warungku.data.model.Warung;
import java.util.UUID;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnRegister;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        authManager = AuthManager.getInstance(getApplication());

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
            // Load fresh data from Firestore
            authManager.loadUserFromFirestore(authManager.getCurrentUserId(), new AuthManager.LoadUserCallback() {
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
        
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser firebaseUser = authResult.getUser();
                if (firebaseUser != null) {
                    // Load user data from Firestore
                    Log.d(TAG, "Loading user from Firestore: " + firebaseUser.getUid());
                    authManager.loadUserFromFirestore(firebaseUser.getUid(), new AuthManager.LoadUserCallback() {
                        @Override
                        public void onSuccess() {
                            showLoading(false);
                            Log.d(TAG, "User loaded successfully");
                            navigateToMain();
                        }

                        @Override
                        public void onError(String error) {
                            showLoading(false);
                            String errorMsg = "Error: " + error;
                            Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error loading user: " + error);
                            Log.e(TAG, "User ID: " + firebaseUser.getUid());
                            Log.e(TAG, "Firebase Auth user exists: " + (firebaseUser != null));
                            
                            // If user not found, suggest to register
                            if (error.contains("not found")) {
                                Toast.makeText(LoginActivity.this, 
                                    "User tidak ditemukan. Silakan daftar terlebih dahulu.", 
                                    Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Login gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Login error", e);
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

        // Check if email has pending invite OR if invite code is provided
        // For now, we'll check by email. Invite code can be added later via deep link
        firestore.collection("invites")
            .whereEqualTo("email", email)
            .whereEqualTo("status", "pending")
            .limit(1)
            .get()
            .addOnSuccessListener(inviteQuery -> {
                if (!inviteQuery.isEmpty()) {
                    // Email has pending invite - register as employee
                    com.zanjaprogrammer.warungku.data.model.Invite invite = 
                        inviteQuery.getDocuments().get(0).toObject(com.zanjaprogrammer.warungku.data.model.Invite.class);
                    
                    if (invite != null && invite.isPending() && !invite.isExpired()) {
                        registerAsEmployee(email, password, invite);
                        return;
                    } else if (invite != null && invite.isExpired()) {
                        Toast.makeText(this, "Invite sudah kadaluarsa. Silakan minta invite baru.", Toast.LENGTH_LONG).show();
                        showLoading(false);
                        return;
                    }
                }
                
                // No invite found - register as owner
                registerAsOwner(email, password);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error checking invite", e);
                // Continue with owner registration if invite check fails
                registerAsOwner(email, password);
            });
    }
    
    private void registerAsOwner(String email, String password) {
        // Register with Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser firebaseUser = authResult.getUser();
                if (firebaseUser != null) {
                    // Create warung first
                    String warungId = UUID.randomUUID().toString();
                    String warungName = "Warung " + email.split("@")[0]; // Default name
                    
                    Warung warung = new Warung(warungId, warungName, firebaseUser.getUid());
                    
                    // Save warung to Firestore
                    Log.d(TAG, "Creating warung: " + warungId);
                    firestore.collection("warungs").document(warungId)
                        .set(warung)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Warung document created successfully: " + warungId);
                            // Create user document
                            User user = new User(
                                firebaseUser.getUid(),
                                email,
                                warungName.split(" ")[1], // Use email username as name
                                "owner",
                                warungId
                            );

                            // Save user to Firestore
                            firestore.collection("users").document(firebaseUser.getUid())
                                .set(user)
                                .addOnSuccessListener(aVoid2 -> {
                                    Log.d(TAG, "User document created successfully: " + firebaseUser.getUid());
                                    Log.d(TAG, "User data: " + user.userId + ", " + user.email + ", " + user.role + ", " + user.warungId);
                                    
                                    // Set current user and warung
                                    authManager.currentUser = user;
                                    authManager.currentWarung = warung;
                                    authManager.saveUserToCache();
                                    
                                    showLoading(false);
                                    Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show();
                                    navigateToMain();
                                })
                                .addOnFailureListener(e -> {
                                    showLoading(false);
                                    String errorMsg = "Error membuat user: " + e.getMessage();
                                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "Error creating user document", e);
                                    Log.e(TAG, "User ID: " + firebaseUser.getUid());
                                    Log.e(TAG, "Warung ID: " + warungId);
                                    
                                    // Sign out user jika gagal create user document
                                    firebaseUser.delete().addOnCompleteListener(task -> {
                                        Log.d(TAG, "Cleaned up Firebase Auth user after failed user document creation");
                                    });
                                });
                        })
                        .addOnFailureListener(e -> {
                            showLoading(false);
                            Toast.makeText(this, "Error membuat warung: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error creating warung", e);
                        });
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Registrasi gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Register error", e);
            });
    }
    
    private void registerAsEmployee(String email, String password, com.zanjaprogrammer.warungku.data.model.Invite invite) {
        // Register with Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser firebaseUser = authResult.getUser();
                if (firebaseUser != null) {
                    // Get warung data
                    firestore.collection("warungs").document(invite.warungId)
                        .get()
                        .addOnSuccessListener(warungSnapshot -> {
                            if (!warungSnapshot.exists()) {
                                showLoading(false);
                                Toast.makeText(this, "Data warung tidak ditemukan", Toast.LENGTH_LONG).show();
                                firebaseUser.delete();
                                return;
                            }
                            
                            Warung warung = warungSnapshot.toObject(Warung.class);
                            
                            // Create user document with invite role
                            User user = new User(
                                firebaseUser.getUid(),
                                email,
                                email.split("@")[0], // Use email username as name
                                invite.role,
                                invite.warungId
                            );

                            // Save user to Firestore
                            firestore.collection("users").document(firebaseUser.getUid())
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    // Update invite status
                                    firestore.collection("invites").document(invite.inviteId)
                                        .update("status", "accepted", 
                                                "acceptedAt", System.currentTimeMillis(),
                                                "acceptedBy", firebaseUser.getUid())
                                        .addOnCompleteListener(task -> {
                                            // Set current user and warung
                                            authManager.currentUser = user;
                                            authManager.currentWarung = warung;
                                            authManager.saveUserToCache();
                                            
                                            showLoading(false);
                                            Toast.makeText(this, "Registrasi berhasil! Selamat bergabung!", Toast.LENGTH_SHORT).show();
                                            navigateToMain();
                                        });
                                })
                                .addOnFailureListener(e -> {
                                    showLoading(false);
                                    Toast.makeText(this, "Error membuat user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "Error creating user document", e);
                                    firebaseUser.delete();
                                });
                        })
                        .addOnFailureListener(e -> {
                            showLoading(false);
                            Toast.makeText(this, "Error memuat data warung: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error loading warung", e);
                            firebaseUser.delete();
                        });
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Registrasi gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Register error", e);
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

