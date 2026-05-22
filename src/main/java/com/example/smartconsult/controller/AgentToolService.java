package com.example.smartconsult.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smartconsult.assessment.AssessmentSessionService;
import com.example.smartconsult.assessment.entity.AssessmentAnswer;
import com.example.smartconsult.assessment.entity.AssessmentQuestion;
import com.example.smartconsult.assessment.entity.AssessmentSession;
import com.example.smartconsult.assessment.mapper.AssessmentAnswerMapper;
import com.example.smartconsult.assessment.mapper.AssessmentQuestionMapper;
import com.example.smartconsult.auth.CurrentUserContext;
import com.example.smartconsult.exception.BusinessException;
import com.example.smartconsult.user.UserHealthProfileService;
import com.example.smartconsult.user.dto.UserHealthProfileResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class AgentToolService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final UserHealthProfileService userHealthProfileService;
    private final AssessmentQuestionMapper assessmentQuestionMapper;
    private final AssessmentAnswerMapper assessmentAnswerMapper;
    private final AssessmentSessionService assessmentSessionService;
    private final ObjectMapper objectMapper;

    public AgentToolService(
            UserHealthProfileService userHealthProfileService,
            AssessmentQuestionMapper assessmentQuestionMapper,
            AssessmentAnswerMapper assessmentAnswerMapper,
            AssessmentSessionService assessmentSessionService,
            ObjectMapper objectMapper) {
        this.userHealthProfileService = userHealthProfileService;
        this.assessmentQuestionMapper = assessmentQuestionMapper;
        this.assessmentAnswerMapper = assessmentAnswerMapper;
        this.assessmentSessionService = assessmentSessionService;
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
        log.info("获取当前用户信息工具");
        return response;
    }

    public Map<String, Object> getNextQuestion() {
        Long currentUserId = resolveUserId();
        AssessmentSession session = assessmentSessionService.getOrCreateInProgressSession(currentUserId);

        LambdaQueryWrapper<AssessmentQuestion> wrapper = new LambdaQueryWrapper<AssessmentQuestion>()
                .eq(AssessmentQuestion::getStatus, 1)
                .last("LIMIT 1");

        if (session.getCurrentQuestionId() == null) {
            wrapper.eq(AssessmentQuestion::getStage, defaultStage(session.getCurrentStage()))
                    .orderByAsc(AssessmentQuestion::getQuestionOrder);
        } else {
            AssessmentQuestion currentQuestion = assessmentQuestionMapper.selectById(session.getCurrentQuestionId());
            if (currentQuestion == null) {
                throw new BusinessException(404, "current assessment question not found");
            }
            wrapper.eq(AssessmentQuestion::getStage, currentQuestion.getStage())
                    .eq(AssessmentQuestion::getQuestionOrder, currentQuestion.getQuestionOrder() + 1);
        }

        AssessmentQuestion question = assessmentQuestionMapper.selectOne(wrapper);
        if (question == null) {
            throw new BusinessException(404, "no next assessment question");
        }
        assessmentSessionService.moveToQuestion(session.getId(), question.getId(), question.getStage());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("question_id", question.getId());
        response.put("question_order", question.getQuestionOrder());
        response.put("question_text", question.getQuestionText());
        response.put("expected_fields", readJsonValue(question.getExpectedFields()));
        response.put("field_schema", readJsonValue(question.getFieldSchema()));
        response.put("branch_rules", readJsonValue(question.getBranchRules()));
        response.put("allow_skip", Boolean.TRUE.equals(question.getAllowSkip()));
        log.info("获取下一题工具，questionId={}, questionOrder={}, sessionId={}", question.getId(), question.getQuestionOrder(), session.getId());
        return response;
    }

    public Map<String, Object> saveQuestionAnswerFields(SaveQuestionAnswerFieldsRequest request) {
        if (request == null) {
            throw new BusinessException(400, "save_question_answer_fields request must not be null");
        }
        if (isBlank(request.getRawUserAnswer())) {
            throw new BusinessException(400, "raw_user_answer is required");
        }
        if (isBlank(request.getConfidence())) {
            throw new BusinessException(400, "confidence is required");
        }

        Long currentUserId = resolveUserId();
        AssessmentSession session = assessmentSessionService.getOrCreateInProgressSession(currentUserId);
        if (session.getCurrentQuestionId() == null) {
            throw new BusinessException(400, "assessment session has no current question");
        }

        AssessmentAnswer answer = assessmentAnswerMapper.selectOne(new LambdaQueryWrapper<AssessmentAnswer>()
                .eq(AssessmentAnswer::getSessionId, session.getId())
                .eq(AssessmentAnswer::getQuestionId, session.getCurrentQuestionId())
                .last("LIMIT 1"));
        if (answer == null) {
            answer = new AssessmentAnswer();
            answer.setSessionId(session.getId());
            answer.setUserId(currentUserId);
            answer.setQuestionId(session.getCurrentQuestionId());
        }
        answer.setRawUserAnswer(request.getRawUserAnswer().trim());
        answer.setExtractedFields(toJson(request.getExtractedFields()));
        answer.setConfidence(request.getConfidence().trim());
        answer.setSkipped(Boolean.TRUE.equals(request.getSkipped()) ? 1 : 0);
        answer.setNeedFollowUp(Boolean.TRUE.equals(request.getNeedFollowUp()) ? 1 : 0);

        if (answer.getId() == null) {
            assessmentAnswerMapper.insert(answer);
        } else {
            assessmentAnswerMapper.updateById(answer);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("saved", true);
        response.put("answer_id", answer.getId());
        response.put("session_id", answer.getSessionId());
        response.put("question_id", answer.getQuestionId());
        log.info("保存题目回答工具，answerId={}, questionId={}, sessionId={}", answer.getId(), answer.getQuestionId(), answer.getSessionId());
        return response;
    }

    private Long resolveUserId() {
        return CurrentUserContext.getRequired().id();
    }

    private String defaultStage(String stage) {
        return isBlank(stage) ? AssessmentSessionService.DEFAULT_STAGE : stage;
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

    private String toJson(Object value) {
        if (value == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new BusinessException(500, "invalid extracted_fields json");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record CentralObesity(Boolean value, String basis) {
    }
}
