package com.cashflow;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.application.Platform;
import javafx.concurrent.Task;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.logging.Level;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

public class SettingsPage {
    
    private static final Logger logger = Logger.getLogger(SettingsPage.class.getName());
    
    // Colors matching the app theme
    private final Color BACKGROUND_COLOR = Color.web("#0f0f23");
    private final Color CARD_COLOR = Color.web("#1e2749");
    private final Color ACCENT_COLOR = Color.web("#4ECDC4");
    private final Color TEXT_PRIMARY = Color.WHITE;
    private final Color TEXT_SECONDARY = Color.web("#8892b0");
    private final Color TEXT_MUTED = Color.web("#64748b");
    private final Color SUCCESS_COLOR = Color.web("#4CAF50");
    private final Color WARNING_COLOR = Color.web("#FF9800");
    
    private LanguageManager languageManager;
    private DatabaseManager dbManager;
    
    public SettingsPage(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.languageManager = LanguageManager.getInstance();
    }
    
    public VBox createSettingsPageContent() {
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30));
        mainContent.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");
        
        // Header
        HBox header = createSettingsHeader();
        
        // Settings sections
        VBox settingsContainer = new VBox(20);
        
        // Language Settings Section
        VBox languageSection = createLanguageSettingsSection();
        
        // App Settings Section
        VBox appSection = createAppSettingsSection();
        
        // Data Management Section
        VBox dataSection = createDataManagementSection();
        
        // About Section
        VBox aboutSection = createAboutSection();
        
        settingsContainer.getChildren().addAll(languageSection, appSection, dataSection, aboutSection);
        
        mainContent.getChildren().addAll(header, settingsContainer);
        return mainContent;
    }
    
    private HBox createSettingsHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleSection = new VBox(5);
        
        Label title = new Label(languageManager.translate("settings"));
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        
        Label subtitle = new Label("Customize your application preferences");
        subtitle.setTextFill(TEXT_SECONDARY);
        subtitle.setFont(Font.font("Segoe UI", 14));
        
        titleSection.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Settings icon
        Label settingsIcon = new Label("⚙️");
        settingsIcon.setFont(Font.font(32));
        
        header.getChildren().addAll(titleSection, spacer, settingsIcon);
        return header;
    }
    
    private VBox createLanguageSettingsSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(25));
        section.setStyle(createCardStyle());
        
        // Section header
        HBox sectionHeader = new HBox(15);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label("🌐");
        icon.setFont(Font.font(24));
        
        VBox headerText = new VBox(3);
        Label sectionTitle = new Label("Language & Region");
        sectionTitle.setTextFill(TEXT_PRIMARY);
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        Label sectionDesc = new Label("Choose your preferred language");
        sectionDesc.setTextFill(TEXT_SECONDARY);
        sectionDesc.setFont(Font.font("Segoe UI", 12));
        
        headerText.getChildren().addAll(sectionTitle, sectionDesc);
        sectionHeader.getChildren().addAll(icon, headerText);
        
        // Language selection
        HBox languageRow = new HBox(20);
        languageRow.setAlignment(Pos.CENTER_LEFT);
        
        Label languageLabel = new Label("Application Language:");
        languageLabel.setTextFill(TEXT_SECONDARY);
        languageLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll(languageManager.getAvailableLanguages());
        
        // Set current language
        String currentLang = languageManager.getCurrentLanguage();
        if ("vi".equals(currentLang)) {
            languageCombo.setValue("Tiếng Việt");
        } else {
            languageCombo.setValue("English");
        }
        
        languageCombo.setStyle(createComboBoxStyle());
        languageCombo.setPrefWidth(200);
        
        languageCombo.setOnAction(e -> {
            String selected = languageCombo.getValue();
            String langCode = "English".equals(selected) ? "en" : "vi";
            
            // Show confirmation dialog
            showLanguageChangeConfirmation(langCode);
        });
        
        Button testButton = new Button("🔄 Apply Changes");
        testButton.setStyle(createPrimaryButtonStyle());
        testButton.setOnAction(e -> {
            showSuccessMessage("Language settings applied successfully!");
        });
        
        languageRow.getChildren().addAll(languageLabel, languageCombo, testButton);
        
        // Additional language options
        VBox additionalOptions = new VBox(10);
        
        CheckBox rtlCheckBox = new CheckBox("Right-to-left text direction");
        rtlCheckBox.setTextFill(TEXT_SECONDARY);
        rtlCheckBox.setFont(Font.font("Segoe UI", 12));
        rtlCheckBox.setDisable(true); // Not implemented yet
        
        CheckBox dateFormatCheckBox = new CheckBox("Use local date format");
        dateFormatCheckBox.setTextFill(TEXT_SECONDARY);
        dateFormatCheckBox.setFont(Font.font("Segoe UI", 12));
        dateFormatCheckBox.setSelected(true);
        
        additionalOptions.getChildren().addAll(rtlCheckBox, dateFormatCheckBox);
        
        section.getChildren().addAll(sectionHeader, new Separator(), languageRow, additionalOptions);
        return section;
    }
    
    private VBox createAppSettingsSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(25));
        section.setStyle(createCardStyle());
        
        // Section header
        HBox sectionHeader = new HBox(15);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label("🎨");
        icon.setFont(Font.font(24));
        
        VBox headerText = new VBox(3);
        Label sectionTitle = new Label("Appearance & Behavior");
        sectionTitle.setTextFill(TEXT_PRIMARY);
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        Label sectionDesc = new Label("Customize how the application looks and behaves");
        sectionDesc.setTextFill(TEXT_SECONDARY);
        sectionDesc.setFont(Font.font("Segoe UI", 12));
        
        headerText.getChildren().addAll(sectionTitle, sectionDesc);
        sectionHeader.getChildren().addAll(icon, headerText);
        
        // Theme selection
        HBox themeRow = new HBox(20);
        themeRow.setAlignment(Pos.CENTER_LEFT);
        
        Label themeLabel = new Label("Theme:");
        themeLabel.setTextFill(TEXT_SECONDARY);
        themeLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        ComboBox<String> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll("Dark Theme", "Light Theme", "Auto");
        themeCombo.setValue("Dark Theme");
        themeCombo.setStyle(createComboBoxStyle());
        themeCombo.setPrefWidth(150);
        
        themeRow.getChildren().addAll(themeLabel, themeCombo);
        
        // Currency settings
        HBox currencyRow = new HBox(20);
        currencyRow.setAlignment(Pos.CENTER_LEFT);
        
        Label currencyLabel = new Label("Currency:");
        currencyLabel.setTextFill(TEXT_SECONDARY);
        currencyLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        ComboBox<String> currencyCombo = new ComboBox<>();
        currencyCombo.getItems().addAll("USD ($)", "VND (₫)", "EUR (€)", "GBP (£)");
        currencyCombo.setValue("USD ($)");
        currencyCombo.setStyle(createComboBoxStyle());
        currencyCombo.setPrefWidth(150);
        
        currencyRow.getChildren().addAll(currencyLabel, currencyCombo);
        
        // Notification settings
        VBox notificationSettings = new VBox(10);
        Label notifLabel = new Label("Notifications:");
        notifLabel.setTextFill(TEXT_SECONDARY);
        notifLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        CheckBox budgetAlerts = new CheckBox("Budget limit alerts");
        budgetAlerts.setTextFill(TEXT_SECONDARY);
        budgetAlerts.setSelected(true);
        
        CheckBox transactionNotifs = new CheckBox("Transaction notifications");
        transactionNotifs.setTextFill(TEXT_SECONDARY);
        transactionNotifs.setSelected(false);
        
        CheckBox monthlyReports = new CheckBox("Monthly financial reports");
        monthlyReports.setTextFill(TEXT_SECONDARY);
        monthlyReports.setSelected(true);
        
        notificationSettings.getChildren().addAll(notifLabel, budgetAlerts, transactionNotifs, monthlyReports);
        
        section.getChildren().addAll(
            sectionHeader, new Separator(), 
            themeRow, currencyRow, notificationSettings
        );
        return section;
    }
    
    private VBox createDataManagementSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(25));
        section.setStyle(createCardStyle());
        
        // Section header
        HBox sectionHeader = new HBox(15);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label("🗄️");
        icon.setFont(Font.font(24));
        
        VBox headerText = new VBox(3);
        Label sectionTitle = new Label("Data Management");
        sectionTitle.setTextFill(TEXT_PRIMARY);
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        Label sectionDesc = new Label("Backup, export, and manage your financial data");
        sectionDesc.setTextFill(TEXT_SECONDARY);
        sectionDesc.setFont(Font.font("Segoe UI", 12));
        
        headerText.getChildren().addAll(sectionTitle, sectionDesc);
        sectionHeader.getChildren().addAll(icon, headerText);
        
        // Data action buttons
        HBox dataActions = new HBox(15);
        dataActions.setAlignment(Pos.CENTER_LEFT);
        
        Button exportButton = new Button("📤 Export Data");
        exportButton.setStyle(createSecondaryButtonStyle());
        exportButton.setOnAction(e -> exportAllData());
        
        Button importButton = new Button("📥 Import Data");
        importButton.setStyle(createSecondaryButtonStyle());
        importButton.setOnAction(e -> importData());
        
        Button backupButton = new Button("💾 Create Backup");
        backupButton.setStyle(createPrimaryButtonStyle());
        backupButton.setOnAction(e -> createBackup());
        
        dataActions.getChildren().addAll(exportButton, importButton, backupButton);
        
        // Database info
        VBox dbInfo = new VBox(10);
        Label dbInfoLabel = new Label("Database Information:");
        dbInfoLabel.setTextFill(TEXT_SECONDARY);
        dbInfoLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        Label recordsInfo = new Label("Total Records: " + getTotalRecordsCount());
        recordsInfo.setTextFill(TEXT_MUTED);
        recordsInfo.setFont(Font.font("Segoe UI", 12));
        
        Label dbSizeInfo = new Label("Database Size: ~2.3 MB");
        dbSizeInfo.setTextFill(TEXT_MUTED);
        dbSizeInfo.setFont(Font.font("Segoe UI", 12));
        
        dbInfo.getChildren().addAll(dbInfoLabel, recordsInfo, dbSizeInfo);
        
        // Dangerous actions
        VBox dangerZone = new VBox(15);
        dangerZone.setPadding(new Insets(15));
        dangerZone.setStyle(
            "-fx-background-color: " + toRgbaString(Color.web("#FF6B6B"), 0.1) + ";" +
            "-fx-background-radius: 10px;" +
            "-fx-border-color: " + toRgbaString(Color.web("#FF6B6B"), 0.3) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 10px;"
        );
        
        Label dangerLabel = new Label("⚠️ Danger Zone");
        dangerLabel.setTextFill(Color.web("#FF6B6B"));
        dangerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        Button clearDataButton = new Button("🗑️ Clear All Data");
        clearDataButton.setStyle(createDangerButtonStyle());
        clearDataButton.setOnAction(e -> confirmClearAllData());
        
        dangerZone.getChildren().addAll(dangerLabel, clearDataButton);
        
        section.getChildren().addAll(
            sectionHeader, new Separator(), 
            dataActions, dbInfo, dangerZone
        );
        return section;
    }
    
    private VBox createAboutSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(25));
        section.setStyle(createCardStyle());
        
        // Section header
        HBox sectionHeader = new HBox(15);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label("ℹ️");
        icon.setFont(Font.font(24));
        
        VBox headerText = new VBox(3);
        Label sectionTitle = new Label("About Cashflow");
        sectionTitle.setTextFill(TEXT_PRIMARY);
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        Label sectionDesc = new Label("Application information and support");
        sectionDesc.setTextFill(TEXT_SECONDARY);
        sectionDesc.setFont(Font.font("Segoe UI", 12));
        
        headerText.getChildren().addAll(sectionTitle, sectionDesc);
        sectionHeader.getChildren().addAll(icon, headerText);
        
        // App info
        VBox appInfo = new VBox(10);
        
        Label versionLabel = new Label("Version: 2.1.0");
        versionLabel.setTextFill(TEXT_SECONDARY);
        versionLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        Label buildLabel = new Label("Build: 2025.06.22");
        buildLabel.setTextFill(TEXT_MUTED);
        buildLabel.setFont(Font.font("Segoe UI", 12));
        
        Label authorLabel = new Label("Developed with ❤️ by Nguyễn Tâm");
        authorLabel.setTextFill(TEXT_MUTED);
        authorLabel.setFont(Font.font("Segoe UI", 12));
        
        appInfo.getChildren().addAll(versionLabel, buildLabel, authorLabel);
        
        // Action buttons
        HBox aboutActions = new HBox(15);
        aboutActions.setAlignment(Pos.CENTER_LEFT);
        
        Button checkUpdatesButton = new Button("🔄 Check Updates");
        checkUpdatesButton.setStyle(createSecondaryButtonStyle());
        checkUpdatesButton.setOnAction(e -> checkForUpdates());
        
        Button supportButton = new Button("💬 Support");
        supportButton.setStyle(createSecondaryButtonStyle());
        supportButton.setOnAction(e -> openSupport());
        
        Button licenseButton = new Button("📄 License");
        licenseButton.setStyle(createSecondaryButtonStyle());
        licenseButton.setOnAction(e -> showLicense());
        
        aboutActions.getChildren().addAll(checkUpdatesButton, supportButton, licenseButton);
        
        section.getChildren().addAll(
            sectionHeader, new Separator(), 
            appInfo, aboutActions
        );
        return section;
    }
    
    // Event handlers
    private void showLanguageChangeConfirmation(String langCode) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Change Language");
        confirmDialog.setHeaderText("Confirm Language Change");
        confirmDialog.setContentText("Do you want to change the language? The application will need to restart.");
        
        ButtonType yesButton = new ButtonType("Yes, Change");
        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(yesButton, noButton);
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                languageManager.setLanguage(langCode);
                showSuccessMessage("Language changed! Please restart the application.");
            }
        });
    }
    
    private void exportAllData() {
        showInfoMessage("Export functionality will be implemented soon!");
    }
    
    private void importData() {
        try {
            Stage stage = (Stage) ((VBox) createSettingsPageContent()).getScene().getWindow();
            BackupManager backupManager = new BackupManager(dbManager, stage);
            backupManager.restoreBackupWithValidation();
        } catch (Exception e) {
            showErrorMessage("Failed to access backup manager: " + e.getMessage());
        }
    }
    
    private void createBackup() {
        try {
            Stage stage = (Stage) ((VBox) createSettingsPageContent()).getScene().getWindow();
            BackupManager backupManager = new BackupManager(dbManager, stage);
            backupManager.createBackupWithProgress();
        } catch (Exception e) {
            showErrorMessage("Failed to access backup manager: " + e.getMessage());
        }
    }
    
    private void createQuickBackup() {
        try {
            String quickBackupPath = "quick_backup_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".cbk";
            
            Task<Boolean> quickBackupTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    // Simple backup without file chooser
                    return dbManager.backupDatabase(quickBackupPath);
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        if (getValue()) {
                            showSuccessMessage("Quick backup created: " + quickBackupPath);
                        } else {
                            showErrorMessage("Quick backup failed");
                        }
                    });
                }
            };

            new Thread(quickBackupTask).start();
            
        } catch (Exception e) {
            showErrorMessage("Quick backup error: " + e.getMessage());
        }
    }
    
    private void confirmClearAllData() {
        Alert confirmDialog = new Alert(Alert.AlertType.WARNING);
        confirmDialog.setTitle("Clear All Data");
        confirmDialog.setHeaderText("⚠️ THIS ACTION CANNOT BE UNDONE!");
        confirmDialog.setContentText("Are you absolutely sure you want to delete all your financial data?");
        
        ButtonType deleteButton = new ButtonType("Yes, Delete Everything", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(deleteButton, cancelButton);
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == deleteButton) {
                // Implement clear all data
                showWarningMessage("Clear all data functionality needs to be implemented");
            }
        });
    }
    
    private void checkForUpdates() {
        showInfoMessage("You are running the latest version!");
    }
    
    private void openSupport() {
        showInfoMessage("Support contact: ntam.dev@example.com");
    }
    
    private void showLicense() {
        Alert licenseDialog = new Alert(Alert.AlertType.INFORMATION);
        licenseDialog.setTitle("License Information");
        licenseDialog.setHeaderText("Cashflow - Personal Finance Manager");
        licenseDialog.setContentText("MIT License\n\nCopyright (c) 2025 Nguyễn Tâm\n\nPermission is hereby granted, free of charge...");
        licenseDialog.showAndWait();
    }
    
    private int getTotalRecordsCount() {
        return dbManager.getIncomeRecordsCount() + dbManager.getOutcomeRecordsCount();
    }
    
    // Styling methods
    private String createCardStyle() {
        return "-fx-background-color: " + toHexString(CARD_COLOR) + ";" +
               "-fx-background-radius: 20px;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);";
    }
    
    private String createComboBoxStyle() {
        return "-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";" +
               "-fx-text-fill: " + toHexString(TEXT_PRIMARY) + ";" +
               "-fx-background-radius: 10px;" +
               "-fx-border-color: " + toRgbaString(ACCENT_COLOR, 0.3) + ";" +
               "-fx-border-width: 1px;" +
               "-fx-border-radius: 10px;";
    }
    
    private String createPrimaryButtonStyle() {
        return "-fx-background-color: " + toHexString(ACCENT_COLOR) + ";" +
               "-fx-text-fill: white;" +
               "-fx-background-radius: 10px;" +
               "-fx-padding: 10 20 10 20;" +
               "-fx-font-size: 13px;" +
               "-fx-font-weight: bold;" +
               "-fx-cursor: hand;" +
               "-fx-effect: dropshadow(three-pass-box, " + toRgbaString(ACCENT_COLOR, 0.4) + ", 10, 0, 0, 3);";
    }
    
    private String createSecondaryButtonStyle() {
        return "-fx-background-color: " + toRgbaString(TEXT_PRIMARY, 0.1) + ";" +
               "-fx-text-fill: " + toHexString(TEXT_PRIMARY) + ";" +
               "-fx-background-radius: 10px;" +
               "-fx-padding: 10 20 10 20;" +
               "-fx-font-size: 13px;" +
               "-fx-font-weight: 500;" +
               "-fx-cursor: hand;" +
               "-fx-border-color: " + toRgbaString(TEXT_PRIMARY, 0.2) + ";" +
               "-fx-border-width: 1px;" +
               "-fx-border-radius: 10px;";
    }
    
    private String createDangerButtonStyle() {
        return "-fx-background-color: " + toHexString(Color.web("#FF6B6B")) + ";" +
               "-fx-text-fill: white;" +
               "-fx-background-radius: 10px;" +
               "-fx-padding: 10 20 10 20;" +
               "-fx-font-size: 13px;" +
               "-fx-font-weight: bold;" +
               "-fx-cursor: hand;";
    }
    
    // Utility methods
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
    
    private String toRgbaString(Color color, double alpha) {
        return String.format("rgba(%.0f, %.0f, %.0f, %.2f)",
                color.getRed() * 255, color.getGreen() * 255, color.getBlue() * 255, alpha);
    }
    
    // Message methods
    private void showSuccessMessage(String message) {
        showMessage(message, Alert.AlertType.INFORMATION);
    }
    
    private void showErrorMessage(String message) {
        showMessage(message, Alert.AlertType.ERROR);
    }
    
    private void showWarningMessage(String message) {
        showMessage(message, Alert.AlertType.WARNING);
    }
    
    private void showInfoMessage(String message) {
        showMessage(message, Alert.AlertType.INFORMATION);
    }
    
    private void showMessage(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Settings");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 