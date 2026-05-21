package com.example.smartconsult.demo;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @author lizhifu
 */
@Component
@ConditionalOnProperty(prefix = "app.demo.dashscope", name = "enabled", havingValue = "true")
public class DashScopeApiDemo implements CommandLineRunner {

    @Resource
    private ChatModel dashScopeChatModel;

    @Override
    public void run(String... args) throws Exception {
        ChatResponse chatResponse = dashScopeChatModel.call(new Prompt("你好, 千问!"));
        System.out.println(chatResponse.getResult().getOutput().getText());
    }
}
