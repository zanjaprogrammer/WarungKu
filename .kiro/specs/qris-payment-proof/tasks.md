# Implementation Plan: QRIS Payment Proof Feature

## Overview

This implementation plan breaks down the QRIS Payment Proof feature into discrete coding tasks that build incrementally. The plan follows the existing Android Java architecture and integrates seamlessly with the current transaction flow. Each task focuses on specific components while ensuring proper integration with existing systems.

## Tasks

- [x] 1. Set up core data models and database schema
  - Create PaymentProof entity with Room annotations
  - Create PaymentProofDao interface with CRUD operations
  - Update AppDatabase to include PaymentProof entity and dao
  - Add migration script for database schema changes
  - Extend CashFlow entity with payment method and proof indicator fields
  - _Requirements: 2.2, 2.3, 1.4_

- [ ]* 1.1 Write property test for PaymentProof entity
  - **Property 2: Transaction Linkage Integrity**
  - **Validates: Requirements 1.4**

- [x] 2. Implement core storage and file management utilities
  - [x] 2.1 Create PaymentProofStorageHelper class
    - Implement savePhotoToInternalStorage method
    - Implement loadPhotoFromInternalStorage method
    - Implement deletePhotoFile method
    - Implement storage space management methods
    - _Requirements: 2.3, 4.3_

  - [ ]* 2.2 Write property test for storage operations
    - **Property 1: Photo Storage Consistency**
    - **Validates: Requirements 1.3, 2.1, 2.2, 2.3**

  - [x] 2.3 Create ImageCompressionUtil class
    - Implement image compression with quality preservation
    - Implement unique filename generation with transaction ID and timestamp
    - Implement image validation methods
    - _Requirements: 2.1, 2.2, 2.4_

  - [ ]* 2.4 Write property test for image compression
    - **Property 3: File Validation Before Storage**
    - **Validates: Requirements 2.4**

- [x] 3. Implement PaymentProofManager service class
  - [x] 3.1 Create PaymentProofManager with core operations
    - Implement capturePaymentProof method
    - Implement savePaymentProof method with compression and validation
    - Implement getPaymentProofByTransactionId method
    - Implement deletePaymentProof method with file and database cleanup
    - _Requirements: 1.3, 1.4, 4.3_

  - [ ]* 3.2 Write property test for PaymentProofManager operations
    - **Property 6: Data Consistency on Deletion**
    - **Validates: Requirements 4.3, 6.4**

  - [x] 3.3 Add search and management operations
    - Implement getPaymentProofsByDateRange method
    - Implement searchPaymentProofs method with filters
    - Implement bulk operations support
    - _Requirements: 4.1, 4.4_

  - [ ]* 3.4 Write property test for search operations
    - **Property 7: Search and Filter Accuracy**
    - **Validates: Requirements 4.4**

- [-] 4. Create PaymentProofCaptureActivity
  - [x] 4.1 Implement camera capture activity
    - Create activity layout with camera preview and capture controls
    - Implement camera initialization and permission handling
    - Implement photo capture with preview and confirmation
    - Implement save/cancel flow with proper navigation
    - _Requirements: 1.1, 1.2, 1.3, 1.5_

  - [ ]* 4.2 Write unit tests for camera activity
    - Test camera permission handling
    - Test photo capture and save flow
    - Test cancel and navigation scenarios
    - _Requirements: 1.1, 1.2, 1.5_

- [x] 5. Create PaymentProofViewActivity
  - [x] 5.1 Implement photo viewing activity
    - Create layout for full-screen photo display with zoom
    - Implement photo loading and display with transaction details
    - Implement share and export functionality
    - Add navigation and UI controls
    - _Requirements: 3.2, 3.3, 3.4, 3.5_

  - [ ]* 5.2 Write unit tests for photo viewing
    - Test photo display and zoom functionality
    - Test transaction details display
    - Test share and export operations
    - _Requirements: 3.2, 3.3, 3.4, 3.5_

