package com.example.smartconsult.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentChatRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesMessageFromFrontendPayload() throws Exception {
        AgentChatRequest request = objectMapper.readValue("""
                {
                  "message": "开始评估"
                }
                """, AgentChatRequest.class);

        assertThat(request.getMessage()).isEqualTo("开始评估");
    }

    @Test
    void canCreateInitRequestForAutomaticFirstQuestion() {
        AgentChatRequest request = AgentChatRequest.init();

        assertThat(request.getMessage()).contains("请先调用 get_current_user_profile");
        assertThat(request.getMessage()).contains("get_next_question");
    }
}
