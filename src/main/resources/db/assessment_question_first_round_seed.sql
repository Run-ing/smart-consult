INSERT INTO assessment_question (
  code,
  stage,
  question_order,
  title,
  question_text,
  expected_fields,
  field_schema,
  branch_rules,
  allow_skip,
  status,
  version
) VALUES
(
  'FIRST_PROFILE_CONFIRM',
  'FIRST_ROUND',
  1,
  '确认基础资料',
  '我已经读取到你的基础资料：性别、年龄、身高、体重和腰围。请问这些信息最近是否有变化？如果体重或腰围有变化，可以直接告诉我最新值。',
  '["profile_confirmed","updated_weight_kg","updated_waist_cm"]',
  '{
    "profile_confirmed": {
      "type": "boolean",
      "nullable": true,
      "description": "是否确认建档资料有效"
    },
    "updated_weight_kg": {
      "type": "number",
      "unit": "kg",
      "nullable": true,
      "min": 10,
      "max": 300,
      "description": "最新体重，用于更新 BMI"
    },
    "updated_waist_cm": {
      "type": "number",
      "unit": "cm",
      "nullable": true,
      "min": 30,
      "max": 250,
      "description": "最新腰围，用于更新中心型肥胖判断"
    }
  }',
  '{
    "next": "FIRST_SMOKING",
    "profile_update_fields": {
      "updated_weight_kg": "weight_kg",
      "updated_waist_cm": "waist_cm"
    }
  }',
  0,
  1,
  1
),
(
  'FIRST_SMOKING',
  'FIRST_ROUND',
  2,
  '吸烟与二手烟',
  '你现在或过去是否吸烟？如果吸烟，请告诉我大概每天多少支、吸了多少年；如果已经戒烟，也告诉我戒烟几年了。另外，你是否长期接触二手烟，比如共同生活或同室工作超过 20 年？',
  '["smoking_status","cigarettes_per_day","smoking_years","quit_smoking_years","secondhand_smoke_exposure"]',
  '{
    "smoking_status": {
      "type": "enum",
      "values": ["NEVER","CURRENT","FORMER","UNKNOWN"],
      "nullable": false,
      "description": "吸烟状态：不吸烟、当前吸烟、已戒烟、不确定"
    },
    "cigarettes_per_day": {
      "type": "number",
      "unit": "支/天",
      "nullable": true,
      "min": 0,
      "max": 200,
      "description": "平均每天吸烟支数，用于包年计算"
    },
    "smoking_years": {
      "type": "number",
      "unit": "年",
      "nullable": true,
      "min": 0,
      "max": 100,
      "description": "累计吸烟年数，用于包年计算"
    },
    "quit_smoking_years": {
      "type": "number",
      "unit": "年",
      "nullable": true,
      "min": 0,
      "max": 100,
      "description": "戒烟年数"
    },
    "secondhand_smoke_exposure": {
      "type": "boolean",
      "nullable": true,
      "description": "是否长期二手烟暴露"
    }
  }',
  '{
    "next": "FIRST_ALCOHOL_DIET",
    "branch_candidates": ["COPD","LUNG_CANCER","CARDIOVASCULAR"]
  }',
  1,
  1,
  1
),
(
  'FIRST_ALCOHOL_DIET',
  'FIRST_ROUND',
  3,
  '饮酒与饮食习惯',
  '平时是否经常饮酒，或者有长期大量饮酒？饮食方面是否偏咸、经常吃腌制食品，或者常吃很烫的汤、茶、食物？',
  '["alcohol_status","heavy_drinking","high_salt_diet","pickled_food","hot_food_or_soup"]',
  '{
    "alcohol_status": {
      "type": "enum",
      "values": ["NONE","OCCASIONAL","REGULAR","EXCESSIVE","UNKNOWN"],
      "nullable": false,
      "description": "饮酒状态"
    },
    "heavy_drinking": {
      "type": "boolean",
      "nullable": true,
      "description": "是否存在长期大量或重度饮酒"
    },
    "high_salt_diet": {
      "type": "boolean",
      "nullable": true,
      "description": "是否偏咸或高盐饮食"
    },
    "pickled_food": {
      "type": "boolean",
      "nullable": true,
      "description": "是否经常吃腌制食品"
    },
    "hot_food_or_soup": {
      "type": "boolean",
      "nullable": true,
      "description": "是否经常吃很烫的汤、茶或食物"
    }
  }',
  '{
    "next": "FIRST_DISEASE_HISTORY",
    "branch_candidates": ["HYPERTENSION","DIABETES","GASTRIC_CANCER","ESOPHAGEAL_CANCER","LIVER_DISEASE"]
  }',
  1,
  1,
  1
),
(
  'FIRST_DISEASE_HISTORY',
  'FIRST_ROUND',
  4,
  '已知慢病和重要病史',
  '你是否曾被医生诊断过这些疾病或情况：高血压、糖尿病或糖尿病前期、冠心病或其他心脏病、脑卒中相关问题、慢阻肺、慢性胃病、脂肪肝/肝炎/肝硬化、胰腺炎？可以只说有或没有，不确定也可以说不确定。',
  '["hypertension_history","diabetes_history","prediabetes_history","coronary_heart_disease_history","atrial_fibrillation_history","other_heart_disease_history","carotid_stenosis_history","copd_history","chronic_gastric_disease_history","hepatic_steatosis_history","mafld_history","hbv_infection_history","hcv_infection_history","cirrhosis_history","pancreatitis_history"]',
  '{
    "hypertension_history": {"type": "boolean", "nullable": true, "description": "高血压病史"},
    "diabetes_history": {"type": "boolean", "nullable": true, "description": "糖尿病史"},
    "prediabetes_history": {"type": "boolean", "nullable": true, "description": "糖尿病前期史"},
    "coronary_heart_disease_history": {"type": "boolean", "nullable": true, "description": "冠心病病史"},
    "atrial_fibrillation_history": {"type": "boolean", "nullable": true, "description": "心房颤动病史"},
    "other_heart_disease_history": {"type": "boolean", "nullable": true, "description": "其他心脏病史"},
    "carotid_stenosis_history": {"type": "boolean", "nullable": true, "description": "颈动脉狭窄病史"},
    "copd_history": {"type": "boolean", "nullable": true, "description": "慢性阻塞性肺疾病病史"},
    "chronic_gastric_disease_history": {"type": "boolean", "nullable": true, "description": "慢性胃部疾病病史"},
    "hepatic_steatosis_history": {"type": "boolean", "nullable": true, "description": "肝脂肪变性"},
    "mafld_history": {"type": "boolean", "nullable": true, "description": "代谢相关脂肪性肝病病史"},
    "hbv_infection_history": {"type": "boolean", "nullable": true, "description": "乙型肝炎病毒感染"},
    "hcv_infection_history": {"type": "boolean", "nullable": true, "description": "丙型肝炎病毒感染"},
    "cirrhosis_history": {"type": "boolean", "nullable": true, "description": "肝硬化"},
    "pancreatitis_history": {"type": "boolean", "nullable": true, "description": "胰腺炎病史"}
  }',
  '{
    "next": "FIRST_FAMILY_HISTORY",
    "branch_candidates": ["CARDIOVASCULAR","STROKE","DIABETES","COPD","GASTRIC_CANCER","LIVER_CANCER","PANCREATIC_DISEASE"]
  }',
  1,
  1,
  1
),
(
  'FIRST_FAMILY_HISTORY',
  'FIRST_ROUND',
  5,
  '家族史',
  '你的父母、兄弟姐妹或子女中，是否有人患过高血压、冠心病、脑卒中、糖尿病、慢阻肺相关疾病，或肺癌、结直肠癌、胃癌、肝癌、乳腺癌、卵巢癌、宫颈癌、前列腺癌、食管癌、甲状腺癌？',
  '["hypertension_family_history","coronary_heart_disease_family_history","stroke_family_history","diabetes_family_history","copd_related_family_history","cancer_family_history_summary"]',
  '{
    "hypertension_family_history": {"type": "boolean", "nullable": true, "description": "高血压家族史"},
    "coronary_heart_disease_family_history": {"type": "boolean", "nullable": true, "description": "冠心病家族史"},
    "stroke_family_history": {"type": "boolean", "nullable": true, "description": "脑卒中家族史"},
    "diabetes_family_history": {"type": "boolean", "nullable": true, "description": "糖尿病家族史"},
    "copd_related_family_history": {"type": "boolean", "nullable": true, "description": "COPD 相关家族史"},
    "cancer_family_history_summary": {
      "type": "array",
      "items": "string",
      "nullable": true,
      "description": "肿瘤家族史摘要，可记录涉及的癌种"
    }
  }',
  '{
    "next": "FIRST_SYMPTOM_LAB_REPORT",
    "branch_candidates": ["CARDIOVASCULAR","STROKE","DIABETES","COPD","CANCER_FAMILY_HISTORY"]
  }',
  1,
  1,
  1
),
(
  'FIRST_SYMPTOM_LAB_REPORT',
  'FIRST_ROUND',
  6,
  '症状和体检报告',
  '最近是否有长期咳嗽、活动后气促或走路气喘？另外，如果你手上有近一年体检报告，我可以帮你结合血脂、幽门螺杆菌、同型半胱氨酸等指标一起评估；没有也可以先跳过。',
  '["chronic_cough","dyspnea_level","has_lab_report"]',
  '{
    "chronic_cough": {
      "type": "boolean",
      "nullable": true,
      "description": "是否有慢性持续性咳嗽"
    },
    "dyspnea_level": {
      "type": "enum",
      "values": ["NONE","AFTER_ACTIVITY","DAILY_WALKING","UNKNOWN"],
      "nullable": true,
      "description": "气促程度：无气促、活动后气促、日常行走气促、不确定"
    },
    "has_lab_report": {
      "type": "boolean",
      "nullable": true,
      "description": "是否有近一年体检或化验报告"
    }
  }',
  '{
    "next": null,
    "branch_candidates": ["COPD","LAB_REPORT"]
  }',
  1,
  1,
  1
)
ON DUPLICATE KEY UPDATE
  stage = VALUES(stage),
  question_order = VALUES(question_order),
  title = VALUES(title),
  question_text = VALUES(question_text),
  expected_fields = VALUES(expected_fields),
  field_schema = VALUES(field_schema),
  branch_rules = VALUES(branch_rules),
  allow_skip = VALUES(allow_skip),
  status = VALUES(status),
  updated_at = CURRENT_TIMESTAMP;
