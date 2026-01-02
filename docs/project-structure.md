# 项目结构分析文档

## 依赖列表

### 核心框架依赖
1. **Spring Boot 3.2.0** (parent)
   - 提供 Spring Boot 基础功能和自动配置

2. **Spring Web** (`spring-boot-starter-web`)
   - 提供 Web MVC 功能
   - 包含内嵌 Tomcat 服务器
   - RESTful API 支持

3. **Spring Security** (`spring-boot-starter-security`)
   - 提供认证和授权功能
   - 安全过滤器链

### 数据访问层依赖
4. **MyBatis-Plus 3.5.5** (`mybatis-plus-boot-starter`)
   - 简化 MyBatis 使用
   - 提供通用 CRUD 操作
   - 自动代码生成支持

5. **MySQL Connector** (`mysql-connector-j`)
   - MySQL 数据库驱动
   - 运行时依赖

### 开发工具依赖
6. **Lombok**
   - 简化 Java 代码
   - 自动生成 getter/setter/toString 等方法
   - 可选依赖

7. **Spring Boot DevTools** (`spring-boot-devtools`)
   - 开发时热重载
   - 自动重启功能
   - 运行时可选依赖

### 测试依赖
8. **Spring Boot Test** (`spring-boot-starter-test`)
   - 单元测试和集成测试支持
   - 包含 JUnit、Mockito 等

9. **Spring Security Test** (`spring-security-test`)
   - Spring Security 测试支持

## 依赖验证

### 与 Spring Initializr 生成的一致性检查

**初始需求：**
- Java 17 ✓
- Maven ✓
- Spring Boot 3.x ✓ (3.2.0)
- Spring Web ✓
- Spring Security ✓
- MyBatis-Plus ✓
- MySQL Connector ✓
- Lombok ✓
- Spring Boot DevTools ✓

**验证结果：**
✅ 所有依赖与 Spring Initializr 配置要求完全一致

**额外依赖：**
- Spring Boot Test（标准测试依赖，通常自动包含）
- Spring Security Test（测试支持，合理添加）

## 项目包结构

### 当前结构
```
src/main/java/com/university/foreignstudent/
├── ForeignStudentApplication.java    # 主应用类
└── controller/
    └── TestController.java           # 测试控制器
```

### 预期完整结构（待开发）
```
src/main/java/com/university/foreignstudent/
├── ForeignStudentApplication.java    # 主应用类
├── config/                            # 配置类
│   └── SecurityConfig.java           # Spring Security 配置
├── entity/                            # 实体类
│   ├── User.java                     # 用户实体
│   ├── AdmissionProject.java        # 招生项目实体
│   ├── Application.java              # 申请实体
│   ├── StudentRecord.java           # 学籍实体
│   ├── Fees.java                     # 收费实体
│   ├── DormBuilding.java            # 宿舍楼实体
│   ├── DormRoom.java                 # 宿舍房间实体
│   ├── DormAllocation.java          # 宿舍分配实体
│   ├── Visas.java                   # 签证实体
│   └── Alumni.java                  # 校友实体
├── mapper/                            # MyBatis-Plus Mapper 接口
│   ├── UserMapper.java
│   ├── AdmissionProjectMapper.java
│   ├── ApplicationMapper.java
│   ├── StudentRecordMapper.java
│   ├── FeesMapper.java
│   ├── DormBuildingMapper.java
│   ├── DormRoomMapper.java
│   ├── DormAllocationMapper.java
│   ├── VisasMapper.java
│   └── AlumniMapper.java
├── service/                           # 业务逻辑层
│   ├── UserService.java
│   ├── AdmissionService.java
│   ├── StudentService.java
│   ├── FeesService.java
│   ├── DormService.java
│   ├── VisasService.java
│   └── AlumniService.java
├── controller/                        # 控制器层
│   ├── AuthController.java          # 认证控制器
│   ├── AdmissionController.java     # 招生控制器
│   ├── StudentController.java       # 学生控制器
│   ├── FeesController.java          # 收费控制器
│   ├── DormController.java          # 住宿控制器
│   ├── VisasController.java        # 签证控制器
│   └── AlumniController.java       # 校友控制器
├── dto/                               # 数据传输对象
│   └── Result.java                  # 统一响应结果
└── exception/                         # 异常处理
    └── GlobalExceptionHandler.java  # 全局异常处理器
```

## 配置文件

### application.yml
- 数据源配置（MySQL）
- MyBatis-Plus 配置
- 服务器端口配置
- 日志配置

## 构建配置

### Maven 配置
- Java 版本：17
- 编码：UTF-8
- Spring Boot Maven Plugin 配置
- Lombok 排除配置（构建时）

## 总结

项目结构符合 Spring Boot 标准分层架构：
- **Controller 层**：处理 HTTP 请求
- **Service 层**：业务逻辑处理
- **Mapper 层**：数据访问（MyBatis-Plus）
- **Entity 层**：数据模型

所有依赖配置正确，与 Spring Initializr 生成的项目一致。

