package com.cashflow;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Enhanced Income Record Model
public class IncomeRecord {
    private int id;
    private double amount;
    private String source;
    private String category;
    private LocalDate date;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public IncomeRecord(int id, double amount, String source, String category, 
                       LocalDate date, String description, LocalDateTime createdAt) {
        this.id = id;
        this.amount = amount;
        this.source = source;
        this.category = category;
        this.date = date;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = LocalDateTime.now();
    }

    // Enhanced constructor with updatedAt
    public IncomeRecord(int id, double amount, String source, String category, 
                       LocalDate date, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, amount, source, category, date, description, createdAt);
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { 
        this.amount = amount; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getSource() { return source; }
    public void setSource(String source) { 
        this.source = source; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { 
        this.category = category; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { 
        this.date = date; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return String.format("IncomeRecord{id=%d, amount=%.2f, source='%s', category='%s', date=%s}", 
                           id, amount, source, category, date);
    }
} 