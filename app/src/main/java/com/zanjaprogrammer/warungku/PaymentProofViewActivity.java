package com.zanjaprogrammer.warungku;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.zanjaprogrammer.warungku.data.AppDatabase;
import com.zanjaprogrammer.warungku.data.dao.CashFlowDao;
import com.zanjaprogrammer.warungku.data.entity.CashFlow;
import com.zanjaprogrammer.warungku.data.entity.PaymentProof;
import com.zanjaprogrammer.warungku.service.PaymentProofManager;
import com.zanjaprogrammer.warungku.utils.CurrencyFormatter;
import com.zanjaprogrammer.warungku.utils.CustomToast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Activity for viewing payment proof photos with zoom capabilities and transaction details
 * Implements Requirements 3.2, 3.3, 3.4, 3.5
 */
public class PaymentProofViewActivity extends AppCompatActivity {
    
    private static final String TAG = "PaymentProofViewActivity";
    public static final String EXTRA_PAYMENT_PROOF_ID = "payment_proof_id";
    public static final String EXTRA_TRANSACTION_ID = "transaction_id";
    
    // UI Components
    private ImageView imagePaymentProof;
    private LinearLayout loadingContainer;
    private LinearLayout errorContainer;
    private LinearLayout transactionDetailsContainer;
    private ImageButton btnBack;
    private ImageButton btnShare;
    private Button btnExport;
    private Button btnSharePhoto;
    
    // Transaction Detail TextViews
    private TextView textTransactionId;
    private TextView textAmount;
    private TextView textPaymentMethod;
    private TextView textTransactionDate;
    private TextView textCaptureDate;
    private TextView textDescription;
    
    // Zoom and Pan functionality
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private ScaleGestureDetector scaleDetector;
    private float minScale = 1f;
    private float maxScale = 5f;
    private float currentScale = 1f;
    
