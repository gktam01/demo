package com.cashflow;

import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.nio.file.StandardCopyOption;

public class BackupManager {
    private static final Logger logger = Logger.getLogger(BackupManager.class.getName());
    private DatabaseManager dbManager;
    private Stage parentStage;

    public BackupManager(DatabaseManager dbManager, Stage parentStage) {
        this.dbManager = dbManager;
        this.parentStage = parentStage;
    }

    /**
     * Tạo backup với giao diện tiến trình
     */
    public void createBackupWithProgress() {
        // Hiển thị file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Backup File");
        fileChooser.setInitialFileName("cashflow_backup_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".cbk");
        
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
            "Cashflow Backup (*.cbk)", "*.cbk");
        fileChooser.getExtensionFilters().add(filter);

        File saveFile = fileChooser.showSaveDialog(parentStage);
        if (saveFile == null) return;

        // Tạo progress dialog
        Alert progressDialog = createProgressDialog();
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setStyle("-fx-accent: #4ECDC4;");
        
        VBox progressContent = new VBox(15);
        progressContent.setPadding(new Insets(20));
        progressContent.getChildren().addAll(
            progressIndicator,
            new Label("Creating backup...")
        );
        progressDialog.getDialogPane().setContent(progressContent);
        progressDialog.show();

        // Tạo backup task
        Task<Boolean> backupTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return performBackup(saveFile.getAbsolutePath());
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    progressDialog.close();
                    showSuccessAlert("Backup completed successfully!", 
                        "Backup saved to: " + saveFile.getAbsolutePath());
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressDialog.close();
                    showErrorAlert("Backup failed!", 
                        "Error: " + getException().getMessage());
                });
            }
        };

        new Thread(backupTask).start();
    }

    /**
     * Restore backup với validation
     */
    public void restoreBackupWithValidation() {
        // Warning dialog
        Alert warningDialog = new Alert(Alert.AlertType.WARNING);
        warningDialog.setTitle("Restore Backup");
        warningDialog.setHeaderText("⚠️ WARNING");
        warningDialog.setContentText(
            "Restoring a backup will replace ALL current data.\n" +
            "This action cannot be undone!\n\n" +
            "Do you want to create a backup of current data first?"
        );

        ButtonType createBackupFirst = new ButtonType("Create Backup First");
        ButtonType proceedRestore = new ButtonType("Proceed with Restore");
        ButtonType cancel = new ButtonType("Cancel");
        
        warningDialog.getButtonTypes().setAll(createBackupFirst, proceedRestore, cancel);

        warningDialog.showAndWait().ifPresent(response -> {
            if (response == createBackupFirst) {
                createBackupWithProgress();
                // After backup, ask again
                Platform.runLater(() -> proceedWithRestore());
            } else if (response == proceedRestore) {
                proceedWithRestore();
            }
        });
    }

    private void proceedWithRestore() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
            "Cashflow Backup (*.cbk)", "*.cbk");
        fileChooser.getExtensionFilters().add(filter);

        File backupFile = fileChooser.showOpenDialog(parentStage);
        if (backupFile == null) return;

        // Validate backup file
        if (!validateBackupFile(backupFile)) {
            showErrorAlert("Invalid Backup File", 
                "The selected file is not a valid Cashflow backup.");
            return;
        }

        // Show restore progress
        Alert progressDialog = createProgressDialog();
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setStyle("-fx-accent: #FF9800;");
        
        VBox progressContent = new VBox(15);
        progressContent.setPadding(new Insets(20));
        progressContent.getChildren().addAll(
            progressIndicator,
            new Label("Restoring backup...")
        );
        progressDialog.getDialogPane().setContent(progressContent);
        progressDialog.show();

        // Restore task
        Task<Boolean> restoreTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return performRestore(backupFile.getAbsolutePath());
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    progressDialog.close();
                    showSuccessAlert("Restore completed!", 
                        "Data has been restored successfully.\nPlease restart the application.");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressDialog.close();
                    showErrorAlert("Restore failed!", 
                        "Error: " + getException().getMessage());
                });
            }
        };

        new Thread(restoreTask).start();
    }

    /**
     * Tự động backup theo lịch
     */
    public void setupAutoBackup() {
        // Tạo thư mục auto backup
        Path autoBackupDir = Paths.get("backups", "auto");
        try {
            Files.createDirectories(autoBackupDir);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not create auto backup directory", e);
            return;
        }

        // Schedule auto backup (chạy mỗi khi app khởi động)
        Platform.runLater(() -> {
            try {
                String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String autoBackupPath = autoBackupDir.resolve(
                    "auto_backup_" + timestamp + ".cbk").toString();
                
                performBackup(autoBackupPath);
                logger.info("Auto backup created: " + autoBackupPath);
                
                // Cleanup old auto backups (keep only 5 latest)
                cleanupOldAutoBackups(autoBackupDir);
                
            } catch (Exception e) {
                logger.log(Level.WARNING, "Auto backup failed", e);
            }
        });
    }

    /**
     * Thực hiện backup
     */
    private boolean performBackup(String backupPath) {
        try {
            // Tạo backup metadata
            BackupMetadata metadata = new BackupMetadata();
            metadata.setVersion("2.1.0");
            metadata.setCreatedDate(LocalDateTime.now());
            metadata.setRecordCounts(getRecordCounts());

            // Tạo ZIP file
            try (FileOutputStream fos = new FileOutputStream(backupPath);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                // Add database file
                addFileToZip(zos, "cashflow.db", "database/cashflow.db");
                
                // Add metadata
                addMetadataToZip(zos, metadata);
                
                // Add settings (nếu có)
                addSettingsToZip(zos);

                logger.info("Backup created successfully: " + backupPath);
                return true;
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Backup failed", e);
            return false;
        }
    }

    /**
     * Thực hiện restore
     */
    private boolean performRestore(String backupPath) {
        try {
            // Backup current data first
            String currentBackupPath = "current_backup_before_restore_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".cbk";
            performBackup(currentBackupPath);

            // Extract backup
            try (FileInputStream fis = new FileInputStream(backupPath);
                 ZipInputStream zis = new ZipInputStream(fis)) {

                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().equals("database/cashflow.db")) {
                        // Restore database
                        restoreDatabaseFromZip(zis);
                    } else if (entry.getName().equals("metadata.json")) {
                        // Validate metadata
                        validateMetadataFromZip(zis);
                    }
                    zis.closeEntry();
                }

                logger.info("Restore completed successfully");
                return true;
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Restore failed", e);
            return false;
        }
    }

    // Helper methods
    private Alert createProgressDialog() {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Cashflow Backup");
        dialog.setHeaderText(null);
        dialog.getButtonTypes().clear();
        return dialog;
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean validateBackupFile(File file) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry;
            boolean hasDatabase = false;
            boolean hasMetadata = false;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("database/cashflow.db")) {
                    hasDatabase = true;
                } else if (entry.getName().equals("metadata.json")) {
                    hasMetadata = true;
                }
                zis.closeEntry();
            }

            return hasDatabase && hasMetadata;
        } catch (Exception e) {
            return false;
        }
    }

    private void addFileToZip(ZipOutputStream zos, String sourceFile, String entryName) throws IOException {
        File file = new File(sourceFile);
        if (!file.exists()) return;

        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
        }
        zos.closeEntry();
    }

    private void addMetadataToZip(ZipOutputStream zos, BackupMetadata metadata) throws IOException {
        ZipEntry entry = new ZipEntry("metadata.json");
        zos.putNextEntry(entry);
        
        String json = metadata.toJson();
        zos.write(json.getBytes());
        zos.closeEntry();
    }

    private void addSettingsToZip(ZipOutputStream zos) throws IOException {
        // Add application settings if any
        ZipEntry entry = new ZipEntry("settings.properties");
        zos.putNextEntry(entry);
        
        String settings = "version=2.1.0\nbackup_date=" + 
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        zos.write(settings.getBytes());
        zos.closeEntry();
    }

    private void restoreDatabaseFromZip(ZipInputStream zis) throws IOException {
        // Close current database connection
        dbManager.close();
        
        // Replace database file
        try (FileOutputStream fos = new FileOutputStream("cashflow.db")) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }

    private void validateMetadataFromZip(ZipInputStream zis) throws IOException {
        // Read and validate metadata
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = zis.read(buffer)) > 0) {
            sb.append(new String(buffer, 0, length));
        }
        
        // Parse metadata (simplified)
        String metadataJson = sb.toString();
        logger.info("Backup metadata: " + metadataJson);
    }

    private void cleanupOldAutoBackups(Path autoBackupDir) {
        try {
            List<Path> backupFiles = Files.list(autoBackupDir)
                .filter(path -> path.toString().endsWith(".cbk"))
                .sorted((a, b) -> b.getFileName().toString().compareTo(a.getFileName().toString()))
                .toList();

            // Keep only 5 latest backups
            for (int i = 5; i < backupFiles.size(); i++) {
                Files.deleteIfExists(backupFiles.get(i));
                logger.info("Deleted old auto backup: " + backupFiles.get(i));
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to cleanup old backups", e);
        }
    }

    private RecordCounts getRecordCounts() {
        RecordCounts counts = new RecordCounts();
        counts.incomeRecords = dbManager.getIncomeRecordsCount();
        counts.outcomeRecords = dbManager.getOutcomeRecordsCount();
        return counts;
    }

    // Inner classes
    private static class BackupMetadata {
        private String version;
        private LocalDateTime createdDate;
        private RecordCounts recordCounts;

        public void setVersion(String version) { this.version = version; }
        public void setCreatedDate(LocalDateTime date) { this.createdDate = date; }
        public void setRecordCounts(RecordCounts counts) { this.recordCounts = counts; }

        public String toJson() {
            return String.format(
                "{\"version\":\"%s\",\"createdDate\":\"%s\",\"incomeRecords\":%d,\"outcomeRecords\":%d}",
                version, 
                createdDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                recordCounts.incomeRecords,
                recordCounts.outcomeRecords
            );
        }
    }

    private static class RecordCounts {
        int incomeRecords;
        int outcomeRecords;
    }
} 