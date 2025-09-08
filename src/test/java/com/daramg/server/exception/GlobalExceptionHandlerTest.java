package com.daramg.server.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void 비즈니스_예외처리_정상_테스트() throws Exception {
        // when & then
        mockMvc.perform(get("/exception/business"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COMMON_403"))
                .andExpect(jsonPath("$.message").value("금지된 요청입니다."));
    }

    @Test
    @WithMockUser
    void 유효성_검사_예외처리_정상_테스트() throws Exception {
        // given
        String invalidEmptyRequest = "";
        String invalidSizeRequest = "3글자 초과";

        ExceptionTestController.TestRequest invalidRequest =
                new ExceptionTestController.TestRequest(invalidEmptyRequest, invalidSizeRequest);

        // when & then
        mockMvc.perform(post("/exception/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 요청입니다."))
                .andExpect(jsonPath("$.fieldErrors", hasSize(2)));
    }

    @Test
    @WithMockUser
    void 예상치못한_예외처리_정상_테스트() throws Exception {
        // when & then
        mockMvc.perform(get("/exception/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("COMMON_500"))
                .andExpect(jsonPath("$.message").value("서버 에러, 서버 관리자에게 문의 바랍니다."));
    }
}
