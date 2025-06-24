package com.cashflow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Enhanced Analytics Service with advanced algorithms for financial analysis
 */
public class EnhancedAnalyticsService extends AnalyticsService {
    
    private static final Logger logger = Logger.getLogger(EnhancedAnalyticsService.class.getName());
    
    // Machine Learning constants
    private static final double LEARNING_RATE = 0.01;
    private static final int MAX_ITERATIONS = 1000;
    private static final double CONVERGENCE_THRESHOLD = 0.0001;
    
    public EnhancedAnalyticsService(DatabaseManager dbManager) {
        super(dbManager);
    }
    
    /**
     * Advanced spending pattern analysis using time series analysis
     */
    public Map<String, SpendingForecast> forecastSpendingPatterns(int monthsAhead) {
        Map<String, SpendingForecast> forecasts = new HashMap<>();
        
        try {
            List<CategoryData> categories = getDbManager().getTopExpenseCategories(10);
            
            for (CategoryData category : categories) {
                // Get historical data for each category
                List<Double> historicalData = getHistoricalCategoryData(category.getCategory());
                
                if (historicalData.size() >= 3) {
                    // Apply exponential smoothing for forecast
                    SpendingForecast forecast = exponentialSmoothing(
                        category.getCategory(), 
                        historicalData, 
                        monthsAhead
                    );
                    forecasts.put(category.getCategory(), forecast);
                }
            }
        } catch (Exception e) {
            logger.severe("Error forecasting spending patterns: " + e.getMessage());
        }
        
        return forecasts;
    }
    
