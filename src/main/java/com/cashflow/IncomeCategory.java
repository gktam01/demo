package com.cashflow;

public class IncomeCategory {
    private String category;
    private double amount;
    private double percentage;

    public IncomeCategory(String category, double amount, double percentage) {
        this.category = category;
        this.amount = amount;
        this.percentage = percentage;
    }

    // Getters and Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
} 