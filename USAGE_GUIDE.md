# Hướng Dẫn Sử Dụng Ứng Dụng Quản Lý Dòng Tiền Hiện Đại

## Cách Sử Dụng

### Chạy Ứng Dụng Modern

Có 2 cách để chạy ứng dụng:

#### Cách 1: Sử dụng Launcher chính (Đã được cập nhật)
```bash
java com.cashflow.Launcher
```

#### Cách 2: Sử dụng ModernLauncher (Khuyến nghị)
```bash
java com.cashflow.ModernLauncher
```

**Lưu ý**: `ModernLauncher` có cấu hình tối ưu hóa rendering tốt hơn.

### CSS Styling

- Đặt file `modern-styles.css` trong resources folder
- CSS cung cấp styling tùy chỉnh cho:
  - Scrollbars
  - Buttons
  - Charts
  - Và các thành phần UI khác

### Enhanced Analytics

#### Sử Dụng Trong Code

```java
EnhancedAnalyticsService analytics = new EnhancedAnalyticsService(dbManager);

// Dự báo chi tiêu
Map<String, SpendingForecast> forecasts = analytics.forecastSpendingPatterns(6);

// Phát hiện anomaly
List<SpendingAnomaly> anomalies = analytics.detectSpendingAnomalies();

// Đánh giá rủi ro
FinancialRiskAssessment risk = analytics.assessFinancialRisk(1000);
```

#### Sử Dụng Trong Giao Diện

1. **Mở trang Analytics**: Click vào menu "Analytics" trong sidebar
2. **Enhanced Analytics Section**: Hiển thị 3 card chính:
   - **Spending Forecast** (🔮): Dự báo chi tiêu trong 6 tháng tới
   - **Anomaly Detection** (⚠️): Phát hiện các giao dịch bất thường
   - **Risk Assessment** (🛡️): Đánh giá rủi ro tài chính với VaR

#### Tính Năng Chi Tiết

- **Dự Báo Chi Tiêu**: Sử dụng thuật toán exponential smoothing để dự báo xu hướng chi tiêu theo danh mục
- **Phát Hiện Bất Thường**: Sử dụng Z-score để phát hiện các giao dịch vượt quá 2.5 độ lệch chuẩn
- **Đánh Giá Rủi Ro**: Tính toán Value at Risk (VaR), risk score và phân loại mức độ rủi ro

### Database Vẫn Tương Thích

- Sử dụng cùng `DatabaseManager`
- Không cần thay đổi schema
- Tự động migrate data

## Cấu Trúc File

- `Launcher.java` - Launcher chính (đã cập nhật để chạy ModernCashflowApp)
- `ModernLauncher.java` - Launcher hiện đại với cấu hình tối ưu
- `ModernCashflowApp.java` - Ứng dụng chính với giao diện hiện đại và Enhanced Analytics tích hợp
- `EnhancedAnalyticsService.java` - Dịch vụ phân tích nâng cao
- `modern-styles.css` - Styling cho giao diện hiện đại

## Tính Năng Mới

1. **Giao Diện Hiện Đại**: Sử dụng CSS tùy chỉnh cho trải nghiệm người dùng tốt hơn
2. **Phân Tích Nâng Cao**: Dự báo chi tiêu, phát hiện bất thường, đánh giá rủi ro
3. **Tích Hợp UI**: Enhanced Analytics được tích hợp trực tiếp vào giao diện người dùng
4. **Tương Thích Ngược**: Hoạt động với database hiện có mà không cần thay đổi

## Hướng Dẫn Sử Dụng Giao Diện

### Trang Analytics
- **Financial Health Score**: Điểm đánh giá sức khỏe tài chính tổng thể
- **Enhanced Analytics Cards**: 3 card hiển thị các tính năng phân tích nâng cao
- **Financial Insights**: Các insight tự động được tạo ra từ dữ liệu
- **Spending Patterns**: Biểu đồ phân tích mẫu chi tiêu

### Navigation
- Sử dụng sidebar để chuyển đổi giữa các trang
- Dashboard: Tổng quan tổng thể
- Income: Quản lý thu nhập
- Outcome: Quản lý chi tiêu
- Analytics: Phân tích nâng cao
- Transaction: Lịch sử giao dịch
- Card: Quản lý thẻ thanh toán

## Khắc Phục Sự Cố

### Lỗi Biên Dịch
- Đảm bảo đã compile tất cả các file Java
- Kiểm tra các import và dependencies

### Lỗi Chạy Ứng Dụng
- Sử dụng `ModernLauncher` thay vì `Launcher` cũ
- Đảm bảo JavaFX đã được cài đặt đúng cách
- Kiểm tra đường dẫn đến file CSS

### Lỗi Database
- Đảm bảo file `cashflow.db` tồn tại trong thư mục gốc
- Kiểm tra quyền truy cập file database 