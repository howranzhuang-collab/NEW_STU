# 部署指南

## 环境变量配置

### 1. 创建环境变量文件

在 `frontend` 目录下创建以下环境变量文件：

#### `.env.development` (开发环境)
```env
# 开发环境配置
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_TITLE=来华留学生服务系统
```

#### `.env.production` (生产环境)
```env
# 生产环境配置
# 请根据实际部署的后端地址修改
VITE_API_BASE_URL=https://your-backend-domain.com/api
VITE_APP_TITLE=来华留学生服务系统
```

**注意**：请将 `your-backend-domain.com` 替换为实际的后端服务器地址。

## 构建生产包

### 1. 安装依赖（如果尚未安装）
```bash
cd frontend
npm install
```

### 2. 配置生产环境变量
确保 `.env.production` 文件中的 `VITE_API_BASE_URL` 指向正确的生产后端地址。

### 3. 构建项目
```bash
npm run build
```

构建完成后，会在 `frontend/dist` 目录下生成生产文件。

### 4. 验证构建结果
检查 `dist` 目录应包含：
- `index.html`
- `assets/` 目录（包含 JS、CSS 等静态资源）
- 其他静态文件

## 本地测试生产构建

### 方法1：使用 Vite Preview
```bash
npm run preview
```
访问 http://localhost:4173 进行测试。

### 方法2：使用 serve
```bash
# 全局安装 serve
npm install -g serve

# 启动服务
serve -s dist
```
访问显示的地址（通常是 http://localhost:3000）进行测试。

### 方法3：使用 http-server
```bash
# 全局安装 http-server
npm install -g http-server

# 启动服务
cd dist
http-server -p 3000
```

## 部署到静态服务器

### 部署到 Nginx

#### 1. 上传文件
将 `dist` 目录下的所有文件上传到服务器的网站根目录（如 `/usr/share/nginx/html` 或 `/var/www/html`）。

#### 2. Nginx 配置示例
创建或编辑 Nginx 配置文件（如 `/etc/nginx/sites-available/foreign-student`）：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 重定向到 HTTPS（可选）
    # return 301 https://$server_name$request_uri;
    
    root /var/www/foreign-student;
    index index.html;

    # 前端路由支持（Vue Router history 模式）
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # API 代理（如果需要）
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### 3. 启用配置并重启 Nginx
```bash
# 创建符号链接（如果使用 sites-available/sites-enabled）
sudo ln -s /etc/nginx/sites-available/foreign-student /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx
```

#### 4. 配置 HTTPS（推荐）
使用 Let's Encrypt 免费 SSL 证书：
```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

### 部署到 Vercel

1. 安装 Vercel CLI：
```bash
npm install -g vercel
```

2. 在项目根目录执行：
```bash
cd frontend
vercel
```

3. 按照提示完成部署。

4. 在 Vercel 项目设置中配置环境变量：
   - `VITE_API_BASE_URL`: 生产后端地址

### 部署到 Netlify

1. 安装 Netlify CLI：
```bash
npm install -g netlify-cli
```

2. 在项目根目录执行：
```bash
cd frontend
netlify deploy --prod
```

3. 在 Netlify 项目设置中配置环境变量和构建命令：
   - Build command: `npm run build`
   - Publish directory: `dist`
   - 环境变量：`VITE_API_BASE_URL`

## 后端部署确认

### 1. 后端 CORS 配置
确保后端允许前端域名的跨域请求。在 Spring Boot 配置中添加：

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("https://your-frontend-domain.com")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

### 2. 后端部署
- 确保后端 JAR 包部署在服务器
- 开放 8080 端口（或配置的端口）
- 如果使用 Nginx 反向代理，配置代理规则

### 3. 验证后端接口
使用浏览器或 Postman 测试后端接口是否正常：
```bash
curl https://your-backend-domain.com/api/health
```

## 验证测试清单

- [ ] 构建生产包成功，dist 目录存在且包含所有文件
- [ ] 本地测试生产构建，所有功能正常
- [ ] 环境变量配置正确，API 地址指向生产后端
- [ ] 部署到服务器后，通过域名访问系统
- [ ] 登录功能正常
- [ ] 所有功能模块正常运行
- [ ] 无跨域错误
- [ ] 静态资源加载正常
- [ ] 后端接口调用正常
- [ ] HTTPS 配置正确（如果使用）

## 常见问题

### 1. 路由 404 错误
确保服务器配置了 Vue Router history 模式的支持（如 Nginx 的 `try_files` 配置）。

### 2. API 请求失败
- 检查 `VITE_API_BASE_URL` 环境变量是否正确
- 检查后端 CORS 配置
- 检查网络连接和防火墙设置

### 3. 静态资源加载失败
- 检查资源路径是否正确
- 检查服务器文件权限
- 检查 Nginx 配置中的静态资源路径

### 4. 构建失败
- 检查 Node.js 版本（建议 16+）
- 清除 node_modules 和重新安装依赖
- 检查 TypeScript 编译错误

