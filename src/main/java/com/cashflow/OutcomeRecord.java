package com.cashflow;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Enhanced Outcome Record Model
public class OutcomeRecord {
    private int id;
    private double amount;
    private String title;
    private String category;
    private LocalDate date;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String paymentMethod;
    private boolean isRecurring;

    public OutcomeRecord(int id, double amount, String title, String category, 
                        LocalDate date, String description, LocalDateTime createdAt) {
        this.id = id;
        this.amount = amount;
        this.title = title;
        this.category = category;
        this.date = date;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = LocalDateTime.now();
        this.paymentMethod = "Cash";
        this.isRecurring = false;
    }

    // Enhanced constructor
    public OutcomeRecord(int id, double amount, String title, String category, 
                        LocalDate date, String description, LocalDateTime createdAt, 
                        LocalDateTime updatedAt, String paymentMethod, boolean isRecurring) {
        this(id, amount, title, category, date, description, createdAt);
        this.updatedAt = updatedAt;
        this.paymentMethod = paymentMethod;
        this.isRecurring = isRecurring;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { 
        this.amount = amount; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { 
        this.title = title; 
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
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { 
        this.paymentMethod = paymentMethod; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { 
        isRecurring = recurring; 
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("OutcomeRecord{id=%d, amount=%.2f, title='%s', category='%s', date=%s}", 
                           id, amount, title, category, date);
    }
} 