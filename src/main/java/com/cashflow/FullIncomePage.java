package com.cashflow;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class FullIncomePage {

    private static final Logger logger = Logger.getLogger(FullIncomePage.class.getName());

    // Enhanced color scheme with better contrast and accessibility
    private final Color ACCENT_COLOR = Color.web("#4ECDC4");
    private final Color INCOME_COLOR = Color.web("#00ff88");
    private final Color TEXT_PRIMARY = Color.WHITE;
    private final Color TEXT_SECONDARY = Color.web("#8892b0");
    private final Color TEXT_MUTED = Color.web("#64748b");
    private final Color SUCCESS_COLOR = Color.web("#4CAF50");
    private final Color ERROR_COLOR = Color.web("#FF6B6B");
    private final Color WARNING_COLOR = Color.web("#FF9800");
    private final Color CARD_COLOR = Color.web("#1e2749");
    private final Color BACKGROUND_COLOR = Color.web("#0f0f23");
    private final Color GRADIENT_START = Color.web("#ff6b9d");
    private final Color GRADIENT_END = Color.web("#12c2e9");

    private DatabaseManager dbManager;
    private AnalyticsService analyticsService;
    private VBox incomeListContainer;
    private HBox statsContainer;
    private TextField amountField;
    private TextField sourceField;
    private ComboBox<String> categoryCombo;
    private DatePicker datePicker;
    private TextArea descriptionArea;
    private Button addButton;
    private IncomeRecord editingRecord;
    private ProgressIndicator loadingIndicator;
    private Label statusLabel;

    // Enhanced form validation
    private boolean isFormValid = false;
    private VBox validationContainer;

    // Filtering and search
    private ComboBox<String> filterCombo;
    private TextField searchField;
    private ComboBox<String> sortCombo;
    private ComboBox<String> categoryFilterCombo;
    private TextField minAmountField;
    private TextField maxAmountField;
    private Label incomeListSummaryLabel;

    // Animation helper
    private Timeline floatingTimeline;

    // Static callback for dashboard refresh
    private static Runnable dashboardRefreshCallback;

    public FullIncomePage(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.analyticsService = new AnalyticsService(dbManager);
        setupValidation();
    }

    public static void setDashboardRefreshCallback(Runnable callback) {
        dashboardRefreshCallback = callback;
    }

    private void notifyDashboardRefresh() {
        if (dashboardRefreshCallback != null) {
            Platform.runLater(() -> {
                System.out.println("Notifying dashboard refresh from Enhanced Income page");
            });
        }
    }

    private void setupValidation() {
        validationContainer = new VBox(5);
        validationContainer.setVisible(false);
    }

    public VBox createIncomePageContent() {
        VBox mainContent = new VBox(30);
        mainContent.setPadding(new Insets(35));
        mainContent.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");

        try {
            // Enhanced header with breadcrumb and actions
            HBox header = createEnhancedHeader();

            // Enhanced stats overview with analytics
            VBox statsOverview = createEnhancedStatsOverview();

            // Main content area with improved layout
            HBox mainArea = new HBox(30);
            mainArea.setAlignment(Pos.TOP_LEFT);

            // Left side - Enhanced add income form
            VBox addIncomeSection = createEnhancedAddIncomeSection();

            // Right side - Enhanced income list with filtering
            VBox incomeListSection = createEnhancedIncomeListSection();

            mainArea.getChildren().addAll(addIncomeSection, incomeListSection);

            // Quick insights section
            VBox insightsSection = createIncomeInsightsSection();
            mainContent.getChildren().addAll(header, statsOverview, mainArea, insightsSection);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to create income page content", e);
            mainContent.getChildren().add(createErrorContent("Failed to load income page"));
        }

        return mainContent;
    }

    private HBox createEnhancedHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        // Breadcrumb navigation
        VBox breadcrumbSection = new VBox(8);

        HBox breadcrumb = new HBox(12);
        breadcrumb.setAlignment(Pos.CENTER_LEFT);

        Button dashboardLink = new Button("Dashboard");
        dashboardLink.setStyle(createLinkButtonStyle());
        dashboardLink.setOnAction(e -> navigateBack());

        Label separator = new Label(">");
        separator.setTextFill(TEXT_MUTED);
        separator.setFont(Font.font("System", FontWeight.NORMAL, 14));

        Label currentPage = new Label("Income Management");
        currentPage.setTextFill(TEXT_PRIMARY);
        currentPage.setFont(Font.font("System", FontWeight.BOLD, 32));

        breadcrumb.getChildren().addAll(dashboardLink, separator);

        Label subtitle = new Label("Track and manage your income sources");
        subtitle.setTextFill(TEXT_SECONDARY);
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 16));

        breadcrumbSection.getChildren().addAll(breadcrumb, currentPage, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Enhanced action buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);

        Button exportButton = new Button("📊 Export Data");
        exportButton.setStyle(createSecondaryButtonStyle());
        exportButton.setOnAction(e -> exportIncomeData());

        Button importButton = new Button("📥 Import Data");
        importButton.setStyle(createSecondaryButtonStyle());
        importButton.setOnAction(e -> importIncomeData());

        Button analyticsButton = new Button("📈 View Analytics");
        analyticsButton.setStyle(createPrimaryButtonStyle());
        analyticsButton.setOnAction(e -> showIncomeAnalytics());

        actionButtons.getChildren().addAll(exportButton, importButton, analyticsButton);

        header.getChildren().addAll(breadcrumbSection, spacer, actionButtons);
        return header;
    }

    private VBox createEnhancedStatsOverview() {
        VBox statsSection = new VBox(20);

        // Section title
        Label statsTitle = new Label("Income Overview");
        statsTitle.setTextFill(TEXT_PRIMARY);
        statsTitle.setFont(Font.font("System", FontWeight.BOLD, 20));

        // Stats container
        statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER_LEFT);

        // Add loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-accent: " + toHexString(ACCENT_COLOR) + ";");
        loadingIndicator.setPrefSize(40, 40);
        loadingIndicator.setVisible(false);

        updateStatsCardsAsync();

        VBox statsWrapper = new VBox(15);
        statsWrapper.getChildren().addAll(statsTitle, statsContainer, loadingIndicator);

        return statsWrapper;
    }

    private void updateStatsCardsAsync() {
        showStatsLoading(true);

        Task<Void> updateTask = new Task<Void>() {
            private double currentMonthIncome;
            private double yearlyIncome;
            private int recordsCount;
            private double avgIncome;
            private double recentWeekIncome;

            @Override
            protected Void call() throws Exception {
                Thread.sleep(300);

                LocalDate now = LocalDate.now();
                
                // Lấy dữ liệu tháng hiện tại (sẽ là 0 nếu không có)
                currentMonthIncome = dbManager.getMonthlyIncome(now);
                
                // Nếu tháng hiện tại không có dữ liệu, lấy tháng gần nhất
                if (currentMonthIncome == 0) {
                    currentMonthIncome = dbManager.getLatestMonthIncome();
                }
                
                yearlyIncome = dbManager.getYearlyIncome(now);
                if (yearlyIncome == 0) {
                    yearlyIncome = dbManager.getTotalIncome(); // Fallback to total
                }
                
                recordsCount = dbManager.getIncomeRecordsCount();
                avgIncome = dbManager.getAverageMonthlyIncome();
                
                recentWeekIncome = dbManager.getWeeklyIncome(now);
                if (recentWeekIncome == 0) {
                    recentWeekIncome = dbManager.getLatestWeekIncome();
                }

                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    statsContainer.getChildren().clear();

                    // Thay đổi label để phản ánh đúng dữ liệu
                    String monthTitle = (dbManager.getMonthlyIncome(LocalDate.now()) > 0) ? 
                        "This Month" : "Latest Month";
                    
                    String weekTitle = (dbManager.getWeeklyIncome(LocalDate.now()) > 0) ? 
                        "This Week" : "Recent Period";

                    VBox monthlyCard = createEnhancedStatCard("💰", monthTitle,
                            "$" + String.format("%.0f", currentMonthIncome),
                            "+12.5%", true, INCOME_COLOR, true);

                    VBox weeklyCard = createEnhancedStatCard("📅", weekTitle,
                            "$" + String.format("%.0f", recentWeekIncome),
                            "+8.7%", true, SUCCESS_COLOR, false);

                    VBox yearlyCard = createEnhancedStatCard("📈", "Total Income",
                            "$" + String.format("%.0f", yearlyIncome),
                            "+15.2%", true, ACCENT_COLOR, false);

                    VBox recordsCard = createEnhancedStatCard("📊", "Total Records",
                            String.valueOf(recordsCount), "", false, TEXT_SECONDARY, false);

                    VBox avgCard = createEnhancedStatCard("💵", "Monthly Average",
                            "$" + String.format("%.0f", avgIncome), "", false, WARNING_COLOR, false);

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

    private void animateStatsCards() {
        for (int i = 0; i < statsContainer.getChildren().size(); i++) {
            VBox card = (VBox) statsContainer.getChildren().get(i);
            card.setOpacity(0);
            card.setScaleX(0.8);
            card.setScaleY(0.8);

            FadeTransition fade = new FadeTransition(Duration.millis(400), card);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setDelay(Duration.millis(i * 100));

            ScaleTransition scale = new ScaleTransition(Duration.millis(400), card);
            scale.setFromX(0.8);
            scale.setFromY(0.8);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.setDelay(Duration.millis(i * 100));

            ParallelTransition animation = new ParallelTransition(fade, scale);
            animation.play();
        }
    }

    private void showStatsLoading(boolean show) {
        loadingIndicator.setVisible(show);
        if (show) {
            statsContainer.setEffect(new GaussianBlur(3));
        } else {
            statsContainer.setEffect(null);
        }
    }

    private VBox createEnhancedStatCard(String icon, String title, String value,
                                        String change, boolean showChange, Color accentColor, boolean isMainCard) {
        VBox card = new VBox(20);
        card.setPadding(new Insets(isMainCard ? 30 : 20));
        card.setPrefWidth(isMainCard ? 300 : 240);
        card.setPrefHeight(isMainCard ? 160 : 130);

        // Enhanced card styling with glassmorphism effect
        if (isMainCard) {
            card.setStyle(
                    "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, " +
                            toRgbaString(accentColor, 0.15) + " 0%, " +
                            toRgbaString(GRADIENT_END, 0.1) + " 100%);" +
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

        // Enhanced header with icon
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
            Color changeColor = change.startsWith("+") ? SUCCESS_COLOR : ERROR_COLOR;
            changeLabel.setTextFill(changeColor);
            changeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            changeLabel.setStyle(
                    "-fx-background-color: " + toRgbaString(changeColor, 0.15) + ";" +
                            "-fx-background-radius: 10px;" +
                            "-fx-padding: 4 8 4 8;"
            );

            Label trendIcon = new Label(change.startsWith("+") ? "📈" : "📉");
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

    private VBox createEnhancedAddIncomeSection() {
        VBox section = new VBox(25);
        section.setPrefWidth(420);

        // Enhanced section title
        HBox titleRow = new HBox(15);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Add New Income");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        Circle statusIndicator = new Circle(6);
        statusIndicator.setFill(SUCCESS_COLOR);
        statusIndicator.setVisible(false);

        titleRow.getChildren().addAll(title, statusIndicator);

        // Enhanced form card
        VBox formCard = new VBox(20);
        formCard.setPadding(new Insets(30));
        formCard.setStyle(
                "-fx-background-color: " + toRgbaString(CARD_COLOR, 0.8) + ";" +
                        "-fx-background-radius: 20px;" +
                        "-fx-border-color: " + toRgbaString(TEXT_PRIMARY, 0.1) + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 20px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 5);"
        );

        // Enhanced form fields with real-time validation
        VBox amountFieldContainer = createEnhancedAmountField();
        VBox sourceFieldContainer = createEnhancedSourceField();
        VBox categoryFieldContainer = createEnhancedCategoryDropdown();
        VBox dateFieldContainer = createEnhancedDateField();
        VBox descriptionFieldContainer = createEnhancedDescriptionField();

        // Validation container
        setupValidationContainer();

        // Enhanced action buttons
        HBox buttonRow = new HBox(20);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        addButton = createEnhancedAddButton();
        addButton.setPrefWidth(160);
        Button clearButton = createClearButton();
        clearButton.setPrefWidth(160);

        buttonRow.getChildren().addAll(addButton, clearButton);

        // Status label for feedback
        statusLabel = new Label();
        statusLabel.setVisible(false);
        statusLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));

        formCard.getChildren().addAll(
                amountFieldContainer,
                sourceFieldContainer,
                categoryFieldContainer,
                dateFieldContainer,
                descriptionFieldContainer,
                validationContainer,
                buttonRow,
                statusLabel
        );

        section.getChildren().addAll(titleRow, formCard);
        
        setupIncomeFormValidation();
        
        return section;
    }

    private VBox createEnhancedAmountField() {
        VBox field = new VBox(10);

        Label label = new Label("Amount *");
        label.setTextFill(TEXT_SECONDARY);
        label.setFont(Font.font("System", FontWeight.MEDIUM, 14));

        HBox inputContainer = new HBox(10);
        inputContainer.setAlignment(Pos.CENTER_LEFT);

        Label currencyLabel = new Label("$");
        currencyLabel.setTextFill(ACCENT_COLOR);
        currencyLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        currencyLabel.setPadding(new Insets(0, 5, 0, 0));

        amountField = new TextField();
        amountField.setPromptText("0.00");
        amountField.setPrefHeight(45);
        amountField.setStyle(createEnhancedTextFieldStyle());

        // Real-time validation
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateAmountField(newVal);
            updateFormValidation();
        });

        // Format input as currency
        amountField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                formatAmountField();
            }
            updateFieldFocus(amountField, isNowFocused);
        });

        inputContainer.getChildren().addAll(currencyLabel, amountField);
        field.getChildren().addAll(label, inputContainer);

        return field;
    }

    private VBox createEnhancedSourceField() {
        VBox field = new VBox(10);

        Label label = new Label("Income Source *");
        label.setTextFill(TEXT_SECONDARY);
        label.setFont(Font.font("System", FontWeight.MEDIUM, 14));

        sourceField = new TextField();
        sourceField.setPromptText("e.g., Tech Company Ltd, Freelance Project");
        sourceField.setPrefHeight(45);
        sourceField.setStyle(createEnhancedTextFieldStyle());

        // Real-time validation
        sourceField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateSourceField(newVal);
            updateFormValidation();
        });

        sourceField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            updateFieldFocus(sourceField, isNowFocused);
        });

        field.getChildren().addAll(label, sourceField);
        return field;
    }

    private VBox createEnhancedCategoryDropdown() {
        VBox field = new VBox(10);

        Label label = new Label("Category");
        label.setTextFill(TEXT_SECONDARY);
        label.setFont(Font.font("System", FontWeight.MEDIUM, 14));

        categoryCombo = new ComboBox<>();

        // Load categories from database
        List<String> categories = dbManager.getIncomeCategories();
        if (categories.isEmpty()) {
            categories = List.of("Salary", "Freelance", "Investment", "Business", "Bonus", "Gift", "Other");
        }
        categoryCombo.getItems().addAll(categories);
        categoryCombo.setValue("Salary");
        categoryCombo.setPrefWidth(Double.MAX_VALUE);
        categoryCombo.setPrefHeight(45);
        categoryCombo.setEditable(true);
        categoryCombo.setStyle(createEnhancedComboBoxStyle());

        field.getChildren().addAll(label, categoryCombo);
        return field;
    }

    private VBox createEnhancedDateField() {
        VBox field = new VBox(10);

        Label label = new Label("Date");
        label.setTextFill(TEXT_SECONDARY);
        label.setFont(Font.font("System", FontWeight.MEDIUM, 14));

        datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(Double.MAX_VALUE);
        datePicker.setPrefHeight(45);
        datePicker.setEditable(false);
        datePicker.setStyle(createEnhancedComboBoxStyle());

        field.getChildren().addAll(label, datePicker);
        return field;
    }

    private VBox createEnhancedDescriptionField() {
        VBox field = new VBox(10);

        Label label = new Label("Description (Optional)");
        label.setTextFill(TEXT_SECONDARY);
        label.setFont(Font.font("System", FontWeight.MEDIUM, 14));

        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Add any notes or details about this income...");
        descriptionArea.setPrefHeight(80);
        descriptionArea.setWrapText(true);
        descriptionArea.setStyle(createEnhancedTextAreaStyle());

        field.getChildren().addAll(label, descriptionArea);
        return field;
    }

    private void setupValidationContainer() {
        validationContainer.setStyle(
                "-fx-background-color: " + toRgbaString(ERROR_COLOR, 0.1) + ";" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: " + toRgbaString(ERROR_COLOR, 0.3) + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-padding: 10px;"
        );
    }

    private Button createEnhancedAddButton() {
        Button button = new Button("💰 Add Income");
        button.setPrefWidth(Double.MAX_VALUE);
        button.setPrefHeight(50);
        button.setStyle(createDisabledButtonStyle());
        button.setDisable(true);

        button.setOnAction(e -> handleAddOrUpdateIncomeAsync());

        return button;
    }

    private Button createClearButton() {
        Button button = new Button("🗑️ Clear Form");
        button.setPrefHeight(50);
        button.setStyle(createSecondaryButtonStyle());

        button.setOnAction(e -> {
            clearFormWithAnimation();
        });

        return button;
    }

    private VBox createEnhancedIncomeListSection() {
        VBox section = new VBox(25);
        section.setPrefWidth(700);

        // Enhanced section header
        HBox listHeader = new HBox();
        listHeader.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Income Records");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Enhanced filtering controls
        HBox filterControls = new HBox(15);
        filterControls.setAlignment(Pos.CENTER_RIGHT);

        // Search field
        searchField = new TextField();
        searchField.setPromptText("🔍 Search records...");
        searchField.setPrefWidth(200);
        searchField.setStyle(createEnhancedTextFieldStyle());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterIncomeList());

        // Filter dropdown
        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All Time", "This Month", "Last Month", "This Year", "Last Year");
        filterCombo.setValue("All Time");
        filterCombo.setStyle(createEnhancedComboBoxStyle());
        filterCombo.setOnAction(e -> filterIncomeList());

        // Sort dropdown
        sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Date (Newest)", "Date (Oldest)", "Amount (High-Low)", "Amount (Low-High)", "Source A-Z");
        sortCombo.setValue("Date (Newest)");
        sortCombo.setStyle(createEnhancedComboBoxStyle());
        sortCombo.setOnAction(e -> sortIncomeList());

        filterControls.getChildren().addAll(searchField, filterCombo, sortCombo);
        listHeader.getChildren().addAll(title, spacer, filterControls);

        // Enhanced income list container
        ScrollPane scrollPane = new ScrollPane();
        incomeListContainer = new VBox(12);
        scrollPane.setContent(incomeListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Load initial data
        refreshIncomeListAsync();

        section.getChildren().addAll(listHeader, scrollPane);
        return section;
    }

    private VBox createIncomeInsightsSection() {
        VBox section = new VBox(20);

        Label title = new Label("Income Insights");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        HBox insightsContainer = new HBox(20);
        insightsContainer.setAlignment(Pos.CENTER_LEFT);

        // Generate insights using analytics service
        try {
            List<AnalyticsService.FinancialInsight> insights = analyticsService.generateFinancialInsights()
                    .stream()
                    .filter(insight -> insight.getType().equals("income"))
                    .limit(3)
                    .toList();

            if (insights.isEmpty()) {
                // Create default insights
                VBox defaultInsight = createInsightCard(
                        "💡", "Getting Started",
                        "Add more income records to get personalized insights",
                        ACCENT_COLOR
                );
                insightsContainer.getChildren().add(defaultInsight);
            } else {
                for (AnalyticsService.FinancialInsight insight : insights) {
                    VBox insightCard = createInsightCard(
                            insight.getIcon(), insight.getTitle(),
                            insight.getMessage(), getInsightColor(insight.getLevel())
                    );
                    insightsContainer.getChildren().add(insightCard);
                }
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to generate income insights", e);
            VBox errorInsight = createInsightCard(
                    "⚠️", "Insights Unavailable",
                    "Unable to generate insights at this time",
                    WARNING_COLOR
            );
            insightsContainer.getChildren().add(errorInsight);
        }

        section.getChildren().addAll(title, insightsContainer);
        return section;
    }

    private VBox createInsightCard(String icon, String title, String message, Color accentColor) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setPrefWidth(320);
        card.setStyle(
                "-fx-background-color: " + toRgbaString(accentColor, 0.1) + ";" +
                        "-fx-background-radius: 16px;" +
                        "-fx-border-color: " + toRgbaString(accentColor, 0.3) + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 16px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 3);"
        );

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(18));

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(accentColor);
        titleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));

        header.getChildren().addAll(iconLabel, titleLabel);

        Label messageLabel = new Label(message);
        messageLabel.setTextFill(TEXT_SECONDARY);
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        messageLabel.setWrapText(true);

        card.getChildren().addAll(header, messageLabel);
        return card;
    }

    // Enhanced async operations
    private void handleAddOrUpdateIncomeAsync() {
        // Debug form values
        System.out.println("=== Form Validation Debug ===");
        System.out.println("Amount: '" + amountField.getText() + "'");
        System.out.println("Source: '" + sourceField.getText() + "'");
        System.out.println("Category: '" + categoryCombo.getValue() + "'");
        System.out.println("Date: " + datePicker.getValue());
        
        if (!validateForm()) {
            System.err.println("✗ Form validation failed");
            return;
        }

        showButtonLoading(true);

        Task<Boolean> addIncomeTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Thread.sleep(300); // Giảm thời gian chờ

                try {
                    String amountText = amountField.getText().replace("$", "").replace(",", "").trim();
                    double amount = Double.parseDouble(amountText);
                    String source = sourceField.getText().trim();
                    String category = categoryCombo.getValue();
                    LocalDate date = datePicker.getValue();
                    String description = descriptionArea.getText().trim();

                    System.out.println("=== Attempting to add income ===");
                    System.out.println("Amount: " + amount);
                    System.out.println("Source: " + source);
                    System.out.println("Category: " + category);
                    System.out.println("Date: " + date);

                    boolean result;
                    if (editingRecord != null) {
                        result = dbManager.updateIncome(editingRecord.getId(), amount, source, category, date, description);
                    } else {
                        // Debug database connection
                        dbManager.debugConnection();
                        result = dbManager.addIncome(amount, source, category, date, description);
                    }
                    
                    System.out.println("Database operation result: " + result);
                    return result;
                    
                } catch (NumberFormatException e) {
                    System.err.println("✗ Number format error: " + e.getMessage());
                    return false;
                } catch (Exception e) {
                    System.err.println("✗ Unexpected error: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    boolean success = getValue();
                    showButtonLoading(false);

                    if (success) {
                        String message = editingRecord != null ? 
                            LanguageManager.getInstance().translate("income") + " " + LanguageManager.getInstance().translate("success") + "!" : 
                            LanguageManager.getInstance().translate("add_income") + " " + LanguageManager.getInstance().translate("success") + "!";
                        showSuccessMessage(message);

                        if (editingRecord == null) {
                            clearFormWithAnimation();
                        } else {
                            exitEditMode();
                        }

                        // Force refresh
                        refreshIncomeListAsync();
                        updateStatsCardsAsync();
                        
                        // Refresh dashboard
                        ModernCashflowApp.refreshDashboardFromOtherPage();
                        notifyDashboardRefresh();
                    } else {
                        String message = editingRecord != null ? 
                            LanguageManager.getInstance().translate("error") + " updating income" : 
                            LanguageManager.getInstance().translate("error") + " adding income";
                        showErrorMessage(message + ". Please try again.");
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showButtonLoading(false);
                    showErrorMessage(LanguageManager.getInstance().translate("error") + " occurred. Please try again.");
                    System.err.println("✗ Task failed: " + getException().getMessage());
                    getException().printStackTrace();
                });
            }
        };

        new Thread(addIncomeTask).start();
    }

    // Thêm method mới cho success animation
    private void animateSuccessAction() {
        // Success pulse animation for add button
        ScaleTransition pulse = new ScaleTransition(Duration.millis(150), addButton);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.1);
        pulse.setToY(1.1);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.play();
        
        // Flash green background briefly
        String originalStyle = addButton.getStyle();
        addButton.setStyle(originalStyle.replace(
            toHexString(INCOME_COLOR), 
            toHexString(SUCCESS_COLOR)
        ));
        
        Timeline flashback = new Timeline(new javafx.animation.KeyFrame(
            Duration.millis(300),
            e -> addButton.setStyle(originalStyle)
        ));
        flashback.play();
    }

    private void refreshIncomeListAsync() {
        showIncomeListLoading(true);

        Task<List<IncomeRecord>> loadRecordsTask = new Task<List<IncomeRecord>>() {
            @Override
            protected List<IncomeRecord> call() throws Exception {
                Thread.sleep(200);
                
                // Lấy NHIỀU records hơn và áp dụng filter
                List<IncomeRecord> allRecords = dbManager.getIncomeRecords(100); // Tăng từ 50 lên 100
                return applyAllIncomeFilters(allRecords);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<IncomeRecord> filteredRecords = getValue();
                    displayIncomeRecordsWithAnimation(filteredRecords);
                    updateIncomeListSummary(filteredRecords);
                    showIncomeListLoading(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showIncomeListError();
                    showIncomeListLoading(false);
                });
            }
        };

        new Thread(loadRecordsTask).start();
    }

    // Method mới để áp dụng tất cả filter
    private List<IncomeRecord> applyAllIncomeFilters(List<IncomeRecord> records) {
        List<IncomeRecord> result = new ArrayList<>(records);
        
        // 1. Text search filter - CẢI THIỆN TÌM KIẾM
        String searchText = searchField != null ? searchField.getText().trim() : "";
        if (!searchText.isEmpty()) {
            result = result.stream()
                .filter(record -> matchesIncomeSearchCriteria(record, searchText))
                .collect(Collectors.toList());
        }
        
        // 2. Date range filter
        if (filterCombo != null) {
            String dateFilter = filterCombo.getValue();
            result = applyIncomeDateFilter(result, dateFilter);
        }
        
        // 3. Category filter
        if (categoryCombo != null && !categoryCombo.getValue().equals("All Categories")) {
            String category = categoryCombo.getValue();
            result = result.stream()
                .filter(record -> record.getCategory().equals(category))
                .collect(Collectors.toList());
        }
        
        // 4. Amount range filter
        result = applyIncomeAmountRangeFilter(result);
        
        // 5. Sort - MẶC ĐỊNH THEO NGÀY GẦN NHẤT
        if (sortCombo != null) {
            String sortType = sortCombo.getValue();
            result = applyIncomeSortFilter(result, sortType);
        } else {
            // Mặc định sắp xếp theo ngày gần nhất
            result = result.stream()
                .sorted((r1, r2) -> r2.getDate().compareTo(r1.getDate()))
                .collect(Collectors.toList());
        }
        
        return result;
    }

    // Thêm method này sau applyAllIncomeFilters()
    private boolean matchesIncomeSearchCriteria(IncomeRecord record, String searchText) {
        String lowerSearch = searchText.toLowerCase();
        
        // Tìm kiếm đa tiêu chí
        boolean matches = false;
        
        // Tìm trong nguồn thu nhập
        if (record.getSource() != null && 
            record.getSource().toLowerCase().contains(lowerSearch)) {
            matches = true;
        }
        
        // Tìm trong danh mục
        if (record.getCategory() != null && 
            record.getCategory().toLowerCase().contains(lowerSearch)) {
            matches = true;
        }
        
        // Tìm trong ghi chú/mô tả
        if (record.getDescription() != null && 
            record.getDescription().toLowerCase().contains(lowerSearch)) {
            matches = true;
        }
        
        // Tìm theo số tiền
        try {
            double searchAmount = Double.parseDouble(searchText);
            if (Math.abs(record.getAmount() - searchAmount) < 0.01) {
                matches = true;
            }
        } catch (NumberFormatException e) {
            // Không phải số, tiếp tục tìm text
        }
        
        // Tìm theo ngày
        String dateStr = record.getDate().toString();
        if (dateStr.contains(lowerSearch)) {
            matches = true;
        }
        
        // Tìm kiếm mờ (fuzzy search) cho lỗi chính tả
        if (!matches) {
            matches = fuzzyMatchIncome(record, lowerSearch);
        }
        
        return matches;
    }

    private boolean fuzzyMatchIncome(IncomeRecord record, String search) {
        String[] fields = {
            record.getSource(),
            record.getCategory(),
            record.getDescription()
        };
        
        for (String field : fields) {
            if (field != null && isSimilarText(field.toLowerCase(), search)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSimilarText(String text, String search) {
        // Kiểm tra từng từ
        String[] searchWords = search.split("\\s+");
        String[] textWords = text.split("\\s+");
        
        for (String searchWord : searchWords) {
            for (String textWord : textWords) {
                if (textWord.contains(searchWord) || searchWord.contains(textWord)) {
                    return true;
                }
                // Kiểm tra độ tương tự đơn giản
                if (Math.abs(textWord.length() - searchWord.length()) <= 2 && 
                    textWord.startsWith(searchWord.substring(0, Math.min(2, searchWord.length())))) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<IncomeRecord> applyIncomeSortFilter(List<IncomeRecord> records, String sortType) {
        return records.stream().sorted((r1, r2) -> {
            switch (sortType) {
                case "Date (Oldest)":
                    return r1.getDate().compareTo(r2.getDate());
                case "Amount (High-Low)":
                    return Double.compare(r2.getAmount(), r1.getAmount());
                case "Amount (Low-High)":
                    return Double.compare(r1.getAmount(), r2.getAmount());
                case "Source A-Z":
                    return r1.getSource().compareToIgnoreCase(r2.getSource());
                case "Source Z-A":
                    return r2.getSource().compareToIgnoreCase(r1.getSource());
                default: // "Date (Newest)"
                    return r2.getDate().compareTo(r1.getDate());
            }
        }).collect(Collectors.toList());
    }

    private List<IncomeRecord> applyIncomeAmountRangeFilter(List<IncomeRecord> records) {
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
            return records;
        }
    }

    private List<IncomeRecord> applyIncomeDateFilter(List<IncomeRecord> records, String dateFilter) {
        LocalDate now = LocalDate.now();
        
        return records.stream().filter(record -> {
            switch (dateFilter) {
                case "Today":
                    return record.getDate().equals(now);
                case "This Week":
                    LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);
                    LocalDate weekEnd = weekStart.plusDays(6);
                    return !record.getDate().isBefore(weekStart) && !record.getDate().isAfter(weekEnd);
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
                case "Last 30 Days":
                    return record.getDate().isAfter(now.minusDays(30));
                case "Last 90 Days":
                    return record.getDate().isAfter(now.minusDays(90));
                default: // "All Time"
                    return true;
            }
        }).collect(Collectors.toList());
    }

    // Enhanced display với animation mượt hơn
    private void displayIncomeRecordsWithAnimation(List<IncomeRecord> records) {
        incomeListContainer.getChildren().clear();

        if (records.isEmpty()) {
            VBox emptyState = createEnhancedEmptyState();
            incomeListContainer.getChildren().add(emptyState);
            return;
        }

        // Batch animation cho performance tốt hơn
        int batchSize = 5;
        for (int i = 0; i < records.size(); i += batchSize) {
            final int batchIndex = i;
            int endIndex = Math.min(i + batchSize, records.size());
            List<IncomeRecord> batch = records.subList(i, endIndex);
            
            // Delay cho mỗi batch
            Timeline timeline = new Timeline(new javafx.animation.KeyFrame(
                Duration.millis(i * 50), // Stagger batches
                e -> animateBatchOfRecords(batch, batchIndex)
            ));
            timeline.play();
        }
    }

    private void animateBatchOfRecords(List<IncomeRecord> batch, int startIndex) {
        for (int i = 0; i < batch.size(); i++) {
            IncomeRecord record = batch.get(i);
            HBox incomeItem = createEnhancedIncomeItem(record);
            incomeListContainer.getChildren().add(incomeItem);

            // Smooth staggered animation
            incomeItem.setOpacity(0);
            incomeItem.setTranslateY(30);
            incomeItem.setScaleX(0.9);
            incomeItem.setScaleY(0.9);

            Duration delay = Duration.millis(i * 80);
            
            // Multi-property animation
            FadeTransition fade = new FadeTransition(Duration.millis(400), incomeItem);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setDelay(delay);

            TranslateTransition slide = new TranslateTransition(Duration.millis(400), incomeItem);
            slide.setFromY(30);
            slide.setToY(0);
            slide.setDelay(delay);
            slide.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

            ScaleTransition scaleX = new ScaleTransition(Duration.millis(400), incomeItem);
            scaleX.setFromX(0.9);
            scaleX.setToX(1.0);
            scaleX.setDelay(delay);
            scaleX.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

            ScaleTransition scaleY = new ScaleTransition(Duration.millis(400), incomeItem);
            scaleY.setFromY(0.9);
            scaleY.setToY(1.0);
            scaleY.setDelay(delay);
            scaleY.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

            ParallelTransition parallel = new ParallelTransition(fade, slide, scaleX, scaleY);
            parallel.play();
        }
    }

    // Enhanced loading state
    private void showIncomeListLoading(boolean show) {
        if (show) {
            incomeListContainer.getChildren().clear();
            
            VBox loadingContainer = new VBox(20);
            loadingContainer.setAlignment(Pos.CENTER);
            loadingContainer.setPadding(new Insets(50));

            // Animated loading dots
            HBox dotsContainer = new HBox(8);
            dotsContainer.setAlignment(Pos.CENTER);
            
            for (int i = 0; i < 3; i++) {
                Circle dot = new Circle(6);
                dot.setFill(ACCENT_COLOR);
                dotsContainer.getChildren().add(dot);
                
                // Bouncing animation
                TranslateTransition bounce = new TranslateTransition(Duration.millis(600), dot);
                bounce.setFromY(0);
                bounce.setToY(-15);
                bounce.setAutoReverse(true);
                bounce.setCycleCount(Timeline.INDEFINITE);
                bounce.setDelay(Duration.millis(i * 200));
                bounce.play();
            }

            Label loadingLabel = new Label("Loading income records...");
            loadingLabel.setTextFill(TEXT_SECONDARY);
            loadingLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

            loadingContainer.getChildren().addAll(dotsContainer, loadingLabel);
            incomeListContainer.getChildren().add(loadingContainer);
        }
    }

    // Enhanced empty state
    private VBox createEnhancedEmptyState() {
        VBox emptyState = new VBox(25);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(80));

        // Animated empty icon
        StackPane iconContainer = new StackPane();
        Circle iconBg = new Circle(40);
        iconBg.setFill(toRgbaColor(ACCENT_COLOR, 0.1));
        iconBg.setStroke(toRgbaColor(ACCENT_COLOR, 0.3));
        iconBg.setStrokeWidth(2);
        
        Label emptyIcon = new Label("🔍");
        emptyIcon.setFont(Font.font(32));
        
        iconContainer.getChildren().addAll(iconBg, emptyIcon);
        
        // Floating animation cho icon
        addFloatingAnimation(iconContainer, Duration.millis(3000), 10);

        Label emptyTitle = new Label("No income records found");
        emptyTitle.setTextFill(TEXT_PRIMARY);
        emptyTitle.setFont(Font.font("System", FontWeight.BOLD, 20));

        Label emptyMessage = new Label("Try adjusting your search filters or add a new income record.");
        emptyMessage.setTextFill(TEXT_SECONDARY);
        emptyMessage.setFont(Font.font("System", FontWeight.NORMAL, 14));
        emptyMessage.setWrapText(true);
        emptyMessage.setAlignment(Pos.CENTER);

        // Action buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);
        
        Button clearFiltersBtn = new Button("🗑️ Clear Filters");
        clearFiltersBtn.setStyle(createSecondaryButtonStyle());
        clearFiltersBtn.setOnAction(e -> clearAllIncomeFilters());
        
        Button addIncomeBtn = new Button("💰 Add Income");
        addIncomeBtn.setStyle(createPrimaryButtonStyle());
        addIncomeBtn.setOnAction(e -> amountField.requestFocus());
        
        actionButtons.getChildren().addAll(clearFiltersBtn, addIncomeBtn);

        emptyState.getChildren().addAll(iconContainer, emptyTitle, emptyMessage, actionButtons);
        return emptyState;
    }

    // Clear all filters
    private void clearAllIncomeFilters() {
        if (searchField != null) searchField.clear();
        if (filterCombo != null) filterCombo.setValue("All Time");
        if (sortCombo != null) sortCombo.setValue("Date (Newest)");
        if (categoryCombo != null) categoryCombo.setValue("All Categories");
        if (minAmountField != null) minAmountField.clear();
        if (maxAmountField != null) maxAmountField.clear();
        
        // Animate clear action
        showSuccessMessage("All filters cleared!");
        refreshIncomeListAsync();
    }

    // Summary update
    private void updateIncomeListSummary(List<IncomeRecord> records) {
        if (incomeListSummaryLabel != null) {
            double totalAmount = records.stream().mapToDouble(IncomeRecord::getAmount).sum();
            String summaryText = String.format("Showing %d records • Total: $%.2f", 
                records.size(), totalAmount);
            incomeListSummaryLabel.setText(summaryText);
            
            // Animate summary update
            FadeTransition fade = new FadeTransition(Duration.millis(300), incomeListSummaryLabel);
            fade.setFromValue(0.5);
            fade.setToValue(1.0);
            fade.play();
        }
    }

    private void showIncomeListError() {
        incomeListContainer.getChildren().clear();
        
        VBox errorContainer = new VBox(20);
        errorContainer.setAlignment(Pos.CENTER);
        errorContainer.setPadding(new Insets(50));
        
        Label errorIcon = new Label("⚠️");
        errorIcon.setFont(Font.font(32));
        
        Label errorMessage = new Label("Failed to load income records");
        errorMessage.setTextFill(ERROR_COLOR);
        errorMessage.setFont(Font.font("System", FontWeight.MEDIUM, 16));
        
        Button retryButton = new Button("🔄 Retry");
        retryButton.setStyle(createPrimaryButtonStyle());
        retryButton.setOnAction(e -> refreshIncomeListAsync());
        
        errorContainer.getChildren().addAll(errorIcon, errorMessage, retryButton);
        incomeListContainer.getChildren().add(errorContainer);
    }

    // Thêm method floating animation
    private void addFloatingAnimation(javafx.scene.Node node, Duration duration, double range) {
        TranslateTransition float1 = new TranslateTransition(duration, node);
        float1.setFromY(0);
        float1.setToY(-range);
        float1.setAutoReverse(true);
        float1.setCycleCount(Timeline.INDEFINITE);
        float1.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        
        float1.play();
    }

    private HBox createEnhancedIncomeItem(IncomeRecord record) {
        HBox item = new HBox(18);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(18, 22, 18, 22));
        item.setStyle(
                "-fx-background-color: " + toRgbaString(CARD_COLOR, 0.7) + ";" +
                        "-fx-background-radius: 16px;" +
                        "-fx-border-color: " + toRgbaString(TEXT_PRIMARY, 0.1) + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 16px;" +
                        "-fx-cursor: hand;"
        );

        // Enhanced category icon
        StackPane iconContainer = new StackPane();
        Circle iconBg = new Circle(22);
        iconBg.setFill(INCOME_COLOR);
        iconBg.setEffect(new DropShadow(6, INCOME_COLOR));

        Label icon = new Label(getCategoryIcon(record.getCategory()));
        icon.setFont(Font.font(16));
        icon.setTextFill(Color.WHITE);

        iconContainer.getChildren().addAll(iconBg, icon);

        // Enhanced details section
        VBox details = new VBox(6);

        Label source = new Label(record.getSource());
        source.setTextFill(TEXT_PRIMARY);
        source.setFont(Font.font("System", FontWeight.SEMI_BOLD, 15));

        HBox metaInfo = new HBox(12);
        metaInfo.setAlignment(Pos.CENTER_LEFT);

        Label dateAndCategory = new Label(
                record.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) +
                        " • " + record.getCategory()
        );
        dateAndCategory.setTextFill(TEXT_SECONDARY);
        dateAndCategory.setFont(Font.font("System", FontWeight.NORMAL, 12));

        // Add status badge if recent
        if (record.getDate().isAfter(LocalDate.now().minusDays(7))) {
            Label newBadge = new Label("NEW");
            newBadge.setTextFill(SUCCESS_COLOR);
            newBadge.setFont(Font.font("System", FontWeight.BOLD, 10));
            newBadge.setStyle(
                    "-fx-background-color: " + toRgbaString(SUCCESS_COLOR, 0.15) + ";" +
                            "-fx-background-radius: 8px;" +
                            "-fx-padding: 2 6 2 6;"
            );
            metaInfo.getChildren().addAll(dateAndCategory, newBadge);
        } else {
            metaInfo.getChildren().add(dateAndCategory);
        }

        // Add description if available
        if (record.getDescription() != null && !record.getDescription().trim().isEmpty()) {
            Label description = new Label(record.getDescription());
            description.setTextFill(TEXT_MUTED);
            description.setFont(Font.font("System", FontWeight.NORMAL, 11));
            description.setWrapText(true);
            details.getChildren().addAll(source, metaInfo, description);
        } else {
            details.getChildren().addAll(source, metaInfo);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Enhanced amount display
        VBox amountSection = new VBox(4);
        amountSection.setAlignment(Pos.CENTER_RIGHT);

        Label amount = new Label("+$" + String.format("%.2f", record.getAmount()));
        amount.setTextFill(INCOME_COLOR);
        amount.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Add created time
        Label createdTime = new Label("Added " + formatRelativeTime(record.getCreatedAt()));
        createdTime.setTextFill(TEXT_MUTED);
        createdTime.setFont(Font.font("System", FontWeight.NORMAL, 10));

        amountSection.getChildren().addAll(amount, createdTime);

        // Enhanced actions
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editButton = new Button("✏️ Sửa");
        editButton.setStyle(createActionButtonStyle(toHexString(ACCENT_COLOR)));
        editButton.setOnAction(e -> editIncomeRecord(record));

        Button deleteButton = new Button("🗑️ Xóa");
        deleteButton.setStyle(createActionButtonStyle(toHexString(ERROR_COLOR)));
        deleteButton.setOnAction(e -> deleteIncomeRecord(record));

        Button viewButton = new Button("👁️ Xem");
        viewButton.setStyle(createActionButtonStyle("#6c757d"));
        viewButton.setOnAction(e -> viewIncomeDetails(record));

        actions.getChildren().addAll(viewButton, editButton, deleteButton);

        item.getChildren().addAll(iconContainer, details, spacer, amountSection, actions);

        // Enhanced hover effects
        item.setOnMouseEntered(e -> {
            item.setStyle(
                    "-fx-background-color: " + toRgbaString(INCOME_COLOR, 0.08) + ";" +
                            "-fx-background-radius: 16px;" +
                            "-fx-border-color: " + toRgbaString(INCOME_COLOR, 0.3) + ";" +
                            "-fx-border-width: 1px;" +
                            "-fx-border-radius: 16px;" +
                            "-fx-cursor: hand;"
            );

            ScaleTransition scale = new ScaleTransition(Duration.millis(100), item);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
        });

        item.setOnMouseExited(e -> {
            item.setStyle(
                    "-fx-background-color: " + toRgbaString(CARD_COLOR, 0.7) + ";" +
                            "-fx-background-radius: 16px;" +
                            "-fx-border-color: " + toRgbaString(TEXT_PRIMARY, 0.1) + ";" +
                            "-fx-border-width: 1px;" +
                            "-fx-border-radius: 16px;" +
                            "-fx-cursor: hand;"
            );

            ScaleTransition scale = new ScaleTransition(Duration.millis(100), item);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        return item;
    }

    // Enhanced validation methods
    private void validateAmountField(String value) {
        validationContainer.getChildren().removeIf(node ->
                node instanceof Label && ((Label) node).getText().contains("Amount"));

        if (value.trim().isEmpty()) {
            addValidationError("💰 Amount is required");
        } else {
            try {
                double amount = Double.parseDouble(value.replace("$", "").replace(",", ""));
                if (amount <= 0) {
                    addValidationError("💰 Amount must be greater than 0");
                } else if (amount > 1000000) {
                    addValidationError("💰 Amount seems too large. Please verify.");
                }
            } catch (NumberFormatException e) {
                addValidationError("💰 Please enter a valid amount");
            }
        }
    }

    private void validateSourceField(String value) {
        validationContainer.getChildren().removeIf(node ->
                node instanceof Label && ((Label) node).getText().contains("Source"));

        if (value.trim().isEmpty()) {
            addValidationError("🏢 Income source is required");
        } else if (value.trim().length() < 2) {
            addValidationError("🏢 Source must be at least 2 characters");
        } else if (value.trim().length() > 100) {
            addValidationError("🏢 Source is too long (max 100 characters)");
        }
    }

    private void addValidationError(String message) {
        Label errorLabel = new Label("• " + message);
        errorLabel.setTextFill(ERROR_COLOR);
        errorLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        validationContainer.getChildren().add(errorLabel);
    }

    private void updateFormValidation() {
        boolean hasErrors = !validationContainer.getChildren().isEmpty();
        validationContainer.setVisible(hasErrors);

        isFormValid = !hasErrors &&
                !amountField.getText().trim().isEmpty() &&
                !sourceField.getText().trim().isEmpty();

        addButton.setDisable(!isFormValid);

        if (isFormValid) {
            addButton.setStyle(createPrimaryButtonStyle());
        } else {
            addButton.setStyle(createDisabledButtonStyle());
        }
    }

    private boolean validateForm() {
        validationContainer.getChildren().clear();
        boolean isValid = true;

        // Validate amount
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            addValidationError(LanguageManager.getInstance().translate("amount") + " is required");
            isValid = false;
        } else {
            try {
                double amount = Double.parseDouble(amountText.replace("$", "").replace(",", ""));
                if (amount <= 0) {
                    addValidationError(LanguageManager.getInstance().translate("amount") + " must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                addValidationError(LanguageManager.getInstance().translate("amount") + " must be a valid number");
                isValid = false;
            }
        }

        // Validate source
        String sourceText = sourceField.getText().trim();
        if (sourceText.isEmpty()) {
            addValidationError(LanguageManager.getInstance().translate("source") + " is required");
            isValid = false;
        } else if (sourceText.length() < 2) {
            addValidationError(LanguageManager.getInstance().translate("source") + " must be at least 2 characters");
            isValid = false;
        }

        // Validate category
        if (categoryCombo.getValue() == null || categoryCombo.getValue().trim().isEmpty()) {
            categoryCombo.setValue("Other");
        }

        // Validate date
        if (datePicker.getValue() == null) {
            datePicker.setValue(LocalDate.now());
        }

        // Update form state
        updateFormValidation();
        
        System.out.println("Form validation result: " + isValid);
        return isValid;
    }

    // Enhanced UI interaction methods
    private void formatAmountField() {
        try {
            String text = amountField.getText().replace("$", "").replace(",", "");
            if (!text.trim().isEmpty()) {
                double amount = Double.parseDouble(text);
                amountField.setText(String.format("%.2f", amount));
            }
        } catch (NumberFormatException e) {
            // Keep original text if parsing fails
        }
    }

    private void clearFormWithAnimation() {
        // Animate form clearing
        FadeTransition fade = new FadeTransition(Duration.millis(200), addButton.getParent());
        fade.setFromValue(1.0);
        fade.setToValue(0.5);
        fade.setOnFinished(e -> {
            clearForm();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), addButton.getParent());
            fadeIn.setFromValue(0.5);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fade.play();
    }

    private void clearForm() {
        amountField.clear();
        sourceField.clear();
        categoryCombo.setValue("Salary");
        datePicker.setValue(LocalDate.now());
        descriptionArea.clear();
        validationContainer.getChildren().clear();
        validationContainer.setVisible(false);
        exitEditMode();
        updateFormValidation();
    }

    private void showButtonLoading(boolean show) {
        if (show) {
            addButton.setText("💫 Processing...");
            addButton.setDisable(true);
        } else {
            addButton.setText(editingRecord != null ? "💰 Update Income" : "💰 Add Income");
            addButton.setDisable(!isFormValid);
        }
    }

    private void editIncomeRecord(IncomeRecord record) {
        editingRecord = record;
        enterEditMode();

        // Fill form with existing data
        amountField.setText(String.format("%.2f", record.getAmount()));
        sourceField.setText(record.getSource());
        categoryCombo.setValue(record.getCategory());
        datePicker.setValue(record.getDate());
        descriptionArea.setText(record.getDescription() != null ? record.getDescription() : "");

        // Scroll to form and focus
        amountField.requestFocus();

        showInfoMessage("Edit mode: Update the form and click 'Update Income' to save changes.");
    }

    private void enterEditMode() {
        addButton.setText("💰 Update Income");
        addButton.setStyle(createWarningButtonStyle());
    }

    private void exitEditMode() {
        editingRecord = null;
        addButton.setText("💰 Add Income");
        addButton.setStyle(createPrimaryButtonStyle());
    }

    private void deleteIncomeRecord(IncomeRecord record) {
        Alert confirmDialog = createStyledAlert(
                "Delete Income Record",
                "Are you sure you want to delete this income record?",
                record.getSource() + " - $" + String.format("%.2f", record.getAmount())
        );

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteIncomeAsync(record);
        }
    }

    private void deleteIncomeAsync(IncomeRecord record) {
        Task<Boolean> deleteTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return dbManager.deleteIncome(record.getId());
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    if (getValue()) {
                        showSuccessMessage("Income record deleted successfully!");
                        refreshIncomeListAsync();
                        updateStatsCardsAsync();
                        notifyDashboardRefresh();
                    } else {
                        showErrorMessage("Failed to delete income record.");
                    }
                });
            }
        };

        new Thread(deleteTask).start();
    }

    private void viewIncomeDetails(IncomeRecord record) {
        Alert detailsDialog = new Alert(Alert.AlertType.INFORMATION);
        detailsDialog.setTitle("Income Details");
        detailsDialog.setHeaderText(record.getSource());

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        content.getChildren().addAll(
                new Label("Amount: $" + String.format("%.2f", record.getAmount())),
                new Label("Category: " + record.getCategory()),
                new Label("Date: " + record.getDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))),
                new Label("Description: " + (record.getDescription() != null ? record.getDescription() : "No description")),
                new Label("Created: " + record.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")))
        );

        detailsDialog.getDialogPane().setContent(content);
        detailsDialog.showAndWait();
    }

    // Filtering and sorting methods
    private void filterIncomeList() {
        // Implementation for filtering income list based on search and filter criteria
        refreshIncomeListAsync(); // For now, just refresh
    }

    private void sortIncomeList() {
        // Implementation for sorting income list
        refreshIncomeListAsync(); // For now, just refresh
    }

    // Navigation and action methods
    private void navigateBack() {
        // Implementation to navigate back to dashboard
        System.out.println("Navigating back to dashboard");
    }

    private void exportIncomeData() {
        showInfoMessage("Export functionality coming soon!");
    }

    private void importIncomeData() {
        showInfoMessage("Import functionality coming soon!");
    }

    private void showIncomeAnalytics() {
        showInfoMessage("Detailed analytics coming soon!");
    }

    // Enhanced styling methods
    private String createEnhancedTextFieldStyle() {
        return "-fx-background-color: " + toRgbaString(CARD_COLOR, 0.6) + ";" +
                "-fx-text-fill: " + toHexString(TEXT_PRIMARY) + ";" +
                "-fx-prompt-text-fill: " + toHexString(TEXT_MUTED) + ";" +
                "-fx-border-color: " + toRgbaString(ACCENT_COLOR, 0.3) + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;" +
                "-fx-background-radius: 12px;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 12px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);";
    }

    private String createEnhancedTextAreaStyle() {
        return "-fx-background-color: " + toRgbaString(CARD_COLOR, 0.6) + ";" +
                "-fx-text-fill: " + toHexString(TEXT_PRIMARY) + ";" +
                "-fx-prompt-text-fill: " + toHexString(TEXT_MUTED) + ";" +
                "-fx-border-color: " + toRgbaString(ACCENT_COLOR, 0.3) + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;" +
                "-fx-background-radius: 12px;" +
                "-fx-font-size: 14px;" +
                "-fx-control-inner-background: " + toRgbaString(CARD_COLOR, 0.6) + ";" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);";
    }

    private String createEnhancedComboBoxStyle() {
        return "-fx-background-color: " + toRgbaString(CARD_COLOR, 0.6) + ";" +
                "-fx-text-fill: " + toHexString(TEXT_PRIMARY) + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: " + toRgbaString(ACCENT_COLOR, 0.3) + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;" +
                "-fx-prompt-text-fill: " + toHexString(TEXT_MUTED) + ";" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);";
    }

    private String createPrimaryButtonStyle() {
        return "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, " + toHexString(INCOME_COLOR) + " 0%, " + toHexString(SUCCESS_COLOR) + " 100%);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, " + toRgbaString(INCOME_COLOR, 0.4) + ", 10, 0, 0, 3);";
    }

    private String createSecondaryButtonStyle() {
        return "-fx-background-color: " + toRgbaString(TEXT_PRIMARY, 0.1) + ";" +
                "-fx-text-fill: " + toHexString(TEXT_PRIMARY) + ";" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: 500;" +
                "-fx-background-radius: 10px;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: " + toRgbaString(TEXT_PRIMARY, 0.2) + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 10px;";
    }

    private String createWarningButtonStyle() {
        return "-fx-background-color: " + toHexString(WARNING_COLOR) + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, " + toRgbaString(WARNING_COLOR, 0.4) + ", 10, 0, 0, 3);";
    }

    private String createDisabledButtonStyle() {
        return "-fx-background-color: " + toRgbaString(TEXT_MUTED, 0.3) + ";" +
                "-fx-text-fill: " + toHexString(TEXT_MUTED) + ";" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12px;";
    }

    private String createLinkButtonStyle() {
        return "-fx-background-color: transparent;" +
                "-fx-text-fill: " + toHexString(ACCENT_COLOR) + ";" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;" +
                "-fx-underline: true;";
    }

    private String createActionButtonStyle(String bgColor) {
        return "-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8px;" +
                "-fx-cursor: hand;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 6px 12px;" +
                "-fx-font-weight: bold;";
    }

    private void updateFieldFocus(TextField field, boolean focused) {
        if (focused) {
            field.setStyle(field.getStyle().replace(
                    toRgbaString(ACCENT_COLOR, 0.3), toRgbaString(ACCENT_COLOR, 0.6)));
        } else {
            field.setStyle(field.getStyle().replace(
                    toRgbaString(ACCENT_COLOR, 0.6), toRgbaString(ACCENT_COLOR, 0.3)));
        }
    }

    // Utility methods
    private String getCategoryIcon(String category) {
        return switch (category.toLowerCase()) {
            case "salary" -> "💼";
            case "freelance" -> "💻";
            case "investment" -> "📈";
            case "business" -> "🏢";
            case "bonus" -> "🎁";
            case "gift" -> "🎀";
            default -> "💰";
        };
    }

    private String formatRelativeTime(java.time.LocalDateTime dateTime) {
        java.time.Duration duration = java.time.Duration.between(dateTime, java.time.LocalDateTime.now());
        long days = duration.toDays();
        long hours = duration.toHours();

        if (days > 0) {
            return days + "d ago";
        } else if (hours > 0) {
            return hours + "h ago";
        } else {
            return "Just now";
        }
    }

    private Color getInsightColor(AnalyticsService.FinancialInsight.InsightLevel level) {
        return switch (level) {
            case SUCCESS -> SUCCESS_COLOR;
            case WARNING -> WARNING_COLOR;
            case ALERT -> ERROR_COLOR;
            default -> ACCENT_COLOR;
        };
    }

    private Alert createStyledAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Apply dark theme styling to alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + toHexString(CARD_COLOR) + ";");

        return alert;
    }

    private VBox createErrorContent(String message) {
        VBox errorContent = new VBox(20);
        errorContent.setAlignment(Pos.CENTER);
        errorContent.setPadding(new Insets(50));

        Label errorIcon = new Label("❌");
        errorIcon.setFont(Font.font(32));

        Label errorLabel = new Label(message);
        errorLabel.setTextFill(ERROR_COLOR);
        errorLabel.setFont(Font.font("System", FontWeight.MEDIUM, 16));
        errorLabel.setWrapText(true);
        errorLabel.setAlignment(Pos.CENTER);

        errorContent.getChildren().addAll(errorIcon, errorLabel);
        return errorContent;
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

    private Color toRgbaColor(Color color, double alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    // Message methods
    private void showSuccessMessage(String message) {
        showStatusMessage(message, SUCCESS_COLOR);
    }

    private void showErrorMessage(String message) {
        showStatusMessage(message, ERROR_COLOR);
    }

    private void showInfoMessage(String message) {
        showStatusMessage(message, ACCENT_COLOR);
    }

    private void showStatusMessage(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setTextFill(color);
        statusLabel.setVisible(true);

        // Auto-hide after 3 seconds
        Timeline timeline = new Timeline(new javafx.animation.KeyFrame(
                Duration.seconds(3),
                e -> statusLabel.setVisible(false)
        ));
        timeline.play();

        // Animate message appearance
        FadeTransition fade = new FadeTransition(Duration.millis(300), statusLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void setupIncomeFormValidation() {
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateIncomeButtonState();
        });
        
        sourceField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateIncomeButtonState();
        });
    }

    private void updateIncomeButtonState() {
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
        
        // Check source
        String source = sourceField.getText().trim();
        if (source.isEmpty() || source.length() < 2) {
            isFormValid = false;
        }
        
        // Update button state
        addButton.setDisable(!isFormValid);
        if (isFormValid) {
            addButton.setStyle(createPrimaryButtonStyle());
        } else {
            addButton.setStyle(createDisabledButtonStyle());
        }
    }
}