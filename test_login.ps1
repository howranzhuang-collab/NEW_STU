# PowerShell 测试登录接口脚本

Write-Host "测试登录接口..." -ForegroundColor Green
Write-Host "URL: http://localhost:8080/api/auth/login" -ForegroundColor Yellow
Write-Host "请求体: {`"username`":`"admin1`",`"password`":`"admin1`"}" -ForegroundColor Yellow
Write-Host ""

$body = @{
    username = "admin1"
    password = "admin1"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $body `
        -ErrorAction Stop
    
    Write-Host "响应:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "错误:" -ForegroundColor Red
    Write-Host $_.Exception.Message
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应体: $responseBody"
    }
}

Write-Host ""
Write-Host "测试完成" -ForegroundColor Green

