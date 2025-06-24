package com.cashflow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ModernCashflowApp extends Application implements ThemeManager.ThemeChangeListener {

    // Logger instance
    private static final Logger logger = Logger.getLogger(ModernCashflowApp.class.getName());

    // Theme manager instance
    private ThemeManager themeManager;
    private ThemeManager.Theme currentTheme;

    // Dynamic color properties (sẽ thay đổi theo theme)
    private Color BG_PRIMARY;
    private Color BG_SECONDARY;
    private Color CARD_BG;
    private Color CARD_HOVER;
    
    // Accent Colors
    private Color ACCENT_PURPLE;
    private Color ACCENT_PINK;
    private Color ACCENT_BLUE;
    private Color ACCENT_GREEN;
    private Color ACCENT_CYAN;
    
    // Text Colors
    private Color TEXT_PRIMARY;
    private Color TEXT_SECONDARY;
    private Color TEXT_MUTED;

    // Database and UI references
    private DatabaseManager dbManager;
    private BorderPane mainRoot;
    private VBox contentArea;
    private HBox mainContainer;
    private VBox dashboardContent;
    private static ModernCashflowApp instance;
    private SettingsPage settingsPage;

    // Theme toggle button reference
    private Button themeToggleButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        
        // Initialize theme manager first
        themeManager = ThemeManager.getInstance();
        themeManager.addThemeChangeListener(this);
        updateColorsFromTheme();
        
        dbManager = new DatabaseManager();
        settingsPage = new SettingsPage(dbManager);
        
        // Debug all data
        dbManager.debugAllData();
        
        mainRoot = new BorderPane();
        updateMainRootStyle();

        // Modern Sidebar with theme toggle
        VBox sidebar = createModernSidebar();
        mainRoot.setLeft(sidebar);

        // Content area with smooth transitions
        contentArea = new VBox();
        updateContentAreaStyle();
        
        // Load dashboard by default
        loadDashboard();
        mainRoot.setCenter(contentArea);

        Scene scene = new Scene(mainRoot, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/modern-styles.css").toExternalForm());
        
        // Apply theme to scene
        updateSceneTheme(scene);
        
        primaryStage.setTitle("Cashflow - Modern Financial Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Add window effects
        addWindowEffects(primaryStage);
        
        // Setup auto backup
        setupAutoBackup(primaryStage);

        // Set callbacks cho Income và Outcome pages
        FullIncomePage.setDashboardRefreshCallback(() -> refreshDashboard());
        FullOutcomePage.setDashboardRefreshCallback(() -> refreshDashboard());
    }

    @Override
    public void stop() throws Exception {
        if (dbManager != null) {
            dbManager.close(); // Đóng kết nối khi ứng dụng dừng
        }
        super.stop();
    }

    private VBox createModernSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPrefWidth(260);
        sidebar.setStyle(
            "-fx-background-color: " + ThemeManager.toHexString(BG_SECONDARY) + ";" +
            "-fx-padding: 25;" +
            "-fx-effect: dropshadow(gaussian, " + ThemeManager.toRgbaString(currentTheme.getShadowColor(), 0.3) + ", 20, 0, 5, 0);"
        );

        // Logo section with animation
        HBox logoSection = createAnimatedLogo();
        
        // Navigation items
        VBox navItems = new VBox(8);
        navItems.setPadding(new Insets(30, 0, 0, 0));

        LanguageManager langManager = LanguageManager.getInstance();
        
        String[][] menuItems = {
            {langManager.translate("dashboard"), "📊", "dashboard"},
            {langManager.translate("income"), "💰", "income"},
            {langManager.translate("outcome"), "💳", "outcome"},
            {langManager.translate("analytics"), "📈", "analytics"},
            {langManager.translate("transaction"), "🔄", "transaction"},
            {langManager.translate("card"), "💎", "card"},
            {langManager.translate("settings"), "⚙️", "settings"}
        };

        for (int i = 0; i < menuItems.length; i++) {
            HBox menuItem = createModernMenuItem(menuItems[i][0], menuItems[i][1], menuItems[i][2], i == 0);
            navItems.getChildren().add(menuItem);
        }

        // Theme toggle section
        VBox themeSection = createThemeToggleSection();
        
        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // User profile section
        VBox profileSection = createProfileSection();

        sidebar.getChildren().addAll(logoSection, navItems, themeSection, spacer, profileSection);
        return sidebar;
    }

    private VBox createThemeToggleSection() {
        VBox themeSection = new VBox(15);
        themeSection.setPadding(new Insets(20, 0, 0, 0));
        
        Label themeLabel = new Label("🎨 Theme");
        themeLabel.setTextFill(TEXT_SECONDARY);
        themeLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        // Theme toggle button
        themeToggleButton = new Button();
        updateThemeToggleButton();
        themeToggleButton.setPrefWidth(200);
        themeToggleButton.setPrefHeight(35);
        themeToggleButton.setOnAction(e -> toggleTheme());
        
        themeSection.getChildren().addAll(themeLabel, themeToggleButton);
        return themeSection;
    }

    private void updateThemeToggleButton() {
        if (themeToggleButton != null) {
            if (currentTheme instanceof ThemeManager.DarkTheme) {
                themeToggleButton.setText("☀️ Switch to Light");
                themeToggleButton.setStyle(
                    "-fx-background-color: " + ThemeManager.toRgbaString(TEXT_PRIMARY, 0.1) + ";" +
                    "-fx-text-fill: " + ThemeManager.toHexString(TEXT_PRIMARY) + ";" +
                    "-fx-background-radius: 10;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-size: 12;"
                );
            } else {
                themeToggleButton.setText("🌙 Switch to Dark");
                themeToggleButton.setStyle(
                    "-fx-background-color: " + ThemeManager.toRgbaString(TEXT_PRIMARY, 0.1) + ";" +
                    "-fx-text-fill: " + ThemeManager.toHexString(TEXT_PRIMARY) + ";" +
                    "-fx-background-radius: 10;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-size: 12;"
                );
            }
        }
    }

    private void toggleTheme() {
        ThemeManager.ThemeType newTheme = currentTheme instanceof ThemeManager.DarkTheme ? 
            ThemeManager.ThemeType.LIGHT : ThemeManager.ThemeType.DARK;
        
        themeManager.setThemeType(newTheme);
        
        // Success feedback
        showThemeChangeNotification(newTheme);
    }

    private void showThemeChangeNotification(ThemeManager.ThemeType themeType) {
        // Create a temporary notification
        Label notification = new Label(themeType == ThemeManager.ThemeType.DARK ? 
            "🌙 Switched to Dark Theme" : "☀️ Switched to Light Theme");
        notification.setStyle(
            "-fx-background-color: " + ThemeManager.toHexString(ACCENT_GREEN) + ";" +
            "-fx-text-fill: white;" +
            "-fx-padding: 10 15;" +
            "-fx-background-radius: 8px;" +
            "-fx-font-weight: bold;"
        );
        
        // Position notification (simplified)
        StackPane notificationContainer = new StackPane(notification);
        notificationContainer.setAlignment(Pos.TOP_RIGHT);
        notificationContainer.setPadding(new Insets(20));
        
        // Auto-hide notification
        Timeline timeline = new Timeline(new KeyFrame(
            Duration.seconds(2),
            e -> {
                FadeTransition fade = new FadeTransition(Duration.millis(300), notification);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                fade.play();
            }
        ));
        timeline.play();
    }

    private HBox createAnimatedLogo() {
        HBox logoBox = new HBox(12);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        // Animated logo circle
        Circle logoCircle = new Circle(20);
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, ACCENT_PURPLE),
            new Stop(0.5, ACCENT_PINK),
            new Stop(1, ACCENT_BLUE)
        );
        logoCircle.setFill(gradient);
        
        // Add glow effect
        DropShadow glow = new DropShadow();
        glow.setColor(ACCENT_PURPLE);
        glow.setRadius(15);
        glow.setSpread(0.3);
        logoCircle.setEffect(glow);

        // Logo text
        Label logoText = new Label("Cashflow");
        logoText.setTextFill(TEXT_PRIMARY);
        logoText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        // Add pulse animation to logo
        ScaleTransition pulse = new ScaleTransition(Duration.millis(1000), logoCircle);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.1);
        pulse.setToY(1.1);
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        logoBox.getChildren().addAll(logoCircle, logoText);
        return logoBox;
    }

    private HBox createModernMenuItem(String text, String icon, String id, boolean isActive) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(14, 20, 14, 20));
        item.setId(id);
        item.setCursor(javafx.scene.Cursor.HAND);

        // Icon
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(20));

        // Text
        Label textLabel = new Label(text);
        textLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));

        if (isActive) {
            item.setStyle(
                "-fx-background-color: linear-gradient(to right, " + ThemeManager.toHexString(ACCENT_PURPLE) + ", " + ThemeManager.toHexString(ACCENT_PINK) + ");" +
                "-fx-background-radius: 12;"
            );
            textLabel.setTextFill(TEXT_PRIMARY);
        } else {
            textLabel.setTextFill(TEXT_SECONDARY);
            
            // Hover effect
            item.setOnMouseEntered(e -> {
                item.setStyle("-fx-background-color: " + ThemeManager.toHexString(CARD_BG) + "; -fx-background-radius: 12;");
                textLabel.setTextFill(TEXT_PRIMARY);
            });
            
            item.setOnMouseExited(e -> {
                item.setStyle("-fx-background-color: transparent;");
                textLabel.setTextFill(TEXT_SECONDARY);
            });
        }

        // Click handler
        item.setOnMouseClicked(e -> {
            handleNavigation(id);
            updateActiveMenuItem(item);
        });

        item.getChildren().addAll(iconLabel, textLabel);
        return item;
    }

    private void handleNavigation(String id) {
        // Add fade transition
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), contentArea);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        fadeOut.setOnFinished(e -> {
            switch (id) {
                case "dashboard":
                    loadDashboard();
                    break;
                case "income":
                    loadIncomePage();
                    break;
                case "outcome":
                    loadOutcomePage();
                    break;
                case "analytics":
                    loadAnalyticsPage();
                    break;
                case "transaction":
                    loadTransactionPage();
                    break;
                case "card":
                    loadCardPage();
                    break;
                case "settings":
                    loadSettingsPage();
                    break;
            }
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), contentArea);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        
        fadeOut.play();
    }

    private void loadDashboard() {
        contentArea.getChildren().clear();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        VBox dashboard = new VBox(25);
        dashboard.setPadding(new Insets(30));
        
        // Header
        HBox header = createModernHeader();
        
        // Stats cards with animation
        HBox statsCards = createAnimatedStatsCards();
        
        // Charts section
        HBox chartsSection = new HBox(20);
        chartsSection.setPrefHeight(400);
        
        // Income/Outcome Chart
        VBox expensesChart = createModernExpensesChart();
        HBox.setHgrow(expensesChart, Priority.ALWAYS);
        
        // Spending breakdown
        VBox spendingBreakdown = createModernSpendingBreakdown();
        
        chartsSection.getChildren().addAll(expensesChart, spendingBreakdown);
        
        // Recent transactions
        VBox transactions = createModernTransactions();
        
        dashboard.getChildren().addAll(header, statsCards, chartsSection, transactions);
        scrollPane.setContent(dashboard);
        contentArea.getChildren().add(scrollPane);
    }

    private HBox createModernHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleSection = new VBox(5);
        
        // Thêm thời gian và lời chào
        HBox greetingRow = new HBox(15);
        greetingRow.setAlignment(Pos.CENTER_LEFT);
        
        Label greeting = createGreetingLabel();
        Label currentTime = createTimeLabel();
        
        greetingRow.getChildren().addAll(greeting, currentTime);
        
        Label title = new Label(LanguageManager.getInstance().translate("dashboard"));
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        
        Label subtitle = new Label(LanguageManager.getInstance().translate("welcome_back"));
        subtitle.setTextFill(TEXT_SECONDARY);
        subtitle.setFont(Font.font("Segoe UI", 14));
        
        titleSection.getChildren().addAll(greetingRow, title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // User section
        HBox userSection = createUserSection();
        
        header.getChildren().addAll(titleSection, spacer, userSection);
        
        // Start time updater
        startTimeUpdater(currentTime);
        
        return header;
    }

    // Cải thiện method createGreetingLabel() để hỗ trợ đa ngôn ngữ
    private Label createGreetingLabel() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        
        LanguageManager langManager = LanguageManager.getInstance();
        String greetingKey;
        String icon;
        
        if (hour >= 5 && hour < 12) {
            greetingKey = "good_morning";
            icon = "🌅";
        } else if (hour >= 12 && hour < 18) {
            greetingKey = "good_afternoon"; 
            icon = "☀️";
        } else {
            greetingKey = "good_evening";
            icon = "🌙";
        }
        
        Label greeting = new Label(icon + " " + langManager.translate(greetingKey));
        greeting.setTextFill(ACCENT_GREEN);
        greeting.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        greeting.setStyle(
            "-fx-background-color: " + toRgbaString(ACCENT_GREEN, 0.15) + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-padding: 6 12 6 12;"
        );
        
        return greeting;
    }

    // Tạo label cho thời gian
    private Label createTimeLabel() {
        Label timeLabel = new Label();
        timeLabel.setTextFill(TEXT_SECONDARY);
        timeLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        updateTimeLabel(timeLabel);
        
        return timeLabel;
    }

    // Cập nhật thời gian
    private void updateTimeLabel(Label timeLabel) {
        LocalDateTime now = LocalDateTime.now();
        String timeText = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy - HH:mm:ss"));
        timeLabel.setText("📅 " + timeText);
    }

    // Bắt đầu timer cập nhật thời gian
    private void startTimeUpdater(Label timeLabel) {
        Timeline timeline = new Timeline(new KeyFrame(
            Duration.seconds(1),
            e -> updateTimeLabel(timeLabel)
        ));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // Tạo user section
    private HBox createUserSection() {
        HBox userSection = new HBox(20);
        userSection.setAlignment(Pos.CENTER_RIGHT);
        
        // Notification bell
        Button notificationBtn = new Button("🔔");
        notificationBtn.setStyle(
            "-fx-background-color: " + ThemeManager.toHexString(CARD_BG) + ";" +
            "-fx-background-radius: 50%;" +
            "-fx-min-width: 40;" +
            "-fx-min-height: 40;" +
            "-fx-font-size: 16;" +
            "-fx-cursor: hand;"
        );
        
        // User info
        HBox userInfo = new HBox(12);
        userInfo.setAlignment(Pos.CENTER);
        
        Label greeting = new Label("Hi, Ferra");
        greeting.setTextFill(TEXT_PRIMARY);
        greeting.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        Circle avatar = new Circle(20);
        avatar.setFill(ACCENT_PINK);
        
        userInfo.getChildren().addAll(greeting, avatar);
        userSection.getChildren().addAll(notificationBtn, userInfo);
        
        return userSection;
    }

    private HBox createAnimatedStatsCards() {
        HBox cards = new HBox(20);
        cards.setAlignment(Pos.CENTER_LEFT);
        
        // Lấy dữ liệu thực từ database
        DashboardStats stats = dbManager.getDashboardStats();
        
        // Debug log để kiểm tra dữ liệu
        System.out.println("=== Dashboard Stats Debug (Enhanced) ===");
        System.out.println("Monthly Income: " + stats.getMonthlyIncome());
        System.out.println("Monthly Outcome: " + stats.getMonthlyOutcome());
        System.out.println("Total Income: " + stats.getTotalIncome());
        System.out.println("Total Outcome: " + stats.getTotalOutcome());
        System.out.println("Balance: " + stats.getBalance());
        System.out.println("Savings Rate: " + stats.getSavingsRate());
        
        // Tạo cards với dữ liệu thực và animation mới
        VBox incomeCard = createEnhancedStatCard(
            "💰", "Monthly Income", 
            "$" + String.format("%.0f", stats.getMonthlyIncome()), 
            "+12.5%", true, ACCENT_GREEN, true
        );
        
        VBox outcomeCard = createEnhancedStatCard(
            "💳", "Monthly Expenses", 
            "$" + String.format("%.0f", stats.getMonthlyOutcome()), 
            "+8.2%", true, ACCENT_PURPLE, false
        );
        
        VBox balanceCard = createEnhancedStatCard(
            "💎", "Current Balance", 
            "$" + String.format("%.0f", stats.getBalance()), 
            stats.getBalance() >= 0 ? "+5.4%" : "-2.1%", 
            true, stats.getBalance() >= 0 ? ACCENT_BLUE : ACCENT_PINK, false
        );
        
        VBox savingsCard = createEnhancedStatCard(
            "📈", "Savings Rate", 
            String.format("%.1f%%", stats.getSavingsRate()), 
            "+2.1%", true, ACCENT_CYAN, false
        );
        
        // Thêm Progress Ring cho main card
        incomeCard = addProgressRingToCard(incomeCard, stats.getSavingsRate() / 100.0);
        
        cards.getChildren().addAll(incomeCard, outcomeCard, balanceCard, savingsCard);
        
        // Enhanced staggered animation với bounce effect
        animateCardsWithBounce(cards);
        
        // Thêm hover animation cho tất cả cards
        addAdvancedHoverEffects(cards);
        
        return cards;
    }

    private void animateCardsWithBounce(HBox cards) {
        for (int i = 0; i < cards.getChildren().size(); i++) {
            VBox card = (VBox) cards.getChildren().get(i);
            
            // Initial state
            card.setOpacity(0);
            card.setScaleX(0.5);
            card.setScaleY(0.5);
            card.setTranslateY(50);
            card.setRotate(-10);
            
            // Create bounce animation
            Duration delay = Duration.millis(i * 150);
            Duration duration = Duration.millis(800);
            
            // Fade in
            FadeTransition fade = new FadeTransition(duration, card);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setDelay(delay);
            
            // Scale bounce
            ScaleTransition scaleX = new ScaleTransition(duration, card);
            scaleX.setFromX(0.5);
            scaleX.setToX(1.0);
            scaleX.setDelay(delay);
            scaleX.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            
            ScaleTransition scaleY = new ScaleTransition(duration, card);
            scaleY.setFromY(0.5);
            scaleY.setToY(1.0);
            scaleY.setDelay(delay);
            scaleY.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            
            // Slide up
            TranslateTransition translate = new TranslateTransition(duration, card);
            translate.setFromY(50);
            translate.setToY(0);
            translate.setDelay(delay);
            translate.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            
            // Rotation
            RotateTransition rotate = new RotateTransition(duration, card);
            rotate.setFromAngle(-10);
            rotate.setToAngle(0);
            rotate.setDelay(delay);
            rotate.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            
            ParallelTransition parallel = new ParallelTransition(fade, scaleX, scaleY, translate, rotate);
            parallel.play();
        }
    }

    private void addAdvancedHoverEffects(HBox cards) {
        for (javafx.scene.Node node : cards.getChildren()) {
            VBox card = (VBox) node;
            
            card.setOnMouseEntered(e -> {
                // Glow effect
                DropShadow glow = new DropShadow();
                glow.setColor(ACCENT_PURPLE);
                glow.setRadius(20);
                glow.setSpread(0.3);
                
                // Scale and glow animation
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
                scale.setToX(1.05);
                scale.setToY(1.05);
                
                TranslateTransition lift = new TranslateTransition(Duration.millis(200), card);
                lift.setToY(-5);
                
                ParallelTransition hoverIn = new ParallelTransition(scale, lift);
                hoverIn.play();
                
                card.setEffect(glow);
            });
            
            card.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
                scale.setToX(1.0);
                scale.setToY(1.0);
                
                TranslateTransition drop = new TranslateTransition(Duration.millis(200), card);
                drop.setToY(0);
                
                ParallelTransition hoverOut = new ParallelTransition(scale, drop);
                hoverOut.play();
                
                card.setEffect(null);
            });
        }
    }

    private VBox addProgressRingToCard(VBox card, double progress) {
        // Thêm progress ring vào card chính
        HBox header = (HBox) card.getChildren().get(0);
        StackPane iconContainer = (StackPane) header.getChildren().get(0);
        
        // Create progress ring
        Circle progressRing = new Circle(30);
        progressRing.setFill(Color.TRANSPARENT);
        progressRing.setStroke(ThemeManager.toRgbaColor(ACCENT_GREEN, 0.3));
        progressRing.setStrokeWidth(3);
        
        Arc progressArc = new Arc();
        progressArc.setCenterX(0);
        progressArc.setCenterY(0);
        progressArc.setRadiusX(30);
        progressArc.setRadiusY(30);
        progressArc.setStartAngle(90);
        progressArc.setLength(0); // Will be animated
        progressArc.setType(ArcType.OPEN);
        progressArc.setFill(Color.TRANSPARENT);
        progressArc.setStroke(ACCENT_GREEN);
        progressArc.setStrokeWidth(3);
        
        iconContainer.getChildren().addAll(progressRing, progressArc);
        
        // Animate progress ring
        Timeline progressAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new javafx.animation.KeyValue(progressArc.lengthProperty(), 0)),
            new KeyFrame(Duration.millis(1500), 
                new javafx.animation.KeyValue(progressArc.lengthProperty(), -(progress * 360)))
        );
        progressAnimation.setDelay(Duration.millis(800));
        progressAnimation.play();
        
        return card;
    }

    // Thêm method mới để tạo floating animation cho các elements
    private void addFloatingAnimation(javafx.scene.Node node, Duration duration, double range) {
        TranslateTransition float1 = new TranslateTransition(duration, node);
        float1.setFromY(0);
        float1.setToY(-range);
        float1.setAutoReverse(true);
        float1.setCycleCount(Timeline.INDEFINITE);
        float1.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        
        float1.play();
    }

    private VBox createEnhancedStatCard(String icon, String title, String value, String change, 
                                       boolean isPositive, Color accentColor, boolean isMain) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setPrefWidth(280);
        card.setPrefHeight(140);
        card.setStyle(
            "-fx-background-color: " + ThemeManager.toHexString(currentTheme.getCardBg()) + ";" +
            "-fx-background-radius: 15;" +
            "-fx-border-radius: 15;" +
            "-fx-border-color: " + ThemeManager.toRgbaString(accentColor, 0.3) + ";" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, " + ThemeManager.toRgbaString(currentTheme.getShadowColor(), 0.1) + ", 15, 0, 0, 5);");
        
        // Header with icon
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Icon background
        StackPane iconBg = new StackPane();
        Rectangle iconRect = new Rectangle(40, 40);
        iconRect.setArcWidth(12);
        iconRect.setArcHeight(12);
        iconRect.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, accentColor),
            new Stop(1, accentColor.darker())
        ));
        iconRect.setEffect(new DropShadow(10, accentColor));
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(20));
        iconLabel.setTextFill(TEXT_PRIMARY);
        
        iconBg.getChildren().addAll(iconRect, iconLabel);
        
        Label titleLabel = new Label(title);
        titleLabel.setTextFill(TEXT_SECONDARY);
        titleLabel.setFont(Font.font("Segoe UI", 12));
        
        header.getChildren().addAll(iconBg, titleLabel);
        
        // Value section
        HBox valueSection = new HBox(10);
        valueSection.setAlignment(Pos.BASELINE_LEFT);
        
        Label valueLabel = new Label(value);
        valueLabel.setTextFill(TEXT_PRIMARY);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        
        Label changeLabel = new Label(change);
        changeLabel.setTextFill(isPositive ? ACCENT_GREEN : ACCENT_PINK);
        changeLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        changeLabel.setStyle(
            "-fx-background-color: " + (isPositive ? 
                toRgbaString(ACCENT_GREEN, 0.2) : toRgbaString(ACCENT_PINK, 0.2)) + ";" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 2 8 2 8;"
        );
        
        valueSection.getChildren().addAll(valueLabel, changeLabel);
        
        card.getChildren().addAll(header, valueSection);
        return card;
    }

    private VBox createModernExpensesChart() {
        VBox chartCard = new VBox(20);
        chartCard.setPadding(new Insets(25));
        chartCard.setStyle(
            "-fx-background-color: " + ThemeManager.toHexString(CARD_BG) + ";" +
            "-fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);"
        );
        
        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Expenses");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Time filter
        ComboBox<String> timeFilter = new ComboBox<>();
        timeFilter.getItems().addAll("All", "Utility", "Entertainment", "Groceries");
        timeFilter.setValue("All");
        timeFilter.setStyle(
            "-fx-background-color: " + ThemeManager.toHexString(BG_SECONDARY) + ";" +
            "-fx-text-fill: " + ThemeManager.toHexString(TEXT_PRIMARY) + ";" +
            "-fx-border-color: transparent;" +
            "-fx-background-radius: 8;"
        );
        
        header.getChildren().addAll(title, spacer, timeFilter);
        
        // Create modern area chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        
        xAxis.setTickLabelFill(TEXT_MUTED);
        yAxis.setTickLabelFill(TEXT_MUTED);
        xAxis.setTickLabelFont(Font.font("Segoe UI", 10));
        yAxis.setTickLabelFont(Font.font("Segoe UI", 10));
        
        AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setLegendVisible(false);
        areaChart.setAnimated(true);
        areaChart.setStyle("-fx-background-color: transparent;");
        areaChart.setCreateSymbols(false);
        areaChart.setPrefHeight(300);
        
        // Add data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        List<MonthlyData> monthlyData = dbManager.getMonthlyChartData();
        
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (int i = 0; i < Math.min(months.length, monthlyData.size()); i++) {
            series.getData().add(new XYChart.Data<>(months[i], monthlyData.get(i).getOutcome()));
        }
        
        areaChart.getData().add(series);
        
        // Style the chart
        areaChart.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");
        areaChart.lookup(".chart-series-area-fill").setStyle(
            "-fx-fill: linear-gradient(to bottom, " + ThemeManager.toRgbaString(ACCENT_PURPLE, 0.5) + ", transparent);"
        );
        areaChart.lookup(".chart-series-area-line").setStyle(
            "-fx-stroke: " + ThemeManager.toHexString(ACCENT_PURPLE) + ";" +
            "-fx-stroke-width: 3;"
        );
        
        // Outcome info
        HBox outcomeInfo = new HBox(20);
        outcomeInfo.setAlignment(Pos.CENTER_LEFT);
        
        Circle dot = new Circle(6, ACCENT_PURPLE);
        Label outcomeLabel = new Label("Outcome");
        outcomeLabel.setTextFill(TEXT_SECONDARY);
        outcomeLabel.setFont(Font.font("Segoe UI", 14));
        
        Label outcomeValue = new Label("$" + String.format("%.0f", dbManager.getMonthlyOutcome(LocalDate.now())));
        outcomeValue.setTextFill(TEXT_PRIMARY);
        outcomeValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        Label changeLabel = new Label("+13%");
        changeLabel.setTextFill(ACCENT_PURPLE);
        changeLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        changeLabel.setStyle(
            "-fx-background-color: " + ThemeManager.toRgbaString(ACCENT_PURPLE, 0.2) + ";" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 2 8 2 8;"
        );
        
        outcomeInfo.getChildren().addAll(dot, outcomeLabel, outcomeValue, changeLabel);
        
        chartCard.getChildren().addAll(header, areaChart, outcomeInfo);
        return chartCard;
    }

    private VBox createModernSpendingBreakdown() {
        VBox breakdownCard = new VBox(20);
        breakdownCard.setPadding(new Insets(25));
        breakdownCard.setPrefWidth(400);
        breakdownCard.setStyle(
            "-fx-background-color: " + ThemeManager.toHexString(CARD_BG) + ";" +
            "-fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);"
        );
        
        Label title = new Label("Spending Breakdown");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        // Donut chart container
        StackPane chartContainer = new StackPane();
        chartContainer.setPrefHeight(250);
        
        // Create custom donut chart
        Pane donutChart = createDonutChart();
        
        // Center info
        VBox centerInfo = new VBox(5);
        centerInfo.setAlignment(Pos.CENTER);
        
        Label totalLabel = new Label("$1,200");
        totalLabel.setTextFill(TEXT_PRIMARY);
        totalLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        
        Label categoryLabel = new Label("Utility");
        categoryLabel.setTextFill(ACCENT_BLUE);
        categoryLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        centerInfo.getChildren().addAll(totalLabel, categoryLabel);
        
        chartContainer.getChildren().addAll(donutChart, centerInfo);
        
        // Legend
        VBox legend = new VBox(10);
        legend.setPadding(new Insets(20, 0, 0, 0));
        
        addLegendItem(legend, "Utility 30%", ACCENT_BLUE, "⚡");
        addLegendItem(legend, "Entertainment 20%", ACCENT_PURPLE, "🎬");
        addLegendItem(legend, "Groceries 50%", ACCENT_GREEN, "🛒");
        
        breakdownCard.getChildren().addAll(title, chartContainer, legend);
        return breakdownCard;
    }

    private Pane createDonutChart() {
        Pane pane = new Pane();
        pane.setPrefSize(250, 250);
        
        double centerX = 125;
        double centerY = 125;
        double radius = 80;
        double innerRadius = 60;
        
        // Data
        double[] values = {30, 20, 50}; // Percentages
        Color[] colors = {ACCENT_BLUE, ACCENT_PURPLE, ACCENT_GREEN};
        
        double startAngle = -90;
        for (int i = 0; i < values.length; i++) {
            double angle = values[i] * 3.6; // Convert percentage to degrees
            
            Arc arc = new Arc(centerX, centerY, radius, radius, startAngle, angle);
            arc.setType(ArcType.ROUND);
            arc.setFill(colors[i]);
            arc.setStrokeWidth(0);
            
            // Add hover effect
            final int index = i;
            arc.setOnMouseEntered(e -> {
                arc.setScaleX(1.05);
                arc.setScaleY(1.05);
                arc.setEffect(new DropShadow(20, colors[index]));
            });
            
            arc.setOnMouseExited(e -> {
                arc.setScaleX(1.0);
                arc.setScaleY(1.0);
                arc.setEffect(null);
            });
            
            pane.getChildren().add(arc);
            startAngle += angle;
        }
        
        // Inner circle to create donut effect
        Circle innerCircle = new Circle(centerX, centerY, innerRadius);
        innerCircle.setFill(CARD_BG);
        pane.getChildren().add(innerCircle);
        
        return pane;
    }

    private void addLegendItem(VBox legend, String text, Color color, String icon) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(14));
        
        Circle dot = new Circle(6, color);
        dot.setEffect(new DropShadow(5, color));
        
        Label label = new Label(text);
        label.setTextFill(TEXT_SECONDARY);
        label.setFont(Font.font("Segoe UI", 14));
        
        item.getChildren().addAll(iconLabel, dot, label);
        legend.getChildren().add(item);
    }

    private VBox createModernTransactions() {
        VBox transactionCard = new VBox(20);
        transactionCard.setPadding(new Insets(25));
        transactionCard.setStyle(
            "-fx-background-color: " + ThemeManager.toHexString(CARD_BG) + ";" +
            "-fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);"
        );
        
        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Recent Transactions");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button viewAllBtn = new Button("View All");
        viewAllBtn.setStyle(
            "-fx-background-color: " + ThemeManager.toHexString(ACCENT_PURPLE) + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-font-size: 12;" +
            "-fx-cursor: hand;"
        );
        
        header.getChildren().addAll(title, spacer, viewAllBtn);
        
        // Transaction list
        VBox transactionList = new VBox(12);
        
        // Get recent outcomes from database
        List<OutcomeRecord> recentOutcomes = dbManager.getOutcomeRecords();
        for (int i = 0; i < Math.min(3, recentOutcomes.size()); i++) {
            HBox transaction = createModernTransactionItem(recentOutcomes.get(i));
            transactionList.getChildren().add(transaction);
        }
        
        transactionCard.getChildren().addAll(header, transactionList);
        return transactionCard;
    }

    private HBox createModernTransactionItem(OutcomeRecord record) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12));
        item.setStyle(
            "-fx-background-color: " + ThemeManager.toRgbaString(TEXT_PRIMARY, 0.03) + ";" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        );
        
        // Icon
        StackPane iconContainer = new StackPane();
        Circle iconBg = new Circle(20);
        iconBg.setFill(getCategoryGradient(record.getCategory()));
        
        Label icon = new Label(getCategoryIcon(record.getCategory()));
        icon.setFont(Font.font(16));
        icon.setTextFill(TEXT_PRIMARY);
        
        iconContainer.getChildren().addAll(iconBg, icon);
        
        // Details
        VBox details = new VBox(4);
        Label nameLabel = new Label(record.getTitle());
        nameLabel.setTextFill(TEXT_PRIMARY);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        Label dateLabel = new Label(record.getDate().format(DateTimeFormatter.ofPattern("MMM dd")) + " at " + 
                                   record.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")));
        dateLabel.setTextFill(TEXT_MUTED);
        dateLabel.setFont(Font.font("Segoe UI", 11));
        
        details.getChildren().addAll(nameLabel, dateLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Amount
        Label amount = new Label("-$" + String.format("%.0f", record.getAmount()));
        amount.setTextFill(TEXT_PRIMARY);
        amount.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        // Hover effect
        item.setOnMouseEntered(e -> {
            item.setStyle(item.getStyle().replace(
                ThemeManager.toRgbaString(TEXT_PRIMARY, 0.03),
                ThemeManager.toRgbaString(TEXT_PRIMARY, 0.08)
            ));
        });
        
        item.setOnMouseExited(e -> {
            item.setStyle(item.getStyle().replace(
                ThemeManager.toRgbaString(TEXT_PRIMARY, 0.08),
                ThemeManager.toRgbaString(TEXT_PRIMARY, 0.03)
            ));
        });
        
        item.getChildren().addAll(iconContainer, details, spacer, amount);
        return item;
    }

    private VBox createProfileSection() {
        VBox profile = new VBox(15);
        profile.setPadding(new Insets(20, 0, 0, 0));
        
        HBox profileInfo = new HBox(12);
        profileInfo.setAlignment(Pos.CENTER_LEFT);
        
        // Avatar with gradient border
        StackPane avatarContainer = new StackPane();
        Circle avatarBorder = new Circle(25);
        avatarBorder.setFill(Color.TRANSPARENT);
        avatarBorder.setStroke(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, ACCENT_PURPLE),
            new Stop(1, ACCENT_PINK)
        ));
        avatarBorder.setStrokeWidth(2);
        
        Circle avatar = new Circle(23);
        avatar.setFill(ACCENT_PINK);
        
        avatarContainer.getChildren().addAll(avatarBorder, avatar);
        
        VBox userInfo = new VBox(3);
        Label userName = new Label("Ferra Alexandra");
        userName.setTextFill(TEXT_PRIMARY);
        userName.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        Label userRole = new Label("UX Designer");
        userRole.setTextFill(TEXT_SECONDARY);
        userRole.setFont(Font.font("Segoe UI", 12));
        
        userInfo.getChildren().addAll(userName, userRole);
        profileInfo.getChildren().addAll(avatarContainer, userInfo);
        
        // Spending limit
        VBox spendingLimit = new VBox(8);
        Label limitLabel = new Label("Spending Limit");
        limitLabel.setTextFill(TEXT_MUTED);
        limitLabel.setFont(Font.font("Segoe UI", 11));
        
        ProgressBar progressBar = new ProgressBar(0.7);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(6);
        progressBar.setStyle(
            "-fx-accent: " + ThemeManager.toHexString(ACCENT_PURPLE) + ";" +
            "-fx-background-color: " + ThemeManager.toRgbaString(TEXT_PRIMARY, 0.1) + ";" +
            "-fx-background-radius: 3;"
        );
        
        Label limitAmount = new Label("$42000");
        limitAmount.setTextFill(TEXT_PRIMARY);
        limitAmount.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        spendingLimit.getChildren().addAll(limitLabel, progressBar, limitAmount);
        
        profile.getChildren().addAll(profileInfo, spendingLimit);
        return profile;
    }

    // Page loaders
    private void loadIncomePage() {
        contentArea.getChildren().clear();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        VBox incomePage = new VBox(25);
        incomePage.setPadding(new Insets(30));
        
        Label title = new Label("Income Management");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        
        // Enhanced income page from FullIncomePage
        FullIncomePage fullIncomePage = new FullIncomePage(dbManager);
        VBox incomeContent = fullIncomePage.createIncomePageContent();
        
        incomePage.getChildren().addAll(title, incomeContent);
        scrollPane.setContent(incomePage);
        contentArea.getChildren().add(scrollPane);
    }

    private void loadOutcomePage() {
        contentArea.getChildren().clear();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        VBox outcomePage = new VBox(25);
        outcomePage.setPadding(new Insets(30));
        
        Label title = new Label("Expense Management");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        
        // Enhanced outcome page from FullOutcomePage
        FullOutcomePage fullOutcomePage = new FullOutcomePage(dbManager);
        VBox outcomeContent = fullOutcomePage.createOutcomePageContent();
        
        outcomePage.getChildren().addAll(title, outcomeContent);
        scrollPane.setContent(outcomePage);
        contentArea.getChildren().add(scrollPane);
    }

    private void loadAnalyticsPage() {
        contentArea.getChildren().clear();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        VBox analyticsPage = new VBox(25);
        analyticsPage.setPadding(new Insets(30));
        
        // Header with theme-aware colors
        HBox header = createAnalyticsHeader();
        
        // Enhanced analytics content
        VBox analyticsContent = createEnhancedAnalyticsContent();
        
        analyticsPage.getChildren().addAll(header, analyticsContent);
        scrollPane.setContent(analyticsPage);
        contentArea.getChildren().add(scrollPane);
    }

    private HBox createAnalyticsHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleSection = new VBox(8);
        
        Label title = new Label("📈 Advanced Analytics");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        
        Label subtitle = new Label("Deep insights into your financial patterns and trends");
        subtitle.setTextFill(TEXT_SECONDARY);
        subtitle.setFont(Font.font("Segoe UI", 14));
        
        titleSection.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Analytics actions
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button exportBtn = new Button("📊 Export Report");
        exportBtn.setStyle(createThemedSecondaryButtonStyle());
        exportBtn.setOnAction(e -> exportAnalyticsReport());
        
        Button refreshBtn = new Button("🔄 Refresh Data");
        refreshBtn.setStyle(createThemedPrimaryButtonStyle());
        refreshBtn.setOnAction(e -> refreshAnalyticsData());
        
        actions.getChildren().addAll(exportBtn, refreshBtn);
        header.getChildren().addAll(titleSection, spacer, actions);
        
        return header;
    }

    private VBox createEnhancedAnalyticsContent() {
        VBox content = new VBox(30);
        
        try {
            // Initialize services
            AnalyticsService analytics = new AnalyticsService(dbManager);
            EnhancedAnalyticsService enhancedAnalytics = new EnhancedAnalyticsService(dbManager);
            AdvancedAnalyticsService advancedAnalytics = new AdvancedAnalyticsService(dbManager);
            
            // Financial score card (enhanced with theme)
            HBox scoreCard = createEnhancedFinancialScoreCard(analytics.calculateFinancialScore());
            
            // Advanced charts row 1
            HBox chartsRow1 = new HBox(20);
            chartsRow1.setAlignment(Pos.CENTER_LEFT);
            
            VBox trendChart = advancedAnalytics.createTrendlineChart();
            VBox savingsGoalChart = advancedAnalytics.createSavingsGoalChart();
            
            HBox.setHgrow(trendChart, Priority.ALWAYS);
            chartsRow1.getChildren().addAll(trendChart, savingsGoalChart);
            
            // Advanced charts row 2
            HBox chartsRow2 = new HBox(20);
            chartsRow2.setAlignment(Pos.CENTER_LEFT);
            
            VBox heatmapChart = advancedAnalytics.createSpendingHeatmap();
            VBox waterfallChart = advancedAnalytics.createCashFlowWaterfallChart();
            
            HBox.setHgrow(waterfallChart, Priority.ALWAYS);
            chartsRow2.getChildren().addAll(heatmapChart, waterfallChart);
            
            // Bubble chart (full width)
            VBox bubbleChart = advancedAnalytics.createCategoryBubbleChart();
            
            // Enhanced insights section
            VBox enhancedSection = createEnhancedAnalyticsInsights(enhancedAnalytics);
            
            // Traditional insights cards
            HBox insightsCards = new HBox(20);
            List<AnalyticsService.FinancialInsight> insights = analytics.generateFinancialInsights();
            
            for (int i = 0; i < Math.min(3, insights.size()); i++) {
                VBox insightCard = createThemedInsightCard(insights.get(i));
                insightsCards.getChildren().add(insightCard);
            }
            
            // Animate content appearance
            animateAnalyticsContent(content);
            
            content.getChildren().addAll(
                scoreCard, 
                chartsRow1, 
                chartsRow2, 
                bubbleChart,
                enhancedSection,
                insightsCards
            );
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to create analytics content", e);
            content.getChildren().add(createAnalyticsErrorContent(e.getMessage()));
        }
        
        return content;
    }

    private HBox createEnhancedFinancialScoreCard(double score) {
        HBox card = new HBox(30);
        card.setPadding(new Insets(30));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
            "-fx-background-color: linear-gradient(to right, " + 
            ThemeManager.toRgbaString(ACCENT_PURPLE, 0.8) + ", " + 
            ThemeManager.toRgbaString(ACCENT_PINK, 0.8) + ");" +
            "-fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, " + ThemeManager.toRgbaString(ACCENT_PURPLE, 0.4) + ", 20, 0, 0, 10);"
        );
        
        // Score circle with animation
        StackPane scoreCircle = new StackPane();
        Circle outerCircle = new Circle(60);
        outerCircle.setFill(Color.TRANSPARENT);
        outerCircle.setStroke(ThemeManager.toRgbaColor(TEXT_PRIMARY, 0.3));
        outerCircle.setStrokeWidth(3);
        
        Arc scoreArc = new Arc(0, 0, 60, 60, 90, -(score * 3.6));
        scoreArc.setType(ArcType.OPEN);
        scoreArc.setFill(Color.TRANSPARENT);
        scoreArc.setStroke(TEXT_PRIMARY);
        scoreArc.setStrokeWidth(5);
        
        Label scoreLabel = new Label(String.format("%.0f", score));
        scoreLabel.setTextFill(TEXT_PRIMARY);
        scoreLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        
        scoreCircle.getChildren().addAll(outerCircle, scoreArc, scoreLabel);
        
        // Score details with theme-aware colors
        VBox details = new VBox(10);
        Label titleLabel = new Label("Financial Health Score");
        titleLabel.setTextFill(TEXT_PRIMARY);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        
        Label gradeLabel = new Label("Grade: " + getGradeFromScore(score));
        gradeLabel.setTextFill(ThemeManager.toRgbaColor(TEXT_PRIMARY, 0.9));
        gradeLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 18));
        
        Label descLabel = new Label("Your financial health is " + getHealthDescription(score));
        descLabel.setTextFill(ThemeManager.toRgbaColor(TEXT_PRIMARY, 0.8));
        descLabel.setFont(Font.font("Segoe UI", 14));
        
        details.getChildren().addAll(titleLabel, gradeLabel, descLabel);
        
        card.getChildren().addAll(scoreCircle, details);
        return card;
    }

    private VBox createEnhancedAnalyticsInsights(EnhancedAnalyticsService enhancedAnalytics) {
        VBox section = new VBox(20);
        
        // Section title
        Label sectionTitle = new Label("🔮 AI-Powered Insights");
        sectionTitle.setTextFill(TEXT_PRIMARY);
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        
        // Create cards for each enhanced feature
        HBox enhancedCards = new HBox(20);
        
        // Spending Forecast Card
        VBox forecastCard = createThemedSpendingForecastCard(enhancedAnalytics);
        
        // Anomaly Detection Card
        VBox anomalyCard = createThemedAnomalyDetectionCard(enhancedAnalytics);
        
        // Risk Assessment Card
        VBox riskCard = createThemedRiskAssessmentCard(enhancedAnalytics);
        
        enhancedCards.getChildren().addAll(forecastCard, anomalyCard, riskCard);
        section.getChildren().addAll(sectionTitle, enhancedCards);
        
        return section;
    }

    private VBox createThemedSpendingForecastCard(EnhancedAnalyticsService enhancedAnalytics) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setPrefWidth(350);
        card.setStyle(createThemedCardStyle(ACCENT_BLUE));
        
        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label("🔮");
        icon.setFont(Font.font(20));
        
        Label title = new Label("Spending Forecast");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        header.getChildren().addAll(icon, title);
        
        // Get forecast data
        Map<String, EnhancedAnalyticsService.SpendingForecast> forecasts = enhancedAnalytics.forecastSpendingPatterns(6);
        
        VBox forecastContent = new VBox(10);
        
        if (!forecasts.isEmpty()) {
            int count = 0;
            for (Map.Entry<String, EnhancedAnalyticsService.SpendingForecast> entry : forecasts.entrySet()) {
                if (count >= 3) break;
                
                String category = entry.getKey();
                
                HBox forecastItem = new HBox(10);
                forecastItem.setAlignment(Pos.CENTER_LEFT);
                
                Label categoryLabel = new Label(category);
                categoryLabel.setTextFill(TEXT_SECONDARY);
                categoryLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                Label trendLabel = new Label("📈 +5%");
                trendLabel.setTextFill(ACCENT_GREEN);
                trendLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                
                forecastItem.getChildren().addAll(categoryLabel, spacer, trendLabel);
                forecastContent.getChildren().add(forecastItem);
                
                count++;
            }
        } else {
            Label noDataLabel = new Label("Insufficient data for forecasting");
            noDataLabel.setTextFill(TEXT_MUTED);
            noDataLabel.setFont(Font.font("Segoe UI", 12));
            forecastContent.getChildren().add(noDataLabel);
        }
        
        card.getChildren().addAll(header, forecastContent);
        return card;
    }

    private VBox createThemedAnomalyDetectionCard(EnhancedAnalyticsService enhancedAnalytics) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setPrefWidth(350);
        card.setStyle(createThemedCardStyle(ACCENT_PINK));
        
        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label("⚠️");
        icon.setFont(Font.font(20));
        
        Label title = new Label("Anomaly Detection");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        header.getChildren().addAll(icon, title);
        
        // Get anomaly data
        List<EnhancedAnalyticsService.SpendingAnomaly> anomalies = enhancedAnalytics.detectSpendingAnomalies();
        
        VBox anomalyContent = new VBox(10);
        
        if (!anomalies.isEmpty()) {
            int count = 0;
            for (EnhancedAnalyticsService.SpendingAnomaly anomaly : anomalies) {
                if (count >= 3) break;
                
                HBox anomalyItem = new HBox(10);
                anomalyItem.setAlignment(Pos.CENTER_LEFT);
                
                Label categoryLabel = new Label(anomaly.getCategory());
                categoryLabel.setTextFill(TEXT_SECONDARY);
                categoryLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                Label amountLabel = new Label(String.format("$%.0f", anomaly.getAmount()));
                amountLabel.setTextFill(ACCENT_PINK);
                amountLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                
                anomalyItem.getChildren().addAll(categoryLabel, spacer, amountLabel);
                anomalyContent.getChildren().add(anomalyItem);
                
                count++;
            }
        } else {
            Label noDataLabel = new Label("No anomalies detected");
            noDataLabel.setTextFill(TEXT_MUTED);
            noDataLabel.setFont(Font.font("Segoe UI", 12));
            anomalyContent.getChildren().add(noDataLabel);
        }
        
        card.getChildren().addAll(header, anomalyContent);
        return card;
    }

    private VBox createThemedRiskAssessmentCard(EnhancedAnalyticsService enhancedAnalytics) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setPrefWidth(350);
        card.setStyle(createThemedCardStyle(ACCENT_GREEN));
        
        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label("🛡️");
        icon.setFont(Font.font(20));
        
        Label title = new Label("Risk Assessment");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        header.getChildren().addAll(icon, title);
        
        // Get risk assessment data
        EnhancedAnalyticsService.FinancialRiskAssessment risk = enhancedAnalytics.assessFinancialRisk(1000);
        
        VBox riskContent = new VBox(10);
        
        // Risk level
        HBox riskLevelItem = new HBox(10);
        riskLevelItem.setAlignment(Pos.CENTER_LEFT);
        
        Label riskLevelLabel = new Label("Risk Level:");
        riskLevelLabel.setTextFill(TEXT_SECONDARY);
        riskLevelLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        Label riskValueLabel = new Label(risk.getRiskLevel());
        riskValueLabel.setTextFill(getRiskColor(risk.getRiskLevel()));
        riskValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        
        riskLevelItem.getChildren().addAll(riskLevelLabel, spacer1, riskValueLabel);
        
        // Risk score
        HBox riskScoreItem = new HBox(10);
        riskScoreItem.setAlignment(Pos.CENTER_LEFT);
        
        Label riskScoreLabel = new Label("Score:");
        riskScoreLabel.setTextFill(TEXT_SECONDARY);
        riskScoreLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        Label scoreValueLabel = new Label(String.format("%.1f/100", risk.getRiskScore()));
        scoreValueLabel.setTextFill(TEXT_PRIMARY);
        scoreValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        
        riskScoreItem.getChildren().addAll(riskScoreLabel, spacer2, scoreValueLabel);
        
        riskContent.getChildren().addAll(riskLevelItem, riskScoreItem);
        card.getChildren().addAll(header, riskContent);
        
        return card;
    }

    private VBox createThemedInsightCard(AnalyticsService.FinancialInsight insight) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setPrefWidth(300);
        card.setStyle(createThemedCardStyle(getInsightColor(insight.getLevel())));
        
        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label(insight.getIcon());
        icon.setFont(Font.font(20));
        
        Label title = new Label(insight.getTitle());
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        header.getChildren().addAll(icon, title);
        
        // Message
        Label message = new Label(insight.getMessage());
        message.setTextFill(TEXT_SECONDARY);
        message.setFont(Font.font("Segoe UI", 13));
        message.setWrapText(true);
        
        // Level indicator
        HBox levelIndicator = new HBox(8);
        Circle levelDot = new Circle(4, getInsightColor(insight.getLevel()));
        Label levelLabel = new Label(insight.getLevel().toString());
        levelLabel.setTextFill(getInsightColor(insight.getLevel()));
        levelLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        
        levelIndicator.getChildren().addAll(levelDot, levelLabel);
        
        card.getChildren().addAll(header, message, levelIndicator);
        return card;
    }

    private VBox createAnalyticsErrorContent(String errorMessage) {
        VBox errorContent = new VBox(20);
        errorContent.setAlignment(Pos.CENTER);
        errorContent.setPadding(new Insets(50));
        
        Label errorIcon = new Label("⚠️");
        errorIcon.setFont(Font.font(32));
        
        Label errorLabel = new Label("Analytics Error");
        errorLabel.setTextFill(currentTheme.getError());
        errorLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        
        Label errorDesc = new Label("Failed to load analytics: " + errorMessage);
        errorDesc.setTextFill(TEXT_SECONDARY);
        errorDesc.setFont(Font.font("Segoe UI", 14));
        errorDesc.setWrapText(true);
        
        Button retryBtn = new Button("🔄 Retry");
        retryBtn.setStyle(createThemedPrimaryButtonStyle());
        retryBtn.setOnAction(e -> loadAnalyticsPage());
        
        errorContent.getChildren().addAll(errorIcon, errorLabel, errorDesc, retryBtn);
        return errorContent;
    }

    private void animateAnalyticsContent(VBox content) {
        // Staggered fade-in animation for analytics content
        for (int i = 0; i < content.getChildren().size(); i++) {
            javafx.scene.Node node = content.getChildren().get(i);
            
            node.setOpacity(0);
            node.setTranslateY(20);
            
            FadeTransition fade = new FadeTransition(Duration.millis(400), node);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setDelay(Duration.millis(i * 100));
            
            TranslateTransition slide = new TranslateTransition(Duration.millis(400), node);
            slide.setFromY(20);
            slide.setToY(0);
            slide.setDelay(Duration.millis(i * 100));
            
            ParallelTransition animation = new ParallelTransition(fade, slide);
            animation.play();
        }
    }

    // Helper methods for themed styling
    private String createThemedCardStyle(Color accentColor) {
        return "-fx-background-color: " + ThemeManager.toHexString(currentTheme.getCardBg()) + ";" +
               "-fx-background-radius: 16px;" +
               "-fx-border-color: " + ThemeManager.toRgbaString(accentColor, 0.3) + ";" +
               "-fx-border-width: 1px;" +
               "-fx-border-radius: 16px;" +
               "-fx-effect: dropshadow(gaussian, " + ThemeManager.toRgbaString(currentTheme.getShadowColor(), 0.1) + ", 15, 0, 0, 5);";
    }

    private String createThemedPrimaryButtonStyle() {
        return "-fx-background-color: " + ThemeManager.toHexString(ACCENT_PURPLE) + ";" +
               "-fx-text-fill: white;" +
               "-fx-background-radius: 10px;" +
               "-fx-padding: 10 20;" +
               "-fx-font-weight: bold;" +
               "-fx-cursor: hand;" +
               "-fx-effect: dropshadow(three-pass-box, " + ThemeManager.toRgbaString(ACCENT_PURPLE, 0.4) + ", 10, 0, 0, 3);";
    }

    private String createThemedSecondaryButtonStyle() {
        return "-fx-background-color: " + ThemeManager.toRgbaString(TEXT_PRIMARY, 0.1) + ";" +
               "-fx-text-fill: " + ThemeManager.toHexString(TEXT_PRIMARY) + ";" +
               "-fx-background-radius: 10px;" +
               "-fx-padding: 10 20;" +
               "-fx-cursor: hand;" +
               "-fx-border-color: " + ThemeManager.toRgbaString(TEXT_PRIMARY, 0.2) + ";" +
               "-fx-border-width: 1px;" +
               "-fx-border-radius: 10px;";
    }

    // Event handlers
    private void exportAnalyticsReport() {
        showInfoMessage("📊 Analytics export feature coming soon!");
    }

    private void refreshAnalyticsData() {
        loadAnalyticsPage(); // Refresh by reloading
    }

    private void showInfoMessage(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Analytics");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadTransactionPage() {
        contentArea.getChildren().clear();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        VBox transactionPage = new VBox(25);
        transactionPage.setPadding(new Insets(30));
        
        Label title = new Label(LanguageManager.getInstance().translate("transaction") + " History");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        
        // Enhanced transaction filters
        VBox filtersSection = createEnhancedTransactionFilters();
        
        // Transaction list with filtering
        VBox transactionList = createFilteredTransactionList();
        
        transactionPage.getChildren().addAll(title, filtersSection, transactionList);
        scrollPane.setContent(transactionPage);
        contentArea.getChildren().add(scrollPane);
    }

    private VBox createEnhancedTransactionFilters() {
        VBox filtersContainer = new VBox(15);
        filtersContainer.setPadding(new Insets(20));
        filtersContainer.setStyle(
            "-fx-background-color: " + ThemeManager.toHexString(CARD_BG) + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);"
        );
        
        Label filtersTitle = new Label("🔍 Advanced Search & Filters");
        filtersTitle.setTextFill(TEXT_PRIMARY);
        filtersTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        // Enhanced search row
        HBox searchRow = new HBox(15);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        
        // Tìm kiếm văn bản nâng cao
        TextField advancedSearch = new TextField();
        advancedSearch.setPromptText("🔍 Search by title, category, description, amount...");
        advancedSearch.setPrefWidth(350);
        advancedSearch.setStyle(createEnhancedTextFieldStyle());
        
        // Realtime search
        advancedSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            filterTransactionsBySearch(newVal);
        });
        
        Button searchButton = new Button("🔍 Search");
        searchButton.setStyle(createThemedPrimaryButtonStyle());
        searchButton.setOnAction(e -> filterTransactionsBySearch(advancedSearch.getText()));
        
        searchRow.getChildren().addAll(new Label("Search:"), advancedSearch, searchButton);
        
        // Filter row 1
        HBox filterRow1 = new HBox(20);
        filterRow1.setAlignment(Pos.CENTER_LEFT);
        
        // Transaction type filter
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("All Transactions", "Income Only", "Expenses Only");
        typeFilter.setValue("All Transactions");
        typeFilter.setPrefWidth(180);
        
        // Date range filter
        ComboBox<String> dateFilter = new ComboBox<>();
        dateFilter.getItems().addAll(
            "All Time", "Today", "This Week", "This Month", "This Year",
            "Last 30 Days", "Last 90 Days", "Custom Range"
        );
        dateFilter.setValue("All Time");
        dateFilter.setPrefWidth(150);
        
        // Category filter - DYNAMIC từ database
        ComboBox<String> categoryFilter = new ComboBox<>();
        List<String> allCategories = new ArrayList<>();
        allCategories.add("All Categories");
        allCategories.addAll(dbManager.getIncomeCategories());
        allCategories.addAll(dbManager.getOutcomeCategories());
        categoryFilter.getItems().addAll(allCategories);
        categoryFilter.setValue("All Categories");
        categoryFilter.setPrefWidth(150);
        
        filterRow1.getChildren().addAll(
            new Label("Type:"), typeFilter,
            new Label("Period:"), dateFilter,
            new Label("Category:"), categoryFilter
        );
        
        // Filter row 2 - Amount and sort
        HBox filterRow2 = new HBox(20);
        filterRow2.setAlignment(Pos.CENTER_LEFT);
        
        // Amount range
        TextField minAmount = new TextField();
        minAmount.setPromptText("Min amount");
        minAmount.setPrefWidth(120);
        
        TextField maxAmount = new TextField();
        maxAmount.setPromptText("Max amount");
        maxAmount.setPrefWidth(120);
        
        // Sort options
        ComboBox<String> sortFilter = new ComboBox<>();
        sortFilter.getItems().addAll(
            "Date (Newest First)", "Date (Oldest First)",
            "Amount (Highest First)", "Amount (Lowest First)",
            "Title (A-Z)", "Title (Z-A)",
            "Category (A-Z)"
        );
        sortFilter.setValue("Date (Newest First)");
        sortFilter.setPrefWidth(180);
        
        Button applyFiltersBtn = new Button("📊 Apply Filters");
        applyFiltersBtn.setStyle(createThemedPrimaryButtonStyle());
        applyFiltersBtn.setOnAction(e -> applyAllTransactionFilters(
            typeFilter.getValue(),
            dateFilter.getValue(), 
            categoryFilter.getValue(),
            minAmount.getText(),
            maxAmount.getText(),
            sortFilter.getValue(),
            advancedSearch.getText()
        ));
        
        Button clearFiltersBtn = new Button("🗑️ Clear All");
        clearFiltersBtn.setStyle(createThemedSecondaryButtonStyle());
        clearFiltersBtn.setOnAction(e -> clearAllTransactionFilters(
            typeFilter, dateFilter, categoryFilter, minAmount, maxAmount, sortFilter, advancedSearch
        ));
        
        filterRow2.getChildren().addAll(
            new Label("Amount:"), minAmount, new Label("to"), maxAmount,
            new Label("Sort:"), sortFilter,
            applyFiltersBtn, clearFiltersBtn
        );
        
        // Style all labels
        styleFilterLabels(filtersContainer);
        
        filtersContainer.getChildren().addAll(filtersTitle, searchRow, filterRow1, filterRow2);
        return filtersContainer;
    }

    private VBox createFilteredTransactionList() {
        VBox listContainer = new VBox(15);
        listContainer.setPadding(new Insets(20));
        listContainer.setStyle(
            "-fx-background-color: " + ThemeManager.toHexString(CARD_BG) + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);"
        );
        
        // Header
        HBox listHeader = new HBox();
        listHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label listTitle = new Label("💼 Transaction History");
        listTitle.setTextFill(TEXT_PRIMARY);
        listTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label summaryLabel = new Label("Loading transactions...");
        summaryLabel.setTextFill(TEXT_SECONDARY);
        summaryLabel.setFont(Font.font("Segoe UI", 12));
        summaryLabel.getStyleClass().add("summary-label");
        
        listHeader.getChildren().addAll(listTitle, spacer, summaryLabel);
        
        // Transaction items container
        VBox transactionItems = new VBox(8);
        transactionItems.setId("transactionItemsContainer"); // Để tìm sau này
        
        // Load initial data
        loadAllTransactions();
        
        ScrollPane scrollPane = new ScrollPane(transactionItems);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500); // Tăng chiều cao
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        listContainer.getChildren().addAll(listHeader, new Separator(), scrollPane);
        return listContainer;
    }

    private HBox createEnhancedTransactionItem(String title, double amount, LocalDate date, 
                                             boolean isIncome, String category, String description) {
        HBox item = new HBox(20);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(15, 20, 15, 20));
        item.setStyle(
            "-fx-background-color: " + ThemeManager.toRgbaString(TEXT_PRIMARY, 0.02) + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + ThemeManager.toRgbaString(TEXT_PRIMARY, 0.05) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;" +
            "-fx-cursor: hand;"
        );
        
        // Transaction type icon
        StackPane iconContainer = new StackPane();
        Circle iconBg = new Circle(22);
        iconBg.setFill(isIncome ? ACCENT_GREEN : ACCENT_PURPLE);
        iconBg.setEffect(new DropShadow(6, isIncome ? ACCENT_GREEN : ACCENT_PURPLE));
        
        Label icon = new Label(isIncome ? "💰" : getCategoryIcon(category));
        icon.setFont(Font.font(16));
        icon.setTextFill(Color.WHITE);
        
        iconContainer.getChildren().addAll(iconBg, icon);
        
        // Transaction details
        VBox details = new VBox(4);
        
        Label titleLabel = new Label(title);
        titleLabel.setTextFill(TEXT_PRIMARY);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 15));
        
        HBox metaRow = new HBox(15);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        
        Label categoryLabel = new Label("📂 " + category);
        categoryLabel.setTextFill(TEXT_MUTED);
        categoryLabel.setFont(Font.font("Segoe UI", 11));
        
        Label dateLabel = new Label("📅 " + date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dateLabel.setTextFill(TEXT_MUTED);
        dateLabel.setFont(Font.font("Segoe UI", 11));
        
        Label typeLabel = new Label(isIncome ? "📈 Income" : "📉 Expense");
        typeLabel.setTextFill(isIncome ? ACCENT_GREEN : ACCENT_PURPLE);
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 10));
        typeLabel.setStyle(
            "-fx-background-color: " + ThemeManager.toRgbaString(isIncome ? ACCENT_GREEN : ACCENT_PURPLE, 0.15) + ";" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 2 6 2 6;"
        );
        
        metaRow.getChildren().addAll(categoryLabel, dateLabel, typeLabel);
        
        details.getChildren().addAll(titleLabel, metaRow);
        
        // Add description if available
        if (description != null && !description.trim().isEmpty()) {
            Label descLabel = new Label("💬 " + description);
            descLabel.setTextFill(TEXT_SECONDARY);
            descLabel.setFont(Font.font("Segoe UI", 11));
            descLabel.setWrapText(true);
            details.getChildren().add(descLabel);
        }
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Amount
        VBox amountSection = new VBox(2);
        amountSection.setAlignment(Pos.CENTER_RIGHT);
        
        Label amountLabel = new Label((isIncome ? "+" : "-") + "$" + String.format("%.2f", amount));
        amountLabel.setTextFill(isIncome ? ACCENT_GREEN : ACCENT_PURPLE);
        amountLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        Label balanceImpact = new Label(isIncome ? "Balance +" : "Balance -");
        balanceImpact.setTextFill(TEXT_MUTED);
        balanceImpact.setFont(Font.font("Segoe UI", 10));
        
        amountSection.getChildren().addAll(amountLabel, balanceImpact);
        
        item.getChildren().addAll(iconContainer, details, spacer, amountSection);
        
        // Enhanced hover effect
        item.setOnMouseEntered(e -> {
            item.setStyle(item.getStyle().replace(
                ThemeManager.toRgbaString(TEXT_PRIMARY, 0.02),
                ThemeManager.toRgbaString(isIncome ? ACCENT_GREEN : ACCENT_PURPLE, 0.08)
            ));
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), item);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
        });
        
        item.setOnMouseExited(e -> {
            item.setStyle(item.getStyle().replace(
                ThemeManager.toRgbaString(isIncome ? ACCENT_GREEN : ACCENT_PURPLE, 0.08),
                ThemeManager.toRgbaString(TEXT_PRIMARY, 0.02)
            ));
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), item);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
        
        return item;
    }

    private String getCategoryIcon(String category) {
        switch (category.toLowerCase()) {
            case "food": return "🍔";
            case "transportation": return "🚗";
            case "housing": return "🏠";
            case "utilities": return "⚡";
            case "entertainment": return "🎬";
            case "healthcare": return "🏥";
            case "education": return "📚";
            case "shopping": return "🛒";
            case "travel": return "✈️";
            case "groceries": return "🛒";
            default: return "💳";
        }
    }

    private Color getInsightColor(AnalyticsService.FinancialInsight.InsightLevel level) {
        return switch (level) {
            case SUCCESS -> ACCENT_GREEN;
            case WARNING -> Color.web("#F59E0B");
            case ALERT -> ACCENT_PINK;
            default -> ACCENT_BLUE;
        };
    }

    private String getGradeFromScore(double score) {
        if (score >= 90) return "A+";
        else if (score >= 80) return "A";
        else if (score >= 70) return "B+";
        else if (score >= 60) return "B";
        else if (score >= 50) return "C";
        else return "D";
    }

    private String getHealthDescription(double score) {
        if (score >= 80) return "excellent! Keep up the great work.";
        else if (score >= 60) return "good. Some improvements can be made.";
        else if (score >= 40) return "fair. Consider reviewing your financial habits.";
        else return "needs attention. Let's work on improving it together.";
    }

    // Color utility methods - removed as we now use ThemeManager
    // private String toHexString(Color color) {
    //     return String.format("#%02X%02X%02X",
    //             (int)(color.getRed() * 255),
    //             (int)(color.getGreen() * 255),
    //             (int)(color.getBlue() * 255));
    // }

    private String toRgbaString(Color color, double alpha) {
        return String.format("rgba(%.0f, %.0f, %.0f, %.2f)",
                color.getRed() * 255, color.getGreen() * 255, color.getBlue() * 255, alpha);
    }

    // Color utility methods - removed as we now use ThemeManager
    // private Color toRgbaColor(Color color, double alpha) {
    //     return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    // }

    // Thêm method để refresh dashboard
    public void refreshDashboard() {
        Platform.runLater(() -> {
            loadDashboard();
        });
    }

    public static void refreshDashboardFromOtherPage() {
        if (instance != null) {
            instance.refreshDashboard();
        }
    }

    // Thêm các method bị thiếu
    private void addWindowEffects(Stage stage) {
        // Add window effects like shadow, blur, etc.
        // Comment out the problematic line to avoid runtime error
        // stage.initStyle(StageStyle.UNDECORATED);
    }

    private void updateActiveMenuItem(HBox activeItem) {
        // Update active menu item styling
        // This method will be implemented to highlight the active menu item
    }

    private void loadCardPage() {
        contentArea.getChildren().clear();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        try {
            // Create card manager and load page
            CardManager cardManager = new CardManager(dbManager);
            VBox cardContent = cardManager.createCardManagementPage();
            
            // Apply theme to card content
            applyThemeToCardContent(cardContent);
            
            scrollPane.setContent(cardContent);
            
        } catch (Exception e) {
            // Error fallback
            VBox errorContent = createCardPageError(e.getMessage());
            scrollPane.setContent(errorContent);
        }
        
        contentArea.getChildren().add(scrollPane);
    }

    private void applyThemeToCardContent(VBox cardContent) {
        // Update background color to match current theme
        cardContent.setStyle("-fx-background-color: " + ThemeManager.toHexString(currentTheme.getBgPrimary()) + ";");
        
        // Apply theme-aware styling to child elements
        updateCardContentTheme(cardContent);
    }

    private void updateCardContentTheme(VBox content) {
        for (javafx.scene.Node node : content.getChildren()) {
            if (node instanceof VBox) {
                VBox vbox = (VBox) node;
                updateCardContentTheme(vbox);
            } else if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                updateHBoxTheme(hbox);
            } else if (node instanceof Label) {
                Label label = (Label) node;
                updateLabelTheme(label);
            }
        }
    }

    private void updateHBoxTheme(HBox hbox) {
        for (javafx.scene.Node node : hbox.getChildren()) {
            if (node instanceof Label) {
                updateLabelTheme((Label) node);
            } else if (node instanceof VBox) {
                updateCardContentTheme((VBox) node);
            }
        }
    }

    private void updateLabelTheme(Label label) {
        // Update label colors based on current style
        String currentStyle = label.getStyle();
        if (currentStyle != null && !currentStyle.isEmpty()) {
            // Update text colors in existing styles
            if (currentStyle.contains("-fx-text-fill:")) {
                // Keep existing color logic but ensure visibility
                return;
            }
        }
        
        // Default theme-aware text color
        label.setTextFill(TEXT_PRIMARY);
    }

    private VBox createCardPageError(String errorMessage) {
        VBox errorContent = new VBox(25);
        errorContent.setPadding(new Insets(50));
        errorContent.setAlignment(Pos.CENTER);
        errorContent.setStyle("-fx-background-color: " + ThemeManager.toHexString(currentTheme.getBgPrimary()) + ";");
        
        Label errorIcon = new Label("💳");
        errorIcon.setFont(Font.font(48));
        
        Label errorTitle = new Label("Card Management Error");
        errorTitle.setTextFill(currentTheme.getError());
        errorTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        
        Label errorDesc = new Label("Failed to load card management: " + errorMessage);
        errorDesc.setTextFill(TEXT_SECONDARY);
        errorDesc.setFont(Font.font("Segoe UI", 14));
        errorDesc.setWrapText(true);
        errorDesc.setAlignment(Pos.CENTER);
        
        Button retryBtn = new Button("🔄 Try Again");
        retryBtn.setStyle(createThemedPrimaryButtonStyle());
        retryBtn.setOnAction(e -> loadCardPage());
        
        Button fallbackBtn = new Button("📊 Go to Dashboard");
        fallbackBtn.setStyle(createThemedSecondaryButtonStyle());
        fallbackBtn.setOnAction(e -> loadDashboard());
        
        HBox buttonRow = new HBox(15);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.getChildren().addAll(retryBtn, fallbackBtn);
        
        errorContent.getChildren().addAll(errorIcon, errorTitle, errorDesc, buttonRow);
        return errorContent;
    }

    private void animateCardEntrance(VBox card, int index) {
        card.setOpacity(0);
        card.setTranslateY(20);
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(index * 100));
        
        TranslateTransition translate = new TranslateTransition(Duration.millis(300), card);
        translate.setFromY(20);
        translate.setToY(0);
        translate.setDelay(Duration.millis(index * 100));
        
        ParallelTransition parallel = new ParallelTransition(fade, translate);
        parallel.play();
    }

    private LinearGradient getCategoryGradient(String category) {
        Color color1, color2;
        
        switch (category.toLowerCase()) {
            case "food":
                color1 = Color.web("#FF6B6B");
                color2 = Color.web("#FF8E8E");
                break;
            case "transportation":
                color1 = Color.web("#4ECDC4");
                color2 = Color.web("#6EDDD6");
                break;
            case "entertainment":
                color1 = Color.web("#45B7D1");
                color2 = Color.web("#67C7DB");
                break;
            case "shopping":
                color1 = Color.web("#96CEB4");
                color2 = Color.web("#A8D8C4");
                break;
            default:
                color1 = ACCENT_PURPLE;
                color2 = ACCENT_PINK;
                break;
        }
        
        return new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, color1),
            new Stop(1, color2)
        );
    }

    // Thêm method loadSettingsPage
    private void loadSettingsPage() {
        contentArea.getChildren().clear();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        VBox settingsContent = settingsPage.createSettingsPageContent();
        scrollPane.setContent(settingsContent);
        contentArea.getChildren().add(scrollPane);
    }

    @Override
    public void onThemeChanged(ThemeManager.Theme newTheme) {
        Platform.runLater(() -> {
            updateColorsFromTheme();
            refreshAllUIWithTheme();
        });
    }

    private void updateColorsFromTheme() {
        currentTheme = themeManager.getCurrentTheme();
        
        BG_PRIMARY = currentTheme.getBgPrimary();
        BG_SECONDARY = currentTheme.getBgSecondary();
        CARD_BG = currentTheme.getCardBg();
        CARD_HOVER = currentTheme.getCardHover();
        
        ACCENT_PURPLE = currentTheme.getAccentPurple();
        ACCENT_PINK = currentTheme.getAccentPink();
        ACCENT_BLUE = currentTheme.getAccentBlue();
        ACCENT_GREEN = currentTheme.getAccentGreen();
        ACCENT_CYAN = currentTheme.getAccentCyan();
        
        TEXT_PRIMARY = currentTheme.getTextPrimary();
        TEXT_SECONDARY = currentTheme.getTextSecondary();
        TEXT_MUTED = currentTheme.getTextMuted();
    }

    private void refreshAllUIWithTheme() {
        // Update UI with new theme
        if (mainRoot != null) {
            updateMainRootStyle();
            updateContentAreaStyle();
            
            // Update sidebar
            VBox sidebar = (VBox) mainRoot.getLeft();
            if (sidebar != null) {
                updateSidebarTheme(sidebar);
            }
            
            // Refresh current page
            refreshCurrentPage();
            
            // Update theme toggle button
            updateThemeToggleButton();
        }
    }

    private void updateMainRootStyle() {
        if (mainRoot != null) {
            mainRoot.setStyle("-fx-background-color: " + ThemeManager.toHexString(BG_PRIMARY) + ";");
        }
    }

    private void updateContentAreaStyle() {
        if (contentArea != null) {
            contentArea.setStyle("-fx-background-color: " + ThemeManager.toHexString(BG_PRIMARY) + ";");
        }
    }

    private void updateSidebarTheme(VBox sidebar) {
        // Update sidebar background
        sidebar.setStyle(
            "-fx-background-color: " + ThemeManager.toHexString(BG_SECONDARY) + ";" +
            "-fx-padding: 25;" +
            "-fx-effect: dropshadow(gaussian, " + ThemeManager.toRgbaString(currentTheme.getShadowColor(), 0.3) + ", 20, 0, 5, 0);"
        );
        
        // Update menu items
        if (sidebar.getChildren().size() > 1) {
            VBox navItems = (VBox) sidebar.getChildren().get(1);
            for (javafx.scene.Node node : navItems.getChildren()) {
                if (node instanceof HBox) {
                    HBox menuItem = (HBox) node;
                    updateMenuItemTheme(menuItem);
                }
            }
        }
    }

    private void updateMenuItemTheme(HBox menuItem) {
        // Check if this is the active menu item
        boolean isActive = menuItem.getStyle().contains("linear-gradient");
        
        if (isActive) {
            menuItem.setStyle(
                "-fx-background-color: linear-gradient(to right, " + 
                ThemeManager.toHexString(ACCENT_PURPLE) + ", " + 
                ThemeManager.toHexString(ACCENT_PINK) + ");" +
                "-fx-background-radius: 12;"
            );
        } else {
            // Update hover effects
            menuItem.setOnMouseEntered(e -> {
                menuItem.setStyle("-fx-background-color: " + ThemeManager.toHexString(CARD_BG) + "; -fx-background-radius: 12;");
                if (menuItem.getChildren().size() > 1) {
                    Label textLabel = (Label) menuItem.getChildren().get(1);
                    textLabel.setTextFill(TEXT_PRIMARY);
                }
            });
            
            menuItem.setOnMouseExited(e -> {
                menuItem.setStyle("-fx-background-color: transparent;");
                if (menuItem.getChildren().size() > 1) {
                    Label textLabel = (Label) menuItem.getChildren().get(1);
                    textLabel.setTextFill(TEXT_SECONDARY);
                }
            });
        }
        
        // Update text color
        if (menuItem.getChildren().size() > 1) {
            Label textLabel = (Label) menuItem.getChildren().get(1);
            textLabel.setTextFill(isActive ? TEXT_PRIMARY : TEXT_SECONDARY);
        }
    }

    private void refreshCurrentPage() {
        // Re-load current page to apply new theme
        // This is a simplified approach - ideally each page would have its own theme update method
        loadDashboard();
    }

    private void updateSceneTheme(Scene scene) {
        // Apply theme-specific CSS classes
        if (currentTheme instanceof ThemeManager.DarkTheme) {
            scene.getRoot().getStyleClass().add("dark-theme");
            scene.getRoot().getStyleClass().remove("light-theme");
        } else {
            scene.getRoot().getStyleClass().add("light-theme");
            scene.getRoot().getStyleClass().remove("dark-theme");
        }
    }

    private void setupAutoBackup(Stage primaryStage) {
        try {
            BackupManager backupManager = new BackupManager(dbManager, primaryStage);
            backupManager.setupAutoBackup();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to setup auto backup", e);
        }
    }

    private Color getRiskColor(String riskLevel) {
        return switch (riskLevel.toLowerCase()) {
            case "low" -> ACCENT_GREEN;
            case "medium" -> ACCENT_PINK;
            case "high" -> ACCENT_PURPLE;
            default -> TEXT_MUTED;
        };
    }

    // Thêm class này ở cuối file, trước dấu đóng ngoặc cuối
    private static class TransactionItem {
        private String title;
        private double amount;
        private LocalDate date;
        private boolean isIncome;
        private String category;
        private String description;
        private LocalDateTime createdAt;
        
        public TransactionItem(String title, double amount, LocalDate date, boolean isIncome, 
                              String category, String description, LocalDateTime createdAt) {
            this.title = title;
            this.amount = amount;
            this.date = date;
            this.isIncome = isIncome;
            this.category = category;
            this.description = description;
            this.createdAt = createdAt;
        }
        
        // Getters
        public String getTitle() { return title; }
        public double getAmount() { return amount; }
        public LocalDate getDate() { return date; }
        public boolean isIncome() { return isIncome; }
        public String getCategory() { return category; }
        public String getDescription() { return description; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    private String createEnhancedTextFieldStyle() {
        return "-fx-background-color: " + ThemeManager.toHexString(BG_SECONDARY) + ";" +
               "-fx-text-fill: " + ThemeManager.toHexString(TEXT_PRIMARY) + ";" +
               "-fx-border-color: " + ThemeManager.toRgbaString(ACCENT_PURPLE, 0.3) + ";" +
               "-fx-border-radius: 6px;" +
               "-fx-background-radius: 6px;" +
               "-fx-padding: 8 12 8 12;";
    }

    private void styleFilterLabels(VBox container) {
        container.getChildren().forEach(node -> {
            if (node instanceof HBox) {
                ((HBox) node).getChildren().forEach(child -> {
                    if (child instanceof Label) {
                        ((Label) child).setTextFill(TEXT_SECONDARY);
                        ((Label) child).setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
                    }
                    if (child instanceof ComboBox || child instanceof TextField) {
                        child.setStyle(
                            "-fx-background-color: " + ThemeManager.toHexString(BG_SECONDARY) + ";" +
                            "-fx-text-fill: " + ThemeManager.toHexString(TEXT_PRIMARY) + ";" +
                            "-fx-border-color: " + ThemeManager.toRgbaString(ACCENT_PURPLE, 0.3) + ";" +
                            "-fx-border-radius: 6px;" +
                            "-fx-background-radius: 6px;"
                        );
                    }
                });
            }
        });
    }

    // Thêm method tìm kiếm realtime
    private void filterTransactionsBySearch(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadAllTransactions(); // Load lại tất cả
            return;
        }
        
        // Apply search filter
        applyAllTransactionFilters("All Transactions", "All Time", "All Categories", 
                                  "", "", "Date (Newest First)", searchText);
    }

    // Thêm method áp dụng tất cả filter
    private void applyAllTransactionFilters(String type, String dateRange, String category,
                                           String minAmount, String maxAmount, String sort, String search) {
        
        List<TransactionItem> allTransactions = getAllTransactions();
        List<TransactionItem> filteredTransactions = new ArrayList<>(allTransactions);
        
        // 1. Text search filter
        if (search != null && !search.trim().isEmpty()) {
            filteredTransactions = filteredTransactions.stream()
                .filter(t -> matchesTransactionSearch(t, search.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        // 2. Type filter
        if (!"All Transactions".equals(type)) {
            boolean showIncome = "Income Only".equals(type);
            filteredTransactions = filteredTransactions.stream()
                .filter(t -> t.isIncome() == showIncome)
                .collect(Collectors.toList());
        }
        
        // 3. Category filter
        if (!"All Categories".equals(category)) {
            filteredTransactions = filteredTransactions.stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());
        }
        
        // 4. Amount filter
        filteredTransactions = applyTransactionAmountFilter(filteredTransactions, minAmount, maxAmount);
        
        // 5. Date filter
        filteredTransactions = applyTransactionDateFilter(filteredTransactions, dateRange);
        
        // 6. Sort
        filteredTransactions = applyTransactionSort(filteredTransactions, sort);
        
        // Update UI
        updateTransactionDisplay(filteredTransactions);
    }

    private boolean matchesTransactionSearch(TransactionItem transaction, String search) {
        // Tìm kiếm trong tất cả field
        String[] searchTerms = search.split("\\s+");
        
        for (String term : searchTerms) {
            boolean found = false;
            
            // Tìm trong title
            if (transaction.getTitle() != null && 
                transaction.getTitle().toLowerCase().contains(term)) {
                found = true;
            }
            
            // Tìm trong category
            if (transaction.getCategory() != null && 
                transaction.getCategory().toLowerCase().contains(term)) {
                found = true;
            }
            
            // Tìm trong description
            if (transaction.getDescription() != null && 
                transaction.getDescription().toLowerCase().contains(term)) {
                found = true;
            }
            
            // Tìm theo amount
            try {
                double searchAmount = Double.parseDouble(term);
                if (Math.abs(transaction.getAmount() - searchAmount) < 0.01) {
                    found = true;
                }
            } catch (NumberFormatException e) {
                // Không phải số
            }
            
            // Tìm theo date
            if (transaction.getDate().toString().toLowerCase().contains(term)) {
                found = true;
            }
            
            if (!found) {
                return false; // Phải tìm thấy tất cả term
            }
        }
        
        return true;
    }

    private List<TransactionItem> getAllTransactions() {
        List<TransactionItem> allTransactions = new ArrayList<>();
        
        // Load Income
        List<IncomeRecord> incomes = dbManager.getIncomeRecords(200); // Tăng limit
        for (IncomeRecord income : incomes) {
            allTransactions.add(new TransactionItem(
                income.getSource(),
                income.getAmount(),
                income.getDate(),
                true,
                income.getCategory(),
                income.getDescription(),
                income.getCreatedAt()
            ));
        }
        
        // Load Outcome
        List<OutcomeRecord> outcomes = dbManager.getOutcomeRecords(200); // Tăng limit
        for (OutcomeRecord outcome : outcomes) {
            allTransactions.add(new TransactionItem(
                outcome.getTitle(),
                outcome.getAmount(),
                outcome.getDate(),
                false,
                outcome.getCategory(),
                outcome.getDescription(),
                outcome.getCreatedAt()
            ));
        }
        
        return allTransactions;
    }

    private void clearAllTransactionFilters(ComboBox<String> typeFilter, ComboBox<String> dateFilter,
                                           ComboBox<String> categoryFilter, TextField minAmount,
                                           TextField maxAmount, ComboBox<String> sortFilter,
                                           TextField searchField) {
        typeFilter.setValue("All Transactions");
        dateFilter.setValue("All Time");
        categoryFilter.setValue("All Categories");
        minAmount.clear();
        maxAmount.clear();
        sortFilter.setValue("Date (Newest First)");
        searchField.clear();
        
        loadAllTransactions();
    }

    private List<TransactionItem> applyTransactionAmountFilter(List<TransactionItem> transactions, String minAmount, String maxAmount) {
        try {
            double min = minAmount.isEmpty() ? 0 : Double.parseDouble(minAmount);
            double max = maxAmount.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxAmount);
            
            return transactions.stream()
                .filter(t -> t.getAmount() >= min && t.getAmount() <= max)
                .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return transactions;
        }
    }

    private List<TransactionItem> applyTransactionDateFilter(List<TransactionItem> transactions, String dateRange) {
        LocalDate now = LocalDate.now();
        
        return transactions.stream().filter(t -> {
            switch (dateRange) {
                case "Today":
                    return t.getDate().equals(now);
                case "This Week":
                    LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);
                    LocalDate weekEnd = weekStart.plusDays(6);
                    return !t.getDate().isBefore(weekStart) && !t.getDate().isAfter(weekEnd);
                case "This Month":
                    return t.getDate().getYear() == now.getYear() && 
                           t.getDate().getMonth() == now.getMonth();
                case "This Year":
                    return t.getDate().getYear() == now.getYear();
                case "Last 30 Days":
                    return t.getDate().isAfter(now.minusDays(30));
                case "Last 90 Days":
                    return t.getDate().isAfter(now.minusDays(90));
                default: // "All Time"
                    return true;
            }
        }).collect(Collectors.toList());
    }

    private List<TransactionItem> applyTransactionSort(List<TransactionItem> transactions, String sort) {
        return transactions.stream().sorted((t1, t2) -> {
            switch (sort) {
                case "Date (Oldest First)":
                    return t1.getDate().compareTo(t2.getDate());
                case "Amount (Highest First)":
                    return Double.compare(t2.getAmount(), t1.getAmount());
                case "Amount (Lowest First)":
                    return Double.compare(t1.getAmount(), t2.getAmount());
                case "Title (A-Z)":
                    return t1.getTitle().compareToIgnoreCase(t2.getTitle());
                case "Title (Z-A)":
                    return t2.getTitle().compareToIgnoreCase(t1.getTitle());
                case "Category (A-Z)":
                    return t1.getCategory().compareToIgnoreCase(t2.getCategory());
                default: // "Date (Newest First)"
                    return t2.getDate().compareTo(t1.getDate());
            }
        }).collect(Collectors.toList());
    }

    private void loadAllTransactions() {
        // Implementation để load và hiển thị tất cả transactions
        List<TransactionItem> allTransactions = getAllTransactions();
        
        // Sort theo ngày gần nhất
        allTransactions.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));
        
        updateTransactionDisplay(allTransactions);
    }

    private void updateTransactionDisplay(List<TransactionItem> transactions) {
        Platform.runLater(() -> {
            VBox container = (VBox) contentArea.lookup("#transactionItemsContainer");
            if (container != null) {
                container.getChildren().clear();
                
                for (TransactionItem transaction : transactions) {
                    HBox item = createEnhancedTransactionItem(
                        transaction.getTitle(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        transaction.isIncome(),
                        transaction.getCategory(),
                        transaction.getDescription()
                    );
                    container.getChildren().add(item);
                }
                
                // Update summary
                updateTransactionSummary(transactions.size());
            }
        });
    }

    private void updateTransactionSummary(int count) {
        Platform.runLater(() -> {
            // Tìm label summary trong content area
            for (javafx.scene.Node node : contentArea.getChildren()) {
                if (node instanceof VBox) {
                    VBox vbox = (VBox) node;
                    for (javafx.scene.Node child : vbox.getChildren()) {
                        if (child instanceof HBox) {
                            HBox hbox = (HBox) child;
                            for (javafx.scene.Node grandChild : hbox.getChildren()) {
                                if (grandChild instanceof Label && 
                                    grandChild.getStyleClass().contains("summary-label")) {
                                    ((Label) grandChild).setText("Showing " + count + " transactions");
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        });
    }
} 