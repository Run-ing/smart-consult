package com.example.smartconsult.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class SaveQuestionAnswerFieldsRequest {

    @JsonProperty("raw_user_answer")
    private String rawUserAnswer;

    @JsonProperty("extracted_fields")
    private Map<String, Object> extractedFields;

    private String confidence;

    @JsonProperty("need_follow_up")
    private Boolean needFollowUp;

    private Boolean skipped;
}
