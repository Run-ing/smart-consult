CREATE TABLE IF NOT EXISTS sys_user (
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

CREATE TABLE IF NOT EXISTS sms_verification_code (
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

CREATE TABLE IF NOT EXISTS user_health_profile (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '画像ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  sex VARCHAR(16) NOT NULL COMMENT '性别：MALE男性，FEMALE女性',
  birth_date DATE NOT NULL COMMENT '出生日期',
  height_cm DECIMAL(5,2) NOT NULL COMMENT '身高cm',
  weight_kg DECIMAL(5,2) NOT NULL COMMENT '体重kg',
  waist_cm DECIMAL(5,2) NULL COMMENT '腰围cm',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_health_profile_user_id (user_id),
  KEY idx_user_health_profile_sex (sex),
  KEY idx_user_health_profile_birth_date (birth_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户健康画像表';

CREATE TABLE IF NOT EXISTS assessment_question (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  code VARCHAR(64) NOT NULL COMMENT '题目编码',
  stage VARCHAR(32) NOT NULL COMMENT '阶段：FIRST_ROUND/BRANCH/LAB_REPORT',
  question_order INT NOT NULL COMMENT '同阶段排序',
  title VARCHAR(128) NOT NULL COMMENT '题目标题',
  question_text TEXT NOT NULL COMMENT '给LLM使用的建议话术',
  expected_fields JSON NOT NULL COMMENT '本题期望抽取字段列表',
  field_schema JSON NOT NULL COMMENT '字段类型、枚举、单位、是否必填等定义',
  branch_rules JSON NULL COMMENT '分支规则，决定下一题或触发分支',
  allow_skip TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许跳过',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  version INT NOT NULL DEFAULT 1 COMMENT '题目版本',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_assessment_question_code_version (code, version),
  KEY idx_assessment_question_stage_order (stage, question_order),
  KEY idx_assessment_question_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='慢病风险评估题库表';

CREATE TABLE IF NOT EXISTS assessment_session (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评估会话ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  status VARCHAR(32) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT '状态：IN_PROGRESS/COMPLETED/CANCELLED',
  current_question_id BIGINT NULL COMMENT '当前题目ID',
  current_stage VARCHAR(32) NULL COMMENT '当前阶段',
  started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  completed_at DATETIME NULL COMMENT '完成时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_assessment_session_user_status (user_id, status),
  KEY idx_assessment_session_current_question (current_question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='慢病风险评估会话表';

CREATE TABLE IF NOT EXISTS assessment_answer (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '回答ID',
  session_id BIGINT NOT NULL COMMENT '评估会话ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  question_id BIGINT NOT NULL COMMENT '题目ID',
  raw_user_answer TEXT NULL COMMENT '用户原始回答',
  extracted_fields JSON NULL COMMENT 'LLM抽取后的结构化字段',
  confidence VARCHAR(16) NULL COMMENT '置信度：high/medium/low',
  skipped TINYINT NOT NULL DEFAULT 0 COMMENT '是否跳过',
  need_follow_up TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要追问',
  saved_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '保存时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_assessment_answer_session_question (session_id, question_id),
  KEY idx_assessment_answer_user_session (user_id, session_id),
  KEY idx_assessment_answer_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='慢病风险评估回答表';
