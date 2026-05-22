package com.example.smartconsult.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("assessment_answer")
public class AssessmentAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Long userId;

    private Long questionId;

    private String rawUserAnswer;

    private String extractedFields;

    private String confidence;

    private Integer skipped;

    private Integer needFollowUp;

    private LocalDateTime savedAt;
}
