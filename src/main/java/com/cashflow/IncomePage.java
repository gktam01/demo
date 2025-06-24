package com.cashflow;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class IncomePage {
    
    // Colors matching dashboard
    private final Color BACKGROUND_COLOR = Color.web("#0f0f23");
    private final Color CARD_COLOR = Color.web("#1e2749");
    private final Color ACCENT_COLOR = Color.web("#4ECDC4");
    private final Color INCOME_COLOR = Color.web("#4CAF50");
    private final Color TEXT_PRIMARY = Color.WHITE;
    private final Color TEXT_SECONDARY = Color.web("#8892b0");
    private final Color TEXT_MUTED = Color.web("#64748b");

    private DatabaseManager dbManager;

    public IncomePage() {
        this.dbManager = new DatabaseManager();
    }

    public VBox createIncomePageContent() {
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30));
        mainContent.setStyle("-fx-background-color: #0f0f23;");

        // Header với breadcrumb
        HBox header = createIncomeHeader();
        
        // Stats overview
        HBox statsOverview = createIncomeStatsOverview();
        
        // Main content area
        HBox mainArea = new HBox(25);
        mainArea.setAlignment(Pos.TOP_LEFT);
        
        // Left side - Add income form
        VBox addIncomeSection = createAddIncomeSection();
        
        // Right side - Income list và charts
        VBox incomeListSection = createIncomeListSection();
        
        mainArea.getChildren().addAll(addIncomeSection, incomeListSection);

        mainContent.getChildren().addAll(header, statsOverview, mainArea);
        return mainContent;
    }

    private HBox createIncomeHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Breadcrumb
        Label dashboardLink = new Label("Dashboard");
        dashboardLink.setTextFill(TEXT_SECONDARY);
        dashboardLink.setFont(Font.font("System", FontWeight.NORMAL, 14));
        dashboardLink.setStyle("-fx-cursor: hand;");
        dashboardLink.setOnMouseClicked(e -> navigateToDashboard());
        
        Label separator = new Label("/");
        separator.setTextFill(TEXT_MUTED);
        separator.setFont(Font.font("System", FontWeight.NORMAL, 14));
        
        Label currentPage = new Label("Income");
        currentPage.setTextFill(TEXT_PRIMARY);
        currentPage.setFont(Font.font("System", FontWeight.BOLD, 28));
        
        header.getChildren().addAll(dashboardLink, separator, currentPage);
        return header;
    }

    private HBox createIncomeStatsOverview() {
        HBox statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER_LEFT);
        
        // Tổng thu nhập tháng này
        VBox monthlyIncome = createStatCard("💰", "This Month", 
            "$" + dbManager.getMonthlyIncome(LocalDate.now()), "+12.5%", true);
        
        // Tổng thu nhập năm này
        VBox yearlyIncome = createStatCard("📈", "This Year", 
            "$" + dbManager.getYearlyIncome(LocalDate.now()), "+8.2%", true);
        
        // Số lượng income records
        VBox incomeCount = createStatCard("📊", "Total Records", 
            String.valueOf(dbManager.getIncomeRecordsCount()), "", false);
        
        // Average income per month
        VBox avgIncome = createStatCard("💵", "Avg/Month", 
            "$" + dbManager.getAverageMonthlyIncome(), "", false);
        
        statsContainer.getChildren().addAll(monthlyIncome, yearlyIncome, incomeCount, avgIncome);
        return statsContainer;
    }

    private VBox createStatCard(String icon, String title, String value, String change, boolean showChange) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setPrefWidth(280);
        card.setStyle(
            "-fx-background-color: #1e2749;" +
            "-fx-background-radius: 15px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 3);"
        );

        // Icon và title
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(18));
        
        Label titleLabel = new Label(title);
        titleLabel.setTextFill(TEXT_SECONDARY);
        titleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        
        header.getChildren().addAll(iconLabel, titleLabel);

        // Value và change
        HBox valueRow = new HBox(10);
        valueRow.setAlignment(Pos.CENTER_LEFT);
        
        Label valueLabel = new Label(value);
        valueLabel.setTextFill(TEXT_PRIMARY);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        if (showChange && !change.isEmpty()) {
            Label changeLabel = new Label(change);
            changeLabel.setTextFill(INCOME_COLOR);
            changeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            changeLabel.setStyle("-fx-background-color: rgba(76, 175, 80, 0.15); -fx-background-radius: 8px; -fx-padding: 2 6 2 6;");
            valueRow.getChildren().addAll(valueLabel, changeLabel);
        } else {
            valueRow.getChildren().add(valueLabel);
        }

        card.getChildren().addAll(header, valueRow);
        return card;
    }

    private VBox createAddIncomeSection() {
        VBox section = new VBox(20);
        section.setPrefWidth(400);
        
        // Title
        Label title = new Label("Add New Income");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        // Form card
        VBox formCard = new VBox(20);
        formCard.setPadding(new Insets(25));
        formCard.setStyle(
            "-fx-background-color: #1e2749;" +
            "-fx-background-radius: 15px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 3);"
        );

        // Amount field
        VBox amountField = createFormField("Amount", "Enter amount", "💰");
        
        // Source field
        VBox sourceField = createFormField("Source", "e.g., Salary, Freelance", "🏢");
        
        // Category dropdown
        VBox categoryField = createCategoryDropdown();
        
        // Date field
        VBox dateField = createDateField();
        
        // Description field
        VBox descField = createFormField("Description", "Optional description", "📝");

        // Add button
        Button addButton = new Button("Add Income");
        addButton.setPrefWidth(Double.MAX_VALUE);
        addButton.setPrefHeight(45);
        addButton.setStyle(
            "-fx-background-color: #4CAF50;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(76, 175, 80, 0.3), 10, 0, 0, 2);"
        );
        addButton.setOnMouseEntered(e -> addButton.setStyle(addButton.getStyle().replace("#4CAF50", "#45A049")));
        addButton.setOnMouseExited(e -> addButton.setStyle(addButton.getStyle().replace("#45A049", "#4CAF50")));
        addButton.setOnAction(e -> handleAddIncome());

        formCard.getChildren().addAll(amountField, sourceField, categoryField, dateField, descField, addButton);
        section.getChildren().addAll(title, formCard);
        return section;
    }

    private VBox createFormField(String labelText, String placeholder, String icon) {
        VBox field = new VBox(8);
        
        Label label = new Label(labelText);
        label.setTextFill(TEXT_SECONDARY);
        label.setFont(Font.font("System", FontWeight.MEDIUM, 14));
        
        HBox inputContainer = new HBox(10);
        inputContainer.setAlignment(Pos.CENTER_LEFT);
        inputContainer.setStyle(
            "-fx-background-color: #2a3441;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 12 15 12 15;"
        );
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(16));
        
        TextField textField = new TextField();
        textField.setPromptText(placeholder);
        textField.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: #64748b;" +
            "-fx-border-color: transparent;" +
            "-fx-font-size: 14px;"
        );
        textField.setPrefWidth(Double.MAX_VALUE);
        
        inputContainer.getChildren().addAll(iconLabel, textField);
        field.getChildren().addAll(label, inputContainer);
        
        return field;
    }

    private VBox createCategoryDropdown() {
        VBox field = new VBox(8);
        
        Label label = new Label("Category");
        label.setTextFill(TEXT_SECONDARY);
        label.setFont(Font.font("System", FontWeight.MEDIUM, 14));
        
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(
            "Salary", "Freelance", "Investment", "Business", "Bonus", "Other"
        );
        categoryCombo.setValue("Salary");
        categoryCombo.setPrefWidth(Double.MAX_VALUE);
        categoryCombo.setStyle(
            "-fx-background-color: #2a3441;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: transparent;"
        );
        
        field.getChildren().addAll(label, categoryCombo);
        return field;
    }

    private VBox createDateField() {
        VBox field = new VBox(8);
        
        Label label = new Label("Date");
        label.setTextFill(TEXT_SECONDARY);
        label.setFont(Font.font("System", FontWeight.MEDIUM, 14));
        
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(Double.MAX_VALUE);
        datePicker.setStyle(
            "-fx-background-color: #2a3441;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: transparent;"
        );
        
        field.getChildren().addAll(label, datePicker);
        return field;
    }

    private VBox createIncomeListSection() {
        VBox section = new VBox(20);
        section.setPrefWidth(600);
        
        // Header với filter
        HBox listHeader = new HBox();
        listHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Recent Income");
        title.setTextFill(TEXT_PRIMARY);
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Filter dropdown
        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All Time", "This Month", "Last Month", "This Year");
        filterCombo.setValue("This Month");
        filterCombo.setStyle(
            "-fx-background-color: #2a3441;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: transparent;"
        );
        
        listHeader.getChildren().addAll(title, spacer, filterCombo);

        // Income list
        ScrollPane scrollPane = new ScrollPane();
        VBox incomeList = createIncomeList();
        scrollPane.setContent(incomeList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        section.getChildren().addAll(listHeader, scrollPane);
        return section;
    }

    private VBox createIncomeList() {
        VBox list = new VBox(10);
        
        // Get income records from database
        List<IncomeRecord> records = dbManager.getIncomeRecords();
        
        for (IncomeRecord record : records) {
            HBox incomeItem = createIncomeItem(record);
            list.getChildren().add(incomeItem);
        }
        
        return list;
    }

    private HBox createIncomeItem(IncomeRecord record) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(15, 20, 15, 20));
        item.setStyle(
            "-fx-background-color: #1e2749;" +
            "-fx-background-radius: 12px;" +
            "-fx-cursor: hand;"
        );

        // Category icon
        Circle iconBg = new Circle(20);
        iconBg.setFill(INCOME_COLOR);
        
        Label icon = new Label(getCategoryIcon(record.getCategory()));
        icon.setFont(Font.font(14));
        icon.setTextFill(Color.WHITE);
        
        StackPane iconContainer = new StackPane();
        iconContainer.getChildren().addAll(iconBg, icon);

        // Details
        VBox details = new VBox(5);
        Label source = new Label(record.getSource());
        source.setTextFill(TEXT_PRIMARY);
        source.setFont(Font.font("System", FontWeight.MEDIUM, 14));
        
        Label dateAndCategory = new Label(record.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + " • " + record.getCategory());
        dateAndCategory.setTextFill(TEXT_SECONDARY);
        dateAndCategory.setFont(Font.font("System", FontWeight.NORMAL, 12));
        
        details.getChildren().addAll(source, dateAndCategory);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Amount
        Label amount = new Label("+$" + String.format("%.2f", record.getAmount()));
        amount.setTextFill(INCOME_COLOR);
        amount.setFont(Font.font("System", FontWeight.BOLD, 16));

        item.getChildren().addAll(iconContainer, details, spacer, amount);
        
        // Hover effect
        item.setOnMouseEntered(e -> item.setStyle(item.getStyle() + "-fx-background-color: rgba(76, 175, 80, 0.1);"));
        item.setOnMouseExited(e -> item.setStyle(item.getStyle().replace("-fx-background-color: rgba(76, 175, 80, 0.1);", "")));
        
        return item;
    }

    private String getCategoryIcon(String category) {
        return switch (category.toLowerCase()) {
            case "salary" -> "💼";
            case "freelance" -> "💻";
            case "investment" -> "📈";
            case "business" -> "🏢";
            case "bonus" -> "🎁";
            default -> "💰";
        };
    }

    private void handleAddIncome() {
        // Implementation for adding income
        // Get form values and save to database
        // Refresh the income list
        // Show success message
    }

    private void navigateToDashboard() {
        // Implementation for navigation back to dashboard
    }
} 