package com.zanjaprogrammer.warungku.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.zanjaprogrammer.warungku.data.entity.PaymentProof;

import java.util.List;

@Dao
public interface PaymentProofDao {
    @Insert
    long insertPaymentProof(PaymentProof paymentProof);
    
    @Update
    void updatePaymentProof(PaymentProof paymentProof);
    
    @Query("SELECT * FROM payment_proofs WHERE transaction_id = :transactionId")
    LiveData<PaymentProof> getPaymentProofByTransactionId(long transactionId);
    
    @Query("SELECT * FROM payment_proofs WHERE transaction_id = :transactionId")
    PaymentProof getPaymentProofByTransactionIdSync(long transactionId);
    
    @Query("SELECT * FROM payment_proofs WHERE capture_timestamp BETWEEN :startDate AND :endDate ORDER BY capture_timestamp DESC")
    LiveData<List<PaymentProof>> getPaymentProofsByDateRange(long startDate, long endDate);
    
    @Query("SELECT * FROM payment_proofs ORDER BY capture_timestamp DESC")
    LiveData<List<PaymentProof>> getAllPaymentProofs();
    
    @Query("SELECT * FROM payment_proofs WHERE payment_method = :paymentMethod ORDER BY capture_timestamp DESC")
    LiveData<List<PaymentProof>> getPaymentProofsByMethod(String paymentMethod);
    
    @Delete
    void deletePaymentProof(PaymentProof paymentProof);
    
    @Query("DELETE FROM payment_proofs WHERE id = :id")
    void deletePaymentProofById(long id);
    
    @Query("UPDATE payment_proofs SET is_corrupted = 1 WHERE id = :id")
    void markAsCorrupted(long id);
    
    @Query("SELECT COUNT(*) FROM payment_proofs")
    LiveData<Integer> getPaymentProofCount();
    
    @Query("SELECT SUM(file_size) FROM payment_proofs")
    LiveData<Long> getTotalStorageUsed();
    
    @Query("DELETE FROM payment_proofs")
    void deleteAll();
    
    @Query("SELECT * FROM payment_proofs WHERE id = :id")
    PaymentProof getPaymentProofByIdSync(long id);
    
    @Query("SELECT * FROM payment_proofs ORDER BY capture_timestamp DESC")
    List<PaymentProof> getAllPaymentProofsSync();
    
    @Query("SELECT * FROM payment_proofs WHERE " +
           "LOWER(payment_method) LIKE :query OR " +
           "LOWER(file_name) LIKE :query " +
           "ORDER BY capture_timestamp DESC")
    List<PaymentProof> searchPaymentProofs(String query);
}