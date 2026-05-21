# LLM 对话式慢病风险评估问卷设计

数据来源：`（新版）慢病风险&危险因子评估字典.xlsx` / `整理 -lzf` 工作表。

目标：把完整危险因子题库拆成“首次建档 + LLM 首轮必问 + 动态分支追问”，避免用户一次性回答大量问题，同时保证最终能够覆盖所有慢病风险危险因子。

## 一、信息分层原则

| 层级 | 采集方式 | 设计目标 |
| --- | --- | --- |
| 首次建档 | 注册/完善资料页填写 | 采集稳定、结构化、后续高频复用的信息 |
| LLM 首轮必问 | 对话开始后询问 | 用少量问题快速识别主要风险方向 |
| LLM 分支追问 | 根据首轮答案和建档信息触发 | 只追问相关风险方向，减少无效问题 |
| 可选补充 | 用户有体检报告或明确病史时填写 | 提高评估准确度，不阻塞基础评估 |

## 二、首次建档填写的信息

这些信息不建议通过 LLM 反复询问，应在用户首次登录或完善档案时填写。LLM 对话时直接读取。

| 字段 | 表单题目 | 类型 | 规则 | 直接得到/衍生的危险因子 |
| --- | --- | --- | --- | --- |
| sex | 性别 | 单选 | 男 / 女 | 男性、女性 |
| birth_date / age | 出生日期 | 日期 | 系统计算年龄 | 年龄≥40、≥45、≥46、≥50、≥60、≥70 等年龄门槛 |
| height_cm | 身高 | 数值 | cm | 用于 BMI |
| weight_kg | 体重 | 数值 | kg，可允许用户更新 | 用于 BMI |
| waist_cm | 腰围 | 数值 | cm，可选“不清楚” | 中心型肥胖 |

系统衍生字段：

| 衍生字段 | 规则 | 覆盖危险因子 |
| --- | --- | --- |
| BMI | weight_kg / (height_cm / 100)² | 所有 BMI 相关危险因子 |
| BMI ＜ 18.5 | BMI ＜ 18.5 kg/m² | COPD、骨质疏松 |
| 18.5 ≤ BMI ＜ 24 | 18.5 kg/m² ≤ BMI ＜ 24.0 kg/m² | COPD 计分 |
| 24 ≤ BMI ＜ 28 | 24.0 kg/m² ≤ BMI ＜ 28.0 kg/m² | 糖尿病、COPD |
| BMI ≥ 28 | BMI ≥ 28.0 kg/m² | 冠心病、脑卒中、糖尿病、COPD |
| BMI ≥ 23 | BMI ≥ 23.0 kg/m² | 结直肠癌 |
| 中心型肥胖 | 男性腰围≥90cm，女性腰围≥85cm | 糖尿病 |

## 三、LLM 首轮必须询问的信息

首轮目标不是问完所有内容，而是识别需要进入哪些分支。建议控制在 6 个自然语言问题内。

### 首轮问题 1：确认基础资料

建议话术：

> 我已经读取到你的基础资料：性别、年龄、身高、体重和腰围。请问这些信息最近是否有变化？如果体重或腰围有变化，可以直接告诉我最新值。

得到字段：

| 字段                 | 用途         |
|--------------------|------------|
| profile_confirmed  | 是否确认建档资料有效 |
| updated_weight_kg  | 更新 BMI     |
| updated_height_cm  | 更新 BMI     |
| updated_birth_date | 更新 年龄      |
| updated_sex        | 更新 性别      |
| updated_waist_cm   | 更新中心型肥胖判断  |

### 首轮问题 2：吸烟与二手烟

建议话术：

> 你现在或过去是否吸烟？如果吸烟，请告诉我大概每天多少支、吸了多少年；如果已经戒烟，也告诉我戒烟几年了。另外，你是否长期接触二手烟，比如共同生活或同室工作超过 20 年？

得到字段：

| 字段 | 覆盖危险因子 |
| --- | --- |
| smoking_status | 吸烟、不吸烟、已戒烟 |
| cigarettes_per_day | 包年计算 |
| smoking_years | 包年计算 |
| quit_smoking_years | 曾经吸烟≥20包年但戒烟＜15年 |
| secondhand_smoke_exposure | 长期二手烟暴露 |

### 首轮问题 3：饮酒与饮食习惯

建议话术：

> 平时是否经常饮酒，或者有长期大量饮酒？饮食方面是否偏咸、经常吃腌制食品，或者常吃很烫的汤、茶、食物？

得到字段：

| 字段 | 覆盖危险因子 |
| --- | --- |
| alcohol_status | 过量饮酒 |
| heavy_drinking | 重度饮酒 |
| high_salt_diet | 高盐饮食 |
| pickled_food | 腌制食品 |
| hot_food_or_soup | 热汤饮食 |

### 首轮问题 4：已知慢病和重要病史

建议话术：

> 你是否曾被医生诊断过这些疾病或情况：高血压、糖尿病或糖尿病前期、冠心病或其他心脏病、脑卒中相关问题、慢阻肺、慢性胃病、脂肪肝/肝炎/肝硬化、胰腺炎？可以只说有或没有，不确定也可以说不确定。

得到字段：

| 字段 | 覆盖危险因子 |
| --- | --- |
| hypertension_history | 高血压病史 |
| diabetes_history | 糖尿病史 |
| prediabetes_history | 糖尿病前期史 |
| coronary_heart_disease_history | 冠心病相关画像 |
| atrial_fibrillation_history | 心房颤动病史 |
| other_heart_disease_history | 其他心脏病史 |
| carotid_stenosis_history | 颈动脉狭窄病史 |
| copd_history | 慢性阻塞性肺疾病病史 |
| chronic_gastric_disease_history | 慢性胃部疾病病史 |
| hepatic_steatosis_history | 肝脂肪变性 |
| mafld_history | 代谢相关脂肪性肝病病史 |
| hbv_infection_history | 乙型肝炎病毒感染 |
| hcv_infection_history | 丙型肝炎病毒感染 |
| cirrhosis_history | 肝硬化 |
| pancreatitis_history | 胰腺炎病史 |

### 首轮问题 5：家族史

建议话术：

> 你的父母、兄弟姐妹或子女中，是否有人患过高血压、冠心病、脑卒中、糖尿病、慢阻肺相关疾病，或肺癌、结直肠癌、胃癌、肝癌、乳腺癌、卵巢癌、宫颈癌、前列腺癌、食管癌、甲状腺癌？

得到字段：

| 字段 | 覆盖危险因子 |
| --- | --- |
| hypertension_family_history | 高血压家族史 |
| coronary_heart_disease_family_history | 冠心病家族史 |
| stroke_family_history | 脑卒中家族史 |
| diabetes_family_history | 糖尿病家族史 |
| copd_related_family_history | COPD 家族史计分 |
| cancer_family_history_summary | 触发肿瘤家族史分支 |

### 首轮问题 6：症状和体检报告

建议话术：

> 最近是否有长期咳嗽、活动后气促或走路气喘？另外，如果你手上有近一年体检报告，我可以帮你结合血脂、幽门螺杆菌、同型半胱氨酸等指标一起评估；没有也可以先跳过。

得到字段：

| 字段 | 覆盖危险因子 |
| --- | --- |
| chronic_cough | 慢性持续性咳嗽 |
| dyspnea_level | 无气促、活动后气促、日常行走气促 |
| has_lab_report | 是否进入化验检查分支 |


