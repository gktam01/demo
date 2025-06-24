# 🚀 Hướng dẫn nhanh tạo Installer

## ✅ Đã sẵn sàng!

Dự án của bạn đã được chuẩn bị để tạo installer. Các file đã tạo:

- ✅ `cashflow-dashboard-1.0-SNAPSHOT.jar` (22MB) - Ứng dụng chính
- ✅ `run-app.bat` - Script chạy ứng dụng
- ✅ `setup.iss` - Cấu hình installer
- ✅ `create-installer.bat` - Script tự động tạo installer

## 🎯 Bước tiếp theo

### 1. Cài đặt Inno Setup
```
Tải từ: https://jrsoftware.org/isinfo.php
Cài đặt với tùy chọn "Add to PATH"
```

### 2. Tạo installer
```bash
# Chạy script tự động
create-installer.bat
```

### 3. Kết quả
```
installer/CashflowDashboard-Setup.exe
```

## 📦 Phân phối

Chỉ cần chia sẻ file `CashflowDashboard-Setup.exe` cho người dùng!

## 🔧 Test ngay

Để test ứng dụng trước khi tạo installer:
```bash
run-app.bat
```

---
**Trạng thái**: ✅ Sẵn sàng tạo installer  
**Kích thước**: ~22MB  
**Yêu cầu**: Java 17+ 