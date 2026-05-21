package com.example.smartconsult.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("assessment_question")
public class AssessmentQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String stage;

    private Integer questionOrder;

    private String title;

    private String questionText;

    private String expectedFields;

    private String fieldSchema;

    private String branchRules;

    private Boolean allowSkip;

    private Integer status;

    private Integer version;
}
