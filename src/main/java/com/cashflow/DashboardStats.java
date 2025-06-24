package com.cashflow;

import java.util.ArrayList;
import java.util.List;

// Enhanced Dashboard Stats Model
public class DashboardStats {
    private double totalIncome;
    private double totalOutcome;
    private double monthlyIncome;
    private double monthlyOutcome;
    private double weeklyIncome;
    private double weeklyOutcome;
    private double balance;
    private double savingsRate;
    private List<MonthlyData> monthlyData;
    private List<CategoryData> topIncomeCategories;
    private List<CategoryData> topExpenseCategories;

    public DashboardStats() {
        this.monthlyData = new ArrayList<>();
        this.topIncomeCategories = new ArrayList<>();
        this.topExpenseCategories = new ArrayList<>();
    }

    // Getters and Setters
    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }

    public double getTotalOutcome() { return totalOutcome; }
    public void setTotalOutcome(double totalOutcome) { this.totalOutcome = totalOutcome; }

    public double getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(double monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public double getMonthlyOutcome() { return monthlyOutcome; }
    public void setMonthlyOutcome(double monthlyOutcome) { this.monthlyOutcome = monthlyOutcome; }

    public double getWeeklyIncome() { return weeklyIncome; }
    public void setWeeklyIncome(double weeklyIncome) { this.weeklyIncome = weeklyIncome; }

    public double getWeeklyOutcome() { return weeklyOutcome; }
    public void setWeeklyOutcome(double weeklyOutcome) { this.weeklyOutcome = weeklyOutcome; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public double getSavingsRate() { return savingsRate; }
    public void setSavingsRate(double savingsRate) { this.savingsRate = savingsRate; }

    public List<MonthlyData> getMonthlyData() { return monthlyData; }
    public void setMonthlyData(List<MonthlyData> monthlyData) { this.monthlyData = monthlyData; }

    public List<CategoryData> getTopIncomeCategories() { return topIncomeCategories; }
    public void setTopIncomeCategories(List<CategoryData> topIncomeCategories) { this.topIncomeCategories = topIncomeCategories; }

    public List<CategoryData> getTopExpenseCategories() { return topExpenseCategories; }
    public void setTopExpenseCategories(List<CategoryData> topExpenseCategories) { this.topExpenseCategories = topExpenseCategories; }
} 