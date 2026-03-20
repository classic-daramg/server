package com.daramg.server.testsupport.support;

import com.daramg.server.auth.config.CorsProperties;
import com.daramg.server.auth.config.SecurityConfig;
import com.daramg.server.auth.filter.JwtAuthorizationFilter;
import com.daramg.server.auth.resolver.AuthUserResolver;
import com.daramg.server.auth.util.JwtUtil;
import com.daramg.server.common.config.ErrorCodeRegistryConfig;
import com.daramg.server.common.exception.ErrorCodeRegistry;
import com.daramg.server.common.exception.GlobalExceptionHandler;
import com.daramg.server.common.validation.BadWordFilter;
import com.daramg.server.testsupport.config.RestDocsConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Import({SecurityConfig.class, CorsProperties.class, RestDocsConfig.class,
        ErrorCodeRegistry.class, ErrorCodeRegistryConfig.class,
        GlobalExceptionHandler.class, BadWordFilter.class})
@TestPropertySource(properties = {
        "cors.allowed-origins=http://localhost:3000,https://client-git-main-classicdaramgs-projects.vercel.app",
        "cookie.access-name=access_token"
})
public abstract class SecurityControllerTestSupport {

    @Value("${cookie.access-name}")
    public String COOKIE_NAME;

    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected RestDocumentationResultHandler restDocsHandler;

    @MockitoBean
    protected JwtAuthorizationFilter jwtAuthorizationFilter;

    @MockitoBean
    protected AuthenticationEntryPoint authenticationEntryPoint;

    @MockitoBean
    protected JwtUtil jwtUtil;

    @MockitoBean
    protected RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    protected AuthUserResolver authUserResolver;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
               RestDocumentationContextProvider restDocumentation) throws Exception {
        doAnswer(inv -> {
            inv.getArgument(2, FilterChain.class)
                    .doFilter(inv.getArgument(0, ServletRequest.class), inv.getArgument(1, ServletResponse.class));
            return null;
        }).when(jwtAuthorizationFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .apply(springSecurity())
                .build();
    }
}
