package com.cashflow;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.logging.Level;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.ScaleTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.ParallelTransition;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.ProgressIndicator;

public class FullOutcomePage {

    private static final Logger logger = Logger.getLogger(FullOutcomePage.class.getName());

    // Enhanced color scheme
    private final Color OUTCOME_COLOR = Color.web("#8b5cf6"); // Purple theme for outcomes
    private final Color ACCENT_COLOR = Color.web("#4ECDC4");
    private final Color TEXT_PRIMARY = Color.WHITE;
    private final Color TEXT_SECONDARY = Color.web("#8892b0");
    private final Color TEXT_MUTED = Color.web("#64748b");
    private final Color SUCCESS_COLOR = Color.web("#4CAF50");
    private final Color ERROR_COLOR = Color.web("#FF6B6B");
    private final Color WARNING_COLOR = Color.web("#FF9800");
    private final Color CARD_COLOR = Color.web("#1e2749");
    private final Color BACKGROUND_COLOR = Color.web("#0f0f23");

    private DatabaseManager dbManager;
    private VBox outcomeListContainer;
    private HBox statsContainer;
    private TextField amountField;
    private TextField titleField;
    private ComboBox<String> categoryCombo;
    private ComboBox<String> paymentMethodCombo;
    private DatePicker datePicker;
    private TextArea descriptionArea;
    private CheckBox recurringCheckBox;
    private Button addButton;
    private OutcomeRecord editingRecord;
    private Label outcomeTotalLabel;

    // Static callback for dashboard refresh
    private static Runnable dashboardRefreshCallback;

    // Thêm fields cho filtering
    private ComboBox<String> dateRangeFilter;
    private ComboBox<String> sortFilter;
    private TextField searchField;
    private TextField minAmountField;
    private TextField maxAmountField;

    // Enhanced filtering fields
    private ComboBox<String> categoryFilter;
    private ComboBox<String> paymentMethodFilter;
    private ComboBox<String> recurringFilter;

    public FullOutcomePage(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public static void setDashboardRefreshCallback(Runnable callback) {
        dashboardRefreshCallback = callback;
    }

    private void notifyDashboardRefresh() {
        if (dashboardRefreshCallback != null) {
            Platform.runLater(() -> {
                System.out.println("Notifying dashboard refresh from Outcome page");
            });
        }
    }

    public VBox createOutcomePageContent() {
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30));
        mainContent.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");

        // Header with breadcrumb
        HBox header = createOutcomeHeader();

        // Stats overview with real data
        HBox statsOverview = createOutcomeStatsOverview();

        // Main content area
        HBox mainArea = new HBox(25);
        mainArea.setAlignment(Pos.TOP_LEFT);

        // Left side - Add outcome form
        VBox addOutcomeSection = createAddOutcomeSection();

        // Right side - Outcome list
        VBox outcomeListSection = createOutcomeListSection();

        mainArea.getChildren().addAll(addOutcomeSection, outcomeListSection);

