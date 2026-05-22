package com.example.smartconsult.controller;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

@Component
public class AgentToolFactory {

    private final AgentToolService agentToolService;

    public AgentToolFactory(AgentToolService agentToolService) {
        this.agentToolService = agentToolService;
    }

    public ToolCallback[] createTools() {
        return new ToolCallback[]{
                FunctionToolCallback.builder("get_current_user_profile", agentToolService::getCurrentUserProfile)
                        .description("Read the current user's saved health profile and derived profile information, including sex, birth date, age, height, weight, waist, BMI, and central obesity judgment.")
                        .inputType(GetCurrentUserProfileRequest.class)
                        .build(),
                FunctionToolCallback.builder("get_next_question", agentToolService::getNextQuestion)
                        .description("Get exactly one next questionnaire question for the current user's active assessment session. Do not request or return the full question bank.")
                        .build(),
                FunctionToolCallback.builder("save_question_answer_fields", agentToolService::saveQuestionAnswerFields)
                        .description("Save the current question's raw user answer and extracted structured fields for the current user's active assessment session.")
                        .inputType(SaveQuestionAnswerFieldsRequest.class)
                        .build()
        };
    }
}
