package com.cashflow;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.animation.*;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.animation.RotateTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.util.Duration;
import javafx.concurrent.Task;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.CompletableFuture;

public class CardManager {
    private static final Logger logger = Logger.getLogger(CardManager.class.getName());
    private DatabaseManager dbManager;
    private ThemeManager.Theme currentTheme;
    private List<BankCard> cards;

    public CardManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.currentTheme = ThemeManager.getInstance().getCurrentTheme();
        this.cards = new ArrayList<>();
        initializeCardDatabase();
        loadCards();
    }

    public VBox createCardManagementPage() {
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30));
        mainContent.setStyle("-fx-background-color: " + ThemeManager.toHexString(currentTheme.getBgPrimary()) + ";");

        // Header
        HBox header = createCardHeader();
        
        // Add card section
        VBox addCardSection = createAddCardSection();
        
        // Cards grid
        VBox cardsSection = createCardsSection();
        
        // Statistics
        VBox statsSection = createCardStatsSection();

        mainContent.getChildren().addAll(header, addCardSection, cardsSection, statsSection);
        return mainContent;
    }

    private HBox createCardHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleSection = new VBox(5);
        
        Label title = new Label("💳 Card Management");
        title.setTextFill(currentTheme.getTextPrimary());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        
        Label subtitle = new Label("Manage your bank accounts and payment cards");
        subtitle.setTextFill(currentTheme.getTextSecondary());
        subtitle.setFont(Font.font("Segoe UI", 14));
        
        titleSection.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addCardBtn = new Button("+ Add New Card");
        addCardBtn.setStyle(createPrimaryButtonStyle());
        addCardBtn.setOnAction(e -> showAddCardDialog());
        
        header.getChildren().addAll(titleSection, spacer, addCardBtn);
        return header;
    }

    private VBox createAddCardSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(25));
        section.setStyle(createCardStyle());

        Label sectionTitle = new Label("🏦 Quick Add Account");
        sectionTitle.setTextFill(currentTheme.getTextPrimary());
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        HBox quickAddForm = new HBox(15);
        quickAddForm.setAlignment(Pos.CENTER_LEFT);

        TextField accountNameField = new TextField();
        accountNameField.setPromptText("Account Name");
        accountNameField.setPrefWidth(200);
        accountNameField.setStyle(createTextFieldStyle());

        ComboBox<String> cardTypeCombo = new ComboBox<>();
        cardTypeCombo.getItems().addAll("Credit Card", "Debit Card", "Savings Account", "Checking Account");
        cardTypeCombo.setValue("Credit Card");
        cardTypeCombo.setPrefWidth(150);
        cardTypeCombo.setStyle(createComboBoxStyle());

        TextField balanceField = new TextField();
        balanceField.setPromptText("Balance");
        balanceField.setPrefWidth(120);
        balanceField.setStyle(createTextFieldStyle());

        Button quickAddBtn = new Button("➕ Add");
        quickAddBtn.setStyle(createSecondaryButtonStyle());
        quickAddBtn.setOnAction(e -> quickAddCard(accountNameField, cardTypeCombo, balanceField));

        quickAddForm.getChildren().addAll(accountNameField, cardTypeCombo, balanceField, quickAddBtn);
        section.getChildren().addAll(sectionTitle, quickAddForm);
        return section;
    }

    private VBox createCardsSection() {
        VBox section = new VBox(20);

        Label sectionTitle = new Label("💰 Your Accounts");
        sectionTitle.setTextFill(currentTheme.getTextPrimary());
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        HBox cardsGrid = new HBox(20);
        cardsGrid.setAlignment(Pos.CENTER_LEFT);

        // Create card visuals for each account
        for (BankCard card : cards) {
            VBox cardVisual = createBankCardVisual(card);
            cardsGrid.getChildren().add(cardVisual);
        }

        // Add placeholder if no cards
        if (cards.isEmpty()) {
            VBox emptyState = createEmptyCardsState();
            cardsGrid.getChildren().add(emptyState);
        }

        ScrollPane scrollPane = new ScrollPane(cardsGrid);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        section.getChildren().addAll(sectionTitle, scrollPane);
        return section;
    }

    private VBox createBankCardVisual(BankCard card) {
        VBox cardContainer = new VBox(15);
        cardContainer.setPrefWidth(300);

        // Card visual
        StackPane cardVisual = new StackPane();
        cardVisual.setPrefSize(280, 180);

        // Card background with gradient
        Rectangle cardBg = new Rectangle(280, 180);
        cardBg.setArcWidth(20);
        cardBg.setArcHeight(20);
        
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, card.getGradientStart()),
            new Stop(1, card.getGradientEnd())
        );
        cardBg.setFill(gradient);

        // Card content
        VBox cardContent = new VBox(15);
        cardContent.setPadding(new Insets(20));
        cardContent.setAlignment(Pos.TOP_LEFT);

        // Bank name and type
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        Label bankName = new Label(card.getBankName());
        bankName.setTextFill(Color.WHITE);
        bankName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        Label cardType = new Label(card.getCardType());
        cardType.setTextFill(Color.web("#FFFFFF80"));
        cardType.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        topRow.getChildren().addAll(bankName, spacer1, cardType);

        // Card number (masked)
        Label cardNumber = new Label(maskCardNumber(card.getCardNumber()));
        cardNumber.setTextFill(Color.WHITE);
        cardNumber.setFont(Font.font("Consolas", FontWeight.NORMAL, 18));

        // Bottom row
        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        
        VBox balanceSection = new VBox(3);
        Label balanceLabel = new Label("Balance");
        balanceLabel.setTextFill(Color.web("#FFFFFF60"));
        balanceLabel.setFont(Font.font("Segoe UI", 10));
        
        Label balanceAmount = new Label("$" + String.format("%.2f", card.getBalance()));
        balanceAmount.setTextFill(Color.WHITE);
        balanceAmount.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        
        balanceSection.getChildren().addAll(balanceLabel, balanceAmount);
        
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        VBox expirySection = new VBox(3);
        expirySection.setAlignment(Pos.CENTER_RIGHT);
        
        Label expiryLabel = new Label("Expires");
        expiryLabel.setTextFill(Color.web("#FFFFFF60"));
        expiryLabel.setFont(Font.font("Segoe UI", 10));
        
        Label expiryDate = new Label(card.getExpiryDate().format(DateTimeFormatter.ofPattern("MM/yy")));
        expiryDate.setTextFill(Color.WHITE);
        expiryDate.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        expirySection.getChildren().addAll(expiryLabel, expiryDate);
        
        bottomRow.getChildren().addAll(balanceSection, spacer2, expirySection);

        cardContent.getChildren().addAll(topRow, cardNumber, bottomRow);
        cardVisual.getChildren().addAll(cardBg, cardContent);

        // Card actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        
        Button editBtn = new Button("✏️ Edit");
        editBtn.setStyle(createSecondaryButtonStyle());
        editBtn.setOnAction(e -> editCard(card));
        
        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.setStyle(createDangerButtonStyle());
        deleteBtn.setOnAction(e -> deleteCard(card));
        
        Button transactionsBtn = new Button("📊 Transactions");
        transactionsBtn.setStyle(createPrimaryButtonStyle());
        transactionsBtn.setOnAction(e -> showCardTransactions(card));
        
        actions.getChildren().addAll(editBtn, deleteBtn, transactionsBtn);

        // Add hover animation
        cardVisual.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), cardVisual);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });

        cardVisual.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), cardVisual);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        cardContainer.getChildren().addAll(cardVisual, actions);
        return cardContainer;
    }

    private VBox createCardStatsSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(25));
        section.setStyle(createCardStyle());

        Label title = new Label("📈 Account Statistics");
        title.setTextFill(currentTheme.getTextPrimary());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        HBox statsRow = new HBox(30);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        // Total balance
        VBox totalBalanceCard = createStatsCard("��", "Total Balance", "$" + String.format("%.2f", calculateTotalBalance()), currentTheme.getSuccess());
        
        // Number of accounts
        VBox accountCountCard = createStatsCard("🏦", "Accounts", String.valueOf(cards.size()), currentTheme.getAccentPurple());
        
        // Average balance
        VBox avgBalanceCard = createStatsCard("📊", "Average Balance", "$" + String.format("%.2f", calculateAverageBalance()), currentTheme.getWarning());

        statsRow.getChildren().addAll(totalBalanceCard, accountCountCard, avgBalanceCard);
        section.getChildren().addAll(title, statsRow);
        return section;
    }

    private VBox createStatsCard(String icon, String title, String value, Color accentColor) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(180);
        card.setStyle(
            "-fx-background-color: " + ThemeManager.toRgbaString(accentColor, 0.1) + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + ThemeManager.toRgbaString(accentColor, 0.3) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;"
        );

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(18));
        
        Label titleLabel = new Label(title);
        titleLabel.setTextFill(currentTheme.getTextSecondary());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        header.getChildren().addAll(iconLabel, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.setTextFill(currentTheme.getTextPrimary());
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        card.getChildren().addAll(header, valueLabel);
        return card;
    }

    private VBox createEmptyCardsState() {
        VBox emptyState = new VBox(20);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(60));

        Label emptyIcon = new Label("💳");
        emptyIcon.setFont(Font.font(48));

        Label emptyTitle = new Label("No accounts added yet");
        emptyTitle.setTextFill(currentTheme.getTextPrimary());
        emptyTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        Label emptyMessage = new Label("Add your first bank account or payment card to get started");
        emptyMessage.setTextFill(currentTheme.getTextSecondary());
        emptyMessage.setFont(Font.font("Segoe UI", 14));

        Button addFirstCardBtn = new Button("🏦 Add First Account");
        addFirstCardBtn.setStyle(createPrimaryButtonStyle());
        addFirstCardBtn.setOnAction(e -> showAddCardDialog());

        emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptyMessage, addFirstCardBtn);
        return emptyState;
    }

    // Database operations
    private void initializeCardDatabase() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cashflow.db");
             Statement stmt = conn.createStatement()) {
            
            String createCardsTable = """
                CREATE TABLE IF NOT EXISTS bank_cards (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    bank_name TEXT NOT NULL,
                    card_type TEXT NOT NULL,
                    card_number TEXT NOT NULL,
                    balance REAL DEFAULT 0,
                    expiry_date DATE,
                    gradient_start TEXT DEFAULT '#667eea',
                    gradient_end TEXT DEFAULT '#764ba2',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            
            stmt.execute(createCardsTable);
            
        } catch (SQLException e) {
            logger.severe("Failed to initialize cards database: " + e.getMessage());
        }
    }

    private void loadCards() {
        cards.clear();
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cashflow.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM bank_cards ORDER BY created_at DESC")) {
            
            while (rs.next()) {
                BankCard card = new BankCard(
                    rs.getInt("id"),
                    rs.getString("bank_name"),
                    rs.getString("card_type"),
                    rs.getString("card_number"),
                    rs.getDouble("balance"),
                    LocalDate.parse(rs.getString("expiry_date")),
                    Color.web(rs.getString("gradient_start")),
                    Color.web(rs.getString("gradient_end"))
                );
                cards.add(card);
            }
            
        } catch (SQLException e) {
            logger.severe("Failed to load cards: " + e.getMessage());
        }
    }

    // Helper methods
    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() >= 4) {
            return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }
        return "****";
    }

    private String calculateTotalBalance() {
        double total = cards.stream().mapToDouble(BankCard::getBalance).sum();
        return String.format("$%.2f", total);
    }

    private double calculateAverageBalance() {
        if (cards.isEmpty()) return 0;
        return cards.stream().mapToDouble(BankCard::getBalance).average().orElse(0);
    }

    // Event handlers
    private void quickAddCard(TextField nameField, ComboBox<String> typeCombo, TextField balanceField) {
        // Implementation for quick add
        showSuccessMessage("Account added successfully!");
        nameField.clear();
        balanceField.clear();
    }

    private void showAddCardDialog() {
        // Implementation for detailed add dialog
        showInfoMessage("Add card dialog - to be implemented");
    }

    private void editCard(BankCard card) {
        showInfoMessage("Edit card: " + card.getBankName());
    }

    private void deleteCard(BankCard card) {
        showInfoMessage("Delete card: " + card.getBankName());
    }

    private void showCardTransactions(BankCard card) {
        showInfoMessage("Transactions for: " + card.getBankName());
    }

    // Styling methods
    private String createCardStyle() {
        return "-fx-background-color: " + ThemeManager.toHexString(currentTheme.getCardBg()) + ";" +
               "-fx-background-radius: 15;" +
               "-fx-border-radius: 15;" +
               "-fx-border-color: " + ThemeManager.toRgbaString(currentTheme.getTextMuted(), 0.3) + ";" +
               "-fx-border-width: 1;" +
               "-fx-effect: dropshadow(gaussian, " + ThemeManager.toRgbaString(currentTheme.getShadowColor(), 0.1) + ", 15, 0, 0, 5);";
    }

    private String createPrimaryButtonStyle() {
        return "-fx-background-color: " + ThemeManager.toHexString(currentTheme.getAccentPurple()) + ";" +
               "-fx-text-fill: white;" +
               "-fx-background-radius: 8;" +
               "-fx-padding: 10 20;" +
               "-fx-font-weight: bold;" +
               "-fx-cursor: hand;";
    }

    private String createSecondaryButtonStyle() {
        return "-fx-background-color: " + ThemeManager.toRgbaString(currentTheme.getTextPrimary(), 0.1) + ";" +
               "-fx-text-fill: " + ThemeManager.toHexString(currentTheme.getTextPrimary()) + ";" +
               "-fx-background-radius: 8;" +
               "-fx-padding: 10 20;" +
               "-fx-font-weight: bold;" +
               "-fx-cursor: hand;" +
               "-fx-border-color: " + ThemeManager.toRgbaString(currentTheme.getTextPrimary(), 0.2) + ";" +
               "-fx-border-width: 1;" +
               "-fx-border-radius: 8;";
    }

    private String createDangerButtonStyle() {
        return "-fx-background-color: " + ThemeManager.toHexString(currentTheme.getError()) + ";" +
               "-fx-text-fill: white;" +
               "-fx-background-radius: 8;" +
               "-fx-padding: 10 20;" +
               "-fx-font-weight: bold;" +
               "-fx-cursor: hand;";
    }

    private String createTextFieldStyle() {
        return "-fx-background-color: " + ThemeManager.toHexString(currentTheme.getCardBg()) + ";" +
               "-fx-text-fill: " + ThemeManager.toHexString(currentTheme.getTextPrimary()) + ";" +
               "-fx-background-radius: 8px;" +
               "-fx-border-color: " + ThemeManager.toRgbaString(currentTheme.getTextPrimary(), 0.5) + ";" +
               "-fx-border-radius: 8px;";
    }

    private String createComboBoxStyle() {
        return "-fx-background-color: " + ThemeManager.toHexString(currentTheme.getCardBg()) + ";" +
               "-fx-text-fill: " + ThemeManager.toHexString(currentTheme.getTextPrimary()) + ";" +
               "-fx-background-radius: 8px;";
    }

    // Message methods
    private void showSuccessMessage(String message) {
        showMessage(message, Alert.AlertType.INFORMATION);
    }

    private void showInfoMessage(String message) {
        showMessage(message, Alert.AlertType.INFORMATION);
    }

    private void showMessage(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Card Manager");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // BankCard model class
    public static class BankCard {
        private int id;
        private String bankName;
        private String cardType;
        private String cardNumber;
        private double balance;
        private LocalDate expiryDate;
        private Color gradientStart;
        private Color gradientEnd;

        public BankCard(int id, String bankName, String cardType, String cardNumber, 
                       double balance, LocalDate expiryDate, Color gradientStart, Color gradientEnd) {
            this.id = id;
            this.bankName = bankName;
            this.cardType = cardType;
            this.cardNumber = cardNumber;
            this.balance = balance;
            this.expiryDate = expiryDate;
            this.gradientStart = gradientStart;
            this.gradientEnd = gradientEnd;
        }

        // Getters
        public int getId() { return id; }
        public String getBankName() { return bankName; }
        public String getCardType() { return cardType; }
        public String getCardNumber() { return cardNumber; }
        public double getBalance() { return balance; }
        public LocalDate getExpiryDate() { return expiryDate; }
        public Color getGradientStart() { return gradientStart; }
        public Color getGradientEnd() { return gradientEnd; }
    }
} 