package com.cashflow;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnalyticsService {
    private static final Logger logger = Logger.getLogger(AnalyticsService.class.getName());
    private DatabaseManager dbManager;

    public AnalyticsService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // Getter for dbManager to allow access from subclasses
    protected DatabaseManager getDbManager() {
        return dbManager;
    }

    // Financial Insight Model - Public Static Class
    public static class FinancialInsight {
        private String type;
        private String title;
        private String message;
        private String icon;
        private InsightLevel level;
        private double impact;
        private List<String> recommendations;
        private LocalDate generatedDate;

        public enum InsightLevel {
            INFO, WARNING, ALERT, SUCCESS
        }

        public FinancialInsight(String type, String title, String message, String icon,
                                InsightLevel level, double impact) {
            this.type = type;
            this.title = title;
            this.message = message;
            this.icon = icon;
            this.level = level;
            this.impact = impact;
            this.recommendations = new ArrayList<>();
            this.generatedDate = LocalDate.now();
        }

        // Public Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }

        public InsightLevel getLevel() { return level; }
        public void setLevel(InsightLevel level) { this.level = level; }

        public double getImpact() { return impact; }
        public void setImpact(double impact) { this.impact = impact; }

        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

        public LocalDate getGeneratedDate() { return generatedDate; }
        public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }

        public void addRecommendation(String recommendation) {
            this.recommendations.add(recommendation);
        }
    }

    // Spending Pattern Model - Public Static Class
    public static class SpendingPattern {
        private String category;
        private double averageMonthly;
        private double currentMonth;
        private double changePercentage;
        private TrendDirection trend;
        private List<Double> last6Months;

        public enum TrendDirection {
            INCREASING, DECREASING, STABLE, VOLATILE
        }

        public SpendingPattern(String category) {
            this.category = category;
            this.last6Months = new ArrayList<>();
        }

        // Public Getters and Setters
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public double getAverageMonthly() { return averageMonthly; }
        public void setAverageMonthly(double averageMonthly) { this.averageMonthly = averageMonthly; }

        public double getCurrentMonth() { return currentMonth; }
        public void setCurrentMonth(double currentMonth) { this.currentMonth = currentMonth; }

        public double getChangePercentage() { return changePercentage; }
        public void setChangePercentage(double changePercentage) { this.changePercentage = changePercentage; }

        public TrendDirection getTrend() { return trend; }
        public void setTrend(TrendDirection trend) { this.trend = trend; }

        public List<Double> getLast6Months() { return last6Months; }
        public void setLast6Months(List<Double> last6Months) { this.last6Months = last6Months; }
    }

    // Budget Analysis Model - Public Static Class
    public static class BudgetAnalysis {
        private String category;
        private double budgetLimit;
        private double actualSpent;
        private double percentage;
        private BudgetStatus status;
        private int daysRemaining;
        private double projectedSpending;

        public enum BudgetStatus {
            ON_TRACK, WARNING, OVER_BUDGET, EXCELLENT
        }

        public BudgetAnalysis(String category, double budgetLimit, double actualSpent) {
            this.category = category;
            this.budgetLimit = budgetLimit;
            this.actualSpent = actualSpent;
            this.percentage = budgetLimit > 0 ? (actualSpent / budgetLimit) * 100 : 0;
            this.daysRemaining = LocalDate.now().lengthOfMonth() - LocalDate.now().getDayOfMonth();
            calculateStatus();
            calculateProjectedSpending();
        }

        private void calculateStatus() {
            if (percentage <= 50) {
                status = BudgetStatus.EXCELLENT;
            } else if (percentage <= 80) {
                status = BudgetStatus.ON_TRACK;
            } else if (percentage <= 100) {
                status = BudgetStatus.WARNING;
            } else {
                status = BudgetStatus.OVER_BUDGET;
            }
        }

        private void calculateProjectedSpending() {
            int totalDaysInMonth = LocalDate.now().lengthOfMonth();
            int currentDay = LocalDate.now().getDayOfMonth();
            double dailyAverage = actualSpent / currentDay;
            projectedSpending = dailyAverage * totalDaysInMonth;
        }

        // Public Getters and Setters
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public double getBudgetLimit() { return budgetLimit; }
        public void setBudgetLimit(double budgetLimit) { this.budgetLimit = budgetLimit; }

        public double getActualSpent() { return actualSpent; }
        public void setActualSpent(double actualSpent) { this.actualSpent = actualSpent; }

        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }

        public BudgetStatus getStatus() { return status; }
        public void setStatus(BudgetStatus status) { this.status = status; }

        public int getDaysRemaining() { return daysRemaining; }
        public void setDaysRemaining(int daysRemaining) { this.daysRemaining = daysRemaining; }

        public double getProjectedSpending() { return projectedSpending; }
        public void setProjectedSpending(double projectedSpending) { this.projectedSpending = projectedSpending; }
    }

    // Financial Goal Model - Public Static Class
    public static class FinancialGoal {
        private String name;
        private double targetAmount;
        private double currentAmount;
        private LocalDate targetDate;
        private GoalType type;
        private GoalStatus status;
        private double monthlyRequired;

        public enum GoalType {
            SAVINGS, DEBT_PAYOFF, INVESTMENT, EMERGENCY_FUND, VACATION, OTHER
        }

        public enum GoalStatus {
            ON_TRACK, BEHIND, AHEAD, COMPLETED
        }

        public FinancialGoal(String name, double targetAmount, double currentAmount, LocalDate targetDate, GoalType type) {
            this.name = name;
            this.targetAmount = targetAmount;
            this.currentAmount = currentAmount;
            this.targetDate = targetDate;
            this.type = type;
            calculateStatus();
            calculateMonthlyRequired();
        }

        private void calculateStatus() {
            double progress = getProgressPercentage();
            if (progress >= 100) {
                status = GoalStatus.COMPLETED;
            } else if (progress >= 80) {
                status = GoalStatus.AHEAD;
            } else if (progress >= 50) {
                status = GoalStatus.ON_TRACK;
            } else {
                status = GoalStatus.BEHIND;
            }
        }

        private void calculateMonthlyRequired() {
            long monthsRemaining = java.time.temporal.ChronoUnit.MONTHS.between(LocalDate.now(), targetDate);
            if (monthsRemaining > 0) {
                monthlyRequired = (targetAmount - currentAmount) / monthsRemaining;
            } else {
                monthlyRequired = 0;
            }
        }

        // Public Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public double getTargetAmount() { return targetAmount; }
        public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

        public double getCurrentAmount() { return currentAmount; }
        public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }

        public LocalDate getTargetDate() { return targetDate; }
        public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

        public GoalType getType() { return type; }
        public void setType(GoalType type) { this.type = type; }

        public GoalStatus getStatus() { return status; }
        public void setStatus(GoalStatus status) { this.status = status; }

        public double getMonthlyRequired() { return monthlyRequired; }
        public void setMonthlyRequired(double monthlyRequired) { this.monthlyRequired = monthlyRequired; }

        public double getProgressPercentage() {
            return targetAmount > 0 ? (currentAmount / targetAmount) * 100 : 0;
        }
    }

    // Main Analytics Methods
    public List<FinancialInsight> generateFinancialInsights() {
        try {
            List<FinancialInsight> insights = new ArrayList<>();
            DashboardStats stats = dbManager.getDashboardStats();

            // Analyze spending patterns
            insights.addAll(analyzeSpendingPatterns(stats));

            // Analyze income patterns
            insights.addAll(analyzeIncomePatterns(stats));

            // Analyze savings rate
            insights.addAll(analyzeSavingsRate(stats));

            // Analyze budget performance
            insights.addAll(analyzeBudgetPerformance());

            // Analyze overall financial health
            insights.addAll(analyzeFinancialHealth(stats));

            return insights;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating financial insights", e);
            return List.of(createErrorInsight());
        }
    }

    private List<FinancialInsight> analyzeSpendingPatterns(DashboardStats stats) {
        List<FinancialInsight> insights = new ArrayList<>();

        try {
            // Analyze spending trends
            if (stats.getMonthlyOutcome() > stats.getTotalOutcome() / 12 * 1.2) {
                insights.add(new FinancialInsight(
                    "spending", "High Spending Alert", 
                    "Your spending this month is 20% higher than average. Consider reviewing your expenses.",
                    "⚠️", FinancialInsight.InsightLevel.WARNING, 0.8
                ));
            }

            // Analyze category spending
            List<CategoryData> topCategories = dbManager.getTopExpenseCategories(3);
            if (!topCategories.isEmpty()) {
                CategoryData topCategory = topCategories.get(0);
                if (topCategory.getPercentage() > 40) {
                    insights.add(new FinancialInsight(
                        "category", "Category Concentration", 
                        topCategory.getCategory() + " accounts for " + String.format("%.1f", topCategory.getPercentage()) + "% of your spending.",
                        "📊", FinancialInsight.InsightLevel.INFO, 0.6
                    ));
                }
            }

            // Analyze savings rate
            if (stats.getSavingsRate() < 10) {
                insights.add(new FinancialInsight(
                    "savings", "Low Savings Rate", 
                    "Your savings rate is below 10%. Consider increasing your savings.",
                    "💰", FinancialInsight.InsightLevel.ALERT, 0.9
                ));
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error analyzing spending patterns", e);
        }

        return insights;
    }

    private List<FinancialInsight> analyzeIncomePatterns(DashboardStats stats) {
        List<FinancialInsight> insights = new ArrayList<>();

        try {
            // Analyze income stability
            if (stats.getMonthlyIncome() > 0) {
                double incomeGrowth = ((stats.getMonthlyIncome() - stats.getTotalIncome() / 12) / (stats.getTotalIncome() / 12)) * 100;
                
                if (incomeGrowth > 15) {
                    insights.add(new FinancialInsight(
                        "income", "Income Growth", 
                        "Great job! Your income has increased by " + String.format("%.1f", incomeGrowth) + "% this month.",
                        "📈", FinancialInsight.InsightLevel.SUCCESS, 0.7
                    ));
                } else if (incomeGrowth < -10) {
                    insights.add(new FinancialInsight(
                        "income", "Income Decline", 
                        "Your income has decreased by " + String.format("%.1f", Math.abs(incomeGrowth)) + "% this month.",
                        "📉", FinancialInsight.InsightLevel.WARNING, 0.8
                    ));
                }
            }

            // Analyze income sources
            List<CategoryData> topIncomeCategories = dbManager.getTopIncomeCategories(3);
            if (!topIncomeCategories.isEmpty()) {
                CategoryData topCategory = topIncomeCategories.get(0);
                if (topCategory.getPercentage() > 80) {
                    insights.add(new FinancialInsight(
                        "income", "Income Diversification", 
                        "Consider diversifying your income sources. " + topCategory.getCategory() + " accounts for " + String.format("%.1f", topCategory.getPercentage()) + "% of your income.",
                        "🎯", FinancialInsight.InsightLevel.INFO, 0.5
                    ));
                }
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error analyzing income patterns", e);
        }

        return insights;
    }

    private List<FinancialInsight> analyzeSavingsRate(DashboardStats stats) {
        List<FinancialInsight> insights = new ArrayList<>();

        try {
            double savingsRate = stats.getSavingsRate();

            if (savingsRate >= 20) {
                insights.add(new FinancialInsight(
                    "savings", "Excellent Savings", 
                    "Outstanding! You're saving " + String.format("%.1f", savingsRate) + "% of your income. Keep up the great work!",
                    "🏆", FinancialInsight.InsightLevel.SUCCESS, 0.9
                ));
            } else if (savingsRate >= 10) {
                insights.add(new FinancialInsight(
                    "savings", "Good Savings", 
                    "Good job! You're saving " + String.format("%.1f", savingsRate) + "% of your income. Consider increasing to 20% for better financial security.",
                    "👍", FinancialInsight.InsightLevel.SUCCESS, 0.7
                ));
            } else if (savingsRate >= 5) {
                insights.add(new FinancialInsight(
                    "savings", "Moderate Savings", 
                    "You're saving " + String.format("%.1f", savingsRate) + "% of your income. Try to increase this to at least 10%.",
                    "📊", FinancialInsight.InsightLevel.WARNING, 0.6
                ));
            } else {
                insights.add(new FinancialInsight(
                    "savings", "Low Savings", 
                    "Your savings rate is " + String.format("%.1f", savingsRate) + "%. Consider creating a budget to increase your savings.",
                    "💡", FinancialInsight.InsightLevel.ALERT, 0.8
                ));
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error analyzing savings rate", e);
        }

        return insights;
    }

    private List<FinancialInsight> analyzeBudgetPerformance() {
        List<FinancialInsight> insights = new ArrayList<>();

        try {
            // This would analyze budget vs actual spending
            // For now, create a generic insight
            insights.add(new FinancialInsight(
                "budget", "Budget Tracking", 
                "Consider setting up monthly budgets for better financial control.",
                "📋", FinancialInsight.InsightLevel.INFO, 0.5
            ));

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error analyzing budget performance", e);
        }

        return insights;
    }

    private List<FinancialInsight> analyzeFinancialHealth(DashboardStats stats) {
        List<FinancialInsight> insights = new ArrayList<>();

        try {
            double balance = stats.getBalance();
            double totalIncome = stats.getTotalIncome();

            if (balance > 0 && totalIncome > 0) {
                double emergencyFundRatio = balance / (stats.getMonthlyOutcome() * 3);
                
                if (emergencyFundRatio >= 1) {
                    insights.add(new FinancialInsight(
                        "health", "Emergency Fund", 
                        "Excellent! You have a solid emergency fund covering " + String.format("%.1f", emergencyFundRatio) + " months of expenses.",
                        "🛡️", FinancialInsight.InsightLevel.SUCCESS, 0.9
                    ));
                } else if (emergencyFundRatio >= 0.5) {
                    insights.add(new FinancialInsight(
                        "health", "Emergency Fund", 
                        "You have " + String.format("%.1f", emergencyFundRatio) + " months of emergency fund. Aim for 3-6 months.",
                        "📊", FinancialInsight.InsightLevel.WARNING, 0.6
                    ));
                } else {
                    insights.add(new FinancialInsight(
                        "health", "Emergency Fund", 
                        "Consider building an emergency fund. You currently have " + String.format("%.1f", emergencyFundRatio) + " months covered.",
                        "⚠️", FinancialInsight.InsightLevel.ALERT, 0.8
                    ));
                }
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error analyzing financial health", e);
        }

        return insights;
    }

    private FinancialInsight createErrorInsight() {
        return new FinancialInsight(
            "error", "Analysis Error", 
            "Unable to generate insights at this time. Please try again later.",
            "❌", FinancialInsight.InsightLevel.WARNING, 0.0
        );
    }

    public List<SpendingPattern> analyzeSpendingPatterns() {
        List<SpendingPattern> patterns = new ArrayList<>();

        try {
            List<CategoryData> categories = dbManager.getTopExpenseCategories(5);
            
            for (CategoryData category : categories) {
                SpendingPattern pattern = new SpendingPattern(category.getCategory());
                pattern.setAverageMonthly(category.getTotalAmount() / 12);
                pattern.setCurrentMonth(category.getTotalAmount());
                pattern.setChangePercentage(5.0); // Mock data
                pattern.setTrend(SpendingPattern.TrendDirection.STABLE);
                
                // Mock historical data
                List<Double> historical = Arrays.asList(100.0, 120.0, 110.0, 130.0, 125.0, 115.0);
                pattern.setLast6Months(historical);
                
                patterns.add(pattern);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error analyzing spending patterns", e);
        }

        return patterns;
    }

    public List<BudgetAnalysis> analyzeBudgets() {
        List<BudgetAnalysis> analyses = new ArrayList<>();

        try {
            List<CategoryData> categories = dbManager.getTopExpenseCategories(5);
            
            for (CategoryData category : categories) {
                double budgetLimit = category.getTotalAmount() * 1.2;
                BudgetAnalysis analysis = new BudgetAnalysis(category.getCategory(), budgetLimit, category.getTotalAmount());
                analyses.add(analysis);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error analyzing budgets", e);
        }

        return analyses;
    }

    public List<FinancialGoal> analyzeFinancialGoals() {
        List<FinancialGoal> goals = new ArrayList<>();

        try {
            // Mock financial goals
            goals.add(new FinancialGoal("Emergency Fund", 10000.0, 5000.0, 
                LocalDate.now().plusMonths(6), FinancialGoal.GoalType.EMERGENCY_FUND));
            
            goals.add(new FinancialGoal("Vacation Fund", 5000.0, 2000.0, 
                LocalDate.now().plusMonths(12), FinancialGoal.GoalType.VACATION));
            
            goals.add(new FinancialGoal("Investment Portfolio", 20000.0, 15000.0, 
                LocalDate.now().plusMonths(18), FinancialGoal.GoalType.INVESTMENT));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error analyzing financial goals", e);
        }

        return goals;
    }

    public Map<String, Double> forecastIncome(int months) {
        Map<String, Double> forecast = new HashMap<>();

        try {
            DashboardStats stats = dbManager.getDashboardStats();
            double currentIncome = stats.getMonthlyIncome();
            
            for (int i = 1; i <= months; i++) {
                String monthKey = LocalDate.now().plusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM"));
                double forecastedIncome = currentIncome * (1 + (i * 0.02)); // 2% monthly growth
                forecast.put(monthKey, forecastedIncome);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error forecasting income", e);
        }

        return forecast;
    }

    public Map<String, Double> forecastExpenses(int months) {
        Map<String, Double> forecast = new HashMap<>();

        try {
            DashboardStats stats = dbManager.getDashboardStats();
            double currentExpenses = stats.getMonthlyOutcome();
            
            for (int i = 1; i <= months; i++) {
                String monthKey = LocalDate.now().plusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM"));
                double forecastedExpenses = currentExpenses * (1 + (i * 0.01)); // 1% monthly growth
                forecast.put(monthKey, forecastedExpenses);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error forecasting expenses", e);
        }

        return forecast;
    }

    public double calculateFinancialScore() {
        try {
            DashboardStats stats = dbManager.getDashboardStats();
            double score = 0.0;

            // Savings rate (30% weight)
            double savingsRate = stats.getSavingsRate();
            if (savingsRate >= 20) score += 30;
            else if (savingsRate >= 15) score += 25;
            else if (savingsRate >= 10) score += 20;
            else if (savingsRate >= 5) score += 15;
            else score += 10;

            // Income stability (25% weight)
            double incomeStability = calculateIncomeStability();
            score += incomeStability * 25;

            // Expense control (25% weight)
            double expenseControl = calculateExpenseControl(stats);
            score += expenseControl * 25;

            // Emergency fund (20% weight)
            double emergencyFundScore = calculateEmergencyFundScore(stats);
            score += emergencyFundScore * 20;

            return Math.min(100, Math.max(0, score));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating financial score", e);
            return 50.0; // Default score
        }
    }

    private double calculateIncomeStability() {
        try {
            // Simplified calculation - in real app, would analyze historical data
            return 0.8; // 80% stability
        } catch (Exception e) {
            return 0.5;
        }
    }

    private double calculateExpenseControl(DashboardStats stats) {
        try {
            double monthlyOutcome = stats.getMonthlyOutcome();
            double totalIncome = stats.getTotalIncome();
            
            if (totalIncome > 0) {
                double expenseRatio = monthlyOutcome / totalIncome;
                if (expenseRatio <= 0.5) return 1.0;
                else if (expenseRatio <= 0.7) return 0.8;
                else if (expenseRatio <= 0.9) return 0.6;
                else return 0.4;
            }
            return 0.5;
        } catch (Exception e) {
            return 0.5;
        }
    }

    private double calculateEmergencyFundScore(DashboardStats stats) {
        try {
            double balance = stats.getBalance();
            double monthlyOutcome = stats.getMonthlyOutcome();
            
            if (monthlyOutcome > 0) {
                double monthsCovered = balance / monthlyOutcome;
                if (monthsCovered >= 6) return 1.0;
                else if (monthsCovered >= 3) return 0.8;
                else if (monthsCovered >= 1) return 0.6;
                else return 0.3;
            }
            return 0.5;
        } catch (Exception e) {
            return 0.5;
        }
    }

    public List<String> generateRecommendations() {
        List<String> recommendations = new ArrayList<>();

        try {
            DashboardStats stats = dbManager.getDashboardStats();
            double savingsRate = stats.getSavingsRate();

            if (savingsRate < 10) {
                recommendations.add("Increase your savings rate to at least 10% of your income");
                recommendations.add("Create a monthly budget to track your expenses");
                recommendations.add("Look for ways to reduce non-essential spending");
            }

            if (stats.getBalance() < stats.getMonthlyOutcome() * 3) {
                recommendations.add("Build an emergency fund covering 3-6 months of expenses");
                recommendations.add("Set up automatic savings transfers");
            }

            List<CategoryData> topExpenses = dbManager.getTopExpenseCategories(3);
            if (!topExpenses.isEmpty() && topExpenses.get(0).getPercentage() > 40) {
                recommendations.add("Diversify your spending across different categories");
                recommendations.add("Review your spending in " + topExpenses.get(0).getCategory());
            }

            if (recommendations.isEmpty()) {
                recommendations.add("Continue maintaining your current financial habits");
                recommendations.add("Consider setting up long-term financial goals");
                recommendations.add("Explore investment opportunities for your savings");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating recommendations", e);
            recommendations.add("Unable to generate recommendations at this time");
        }

        return recommendations;
    }

    private double calculateVariability(double[] data) {
        if (data.length < 2) return 0.0;
        
        double mean = Arrays.stream(data).average().orElse(0.0);
        double variance = Arrays.stream(data)
            .map(x -> Math.pow(x - mean, 2))
            .average()
            .orElse(0.0);
        
        return Math.sqrt(variance) / mean; // Coefficient of variation
    }

    private double calculateTrend(double[] data) {
        if (data.length < 2) return 0.0;
        
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = data.length;
        
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += data[i];
            sumXY += i * data[i];
            sumX2 += i * i;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        return slope;
    }

    public Map<String, Object> getFinancialHealthSummary() {
        Map<String, Object> summary = new HashMap<>();

        try {
            DashboardStats stats = dbManager.getDashboardStats();
            double financialScore = calculateFinancialScore();

            summary.put("financialScore", financialScore);
            summary.put("grade", getGradeFromScore(financialScore));
            summary.put("savingsRate", stats.getSavingsRate());
            summary.put("balance", stats.getBalance());
            summary.put("monthlyIncome", stats.getMonthlyIncome());
            summary.put("monthlyOutcome", stats.getMonthlyOutcome());
            summary.put("recommendations", generateRecommendations());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting financial health summary", e);
            summary.put("error", "Unable to generate financial health summary");
        }

        return summary;
    }

    private String getGradeFromScore(double score) {
        if (score >= 90) return "A+";
        else if (score >= 80) return "A";
        else if (score >= 70) return "B+";
        else if (score >= 60) return "B";
        else if (score >= 50) return "C+";
        else if (score >= 40) return "C";
        else if (score >= 30) return "D";
        else return "F";
    }
}