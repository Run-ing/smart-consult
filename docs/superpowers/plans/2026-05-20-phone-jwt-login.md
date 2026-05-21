# 手机号 JWT 登录实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标：** 实现手机号验证码登录，未注册手机号自动注册，登录后返回 JWT，并新增前后端分离的 Vue 登录页面。

**架构：** 后端在现有 Spring Boot 单模块中新增 `auth`、`user` 模块，使用 MyBatis-Plus 访问 MySQL，验证码记录落表保存，短信发送阶段先 mock。前端在仓库内新增 `smart-consult-web`，使用 Vue 3 + Vite + TypeScript 调用后端 API 并保存 JWT。

**技术栈：** Spring Boot 3、Java 17、MyBatis-Plus、MySQL、JJWT、Vue 3、Vite、TypeScript、Axios、Vue Router。

---

## 文件结构

- 修改 `pom.xml`：新增 MyBatis-Plus、MySQL、JWT、H2 测试依赖。
- 修改 `src/main/resources/application.yml`：新增 datasource、MyBatis-Plus、JWT、短信 mock 配置。
- 新增 `src/main/resources/db/schema.sql`：提供 MySQL 建表 SQL。
- 新增 `src/main/java/com/example/smartconsult/user/*`：用户实体、Mapper、Service、DTO。
- 新增 `src/main/java/com/example/smartconsult/auth/*`：认证 DTO、Controller、Service、JWT、拦截器、上下文。
- 修改 `src/main/java/com/example/smartconsult/SmartConsultApplication.java`：启用 Mapper 扫描。
- 新增 `src/test/java/com/example/smartconsult/auth/AuthFlowTest.java`：覆盖验证码、自动注册、JWT、重复使用验证码等核心行为。
- 新增 `src/test/resources/application-test.yml`：使用 H2 的 MySQL 兼容模式跑后端集成测试。
- 新增 `smart-consult-web/*`：Vue 前端工程、登录页、登录后首页、API 封装。

## Task 1：后端依赖与配置

**文件：**
- 修改：`pom.xml`
- 修改：`src/main/resources/application.yml`
- 修改：`src/main/java/com/example/smartconsult/SmartConsultApplication.java`
- 新增：`src/main/resources/db/schema.sql`
- 新增：`src/test/resources/application-test.yml`

- [ ] **Step 1：写配置层面的最小失败测试**

在 `src/test/java/com/example/smartconsult/auth/AuthFlowTest.java` 中先创建 Spring Boot 测试类，注入 `MockMvc`，调用尚不存在的 `/auth/sms-code`。

- [ ] **Step 2：运行测试确认失败**

运行：`mvn -Dtest=AuthFlowTest test`

预期：失败，原因是接口或依赖尚未实现。

- [ ] **Step 3：补齐依赖和测试配置**

新增 MyBatis-Plus、MySQL、JWT、H2 依赖；配置 datasource 从环境变量读取；测试环境使用 H2。

- [ ] **Step 4：补齐 schema**

新增 `sys_user` 和 `sms_verification_code` 表结构，测试环境通过 `spring.sql.init.schema-locations` 自动初始化。

## Task 2：验证码申请与登录服务

**文件：**
- 新增：`src/main/java/com/example/smartconsult/auth/AuthController.java`
- 新增：`src/main/java/com/example/smartconsult/auth/AuthService.java`
- 新增：`src/main/java/com/example/smartconsult/auth/dto/*.java`
- 新增：`src/main/java/com/example/smartconsult/auth/entity/SmsVerificationCode.java`
- 新增：`src/main/java/com/example/smartconsult/auth/mapper/SmsVerificationCodeMapper.java`
- 新增：`src/main/java/com/example/smartconsult/user/entity/SysUser.java`
- 新增：`src/main/java/com/example/smartconsult/user/mapper/SysUserMapper.java`
- 新增：`src/main/java/com/example/smartconsult/user/UserService.java`
- 修改：`src/test/java/com/example/smartconsult/auth/AuthFlowTest.java`

- [ ] **Step 1：写验证码申请失败测试**

测试 `POST /auth/sms-code` 传入手机号后返回 `mockCode`、`expiresInSeconds=300`。

- [ ] **Step 2：运行测试确认失败**

