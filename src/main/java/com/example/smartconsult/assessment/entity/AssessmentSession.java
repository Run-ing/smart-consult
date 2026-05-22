package com.example.smartconsult.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("assessment_session")
public class AssessmentSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String status;

    private Long currentQuestionId;

    private String currentStage;
}
