package com.example.smartconsult.controller;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.example.smartconsult.assessment.AssessmentSessionService;
import com.example.smartconsult.auth.CurrentUserContext;
import com.example.smartconsult.common.Result;
import com.example.smartconsult.exception.BusinessException;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author lizhifu
 */
@RestController
@RequestMapping("/agent")
public class AgentController {

    private final ReactAgent agent;
    private final AssessmentSessionService assessmentSessionService;

    public AgentController(
            ChatModel dashScopeChatModel,
            AgentToolFactory agentToolFactory,
            AssessmentSessionService assessmentSessionService,
            @Value("${app.agent.system-prompt-path:docs/prompts/chronic-risk-assessment-agent-system-prompt.txt}") String systemPromptPath) {

        this.assessmentSessionService = assessmentSessionService;
        String systemPrompt = loadSystemPrompt(systemPromptPath);
        this.agent = ReactAgent.builder()
                .name("consult_agent")
                .model(dashScopeChatModel)
                .tools(agentToolFactory.createTools())
                .systemPrompt(systemPrompt)
                .saver(new MemorySaver())
                .build();

    }

    @PostMapping("/chat")
    public Result<AgentChatResponse> chat(@RequestBody AgentChatRequest request) throws GraphRunnerException {
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new BusinessException(400, "消息不能为空");
        }
        assessmentSessionService.getOrCreateInProgressSession(CurrentUserContext.getRequired().id());
        AssistantMessage response = agent.call(request.getMessage().trim());
        return Result.success(new AgentChatResponse(response.getText()));
    }

    private String loadSystemPrompt(String systemPromptPath) {
        try {
            return Files.readString(Path.of(systemPromptPath), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("读取智能体系统提示词失败：" + systemPromptPath, exception);
        }
    }
}