运行：`mvn -Dtest=AuthFlowTest#requestSmsCodeReturnsMockCode test`

预期：失败，原因是接口未实现。

- [ ] **Step 3：实现最小验证码申请链路**

实现 DTO、Controller、Service、验证码实体和 Mapper，生成 6 位验证码并落表。

- [ ] **Step 4：运行测试确认通过**

运行：`mvn -Dtest=AuthFlowTest#requestSmsCodeReturnsMockCode test`

预期：通过。

- [ ] **Step 5：写登录自动注册失败测试**

测试先申请验证码，再用验证码登录，新手机号会自动创建用户并返回 token 和用户信息。

- [ ] **Step 6：实现登录最小链路**

实现用户实体、Mapper、用户服务、登录逻辑、验证码状态更新。

## Task 3：JWT 与登录态接口

**文件：**
- 新增：`src/main/java/com/example/smartconsult/auth/JwtTokenService.java`
- 新增：`src/main/java/com/example/smartconsult/auth/CurrentUser.java`
- 新增：`src/main/java/com/example/smartconsult/auth/CurrentUserContext.java`
- 新增：`src/main/java/com/example/smartconsult/auth/AuthInterceptor.java`
- 新增：`src/main/java/com/example/smartconsult/config/WebMvcConfig.java`
- 修改：`src/test/java/com/example/smartconsult/auth/AuthFlowTest.java`

- [ ] **Step 1：写 `/auth/me` 失败测试**

测试未带 token 访问失败，带登录返回的 token 访问成功并返回当前用户。

- [ ] **Step 2：运行测试确认失败**

运行：`mvn -Dtest=AuthFlowTest#meRequiresValidJwt test`

预期：失败，原因是 JWT 或拦截器未实现。

- [ ] **Step 3：实现 JWT、上下文和拦截器**

签发 JWT，校验 Bearer token，把当前用户写入线程上下文，并保护 `/auth/me`。

- [ ] **Step 4：运行后端测试**

运行：`mvn test`

预期：后端测试全部通过。

## Task 4：前端工程与登录页面

**文件：**
- 新增：`smart-consult-web/package.json`
- 新增：`smart-consult-web/index.html`
- 新增：`smart-consult-web/vite.config.ts`
- 新增：`smart-consult-web/tsconfig*.json`
- 新增：`smart-consult-web/src/main.ts`
- 新增：`smart-consult-web/src/App.vue`
- 新增：`smart-consult-web/src/api/http.ts`
- 新增：`smart-consult-web/src/api/auth.ts`
- 新增：`smart-consult-web/src/router/index.ts`
- 新增：`smart-consult-web/src/stores/auth.ts`
- 新增：`smart-consult-web/src/views/LoginView.vue`
- 新增：`smart-consult-web/src/views/HomeView.vue`
- 新增：`smart-consult-web/src/styles/main.css`

- [ ] **Step 1：创建 Vue 3 + Vite + TypeScript 工程文件**

手工创建前端工程，避免依赖脚手架交互。

- [ ] **Step 2：实现 API 封装**

Axios 使用 `/api` 前缀，自动注入 `Authorization: Bearer <token>`。

- [ ] **Step 3：实现登录页**

页面使用健康管理平台语义，包含手机号输入、获取验证码、开发环境验证码展示、验证码输入、登录按钮、错误提示。

- [ ] **Step 4：实现登录后首页**

展示用户手机号、用户 ID、最后登录时间，并提供退出登录。

## Task 5：验证与收尾

**文件：**
- 可能修改：前面所有文件

- [ ] **Step 1：运行后端测试**

运行：`mvn test`

预期：全部通过。

- [ ] **Step 2：安装并构建前端依赖**

运行：`npm install`，目录：`smart-consult-web`。

运行：`npm run build`，目录：`smart-consult-web`。

预期：构建成功。

- [ ] **Step 3：检查工作区变更**

运行：`git status --short`

确认只包含本次功能相关变更和用户已有未跟踪目录。

## 自检

- 设计文档中的后端接口、MySQL 表结构、JWT、MyBatis-Plus、前端工程均有对应任务。
- 计划未留下 TBD/TODO/placeholder。
- DTO 字段、接口路径、表名与设计文档保持一致。
