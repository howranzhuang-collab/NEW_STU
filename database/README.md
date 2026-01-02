# MySQL 数据库安装和配置指南

## 安装 MySQL 8.0

### Windows 安装步骤：

1. **下载 MySQL**
   - 访问 MySQL 官网：https://dev.mysql.com/downloads/mysql/
   - 下载 MySQL 8.0 Windows 安装程序（推荐 MySQL Installer for Windows）

2. **安装 MySQL**
   - 运行安装程序
   - 选择 "Developer Default" 或 "Server only" 安装类型
   - 完成安装向导，设置 root 用户密码（请记住此密码）

3. **启动 MySQL 服务**
   - 打开 Windows 服务管理器（services.msc）
   - 找到 "MySQL80" 服务
   - 确保服务状态为"正在运行"

## 创建数据库

### 方法 1：使用 MySQL 命令行

```bash
# 连接到 MySQL（需要输入 root 密码）
mysql -u root -p

# 在 MySQL 命令行中执行
CREATE DATABASE IF NOT EXISTS foreign_student_db 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

# 验证数据库创建
SHOW DATABASES;

# 退出
EXIT;
```

### 方法 2：使用 MySQL Workbench

1. 打开 MySQL Workbench
2. 连接到本地 MySQL 服务器（使用 root 用户和密码）
3. 在查询窗口中执行 `database/init.sql` 文件中的 SQL 语句
4. 或手动创建数据库：
   - 右键点击左侧连接
   - 选择 "Create Schema"
   - 输入数据库名称：`foreign_student_db`
   - 设置字符集：`utf8mb4`
   - 设置排序规则：`utf8mb4_unicode_ci`
   - 点击 "Apply"

### 方法 3：使用命令行执行 SQL 文件

```bash
# 如果 MySQL 在 PATH 中
mysql -u root -p < database/init.sql

# 或指定完整路径
mysql -u root -p -e "source database/init.sql"
```

## 验证数据库创建

### 使用 MySQL 命令行：

```bash
mysql -u root -p
SHOW DATABASES;
```

应该能看到 `foreign_student_db` 在列表中。

### 使用 MySQL Workbench：

在左侧 Schema 列表中应该能看到 `foreign_student_db` 数据库。

## 配置应用连接

数据库连接信息已配置在 `src/main/resources/application.yml` 中：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/foreign_student_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
```

### 环境变量配置（推荐）

为避免硬编码密码，建议使用环境变量：

**Windows PowerShell:**
```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
```

**Windows CMD:**
```cmd
set DB_USERNAME=root
set DB_PASSWORD=your_password
```

或在 `application.yml` 中直接修改（不推荐用于生产环境）。

## 测试连接

安装并配置完成后，运行应用：

```bash
mvn spring-boot:run
```

检查控制台日志，应该能看到数据源连接成功的消息。

