package com.cashflow;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.List;
import javafx.scene.control.ScrollPane;

public class NavigationManager {
    private BorderPane root;
    private VBox sidebar;
    private DatabaseManager dbManager;

    public NavigationManager(BorderPane root, VBox sidebar) {
        this.root = root;
        this.sidebar = sidebar;
        this.dbManager = new DatabaseManager();
        setupNavigation();
    }

    private void setupNavigation() {
        // Get menu items from sidebar
        VBox menuItems = (VBox) sidebar.getChildren().get(1);
        
        // Add click handlers to menu items
        for (int i = 0; i < menuItems.getChildren().size(); i++) {
            HBox menuItem = (HBox) menuItems.getChildren().get(i);
            final int index = i;
            
            menuItem.setOnMouseClicked(e -> {
                switch (index) {
                    case 0: // Dashboard
                        showDashboard();
                        break;
                    case 1: // Income
                        showIncomePage();
                        break;
                    case 2: // Outcome
                        showOutcomePage();
                        break;
                    case 3: // Analytics
                        showAnalyticsPage();
                        break;
                    case 4: // Transaction
                        showTransactionPage();
                        break;
                    case 5: // Card
                        showCardPage();
                        break;
                }
                
                // Update active state
                updateActiveMenuItem(index);
            });
        }
    }

    private void updateActiveMenuItem(int activeIndex) {
        VBox menuItems = (VBox) sidebar.getChildren().get(1);
        
        for (int i = 0; i < menuItems.getChildren().size(); i++) {
            HBox menuItem = (HBox) menuItems.getChildren().get(i);
            boolean isActive = (i == activeIndex);
            
            // Update styling
            if (isActive) {
                menuItem.setStyle("-fx-background-color: #4ECDC4; -fx-background-radius: 10px;");
                Label iconLabel = (Label) menuItem.getChildren().get(0);
                Label textLabel = (Label) menuItem.getChildren().get(1);
                iconLabel.setTextFill(Color.WHITE);
                textLabel.setTextFill(Color.WHITE);
            } else {
                menuItem.setStyle("-fx-background-radius: 10px; -fx-cursor: hand;");
                Label iconLabel = (Label) menuItem.getChildren().get(0);
                Label textLabel = (Label) menuItem.getChildren().get(1);
                iconLabel.setTextFill(Color.web("#8892b0"));
                textLabel.setTextFill(Color.web("#8892b0"));
            }
        }
    }

