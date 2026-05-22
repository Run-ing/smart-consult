package com.example.smartconsult.controller;

import lombok.Data;

@Data
public class AgentChatRequest {

    private String message;

    public static AgentChatRequest init() {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("""
                用户已进入慢病风险评估对话页面。请先调用 get_current_user_profile 读取当前登录用户基础资料，再调用 get_next_question 获取当前需要询问的一道题。随后直接向用户提问，不要等待用户先发消息，不要展示内部 JSON 或工具调用细节。
                """);
        return request;
    }
}
