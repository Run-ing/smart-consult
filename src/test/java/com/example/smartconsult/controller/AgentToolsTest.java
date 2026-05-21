package com.example.smartconsult.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

        JsonNode result = objectMapper.readTree(tool("get_current_user_profile").call("""
                {"user_id": 91001}
                """));

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
        jdbcTemplate.update("DELETE FROM assessment_question");
        insertQuestion("FIRST_PROFILE_CONFIRM", "FIRST_ROUND", 1, "profile_confirmed");
        insertQuestion("FIRST_SMOKING", "FIRST_ROUND", 2, "smoking_status");
        insertQuestion("FIRST_ALCOHOL_DIET", "FIRST_ROUND", 3, "alcohol_status");

        JsonNode result = objectMapper.readTree(tool("get_next_question").call("""
                {
                  "user_id": 91001,
                  "assessment_session_id": 1001,
                  "current_stage": "FIRST_ROUND",
                  "previous_question_id": 1
                }
                """));

        assertThat(result.path("question_id").asLong()).isEqualTo(2L);
        assertThat(result.path("question_order").asInt()).isEqualTo(2);
        assertThat(result.path("question_text").asText()).isEqualTo("Question FIRST_SMOKING");
        assertThat(result.path("expected_fields")).hasSize(1);
        assertThat(result.path("expected_fields").get(0).asText()).isEqualTo("smoking_status");
        assertThat(result.path("field_schema").path("smoking_status").path("type").asText()).isEqualTo("boolean");
        assertThat(result.path("branch_rules").path("next").asText()).isEqualTo("FIRST_ALCOHOL_DIET");
        assertThat(result.path("allow_skip").asBoolean()).isTrue();
        assertThat(result.has("questions")).isFalse();
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
}
