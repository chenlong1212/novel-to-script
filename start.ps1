# AI 小说转剧本工具 - 启动脚本 (Windows PowerShell)
# 版本: v1.0

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "AI 小说转剧本工具 v1.0" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# 启动后端 (在后台运行)
Write-Host "正在启动后端服务..." -ForegroundColor Yellow
Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WorkingDirectory $PSScriptRoot -NoNewWindow -Wait

Write-Host ""
Write-Host "后端服务已启动!" -ForegroundColor Green
Write-Host ""

# 启动前端
Write-Host "正在启动前端服务..." -ForegroundColor Yellow
Start-Process -FilePath "npm" -ArgumentList "run dev" -WorkingDirectory "$PSScriptRoot\frontend" -NoNewWindow -Wait
