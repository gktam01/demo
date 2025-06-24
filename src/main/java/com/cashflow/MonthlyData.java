package com.cashflow;

public class MonthlyData {
    private String month;
    private double income;
    private double outcome;
    private double balance;
    private int year;

    public MonthlyData(String month, double income, double outcome) {
        this.month = month;
        this.income = income;
        this.outcome = outcome;
        this.balance = income - outcome;
        this.year = java.time.LocalDate.now().getYear();
    }

    public MonthlyData(String month, double income, double outcome, int year) {
        this(month, income, outcome);
        this.year = year;
    }

    // Getters and Setters
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public double getIncome() { return income; }
    public void setIncome(double income) { 
        this.income = income; 
        this.balance = this.income - this.outcome;
    }

    public double getOutcome() { return outcome; }
    public void setOutcome(double outcome) { 
        this.outcome = outcome; 
        this.balance = this.income - this.outcome;
    }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
} 