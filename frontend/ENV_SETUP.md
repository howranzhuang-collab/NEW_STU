# 环境变量配置说明

## 创建环境变量文件

在 `frontend` 目录下创建以下文件：

### 1. `.env.development` (开发环境)

创建文件 `frontend/.env.development`，内容如下：

```env
# 开发环境配置
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_TITLE=来华留学生服务系统
```

### 2. `.env.production` (生产环境)

创建文件 `frontend/.env.production`，内容如下：

```env
# 生产环境配置
# 请根据实际部署的后端地址修改
VITE_API_BASE_URL=https://your-backend-domain.com/api
VITE_APP_TITLE=来华留学生服务系统
```

**重要**：部署到生产环境时，请将 `your-backend-domain.com` 替换为实际的后端服务器地址。

### 3. `.env.local` (本地覆盖，可选)

如果需要覆盖默认配置，可以创建 `.env.local` 文件（此文件会被 git 忽略）。

## 环境变量说明

- `VITE_API_BASE_URL`: API 基础地址，必须以 `/api` 结尾
- `VITE_APP_TITLE`: 应用标题

## 验证配置

构建时会根据当前模式（development 或 production）自动使用对应的环境变量文件。

开发模式：`npm run dev` → 使用 `.env.development`
生产构建：`npm run build` → 使用 `.env.production`

