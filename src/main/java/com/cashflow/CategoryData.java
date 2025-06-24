package com.cashflow;

public class CategoryData {
    private String category;
    private double totalAmount;
    private double percentage;
    private int transactionCount;

    public CategoryData(String category, double totalAmount, double percentage, int transactionCount) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
        this.transactionCount = transactionCount;
    }

    public CategoryData(String category, double totalAmount) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.percentage = 0;
        this.transactionCount = 0;
    }

    // Getters and Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
} 