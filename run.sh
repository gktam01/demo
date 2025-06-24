#!/bin/bash

# Script để chạy ứng dụng CashflowApp
echo "Đang khởi động CashflowApp..."

# Kiểm tra xem file JAR có tồn tại không
if [ ! -f "target/cashflow-dashboard-1.0-SNAPSHOT.jar" ]; then
    echo "Lỗi: Không tìm thấy file JAR. Vui lòng chạy 'mvn clean package' trước."
    exit 1
fi

# Chạy ứng dụng
java -jar target/cashflow-dashboard-1.0-SNAPSHOT.jar

echo "Ứng dụng đã kết thúc." 