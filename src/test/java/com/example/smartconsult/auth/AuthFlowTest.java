package com.example.smartconsult.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smartconsult.user.entity.SysUser;
import com.example.smartconsult.user.mapper.SysUserMapper;
import com.example.smartconsult.common.ResultCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final SysUserMapper sysUserMapper;

    @Autowired
    AuthFlowTest(MockMvc mockMvc, ObjectMapper objectMapper, SysUserMapper sysUserMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Test
    void requestSmsCodeReturnsMockCode() throws Exception {
        mockMvc.perform(post("/auth/sms-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "13800138000"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.mockCode").isString())
                .andExpect(jsonPath("$.data.expiresInSeconds").value(300));
    }

    @Test
    void loginWithNewPhoneRegistersUserAndReturnsJwt() throws Exception {
        String phone = "13800138001";
        String mockCode = requestMockCode(phone);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "%s",
                                  "smsCode": "%s"
                                }
                                """.formatted(phone, mockCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresInSeconds").value(604800))
                .andExpect(jsonPath("$.data.user.id").isNumber())
                .andExpect(jsonPath("$.data.user.phone").value(phone))
                .andExpect(jsonPath("$.data.user.nickname").value("用户8001"))
                .andExpect(jsonPath("$.data.user.lastLoginTime").isString())
                .andExpect(jsonPath("$.data.registered").value(true));
    }

    @Test
    void loginWithExistingPhoneDoesNotCreateDuplicateUser() throws Exception {
        String phone = "13800138002";
        String firstCode = requestMockCode(phone);
        login(phone, firstCode);
        String secondCode = requestMockCode(phone);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "%s",
                                  "smsCode": "%s"
                                }
                                """.formatted(phone, secondCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.registered").value(false));

        Long userCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone));
        assertThat(userCount).isEqualTo(1);
    }

    @Test
    void wrongCodeFailsAndUsedCodeCannotBeReused() throws Exception {
        String phone = "13800138003";
        String mockCode = requestMockCode(phone);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "%s",
                                  "smsCode": "000000"
                                }
                                """.formatted(phone)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));

        login(phone, mockCode);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "%s",
                                  "smsCode": "%s"
                                }
                                """.formatted(phone, mockCode)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    void meRequiresValidJwt() throws Exception {
        String phone = "13800138004";
        String mockCode = requestMockCode(phone);
        String token = login(phone, mockCode);

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value(phone));
    }

    private String requestMockCode(String phone) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/sms-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "%s"
                                }
                                """.formatted(phone)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("mockCode").asText();
    }

    private String login(String phone, String smsCode) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "%s",
                                  "smsCode": "%s"
                                }
                                """.formatted(phone, smsCode)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("token").asText();
    }
}
