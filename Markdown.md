
来华留学生服务系统实施计划
概述
本实施计划是为 Cursor AI 代码编辑器设计的逐步指导文档，用于开发“来华留学生服务系统”的后端部分。该系统基于 Spring Boot 框架，旨在实现留学生从入学到毕业的全生命周期管理，包括招生、学生注册、学籍、收费、住宿、签证和校友管理等模块。作为大学课程设计作业，重点实现基础 CRUD 操作和核心业务流程，不引入复杂功能。

计划遵循“理解 → 计划 → 执行 → 验证”的开发循环，采用迭代开发模式。将任务分解为小而具体的步骤，确保每步可验证。Cursor 将优先分析现有代码、模仿项目风格，并严格遵守安全和编码规范，如使用描述性命名、统一错误处理和 RESTful API 原则。


准备阶段
本阶段聚焦于环境和基础设置，确保项目可启动。每个步骤小而具体，并包含验证测试。

打开 Cursor 编辑器并安装必要插件：启动 Cursor 编辑器，导航到扩展市场，搜索并安装 Java Extension Pack 和 Spring Boot Tools 插件。
验证测试：重启 Cursor，打开一个 Java 文件，检查是否出现 Spring Boot 特定的提示或运行配置选项；如果未出现，检查插件是否启用。
使用 Spring Initializr 创建新项目：访问 start.spring.io 网站，选择 Java 17、Maven、Spring Boot 3.x，添加依赖：Spring Web、Spring Security、MyBatis-Plus、MySQL Connector、Lombok、Spring Boot DevTools；下载并解压项目到本地目录。
验证测试：打开项目在 Cursor 中，检查 pom.xml 文件是否包含所有指定依赖；如果缺失，手动添加并保存。
初始化 Git 仓库：在项目根目录打开终端，运行 git init 命令初始化仓库，然后添加所有文件并提交初始 commit。
验证测试：运行 git status 命令，确认没有未跟踪文件；运行 git log 检查初始 commit 消息是否存在。
安装 MySQL 数据库：下载并安装 MySQL 8.0，启动服务，创建数据库 foreign_student_db，使用默认 root 用户和密码。
验证测试：使用 MySQL Workbench 或命令行连接数据库，运行 SHOW DATABASES; 命令，确认 foreign_student_db 存在。
配置应用数据源：在 src/main/resources/application.yml 文件中添加 spring.datasource 配置，包括 url、username 和 password，使用环境变量避免硬编码。
验证测试：运行应用（mvn spring-boot:run），检查控制台日志是否显示数据源连接成功；如果报错，检查配置语法。
运行初始应用测试：在 Cursor 中运行应用，访问 http://localhost:8080。
验证测试：浏览器显示 404 或 Whitelabel Error 页面，表示应用启动正常；检查日志无启动异常。
开发阶段：TODO 列表
采用 TODO 列表规划开发，按模块迭代。每个模块分解为小步骤，每个步骤有执行指导和验证测试。更新 TODO 状态时，仅标记一个为“进行中”。

初始 TODO
 理解项目结构：扫描 pom.xml 和 src 目录，记录依赖列表和包结构。
验证测试：手动列出依赖，比较与初始izr 生成的一致性；如果不符，暂停求反馈。
 计划整体模块：基于数据设计，决策使用 MyBatis-Plus 简化 CRUD。
