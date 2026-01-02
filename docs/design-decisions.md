# 设计决策文档

## 技术栈决策

### 1. 数据访问层：MyBatis-Plus

**决策：** 使用 MyBatis-Plus 简化 CRUD 操作

**理由：**
1. **简化开发**：MyBatis-Plus 提供了 `BaseMapper<T>` 接口，自动实现基础 CRUD 方法
   - `insert()` - 插入
   - `deleteById()` - 根据 ID 删除
   - `updateById()` - 根据 ID 更新
   - `selectById()` - 根据 ID 查询
   - `selectList()` - 查询列表
   - `selectPage()` - 分页查询

2. **减少样板代码**：无需为每个实体编写重复的 SQL 映射
3. **灵活扩展**：支持自定义 SQL，满足复杂查询需求
4. **自动填充**：支持字段自动填充（创建时间、更新时间等）
5. **逻辑删除**：内置逻辑删除支持，已在 `application.yml` 中配置

**配置说明：**
```yaml
mybatis-plus:
  global-config:
    db-config:
      id-type: auto              # 主键自增
      logic-delete-field: deleted # 逻辑删除字段
      logic-delete-value: 1      # 删除值
      logic-not-delete-value: 0  # 未删除值
```

**使用方式：**
```java
// Mapper 接口继承 BaseMapper
public interface UserMapper extends BaseMapper<User> {
    // 可添加自定义方法
}

// Service 中直接使用
@Autowired
private UserMapper userMapper;

public User getUserById(Long id) {
    return userMapper.selectById(id);
}
```

### 2. 认证方案：Spring Security + JWT

**决策：** 使用 Spring Security 框架 + JWT Token 进行认证

**理由：**
1. **标准化**：Spring Security 是 Spring 生态系统的标准安全框架
2. **无状态认证**：JWT Token 支持无状态认证，适合 RESTful API
3. **灵活配置**：可以自定义安全规则和过滤器链
4. **已包含依赖**：项目已包含 `spring-boot-starter-security`

**实现计划：**
- 使用 `BCryptPasswordEncoder` 加密密码
- 实现 JWT Token 生成和验证
- 配置 Security 过滤器链
- 实现登录和注册接口

### 3. 项目分层架构

**决策：** 采用标准三层架构 + DTO 层

**架构层次：**
1. **Controller 层**：处理 HTTP 请求，参数验证，调用 Service
2. **Service 层**：业务逻辑处理，事务管理
3. **Mapper 层**：数据访问，SQL 执行
4. **Entity 层**：数据模型，对应数据库表
5. **DTO 层**：数据传输对象，用于 API 响应封装

**优势：**
- 职责清晰，易于维护
- 符合单一职责原则
- 便于单元测试

### 4. 统一响应格式

**决策：** 使用统一的 Result 类封装 API 响应

**设计：**
```java
public class Result<T> {
    private Integer code;      // 状态码
    private String message;    // 消息
    private T data;            // 数据
}
```

**优势：**
- 统一 API 响应格式
- 便于前端处理
- 错误信息标准化

### 5. 异常处理

**决策：** 使用全局异常处理器统一处理异常

**实现：**
- 创建 `GlobalExceptionHandler` 类
- 使用 `@ControllerAdvice` 注解
- 统一异常响应格式
- 记录异常日志

### 6. 数据库设计原则

**决策：** 遵循以下数据库设计原则

1. **主键策略**：使用自增主键（`id-type: auto`）
2. **逻辑删除**：使用 `deleted` 字段实现逻辑删除，不物理删除数据
3. **时间字段**：统一使用 `created_at` 和 `updated_at` 记录时间
4. **字符集**：使用 `utf8mb4` 支持完整 Unicode（包括 emoji）
5. **命名规范**：表名和字段名使用下划线命名（snake_case）

### 7. API 设计原则

**决策：** 遵循 RESTful API 设计规范

**原则：**
- 使用 HTTP 动词（GET、POST、PUT、DELETE）
- 使用名词表示资源
- 使用状态码表示结果
- 使用 JSON 格式传输数据

**示例：**
```
GET    /api/students        # 获取学生列表
GET    /api/students/{id}   # 获取单个学生
POST   /api/students        # 创建学生
PUT    /api/students/{id}   # 更新学生
DELETE /api/students/{id}   # 删除学生
```

## 模块划分

根据业务需求，系统分为以下模块：

1. **认证系统**（Module 1）
   - 用户注册、登录
   - JWT Token 管理
   - 密码加密

2. **招生管理 + 学生注册**（Module 2）
   - 招生项目管理
   - 申请提交和管理
   - 文件上传

3. **学籍管理**（Module 3）
   - 学生学籍信息管理
   - 考勤管理
   - 考试管理

4. **收费管理**（Module 4）
   - 费用计算
   - 缴费记录
   - 欠费查询

5. **住宿管理**（Module 5）
   - 宿舍楼管理
   - 房间管理
   - 分配管理

6. **签证管理**（Module 6）
   - 签证信息管理
   - 到期提醒

7. **校友管理**（Module 7）
   - 校友信息管理
   - 毕业同步

## 开发规范

### 代码规范
1. **命名规范**：
   - 类名：大驼峰（PascalCase）
   - 方法名和变量名：小驼峰（camelCase）
   - 常量：全大写下划线（UPPER_SNAKE_CASE）
   - 包名：全小写

2. **注释规范**：
   - 类和方法添加 JavaDoc 注释
   - 复杂逻辑添加行内注释

3. **异常处理**：
   - 使用卫语句避免深层嵌套
   - 统一异常处理，不暴露系统内部信息

4. **事务管理**：
   - Service 层方法使用 `@Transactional` 注解
   - 只读操作使用 `@Transactional(readOnly = true)`

## 验证测试

### 与项目规范一致性检查

✅ **技术选型**：符合项目要求（Spring Boot + MyBatis-Plus）
✅ **架构设计**：符合标准分层架构
✅ **开发规范**：遵循 Java 编码规范和 Spring Boot 最佳实践
✅ **安全性**：使用 Spring Security 和密码加密
✅ **可维护性**：代码结构清晰，职责分明

## 总结

所有设计决策均符合项目要求和技术规范，采用成熟稳定的技术栈，确保项目的可维护性和可扩展性。使用 MyBatis-Plus 可以显著简化 CRUD 操作，提高开发效率。

