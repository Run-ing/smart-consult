package com.example.smartconsult.assessment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smartconsult.assessment.entity.AssessmentSession;
import com.example.smartconsult.assessment.mapper.AssessmentSessionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssessmentSessionService {

    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String DEFAULT_STAGE = "FIRST_ROUND";

    private final AssessmentSessionMapper assessmentSessionMapper;

    public AssessmentSessionService(AssessmentSessionMapper assessmentSessionMapper) {
        this.assessmentSessionMapper = assessmentSessionMapper;
    }

    @Transactional
    public AssessmentSession getOrCreateInProgressSession(Long userId) {
        AssessmentSession existing = assessmentSessionMapper.selectOne(new LambdaQueryWrapper<AssessmentSession>()
                .eq(AssessmentSession::getUserId, userId)
                .eq(AssessmentSession::getStatus, STATUS_IN_PROGRESS)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }

        AssessmentSession session = new AssessmentSession();
        session.setUserId(userId);
        session.setStatus(STATUS_IN_PROGRESS);
        session.setCurrentStage(DEFAULT_STAGE);
        assessmentSessionMapper.insert(session);
        return session;
    }

    @Transactional
    public void moveToQuestion(Long sessionId, Long questionId, String stage) {
        AssessmentSession session = new AssessmentSession();
        session.setId(sessionId);
        session.setCurrentQuestionId(questionId);
        session.setCurrentStage(stage);
        assessmentSessionMapper.updateById(session);
    }
}
