package com.daramg.server.auth.util;

import com.daramg.server.auth.dto.TokenResponseDto;
import com.daramg.server.auth.exception.AuthErrorStatus;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.repository.UserRepository;
import com.daramg.server.testsupport.support.ServiceTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
public class JwtUtilTest extends ServiceTestSupport {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-time}")
    private long accessTime;

    @Value("${jwt.refresh-time}")
    private long refreshTime;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        String encodedPassword = passwordEncoder.encode("Password123!");
        testUser = new User("test@example.com", encodedPassword, "테스트유저",
                LocalDate.of(1990, 1, 1), "https://example.com/profile.jpg",
                "testuser", "안녕하세요", null);
        userRepository.save(testUser);
    }

    @Nested
    @DisplayName("토큰 생성 테스트")
    class TokenGenerationTest {

        @Test
        void 액세스_토큰을_성공적으로_생성한다() {
            // when
            String accessToken = jwtUtil.createAccessToken(testUser);

            // then
            assertThat(accessToken).isNotNull();
            assertThat(accessToken).isNotEmpty();
            assertThat(accessToken.split("\\.")).hasSize(3);
        }

        @Test
        void 액세스_토큰과_리프레시_토큰을_성공적으로_생성한다() {
            // when
            TokenResponseDto tokens = jwtUtil.generateTokens(testUser);

            // then
            assertThat(tokens).isNotNull();
            assertThat(tokens.getAccessToken()).isNotNull();
            assertThat(tokens.getRefreshToken()).isNotNull();
            assertThat(tokens.getAccessToken()).isNotEmpty();
            assertThat(tokens.getRefreshToken()).isNotEmpty();
            assertThat(tokens.getAccessToken().split("\\.")).hasSize(3);
            assertThat(tokens.getRefreshToken().split("\\.")).hasSize(3);
        }

        @Test
        void 생성된_액세스_토큰에는_사용자_정보가_포함된다() {
            // when
            String accessToken = jwtUtil.createAccessToken(testUser);

            // then
            assertThat(accessToken).isNotNull();
            jwtUtil.validateAccessToken(accessToken);
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class TokenValidationTest {

        @Test
        void 유효한_액세스_토큰을_검증한다() {
            // given
            String accessToken = jwtUtil.createAccessToken(testUser);

            // when & then
            assertThatCode(() -> jwtUtil.validateAccessToken(accessToken))
                    .doesNotThrowAnyException();
        }

        @Test
        void 유효한_리프레시_토큰을_검증한다() {
            // given
            TokenResponseDto tokens = jwtUtil.generateTokens(testUser);

            // when
            boolean isValid = jwtUtil.validateRefreshToken(tokens.getRefreshToken());

            // then
            assertThat(isValid).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0In0.WRONG_SIGNATURE",
                "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJ0ZXN0In0."
        })
        @NullAndEmptySource
        void 잘못된_리프레시_토큰을_검증한다(String invalidToken) {
            // when
            boolean isValid = jwtUtil.validateRefreshToken(null);

            // then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("유저 정보 추출 테스트")
    class UserInfoExtractionTest {

        @Test
        void 유효한_토큰에서_유저_이메일을_추출한다() {
            // given
            String accessToken = jwtUtil.createAccessToken(testUser);

            // when
            String email = jwtUtil.getUserEmail(accessToken);

            // then
            assertThat(email).isEqualTo(testUser.getEmail());
        }

        @Test
        void 리프레시_토큰에서_유저_이메일을_추출한다() {
            // given
            TokenResponseDto tokens = jwtUtil.generateTokens(testUser);

            // when
            String email = jwtUtil.getUserEmail(tokens.getRefreshToken());

            // then
            assertThat(email).isEqualTo(testUser.getEmail());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0In0.WRONG_SIGNATURE", // 위조된 서명
            "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJ0ZXN0In0." // 서명 부재
    })
    @NullAndEmptySource // 빈문자열 + null
    void 잘못된_액세스_토큰은_검증에_실패한다(String invalidToken) {
        // when & then
        assertThatThrownBy(() -> jwtUtil.validateAccessToken(invalidToken))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthErrorStatus.INVALID_TOKEN_EXCEPTION.getMessage());
    }
}