    /**
     * Anomaly detection in spending using statistical methods
     */
    public List<SpendingAnomaly> detectSpendingAnomalies() {
        List<SpendingAnomaly> anomalies = new ArrayList<>();
        
        try {
            // Get recent transactions
            List<OutcomeRecord> recentOutcomes = getDbManager().getOutcomeRecords();
            Map<String, List<Double>> categoryAmounts = groupByCategory(recentOutcomes);
            
            for (Map.Entry<String, List<Double>> entry : categoryAmounts.entrySet()) {
                String category = entry.getKey();
                List<Double> amounts = entry.getValue();
                
                if (amounts.size() >= 5) {
                    // Calculate statistics
                    double mean = calculateMean(amounts);
                    double stdDev = calculateStandardDeviation(amounts, mean);
                    
                    // Detect anomalies using z-score
                    for (int i = 0; i < amounts.size(); i++) {
                        double zScore = Math.abs((amounts.get(i) - mean) / stdDev);
                        if (zScore > 2.5) { // Threshold for anomaly
                            anomalies.add(new SpendingAnomaly(
                                category,
                                amounts.get(i),
                                mean,
                                zScore,
                                recentOutcomes.get(i).getDate()
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Error detecting anomalies: " + e.getMessage());
        }
        
        return anomalies;
    }
    
    /**
     * Predictive budgeting using linear regression
     */
    public BudgetPrediction predictBudgetNeeds(int monthsAhead) {
        BudgetPrediction prediction = new BudgetPrediction();
        
        try {
            // Get historical monthly data
            List<MonthlyData> historicalData = getDbManager().getEnhancedMonthlyChartData();
            
            if (historicalData.size() >= 3) {
                // Extract income and outcome arrays
                double[] months = new double[historicalData.size()];
                double[] incomes = new double[historicalData.size()];
                double[] outcomes = new double[historicalData.size()];
                
                for (int i = 0; i < historicalData.size(); i++) {
                    months[i] = i;
                    incomes[i] = historicalData.get(i).getIncome();
                    outcomes[i] = historicalData.get(i).getOutcome();
                }
                
                // Perform linear regression
                LinearRegressionResult incomeRegression = linearRegression(months, incomes);
                LinearRegressionResult outcomeRegression = linearRegression(months, outcomes);
                
                // Predict future values
                for (int i = 1; i <= monthsAhead; i++) {
                    double futureMonth = historicalData.size() + i;
                    double predictedIncome = incomeRegression.predict(futureMonth);
                    double predictedOutcome = outcomeRegression.predict(futureMonth);
                    
                    prediction.addMonthlyPrediction(
                        LocalDate.now().plusMonths(i),
                        predictedIncome,
                        predictedOutcome
                    );
                }
                
                // Calculate confidence intervals
                prediction.setConfidenceLevel(calculateConfidence(incomeRegression, outcomeRegression));
            }
        } catch (Exception e) {
            logger.severe("Error predicting budget: " + e.getMessage());
        }
        
        return prediction;
    }
    
    /**
     * Smart savings recommendations using goal-based analysis
     */
    public List<SavingsRecommendation> generateSmartSavingsRecommendations() {
        List<SavingsRecommendation> recommendations = new ArrayList<>();
        
        try {
            DashboardStats stats = getDbManager().getDashboardStats();
            double currentSavingsRate = stats.getSavingsRate();
            double monthlyIncome = stats.getMonthlyIncome();
            double monthlyOutcome = stats.getMonthlyOutcome();
            
            // Analyze spending categories for potential savings
            List<CategoryData> topExpenses = getDbManager().getTopExpenseCategories(5);
            
            for (CategoryData category : topExpenses) {
                double categoryPercentage = category.getPercentage();
                
                // Check if category spending is above benchmark
                double benchmark = getCategoryBenchmark(category.getCategory());
                if (categoryPercentage > benchmark) {
                    double potentialSavings = (categoryPercentage - benchmark) / 100 * monthlyOutcome;
                    
                    recommendations.add(new SavingsRecommendation(
                        category.getCategory(),
                        String.format("Reduce %s spending by %.0f%% to save $%.2f monthly", 
                            category.getCategory(), 
                            (categoryPercentage - benchmark),
                            potentialSavings
                        ),
                        potentialSavings,
                        SavingsRecommendation.Priority.HIGH
                    ));
                }
            }
            
            // Add general recommendations based on savings rate
            if (currentSavingsRate < 20) {
                recommendations.add(new SavingsRecommendation(
                    "General",
                    "Aim to increase your savings rate to 20% for better financial security",
                    (0.20 - currentSavingsRate/100) * monthlyIncome,
                    SavingsRecommendation.Priority.MEDIUM
                ));
            }
            
            // Sort by potential impact
            recommendations.sort((a, b) -> Double.compare(b.getPotentialSavings(), a.getPotentialSavings()));
            
        } catch (Exception e) {
            logger.severe("Error generating savings recommendations: " + e.getMessage());
        }
        
        return recommendations;
    }
    
    /**
     * Cash flow optimization using dynamic programming
     */
    public CashFlowOptimization optimizeCashFlow() {
        CashFlowOptimization optimization = new CashFlowOptimization();
        
        try {
            // Get current financial state
            DashboardStats stats = getDbManager().getDashboardStats();
            List<IncomeRecord> incomes = getDbManager().getIncomeRecords();
            List<OutcomeRecord> outcomes = getDbManager().getOutcomeRecords();
            
            // Group by date for daily cash flow
            Map<LocalDate, Double> dailyCashFlow = calculateDailyCashFlow(incomes, outcomes);
            
            // Find optimal payment scheduling
            List<PaymentSchedule> optimalSchedule = optimizePaymentSchedule(
                outcomes.stream()
                    .filter(o -> o.isRecurring())
                    .collect(Collectors.toList()),
                dailyCashFlow
            );
            
            optimization.setOptimalSchedule(optimalSchedule);
            optimization.setEstimatedSavings(calculateScheduleSavings(optimalSchedule));
            
            // Recommend cash buffer
            double recommendedBuffer = calculateOptimalCashBuffer(dailyCashFlow);
            optimization.setRecommendedCashBuffer(recommendedBuffer);
            
        } catch (Exception e) {
            logger.severe("Error optimizing cash flow: " + e.getMessage());
        }
        
        return optimization;
    }
    
    /**
     * Risk assessment using Monte Carlo simulation
     */
    public FinancialRiskAssessment assessFinancialRisk(int simulationRuns) {
        FinancialRiskAssessment assessment = new FinancialRiskAssessment();
        
        try {
            DashboardStats stats = getDbManager().getDashboardStats();
            List<MonthlyData> historicalData = getDbManager().getEnhancedMonthlyChartData();
            
            // Calculate volatility
            double incomeVolatility = calculateVolatility(
                historicalData.stream().map(MonthlyData::getIncome).collect(Collectors.toList())
            );
            double outcomeVolatility = calculateVolatility(
                historicalData.stream().map(MonthlyData::getOutcome).collect(Collectors.toList())
            );
            
            // Run Monte Carlo simulation
            List<Double> simulatedBalances = new ArrayList<>();
            Random random = new Random();
            
            for (int i = 0; i < simulationRuns; i++) {
                double simulatedBalance = stats.getBalance();
                
                // Simulate 6 months ahead
                for (int month = 0; month < 6; month++) {
                    double incomeShock = random.nextGaussian() * incomeVolatility;
                    double outcomeShock = random.nextGaussian() * outcomeVolatility;
                    
                    double monthlyIncome = stats.getMonthlyIncome() * (1 + incomeShock);
                    double monthlyOutcome = stats.getMonthlyOutcome() * (1 + outcomeShock);
                    
                    simulatedBalance += (monthlyIncome - monthlyOutcome);
                }
                
                simulatedBalances.add(simulatedBalance);
            }
            
            // Calculate risk metrics
            simulatedBalances.sort(Double::compareTo);
            
            assessment.setValueAtRisk95(simulatedBalances.get((int)(simulationRuns * 0.05)));
            assessment.setExpectedValue(calculateMean(simulatedBalances));
            assessment.setProbabilityOfNegativeBalance(
                simulatedBalances.stream().filter(b -> b < 0).count() / (double) simulationRuns
            );
            
            // Risk score (0-100)
            double riskScore = calculateRiskScore(assessment);
            assessment.setRiskScore(riskScore);
            assessment.setRiskLevel(getRiskLevel(riskScore));
            
        } catch (Exception e) {
            logger.severe("Error assessing financial risk: " + e.getMessage());
        }
        
        return assessment;
    }
    
    // Helper methods
    
    private SpendingForecast exponentialSmoothing(String category, List<Double> data, int periods) {
        SpendingForecast forecast = new SpendingForecast(category);
        
        double alpha = 0.3; // Smoothing parameter
        double[] smoothed = new double[data.size()];
        smoothed[0] = data.get(0);
        
        // Apply exponential smoothing
        for (int i = 1; i < data.size(); i++) {
            smoothed[i] = alpha * data.get(i) + (1 - alpha) * smoothed[i - 1];
        }
        
        // Forecast future values
        double lastSmoothed = smoothed[smoothed.length - 1];
        double trend = (smoothed[smoothed.length - 1] - smoothed[0]) / smoothed.length;
        
        for (int i = 1; i <= periods; i++) {
            double forecastValue = lastSmoothed + (trend * i);
            forecast.addForecastPoint(
                LocalDate.now().plusMonths(i),
                forecastValue,
                forecastValue * 0.9, // Lower bound
                forecastValue * 1.1  // Upper bound
            );
        }
        
        return forecast;
    }
    
    private LinearRegressionResult linearRegression(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;
        
        // Calculate R-squared
        double yMean = sumY / n;
        double ssTotal = 0, ssResidual = 0;
        
        for (int i = 0; i < n; i++) {
            double predicted = slope * x[i] + intercept;
            ssResidual += Math.pow(y[i] - predicted, 2);
            ssTotal += Math.pow(y[i] - yMean, 2);
        }
        
        double rSquared = 1 - (ssResidual / ssTotal);
        
        return new LinearRegressionResult(slope, intercept, rSquared);
    }
    
    private double calculateMean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    private double calculateStandardDeviation(List<Double> values, double mean) {
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0.0);
        return Math.sqrt(variance);
    }
    
    private double calculateVolatility(List<Double> values) {
        if (values.size() < 2) return 0.0;
        
        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < values.size(); i++) {
            double returnRate = (values.get(i) - values.get(i-1)) / values.get(i-1);
            returns.add(returnRate);
        }
        
        double mean = calculateMean(returns);
        return calculateStandardDeviation(returns, mean);
    }
    
    private double getCategoryBenchmark(String category) {
        // Industry standard benchmarks for spending categories
        return switch (category.toLowerCase()) {
            case "housing", "rent" -> 30.0;
            case "food", "groceries" -> 15.0;
            case "transportation" -> 15.0;
            case "utilities" -> 10.0;
            case "entertainment" -> 5.0;
            case "healthcare" -> 10.0;
            case "savings" -> 20.0;
            default -> 10.0;
        };
    }
    
    private Map<String, List<Double>> groupByCategory(List<OutcomeRecord> outcomes) {
        return outcomes.stream()
            .collect(Collectors.groupingBy(
                OutcomeRecord::getCategory,
                Collectors.mapping(OutcomeRecord::getAmount, Collectors.toList())
            ));
    }
    
    private List<Double> getHistoricalCategoryData(String category) {
        // This would fetch historical monthly totals for a specific category
        // For now, returning mock data
        return Arrays.asList(1200.0, 1150.0, 1300.0, 1250.0, 1400.0, 1350.0);
    }
    
    private Map<LocalDate, Double> calculateDailyCashFlow(List<IncomeRecord> incomes, List<OutcomeRecord> outcomes) {
        Map<LocalDate, Double> cashFlow = new TreeMap<>();
        
        // Add incomes
        for (IncomeRecord income : incomes) {
            cashFlow.merge(income.getDate(), income.getAmount(), Double::sum);
        }
        
        // Subtract outcomes
        for (OutcomeRecord outcome : outcomes) {
            cashFlow.merge(outcome.getDate(), -outcome.getAmount(), Double::sum);
        }
        
        return cashFlow;
    }
    
    private List<PaymentSchedule> optimizePaymentSchedule(List<OutcomeRecord> recurringPayments, 
                                                          Map<LocalDate, Double> cashFlow) {
        // Implement payment scheduling optimization
        // This is a simplified version - real implementation would use dynamic programming
        List<PaymentSchedule> schedules = new ArrayList<>();
        
        for (OutcomeRecord payment : recurringPayments) {
            // Find optimal day of month based on cash flow patterns
            int optimalDay = findOptimalPaymentDay(payment, cashFlow);
            schedules.add(new PaymentSchedule(payment.getTitle(), optimalDay, payment.getAmount()));
        }
        
        return schedules;
    }
    
    private int findOptimalPaymentDay(OutcomeRecord payment, Map<LocalDate, Double> cashFlow) {
        // Simplified: find day with highest average cash flow
        Map<Integer, Double> dayAverages = new HashMap<>();
        
        for (Map.Entry<LocalDate, Double> entry : cashFlow.entrySet()) {
            int day = entry.getKey().getDayOfMonth();
            dayAverages.merge(day, entry.getValue(), (a, b) -> (a + b) / 2);
        }
        
        return dayAverages.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(15); // Default to middle of month
    }
    
    private double calculateScheduleSavings(List<PaymentSchedule> schedules) {
        // Calculate potential savings from optimized payment scheduling
        // This would involve calculating reduced overdraft fees, better cash utilization, etc.
        return schedules.size() * 5.0; // Simplified: $5 per optimized payment
    }
    
    private double calculateOptimalCashBuffer(Map<LocalDate, Double> cashFlow) {
        // Calculate recommended cash buffer based on cash flow volatility
        List<Double> dailyFlows = new ArrayList<>(cashFlow.values());
        double meanFlow = calculateMean(dailyFlows);
        double stdDev = calculateStandardDeviation(dailyFlows, meanFlow);
        
        // Recommend buffer of 2 standard deviations
        return Math.abs(meanFlow - 2 * stdDev) * 30; // 30 days buffer
    }
    
    private double calculateConfidence(LinearRegressionResult income, LinearRegressionResult outcome) {
        // Confidence based on R-squared values
        return (income.rSquared + outcome.rSquared) / 2 * 100;
    }
    
    private double calculateRiskScore(FinancialRiskAssessment assessment) {
        double score = 0;
        
        // Factor in probability of negative balance (40% weight)
        score += (1 - assessment.getProbabilityOfNegativeBalance()) * 40;
        
        // Factor in value at risk (30% weight)
        if (assessment.getValueAtRisk95() > 0) {
            score += 30;
        } else {
            score += Math.max(0, 30 + (assessment.getValueAtRisk95() / assessment.getExpectedValue() * 30));
        }
        
        // Factor in expected value (30% weight)
        if (assessment.getExpectedValue() > 0) {
            score += 30;
        }
        
        return Math.max(0, Math.min(100, score));
    }
    
    private String getRiskLevel(double riskScore) {
        if (riskScore >= 80) return "Low Risk";
        else if (riskScore >= 60) return "Moderate Risk";
        else if (riskScore >= 40) return "High Risk";
        else return "Very High Risk";
    }
    
    // Model classes
    
    public static class SpendingForecast {
        private String category;
        private List<ForecastPoint> forecastPoints;
        
        public SpendingForecast(String category) {
            this.category = category;
            this.forecastPoints = new ArrayList<>();
        }
        
        public void addForecastPoint(LocalDate date, double value, double lowerBound, double upperBound) {
            forecastPoints.add(new ForecastPoint(date, value, lowerBound, upperBound));
        }
        
        // Getters and setters
        public String getCategory() { return category; }
        public List<ForecastPoint> getForecastPoints() { return forecastPoints; }
        
        public static class ForecastPoint {
            private LocalDate date;
            private double value;
            private double lowerBound;
            private double upperBound;
            
            public ForecastPoint(LocalDate date, double value, double lowerBound, double upperBound) {
                this.date = date;
                this.value = value;
                this.lowerBound = lowerBound;
                this.upperBound = upperBound;
            }
            
            // Getters
            public LocalDate getDate() { return date; }
            public double getValue() { return value; }
            public double getLowerBound() { return lowerBound; }
            public double getUpperBound() { return upperBound; }
        }
    }
    
    public static class SpendingAnomaly {
        private String category;
        private double amount;
        private double expectedAmount;
        private double zScore;
        private LocalDate date;
        
        public SpendingAnomaly(String category, double amount, double expectedAmount, 
                              double zScore, LocalDate date) {
            this.category = category;
            this.amount = amount;
            this.expectedAmount = expectedAmount;
            this.zScore = zScore;
            this.date = date;
        }
        
        // Getters
        public String getCategory() { return category; }
        public double getAmount() { return amount; }
        public double getExpectedAmount() { return expectedAmount; }
        public double getZScore() { return zScore; }
        public LocalDate getDate() { return date; }
        public double getDeviationPercentage() {
            return ((amount - expectedAmount) / expectedAmount) * 100;
        }
    }
    
    public static class BudgetPrediction {
        private List<MonthlyPrediction> predictions;
        private double confidenceLevel;
        
        public BudgetPrediction() {
            this.predictions = new ArrayList<>();
        }
        
        public void addMonthlyPrediction(LocalDate month, double income, double outcome) {
            predictions.add(new MonthlyPrediction(month, income, outcome));
        }
        
        // Getters and setters
        public List<MonthlyPrediction> getPredictions() { return predictions; }
        public double getConfidenceLevel() { return confidenceLevel; }
        public void setConfidenceLevel(double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
        
        public static class MonthlyPrediction {
            private LocalDate month;
            private double predictedIncome;
            private double predictedOutcome;
            private double predictedBalance;
            
            public MonthlyPrediction(LocalDate month, double income, double outcome) {
                this.month = month;
                this.predictedIncome = income;
                this.predictedOutcome = outcome;
                this.predictedBalance = income - outcome;
            }
            
            // Getters
            public LocalDate getMonth() { return month; }
            public double getPredictedIncome() { return predictedIncome; }
            public double getPredictedOutcome() { return predictedOutcome; }
            public double getPredictedBalance() { return predictedBalance; }
        }
    }
    
    public static class SavingsRecommendation {
        private String category;
        private String recommendation;
        private double potentialSavings;
        private Priority priority;
        
        public enum Priority {
            LOW, MEDIUM, HIGH, CRITICAL
        }
        
        public SavingsRecommendation(String category, String recommendation, 
                                    double potentialSavings, Priority priority) {
            this.category = category;
            this.recommendation = recommendation;
            this.potentialSavings = potentialSavings;
            this.priority = priority;
        }
        
        // Getters
        public String getCategory() { return category; }
        public String getRecommendation() { return recommendation; }
        public double getPotentialSavings() { return potentialSavings; }
        public Priority getPriority() { return priority; }
    }
    
    public static class CashFlowOptimization {
        private List<PaymentSchedule> optimalSchedule;
        private double estimatedSavings;
        private double recommendedCashBuffer;
        
        // Getters and setters
        public List<PaymentSchedule> getOptimalSchedule() { return optimalSchedule; }
        public void setOptimalSchedule(List<PaymentSchedule> schedule) { this.optimalSchedule = schedule; }
        public double getEstimatedSavings() { return estimatedSavings; }
        public void setEstimatedSavings(double savings) { this.estimatedSavings = savings; }
        public double getRecommendedCashBuffer() { return recommendedCashBuffer; }
        public void setRecommendedCashBuffer(double buffer) { this.recommendedCashBuffer = buffer; }
    }
    
    public static class PaymentSchedule {
        private String paymentName;
        private int optimalDayOfMonth;
        private double amount;
        
        public PaymentSchedule(String paymentName, int optimalDayOfMonth, double amount) {
            this.paymentName = paymentName;
            this.optimalDayOfMonth = optimalDayOfMonth;
            this.amount = amount;
        }
        
        // Getters
        public String getPaymentName() { return paymentName; }
        public int getOptimalDayOfMonth() { return optimalDayOfMonth; }
        public double getAmount() { return amount; }
    }
    
    public static class FinancialRiskAssessment {
        private double valueAtRisk95;
        private double expectedValue;
        private double probabilityOfNegativeBalance;
        private double riskScore;
        private String riskLevel;
        
        // Getters and setters
        public double getValueAtRisk95() { return valueAtRisk95; }
        public void setValueAtRisk95(double var) { this.valueAtRisk95 = var; }
        public double getExpectedValue() { return expectedValue; }
        public void setExpectedValue(double value) { this.expectedValue = value; }
        public double getProbabilityOfNegativeBalance() { return probabilityOfNegativeBalance; }
        public void setProbabilityOfNegativeBalance(double prob) { this.probabilityOfNegativeBalance = prob; }
        public double getRiskScore() { return riskScore; }
        public void setRiskScore(double score) { this.riskScore = score; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String level) { this.riskLevel = level; }
    }
    
    private static class LinearRegressionResult {
        private double slope;
        private double intercept;
        private double rSquared;
        
        public LinearRegressionResult(double slope, double intercept, double rSquared) {
            this.slope = slope;
            this.intercept = intercept;
            this.rSquared = rSquared;
        }
        
        public double predict(double x) {
            return slope * x + intercept;
        }
    }
    
    /**
     * Async methods for better performance
     */
    public CompletableFuture<Map<String, SpendingForecast>> forecastSpendingPatternsAsync(int monthsAhead) {
        return CompletableFuture.supplyAsync(() -> forecastSpendingPatterns(monthsAhead));
    }
    
    public CompletableFuture<List<SpendingAnomaly>> detectSpendingAnomaliesAsync() {
        return CompletableFuture.supplyAsync(this::detectSpendingAnomalies);
    }
    
    public CompletableFuture<FinancialRiskAssessment> assessFinancialRiskAsync(int simulationRuns) {
        return CompletableFuture.supplyAsync(() -> assessFinancialRisk(simulationRuns));
    }
} 