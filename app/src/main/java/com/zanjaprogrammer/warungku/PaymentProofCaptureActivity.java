package com.zanjaprogrammer.warungku;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.zanjaprogrammer.warungku.data.DataRepository;
import com.zanjaprogrammer.warungku.service.PaymentProofManager;
import com.zanjaprogrammer.warungku.utils.CustomToast;
import com.zanjaprogrammer.warungku.viewmodel.AppViewModel;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentProofCaptureActivity extends AppCompatActivity {

    private static final String TAG = "PaymentProofCapture";
    public static final String EXTRA_TRANSACTION_ID = "transaction_id";
    public static final String EXTRA_PAYMENT_METHOD = "payment_method";
    public static final int RESULT_PHOTO_SAVED = RESULT_FIRST_USER + 1;
    public static final int RESULT_CANCELLED = RESULT_FIRST_USER + 2;

    // UI Components
    private PreviewView previewView;
    private ImageView btnCapture;
    private ImageButton btnCancel;
    private LinearLayout photoPreviewContainer;
    private ImageView imagePreview;
    private ImageButton btnRetake;
    private Button btnSavePhoto;
    private Button btnDiscardPhoto;
    private LinearLayout loadingContainer;

    // Camera Components
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;

    // Data
    private long transactionId;
    private String paymentMethod;
    private Bitmap capturedPhoto;
    private PaymentProofManager paymentProofManager;
    private AppViewModel viewModel;

    // Permission Launcher
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_proof_capture);

        // Initialize data from intent
        initializeIntentData();

        // Initialize UI components
        initializeViews();

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Initialize services
        viewModel = new ViewModelProvider(this).get(AppViewModel.class);
        paymentProofManager = new PaymentProofManager(this);

        // Initialize permission launcher
        initializePermissionLauncher();

        // Set up click listeners
        setupClickListeners();

        // Check camera permission and start camera
        checkCameraPermissionAndStart();
    }

    private void initializeIntentData() {
        Intent intent = getIntent();
        transactionId = intent.getLongExtra(EXTRA_TRANSACTION_ID, -1);
        paymentMethod = intent.getStringExtra(EXTRA_PAYMENT_METHOD);

        if (transactionId == -1) {
            Log.e(TAG, "Invalid transaction ID received");
            CustomToast.showError(this, "Terjadi kesalahan: ID transaksi tidak valid");
            finish();
        }
    }

    private void initializeViews() {
        previewView = findViewById(R.id.previewView);
        btnCapture = findViewById(R.id.btnCapture);
        btnCancel = findViewById(R.id.btnCancel);
        photoPreviewContainer = findViewById(R.id.photoPreviewContainer);
        imagePreview = findViewById(R.id.imagePreview);
        btnRetake = findViewById(R.id.btnRetake);
        btnSavePhoto = findViewById(R.id.btnSavePhoto);
        btnDiscardPhoto = findViewById(R.id.btnDiscardPhoto);
        loadingContainer = findViewById(R.id.loadingContainer);
    }

    private void initializePermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startCamera();
                    } else {
                        CustomToast.showError(this, getString(R.string.payment_proof_permission_denied));
                        setResult(RESULT_CANCELLED);
                        finish();
                    }
                }
        );
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELLED);
            finish();
        });

        btnCapture.setOnClickListener(v -> capturePhoto());

        btnRetake.setOnClickListener(v -> showCameraView());

        btnDiscardPhoto.setOnClickListener(v -> showCameraView());

        btnSavePhoto.setOnClickListener(v -> savePhoto());
    }

    private void checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
                CustomToast.showError(this, getString(R.string.payment_proof_camera_error));
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        // Preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        // Camera selector (back camera)
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            // Unbind all use cases before rebinding
            cameraProvider.unbindAll();

            // Bind use cases to camera
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

        } catch (Exception e) {
            Log.e(TAG, "Error binding camera use cases", e);
            CustomToast.showError(this, getString(R.string.payment_proof_camera_error));
        }
    }

    private void capturePhoto() {
        if (imageCapture == null) {
            Log.e(TAG, "ImageCapture is null");
            return;
        }

        // Disable capture button to prevent multiple captures
        btnCapture.setEnabled(false);

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        // Convert ImageProxy to Bitmap
                        capturedPhoto = imageProxyToBitmap(image);
                        image.close();

                        // Show preview
                        runOnUiThread(() -> {
                            showPhotoPreview();
                            btnCapture.setEnabled(true);
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed", exception);
                        runOnUiThread(() -> {
                            CustomToast.showError(PaymentProofCaptureActivity.this, 
                                    getString(R.string.payment_proof_camera_error));
                            btnCapture.setEnabled(true);
                        });
                    }
                }
        );
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        // Rotate bitmap if needed (camera images are often rotated)
        Matrix matrix = new Matrix();
        matrix.postRotate(image.getImageInfo().getRotationDegrees());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void showPhotoPreview() {
        if (capturedPhoto != null) {
            imagePreview.setImageBitmap(capturedPhoto);
            photoPreviewContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showCameraView() {
        photoPreviewContainer.setVisibility(View.GONE);
        capturedPhoto = null;
    }

    private void savePhoto() {
        if (capturedPhoto == null) {
            CustomToast.showError(this, "Tidak ada foto untuk disimpan");
            return;
        }

        // Show loading
        loadingContainer.setVisibility(View.VISIBLE);

        // Save photo using PaymentProofManager
        paymentProofManager.savePaymentProof(transactionId, capturedPhoto)
                .thenAccept(paymentProof -> {
                    runOnUiThread(() -> {
                        loadingContainer.setVisibility(View.GONE);
                        if (paymentProof != null) {
                            CustomToast.showSuccess(this, getString(R.string.payment_proof_saved_success));
                            setResult(RESULT_PHOTO_SAVED);
                            finish();
                        } else {
                            CustomToast.showError(this, getString(R.string.payment_proof_save_failed));
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Error saving payment proof", throwable);
                    runOnUiThread(() -> {
                        loadingContainer.setVisibility(View.GONE);
                        CustomToast.showError(this, getString(R.string.payment_proof_save_failed));
                    });
                    return null;
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }

    @Override
    public void onBackPressed() {
        if (photoPreviewContainer.getVisibility() == View.VISIBLE) {
            showCameraView();
        } else {
            setResult(RESULT_CANCELLED);
            super.onBackPressed();
        }
    }
}