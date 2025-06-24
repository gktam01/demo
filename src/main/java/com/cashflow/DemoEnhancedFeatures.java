package com.cashflow;

import java.time.LocalDate;
import java.util.List;

/**
 * Demo class để minh họa các tính năng mới của package com.cashflow
 */
public class DemoEnhancedFeatures {
    
    public static void main(String[] args) {
        System.out.println("🚀 Demo Enhanced Cashflow Features");
        System.out.println("=====================================\n");
        
        try {
            DatabaseManager dbManager = new DatabaseManager();
            
            // Demo enhanced features
            demoEnhancedDatabaseOperations(dbManager);
            demoEnhancedAnalytics(dbManager);
            demoFinancialHealthAnalysis(dbManager);
            
            System.out.println("\n✅ Demo hoàn thành thành công!");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void demoEnhancedDatabaseOperations(DatabaseManager dbManager) {
        System.out.println("📊 Demo 1: Enhanced Database Operations");
        System.out.println("----------------------------------------");
        
        // Add income with enhanced validation
        boolean incomeAdded = dbManager.addIncome(5000.0, "Tech Company Ltd", "Salary", 
                                                 LocalDate.now(), "Monthly salary");
        System.out.println("✅ Income added: " + incomeAdded);
        
        // Add outcome with payment method and recurring flag
        boolean outcomeAdded = dbManager.addOutcome(100.0, "Netflix Subscription", "Entertainment", 
                                                   LocalDate.now(), "Monthly subscription", "Credit Card", true);
        System.out.println("✅ Outcome added: " + outcomeAdded);
        
        // Get enhanced dashboard stats
        DashboardStats stats = dbManager.getDashboardStats();
        System.out.println("💰 Total Income: $" + String.format("%.2f", stats.getTotalIncome()));
        System.out.println("💸 Total Outcome: $" + String.format("%.2f", stats.getTotalOutcome()));
        System.out.println("💳 Balance: $" + String.format("%.2f", stats.getBalance()));
        System.out.println("📈 Savings Rate: " + String.format("%.1f", stats.getSavingsRate()) + "%");
        
        System.out.println();
    }
    
    private static void demoEnhancedAnalytics(DatabaseManager dbManager) {
        System.out.println("🔍 Demo 2: Enhanced Analytics");
        System.out.println("-----------------------------");
        
        // Get enhanced monthly chart data
        List<MonthlyData> monthlyData = dbManager.getEnhancedMonthlyChartData();
        System.out.println("📅 Monthly data points: " + monthlyData.size());
        
        // Get top categories
        List<CategoryData> topIncomeCategories = dbManager.getTopIncomeCategories(3);
        List<CategoryData> topExpenseCategories = dbManager.getTopExpenseCategories(3);
        
        System.out.println("🏆 Top Income Category: " + 
            (topIncomeCategories.isEmpty() ? "N/A" : topIncomeCategories.get(0).getCategory()));
        System.out.println("🏆 Top Expense Category: " + 
            (topExpenseCategories.isEmpty() ? "N/A" : topExpenseCategories.get(0).getCategory()));
        
        System.out.println();
    }
    
    private static void demoFinancialHealthAnalysis(DatabaseManager dbManager) {
        System.out.println("🏥 Demo 3: Financial Health Analysis");
        System.out.println("------------------------------------");
        
        // Get categories
        List<String> incomeCategories = dbManager.getIncomeCategories();
        List<String> outcomeCategories = dbManager.getOutcomeCategories();
        
        System.out.println("📋 Available Income Categories: " + incomeCategories.size());
        System.out.println("📋 Available Outcome Categories: " + outcomeCategories.size());
        
        // Database maintenance
        System.out.println("🔧 Optimizing database...");
        dbManager.optimizeDatabase();
        System.out.println("✅ Database optimization completed");
        
        System.out.println();
    }
} 