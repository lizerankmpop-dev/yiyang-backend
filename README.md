# 东软颐养中心养老院管理系统 · 后端

基于 Spring Boot + MyBatis-Plus 的养老院综合管理系统后端，覆盖老人档案、床位、护理、餐饮、排班、数据看板与权限管理等业务模块，提供标准 RESTful API 与在线接口文档。

> 配套前端仓库：[yiyang-frontend](https://github.com/lizerankmpop-dev/yiyang-frontend)

## 功能模块

| 模块 | 说明 | 主要实体 |
|---|---|---|
| 老人档案 | 基本信息、家属、护理等级、偏好、外出、退住 | `Customer` `CustomerFamily` `Outward` `Backdown` |
| 床位管理 | 床位、楼层房间、换床、床位历史 | `Bed` `BedBatch` `Room` `ChangeBed` |
| 护理管理 | 护工、护理项目、护理等级、护理记录与任务、健康档案 | `Nurse` `NursingLevel` `NursingRecord` `NursingTask` `HealthRecord` |
| 餐饮管理 | 菜品、套餐、菜单 | `Food` `Meal` `MealFood` `Menu` |
| 排班与提醒 | 护工排班、用药/事项提醒（定时任务驱动） | `Schedule` `ReminderMessage` |
| 系统管理 | 管理员、角色、权限、登录日志、操作日志 | `Admin` `Role` `RoleMenu` `LoginLog` `OperationLog` |
| 数据看板 | 首页统计、趋势数据 | `StatisticsData` `TrendData` |

## 技术栈

| 组件 | 版本 | 用途 |
|---|---|---|
| Spring Boot | 2.6.15 | 应用框架（Java 8） |
| MyBatis-Plus | 3.4.3.4 | ORM / 分页 |
| MySQL | 8.x + HikariCP | 持久化 |
| Redis | - | 缓存（可选，未安装也能启动） |
| SpringDoc OpenAPI | 1.6.15 | 在线接口文档（Swagger UI） |
| JJWT | 0.11.5 | JWT 双令牌鉴权 |
| EasyExcel | 3.3.2 | 老人档案 Excel 导入导出 |
| Lombok | 1.18.30 | 代码简化 |

## 快速开始

### 1. 环境要求

- JDK 8
- Maven 3.6+
- MySQL 8.x
- Redis（可选）

### 2. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

`sql/init.sql` 包含 16 张表的建表语句与初始数据；`sql/rebuild.sql` 用于清库重建。

### 3. 配置（重要：不要硬编码密码）

数据库账号密码、JWT 密钥均通过**环境变量**注入，缺省值不可用于生产：

```bash
# Linux / macOS
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=你的数据库密码
export JWT_SECRET=至少32字节的随机字符串

# Windows PowerShell
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="你的数据库密码"
$env:JWT_SECRET="至少32字节的随机字符串"
```

也可以在 IDE 的运行配置（Run Configuration）里添加同名环境变量。

### 4. 启动

```bash
mvn spring-boot:run
```

服务默认端口 **8081**。

### 5. 访问接口文档

启动后打开：http://localhost:8081/swagger-ui/index.html

## 项目结构

```
src/main/java/com/neusoft/
├── NeusoftApplication.java   启动类
├── common/                   统一响应体 R
├── config/                   跨域、JWT/登录拦截器、MyBatis-Plus、Redis、全局异常处理
├── controller/               32 个 REST 控制器
├── dto/                      Excel 导入 DTO
├── entity/                   32 个实体
├── listener/                 EasyExcel 读取监听器
├── mapper/                   33 个 MyBatis-Plus Mapper
├── service/                  业务层
├── task/                     定时任务（提醒）
└── utils/                    JWT、文件上传、密码、操作日志工具
src/main/resources/
├── application.yml           主配置（敏感项走环境变量）
├── application-dev.yml       开发环境配置
└── sql/test_data_simple.sql  测试数据
sql/
├── init.sql                  建库建表 + 初始数据
└── rebuild.sql               重建脚本
```

## 鉴权说明

登录成功后签发双令牌：

- `accessToken`：有效期 1 天，请求头 `Authorization: Bearer <token>`
- `refreshToken`：有效期 7 天，用于续签

拦截器统一校验，白名单放行登录、文档等接口。

## 接口约定

统一响应体：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

分页参数：`pageNum` / `pageSize`（默认 10，最大 100）。

## License

[MIT](LICENSE)
