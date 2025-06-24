package com.cashflow;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class LanguageManager {
    private static LanguageManager instance;
    private String currentLanguage = "en";
    private Preferences prefs;
    private Map<String, Map<String, String>> translations;
    
    private LanguageManager() {
        prefs = Preferences.userNodeForPackage(LanguageManager.class);
        currentLanguage = prefs.get("language", "en");
        initializeTranslations();
    }
    
    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }
    
    private void initializeTranslations() {
        translations = new HashMap<>();
        
        // English translations
        Map<String, String> english = new HashMap<>();
        english.put("dashboard", "Dashboard");
        english.put("income", "Income");
        english.put("outcome", "Expense");
        english.put("analytics", "Analytics");
        english.put("transaction", "Transaction");
        english.put("card", "Card");
        english.put("settings", "Settings");
        english.put("welcome_back", "Welcome back! Here's your financial overview");
        english.put("good_morning", "Good Morning");
        english.put("good_afternoon", "Good Afternoon");
        english.put("good_evening", "Good Evening");
        english.put("this_month", "This Month");
        english.put("this_week", "This Week");
        english.put("this_year", "This Year");
        english.put("balance", "Balance");
        english.put("savings_rate", "Savings Rate");
        english.put("add_income", "Add Income");
        english.put("add_expense", "Add Expense");
        english.put("amount", "Amount");
        english.put("category", "Category");
        english.put("date", "Date");
        english.put("description", "Description");
        english.put("source", "Source");
        english.put("title", "Title");
        english.put("payment_method", "Payment Method");
        english.put("recurring", "Recurring");
        english.put("total_income", "Total Income");
        english.put("total_expense", "Total Expense");
        english.put("recent_transactions", "Recent Transactions");
        english.put("spending_breakdown", "Spending Breakdown");
        english.put("monthly_overview", "Monthly Overview");
        english.put("financial_health", "Financial Health");
        english.put("edit", "Edit");
        english.put("delete", "Delete");
        english.put("save", "Save");
        english.put("cancel", "Cancel");
        english.put("search", "Search");
        english.put("filter", "Filter");
        english.put("sort", "Sort");
        english.put("export", "Export");
        english.put("import", "Import");
        english.put("backup", "Backup");
        english.put("clear", "Clear");
        english.put("apply", "Apply");
        english.put("success", "Success");
        english.put("error", "Error");
        english.put("warning", "Warning");
        english.put("info", "Information");
        // English additions
        english.put("update", "Update");
        english.put("processing", "Processing");
        english.put("appearance", "Appearance");
        english.put("behavior", "Behavior");
        english.put("theme", "Theme");
        english.put("dark_theme", "Dark Theme");
        english.put("light_theme", "Light Theme");
        english.put("currency", "Currency");
        english.put("notifications", "Notifications");
        english.put("budget_alerts", "Budget limit alerts");
        english.put("transaction_notifications", "Transaction notifications");
        english.put("monthly_reports", "Monthly financial reports");
        english.put("preview", "Preview");
        english.put("recent_period", "Recent Period");
        english.put("latest_month", "Latest Month");
        english.put("total_records", "Total Records");
        english.put("monthly_average", "Monthly Average");
        english.put("excellent_savings", "Excellent Savings");
        english.put("good_savings", "Good Savings");
        english.put("moderate_savings", "Moderate Savings");
        english.put("low_savings", "Low Savings");
        english.put("income_growth", "Income Growth");
        english.put("income_decline", "Income Decline");
        english.put("income_diversification", "Income Diversification");
        english.put("budget_tracking", "Budget Tracking");
        english.put("emergency_fund", "Emergency Fund");
        english.put("financial_health_score", "Financial Health Score");
        english.put("spending_forecast", "Spending Forecast");
        english.put("anomaly_detection", "Anomaly Detection");
        english.put("risk_assessment", "Risk Assessment");
        
        // Vietnamese translations
        Map<String, String> vietnamese = new HashMap<>();
        vietnamese.put("dashboard", "Trang chủ");
        vietnamese.put("income", "Thu nhập");
        vietnamese.put("outcome", "Chi tiêu");
        vietnamese.put("analytics", "Phân tích");
        vietnamese.put("transaction", "Giao dịch");
        vietnamese.put("card", "Thẻ");
        vietnamese.put("settings", "Cài đặt");
        vietnamese.put("welcome_back", "Chào mừng trở lại! Đây là tổng quan tài chính của bạn");
        vietnamese.put("good_morning", "Chào buổi sáng");
        vietnamese.put("good_afternoon", "Chào buổi chiều");
        vietnamese.put("good_evening", "Chào buổi tối");
        vietnamese.put("this_month", "Tháng này");
        vietnamese.put("this_week", "Tuần này");
        vietnamese.put("this_year", "Năm này");
        vietnamese.put("balance", "Số dư");
        vietnamese.put("savings_rate", "Tỷ lệ tiết kiệm");
        vietnamese.put("add_income", "Thêm thu nhập");
        vietnamese.put("add_expense", "Thêm chi tiêu");
        vietnamese.put("amount", "Số tiền");
        vietnamese.put("category", "Danh mục");
        vietnamese.put("date", "Ngày");
        vietnamese.put("description", "Mô tả");
        vietnamese.put("source", "Nguồn");
        vietnamese.put("title", "Tiêu đề");
        vietnamese.put("payment_method", "Phương thức thanh toán");
        vietnamese.put("recurring", "Định kỳ");
        vietnamese.put("total_income", "Tổng thu nhập");
        vietnamese.put("total_expense", "Tổng chi tiêu");
        vietnamese.put("recent_transactions", "Giao dịch gần đây");
        vietnamese.put("spending_breakdown", "Phân tích chi tiêu");
        vietnamese.put("monthly_overview", "Tổng quan tháng");
        vietnamese.put("financial_health", "Sức khỏe tài chính");
        vietnamese.put("edit", "Sửa");
        vietnamese.put("delete", "Xóa");
        vietnamese.put("save", "Lưu");
        vietnamese.put("cancel", "Hủy");
        vietnamese.put("search", "Tìm kiếm");
        vietnamese.put("filter", "Lọc");
        vietnamese.put("sort", "Sắp xếp");
        vietnamese.put("export", "Xuất");
        vietnamese.put("import", "Nhập");
        vietnamese.put("backup", "Sao lưu");
        vietnamese.put("clear", "Xóa");
        vietnamese.put("apply", "Áp dụng");
        vietnamese.put("success", "Thành công");
        vietnamese.put("error", "Lỗi");
        vietnamese.put("warning", "Cảnh báo");
        vietnamese.put("info", "Thông tin");
        // Vietnamese additions
        vietnamese.put("update", "Cập nhật");
        vietnamese.put("processing", "Đang xử lý");
        vietnamese.put("appearance", "Giao diện");
        vietnamese.put("behavior", "Hành vi");
        vietnamese.put("theme", "Chủ đề");
        vietnamese.put("dark_theme", "Chủ đề tối");
        vietnamese.put("light_theme", "Chủ đề sáng");
        vietnamese.put("currency", "Tiền tệ");
        vietnamese.put("notifications", "Thông báo");
        vietnamese.put("budget_alerts", "Cảnh báo ngân sách");
        vietnamese.put("transaction_notifications", "Thông báo giao dịch");
        vietnamese.put("monthly_reports", "Báo cáo hàng tháng");
        vietnamese.put("preview", "Xem trước");
        vietnamese.put("recent_period", "Thời gian gần đây");
        vietnamese.put("latest_month", "Tháng gần nhất");
        vietnamese.put("total_records", "Tổng số bản ghi");
        vietnamese.put("monthly_average", "Trung bình tháng");
        vietnamese.put("excellent_savings", "Tiết kiệm xuất sắc");
        vietnamese.put("good_savings", "Tiết kiệm tốt");
        vietnamese.put("moderate_savings", "Tiết kiệm vừa phải");
        vietnamese.put("low_savings", "Tiết kiệm thấp");
        vietnamese.put("income_growth", "Tăng trưởng thu nhập");
        vietnamese.put("income_decline", "Giảm thu nhập");
        vietnamese.put("income_diversification", "Đa dạng hóa thu nhập");
        vietnamese.put("budget_tracking", "Theo dõi ngân sách");
        vietnamese.put("emergency_fund", "Quỹ khẩn cấp");
        vietnamese.put("financial_health_score", "Điểm sức khỏe tài chính");
        vietnamese.put("spending_forecast", "Dự báo chi tiêu");
        vietnamese.put("anomaly_detection", "Phát hiện bất thường");
        vietnamese.put("risk_assessment", "Đánh giá rủi ro");
        
        translations.put("en", english);
        translations.put("vi", vietnamese);
    }
    
    public String translate(String key) {
        Map<String, String> currentTranslations = translations.get(currentLanguage);
        return currentTranslations.getOrDefault(key, key);
    }
    
    public void setLanguage(String language) {
        this.currentLanguage = language;
        prefs.put("language", language);
    }
    
    public String getCurrentLanguage() {
        return currentLanguage;
    }
    
    public String[] getAvailableLanguages() {
        return new String[]{"English", "Tiếng Việt"};
    }
    
    public String[] getLanguageCodes() {
        return new String[]{"en", "vi"};
    }
} 