package com.example.smartconsult.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GetNextQuestionRequest {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("assessment_session_id")
    private Long assessmentSessionId;

    @JsonProperty("current_stage")
    private String currentStage;

    @JsonProperty("previous_question_id")
    private Long previousQuestionId;

    @JsonProperty("completed_fields_summary")
    private String completedFieldsSummary;
}
