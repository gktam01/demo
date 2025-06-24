#!/bin/bash

echo "🚀 Khởi động Cashflow Dashboard..."

# Kiểm tra Java
if ! command -v java &> /dev/null; then
    echo "❌ Lỗi: Java không được cài đặt hoặc không tìm thấy trong PATH"
    exit 1
fi

echo "✅ Java version:"
java -version

# Compile và chạy với Maven
echo "📦 Compiling project..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "✅ Compile thành công!"
    echo "🎯 Chạy ứng dụng..."
    mvn exec:java -Dexec.mainClass="com.cashflow.ModernCashflowApp"
else
    echo "❌ Compile thất bại!"
    exit 1
fi 