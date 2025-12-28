package com.zanjaprogrammer.warungku;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.zanjaprogrammer.warungku.auth.AuthManager;
import com.zanjaprogrammer.warungku.adapters.EmployeeAdapter;
import com.zanjaprogrammer.warungku.data.model.User;
import com.zanjaprogrammer.warungku.databinding.ActivityManageEmployeesBinding;
import java.util.ArrayList;
import java.util.List;

public class ManageEmployeesActivity extends AppCompatActivity {
    
    private ActivityManageEmployeesBinding binding;
    private AuthManager authManager;
    private FirebaseFirestore firestore;
    private List<User> employees = new ArrayList<>();
    private EmployeeAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check authentication
        authManager = AuthManager.getInstance(getApplication());
        if (!authManager.isLoggedIn()) {
            authManager.loadUserFromCache();
            if (!authManager.isLoggedIn()) {
                startActivity(new android.content.Intent(this, LoginActivity.class));
                finish();
                return;
            }
        }
        
        // Check permission: only owner can manage employees
        String role = authManager.getCurrentUserRole();
        if (!com.zanjaprogrammer.warungku.auth.PermissionManager.canManageEmployees(role)) {
            Toast.makeText(this, "Anda tidak memiliki izin untuk mengakses halaman ini", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        binding = ActivityManageEmployeesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        firestore = FirebaseFirestore.getInstance();
        
        setupToolbar();
        setupRecyclerView();
        setupFab();
        loadEmployees();
    }
    
    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupRecyclerView() {
        adapter = new EmployeeAdapter(employees, this::onRemoveEmployee);
        binding.rvEmployees.setLayoutManager(new LinearLayoutManager(this));
        binding.rvEmployees.setAdapter(adapter);
    }
    
    private void setupFab() {
        binding.fabInvite.setOnClickListener(v -> showInviteDialog());
    }
    
    private void loadEmployees() {
        String warungId = authManager.getCurrentWarungId();
        if (warungId == null) {
            Toast.makeText(this, "Warung ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }
        
        binding.progressBar.setVisibility(View.VISIBLE);
        
        firestore.collection("users")
            .whereEqualTo("warungId", warungId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                employees.clear();
                for (QueryDocumentSnapshot document : querySnapshot) {
                    User user = document.toObject(User.class);
                    if (user != null && !user.role.equals("owner")) {
                        employees.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
                
                if (employees.isEmpty()) {
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    binding.tvEmpty.setVisibility(View.GONE);
                }
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error memuat data karyawan: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
    
    private void showInviteDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_invite_employee, null);
        builder.setView(dialogView);
        
        com.google.android.material.textfield.TextInputEditText etEmail = 
            dialogView.findViewById(R.id.etEmail);
        android.widget.Spinner spinnerRole = dialogView.findViewById(R.id.spinnerRole);
        
        // Setup role spinner
        String[] roles = {"Manager", "Cashier", "Staff"};
        android.widget.ArrayAdapter<String> roleAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);
        
        builder.setTitle("Invite Karyawan");
        builder.setPositiveButton("Kirim Invite", (dialog, which) -> {
            String email = etEmail.getText().toString().trim();
            String role = roles[spinnerRole.getSelectedItemPosition()].toLowerCase();
            
            if (email.isEmpty()) {
                Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }
            
            inviteEmployee(email, role);
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }
    
    private void inviteEmployee(String email, String role) {
        String warungId = authManager.getCurrentWarungId();
        String ownerId = authManager.getCurrentUserId();
        
        if (warungId == null || ownerId == null) {
            Toast.makeText(this, "Data warung tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String inviteId = java.util.UUID.randomUUID().toString();
        com.zanjaprogrammer.warungku.data.model.Invite invite = 
            new com.zanjaprogrammer.warungku.data.model.Invite(inviteId, warungId, ownerId, email, role);
        
        firestore.collection("invites").document(inviteId)
            .set(invite)
            .addOnSuccessListener(aVoid -> {
                // Generate invite code (simple: first 8 chars of inviteId)
                String inviteCode = inviteId.substring(0, 8).toUpperCase();
                
                // Show dialog with invite code and share options
                showInviteSuccessDialog(email, inviteCode, inviteId);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error mengirim invite: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
    
    private void showInviteSuccessDialog(String email, String inviteCode, String inviteId) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_invite_success, null);
        builder.setView(dialogView);
        
        TextView tvInviteCode = dialogView.findViewById(R.id.tvInviteCode);
        TextView tvInviteEmail = dialogView.findViewById(R.id.tvInviteEmail);
        android.widget.Button btnCopy = dialogView.findViewById(R.id.btnCopy);
        android.widget.Button btnShare = dialogView.findViewById(R.id.btnShare);
        
        tvInviteCode.setText(inviteCode);
        tvInviteEmail.setText(email);
        
        android.app.AlertDialog dialog = builder.create();
        
        btnCopy.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Invite Code", inviteCode);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Invite code disalin!", Toast.LENGTH_SHORT).show();
        });
        
        btnShare.setOnClickListener(v -> {
            shareInvite(email, inviteCode);
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    private void shareInvite(String email, String inviteCode) {
        String warungName = authManager.getCurrentWarung() != null ? 
            authManager.getCurrentWarung().name : "WarungKu";
        
        String inviteMessage = "Halo!\n\n" +
            "Anda diundang untuk bergabung dengan " + warungName + " di aplikasi WarungKu.\n\n" +
            "Kode Invite: " + inviteCode + "\n\n" +
            "Untuk bergabung:\n" +
            "1. Download aplikasi WarungKu\n" +
            "2. Buka aplikasi dan pilih 'Daftar'\n" +
            "3. Gunakan email ini dan masukkan kode invite: " + inviteCode + "\n\n" +
            "Terima kasih!";
        
        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Undangan Bergabung - " + warungName);
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, inviteMessage);
        
        startActivity(android.content.Intent.createChooser(shareIntent, "Bagikan Invite via"));
    }
    
    private void onRemoveEmployee(User employee) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Hapus Karyawan")
            .setMessage("Apakah Anda yakin ingin menghapus " + employee.name + "?")
            .setPositiveButton("Hapus", (dialog, which) -> {
                // TODO: Implement remove employee
                Toast.makeText(this, "Fitur hapus karyawan akan segera hadir", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Batal", null)
            .show();
    }
}

