# 手机号 JWT 登录设计

## 目标

为健康管理平台增加第一个用户入口：用户使用手机号和短信验证码登录。登录时如果手机号尚未注册，后端自动创建用户账号。

前端作为独立工程放在当前仓库的 `smart-consult-web` 目录。后端继续使用当前 Spring Boot API 工程，包路径为 `src/main/java/com/example/smartconsult`。

## 范围

- 实现手机号验证码登录流程。
- 验证码按真实产品流程设计：先申请验证码，再输入验证码登录。
- 当前阶段不接真实短信服务，发送动作先 mock。
- 登录成功后返回 JWT token。
- 使用 MyBatis-Plus 连接 MySQL 并持久化用户与验证码记录。
- 在当前仓库新增前端工程 `smart-consult-web`。
- 提供 MySQL 用户表和验证码表结构。

本期不包含：

- 真实短信服务商接入。
- 密码登录。
- OAuth、微信登录等第三方登录。
- 完整 RBAC、角色、权限体系。
- 生产级 refresh token 轮换。

## 用户流程

1. 用户打开 `smart-consult-web` 登录页。
2. 用户输入中国大陆手机号。
3. 用户点击“获取验证码”。
4. 前端调用 `POST /api/auth/sms-code`。
5. 后端生成验证码记录，并标记为 mock 发送。
6. 当前开发阶段，后端在响应里返回 mock 验证码。
7. 前端把验证码显示为“开发环境验证码”。
8. 用户输入验证码并点击“登录”。
9. 前端调用 `POST /api/auth/login`。
10. 后端校验手机号、验证码、过期时间、使用状态。
11. 如果该手机号没有用户，后端自动注册用户。
12. 后端标记验证码已使用，更新用户最后登录时间，签发 JWT，并返回 token 与用户信息。
13. 前端保存 token，并跳转到登录后的首页。

## 后端架构

新增以下模块：

- `auth`：认证 Controller、DTO、Service、JWT 工具、认证拦截器。
- `user`：用户实体、Mapper、用户查询与注册服务。
- `config`：MyBatis-Plus 配置、MVC 拦截器配置。

接口响应继续使用项目已有的 `Result<T>` 结构。

### 接口设计

`POST /api/auth/sms-code`

请求：

```json
{
  "phone": "13800138000"
}
```

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "phone": "13800138000",
    "mockCode": "123456",
    "expiresInSeconds": 300
  }
}
```

`POST /api/auth/login`

请求：

```json
{
  "phone": "13800138000",
  "smsCode": "123456"
}
```

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "<jwt>",
    "tokenType": "Bearer",
    "expiresInSeconds": 604800,
    "user": {
      "id": 1,
      "phone": "13800138000",
      "nickname": "用户1380",
      "avatarUrl": null,
      "lastLoginTime": "2026-05-20T18:00:00"
    },
    "registered": true
  }
}
```

`GET /api/auth/me`

请求头：

```http
Authorization: Bearer <jwt>
```

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "phone": "13800138000",
    "nickname": "用户1380",
    "avatarUrl": null,
    "lastLoginTime": "2026-05-20T18:00:00"
  }
}
```

### MySQL 表结构

```sql
CREATE TABLE sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  phone VARCHAR(20) NOT NULL COMMENT '手机号',
  nickname VARCHAR(64) NOT NULL COMMENT '昵称',
  avatar_url VARCHAR(512) NULL COMMENT '头像URL',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
  last_login_time DATETIME NULL COMMENT '最后登录时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_phone (phone),
  KEY idx_sys_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE sms_verification_code (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '验证码ID',
  phone VARCHAR(20) NOT NULL COMMENT '手机号',
  code VARCHAR(10) NOT NULL COMMENT '验证码',
  scene VARCHAR(32) NOT NULL DEFAULT 'LOGIN' COMMENT '验证码场景',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0未使用，1已使用，2已过期',
  expires_at DATETIME NOT NULL COMMENT '过期时间',
  used_at DATETIME NULL COMMENT '使用时间',
  mock_sent TINYINT NOT NULL DEFAULT 1 COMMENT '是否mock发送',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_sms_code_phone_scene_status (phone, scene, status),
  KEY idx_sms_code_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码表';
```

验证码使用表存储，而不是纯内存缓存。这样当前 mock 阶段也能保持接近真实生产流程，后续接入短信服务商时只需要替换发送实现，不需要重写验证码校验主流程。

### MyBatis-Plus

新增依赖：

- `mybatis-plus-spring-boot3-starter`
- `mysql-connector-j`
- `jjwt-api`
- `jjwt-impl`
- `jjwt-jackson`

配置原则：

- 数据库连接从环境变量读取。
- Mapper 扫描 `com.example.smartconsult.**.mapper`。
- 用户表使用 `deleted` 字段支持逻辑删除。

### JWT

JWT Claims：

- `sub`：用户 ID。
- `phone`：用户手机号。
- `iat`：签发时间。
- `exp`：过期时间。

默认有效期：7 天。

JWT 密钥从 `APP_JWT_SECRET` 读取。开发环境可以有默认值，生产环境必须显式配置。

### 登录鉴权

新增 Spring MVC 拦截器：

- 放行接口：
  - `/health`
  - `/auth/sms-code`
  - `/auth/login`
  - Swagger、Knife4j 相关接口
- 需要登录：
  - `/auth/me`
  - 后续健康管理用户接口

拦截器校验 `Authorization: Bearer <jwt>`，并把当前用户上下文放到请求线程内，供业务接口读取。

## 前端架构

新增 `smart-consult-web`：

- Vue 3
- Vite
- TypeScript
- Vue Router
- Axios

主要结构：

- `src/api/http.ts`：Axios 实例、后端地址、token 注入。
- `src/api/auth.ts`：认证相关 API。
- `src/router/index.ts`：登录页和登录后首页路由。
- `src/stores/auth.ts` 或轻量 composable：维护 token 和用户状态。
- `src/views/LoginView.vue`：手机号验证码登录页。
- `src/views/HomeView.vue`：登录后首页。

### 登录页面

第一屏直接是登录流程，不做营销落地页。

页面结构：

- 左侧：健康管理平台品牌区和信任感信息。
- 右侧：手机号验证码登录表单。
- 表单状态：
  - 手机号格式校验。
  - 获取验证码按钮 loading 和倒计时。
  - 成功获取后显示“开发环境验证码：123456”。
  - 登录按钮 loading。
  - 后端错误清晰展示。

验证码 mock 展示必须明确标记为开发环境，避免用户误以为短信已真实发送。

## 测试

后端测试：

- 申请验证码时校验手机号并返回 mock 验证码。
- 新手机号登录时自动注册用户并返回 JWT。
- 已注册手机号再次登录时不重复创建用户。
- 错误验证码登录失败。
- 已使用验证码不能重复登录。
- `/auth/me` 必须携带有效 JWT，并返回当前用户信息。

前端验证：

- 登录页正常渲染。
- 获取验证码后展示 mock 验证码。
- 登录成功后保存 token 并跳转首页。
- 带登录态的接口请求自动携带 Bearer token。

## 后续接入真实短信

抽象 `SmsSender`：

- 当前实现：`MockSmsSender`，生成并返回验证码。
- 后续实现：短信服务商 Sender，发送同一套验证码，但不再把验证码返回给前端。

后续可以通过 `app.sms.mock-enabled` 控制是否在响应中返回 `mockCode`。
