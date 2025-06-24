# 🚀 Quick Start Guide - Cashflow Dashboard

## ⚡ Chạy Nhanh (3 Bước)

### Bước 1: Mở Terminal/Command Prompt
```bash
cd demo2
```

### Bước 2: Chạy Ứng Dụng
```bash
mvn clean compile javafx:run
```

### Bước 3: Sử Dụng Ứng Dụng
- **Dashboard**: Tổng quan tài chính
- **Income**: Quản lý thu nhập
- **Outcome**: Quản lý chi tiêu  
- **Analytics**: Phân tích nâng cao với Enhanced Analytics
- **Transaction**: Lịch sử giao dịch
- **Card**: Quản lý thẻ thanh toán

## 🔧 Nếu Gặp Lỗi Database

### Cách 1: Reset Tự Động (Windows)
```bash
reset-and-run.bat
```

### Cách 2: Reset Thủ Công
```bash
# Xóa database cũ
del cashflow.db cashflow.db-shm cashflow.db-wal

# Tạo database mới
sqlite3 cashflow.db < reset-database.sql

# Chạy ứng dụng
mvn clean compile javafx:run
```

## 📊 Enhanced Analytics Features

### 🔮 Spending Forecast
- Dự báo chi tiêu 6 tháng tới
- Sử dụng thuật toán exponential smoothing
- Hiển thị khoảng tin cậy

### ⚠️ Anomaly Detection  
- Phát hiện giao dịch bất thường
- Sử dụng Z-score (ngưỡng 2.5)
- Hiển thị mức độ bất thường

### 🛡️ Risk Assessment
- Monte Carlo simulation
- Value at Risk (VaR) calculation
- Risk score từ 0-100
- Phân loại mức độ rủi ro

## 🎨 Giao Diện Hiện Đại

- **Dark Theme**: Giao diện tối hiện đại
- **Gradient Effects**: Hiệu ứng gradient đẹp mắt
- **Animations**: Chuyển động mượt mà
- **Responsive Design**: Tương thích nhiều kích thước màn hình

## 📁 Cấu Trúc File Quan Trọng

```
demo2/
├── src/main/java/com/cashflow/
│   ├── ModernCashflowApp.java      # Ứng dụng chính
│   ├── EnhancedAnalyticsService.java # Phân tích nâng cao
│   ├── Launcher.java               # Launcher chính
│   └── ModernLauncher.java         # Launcher hiện đại
├── src/main/resources/
│   └── modern-styles.css           # CSS hiện đại
├── pom.xml                         # Cấu hình Maven
├── reset-database.sql              # Script reset database
├── reset-and-run.bat               # Script tự động
└── README.md                       # Hướng dẫn chi tiết
```

## 🆘 Khắc Phục Sự Cố

### Lỗi "No plugin found for prefix 'javafx'"
- Đảm bảo đang ở trong thư mục `demo2`
- Chạy `mvn clean compile` trước

### Lỗi Database
- Sử dụng `reset-and-run.bat`
- Hoặc reset thủ công theo hướng dẫn trên

### Lỗi JavaFX
- Đảm bảo Java 17+ đã cài đặt
- Kiểm tra JavaFX dependencies

## 🎯 Tính Năng Nổi Bật

✅ **Giao Diện Hiện Đại**: Dark theme, animations, gradients  
✅ **Enhanced Analytics**: AI/ML algorithms  
✅ **Database Tương Thích**: SQLite với schema đúng  
✅ **Maven Integration**: Build và run dễ dàng  
✅ **Error Handling**: Khắc phục sự cố tự động  
✅ **Cross-Platform**: Chạy trên Windows, macOS, Linux  

## 📞 Hỗ Trợ

Nếu gặp vấn đề, hãy:
1. Kiểm tra README.md để biết thêm chi tiết
2. Chạy `reset-and-run.bat` để reset hoàn toàn
3. Đảm bảo Java 17+ và Maven đã cài đặt

---

**🎉 Chúc bạn sử dụng ứng dụng vui vẻ!** 