验证测试：记录决策笔记，确认与项目规范一致。
模块 1: 认证系统
理解现有配置：扫描 config 包，语义搜索 SecurityConfig 类，检查 Spring Security 相关设置。
验证测试：列出发现的配置项，确认是否已启用 WebSecurity。
创建 User 实体类：在 entity 包下新建 User 类文件，添加必要字段如 id、username、password、role，使用 Lombok 注解简化。
验证测试：编译项目，检查无语法错误；打开类文件确认字段匹配设计。
创建 User Mapper 接口：在 mapper 包下新建 UserMapper 接口，扩展 BaseMapper<User>。
验证测试：运行应用，检查日志显示 MyBatis-Plus 映射成功。
实现 User Service 类：在 service 包下新建 UserService 类，添加注册和登录方法，使用事务注解。
验证测试：添加临时日志，运行服务方法，检查日志输出正确。
配置 SecurityConfig 类：在 config 包下更新或新建 SecurityConfig，启用 JWT 认证，定义过滤器链。
验证测试：运行应用，尝试未授权访问，检查返回 401 错误。
添加 AuthController 类：在 controller 包下新建 AuthController，添加登录端点，返回 JWT token。
验证测试：使用 Postman 发送 POST /login 请求，检查响应包含有效 token。
处理密码加密：在 UserService 中集成 BCryptPasswordEncoder 加密存储密码。
验证测试：注册用户，查询数据库确认密码为哈希值。
模块 2: 招生管理 + 学生注册
理解实体类：扫描 entity 包，检查 AdmissionProject 和 Application 类是否存在。
验证测试：如果缺失，记录并创建；确认字段类型正确。
创建 AdmissionProject 实体：在 entity 包下新建类，添加字段如 title、quota、deadline。
验证测试：编译，检查无错误。
创建 Application 实体：类似步骤，添加字段如 projectId、studentId、status。
验证测试：编译确认。
创建 Mapper 接口：为每个实体新建 Mapper，扩展 BaseMapper。
验证测试：运行应用，日志显示映射。
实现 Service 类：新建 AdmissionService，添加项目 CRUD 和申请提交方法，检查 deadline。
验证测试：调用服务，检查返回数据匹配输入。
添加 Controller 类：新建 AdmissionController，添加 REST 端点，如 GET /projects。
验证测试：Postman 测试端点，确认响应 JSON 格式正确。
集成文件上传：在申请端点添加 MultipartFile 支持，配置上传目录。
验证测试：上传文件，检查服务器目录和数据库路径。
模块 3: 学籍管理
理解相关实体：扫描 StudentRecord、Attendance、Exam 类。
验证测试：确认字段完整。
创建 StudentRecord 实体：新建类，添加 enrollmentDate 等字段。
验证测试：编译无误。
创建 Attendance 和 Exam 实体：类似步骤。
验证测试：编译确认。
创建 Mapper 接口：为每个新建 Mapper。
验证测试：日志映射成功。
实现 Service 类：新建 StudentService，添加录入逻辑，使用卫语句。
验证测试：测试方法，检查边界输入抛异常。
添加 Controller 类：新建 StudentController，添加查看和编辑端点。
验证测试：Postman 测试，确认权限控制。
模块 4: 收费管理
理解 Fees 实体：扫描类文件。
验证测试：字段匹配设计。
创建 Fees 实体：新建类，添加 amount、dueDate 等。
验证测试：编译。
创建 Mapper 接口：扩展 BaseMapper。
验证测试：日志。
实现 Service 类：添加计算欠费和标记已缴方法，确保幂等。
验证测试：重复调用，检查无重复更新。
添加 Controller 类：添加 /fees 端点。
验证测试：Postman，确认响应。
模块 5: 住宿管理
理解 Dorm 实体：扫描相关类。
验证测试：完整性。
创建 DormBuilding 实体：新建类。
验证测试：编译。
创建 DormRoom 和 DormAllocation 实体：类似。
验证测试：编译。
创建 Mapper 接口：为每个。
验证测试：日志。
实现 Service 类：添加分配逻辑，检查容量。
验证测试：测试超限抛异常。
添加 Controller 类：添加分配端点。
验证测试：Postman，确认事务。
模块 6: 签证管理
理解 Visas 实体：扫描。
验证测试：字段。
创建 Visas 实体：新建。
验证测试：编译。
创建 Mapper 接口：扩展。
验证测试：日志。
实现 Service 类：添加到期计算。
验证测试：不同日期，检查状态。
添加 Controller 类：添加查询端点。
验证测试：Postman，确认提醒。
模块 7: 校友管理
理解 Alumni 实体：扫描。
验证测试：完整。
创建 Alumni 实体：新建。
验证测试：编译。
创建 Mapper 接口：扩展。
验证测试：日志。
实现 Service 类：添加毕业同步。
验证测试：测试同步数据一致。
添加 Controller 类：添加列表端点。
验证测试：Postman，确认响应。
整合与测试阶段
添加全局异常处理：新建 GlobalExceptionHandler 类。
验证测试：抛异常，检查统一响应。
创建统一响应 DTO：新建 Result 类。
验证测试：集成到 Controller，检查 JSON 包装。
引入 Swagger：添加依赖，配置文档。
验证测试：访问 /swagger-ui，检查 API 列表。
全流程测试：使用 Postman 模拟完整流程。
验证测试：检查数据一致性，无权限泄漏。
优化与部署阶段（可选）
添加分页支持：集成 PageHelper。
验证测试：查询端点，返回分页数据。
添加日志配置：使用 SLF4J。
验证测试：检查日志文件输出。
打包应用：运行 mvn package。
验证测试：运行 JAR，访问端点。
清理项目：移除临时文件，提交 commit。
验证测试：git status 干净。
注意事项
每步前更新 TODO，仅一任务进行中。
不确定时暂停求反馈。
记录所有决策，如认证选择。
确保步骤匹配项目风格，无复杂性。