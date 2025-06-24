package com.cashflow;

import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.RotateTransition;
import javafx.util.Duration;
import javafx.concurrent.Task;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AdvancedAnalyticsService {
    private static final Logger logger = Logger.getLogger(AdvancedAnalyticsService.class.getName());
    
    private DatabaseManager dbManager;
    private ThemeManager.Theme currentTheme;

    public AdvancedAnalyticsService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.currentTheme = ThemeManager.getInstance().getCurrentTheme();
    }

    // 1. Trendline Chart - Thu nhập vs Chi tiêu theo thời gian
    public VBox createTrendlineChart() {
        VBox chartContainer = new VBox(20);
        chartContainer.setPadding(new Insets(25));
        chartContainer.setStyle(createCardStyle());

        Label title = new Label("📈 Income vs Expenses Trend");
        title.setTextFill(currentTheme.getTextPrimary());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        // Create line chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        
        xAxis.setLabel("Month");
        yAxis.setLabel("Amount ($)");
        lineChart.setTitle("Financial Trend Analysis");
        lineChart.setCreateSymbols(true);
        lineChart.setAnimated(true);
        lineChart.setPrefHeight(350);

        // Income series
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        
        // Expenses series
        XYChart.Series<String, Number> expensesSeries = new XYChart.Series<>();
        expensesSeries.setName("Expenses");

        // Net worth series
        XYChart.Series<String, Number> netWorthSeries = new XYChart.Series<>();
        netWorthSeries.setName("Net Worth");

        // Get data for last 12 months
        List<MonthlyData> monthlyData = dbManager.getEnhancedMonthlyChartData();
        Collections.reverse(monthlyData); // Oldest first for trend

        for (MonthlyData data : monthlyData) {
            String monthLabel = data.getMonth();
            incomeSeries.getData().add(new XYChart.Data<>(monthLabel, data.getIncome()));
            expensesSeries.getData().add(new XYChart.Data<>(monthLabel, data.getOutcome()));
            netWorthSeries.getData().add(new XYChart.Data<>(monthLabel, data.getBalance()));
        }

        lineChart.getData().addAll(incomeSeries, expensesSeries, netWorthSeries);

        // Style the chart
        applyChartStyling(lineChart);

        chartContainer.getChildren().addAll(title, lineChart);
        return chartContainer;
    }

    // 2. Radial Progress Chart - Savings Goal Progress
    public VBox createSavingsGoalChart() {
        VBox chartContainer = new VBox(20);
        chartContainer.setPadding(new Insets(25));
        chartContainer.setStyle(createCardStyle());

        Label title = new Label("🎯 Savings Goals Progress");
        title.setTextFill(currentTheme.getTextPrimary());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        HBox goalsContainer = new HBox(30);
        goalsContainer.setAlignment(Pos.CENTER);

        // Mock savings goals data
        List<SavingsGoal> goals = Arrays.asList(
            new SavingsGoal("Emergency Fund", 10000, 6500, currentTheme.getSuccess()),
            new SavingsGoal("Vacation", 5000, 2800, currentTheme.getWarning()),
            new SavingsGoal("New Car", 25000, 15000, currentTheme.getAccentPurple())
        );

        for (SavingsGoal goal : goals) {
            VBox goalCard = createRadialProgressCard(goal);
            goalsContainer.getChildren().add(goalCard);
        }

        chartContainer.getChildren().addAll(title, goalsContainer);
        return chartContainer;
    }

    // 3. Heatmap Calendar - Spending Activity
    public VBox createSpendingHeatmap() {
        VBox chartContainer = new VBox(20);
        chartContainer.setPadding(new Insets(25));
        chartContainer.setStyle(createCardStyle());

        Label title = new Label("🔥 Spending Activity Heatmap");
        title.setTextFill(currentTheme.getTextPrimary());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        VBox heatmapContainer = createCalendarHeatmap();
        chartContainer.getChildren().addAll(title, heatmapContainer);
        return chartContainer;
    }

    // 4. Waterfall Chart - Cash Flow Breakdown
    public VBox createCashFlowWaterfallChart() {
        VBox chartContainer = new VBox(20);
        chartContainer.setPadding(new Insets(25));
        chartContainer.setStyle(createCardStyle());

        Label title = new Label("🌊 Cash Flow Waterfall");
        title.setTextFill(currentTheme.getTextPrimary());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        StackedBarChart<String, Number> waterfallChart = new StackedBarChart<>(xAxis, yAxis);
        
        waterfallChart.setTitle("Monthly Cash Flow Breakdown");
        waterfallChart.setAnimated(true);
        waterfallChart.setPrefHeight(350);

        // Get top categories data
        List<CategoryData> incomeCategories = dbManager.getTopIncomeCategories(3);
        List<CategoryData> expenseCategories = dbManager.getTopExpenseCategories(5);

        // Create series for each category
        for (CategoryData category : incomeCategories) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(category.getCategory());
            series.getData().add(new XYChart.Data<>("Income", category.getTotalAmount()));
            waterfallChart.getData().add(series);
        }

        for (CategoryData category : expenseCategories) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(category.getCategory());
            series.getData().add(new XYChart.Data<>("Expenses", -category.getTotalAmount()));
            waterfallChart.getData().add(series);
        }

        applyChartStyling(waterfallChart);
        chartContainer.getChildren().addAll(title, waterfallChart);
        return chartContainer;
    }

    // 5. Bubble Chart - Category Analysis
    public VBox createCategoryBubbleChart() {
        VBox chartContainer = new VBox(20);
        chartContainer.setPadding(new Insets(25));
        chartContainer.setStyle(createCardStyle());

        Label title = new Label("🎈 Category Spending Analysis");
        title.setTextFill(currentTheme.getTextPrimary());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        BubbleChart<Number, Number> bubbleChart = new BubbleChart<>(xAxis, yAxis);
        
        xAxis.setLabel("Frequency (Transactions)");
        yAxis.setLabel("Average Amount ($)");
        bubbleChart.setTitle("Spending Patterns by Category");
        bubbleChart.setAnimated(true);
        bubbleChart.setPrefHeight(350);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Categories");

        // Analyze expense categories
        List<CategoryData> categories = dbManager.getTopExpenseCategories(10);
        for (CategoryData category : categories) {
            // Mock transaction count and average (would need real calculation)
            int transactionCount = (int)(Math.random() * 50) + 5;
            double avgAmount = category.getTotalAmount() / transactionCount;
            
            XYChart.Data<Number, Number> data = new XYChart.Data<>(
                transactionCount, avgAmount, category.getTotalAmount() / 100
            );
            series.getData().add(data);
        }

        bubbleChart.getData().add(series);
        applyChartStyling(bubbleChart);

        chartContainer.getChildren().addAll(title, bubbleChart);
        return chartContainer;
    }

    // Helper methods
    private VBox createRadialProgressCard(SavingsGoal goal) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setStyle(
            "-fx-background-color: " + ThemeManager.toRgbaString(currentTheme.getCardBg(), 0.8) + ";" +
            "-fx-background-radius: 15;" +
            "-fx-border-radius: 15;" +
            "-fx-border-color: " + ThemeManager.toRgbaString(goal.color, 0.3) + ";" +
            "-fx-border-width: 2;"
        );

        // Progress circle
        Circle backgroundCircle = new Circle(60);
        backgroundCircle.setFill(Color.TRANSPARENT);
        backgroundCircle.setStroke(ThemeManager.toRgbaColor(goal.color, 0.2));
        backgroundCircle.setStrokeWidth(8);

        double progress = goal.current / goal.target;
        Arc progressArc = new Arc(0, 0, 60, 60, 90, -progress * 360);
        progressArc.setType(ArcType.OPEN);
        progressArc.setStroke(goal.color);
        progressArc.setStrokeWidth(8);
        progressArc.setFill(Color.TRANSPARENT);

        VBox circleContainer = new VBox();
        circleContainer.setAlignment(Pos.CENTER);
        circleContainer.getChildren().addAll(backgroundCircle, progressArc);

        // Labels
        Label nameLabel = new Label(goal.name);
        nameLabel.setTextFill(currentTheme.getTextPrimary());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        Label progressLabel = new Label(String.format("%.0f%%", progress * 100));
        progressLabel.setTextFill(goal.color);
        progressLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        Label amountLabel = new Label(String.format("$%.0f / $%.0f", goal.current, goal.target));
        amountLabel.setTextFill(currentTheme.getTextSecondary());
        amountLabel.setFont(Font.font("Segoe UI", 12));

        card.getChildren().addAll(circleContainer, nameLabel, progressLabel, amountLabel);
        return card;
    }

    private VBox createCalendarHeatmap() {
        VBox heatmapContainer = new VBox(10);
        heatmapContainer.setAlignment(Pos.CENTER);

        // Create a 7x4 grid for 4 weeks
        for (int week = 0; week < 4; week++) {
            HBox weekRow = new HBox(5);
            weekRow.setAlignment(Pos.CENTER);

            for (int day = 0; day < 7; day++) {
                // Mock spending intensity (0-1)
                double intensity = Math.random();
                Color cellColor = ThemeManager.toRgbaColor(currentTheme.getAccentPurple(), intensity * 0.8);
                
                VBox dayCell = new VBox();
                dayCell.setPrefSize(30, 30);
                dayCell.setStyle(
                    "-fx-background-color: " + ThemeManager.toHexString(cellColor) + ";" +
                    "-fx-background-radius: 4;"
                );
                weekRow.getChildren().add(dayCell);
            }
            heatmapContainer.getChildren().add(weekRow);
        }

        return heatmapContainer;
    }

    private void applyChartStyling(Chart chart) {
        chart.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + ThemeManager.toHexString(currentTheme.getTextPrimary()) + ";" +
            "-fx-stroke: " + ThemeManager.toHexString(currentTheme.getAccentPurple()) + ";"
        );
    }

    private String createCardStyle() {
        return "-fx-background-color: " + ThemeManager.toHexString(currentTheme.getCardBg()) + ";" +
               "-fx-background-radius: 15;" +
               "-fx-border-radius: 15;" +
               "-fx-border-color: " + ThemeManager.toRgbaString(currentTheme.getTextMuted(), 0.3) + ";" +
               "-fx-border-width: 1;" +
               "-fx-effect: dropshadow(gaussian, " + ThemeManager.toRgbaString(currentTheme.getShadowColor(), 0.1) + ", 15, 0, 0, 5);";
    }

    // Data classes
    public static class SavingsGoal {
        String name;
        double target;
        double current;
        Color color;

        public SavingsGoal(String name, double target, double current, Color color) {
            this.name = name;
            this.target = target;
            this.current = current;
            this.color = color;
        }
    }
} 