- [-] 6. Integrate with existing SellActivity
  - [x] 6.1 Modify SellActivity for QRIS payment proof option
    - Add payment method tracking to transaction completion
    - Add conditional payment proof capture prompt for QRIS transactions
    - Implement navigation to PaymentProofCaptureActivity
    - Ensure transaction completion works with or without payment proof
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

  - [ ]* 6.2 Write property test for transaction flow integrity
    - **Property 9: Transaction Flow Integrity**
    - **Validates: Requirements 5.4**

- [x] 7. Enhance HistoryActivity with payment proof indicators
  - [x] 7.1 Update HistoryAdapter to show payment proof indicators
    - Modify item layout to include payment proof icon
    - Update adapter to display indicators for transactions with proofs
    - Implement click handling to open PaymentProofViewActivity
    - _Requirements: 3.1, 3.2_

  - [ ]* 7.2 Write property test for payment proof display
    - **Property 4: Payment Proof Display Consistency**
    - **Validates: Requirements 3.1**

- [x] 8. Create payment proof management interface
  - [x] 8.1 Create PaymentProofManagementActivity
    - Create grid layout for payment proof gallery
    - Implement multi-select functionality for bulk operations
    - Add filter and search UI components
    - Implement delete and export operations
    - _Requirements: 4.1, 4.2, 4.4, 4.5_

  - [ ]* 8.2 Write property tests for management operations
    - **Property 5: Management Data Completeness**
    - **Validates: Requirements 4.1**

- [x] 9. Implement backup and restore integration
  - [x] 9.1 Extend DatabaseBackupUtils for payment proofs
    - Modify backup process to include payment proof photos
    - Update backup package structure to handle photos
    - Add photo validation during backup process
    - _Requirements: 6.1, 6.3_

  - [x] 9.2 Extend DatabaseRestoreUtils for payment proofs
    - Modify restore process to handle payment proof photos
    - Implement photo re-linking to transactions after restore
    - Add integrity validation during restore
    - _Requirements: 6.2, 6.3_

  - [ ]* 9.3 Write property test for backup/restore operations
    - **Property 10: Backup and Restore Round-Trip**
    - **Validates: Requirements 6.1, 6.2, 6.3**

- [x] 10. Add export functionality
  - [x] 10.1 Create PaymentProofExportService
    - Implement structured export with photos and transaction data
    - Add export format options (ZIP with photos and CSV data)
    - Implement progress tracking for large exports
    - _Requirements: 4.5, 6.5_

  - [ ]* 10.2 Write property test for export operations
    - **Property 8: Export Structure Completeness**
    - **Validates: Requirements 4.5**

- [x] 11. Error handling and edge cases
  - [x] 11.1 Implement comprehensive error handling
    - Add storage space monitoring and user notifications
    - Implement corrupted file detection and handling
    - Add camera error handling and fallback options
    - Implement network-independent operation
    - _Requirements: 2.5, 5.5, 6.4_

  - [ ]* 11.2 Write unit tests for error scenarios
    - Test storage space limitations
    - Test corrupted file handling
    - Test camera permission and hardware errors
    - _Requirements: 2.5, 5.5, 6.4_

- [x] 12. Final integration and testing
  - [x] 12.1 Integration testing and bug fixes
    - Test complete end-to-end payment proof workflow
    - Verify integration with existing transaction flows
    - Test backup/restore with payment proofs
    - Performance testing with large numbers of photos
    - _Requirements: All_

  - [ ]* 12.2 Write integration tests
    - Test complete payment proof capture and viewing flow
    - Test management and export operations
    - Test backup/restore integration
    - _Requirements: All_

- [x] 13. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- The implementation follows existing Android architecture patterns
- Property tests validate universal correctness properties
- Unit tests validate specific examples and edge cases
- Integration occurs incrementally to avoid breaking existing functionality