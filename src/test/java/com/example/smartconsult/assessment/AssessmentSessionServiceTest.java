package com.example.smartconsult.assessment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AssessmentSessionServiceTest {

    private final AssessmentSessionService assessmentSessionService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    AssessmentSessionServiceTest(AssessmentSessionService assessmentSessionService, JdbcTemplate jdbcTemplate) {
        this.assessmentSessionService = assessmentSessionService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void getOrCreateInProgressSessionCreatesOncePerUser() {
        Long userId = 92001L;
        jdbcTemplate.update("DELETE FROM assessment_session WHERE user_id = ?", userId);

        Long firstSessionId = assessmentSessionService.getOrCreateInProgressSession(userId).getId();
        Long secondSessionId = assessmentSessionService.getOrCreateInProgressSession(userId).getId();

        Integer sessionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM assessment_session WHERE user_id = ? AND status = 'IN_PROGRESS'",
                Integer.class,
                userId);
        assertThat(secondSessionId).isEqualTo(firstSessionId);
        assertThat(sessionCount).isEqualTo(1);
    }
}
