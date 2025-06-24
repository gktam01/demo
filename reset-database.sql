-- Reset Database Script for Cashflow Application
-- This script will drop existing tables and recreate them with the correct schema

-- Drop existing tables if they exist
DROP TABLE IF EXISTS income_records;
DROP TABLE IF EXISTS outcome_records;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS spending_categories;
DROP TABLE IF EXISTS income_categories;

-- Create income_categories table
CREATE TABLE income_categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    color TEXT DEFAULT '#4CAF50',
    icon TEXT DEFAULT '💰',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create spending_categories table
CREATE TABLE spending_categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    color TEXT DEFAULT '#FF5722',
    icon TEXT DEFAULT '💸',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create income_records table
CREATE TABLE income_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    source TEXT NOT NULL,
    amount REAL NOT NULL,
    category TEXT NOT NULL,
    date DATE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create outcome_records table
CREATE TABLE outcome_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    amount REAL NOT NULL,
    category TEXT NOT NULL,
    date DATE NOT NULL,
    description TEXT,
    payment_method TEXT DEFAULT 'Cash',
    is_recurring BOOLEAN DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default income categories
INSERT INTO income_categories (name, color, icon) VALUES
('Salary', '#4CAF50', '💰'),
('Freelance', '#2196F3', '💼'),
('Investment', '#FF9800', '📈'),
('Business', '#9C27B0', '🏢'),
('Other', '#607D8B', '📋');

-- Insert default spending categories
INSERT INTO spending_categories (name, color, icon) VALUES
('Food', '#FF5722', '🍔'),
('Transportation', '#2196F3', '🚗'),
('Housing', '#4CAF50', '🏠'),
('Utilities', '#FF9800', '⚡'),
('Entertainment', '#9C27B0', '🎬'),
('Healthcare', '#F44336', '🏥'),
('Education', '#3F51B5', '📚'),
('Shopping', '#E91E63', '🛍️'),
('Travel', '#00BCD4', '✈️'),
('Other', '#607D8B', '💸');

-- Insert sample income data
INSERT INTO income_records (source, amount, category, date, description) VALUES
('Monthly Salary', 5000.00, 'Salary', '2024-01-15', 'January salary'),
('Freelance Project', 1500.00, 'Freelance', '2024-01-20', 'Web development project'),
('Stock Dividends', 300.00, 'Investment', '2024-01-25', 'Quarterly dividends'),
('Monthly Salary', 5000.00, 'Salary', '2024-02-15', 'February salary'),
('Freelance Project', 2000.00, 'Freelance', '2024-02-22', 'Mobile app development'),
('Monthly Salary', 5000.00, 'Salary', '2024-03-15', 'March salary'),
('Consulting', 2500.00, 'Business', '2024-03-28', 'Business consulting');

-- Insert sample outcome data
INSERT INTO outcome_records (title, amount, category, date, description, payment_method) VALUES
('Grocery Shopping', 200.00, 'Food', '2024-01-10', 'Weekly groceries', 'Credit Card'),
('Gas Station', 50.00, 'Transportation', '2024-01-12', 'Fuel for car', 'Cash'),
('Rent Payment', 1200.00, 'Housing', '2024-01-15', 'Monthly rent', 'Bank Transfer'),
('Electric Bill', 80.00, 'Utilities', '2024-01-20', 'Electricity bill', 'Online Payment'),
('Movie Tickets', 30.00, 'Entertainment', '2024-01-25', 'Weekend movie', 'Credit Card'),
('Doctor Visit', 150.00, 'Healthcare', '2024-01-28', 'Annual checkup', 'Insurance'),
('Grocery Shopping', 180.00, 'Food', '2024-02-05', 'Weekly groceries', 'Credit Card'),
('Gas Station', 45.00, 'Transportation', '2024-02-08', 'Fuel for car', 'Cash'),
('Rent Payment', 1200.00, 'Housing', '2024-02-15', 'Monthly rent', 'Bank Transfer'),
('Internet Bill', 60.00, 'Utilities', '2024-02-18', 'Internet service', 'Online Payment'),
('Restaurant', 80.00, 'Food', '2024-02-22', 'Dinner with friends', 'Credit Card'),
('Grocery Shopping', 220.00, 'Food', '2024-03-03', 'Weekly groceries', 'Credit Card'),
('Gas Station', 55.00, 'Transportation', '2024-03-06', 'Fuel for car', 'Cash'),
('Rent Payment', 1200.00, 'Housing', '2024-03-15', 'Monthly rent', 'Bank Transfer'),
('Phone Bill', 40.00, 'Utilities', '2024-03-18', 'Mobile phone bill', 'Online Payment'),
('Shopping Mall', 150.00, 'Shopping', '2024-03-25', 'New clothes', 'Credit Card');

-- Create indexes for better performance
CREATE INDEX idx_income_date ON income_records(date);
CREATE INDEX idx_income_category ON income_records(category);
CREATE INDEX idx_outcome_date ON outcome_records(date);
CREATE INDEX idx_outcome_category ON outcome_records(category);

-- Commit all changes
COMMIT; 