        mainContent.getChildren().addAll(header, statsOverview, mainArea);
        return mainContent;
    }

    private HBox createOutcomeHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label breadcrumb = new Label("Dashboard");
        breadcrumb.setTextFill(TEXT_SECONDARY);
        breadcrumb.setFont(Font.font("System", FontWeight.NORMAL, 14));
        breadcrumb.setStyle("-fx-cursor: hand;");

        Label separator = new Label("/");
        separator.setTextFill(TEXT_MUTED);
        separator.setFont(Font.font("System", FontWeight.NORMAL, 14));

        Label currentPage = new Label("Expense Management");
        currentPage.setTextFill(TEXT_PRIMARY);
        currentPage.setFont(Font.font("System", FontWeight.BOLD, 28));

        header.getChildren().addAll(breadcrumb, separator, currentPage);
        return header;
    }

    private HBox createOutcomeStatsOverview() {
        VBox statsSection = new VBox(20);

        // Section title
        Label statsTitle = new Label("Expense Overview");
        statsTitle.setTextFill(TEXT_PRIMARY);
        statsTitle.setFont(Font.font("System", FontWeight.BOLD, 20));

        // Stats container
        statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER_LEFT);

        updateStatsCardsAsync();

        VBox statsWrapper = new VBox(15);
        statsWrapper.getChildren().addAll(statsTitle, statsContainer);

        HBox container = new HBox();
        container.getChildren().add(statsWrapper);
        return container;
    }

    private void updateStatsCardsAsync() {
        showStatsLoading(true);

        Task<Void> updateTask = new Task<Void>() {
            private double currentMonthOutcome;
            private double yearlyOutcome;
            private int recordsCount;
            private double avgOutcome;
            private double recentWeekOutcome;

            @Override
            protected Void call() throws Exception {
                Thread.sleep(300);

                LocalDate now = LocalDate.now();
                
                // Lấy dữ liệu tháng hiện tại
                currentMonthOutcome = dbManager.getMonthlyOutcome(now);
                if (currentMonthOutcome == 0) {
                    currentMonthOutcome = dbManager.getLatestMonthOutcome();
                }
                
                yearlyOutcome = dbManager.getYearlyOutcome(now);
                if (yearlyOutcome == 0) {
                    yearlyOutcome = dbManager.getTotalOutcome();
                }
                
                recordsCount = dbManager.getOutcomeRecordsCount();
                avgOutcome = dbManager.getAverageMonthlyOutcome();
                
                recentWeekOutcome = dbManager.getWeeklyOutcome(now);
                if (recentWeekOutcome == 0) {
                    recentWeekOutcome = dbManager.getLatestWeekOutcome();
                }

                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    statsContainer.getChildren().clear();

                    String monthTitle = (dbManager.getMonthlyOutcome(LocalDate.now()) > 0) ? 
                        "This Month" : "Latest Month";
                    
                    String weekTitle = (dbManager.getWeeklyOutcome(LocalDate.now()) > 0) ? 
                        "This Week" : "Recent Period";

                    VBox monthlyCard = createEnhancedStatCard("💸", monthTitle,
                            "$" + String.format("%.0f", currentMonthOutcome),
                            "-5.2%", true, OUTCOME_COLOR, true);

                    VBox weeklyCard = createEnhancedStatCard("📅", weekTitle,
                            "$" + String.format("%.0f", recentWeekOutcome),
                            "+8.7%", true, WARNING_COLOR, false);

                    VBox yearlyCard = createEnhancedStatCard("📈", "Total Outcome",
                            "$" + String.format("%.0f", yearlyOutcome),
                            "+15.2%", true, ACCENT_COLOR, false);

                    VBox recordsCard = createEnhancedStatCard("📊", "Total Records",
                            String.valueOf(recordsCount), "", false, TEXT_SECONDARY, false);

                    VBox avgCard = createEnhancedStatCard("💵", "Monthly Average",
                            "$" + String.format("%.0f", avgOutcome), "", false, WARNING_COLOR, false);

                    statsContainer.getChildren().addAll(monthlyCard, weeklyCard, yearlyCard, recordsCard, avgCard);

                    animateStatsCards();
                    showStatsLoading(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showErrorMessage("Failed to load statistics");
                    showStatsLoading(false);
                });
            }
        };

        new Thread(updateTask).start();
    }

    private VBox createEnhancedStatCard(String icon, String title, String value,
                                        String change, boolean showChange, Color accentColor, boolean isMainCard) {
        VBox card = new VBox(20);
        card.setPadding(new Insets(isMainCard ? 30 : 20));
        card.setPrefWidth(isMainCard ? 300 : 240);
        card.setPrefHeight(isMainCard ? 160 : 130);

        // Enhanced card styling
        if (isMainCard) {
            card.setStyle(
                    "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, " +
                            toRgbaString(accentColor, 0.15) + " 0%, " +
                            toRgbaString(OUTCOME_COLOR, 0.1) + " 100%);" +
                            "-fx-background-radius: 20px;" +
                            "-fx-border-color: " + toRgbaString(accentColor, 0.3) + ";" +
                            "-fx-border-width: 1.5px;" +
                            "-fx-border-radius: 20px;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 20, 0, 0, 8);"
            );
        } else {
            card.setStyle(
                    "-fx-background-color: " + toRgbaString(CARD_COLOR, 0.7) + ";" +
                            "-fx-background-radius: 16px;" +
                            "-fx-border-color: " + toRgbaString(TEXT_PRIMARY, 0.1) + ";" +
                            "-fx-border-width: 1px;" +
                            "-fx-border-radius: 16px;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 5);"
            );
        }

        // Enhanced header với icon
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        Circle iconBg = new Circle(isMainCard ? 22 : 18);
        iconBg.setFill(toRgbaColor(accentColor, 0.2));
        iconBg.setEffect(new DropShadow(4, toRgbaColor(accentColor, 0.4)));

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(isMainCard ? 20 : 16));

        iconContainer.getChildren().addAll(iconBg, iconLabel);

        VBox titleContainer = new VBox(3);
        Label titleLabel = new Label(title);
        titleLabel.setTextFill(TEXT_SECONDARY);
        titleLabel.setFont(Font.font("System", FontWeight.MEDIUM, isMainCard ? 15 : 13));

        if (isMainCard) {
            Label subtitleLabel = new Label("Current period");
            subtitleLabel.setTextFill(TEXT_MUTED);
            subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 11));
            titleContainer.getChildren().addAll(titleLabel, subtitleLabel);
        } else {
            titleContainer.getChildren().add(titleLabel);
        }

        header.getChildren().addAll(iconContainer, titleContainer);

        // Enhanced value section
        VBox valueSection = new VBox(8);

        Label valueLabel = new Label(value);
        valueLabel.setTextFill(TEXT_PRIMARY);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, isMainCard ? 32 : 24));

        if (showChange && !change.isEmpty()) {
            HBox changeContainer = new HBox(8);
            changeContainer.setAlignment(Pos.CENTER_LEFT);

            Label changeLabel = new Label(change);
            Color changeColor = change.startsWith("-") ? SUCCESS_COLOR : ERROR_COLOR;
            changeLabel.setTextFill(changeColor);
            changeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            changeLabel.setStyle(
                    "-fx-background-color: " + toRgbaString(changeColor, 0.15) + ";" +
                            "-fx-background-radius: 10px;" +
                            "-fx-padding: 4 8 4 8;"
            );

            Label trendIcon = new Label(change.startsWith("-") ? "📉" : "📈");
            trendIcon.setFont(Font.font(10));

            changeContainer.getChildren().addAll(changeLabel, trendIcon);
            valueSection.getChildren().addAll(valueLabel, changeContainer);
        } else {
            valueSection.getChildren().add(valueLabel);
        }

        card.getChildren().addAll(header, valueSection);

        // Add hover animation
        card.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), card);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });

        card.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        return card;
    }

    private void showStatsLoading(boolean show) {
        if (show) {
            statsContainer.getChildren().clear();
            for (int i = 0; i < 5; i++) {
                VBox placeholder = new VBox(20);
                placeholder.setPadding(new Insets(20));
                placeholder.setPrefWidth(240);
                placeholder.setPrefHeight(130);
                placeholder.setStyle("-fx-background-color: " + toRgbaString(CARD_COLOR, 0.5) + "; -fx-background-radius: 16px;");
                statsContainer.getChildren().add(placeholder);
            }
        }
    }

    private void animateStatsCards() {
        for (int i = 0; i < statsContainer.getChildren().size(); i++) {
            VBox card = (VBox) statsContainer.getChildren().get(i);
            card.setOpacity(0);
            card.setTranslateY(20);

            FadeTransition ft = new FadeTransition(Duration.millis(300), card);
            ft.setToValue(1);
            ft.setDelay(Duration.millis(i * 80));

            TranslateTransition tt = new TranslateTransition(Duration.millis(300), card);
            tt.setToY(0);
            tt.setDelay(Duration.millis(i * 80));

            ParallelTransition pt = new ParallelTransition(ft, tt);
            pt.play();
        }
    }

    private Color toRgbaColor(Color color, double alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private VBox createAddOutcomeSection() {
        VBox section = new VBox(20);
        section.setPrefWidth(400);

        // Title
        Label title = new Label("Add New Expense");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Form card
        VBox formCard = new VBox(20);
        formCard.setPadding(new Insets(25));
        formCard.setStyle(
                "-fx-background-color: " + toHexString(CARD_COLOR) + ";" +
                        "-fx-background-radius: 15px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 3);"
        );

        // Form fields
        VBox amountFieldContainer = createAmountField();
        VBox titleFieldContainer = createTitleField();
        VBox categoryFieldContainer = createCategoryDropdown();
        VBox paymentMethodContainer = createPaymentMethodDropdown();
        VBox dateFieldContainer = createDateField();
        VBox descriptionFieldContainer = createDescriptionField();
        VBox recurringContainer = createRecurringCheckbox();

        // Add button
        Button addButton = createEnhancedAddButton();

        // Cancel button (only visible in edit mode)
        Button cancelButton = new Button("Cancel Edit");
        cancelButton.setPrefWidth(Double.MAX_VALUE);
        cancelButton.setPrefHeight(35);
        cancelButton.setStyle(
                "-fx-background-color: #6c757d;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;"
        );
        cancelButton.setVisible(false);
        cancelButton.setOnAction(e -> {
            clearForm();
            cancelButton.setVisible(false);
        });

        formCard.getChildren().addAll(
                amountFieldContainer,
                titleFieldContainer,
                categoryFieldContainer,
                paymentMethodContainer,
                dateFieldContainer,
                descriptionFieldContainer,
                recurringContainer,
                addButton,
                cancelButton
        );

        section.getChildren().addAll(title, formCard);
        
        setupFormValidation();
        
        return section;
    }

    private VBox createAmountField() {
        VBox field = new VBox(8);

        Label label = new Label("Amount");
        label.setTextFill(TEXT_SECONDARY);
        label.setFont(Font.font("System", FontWeight.MEDIUM, 14));

        amountField = new TextField();
        amountField.setPromptText("Enter amount (e.g., 150.00)");
        amountField.setPrefHeight(40);
        amountField.setStyle(createEnhancedTextFieldStyle());

        // Focus styling
        amountField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            updateFieldFocus(amountField, isNowFocused);
        });

        field.getChildren().addAll(label, amountField);
        return field;
    }

    private VBox createTitleField() {
        VBox field = new VBox(8);

        Label label = new Label("Title/Description");
        label.setTextFill(TEXT_SECONDARY);
        label.setFont(Font.font("System", FontWeight.MEDIUM, 14));

        titleField = new TextField();
        titleField.setPromptText("e.g., Grocery Shopping, Electric Bill");
        titleField.setPrefHeight(40);
        titleField.setStyle(createEnhancedTextFieldStyle());

        // Focus styling
        titleField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            updateFieldFocus(titleField, isNowFocused);
        });

        field.getChildren().addAll(label, titleField);
        return field;
    }

    private VBox createCategoryDropdown() {
        VBox field = new VBox(8);
        
        Label label = new Label("Category");
        label.setTextFill(TEXT_SECONDARY);
        label.setFont(Font.font("System", FontWeight.MEDIUM, 14));
        
        categoryCombo = new ComboBox<>();
        // Lấy danh mục động từ database
        List<String> categories = dbManager.getOutcomeCategories();
        if (categories != null && !categories.isEmpty()) {
            categoryCombo.getItems().addAll(categories);
        } else {
            categoryCombo.getItems().addAll(
                "Food", "Transportation", "Housing", "Utilities", "Entertainment", 
                "Healthcare", "Education", "Shopping", "Travel", "Groceries", "Other"
            );
        }
        categoryCombo.setValue(categoryCombo.getItems().isEmpty() ? "Other" : categoryCombo.getItems().get(0));
        categoryCombo.setPrefWidth(Double.MAX_VALUE);
        categoryCombo.setEditable(false);
        categoryCombo.setStyle(createEnhancedComboBoxStyle());
        
        field.getChildren().addAll(label, categoryCombo);
        return field;
    }

    private VBox createPaymentMethodDropdown() {
        VBox field = new VBox(8);

        Label label = new Label("Payment Method");
        label.setTextFill(TEXT_SECONDARY);
        label.setFont(Font.font("System", FontWeight.MEDIUM, 14));

        paymentMethodCombo = new ComboBox<>();
        paymentMethodCombo.getItems().addAll(
                "Cash", "Credit Card", "Debit Card", "Bank Transfer", "Digital Wallet", "Check"
        );
        paymentMethodCombo.setValue("Cash");
        paymentMethodCombo.setPrefWidth(Double.MAX_VALUE);
        paymentMethodCombo.setEditable(false);
        paymentMethodCombo.setStyle(createEnhancedComboBoxStyle());

        field.getChildren().addAll(label, paymentMethodCombo);
        return field;
    }

    private VBox createDateField() {
        VBox field = new VBox(8);

        Label label = new Label("Date");
        label.setTextFill(TEXT_SECONDARY);
        label.setFont(Font.font("System", FontWeight.MEDIUM, 14));

        datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(Double.MAX_VALUE);
        datePicker.setEditable(false);
        datePicker.setStyle(createEnhancedComboBoxStyle());

        field.getChildren().addAll(label, datePicker);
        return field;
    }

    private VBox createDescriptionField() {
        VBox field = new VBox(8);

        Label label = new Label("Additional Notes (Optional)");
        label.setTextFill(TEXT_SECONDARY);
        label.setFont(Font.font("System", FontWeight.MEDIUM, 14));

        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Add any additional details...");
        descriptionArea.setPrefHeight(80);
        descriptionArea.setWrapText(true);
        descriptionArea.setStyle(
                "-fx-background-color: #2a3441;" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: " + toHexString(TEXT_MUTED) + ";" +
                        "-fx-border-color: transparent;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-font-size: 14px;" +
                        "-fx-control-inner-background: #2a3441;"
        );

        field.getChildren().addAll(label, descriptionArea);
        return field;
    }

    private VBox createRecurringCheckbox() {
        VBox field = new VBox(8);

        recurringCheckBox = new CheckBox("Recurring Expense");
        recurringCheckBox.setTextFill(TEXT_SECONDARY);
        recurringCheckBox.setFont(Font.font("System", FontWeight.MEDIUM, 14));
        recurringCheckBox.setStyle(
                "-fx-text-fill: " + toHexString(TEXT_SECONDARY) + ";" +
                        "-fx-font-size: 14px;"
        );

        Label helpText = new Label("Check if this is a monthly recurring expense");
        helpText.setTextFill(TEXT_MUTED);
        helpText.setFont(Font.font("System", FontWeight.NORMAL, 12));

        field.getChildren().addAll(recurringCheckBox, helpText);
        return field;
    }

    private Button createEnhancedAddButton() {
        addButton = new Button("💸 Add Expense");
        addButton.setPrefWidth(160);
        addButton.setPrefHeight(50);
        addButton.setStyle(createDisabledButtonStyle());
        addButton.setDisable(true);

        addButton.setOnAction(e -> {
            addRotateAnimation(addButton);
            handleAddOrUpdateOutcome();
        });

        return addButton;
    }

    private void addRotateAnimation(Button button) {
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(600), button);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(1);
        rotateTransition.setAutoReverse(false);
        
        // Thêm hiệu ứng scale nhẹ
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(300), button);
        scaleOut.setToX(1.1);
        scaleOut.setToY(1.1);
        
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), button);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setDelay(Duration.millis(300));
        
        ParallelTransition parallel = new ParallelTransition(rotateTransition, scaleOut);
        
        parallel.setOnFinished(event -> scaleIn.play());
        parallel.play();
    }

    private VBox createOutcomeListSection() {
        VBox section = new VBox(20);
        section.setPrefWidth(600);
        
        // Enhanced header with filters
        VBox listHeader = createAdvancedOutcomeFilterHeader();
        
        // Outcome list container
        ScrollPane scrollPane = new ScrollPane();
        outcomeListContainer = new VBox(10);
        scrollPane.setContent(outcomeListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        // Load initial data with filters
        refreshOutcomeListWithFilters();
        
        section.getChildren().addAll(listHeader, scrollPane);
        return section;
    }

    // Tạo advanced filter header
    private VBox createAdvancedOutcomeFilterHeader() {
        VBox headerContainer = new VBox(15);
        
        // Title row
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Recent Expenses");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        outcomeTotalLabel = new Label("Total: $0");
        outcomeTotalLabel.setTextFill(TEXT_SECONDARY);
        outcomeTotalLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        
        titleRow.getChildren().addAll(title, spacer, outcomeTotalLabel);
        
        // Filter controls
        HBox filterRow = new HBox(15);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        
        // Search field
        searchField = new TextField();
        searchField.setPromptText("🔍 Search expenses...");
        searchField.setPrefWidth(200);
        searchField.setStyle(createEnhancedTextFieldStyle());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshOutcomeListWithFilters());
        
        // Date filter
        dateRangeFilter = new ComboBox<>();
        dateRangeFilter.getItems().addAll(
            "All Time", "Today", "This Week", "This Month", "This Year",
            "Last Week", "Last Month", "Last Year"
        );
        dateRangeFilter.setValue("All Time");
        dateRangeFilter.setStyle(createEnhancedComboBoxStyle());
        dateRangeFilter.setOnAction(e -> refreshOutcomeListWithFilters());
        
        // Sort filter
        sortFilter = new ComboBox<>();
        sortFilter.getItems().addAll(
            "Date (Newest)", "Date (Oldest)", 
            "Amount (High to Low)", "Amount (Low to High)"
        );
        sortFilter.setValue("Date (Newest)");
        sortFilter.setStyle(createEnhancedComboBoxStyle());
        sortFilter.setOnAction(e -> refreshOutcomeListWithFilters());
        
        filterRow.getChildren().addAll(searchField, dateRangeFilter, sortFilter);
        
        // Amount range filter
        HBox amountRow = new HBox(10);
        amountRow.setAlignment(Pos.CENTER_LEFT);
        
        Label amountLabel = new Label("Amount:");
        amountLabel.setTextFill(TEXT_SECONDARY);
        amountLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        
        minAmountField = new TextField();
        minAmountField.setPromptText("Min");
        minAmountField.setPrefWidth(80);
        minAmountField.setStyle(createEnhancedTextFieldStyle());
        minAmountField.textProperty().addListener((obs, oldVal, newVal) -> refreshOutcomeListWithFilters());
        
        Label toLabel = new Label("to");
        toLabel.setTextFill(TEXT_MUTED);
        
        maxAmountField = new TextField();
        maxAmountField.setPromptText("Max");
        maxAmountField.setPrefWidth(80);
        maxAmountField.setStyle(createEnhancedTextFieldStyle());
        maxAmountField.textProperty().addListener((obs, oldVal, newVal) -> refreshOutcomeListWithFilters());
        
        Button clearButton = new Button("Clear");
        clearButton.setStyle(createSecondaryButtonStyle());
        clearButton.setOnAction(e -> clearOutcomeFilters());
        
        amountRow.getChildren().addAll(amountLabel, minAmountField, toLabel, maxAmountField, clearButton);
        
        headerContainer.getChildren().addAll(titleRow, filterRow, amountRow);
        return headerContainer;
    }

    // Method để refresh outcome list với filters
    private void refreshOutcomeListWithFilters() {
        showOutcomeListLoading(true);

        Task<List<OutcomeRecord>> loadRecordsTask = new Task<List<OutcomeRecord>>() {
            @Override
            protected List<OutcomeRecord> call() throws Exception {
                Thread.sleep(200);
                
                // Lấy NHIỀU records hơn
                List<OutcomeRecord> allRecords = dbManager.getOutcomeRecords(100); // Tăng limit
                return applyAllOutcomeFilters(allRecords);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<OutcomeRecord> filteredRecords = getValue();
                    displayOutcomeRecordsWithAnimation(filteredRecords);
                    updateOutcomeListSummary(filteredRecords);
                    showOutcomeListLoading(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showOutcomeListError();
                    showOutcomeListLoading(false);
                });
            }
        };

        new Thread(loadRecordsTask).start();
    }

    private List<OutcomeRecord> applyAllOutcomeFilters(List<OutcomeRecord> records) {
        List<OutcomeRecord> result = new ArrayList<>(records);
        
        // 1. Enhanced text search
        String searchText = searchField != null ? searchField.getText().trim() : "";
        if (!searchText.isEmpty()) {
            result = result.stream()
                .filter(record -> matchesOutcomeSearchCriteria(record, searchText))
                .collect(Collectors.toList());
        }
        
        // 2. Advanced date filter
        if (dateRangeFilter != null) {
            String dateFilter = dateRangeFilter.getValue();
            result = applyOutcomeDateFilter(result, dateFilter);
        }
        
        // 3. Category filter
        if (categoryCombo != null && !categoryCombo.getValue().equals("All Categories")) {
            String category = categoryCombo.getValue();
            result = result.stream()
                .filter(record -> record.getCategory().equals(category))
                .collect(Collectors.toList());
        }
        
        // 4. Payment method filter
        if (paymentMethodCombo != null && !paymentMethodCombo.getValue().equals("All Methods")) {
            String method = paymentMethodCombo.getValue();
            result = result.stream()
                .filter(record -> record.getPaymentMethod().equals(method))
                .collect(Collectors.toList());
        }
        
        // 5. Recurring filter
        if (recurringCheckBox != null && !recurringCheckBox.isSelected()) {
            result = result.stream()
                .filter(record -> !record.isRecurring())
                .collect(Collectors.toList());
        }
        
        // 6. Amount range
        result = applyOutcomeAmountFilter(result);
        
        // 7. Sort
        if (sortFilter != null) {
            String sortType = sortFilter.getValue();
            result = applyOutcomeSortFilter(result, sortType);
        }
        
        return result;
    }

    private boolean matchesOutcomeSearchCriteria(OutcomeRecord record, String searchText) {
        String lowerSearch = searchText.toLowerCase();
        
        // Smart search với scoring
        int matchScore = 0;
        
        // Title search (highest priority)
        if (record.getTitle() != null && 
            record.getTitle().toLowerCase().contains(lowerSearch)) {
            matchScore += 10;
        }
        
        // Category search
        if (record.getCategory() != null && 
            record.getCategory().toLowerCase().contains(lowerSearch)) {
            matchScore += 8;
        }
        
        // Description search
        if (record.getDescription() != null && 
            record.getDescription().toLowerCase().contains(lowerSearch)) {
            matchScore += 6;
        }
        
        // Payment method search
        if (record.getPaymentMethod() != null && 
            record.getPaymentMethod().toLowerCase().contains(lowerSearch)) {
            matchScore += 4;
        }
        
        // Amount search
        try {
            double searchAmount = Double.parseDouble(searchText);
            if (Math.abs(record.getAmount() - searchAmount) < 0.01) {
                matchScore += 5;
            }
        } catch (NumberFormatException e) {
            // Continue with text search
        }
        
        // Date search
        String dateStr = record.getDate().toString();
        if (dateStr.contains(lowerSearch)) {
            matchScore += 3;
        }
        
        // Fuzzy matching for typos
        if (matchScore == 0) {
            matchScore += fuzzyMatch(record, lowerSearch);
        }
        
        return matchScore > 0;
    }

    private int fuzzyMatch(OutcomeRecord record, String search) {
        // Simple fuzzy matching for common typos
        String[] fields = {
            record.getTitle(),
            record.getCategory(),
            record.getDescription()
        };
        
        for (String field : fields) {
            if (field != null && levenshteinDistance(field.toLowerCase(), search) <= 2) {
                return 2; // Low score for fuzzy matches
            }
        }
        return 0;
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        Math.min(dp[i-1][j] + 1, dp[i][j-1] + 1),
                        dp[i-1][j-1] + (a.charAt(i-1) == b.charAt(j-1) ? 0 : 1)
                    );
                }
            }
        }
        
        return dp[a.length()][b.length()];
    }

    private List<OutcomeRecord> applyOutcomeDateFilter(List<OutcomeRecord> records, String dateFilter) {
        LocalDate now = LocalDate.now();
        
        return records.stream().filter(record -> {
            switch (dateFilter) {
                case "Today":
                    return record.getDate().equals(now);
                case "Yesterday":
                    return record.getDate().equals(now.minusDays(1));
                case "This Week":
                    LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);
                    LocalDate weekEnd = weekStart.plusDays(6);
                    return !record.getDate().isBefore(weekStart) && !record.getDate().isAfter(weekEnd);
                case "Last Week":
                    LocalDate lastWeekStart = now.minusWeeks(1).minusDays(now.getDayOfWeek().getValue() - 1);
                    LocalDate lastWeekEnd = lastWeekStart.plusDays(6);
                    return !record.getDate().isBefore(lastWeekStart) && !record.getDate().isAfter(lastWeekEnd);
                case "This Month":
                    return record.getDate().getYear() == now.getYear() && 
                           record.getDate().getMonth() == now.getMonth();
                case "Last Month":
                    LocalDate lastMonth = now.minusMonths(1);
                    return record.getDate().getYear() == lastMonth.getYear() && 
                           record.getDate().getMonth() == lastMonth.getMonth();
                case "This Year":
                    return record.getDate().getYear() == now.getYear();
                case "Last Year":
                    return record.getDate().getYear() == now.getYear() - 1;
                case "Last 7 Days":
                    return record.getDate().isAfter(now.minusDays(7));
                case "Last 30 Days":
                    return record.getDate().isAfter(now.minusDays(30));
                case "Last 90 Days":
                    return record.getDate().isAfter(now.minusDays(90));
                default: // "All Time"
                    return true;
            }
        }).collect(Collectors.toList());
    }

    private List<OutcomeRecord> applyOutcomeSortFilter(List<OutcomeRecord> records, String sortType) {
        return records.stream().sorted((r1, r2) -> {
            switch (sortType) {
                case "Date (Oldest)":
                    return r1.getDate().compareTo(r2.getDate());
                case "Amount (High to Low)":
                    return Double.compare(r2.getAmount(), r1.getAmount());
                case "Amount (Low to High)":
                    return Double.compare(r1.getAmount(), r2.getAmount());
                case "Title A-Z":
                    return r1.getTitle().compareToIgnoreCase(r2.getTitle());
                case "Title Z-A":
                    return r2.getTitle().compareToIgnoreCase(r1.getTitle());
                case "Category A-Z":
                    return r1.getCategory().compareToIgnoreCase(r2.getCategory());
                case "Recurring First":
                    return Boolean.compare(r2.isRecurring(), r1.isRecurring());
                default: // "Date (Newest)"
                    return r2.getDate().compareTo(r1.getDate());
            }
        }).collect(Collectors.toList());
    }

    // Enhanced display với micro-interactions
    private void displayOutcomeRecordsWithAnimation(List<OutcomeRecord> records) {
        outcomeListContainer.getChildren().clear();

        if (records.isEmpty()) {
            VBox emptyState = createEnhancedOutcomeEmptyState();
            outcomeListContainer.getChildren().add(emptyState);
            return;
        }

        // Grouped animation for better performance
        for (int i = 0; i < records.size(); i++) {
            OutcomeRecord record = records.get(i);
            HBox outcomeItem = createEnhancedOutcomeItem(record);
            outcomeListContainer.getChildren().add(outcomeItem);
            
            // Progressive enhancement animation
            animateOutcomeItemEntrance(outcomeItem, i);
        }
    }

    private void animateOutcomeItemEntrance(HBox item, int index) {
        // Initial state
        item.setOpacity(0);
        item.setTranslateX(-50);
        item.setScaleY(0.8);
        
        // Animate entrance
        Duration delay = Duration.millis(index * 50);
        Duration duration = Duration.millis(400);
        
        FadeTransition fade = new FadeTransition(duration, item);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(delay);
        
        TranslateTransition slide = new TranslateTransition(duration, item);
        slide.setFromX(-50);
        slide.setToX(0);
        slide.setDelay(delay);
        slide.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        ScaleTransition scale = new ScaleTransition(duration, item);
        scale.setFromY(0.8);
        scale.setToY(1.0);
        scale.setDelay(delay);
        scale.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        ParallelTransition entrance = new ParallelTransition(fade, slide, scale);
        entrance.play();
    }

    private void showOutcomeListLoading(boolean show) {
        if (show) {
            outcomeListContainer.getChildren().clear();
            
            VBox loadingContainer = new VBox(20);
            loadingContainer.setAlignment(Pos.CENTER);
            loadingContainer.setPadding(new Insets(50));

            // Animated spinner
            ProgressIndicator spinner = new ProgressIndicator();
            spinner.setStyle("-fx-accent: " + toHexString(OUTCOME_COLOR) + ";");
            spinner.setPrefSize(40, 40);

            Label loadingLabel = new Label("Filtering expenses...");
            loadingLabel.setTextFill(TEXT_SECONDARY);
            loadingLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

            loadingContainer.getChildren().addAll(spinner, loadingLabel);
            outcomeListContainer.getChildren().add(loadingContainer);
        }
    }

    private VBox createEnhancedOutcomeEmptyState() {
        VBox emptyState = new VBox(25);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(80));

        // Animated empty illustration
        StackPane illustration = new StackPane();
        Circle bg = new Circle(50);
        bg.setFill(toRgbaColor(OUTCOME_COLOR, 0.1));
        bg.setStroke(toRgbaColor(OUTCOME_COLOR, 0.3));
        bg.setStrokeWidth(2);
        
        Label emptyIcon = new Label("💸");
        emptyIcon.setFont(Font.font(36));
        
        illustration.getChildren().addAll(bg, emptyIcon);
        
        // Pulse animation
        ScaleTransition pulse = new ScaleTransition(Duration.millis(2000), illustration);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.1);
        pulse.setToY(1.1);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();

        Label emptyTitle = new Label("No expenses match your criteria");
        emptyTitle.setTextFill(TEXT_PRIMARY);
        emptyTitle.setFont(Font.font("System", FontWeight.BOLD, 20));

        Label emptyMessage = new Label("Try adjusting your filters or add a new expense record.");
        emptyMessage.setTextFill(TEXT_SECONDARY);
        emptyMessage.setFont(Font.font("System", FontWeight.NORMAL, 14));
        emptyMessage.setWrapText(true);

        // Quick action buttons
        HBox quickActions = new HBox(15);
        quickActions.setAlignment(Pos.CENTER);
        
        Button clearFiltersBtn = new Button("🗑️ Clear Filters");
        clearFiltersBtn.setStyle(createSecondaryButtonStyle());
        clearFiltersBtn.setOnAction(e -> clearAllOutcomeFilters());
        
        Button addExpenseBtn = new Button("💸 Add Expense");
        addExpenseBtn.setStyle(createActionButtonStyle(toHexString(OUTCOME_COLOR)));
        addExpenseBtn.setOnAction(e -> amountField.requestFocus());
        
        quickActions.getChildren().addAll(clearFiltersBtn, addExpenseBtn);

        emptyState.getChildren().addAll(illustration, emptyTitle, emptyMessage, quickActions);
        return emptyState;
    }

    private void clearAllOutcomeFilters() {
        if (searchField != null) searchField.clear();
        if (dateRangeFilter != null) dateRangeFilter.setValue("All Time");
        if (sortFilter != null) sortFilter.setValue("Date (Newest)");
        if (minAmountField != null) minAmountField.clear();
        if (maxAmountField != null) maxAmountField.clear();
        
        // Success feedback với animation
        showInfoMessage("All filters cleared!");
        refreshOutcomeListWithFilters();
    }

    private void showOutcomeListError() {
        outcomeListContainer.getChildren().clear();
        
        VBox errorContainer = new VBox(20);
        errorContainer.setAlignment(Pos.CENTER);
        errorContainer.setPadding(new Insets(50));
        
        Label errorIcon = new Label("⚠️");
        errorIcon.setFont(Font.font(32));
        
        Label errorMessage = new Label("Failed to load expense records");
        errorMessage.setTextFill(ERROR_COLOR);
        errorMessage.setFont(Font.font("System", FontWeight.MEDIUM, 16));
        
        Button retryButton = new Button("🔄 Try Again");
        retryButton.setStyle(createActionButtonStyle(toHexString(ERROR_COLOR)));
        retryButton.setOnAction(e -> refreshOutcomeListWithFilters());
        
        errorContainer.getChildren().addAll(errorIcon, errorMessage, retryButton);
        outcomeListContainer.getChildren().add(errorContainer);
    }

    private void updateOutcomeListSummary(List<OutcomeRecord> records) {
        double totalAmount = records.stream().mapToDouble(OutcomeRecord::getAmount).sum();
        String summaryText = String.format("Showing %d records • Total: $%.2f", 
            records.size(), totalAmount);
        
        if (outcomeTotalLabel != null) {
            outcomeTotalLabel.setText(summaryText);
            
            // Animate summary update
            FadeTransition fade = new FadeTransition(Duration.millis(300), outcomeTotalLabel);
            fade.setFromValue(0.5);
            fade.setToValue(1.0);
            fade.play();
        }
    }

    private HBox createEnhancedOutcomeItem(OutcomeRecord record) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 16, 12, 16));
        item.setStyle(
            "-fx-background-color: " + toRgbaString(CARD_COLOR, 0.8) + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + toRgbaString(ACCENT_COLOR, 0.2) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;" +
            "-fx-cursor: hand;"
        );

        // Category icon
        StackPane iconContainer = new StackPane();
        Circle iconBg = new Circle(20);
        iconBg.setFill(OUTCOME_COLOR);
        iconBg.setEffect(new DropShadow(5, OUTCOME_COLOR));

        Label icon = new Label(getCategoryIcon(record.getCategory()));
        icon.setFont(Font.font(14));
        icon.setTextFill(Color.WHITE);

        iconContainer.getChildren().addAll(iconBg, icon);

        // Record details
        VBox details = new VBox(4);
        
        Label titleLabel = new Label(record.getTitle());
        titleLabel.setTextFill(TEXT_PRIMARY);
        titleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));

        HBox metaRow = new HBox(15);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label categoryLabel = new Label("📂 " + record.getCategory());
        categoryLabel.setTextFill(TEXT_MUTED);
        categoryLabel.setFont(Font.font("System", FontWeight.NORMAL, 11));

        Label dateLabel = new Label("📅 " + record.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dateLabel.setTextFill(TEXT_MUTED);
        dateLabel.setFont(Font.font("System", FontWeight.NORMAL, 11));

        Label paymentLabel = new Label("💳 " + record.getPaymentMethod());
        paymentLabel.setTextFill(TEXT_MUTED);
        paymentLabel.setFont(Font.font("System", FontWeight.NORMAL, 11));

        metaRow.getChildren().addAll(categoryLabel, dateLabel, paymentLabel);

        // Add recurring badge if applicable
        if (record.isRecurring()) {
            Label recurringBadge = new Label("🔄 Recurring");
            recurringBadge.setTextFill(SUCCESS_COLOR);
            recurringBadge.setFont(Font.font("System", FontWeight.BOLD, 10));
            recurringBadge.setStyle(
                "-fx-background-color: " + toRgbaString(SUCCESS_COLOR, 0.15) + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-padding: 2 6 2 6;"
            );
            metaRow.getChildren().add(recurringBadge);
        }

        details.getChildren().addAll(titleLabel, metaRow);

        // Add description if available
        if (record.getDescription() != null && !record.getDescription().trim().isEmpty()) {
            Label descLabel = new Label(record.getDescription());
            descLabel.setTextFill(TEXT_SECONDARY);
            descLabel.setFont(Font.font("System", FontWeight.NORMAL, 11));
            descLabel.setWrapText(true);
            details.getChildren().add(descLabel);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Amount section
        VBox amountSection = new VBox(2);
        amountSection.setAlignment(Pos.CENTER_RIGHT);

        Label amountLabel = new Label("-$" + String.format("%.2f", record.getAmount()));
        amountLabel.setTextFill(OUTCOME_COLOR);
        amountLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        amountSection.getChildren().add(amountLabel);

        // Actions
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("✏️");
        editBtn.setStyle(createActionButtonStyle(toHexString(ACCENT_COLOR)));
        editBtn.setOnAction(e -> editOutcomeRecord(record));

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle(createActionButtonStyle(toHexString(ERROR_COLOR)));
        deleteBtn.setOnAction(e -> deleteOutcomeRecord(record));

        actions.getChildren().addAll(editBtn, deleteBtn);

        item.getChildren().addAll(iconContainer, details, spacer, amountSection, actions);

        // Enhanced hover effects
        item.setOnMouseEntered(e -> {
            item.setStyle(
                "-fx-background-color: " + toRgbaString(OUTCOME_COLOR, 0.1) + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: " + toRgbaString(OUTCOME_COLOR, 0.4) + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;" +
                "-fx-cursor: hand;"
            );

            ScaleTransition scale = new ScaleTransition(Duration.millis(100), item);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
        });

        item.setOnMouseExited(e -> {
            item.setStyle(
                "-fx-background-color: " + toRgbaString(CARD_COLOR, 0.8) + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: " + toRgbaString(ACCENT_COLOR, 0.2) + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;" +
                "-fx-cursor: hand;"
            );

            ScaleTransition scale = new ScaleTransition(Duration.millis(100), item);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        return item;
    }

    private void handleAddOrUpdateOutcome() {
        // Enhanced validation
        if (!validateOutcomeInput()) {
            return;
        }

        // Show loading state
        showOutcomeButtonLoading(true);

        // Create async task
        Task<Boolean> outcomeTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Thread.sleep(300); // Loading simulation

                // Get and validate form data
                double amount = Double.parseDouble(amountField.getText().trim());
                String title = titleField.getText().trim();
                String category = categoryCombo.getValue();
                String paymentMethod = paymentMethodCombo.getValue();
                LocalDate date = datePicker.getValue();
                String description = descriptionArea.getText().trim();
                boolean isRecurring = recurringCheckBox.isSelected();

                // Validate and set defaults
                if (category == null || category.trim().isEmpty()) {
                    category = "Other";
                }
                if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                    paymentMethod = "Cash";
                }
                if (date == null) {
                    date = LocalDate.now();
                }

                // Perform database operation
                boolean success;
                if (editingRecord != null) {
                    success = dbManager.updateOutcome(editingRecord.getId(), amount, title, category,
                            date, description, paymentMethod, isRecurring);
                } else {
                    success = dbManager.addOutcome(amount, title, category, date, description, paymentMethod, isRecurring);
                }

                return success;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    boolean success = getValue();
                    showOutcomeButtonLoading(false);

                    if (success) {
                        String message = editingRecord != null ? 
                            "💳 Expense updated successfully!" : "💳 Expense added successfully!";
                        showSuccessMessage(message);

                        // Clear form after successful add
                        if (editingRecord == null) {
                            clearOutcomeFormWithAnimation();
                        } else {
                            exitOutcomeEditMode();
                        }

                        // Refresh UI
                        refreshOutcomeListWithFilters();
                        updateStatsCardsAsync();
                        
                        // Refresh dashboard
                        try {
                            ModernCashflowApp.refreshDashboardFromOtherPage();
                        } catch (Exception e) {
                            System.out.println("Dashboard refresh failed: " + e.getMessage());
                        }
                        
                        notifyDashboardRefresh();

                        // Success animation
                        animateOutcomeSuccessAction();
                        
                    } else {
                        String message = editingRecord != null ? 
                            "❌ Failed to update expense" : "❌ Failed to add expense";
                        showErrorMessage(message + ". Please check your inputs and try again.");
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showOutcomeButtonLoading(false);
                    Throwable exception = getException();
                    String errorMessage = "❌ An error occurred: " + 
                        (exception != null ? exception.getMessage() : "Unknown error");
                    showErrorMessage(errorMessage);
                    
                    // Debug logging
                    if (exception != null) {
                        exception.printStackTrace();
                    }
                });
            }
        };

        new Thread(outcomeTask).start();
    }

    // Enhanced validation for outcome
    private boolean validateOutcomeInput() {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        // Validate amount
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            errors.append("💸 Amount is required\n");
            isValid = false;
        } else {
            try {
                double amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    errors.append("💸 Amount must be greater than 0\n");
                    isValid = false;
                } else if (amount > 100000) {
                    errors.append("💸 Amount seems too large. Please verify.\n");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                errors.append("💸 Please enter a valid amount\n");
                isValid = false;
            }
        }

        // Validate title
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            errors.append("📝 Title/Description is required\n");
            isValid = false;
        } else if (title.length() < 2) {
            errors.append("📝 Title must be at least 2 characters\n");
            isValid = false;
        }

        // Show validation errors - CHỈ khi có lỗi thật sự
        if (!isValid) {
            showErrorMessage("Please fix the following errors:\n\n" + errors.toString().trim());
        }

        return isValid;
    }

    // Loading state for outcome button
    private void showOutcomeButtonLoading(boolean show) {
        if (show) {
            addButton.setText("💫 Processing...");
            addButton.setDisable(true);
            addButton.setStyle(createOutcomeLoadingButtonStyle());
        } else {
            addButton.setText(editingRecord != null ? "💸 Update Expense" : "💸 Add Expense");
            addButton.setDisable(false);
            addButton.setStyle(createActionButtonStyle(toHexString(OUTCOME_COLOR)));
        }
    }

    // Success animation for outcome
    private void animateOutcomeSuccessAction() {
        // Success pulse animation
        ScaleTransition pulse = new ScaleTransition(Duration.millis(150), addButton);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.1);
        pulse.setToY(1.1);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.play();
        
        // Flash success color briefly
        String originalStyle = addButton.getStyle();
        addButton.setStyle(originalStyle.replace(
            toHexString(OUTCOME_COLOR), 
            toHexString(SUCCESS_COLOR)
        ));
        
        Timeline flashback = new Timeline(new javafx.animation.KeyFrame(
            Duration.millis(400),
            e -> addButton.setStyle(originalStyle)
        ));
        flashback.play();
    }

    // Clear form with animation
    private void clearOutcomeFormWithAnimation() {
        // Animate form clearing
        FadeTransition fade = new FadeTransition(Duration.millis(200), addButton.getParent());
        fade.setFromValue(1.0);
        fade.setToValue(0.7);
        fade.setOnFinished(e -> {
            clearOutcomeForm();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), addButton.getParent());
            fadeIn.setFromValue(0.7);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fade.play();
    }

    // Clear outcome form
    private void clearOutcomeForm() {
        amountField.clear();
        titleField.clear();
        categoryCombo.setValue(categoryCombo.getItems().isEmpty() ? "Food" : categoryCombo.getItems().get(0));
        paymentMethodCombo.setValue("Cash");
        datePicker.setValue(LocalDate.now());
        descriptionArea.clear();
        recurringCheckBox.setSelected(false);
        exitOutcomeEditMode();
    }

    // Exit edit mode
    private void exitOutcomeEditMode() {
        editingRecord = null;
        addButton.setText("💸 Add Expense");
        addButton.setStyle(createActionButtonStyle(toHexString(OUTCOME_COLOR)));
    }

    // Enhanced styling for loading button
    private String createOutcomeLoadingButtonStyle() {
        return "-fx-background-color: " + toRgbaString(OUTCOME_COLOR, 0.6) + ";" +
               "-fx-text-fill: white;" +
               "-fx-background-radius: 6px;" +
               "-fx-cursor: wait;" +
               "-fx-font-size: 12px;" +
               "-fx-opacity: 0.8;";
    }

    // Helper methods for styling and colors
    private String createEnhancedTextFieldStyle() {
        return "-fx-background-color: " + toRgbaString(CARD_COLOR, 0.6) + ";" +
               "-fx-text-fill: " + toHexString(TEXT_PRIMARY) + ";" +
               "-fx-prompt-text-fill: " + toHexString(TEXT_MUTED) + ";" +
               "-fx-border-color: " + toRgbaString(ACCENT_COLOR, 0.3) + ";" +
               "-fx-border-width: 1px;" +
               "-fx-border-radius: 8px;" +
               "-fx-background-radius: 8px;" +
               "-fx-font-size: 12px;" +
               "-fx-padding: 8px;";
    }

    private String createEnhancedComboBoxStyle() {
        return "-fx-background-color: " + toRgbaString(CARD_COLOR, 0.6) + ";" +
               "-fx-text-fill: " + toHexString(TEXT_PRIMARY) + ";" +
               "-fx-background-radius: 8px;" +
               "-fx-border-color: " + toRgbaString(ACCENT_COLOR, 0.3) + ";" +
               "-fx-border-width: 1px;" +
               "-fx-border-radius: 8px;";
    }

    private String createSecondaryButtonStyle() {
        return "-fx-background-color: " + toRgbaString(TEXT_PRIMARY, 0.1) + ";" +
               "-fx-text-fill: " + toHexString(TEXT_PRIMARY) + ";" +
               "-fx-font-size: 11px;" +
               "-fx-background-radius: 6px;" +
               "-fx-cursor: hand;" +
               "-fx-padding: 5 10 5 10;";
    }

    private void updateFieldFocus(TextField field, boolean focused) {
        String currentStyle = field.getStyle();
        String borderStyle = "-fx-border-width: 2px; -fx-border-color: " + toHexString(ACCENT_COLOR) + ";";
        
        if (focused) {
            if (!currentStyle.contains("-fx-border-width: 2px;")) {
                 field.setStyle(currentStyle + borderStyle);
            }
        } else {
            field.setStyle(currentStyle.replace(borderStyle, ""));
        }
    }

    private Color getCategoryColor(String category) {
        return switch (category.toLowerCase()) {
            case "food", "groceries" -> Color.web("#4CAF50");
            case "transportation" -> Color.web("#2196F3");
            case "housing" -> Color.web("#795548");
            case "utilities" -> Color.web("#FFC107");
            case "entertainment" -> Color.web("#9C27B0");
            case "healthcare" -> Color.web("#00BCD4");
            case "education" -> Color.web("#3F51B5");
            case "shopping" -> Color.web("#E91E63");
            case "travel" -> Color.web("#009688");
            default -> OUTCOME_COLOR;
        };
    }

    private String getCategoryIcon(String category) {
        return switch (category.toLowerCase()) {
            case "food" -> "🍔";
            case "groceries" -> "🛒";
            case "transportation" -> "🚗";
            case "housing" -> "🏠";
            case "utilities" -> "⚡";
            case "entertainment" -> "🎬";
            case "healthcare" -> "🏥";
            case "education" -> "📚";
            case "shopping" -> "🛍️";
            case "travel" -> "✈️";
            default -> "💸";
        };
    }

    private String getPaymentMethodIcon(String paymentMethod) {
        return switch (paymentMethod.toLowerCase()) {
            case "cash" -> "💵";
            case "credit card" -> "💳";
            case "debit card" -> "💳";
            case "bank transfer" -> "🏦";
            case "digital wallet" -> "📱";
            case "check" -> "📝";
            default -> "💰";
        };
    }

    // Color utility methods
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

    private String darkenColor(Color color) {
        return toHexString(color.darker());
    }

    // Message methods
    private void showSuccessMessage(String message) {
        showMessage(message, SUCCESS_COLOR);
    }

    private void showErrorMessage(String message) {
        showMessage(message, ERROR_COLOR);
    }

    private void showInfoMessage(String message) {
        showMessage(message, ACCENT_COLOR);
    }

    private void showMessage(String message, Color color) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Expense Manager");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String createActionButtonStyle(String bgColor) {
        return "-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;" +
                "-fx-font-size: 12px;" +
                "-fx-min-width: 30;" +
                "-fx-min-height: 30;";
    }

    private List<OutcomeRecord> applyOutcomeAmountFilter(List<OutcomeRecord> records) {
        try {
            String minText = minAmountField != null ? minAmountField.getText().trim() : "";
            String maxText = maxAmountField != null ? maxAmountField.getText().trim() : "";
            
            if (minText.isEmpty() && maxText.isEmpty()) {
                return records;
            }
            
            double minAmount = minText.isEmpty() ? 0 : Double.parseDouble(minText);
            double maxAmount = maxText.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxText);
            
            return records.stream()
                .filter(record -> record.getAmount() >= minAmount && record.getAmount() <= maxAmount)
                .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return records; // Return unfiltered if invalid numbers
        }
    }

    private void clearOutcomeFilters() {
        if (searchField != null) searchField.clear();
        if (dateRangeFilter != null) dateRangeFilter.setValue("All Time");
        if (sortFilter != null) sortFilter.setValue("Date (Newest)");
        if (minAmountField != null) minAmountField.clear();
        if (maxAmountField != null) maxAmountField.clear();
        refreshOutcomeListWithFilters();
    }

    // Legacy method for compatibility
    private void clearForm() {
        clearOutcomeForm();
    }

    // Edit mode methods
    private void enterEditMode() {
        editingRecord = null; // Will be set by editOutcomeRecord
        addButton.setText("Update Expense");
        addButton.setStyle(createActionButtonStyle(toHexString(WARNING_COLOR)));
    }

    private void exitEditMode() {
        exitOutcomeEditMode();
    }

    // Edit outcome record
    private void editOutcomeRecord(OutcomeRecord record) {
        editingRecord = record;
        enterEditMode();

        // Fill form with existing data
        amountField.setText(String.format("%.2f", record.getAmount()));
        titleField.setText(record.getTitle());
        categoryCombo.setValue(record.getCategory());
        paymentMethodCombo.setValue(record.getPaymentMethod());
        datePicker.setValue(record.getDate());
        descriptionArea.setText(record.getDescription() != null ? record.getDescription() : "");
        recurringCheckBox.setSelected(record.isRecurring());

        // Focus on amount field
        amountField.requestFocus();
    }

    // Delete outcome record
    private void deleteOutcomeRecord(OutcomeRecord record) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Expense Record");
        confirmDialog.setHeaderText("Are you sure you want to delete this expense record?");
        confirmDialog.setContentText(record.getTitle() + " - $" + String.format("%.2f", record.getAmount()));

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = dbManager.deleteOutcome(record.getId());
            if (success) {
                showSuccessMessage("Expense record deleted successfully!");
                refreshOutcomeListWithFilters();
                updateStatsCardsAsync();
                
                // Refresh dashboard
                ModernCashflowApp.refreshDashboardFromOtherPage();
                
                notifyDashboardRefresh();
            } else {
                showErrorMessage("Failed to delete expense record.");
            }
        }
    }

    private void setupFormValidation() {
        // Real-time validation cho amount field
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateAddButtonState();
        });
        
        // Real-time validation cho title field  
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateAddButtonState();
        });
    }

    private void updateAddButtonState() {
        boolean isFormValid = true;
        
        // Check amount
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            isFormValid = false;
        } else {
            try {
                double amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    isFormValid = false;
                }
            } catch (NumberFormatException e) {
                isFormValid = false;
            }
        }
        
        // Check title
        String title = titleField.getText().trim();
        if (title.isEmpty() || title.length() < 2) {
            isFormValid = false;
        }
        
        // Update button state
        addButton.setDisable(!isFormValid);
        if (isFormValid) {
            addButton.setStyle(createActionButtonStyle(toHexString(OUTCOME_COLOR)));
        } else {
            addButton.setStyle(createDisabledButtonStyle());
        }
    }

    private String createDisabledButtonStyle() {
        return "-fx-background-color: #6c757d;" +
               "-fx-text-fill: #adb5bd;" +
               "-fx-background-radius: 6px;" +
               "-fx-font-size: 12px;" +
               "-fx-opacity: 0.6;";
    }
}