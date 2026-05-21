package com.example.smartconsult.user;

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

import java.time.LocalDate;
import java.time.Period;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserHealthProfileFlowTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    UserHealthProfileFlowTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    void firstLoginRequiresHealthProfileAndCompletesAfterSubmit() throws Exception {
        String phone = "13800138101";
        String firstToken = login(phone, requestMockCode(phone));

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profileCompleted").value(false));

        LocalDate birthDate = LocalDate.of(1990, 5, 20);
        int expectedAge = Period.between(birthDate, LocalDate.now()).getYears();

        mockMvc.perform(post("/user/profile")
                        .header("Authorization", "Bearer " + firstToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sex": "MALE",
                                  "birthDate": "1990-05-20",
                                  "heightCm": 175.5,
                                  "weightKg": 70.2,
                                  "waistCm": 82.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.sex").value("MALE"))
                .andExpect(jsonPath("$.data.birthDate").value("1990-05-20"))
                .andExpect(jsonPath("$.data.age").value(expectedAge))
                .andExpect(jsonPath("$.data.heightCm").value(175.5))
                .andExpect(jsonPath("$.data.weightKg").value(70.2))
                .andExpect(jsonPath("$.data.waistCm").value(82.0));

        String secondToken = login(phone, requestMockCode(phone));

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profileCompleted").value(true));
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