    private void showDashboard() {
        // Create dashboard content
        ScrollPane scrollPane = new ScrollPane();
        VBox mainContent = createDashboardContent();
        scrollPane.setContent(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0f0f23; -fx-background-color: #0f0f23;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setCenter(scrollPane);
    }

    private void showIncomePage() {
        // Create income page content
        ScrollPane scrollPane = new ScrollPane();
        VBox mainContent = createIncomePageContent();
        scrollPane.setContent(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0f0f23; -fx-background-color: #0f0f23;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setCenter(scrollPane);
    }

    private void showOutcomePage() {
        // Create outcome page content
        ScrollPane scrollPane = new ScrollPane();
        VBox mainContent = createOutcomePageContent();
        scrollPane.setContent(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0f0f23; -fx-background-color: #0f0f23;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setCenter(scrollPane);
    }

    private void showAnalyticsPage() {
        // Create analytics page content
        ScrollPane scrollPane = new ScrollPane();
        VBox mainContent = createAnalyticsPageContent();
        scrollPane.setContent(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0f0f23; -fx-background-color: #0f0f23;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setCenter(scrollPane);
    }

    private void showTransactionPage() {
        // Create transaction page content
        ScrollPane scrollPane = new ScrollPane();
        VBox mainContent = createTransactionPageContent();
        scrollPane.setContent(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0f0f23; -fx-background-color: #0f0f23;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setCenter(scrollPane);
    }

    private void showCardPage() {
        // Create card page content
        ScrollPane scrollPane = new ScrollPane();
        VBox mainContent = createCardPageContent();
        scrollPane.setContent(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0f0f23; -fx-background-color: #0f0f23;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setCenter(scrollPane);
    }

    private VBox createDashboardContent() {
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30, 30, 30, 30));

        // Get real data from database
        DashboardStats stats = dbManager.getDashboardStats();

        // Header
        HBox header = createSimpleHeader("Dashboard");
        
        // Top row với real data
        HBox topRow = createBorderlessStatsSectionWithData(stats);
        
        // Bottom row - Expenses chart với real data
        VBox expensesRow = createSeamlessExpensesChartWithData(stats);

        // Bottom section - Transactions + Spending với real data
        HBox bottomSection = createBottomSectionWithData();

        mainContent.getChildren().addAll(header, topRow, expensesRow, bottomSection);
        return mainContent;
    }

    private VBox createIncomePageContent() {
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30, 30, 30, 30));

        // Header
        HBox header = createSimpleHeader("Income Management");
        
        // Create income page content
        VBox incomeContent = new VBox(20);
        incomeContent.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 20px;");
        incomeContent.setPadding(new Insets(30));
        
        Label title = new Label("Income Records");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        // Get income records from database
        VBox recordsList = new VBox(10);
        for (IncomeRecord record : dbManager.getIncomeRecords()) {
            HBox recordItem = createIncomeRecordItem(record);
            recordsList.getChildren().add(recordItem);
        }
        
        incomeContent.getChildren().addAll(title, recordsList);
        mainContent.getChildren().addAll(header, incomeContent);
        
        return mainContent;
    }

    private VBox createOutcomePageContent() {
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30, 30, 30, 30));

        // Header
        HBox header = createSimpleHeader("Outcome Management");
        
        // Create outcome page content
        VBox outcomeContent = new VBox(20);
        outcomeContent.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 20px;");
        outcomeContent.setPadding(new Insets(30));
        
        Label title = new Label("Outcome Records");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        // Get outcome records from database
        VBox recordsList = new VBox(10);
        for (OutcomeRecord record : dbManager.getOutcomeRecords()) {
            HBox recordItem = createOutcomeRecordItem(record);
            recordsList.getChildren().add(recordItem);
        }
        
        outcomeContent.getChildren().addAll(title, recordsList);
        mainContent.getChildren().addAll(header, outcomeContent);
        
        return mainContent;
    }

