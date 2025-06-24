package com.cashflow;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DatabaseManager implements AutoCloseable {
    private static final String DB_URL = "jdbc:sqlite:cashflow.db";
    private static final String DB_VERSION = "1.2";
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());

    private Connection connection;

    public DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
                stmt.execute("PRAGMA journal_mode = WAL;");
            }
            initializeDatabase();
            
            // Thêm dữ liệu mẫu cho tháng hiện tại nếu chưa có
            if (getMonthlyIncome(LocalDate.now()) == 0) {
                addCurrentMonthSampleData();
            }
            
            logger.info("Database connection established and initialized successfully.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to or initialize database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void initializeDatabase() {
        try {
            createTablesIfNotExists(connection);
            insertSampleDataIfNeeded(connection);
            updateDatabaseVersion(connection);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database initialization error", e);
            throw new RuntimeException("Failed to initialize database during table/data creation", e);
        }
    }

    private void createTablesIfNotExists(Connection conn) throws SQLException {
        String createIncomeTable = """
            CREATE TABLE IF NOT EXISTS income (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount REAL NOT NULL CHECK(amount > 0),
                source TEXT NOT NULL,
                category TEXT NOT NULL,
                date DATE NOT NULL,
                description TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        
        String createOutcomeTable = """
            CREATE TABLE IF NOT EXISTS outcome (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount REAL NOT NULL CHECK(amount > 0),
                title TEXT NOT NULL,
                category TEXT NOT NULL,
                date DATE NOT NULL,
                description TEXT,
                payment_method TEXT DEFAULT 'Cash',
                is_recurring BOOLEAN DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createCategoriesTable = """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                type TEXT NOT NULL CHECK(type IN ('income', 'outcome')),
                color TEXT DEFAULT '#4ECDC4',
                icon TEXT DEFAULT '💰',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createBudgetsTable = """
            CREATE TABLE IF NOT EXISTS budgets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT NOT NULL,
                monthly_limit REAL NOT NULL CHECK(monthly_limit > 0),
                current_spent REAL DEFAULT 0,
                year INTEGER NOT NULL,
                month INTEGER NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(category, year, month)
            )
        """;

        String createSettingsTable = """
            CREATE TABLE IF NOT EXISTS settings (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createIncomeTable);
            stmt.execute(createOutcomeTable);
            stmt.execute(createCategoriesTable);
            stmt.execute(createBudgetsTable);
            stmt.execute(createSettingsTable);
            
            createIndexes(stmt);
        }
    }

    private void createIndexes(Statement stmt) throws SQLException {
        String[] indexes = {
            "CREATE INDEX IF NOT EXISTS idx_income_date ON income(date)",
            "CREATE INDEX IF NOT EXISTS idx_income_category ON income(category)",
            "CREATE INDEX IF NOT EXISTS idx_outcome_date ON outcome(date)",
            "CREATE INDEX IF NOT EXISTS idx_outcome_category ON outcome(category)",
            "CREATE INDEX IF NOT EXISTS idx_budgets_category_date ON budgets(category, year, month)"
        };

        for (String index : indexes) {
            stmt.execute(index);
        }
    }

    private void insertSampleDataIfNeeded(Connection conn) throws SQLException {
        if (getIncomeRecordsCount() == 0) {
            insertSampleIncomeData(conn);
        }

        if (getOutcomeRecordsCount() == 0) {
            insertSampleOutcomeData(conn);
        }

        insertDefaultCategories(conn);
    }

    private void insertDefaultCategories(Connection conn) throws SQLException {
        String insertCategory = """
            INSERT OR IGNORE INTO categories (name, type, color, icon) VALUES (?, ?, ?, ?)
        """;
        
        Object[][] defaultCategories = {
            {"Salary", "income", "#4CAF50", "💼"},
            {"Freelance", "income", "#2196F3", "💻"},
            {"Investment", "income", "#FF9800", "📈"},
            {"Business", "income", "#9C27B0", "🏢"},
            {"Bonus", "income", "#E91E63", "🎁"},
            {"Gift", "income", "#F44336", "🎀"},
            
            {"Food", "outcome", "#FF5722", "🍔"},
            {"Transportation", "outcome", "#607D8B", "🚗"},
            {"Housing", "outcome", "#795548", "🏠"},
            {"Utilities", "outcome", "#FFC107", "⚡"},
            {"Entertainment", "outcome", "#8BC34A", "🎬"},
            {"Healthcare", "outcome", "#00BCD4", "🏥"},
            {"Education", "outcome", "#3F51B5", "📚"},
            {"Shopping", "outcome", "#E91E63", "🛒"},
            {"Travel", "outcome", "#009688", "✈️"},
            {"Groceries", "outcome", "#4CAF50", "🛒"}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(insertCategory)) {
            for (Object[] row : defaultCategories) {
                pstmt.setString(1, (String) row[0]);
                pstmt.setString(2, (String) row[1]);
                pstmt.setString(3, (String) row[2]);
                pstmt.setString(4, (String) row[3]);
                pstmt.executeUpdate();
            }
        }
    }

    private void insertSampleIncomeData(Connection conn) throws SQLException {
        String insertIncome = """
            INSERT INTO income (amount, source, category, date, description) VALUES (?, ?, ?, ?, ?)
        """;
        
        Object[][] sampleIncome = {
            {5000.0, "Tech Company Ltd", "Salary", "2024-12-01", "Monthly salary"},
            {800.0, "Freelance Project", "Freelance", "2024-12-05", "Website development"},
            {200.0, "Stock Dividends", "Investment", "2024-12-10", "Quarterly dividends"},
            {1500.0, "Bonus Payment", "Bonus", "2024-12-15", "Year-end bonus"},
            {300.0, "Side Business", "Business", "2024-12-20", "Online store sales"},
            {5000.0, "Tech Company Ltd", "Salary", "2024-11-01", "Monthly salary"},
            {600.0, "Consulting Work", "Freelance", "2024-11-15", "Business consultation"},
            {150.0, "Investment Returns", "Investment", "2024-11-20", "Mutual fund returns"},
            {4800.0, "Tech Company Ltd", "Salary", "2024-10-01", "Monthly salary"},
            {400.0, "Stock Trading", "Investment", "2024-10-12", "Day trading profits"}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(insertIncome)) {
            for (Object[] row : sampleIncome) {
                pstmt.setDouble(1, (Double) row[0]);
                pstmt.setString(2, (String) row[1]);
                pstmt.setString(3, (String) row[2]);
                pstmt.setString(4, (String) row[3]);
                pstmt.setString(5, (String) row[4]);
                pstmt.executeUpdate();
            }
        }
    }

    private void insertSampleOutcomeData(Connection conn) throws SQLException {
        String insertOutcome = """
            INSERT INTO outcome (amount, title, category, date, description, payment_method) VALUES (?, ?, ?, ?, ?, ?)
        """;
        
        Object[][] sampleOutcome = {
            {20.0, "Cinema XXI Ticket Avenger 2P", "Entertainment", "2024-12-21", "Movie tickets", "Credit Card"},
            {1400.0, "Electricity Token", "Utilities", "2024-12-12", "Monthly electricity", "Bank Transfer"},
            {500.0, "Grocery Shopping", "Groceries", "2024-12-18", "Weekly groceries", "Cash"},
            {200.0, "Gas Station", "Transportation", "2024-12-19", "Car fuel", "Credit Card"},
            {100.0, "Netflix Subscription", "Entertainment", "2024-12-01", "Monthly subscription", "Credit Card"},
            {1200.0, "Rent Payment", "Housing", "2024-12-01", "Monthly rent", "Bank Transfer"},
            {300.0, "Restaurant Dinner", "Food", "2024-12-14", "Date night dinner", "Credit Card"},
            {80.0, "Coffee & Lunch", "Food", "2024-12-20", "Daily meals", "Cash"},
            {150.0, "Internet Bill", "Utilities", "2024-12-05", "Monthly internet", "Bank Transfer"},
            {250.0, "Clothing Shopping", "Shopping", "2024-12-10", "Winter clothes", "Credit Card"}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(insertOutcome)) {
            for (Object[] row : sampleOutcome) {
                pstmt.setDouble(1, (Double) row[0]);
                pstmt.setString(2, (String) row[1]);
                pstmt.setString(3, (String) row[2]);
                pstmt.setString(4, (String) row[3]);
                pstmt.setString(5, (String) row[4]);
                pstmt.setString(6, (String) row[5]);
                pstmt.executeUpdate();
            }
        }
    }

    private void updateDatabaseVersion(Connection conn) throws SQLException {
        String updateVersion = """
            INSERT OR REPLACE INTO settings (key, value) VALUES ('db_version', ?)
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(updateVersion)) {
            pstmt.setString(1, DB_VERSION);
            pstmt.executeUpdate();
        }
    }

    public boolean addIncome(double amount, String source, String category, LocalDate date, String description) {
        if (amount <= 0) {
            System.err.println("Invalid income amount: " + amount);
            return false;
        }
        
        if (source == null || source.trim().isEmpty()) {
            System.err.println("Invalid income source: " + source);
            return false;
        }

        String sql = "INSERT INTO income (amount, source, category, date, description) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, source.trim());
            pstmt.setString(3, category != null ? category.trim() : "Other");
            pstmt.setString(4, date != null ? date.toString() : LocalDate.now().toString());
            pstmt.setString(5, description != null ? description.trim() : "");
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                System.out.println("✓ Income added successfully: " + source + " - $" + amount);
                logger.info("Income added successfully: " + source + " - $" + amount);
                return true;
            } else {
                System.err.println("✗ Failed to add income - no rows affected");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("✗ SQL Error adding income: " + e.getMessage());
            logger.log(Level.SEVERE, "Failed to add income: " + source, e);
            e.printStackTrace();
            return false;
        }
    }

    public List<IncomeRecord> getIncomeRecords() {
        return getIncomeRecords(100);
    }

    public List<IncomeRecord> getIncomeRecords(int limit) {
        List<IncomeRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM income ORDER BY date DESC, id DESC LIMIT ?";
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                records.add(new IncomeRecord(
                    rs.getInt("id"),
                    rs.getDouble("amount"),
                    rs.getString("source"),
                    rs.getString("category"),
                    LocalDate.parse(rs.getString("date")),
                    rs.getString("description"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getTimestamp("updated_at") != null ? 
                        rs.getTimestamp("updated_at").toLocalDateTime() : null
                ));
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get income records", e);
        }
        
        return records;
    }

    public List<IncomeRecord> getIncomeRecordsByCategory(String category) {
        List<IncomeRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM income WHERE category = ? ORDER BY date DESC";
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                records.add(new IncomeRecord(
                    rs.getInt("id"),
                    rs.getDouble("amount"),
                    rs.getString("source"),
                    rs.getString("category"),
                    LocalDate.parse(rs.getString("date")),
                    rs.getString("description"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get income records by category: " + category, e);
        }
        
        return records;
    }

    // Enhanced analytics methods
    public double getMonthlyIncome(LocalDate date) {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) as total
            FROM income
            WHERE strftime('%Y-%m', date) = strftime('%Y-%m', ?)
        """;
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.setString(1, date.toString());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double result = rs.getDouble("total");
                System.out.println("Month " + date.getMonthValue() + "/" + date.getYear() + " Income: " + result);
                return result;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get monthly income", e);
        }
        
        return 0.0;
    }

    public double getWeeklyIncome(LocalDate date) {
        // Thử lấy tuần hiện tại trước
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        String currentWeekSql = """
            SELECT COALESCE(SUM(amount), 0) as total 
            FROM income 
            WHERE date >= ? AND date <= ?
        """;
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(currentWeekSql)) {
            pstmt.setString(1, weekStart.toString());
            pstmt.setString(2, weekEnd.toString());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double currentResult = rs.getDouble("total");
                if (currentResult > 0) {
                    System.out.println("Current Week Income: " + currentResult);
                    return currentResult;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get current week income", e);
        }
        
        // Nếu tuần hiện tại không có dữ liệu, lấy tuần gần nhất
        String latestWeekSql = """
            SELECT COALESCE(SUM(amount), 0) as total
            FROM income
            WHERE date >= (SELECT MAX(date) FROM income) - 6
        """;
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(latestWeekSql)) {
            
            if (rs.next()) {
                double result = rs.getDouble("total");
                System.out.println("Latest Week Income: " + result);
                return result;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get latest week income", e);
        }
        
        return 0.0;
    }

    // Enhanced outcome operations
    public boolean addOutcome(double amount, String title, String category, LocalDate date, 
                             String description, String paymentMethod, boolean isRecurring) {
        if (amount <= 0) {
            System.err.println("Invalid outcome amount: " + amount);
            return false;
        }
        
        if (title == null || title.trim().isEmpty()) {
            System.err.println("Invalid outcome title: " + title);
            return false;
        }

        String sql = """
            INSERT INTO outcome (amount, title, category, date, description, payment_method, is_recurring) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, title.trim());
            pstmt.setString(3, category != null ? category.trim() : "Other");
            pstmt.setString(4, date != null ? date.toString() : LocalDate.now().toString());
            pstmt.setString(5, description != null ? description.trim() : "");
            pstmt.setString(6, paymentMethod != null ? paymentMethod.trim() : "Cash");
            pstmt.setBoolean(7, isRecurring);
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                System.out.println("✓ Outcome added successfully: " + title + " - $" + amount);
                logger.info("Outcome added successfully: " + title + " - $" + amount);
                return true;
            } else {
                System.err.println("✗ Failed to add outcome - no rows affected");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("✗ SQL Error adding outcome: " + e.getMessage());
            logger.log(Level.SEVERE, "Failed to add outcome: " + title, e);
            e.printStackTrace();
            return false;
        }
    }

    // Backward compatibility
    public boolean addOutcome(double amount, String title, String category, LocalDate date, String description) {
        return addOutcome(amount, title, category, date, description, "Cash", false);
    }

    public List<OutcomeRecord> getOutcomeRecords() {
        return getOutcomeRecords(100);
    }

    public List<OutcomeRecord> getOutcomeRecords(int limit) {
        List<OutcomeRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM outcome ORDER BY date DESC, id DESC LIMIT ?";
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                records.add(new OutcomeRecord(
                    rs.getInt("id"),
                    rs.getDouble("amount"),
                    rs.getString("title"),
                    rs.getString("category"),
                    LocalDate.parse(rs.getString("date")),
                    rs.getString("description"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getTimestamp("updated_at") != null ? 
                        rs.getTimestamp("updated_at").toLocalDateTime() : null,
                    rs.getString("payment_method"),
                    rs.getBoolean("is_recurring")
                ));
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get outcome records", e);
        }
        
        return records;
    }

    public double getMonthlyOutcome(LocalDate date) {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) as total
            FROM outcome
            WHERE strftime('%Y-%m', date) = strftime('%Y-%m', ?)
        """;
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.setString(1, date.toString());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double result = rs.getDouble("total");
                System.out.println("Month " + date.getMonthValue() + "/" + date.getYear() + " Outcome: " + result);
                return result;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get monthly outcome", e);
        }
        
        return 0.0;
    }

    public double getWeeklyOutcome(LocalDate date) {
        // Thử lấy tuần hiện tại trước
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        String currentWeekSql = """
            SELECT COALESCE(SUM(amount), 0) as total 
            FROM outcome 
            WHERE date >= ? AND date <= ?
        """;
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(currentWeekSql)) {
            pstmt.setString(1, weekStart.toString());
            pstmt.setString(2, weekEnd.toString());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double currentResult = rs.getDouble("total");
                if (currentResult > 0) {
                    System.out.println("Current Week Outcome: " + currentResult);
                    return currentResult;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get current week outcome", e);
        }
        
        // Nếu tuần hiện tại không có dữ liệu, lấy tuần gần nhất
        String latestWeekSql = """
            SELECT COALESCE(SUM(amount), 0) as total
            FROM outcome
            WHERE date >= (SELECT MAX(date) FROM outcome) - 6
        """;
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(latestWeekSql)) {
            
            if (rs.next()) {
                double result = rs.getDouble("total");
                System.out.println("Latest Week Outcome: " + result);
                return result;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get latest week outcome", e);
        }
        
        return 0.0;
    }

    public int getOutcomeRecordsCount() {
        String sql = "SELECT COUNT(*) as count FROM outcome";
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get outcome records count", e);
        }
        
        return 0;
    }

    // Enhanced Dashboard Data Operations
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        
        try {
            LocalDate now = LocalDate.now();
            
            // Sử dụng dữ liệu tháng/tuần/năm hiện tại (sẽ là 0 nếu không có data)
            double currentMonthIncome = getMonthlyIncome(now);
            double currentMonthOutcome = getMonthlyOutcome(now);
            double currentWeekIncome = getWeeklyIncome(now);
            double currentWeekOutcome = getWeeklyOutcome(now);
            double currentYearIncome = getYearlyIncome(now);
            double currentYearOutcome = getYearlyOutcome(now);
            
            // Nếu tháng hiện tại không có dữ liệu, sử dụng tháng gần nhất có dữ liệu
            if (currentMonthIncome == 0) {
                currentMonthIncome = getLatestMonthIncome();
            }
            if (currentMonthOutcome == 0) {
                currentMonthOutcome = getLatestMonthOutcome();
            }
            if (currentWeekIncome == 0) {
                currentWeekIncome = getLatestWeekIncome();
            }
            if (currentWeekOutcome == 0) {
                currentWeekOutcome = getLatestWeekOutcome();
            }
            
            stats.setMonthlyIncome(currentMonthIncome);
            stats.setMonthlyOutcome(currentMonthOutcome);
            stats.setWeeklyIncome(currentWeekIncome);
            stats.setWeeklyOutcome(currentWeekOutcome);
            
            // Tính toán dữ liệu tổng
            double totalIncome = getTotalIncome();
            double totalOutcome = getTotalOutcome();
            
            stats.setTotalIncome(totalIncome);
            stats.setTotalOutcome(totalOutcome);
            stats.setBalance(totalIncome - totalOutcome);
            
            // Tính savings rate dựa trên dữ liệu thực
            if (currentMonthIncome > 0) {
                double savingsRate = ((currentMonthIncome - currentMonthOutcome) / currentMonthIncome) * 100;
                stats.setSavingsRate(Math.max(0, savingsRate));
            } else {
                stats.setSavingsRate(0);
            }
            
            // Debug log
            System.out.println("=== Dashboard Stats (Corrected) ===");
            System.out.println("Monthly Income: " + currentMonthIncome);
            System.out.println("Monthly Outcome: " + currentMonthOutcome);
            System.out.println("Weekly Income: " + currentWeekIncome);
            System.out.println("Weekly Outcome: " + currentWeekOutcome);
            System.out.println("Total Income: " + totalIncome);
            System.out.println("Total Outcome: " + totalOutcome);
            System.out.println("Balance: " + stats.getBalance());
            System.out.println("Savings Rate: " + stats.getSavingsRate());
            
            stats.setMonthlyData(getEnhancedMonthlyChartData());
            stats.setTopIncomeCategories(getTopIncomeCategories(5));
            stats.setTopExpenseCategories(getTopExpenseCategories(5));
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get dashboard stats", e);
            e.printStackTrace();
        }
        
        return stats;
    }

    public double getTotalIncome() {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM income";
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get total income", e);
        }
        
        return 0.0;
    }

    public double getTotalOutcome() {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM outcome";
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get total outcome", e);
        }
        
        return 0.0;
    }

    public List<MonthlyData> getEnhancedMonthlyChartData() {
        List<MonthlyData> data = new ArrayList<>();
        String sql = """
            SELECT strftime('%Y-%m', date) as month, 
                   SUM(CASE WHEN type = 'income' THEN amount ELSE 0 END) as total_income,
                   SUM(CASE WHEN type = 'outcome' THEN amount ELSE 0 END) as total_outcome
            FROM (
                SELECT amount, date, 'income' as type FROM income
                UNION ALL
                SELECT amount, date, 'outcome' as type FROM outcome
            )
            GROUP BY month
            ORDER BY month DESC
            LIMIT 12
        """;
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String monthName = rs.getString("month");
                double income = rs.getDouble("total_income");
                double outcome = rs.getDouble("total_outcome");
                
                data.add(new MonthlyData(monthName, income, outcome));
            }
            
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to get enhanced monthly data, using fallback", e);
            return getDefaultMonthlyData();
        }
        
        return data.isEmpty() ? getDefaultMonthlyData() : data;
    }

    public List<CategoryData> getTopIncomeCategories(int limit) {
        List<CategoryData> categories = new ArrayList<>();
        String sql = """
            SELECT category, SUM(amount) as total_amount
            FROM income
            GROUP BY category
            ORDER BY total_amount DESC
            LIMIT ?
        """;
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                categories.add(new CategoryData(
                    rs.getString("category"),
                    rs.getDouble("total_amount")
                ));
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get top income categories", e);
        }
        
        return categories;
    }

    public List<CategoryData> getTopExpenseCategories(int limit) {
        List<CategoryData> categories = new ArrayList<>();
        String sql = """
            SELECT category, SUM(amount) as total_amount
            FROM outcome
            GROUP BY category
            ORDER BY total_amount DESC
            LIMIT ?
        """;
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                categories.add(new CategoryData(
                    rs.getString("category"),
                    rs.getDouble("total_amount")
                ));
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get top expense categories", e);
        }
        
        return categories;
    }

    // Enhanced update and delete operations
    public boolean updateIncome(int id, double amount, String source, String category, LocalDate date, String description) {
        if (amount <= 0 || source == null || source.trim().isEmpty()) {
            logger.warning("Invalid parameters for income update");
            return false;
        }

        String sql = """
            UPDATE income 
            SET amount = ?, source = ?, category = ?, date = ?, description = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            
            pstmt.setDouble(1, amount);
            pstmt.setString(2, source.trim());
            pstmt.setString(3, category != null ? category.trim() : "Other");
            pstmt.setString(4, date != null ? date.toString() : LocalDate.now().toString());
            pstmt.setString(5, description != null ? description.trim() : "");
            pstmt.setInt(6, id);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Income updated successfully: ID " + id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to update income: ID " + id, e);
        }
        
        return false;
    }

    public boolean updateOutcome(int id, double amount, String title, String category, LocalDate date, 
                                String description, String paymentMethod, boolean isRecurring) {
        if (amount <= 0 || title == null || title.trim().isEmpty()) {
            logger.warning("Invalid parameters for outcome update");
            return false;
        }

        String sql = """
            UPDATE outcome 
            SET amount = ?, title = ?, category = ?, date = ?, description = ?, 
                payment_method = ?, is_recurring = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            
            pstmt.setDouble(1, amount);
            pstmt.setString(2, title.trim());
            pstmt.setString(3, category != null ? category.trim() : "Other");
            pstmt.setString(4, date != null ? date.toString() : LocalDate.now().toString());
            pstmt.setString(5, description != null ? description.trim() : "");
            pstmt.setString(6, paymentMethod != null ? paymentMethod.trim() : "Cash");
            pstmt.setBoolean(7, isRecurring);
            pstmt.setInt(8, id);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Outcome updated successfully: ID " + id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to update outcome: ID " + id, e);
        }
        
        return false;
    }

    public boolean deleteIncome(int id) {
        String sql = "DELETE FROM income WHERE id = ?";
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Income deleted successfully: ID " + id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to delete income: ID " + id, e);
        }
        
        return false;
    }

    public boolean deleteOutcome(int id) {
        String sql = "DELETE FROM outcome WHERE id = ?";
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Outcome deleted successfully: ID " + id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to delete outcome: ID " + id, e);
        }
        
        return false;
    }

    // Legacy compatibility methods
    public double getYearlyIncome(LocalDate date) {
        // Thử lấy năm hiện tại trước
        String currentYearSql = """
            SELECT COALESCE(SUM(amount), 0) as total 
            FROM income 
            WHERE strftime('%Y', date) = strftime('%Y', ?)
        """;
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(currentYearSql)) {
            pstmt.setString(1, date.toString());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double currentResult = rs.getDouble("total");
                if (currentResult > 0) {
                    System.out.println("Current Year Income: " + currentResult);
                    return currentResult;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get current year income", e);
        }
        
        // Nếu năm hiện tại không có dữ liệu, lấy năm gần nhất
        String latestYearSql = """
            SELECT COALESCE(SUM(amount), 0) as total, strftime('%Y', date) as year
            FROM income
            GROUP BY strftime('%Y', date)
            ORDER BY year DESC
            LIMIT 1
        """;
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(latestYearSql)) {
            
            if (rs.next()) {
                double result = rs.getDouble("total");
                String year = rs.getString("year");
                System.out.println("Latest Year (" + year + ") Income: " + result);
                return result;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get latest year income", e);
        }
        
        return 0.0;
    }

    public int getIncomeRecordsCount() {
        String sql = "SELECT COUNT(*) as count FROM income";
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get income records count", e);
        }
        
        return 0;
    }

    public double getAverageMonthlyIncome() {
        String sql = """
            SELECT AVG(monthly_total) as avg_monthly
            FROM (
                SELECT SUM(amount) as monthly_total
                FROM income
                GROUP BY strftime('%Y-%m', date)
            )
        """;
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("avg_monthly");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get average monthly income", e);
        }
        
        return 0.0;
    }

    // Legacy methods for backward compatibility
    public List<MonthlyData> getMonthlyChartData() {
        return getEnhancedMonthlyChartData();
    }

    private List<MonthlyData> getDefaultMonthlyData() {
        List<MonthlyData> defaultData = new ArrayList<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        double[] incomeData = {3200, 2800, 3600, 3400, 4100, 3700, 4300, 4000, 3800, 4200, 3900, 4100};
        double[] outcomeData = {2200, 1800, 2400, 2300, 2600, 2100, 2800, 2500, 2300, 2700, 2400, 2600};
        
        for (int i = 0; i < months.length; i++) {
            defaultData.add(new MonthlyData(months[i], incomeData[i], outcomeData[i]));
        }
        
        return defaultData;
    }

    // Enhanced category operations
    public List<String> getIncomeCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT name FROM categories WHERE type = 'income' ORDER BY name";
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
            
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to get income categories, using defaults", e);
            categories.addAll(List.of("Salary", "Freelance", "Investment", "Business", "Bonus", "Other"));
        }
        
        return categories;
    }

    public List<String> getOutcomeCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT name FROM categories WHERE type = 'outcome' ORDER BY name";
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
            
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to get outcome categories, using defaults", e);
            categories.addAll(List.of("Food", "Transportation", "Housing", "Utilities", "Entertainment", "Other"));
        }
        
        return categories;
    }

    // Database maintenance operations
    public boolean backupDatabase(String backupPath) {
        String sql = "BACKUP TO ?";
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            
            pstmt.setString(1, backupPath);
            pstmt.executeUpdate();
            logger.info("Database backed up to: " + backupPath);
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to backup database", e);
        }
        
        return false;
    }

    public void optimizeDatabase() {
        try (Statement stmt = this.connection.createStatement()) {
            stmt.execute("VACUUM;");
            stmt.execute("ANALYZE;");
            logger.info("Database optimized.");
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to optimize database", e);
        }
    }

    // Resource cleanup
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error closing database connection", e);
        }
    }

    // Finalize method to ensure cleanup
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    // Thêm method để debug dữ liệu
    public void debugDatabaseContent() {
        System.out.println("=== DATABASE DEBUG ===");
        
        // Check income records
        String incomeQuery = "SELECT COUNT(*) as count, SUM(amount) as total FROM income";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(incomeQuery)) {
            if (rs.next()) {
                System.out.println("Income Records: " + rs.getInt("count"));
                System.out.println("Total Income: " + rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Check outcome records
        String outcomeQuery = "SELECT COUNT(*) as count, SUM(amount) as total FROM outcome";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(outcomeQuery)) {
            if (rs.next()) {
                System.out.println("Outcome Records: " + rs.getInt("count"));
                System.out.println("Total Outcome: " + rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Thêm các method này vào DatabaseManager.java
    public double getYearlyOutcome(LocalDate date) {
        // Thử lấy năm hiện tại trước
        String currentYearSql = """
            SELECT COALESCE(SUM(amount), 0) as total 
            FROM outcome 
            WHERE strftime('%Y', date) = strftime('%Y', ?)
        """;
        
        try (PreparedStatement pstmt = this.connection.prepareStatement(currentYearSql)) {
            pstmt.setString(1, date.toString());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double currentResult = rs.getDouble("total");
                if (currentResult > 0) {
                    System.out.println("Current Year Outcome: " + currentResult);
                    return currentResult;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get current year outcome", e);
        }
        
        // Nếu năm hiện tại không có dữ liệu, lấy năm gần nhất
        String latestYearSql = """
            SELECT COALESCE(SUM(amount), 0) as total, strftime('%Y', date) as year
            FROM outcome
            GROUP BY strftime('%Y', date)
            ORDER BY year DESC
            LIMIT 1
        """;
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(latestYearSql)) {
            
            if (rs.next()) {
                double result = rs.getDouble("total");
                String year = rs.getString("year");
                System.out.println("Latest Year (" + year + ") Outcome: " + result);
                return result;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get latest year outcome", e);
        }
        
        return 0.0;
    }

    public double getAverageMonthlyOutcome() {
        String sql = """
            SELECT AVG(monthly_total) as avg_monthly
            FROM (
                SELECT SUM(amount) as monthly_total
                FROM outcome
                GROUP BY strftime('%Y-%m', date)
            )
        """;
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("avg_monthly");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get average monthly outcome", e);
        }
        
        return 0.0;
    }

    // Thêm method để debug tất cả dữ liệu
    public void debugAllData() {
        System.out.println("\n=== COMPLETE DATABASE DEBUG ===");
        
        // Check sample data from income table
        String incomeCheck = "SELECT id, amount, source, category, date FROM income LIMIT 5";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(incomeCheck)) {
            
            System.out.println("Sample Income Records:");
            while (rs.next()) {
                System.out.printf("ID: %d, Amount: %.2f, Source: %s, Date: %s%n",
                    rs.getInt("id"), rs.getDouble("amount"), 
                    rs.getString("source"), rs.getString("date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Check sample data from outcome table
        String outcomeCheck = "SELECT id, amount, title, category, date FROM outcome LIMIT 5";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(outcomeCheck)) {
            
            System.out.println("\nSample Outcome Records:");
            while (rs.next()) {
                System.out.printf("ID: %d, Amount: %.2f, Title: %s, Date: %s%n",
                    rs.getInt("id"), rs.getDouble("amount"), 
                    rs.getString("title"), rs.getString("date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Check totals
        debugDatabaseContent();
    }

    // Thêm method này vào DatabaseManager.java
    public void addCurrentMonthSampleData() {
        LocalDate now = LocalDate.now();
        
        // Thêm income tháng hiện tại
        addIncome(5000.0, "Tech Company Ltd", "Salary", now.withDayOfMonth(1), "Monthly salary");
        addIncome(800.0, "Freelance Project", "Freelance", now.withDayOfMonth(5), "Website development");
        addIncome(200.0, "Stock Dividends", "Investment", now.withDayOfMonth(10), "Quarterly dividends");
        
        // Thêm outcome tháng hiện tại
        addOutcome(1200.0, "Rent Payment", "Housing", now.withDayOfMonth(1), "Monthly rent");
        addOutcome(300.0, "Grocery Shopping", "Food", now.withDayOfMonth(3), "Weekly groceries");
        addOutcome(150.0, "Gas Station", "Transportation", now.withDayOfMonth(5), "Car fuel");
        
        System.out.println("Added current month sample data");
    }

    // Thêm method để lấy dữ liệu tháng có data gần nhất (để hiển thị cho This Month)
    public double getLatestMonthIncome() {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) as total, strftime('%Y-%m', date) as month
            FROM income
            GROUP BY strftime('%Y-%m', date)
            ORDER BY date DESC
            LIMIT 1
        """;
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                double result = rs.getDouble("total");
                String month = rs.getString("month");
                System.out.println("Latest Month (" + month + ") Income: " + result);
                return result;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get latest month income", e);
        }
        
        return 0.0;
    }

    public double getLatestMonthOutcome() {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) as total, strftime('%Y-%m', date) as month
            FROM outcome
            GROUP BY strftime('%Y-%m', date)
            ORDER BY date DESC
            LIMIT 1
        """;
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                double result = rs.getDouble("total");
                String month = rs.getString("month");
                System.out.println("Latest Month (" + month + ") Outcome: " + result);
                return result;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get latest month outcome", e);
        }
        
        return 0.0;
    }

    // Thêm method để lấy dữ liệu tuần gần nhất
    public double getLatestWeekIncome() {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) as total
            FROM income
            WHERE date >= (
                SELECT date(MAX(date), '-6 days')
                FROM income
            )
        """;
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                double result = rs.getDouble("total");
                System.out.println("Latest Week Income: " + result);
                return result;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get latest week income", e);
        }
        
        return 0.0;
    }

    public double getLatestWeekOutcome() {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) as total
            FROM outcome
            WHERE date >= (
                SELECT date(MAX(date), '-6 days')
                FROM outcome
            )
        """;
        
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                double result = rs.getDouble("total");
                System.out.println("Latest Week Outcome: " + result);
                return result;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get latest week outcome", e);
        }
        
        return 0.0;
    }

    // Thêm method để kiểm tra kết nối database
    public void debugConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                System.out.println("✓ Database connection is active");
                
                // Test simple query
                String testQuery = "SELECT COUNT(*) as total FROM income";
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(testQuery)) {
                    if (rs.next()) {
                        System.out.println("✓ Database query test passed - Income count: " + rs.getInt("total"));
                    }
                }
            } else {
                System.err.println("✗ Database connection is null or closed");
            }
        } catch (SQLException e) {
            System.err.println("✗ Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}