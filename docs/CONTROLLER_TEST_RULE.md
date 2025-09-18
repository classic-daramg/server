# 컨트롤러 테스트 구현 가이드
- 컨트롤러 테스트는 api 문서화를 위한 계층이다.
- 컨트롤러 테스트는 해당 컨트롤러의 메서드와 같은 개수의 테스트 메서드를 가져야 한다.
- 메서드명은 이해하기 쉽고 간결하게 한글로 작성한다.
- 일반적으로 given/when/then 형식으로 작성한다.
- 각 컨트롤러 테스트는 연관된 컨트롤러부터 넣을 정보를 분석해 uri, 쿼리 파라미터, request body 등을 알맞게 정의해야 한다.

## 설정
- 클래스 레벨의 애노테이션은 "@WebMvcTest(컨트롤러_클래스명.class)"을 기본으로 한다.
- 컨트롤러 테스트 클래스는 전부 'ControllerTestSupport'를 상속받아야 한다.

## 모범 예시
```
@Test
void 인증번호로_이메일_인증() throws Exception {
// given
VerificationMailRequest request = new VerificationMailRequest("daramg123@gmail.com", "123456");

        doNothing().when(mailVerificationService).verifyEmailWithCode(Mockito.any(VerificationMailRequest.class));

        // when
        ResultActions result =  mockMvc.perform(post("/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andDo(document("인증번호로_이메일_인증",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("인증번호로 이메일 인증")
                                .description("사용자가 제공한 인증번호로 이메일 주소를 인증합니다.")
                                .requestFields(
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("verificationCode").type(JsonFieldType.STRING).description("인증번호: 6자리 숫자")
                                )
                                .build()
                        )
                ));
    }
```