    // Touch handling
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    
    // Data
    private PaymentProofManager paymentProofManager;
    private CashFlowDao cashFlowDao;
    private PaymentProof currentPaymentProof;
    private CashFlow currentTransaction;
    private Bitmap currentBitmap;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_proof_view);
        
        initializeComponents();
        setupZoomAndPan();
        setupClickListeners();
        loadPaymentProofData();
    }
    
    /**
     * Initialize UI components and services
     */
    private void initializeComponents() {
        // UI Components
        imagePaymentProof = findViewById(R.id.imagePaymentProof);
        loadingContainer = findViewById(R.id.loadingContainer);
        errorContainer = findViewById(R.id.errorContainer);
        transactionDetailsContainer = findViewById(R.id.transactionDetailsContainer);
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnExport = findViewById(R.id.btnExport);
        btnSharePhoto = findViewById(R.id.btnSharePhoto);
        
        // Transaction Detail TextViews
        textTransactionId = findViewById(R.id.textTransactionId);
        textAmount = findViewById(R.id.textAmount);
        textPaymentMethod = findViewById(R.id.textPaymentMethod);
        textTransactionDate = findViewById(R.id.textTransactionDate);
        textCaptureDate = findViewById(R.id.textCaptureDate);
        textDescription = findViewById(R.id.textDescription);
        
        // Services
        paymentProofManager = new PaymentProofManager(this);
        AppDatabase database = AppDatabase.getDatabase(this);
        cashFlowDao = database.cashFlowDao();
        
        // Set initial state
        showLoading(true);
    }
    
    /**
     * Setup zoom and pan functionality for the image view
     */
    private void setupZoomAndPan() {
        scaleDetector = new ScaleGestureDetector(this, new ScaleListener());
        
        imagePaymentProof.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleDetector.onTouchEvent(event);
                
                PointF curr = new PointF(event.getX(), event.getY());
                
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        savedMatrix.set(matrix);
                        start.set(curr);
                        mode = DRAG;
                        break;
                        
                    case MotionEvent.ACTION_POINTER_DOWN:
                        savedMatrix.set(matrix);
                        midPoint(mid, event);
                        mode = ZOOM;
                        break;
                        
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG && currentScale > minScale) {
                            matrix.set(savedMatrix);
                            float dx = curr.x - start.x;
                            float dy = curr.y - start.y;
                            matrix.postTranslate(dx, dy);
                        }
                        break;
                        
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        break;
                }
                
                imagePaymentProof.setImageMatrix(matrix);
                return true;
            }
        });
    }
    
    /**
     * Setup click listeners for UI components
     */
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnShare.setOnClickListener(v -> sharePaymentProof());
        
        btnExport.setOnClickListener(v -> exportPaymentProof());
        
        btnSharePhoto.setOnClickListener(v -> sharePaymentProof());
    }
    
    /**
     * Load payment proof data from intent extras
     */
    private void loadPaymentProofData() {
        long paymentProofId = getIntent().getLongExtra(EXTRA_PAYMENT_PROOF_ID, -1);
        long transactionId = getIntent().getLongExtra(EXTRA_TRANSACTION_ID, -1);
        
        if (paymentProofId != -1) {
            loadPaymentProofById(paymentProofId);
        } else if (transactionId != -1) {
            loadPaymentProofByTransactionId(transactionId);
        } else {
            showError("Data bukti pembayaran tidak ditemukan");
        }
    }
    
    /**
     * Load payment proof by ID
     */
    private void loadPaymentProofById(long paymentProofId) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return paymentProofManager.getPaymentProofByTransactionId(paymentProofId).get();
            } catch (Exception e) {
                Log.e(TAG, "Error loading payment proof by ID", e);
                return null;
            }
        }).thenAccept(paymentProof -> {
            runOnUiThread(() -> {
                if (paymentProof != null) {
                    currentPaymentProof = paymentProof;
                    loadTransactionDetails(paymentProof.getTransactionId());
                    displayPaymentProof(paymentProof);
                } else {
                    showError("Bukti pembayaran tidak ditemukan");
                }
            });
        });
    }
    
    /**
     * Load payment proof by transaction ID
     */
    private void loadPaymentProofByTransactionId(long transactionId) {
        paymentProofManager.getPaymentProofByTransactionId(transactionId)
            .thenAccept(paymentProof -> {
                runOnUiThread(() -> {
                    if (paymentProof != null) {
                        currentPaymentProof = paymentProof;
                        loadTransactionDetails(transactionId);
                        displayPaymentProof(paymentProof);
                    } else {
                        showError("Bukti pembayaran tidak ditemukan untuk transaksi ini");
                    }
                });
            })
            .exceptionally(throwable -> {
                Log.e(TAG, "Error loading payment proof", throwable);
                runOnUiThread(() -> showError("Gagal memuat bukti pembayaran"));
                return null;
            });
    }
    
    /**
     * Load transaction details from database
     */
    private void loadTransactionDetails(long transactionId) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return cashFlowDao.getCashFlowById(transactionId);
            } catch (Exception e) {
                Log.e(TAG, "Error loading transaction details", e);
                return null;
            }
        }).thenAccept(transaction -> {
            runOnUiThread(() -> {
                if (transaction != null) {
                    currentTransaction = transaction;
                    showTransactionDetails(transaction);
                }
            });
        });
    }
    
    /**
     * Display payment proof photo with zoom capabilities
     */
    private void displayPaymentProof(PaymentProof paymentProof) {
        if (paymentProof.isCorrupted()) {
            showError("File foto rusak atau tidak dapat dibaca");
            return;
        }
        
        paymentProofManager.loadPaymentProofPhoto(paymentProof)
            .thenAccept(bitmap -> {
                runOnUiThread(() -> {
                    if (bitmap != null) {
                        currentBitmap = bitmap;
                        imagePaymentProof.setImageBitmap(bitmap);
                        imagePaymentProof.setScaleType(ImageView.ScaleType.MATRIX);
                        
                        // Reset zoom and center image
                        resetImageMatrix();
                        showLoading(false);
                    } else {
                        showError("Gagal memuat foto bukti pembayaran");
                    }
                });
            })
            .exceptionally(throwable -> {
                Log.e(TAG, "Error loading payment proof photo", throwable);
                runOnUiThread(() -> showError("Gagal memuat foto"));
                return null;
            });
    }
    
    /**
     * Show transaction details in the bottom sheet
     */
    private void showTransactionDetails(CashFlow transaction) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
        
        textTransactionId.setText("#" + transaction.id);
        textAmount.setText(CurrencyFormatter.format(transaction.amount));
        textPaymentMethod.setText(transaction.paymentMethod != null ? transaction.paymentMethod : "QRIS");
        textTransactionDate.setText(dateFormat.format(new Date(transaction.timestamp)));
        textDescription.setText(transaction.description != null ? transaction.description : "Penjualan produk");
        
        if (currentPaymentProof != null) {
            textCaptureDate.setText(dateFormat.format(new Date(currentPaymentProof.getCaptureTimestamp())));
        }
    }
    
    /**
     * Reset image matrix to fit screen
     */
    private void resetImageMatrix() {
        if (currentBitmap == null) return;
        
        int imageWidth = currentBitmap.getWidth();
        int imageHeight = currentBitmap.getHeight();
        int viewWidth = imagePaymentProof.getWidth();
        int viewHeight = imagePaymentProof.getHeight();
        
        if (viewWidth == 0 || viewHeight == 0) {
            // View not measured yet, post to run after layout
            imagePaymentProof.post(this::resetImageMatrix);
            return;
        }
        
        float scaleX = (float) viewWidth / imageWidth;
        float scaleY = (float) viewHeight / imageHeight;
        float scale = Math.min(scaleX, scaleY);
        
        minScale = scale;
        currentScale = scale;
        
        matrix.reset();
        matrix.postScale(scale, scale);
        
        // Center the image
        float dx = (viewWidth - imageWidth * scale) / 2;
        float dy = (viewHeight - imageHeight * scale) / 2;
        matrix.postTranslate(dx, dy);
        
        imagePaymentProof.setImageMatrix(matrix);
    }
    
    /**
     * Share payment proof photo and transaction details
     */
    private void sharePaymentProof() {
        if (currentBitmap == null || currentPaymentProof == null) {
            CustomToast.showError(this, "Tidak ada foto untuk dibagikan");
            return;
        }
        
        try {
            // Create temporary file for sharing
            File cachePath = new File(getCacheDir(), "shared_images");
            cachePath.mkdirs();
            
            String fileName = "bukti_pembayaran_" + currentPaymentProof.getTransactionId() + ".jpg";
            File imageFile = new File(cachePath, fileName);
            
            FileOutputStream stream = new FileOutputStream(imageFile);
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            stream.close();
            
            Uri contentUri = FileProvider.getUriForFile(this, 
                getPackageName() + ".fileprovider", imageFile);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // Add transaction details as text
            if (currentTransaction != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
                String shareText = String.format(
                    "Bukti Pembayaran QRIS\n" +
                    "ID Transaksi: #%d\n" +
                    "Jumlah: %s\n" +
                    "Tanggal: %s\n" +
                    "Metode: %s",
                    currentTransaction.id,
                    CurrencyFormatter.format(currentTransaction.amount),
                    dateFormat.format(new Date(currentTransaction.timestamp)),
                    currentTransaction.paymentMethod != null ? currentTransaction.paymentMethod : "QRIS"
                );
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            }
            
            startActivity(Intent.createChooser(shareIntent, "Bagikan Bukti Pembayaran"));
            
        } catch (IOException e) {
            Log.e(TAG, "Error sharing payment proof", e);
            CustomToast.showError(this, "Gagal membagikan foto");
        }
    }
    
    /**
     * Export payment proof to external storage
     */
    private void exportPaymentProof() {
        if (currentBitmap == null || currentPaymentProof == null) {
            CustomToast.showError(this, "Tidak ada foto untuk diekspor");
            return;
        }
        
        try {
            String fileName = "bukti_pembayaran_" + currentPaymentProof.getTransactionId() + "_" + 
                System.currentTimeMillis() + ".jpg";
            
            String savedImageURL = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                currentBitmap,
                fileName,
                "Bukti pembayaran QRIS - Transaksi #" + currentPaymentProof.getTransactionId()
            );
            
            if (savedImageURL != null) {
                CustomToast.showSuccess(this, "Foto berhasil diekspor ke galeri");
            } else {
                CustomToast.showError(this, "Gagal mengekspor foto");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error exporting payment proof", e);
            CustomToast.showError(this, "Gagal mengekspor foto");
        }
    }
    
    /**
     * Show loading state
     */
    private void showLoading(boolean show) {
        loadingContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        errorContainer.setVisibility(View.GONE);
        imagePaymentProof.setVisibility(show ? View.GONE : View.VISIBLE);
        transactionDetailsContainer.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    /**
     * Show error state with message
     */
    private void showError(String message) {
        showLoading(false);
        errorContainer.setVisibility(View.VISIBLE);
        imagePaymentProof.setVisibility(View.GONE);
        transactionDetailsContainer.setVisibility(View.GONE);
        
        CustomToast.showError(this, message);
    }
    
    /**
     * Calculate midpoint between two touch points
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
    
    /**
     * Scale gesture detector for zoom functionality
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float newScale = currentScale * scaleFactor;
            
            // Limit zoom levels
            if (newScale < minScale) {
                scaleFactor = minScale / currentScale;
                newScale = minScale;
            } else if (newScale > maxScale) {
                scaleFactor = maxScale / currentScale;
                newScale = maxScale;
            }
            
            currentScale = newScale;
            
            matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            imagePaymentProof.setImageMatrix(matrix);
            
            return true;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up resources
        if (paymentProofManager != null) {
            paymentProofManager.shutdown();
        }
        
        if (currentBitmap != null && !currentBitmap.isRecycled()) {
            currentBitmap.recycle();
            currentBitmap = null;
        }
    }
}