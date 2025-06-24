# Hướng dẫn tạo Installer cho Cashflow Dashboard

## 📋 Yêu cầu hệ thống

### Để tạo installer:
- Windows 10/11
- Java 17+ (đã cài đặt)
- Inno Setup 6.0+ (https://jrsoftware.org/isinfo.php)

### Để chạy ứng dụng:
- Windows 10/11
- Java 17+ (sẽ được kiểm tra tự động)

## 🚀 Các bước tạo installer

### 1. Chuẩn bị
```bash
# Đảm bảo JAR file đã được build
mvn clean package
```

### 2. Cài đặt Inno Setup
1. Tải Inno Setup từ: https://jrsoftware.org/isinfo.php
2. Cài đặt với tùy chọn "Add Inno Setup directory to PATH"
3. Khởi động lại Command Prompt

### 3. Tạo installer
```bash
# Chạy script tự động
create-installer.bat
```

Hoặc thủ công:
```bash
# Tạo thư mục installer
mkdir installer

# Compile installer
iscc setup.iss
```

## 📦 Kết quả

Sau khi hoàn thành, bạn sẽ có:
- `installer/CashflowDashboard-Setup.exe` - File installer chính
- `run-app.bat` - Script chạy ứng dụng
- `setup.iss` - File cấu hình installer

## 🎯 Tính năng installer

### ✅ Tự động kiểm tra Java
- Kiểm tra Java đã cài đặt chưa
- Hiển thị thông báo lỗi nếu chưa có Java
- Hướng dẫn tải Java từ https://adoptium.net/

### ✅ Cài đặt tự động
- Tạo shortcut trên Desktop (tùy chọn)
- Tạo shortcut trong Start Menu
- Tạo shortcut trong Quick Launch
- Tự động chạy ứng dụng sau khi cài đặt

### ✅ Gỡ cài đặt
- Tạo uninstaller tự động
- Xóa tất cả file và shortcut
- Xóa registry entries

## 🔧 Tùy chỉnh installer

### Thay đổi thông tin ứng dụng
Chỉnh sửa file `setup.iss`:
```ini
AppName=Cashflow Dashboard
AppVersion=1.0
AppPublisher=Your Company
AppPublisherURL=https://your-website.com
SetupIconFile=icon.ico
```

### Thêm icon
1. Tạo file icon (.ico)
2. Thêm vào `setup.iss`:
```ini
SetupIconFile=icon.ico
```

### Thêm license
1. Tạo file `LICENSE`
2. Thêm vào `setup.iss`:
```ini
LicenseFile=LICENSE
```

## 📱 Phân phối

### File cần chia sẻ:
- `CashflowDashboard-Setup.exe` (khoảng 22MB)

### Hướng dẫn người dùng:
1. Tải và cài đặt Java 17+ từ https://adoptium.net/
2. Chạy `CashflowDashboard-Setup.exe`
3. Làm theo hướng dẫn cài đặt
4. Ứng dụng sẽ tự động khởi động

## 🐛 Xử lý lỗi

### Lỗi "Java not found"
- Hướng dẫn cài đặt Java 17+
- Kiểm tra PATH environment variable

### Lỗi "Failed to start"
- Kiểm tra Java version
- Chạy `java -version` trong Command Prompt
- Cài đặt lại Java nếu cần

### Lỗi "Inno Setup not found"
- Cài đặt Inno Setup
- Thêm vào PATH
- Khởi động lại Command Prompt

## 📞 Hỗ trợ

Nếu gặp vấn đề:
1. Kiểm tra log trong `%TEMP%`
2. Chạy installer với `/LOG` parameter
3. Liên hệ support team

---
**Ngày tạo**: 22/06/2025  
**Phiên bản**: 1.0  
**Trạng thái**: ✅ Sẵn sàng sử dụng 