#!/bin/bash
# 测试登录接口脚本

echo "测试登录接口..."
echo "URL: http://localhost:8080/api/auth/login"
echo "请求体: {\"username\":\"admin1\",\"password\":\"admin1\"}"
echo ""

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin1\",\"password\":\"admin1\"}" \
  -v

echo ""
echo "测试完成"

