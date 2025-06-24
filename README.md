# Cashflow Dashboard - Modern Financial Management Application

Ứng dụng quản lý dòng tiền hiện đại với giao diện đẹp mắt và các tính năng phân tích nâng cao.

## Tính Năng Chính

- 📊 **Dashboard Hiện Đại**: Tổng quan tài chính với giao diện đẹp mắt
- 💰 **Quản Lý Thu Nhập**: Thêm và theo dõi các nguồn thu nhập
- 💳 **Quản Lý Chi Tiêu**: Phân loại và theo dõi chi tiêu
- 📈 **Enhanced Analytics**: Phân tích nâng cao với AI/ML
- 🔄 **Lịch Sử Giao Dịch**: Xem và lọc tất cả giao dịch
- 💎 **Quản Lý Thẻ**: Quản lý thẻ thanh toán

## Enhanced Analytics Features

- 🔮 **Dự Báo Chi Tiêu**: Sử dụng thuật toán exponential smoothing
- ⚠️ **Phát Hiện Bất Thường**: Phát hiện giao dịch bất thường bằng Z-score
- 🛡️ **Đánh Giá Rủi Ro**: Tính toán VaR và risk score
- 📊 **Phân Tích Xu Hướng**: Phân tích xu hướng chi tiêu theo thời gian

## Yêu Cầu Hệ Thống

- Java 17 hoặc cao hơn
- Maven 3.6+
- JavaFX 17

## Cách Chạy Ứng Dụng

### Cách 1: Sử dụng Maven (Khuyến nghị)

```bash
# Compile và chạy ứng dụng
mvn clean compile javafx:run
```

### Cách 2: Sử dụng Java trực tiếp

```bash
# Compile
mvn clean compile

# Chạy với Launcher chính
java -cp target/classes com.cashflow.Launcher

# Hoặc chạy với ModernLauncher (khuyến nghị)
java -cp target/classes com.cashflow.ModernLauncher
```

### Cách 3: Tạo JAR và chạy

```bash
# Tạo JAR executable
mvn clean package

# Chạy JAR
java -jar target/cashflow-dashboard-1.0-SNAPSHOT.jar
```

## Cấu Trúc Dự Án

```
demo2/
├── src/main/java/com/cashflow/
│   ├── ModernCashflowApp.java      # Ứng dụng chính với giao diện hiện đại
│   ├── EnhancedAnalyticsService.java # Dịch vụ phân tích nâng cao
│   ├── Launcher.java               # Launcher chính
│   ├── ModernLauncher.java         # Launcher hiện đại
│   └── ...                         # Các class khác
├── src/main/resources/
│   └── modern-styles.css           # CSS cho giao diện hiện đại
├── pom.xml                         # Cấu hình Maven
└── README.md                       # Hướng dẫn này
```

## Tính Năng Enhanced Analytics

### Dự Báo Chi Tiêu
- Sử dụng thuật toán exponential smoothing
- Dự báo chi tiêu trong 6 tháng tới
- Hiển thị khoảng tin cậy

### Phát Hiện Bất Thường
- Sử dụng Z-score để phát hiện giao dịch bất thường
- Ngưỡng phát hiện: 2.5 độ lệch chuẩn
- Hiển thị mức độ bất thường

### Đánh Giá Rủi Ro
- Monte Carlo simulation
- Tính toán Value at Risk (VaR)
- Risk score từ 0-100
- Phân loại mức độ rủi ro

## Khắc Phục Sự Cố

### Lỗi Database (Khuyến nghị reset)
Nếu gặp lỗi database như "no such column" hoặc "database connection closed":

#### Cách 1: Sử dụng script tự động (Windows)
```bash
# Chạy script reset và run
reset-and-run.bat
```

#### Cách 2: Reset thủ công
```bash
# Xóa database cũ
rm cashflow.db cashflow.db-shm cashflow.db-wal

# Tạo database mới với schema đúng
sqlite3 cashflow.db < reset-database.sql

# Chạy ứng dụng
mvn clean compile javafx:run
```

### Lỗi Maven
```bash
# Xóa cache Maven
mvn clean

# Cập nhật dependencies
mvn dependency:resolve
```

### Lỗi JavaFX
```bash
# Đảm bảo JavaFX đã được cài đặt
# Hoặc sử dụng OpenJDK với JavaFX
```

### Lỗi Database
- Đảm bảo file `cashflow.db` tồn tại
- Kiểm tra quyền truy cập file
- Reset database nếu có lỗi schema

## Đóng Góp

1. Fork dự án
2. Tạo feature branch
3. Commit changes
4. Push to branch
5. Tạo Pull Request

## License

MIT License - xem file LICENSE để biết thêm chi tiết.