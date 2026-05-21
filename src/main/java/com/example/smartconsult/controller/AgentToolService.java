package com.example.smartconsult.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smartconsult.assessment.entity.AssessmentQuestion;
import com.example.smartconsult.assessment.mapper.AssessmentQuestionMapper;
import com.example.smartconsult.auth.CurrentUserContext;
import com.example.smartconsult.exception.BusinessException;
import com.example.smartconsult.user.UserHealthProfileService;
import com.example.smartconsult.user.dto.UserHealthProfileResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AgentToolService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final UserHealthProfileService userHealthProfileService;
    private final AssessmentQuestionMapper assessmentQuestionMapper;
    private final ObjectMapper objectMapper;

    public AgentToolService(
            UserHealthProfileService userHealthProfileService,
            AssessmentQuestionMapper assessmentQuestionMapper,
            ObjectMapper objectMapper) {
        this.userHealthProfileService = userHealthProfileService;
        this.assessmentQuestionMapper = assessmentQuestionMapper;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getCurrentUserProfile(GetCurrentUserProfileRequest request) {
        Long userId = resolveUserId();
        UserHealthProfileResponse profile = userHealthProfileService.getByUserId(userId);

        BigDecimal bmi = calculateBmi(profile.getHeightCm(), profile.getWeightKg());
        CentralObesity centralObesity = judgeCentralObesity(profile.getSex(), profile.getWaistCm());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("user_id", profile.getUserId());
        response.put("sex", profile.getSex());
        response.put("birth_date", profile.getBirthDate().toString());
        response.put("age", profile.getAge());
        response.put("height_cm", profile.getHeightCm());
        response.put("weight_kg", profile.getWeightKg());
        response.put("waist_cm", profile.getWaistCm());
        response.put("bmi", bmi);
        response.put("central_obesity", centralObesity.value());
        response.put("central_obesity_basis", centralObesity.basis());
        return response;
    }

    public Map<String, Object> getNextQuestion(GetNextQuestionRequest request) {
        if (request == null) {
            throw new BusinessException(400, "get_next_question request must not be null");
        }
        if (request.getUserId() == null || request.getAssessmentSessionId() == null || isBlank(request.getCurrentStage())) {
            throw new BusinessException(400, "user_id, assessment_session_id and current_stage are required");
        }

        AssessmentQuestion previousQuestion = null;
        if (request.getPreviousQuestionId() != null) {
            previousQuestion = assessmentQuestionMapper.selectById(request.getPreviousQuestionId());
        }

        LambdaQueryWrapper<AssessmentQuestion> wrapper = new LambdaQueryWrapper<AssessmentQuestion>()
                .eq(AssessmentQuestion::getStage, request.getCurrentStage())
                .eq(AssessmentQuestion::getStatus, 1)
                .orderByAsc(AssessmentQuestion::getQuestionOrder)
                .last("LIMIT 1");
        if (previousQuestion != null) {
            wrapper.gt(AssessmentQuestion::getQuestionOrder, previousQuestion.getQuestionOrder());
        }

        AssessmentQuestion question = assessmentQuestionMapper.selectOne(wrapper);
        if (question == null) {
            throw new BusinessException(404, "no next assessment question");
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("question_id", question.getId());
        response.put("question_order", question.getQuestionOrder());
        response.put("question_text", question.getQuestionText());
        response.put("expected_fields", readJsonValue(question.getExpectedFields()));
        response.put("field_schema", readJsonValue(question.getFieldSchema()));
        response.put("branch_rules", readJsonValue(question.getBranchRules()));
        response.put("allow_skip", Boolean.TRUE.equals(question.getAllowSkip()));
        return response;
    }

    private Long resolveUserId() {
        return CurrentUserContext.getRequired().id();
    }

    private BigDecimal calculateBmi(BigDecimal heightCm, BigDecimal weightKg) {
        BigDecimal heightMeter = heightCm.divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP);
        return weightKg.divide(heightMeter.multiply(heightMeter), 2, RoundingMode.HALF_UP);
    }

    private CentralObesity judgeCentralObesity(String sex, BigDecimal waistCm) {
        if (waistCm == null) {
            return new CentralObesity(null, "waist_cm is not available");
        }
        if ("MALE".equals(sex)) {
            return new CentralObesity(waistCm.compareTo(new BigDecimal("90")) >= 0, "male waist >= 90cm");
        }
        if ("FEMALE".equals(sex)) {
            return new CentralObesity(waistCm.compareTo(new BigDecimal("85")) >= 0, "female waist >= 85cm");
        }
        return new CentralObesity(null, "sex is not supported for central obesity judgment");
    }

    private Object readJsonValue(String json) {
        if (isBlank(json)) {
            return null;
        }
        try {
            Object value = objectMapper.readValue(json, Object.class);
            if (value instanceof String text && (text.startsWith("[") || text.startsWith("{"))) {
                return objectMapper.readValue(text, Object.class);
            }
            return value;
        } catch (Exception exception) {
            throw new BusinessException(500, "invalid assessment question json");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record CentralObesity(Boolean value, String basis) {
    }
}