    private VBox createAnalyticsPageContent() {
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30, 30, 30, 30));

        // Header
        HBox header = createSimpleHeader("Analytics");
        
        // Create analytics content
        VBox analyticsContent = new VBox(20);
        analyticsContent.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 20px;");
        analyticsContent.setPadding(new Insets(30));
        
        Label title = new Label("Financial Analytics");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        Label comingSoon = new Label("Analytics features coming soon...");
        comingSoon.setTextFill(Color.web("#8892b0"));
        comingSoon.setFont(Font.font("System", FontWeight.NORMAL, 16));
        
        analyticsContent.getChildren().addAll(title, comingSoon);
        mainContent.getChildren().addAll(header, analyticsContent);
        
        return mainContent;
    }

    private VBox createTransactionPageContent() {
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30, 30, 30, 30));

        // Header
        HBox header = createSimpleHeader("Transactions");
        
        // Create transaction content
        VBox transactionContent = new VBox(20);
        transactionContent.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 20px;");
        transactionContent.setPadding(new Insets(30));
        
        Label title = new Label("Transaction History");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        Label comingSoon = new Label("Transaction management coming soon...");
        comingSoon.setTextFill(Color.web("#8892b0"));
        comingSoon.setFont(Font.font("System", FontWeight.NORMAL, 16));
        
        transactionContent.getChildren().addAll(title, comingSoon);
        mainContent.getChildren().addAll(header, transactionContent);
        
        return mainContent;
    }

    private VBox createCardPageContent() {
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30, 30, 30, 30));

        // Header
        HBox header = createSimpleHeader("Cards");
        
        // Create card content
        VBox cardContent = new VBox(20);
        cardContent.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 20px;");
        cardContent.setPadding(new Insets(30));
        
        Label title = new Label("Payment Cards");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        Label comingSoon = new Label("Card management coming soon...");
        comingSoon.setTextFill(Color.web("#8892b0"));
        comingSoon.setFont(Font.font("System", FontWeight.NORMAL, 16));
        
        cardContent.getChildren().addAll(title, comingSoon);
        mainContent.getChildren().addAll(header, cardContent);
        
        return mainContent;
    }

    private HBox createSimpleHeader(String titleText) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label(titleText);
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Simple user info
        HBox userInfo = new HBox(15);
        userInfo.setAlignment(Pos.CENTER_RIGHT);
        
        Label bellIcon = new Label("🔔");
        bellIcon.setFont(Font.font(16));
        
        Label greeting = new Label("Hi, Ferra");
        greeting.setTextFill(Color.WHITE);
        greeting.setFont(Font.font("System", FontWeight.NORMAL, 14));
        
        Circle avatar = new Circle(22);
        avatar.setFill(Color.web("#4ECDC4"));
        
        userInfo.getChildren().addAll(bellIcon, greeting, avatar);
        header.getChildren().addAll(title, spacer, userInfo);
        
        return header;
    }

    private HBox createIncomeRecordItem(IncomeRecord record) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 15, 12, 15));
        item.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 10px;");

        Circle iconBg = new Circle(20);
        iconBg.setFill(Color.web("#4ECDC4"));
        
        Label iconLabel = new Label("💰");
        iconLabel.setFont(Font.font(14));
        iconLabel.setTextFill(Color.WHITE);
        
        VBox details = new VBox(3);
        Label sourceLabel = new Label(record.getSource());
        sourceLabel.setTextFill(Color.WHITE);
        sourceLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        
        Label dateLabel = new Label(record.getDate().toString());
        dateLabel.setTextFill(Color.web("#8892b0"));
        dateLabel.setFont(Font.font("System", FontWeight.NORMAL, 11));
        
        details.getChildren().addAll(sourceLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountLabel = new Label("$" + String.format("%.0f", record.getAmount()));
        amountLabel.setTextFill(Color.web("#4ECDC4"));
        amountLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        item.getChildren().addAll(iconBg, iconLabel, details, spacer, amountLabel);
        return item;
    }

    private HBox createOutcomeRecordItem(OutcomeRecord record) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 15, 12, 15));
        item.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 10px;");

        Circle iconBg = new Circle(20);
        iconBg.setFill(Color.web("#FF6B6B"));
        
        Label iconLabel = new Label("📤");
        iconLabel.setFont(Font.font(14));
        iconLabel.setTextFill(Color.WHITE);
        
        VBox details = new VBox(3);
        Label titleLabel = new Label(record.getTitle());
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        
        Label dateLabel = new Label(record.getDate().toString());
        dateLabel.setTextFill(Color.web("#8892b0"));
        dateLabel.setFont(Font.font("System", FontWeight.NORMAL, 11));
        
        details.getChildren().addAll(titleLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountLabel = new Label("-$" + String.format("%.0f", record.getAmount()));
        amountLabel.setTextFill(Color.web("#FF6B6B"));
        amountLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        item.getChildren().addAll(iconBg, iconLabel, details, spacer, amountLabel);
        return item;
    }

    // These methods need to be implemented to match the CashflowApp interface
    private HBox createBorderlessStatsSectionWithData(DashboardStats stats) {
        // This would need to be implemented to match CashflowApp
        HBox section = new HBox();
        Label label = new Label("Stats Section - Implementation needed");
        label.setTextFill(Color.WHITE);
        section.getChildren().add(label);
        return section;
    }

    private VBox createSeamlessExpensesChartWithData(DashboardStats stats) {
        // This would need to be implemented to match CashflowApp
        VBox section = new VBox();
        Label label = new Label("Expenses Chart - Implementation needed");
        label.setTextFill(Color.WHITE);
        section.getChildren().add(label);
        return section;
    }

    private HBox createBottomSectionWithData() {
        // This would need to be implemented to match CashflowApp
        HBox section = new HBox();
        Label label = new Label("Bottom Section - Implementation needed");
        label.setTextFill(Color.WHITE);
        section.getChildren().add(label);
        return section;
    }
} 