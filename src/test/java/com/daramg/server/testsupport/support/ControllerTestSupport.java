package com.daramg.server.testsupport.support;

import com.daramg.server.auth.filter.JwtAuthorizationFilter;
import com.daramg.server.auth.resolver.AuthUserResolver;
import com.daramg.server.auth.util.JwtUtil;
import com.daramg.server.common.config.ErrorCodeRegistryConfig;
import com.daramg.server.common.exception.ErrorCodeRegistry;
import com.daramg.server.common.exception.GlobalExceptionHandler;
import com.daramg.server.testsupport.config.RestDocsConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({RestDocsConfig.class,
        ErrorCodeRegistry.class,
        ErrorCodeRegistryConfig.class,
        GlobalExceptionHandler.class,
})
@TestPropertySource(properties = {
        "cors.allowed-origins=http://localhost:3000,https://client-git-main-classicdaramgs-projects.vercel.app"
})
public abstract class ControllerTestSupport {

    @Value("${cookie.access-name}")
    public String COOKIE_NAME;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected RestDocumentationResultHandler restDocsHandler;

    @MockitoBean
    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @MockitoBean
    protected AuthenticationEntryPoint authenticationEntryPoint;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    protected AuthUserResolver authUserResolver;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
               RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

}
