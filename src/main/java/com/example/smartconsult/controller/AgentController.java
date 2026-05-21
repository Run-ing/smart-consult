package com.example.smartconsult.controller;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
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

    public AgentController(
            ChatModel dashScopeChatModel,
            AgentToolFactory agentToolFactory,
            @Value("${app.agent.system-prompt-path:docs/prompts/chronic-risk-assessment-agent-system-prompt.txt}") String systemPromptPath) {

        String systemPrompt = loadSystemPrompt(systemPromptPath);
        this.agent = ReactAgent.builder()
                .name("consult_agent")
                .model(dashScopeChatModel)
                .tools(agentToolFactory.createTools())
                .systemPrompt(systemPrompt)
                .saver(new MemorySaver())
                .build();

    }

    @GetMapping("/test01")
    public String test01() throws GraphRunnerException {
        AssistantMessage response = agent.call("你好");
        return response.getText();
    }


    private String loadSystemPrompt(String systemPromptPath) {
        try {
            return Files.readString(Path.of(systemPromptPath), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("读取智能体系统提示词失败：" + systemPromptPath, exception);
        }
    }
}
