#!/bin/bash

# AI 小说转剧本工具 - 启动脚本

echo "========================================="
echo "AI 小说转剧本工具 v1.0"
echo "========================================="
echo ""

# 启动后端 (在后台运行)
echo "正在启动后端服务..."
cd "$(dirname "$0")/backend"
mvn spring-boot:run &
BACKEND_PID=$!

# 等待后端启动
echo "等待后端启动..."
sleep 10

# 启动前端
echo "正在启动前端服务..."
cd "$(dirname "$0")/frontend"
npm run dev &
FRONTEND_PID=$!

echo ""
echo "========================================="
echo "服务已启动!"
echo "后端: http://localhost:8080"
echo "前端: http://localhost:5173"
echo "========================================="
echo ""
echo "按 Ctrl+C 停止所有服务"

# 等待用户中断
wait
