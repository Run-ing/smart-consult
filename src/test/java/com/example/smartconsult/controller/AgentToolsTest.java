package com.example.smartconsult.controller;

import com.example.smartconsult.auth.CurrentUser;
import com.example.smartconsult.auth.CurrentUserContext;
import com.example.smartconsult.user.UserHealthProfileService;
import com.example.smartconsult.user.dto.UserHealthProfileRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AgentToolsTest {

    private final AgentToolFactory agentToolFactory;
    private final UserHealthProfileService userHealthProfileService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    AgentToolsTest(
            AgentToolFactory agentToolFactory,
            UserHealthProfileService userHealthProfileService,
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper) {
        this.agentToolFactory = agentToolFactory;
        this.userHealthProfileService = userHealthProfileService;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Test
    void getCurrentUserProfileReturnsSavedProfileWithDerivedRiskFields() throws Exception {
        Long userId = 91001L;
        UserHealthProfileRequest request = new UserHealthProfileRequest();
        request.setSex("MALE");
        request.setBirthDate(LocalDate.of(1990, 5, 20));
        request.setHeightCm(new BigDecimal("175.00"));
        request.setWeightKg(new BigDecimal("81.00"));
        request.setWaistCm(new BigDecimal("92.00"));
        userHealthProfileService.save(userId, request);

        CurrentUserContext.set(new CurrentUser(userId, "13800139999"));
        JsonNode result;
        try {
            result = objectMapper.readTree(tool("get_current_user_profile").call("""
                    {"user_id": 123456}
                    """));
        } finally {
            CurrentUserContext.clear();
        }

        assertThat(result.path("user_id").asLong()).isEqualTo(userId);
        assertThat(result.path("sex").asText()).isEqualTo("MALE");
        assertThat(result.path("birth_date").asText()).isEqualTo("1990-05-20");
        assertThat(result.path("age").asInt()).isGreaterThanOrEqualTo(35);
        assertThat(result.path("height_cm").decimalValue()).isEqualByComparingTo("175.00");
        assertThat(result.path("weight_kg").decimalValue()).isEqualByComparingTo("81.00");
        assertThat(result.path("waist_cm").decimalValue()).isEqualByComparingTo("92.00");
        assertThat(result.path("bmi").decimalValue()).isEqualByComparingTo("26.45");
        assertThat(result.path("central_obesity").asBoolean()).isTrue();
        assertThat(result.path("central_obesity_basis").asText()).contains("male waist >= 90cm");
    }

    @Test
    void getNextQuestionReturnsOnlyTheNextEnabledQuestionForStage() throws Exception {
        Long userId = 91001L;
        jdbcTemplate.update("DELETE FROM assessment_question");
        jdbcTemplate.update("DELETE FROM assessment_session");
        insertQuestion("FIRST_PROFILE_CONFIRM", "FIRST_ROUND", 1, "profile_confirmed");
        insertQuestion("FIRST_SMOKING", "FIRST_ROUND", 2, "smoking_status");
        insertQuestion("FIRST_ALCOHOL_DIET", "FIRST_ROUND", 3, "alcohol_status");
        jdbcTemplate.update("""
                INSERT INTO assessment_session (
                  id, user_id, status, current_question_id, current_stage
                ) VALUES (?, ?, 'IN_PROGRESS', ?, ?)
                """, 1001L, userId, 1L, "FIRST_ROUND");

        CurrentUserContext.set(new CurrentUser(userId, "13800139999"));
        JsonNode result;
        try {
            result = objectMapper.readTree(tool("get_next_question").call("{}"));
        } finally {
            CurrentUserContext.clear();
        }

        assertThat(result.path("question_id").asLong()).isEqualTo(2L);
        assertThat(result.path("question_order").asInt()).isEqualTo(2);
        assertThat(result.path("question_text").asText()).isEqualTo("Question FIRST_SMOKING");
        assertThat(result.path("expected_fields")).hasSize(1);
        assertThat(result.path("expected_fields").get(0).asText()).isEqualTo("smoking_status");
        assertThat(result.path("field_schema").path("smoking_status").path("type").asText()).isEqualTo("boolean");
        assertThat(result.path("branch_rules").path("next").asText()).isEqualTo("FIRST_ALCOHOL_DIET");
        assertThat(result.path("allow_skip").asBoolean()).isTrue();
        assertThat(result.has("questions")).isFalse();
        Long currentQuestionId = jdbcTemplate.queryForObject(
                "SELECT current_question_id FROM assessment_session WHERE id = ?",
                Long.class,
                1001L);
        assertThat(currentQuestionId).isEqualTo(2L);
    }

    @Test
    void saveQuestionAnswerFieldsPersistsCurrentSessionQuestionAnswer() throws Exception {
        Long userId = 91002L;
        jdbcTemplate.update("DELETE FROM assessment_answer");
        jdbcTemplate.update("DELETE FROM assessment_question");
        jdbcTemplate.update("DELETE FROM assessment_session");
        insertQuestion("FIRST_SMOKING", "FIRST_ROUND", 2, "smoking_status");
        jdbcTemplate.update("""
                INSERT INTO assessment_session (
                  id, user_id, status, current_question_id, current_stage
                ) VALUES (?, ?, 'IN_PROGRESS', ?, ?)
                """, 2001L, userId, 2L, "FIRST_ROUND");

        CurrentUserContext.set(new CurrentUser(userId, "13800138888"));
        JsonNode result;
        try {
            result = objectMapper.readTree(tool("save_question_answer_fields").call("""
                    {
                      "user_id": 123456,
                      "assessment_session_id": 9999,
                      "question_id": 9999,
                      "raw_user_answer": "我现在不吸烟，也没有长期二手烟暴露",
                      "extracted_fields": {
                        "smoking_status": "NEVER",
                        "secondhand_smoke_exposure": false
                      },
                      "confidence": "high",
                      "need_follow_up": false,
                      "skipped": false
                    }
                    """));
        } finally {
            CurrentUserContext.clear();
        }

        assertThat(result.path("saved").asBoolean()).isTrue();
        assertThat(result.path("session_id").asLong()).isEqualTo(2001L);
        assertThat(result.path("question_id").asLong()).isEqualTo(2L);

        Map<String, Object> answer = jdbcTemplate.queryForMap(
                "SELECT user_id, session_id, question_id, raw_user_answer, extracted_fields, confidence, skipped, need_follow_up FROM assessment_answer WHERE session_id = ? AND question_id = ?",
                2001L,
                2L);
        assertThat(answer.get("USER_ID")).isEqualTo(91002L);
        assertThat(answer.get("RAW_USER_ANSWER")).isEqualTo("我现在不吸烟，也没有长期二手烟暴露");
        assertThat(asText(answer.get("EXTRACTED_FIELDS"))).contains("smoking_status");
        assertThat(answer.get("CONFIDENCE")).isEqualTo("high");
        assertThat(((Number) answer.get("SKIPPED")).intValue()).isZero();
        assertThat(((Number) answer.get("NEED_FOLLOW_UP")).intValue()).isZero();
    }

    private ToolCallback tool(String name) {
        return Arrays.stream(agentToolFactory.createTools())
                .filter(callback -> callback.getToolDefinition().name().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private void insertQuestion(String code, String stage, int order, String field) {
        jdbcTemplate.update("""
                INSERT INTO assessment_question (
                  id, code, stage, question_order, title, question_text,
                  expected_fields, field_schema, branch_rules, allow_skip, status, version
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 1, 1)
                """,
                (long) order,
                code,
                stage,
                order,
                "Title " + code,
                "Question " + code,
                "[\"" + field + "\"]",
                "{\"" + field + "\": {\"type\": \"boolean\", \"nullable\": true}}",
                order < 3 ? objectMapper.valueToTree(Map.of("next", nextCode(order))).toString() : "{\"next\": null}");
    }

    private String nextCode(int order) {
        return switch (order) {
            case 1 -> "FIRST_SMOKING";
            case 2 -> "FIRST_ALCOHOL_DIET";
            default -> null;
        };
    }

    private String asText(Object value) {
        if (value instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return value == null ? "" : value.toString();
    }
}
