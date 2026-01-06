package com.daramg.server.auth.application;

import com.daramg.server.auth.dto.LoginRequestDto;
import com.daramg.server.auth.dto.PasswordRequestDto;
import com.daramg.server.auth.dto.SignupRequestDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AuthServiceTest extends ServiceTestSupport {

    @Value("${jwt.refresh-time}")
    private long REFRESH_TOKEN_VALID_TIME;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;

    private User deletedUser;

    @BeforeEach
    void setUp() {
        String encodedPassword = passwordEncoder.encode("Password123!");

        user = new User("svt@pledis.com", encodedPassword, "권순영",
                LocalDate.of(1990, 1, 1), "https://example.com/profile.jpg",
                "svt", "안녕하세요", null);
        userRepository.save(user);
    }

    @Nested
    @DisplayName("회원가입 성공 테스트")
    class SignupSuccess {
        @Test
        void 회원가입을_정상적으로_완료한다() {
            //given
            SignupRequestDto signupDto = new SignupRequestDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "test@example.com",
                    "Password123!",
                    "홍길동123",
                    "안녕하세요"
            );

            //when
            authService.signup(signupDto, null);

            //then
            User savedUser = userRepository.findAll().get(1);
            
            assertThat(savedUser.getName()).isEqualTo(signupDto.getName());
            assertThat(savedUser.getBirthDate()).isEqualTo(signupDto.getBirthdate());
            assertThat(savedUser.getEmail()).isEqualTo(signupDto.getEmail());
            // 비밀번호 암호화 정책으로 인해 원문과 같지 않을 수 있음 (기존 테스트 유지)
            // 이미지가 없으면 기본 이미지가 사용됨
            assertThat(savedUser.getProfileImage()).isNotNull();
            assertThat(savedUser.getNickname()).isEqualTo(signupDto.getNickname());
            assertThat(savedUser.getBio()).isEqualTo(signupDto.getBio());
        }
    }

    @Nested
    @Transactional
    @DisplayName("비밀번호를 잊었을 시 비밀번호 재설정 테스트")
    class ResetPasswordSuccess {
        private User localUser;

        @BeforeEach
        void setUp() {
            localUser = new User("test@example.com", passwordEncoder.encode("OldPassword123!"), "홍길동", 
                    LocalDate.of(1990, 1, 1), "https://example.com/profile.jpg", 
                    "홍길동123", "안녕하세요", null);
            userRepository.save(localUser);
        }

        @Test
        void 비밀번호를_정상적으로_변경한다() {
            //given
            String email = "test@example.com";
            String newPassword = "NewPassword123!";
            PasswordRequestDto passwordDto = new PasswordRequestDto(email, newPassword);

            //when
            authService.resetPassword(passwordDto);

            //then
            User updatedUser = userRepository.findById(localUser.getId()).orElseThrow();
            // 암호화 검증은 별도 테스트에서 추가 테스트
            assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isTrue();
        }
    }

    @Nested
    @DisplayName("회원가입 실패 테스트")
    class SignupFailure {
        private User existingUser;

        @BeforeEach
        void setUp() {
            existingUser = new User("existing@example.com", passwordEncoder.encode("Password123!"), "기존닉네임", 
                    LocalDate.of(1990, 1, 1), "https://example.com/profile.jpg", 
                    "기존닉네임", "안녕하세요", null);
            userRepository.save(existingUser);
        }

        @Test
        void 중복된_이메일로_회원가입시_예외가_발생한다() {
            //given
            SignupRequestDto signupDto = new SignupRequestDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "existing@example.com", // 중복된 이메일
                    "Password123!",
                    "새로운닉네임",
                    "안녕하세요"
            );

            //when & then
            assertThatThrownBy(() -> authService.signup(signupDto, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("중복된 이메일입니다.");
        }

        @Test
        void 중복된_닉네임으로_회원가입시_예외가_발생한다() {
            //given
            SignupRequestDto signupDto = new SignupRequestDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "new@example.com",
                    "Password123!",
                    "기존닉네임", // 중복된 닉네임
                    "안녕하세요"
            );

            //when & then
            assertThatThrownBy(() -> authService.signup(signupDto, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("중복된 닉네임입니다.");
        }

        @Test
        void 이메일과_닉네임이_모두_중복될_때_이메일_중복_예외가_먼저_발생한다() {
            //given
            SignupRequestDto signupDto = new SignupRequestDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "existing@example.com", // 중복된 이메일
                    "Password123!",
                    "기존닉네임", // 중복된 닉네임
                    "안녕하세요"
            );

            //when & then
            assertThatThrownBy(() -> authService.signup(signupDto, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("중복된 이메일입니다.");
        }
    }

    @Nested
    @DisplayName("로그인 성공 테스트")
    class LoginSuccess {
        @Test
        void 올바른_이메일과_비밀번호로_로그인을_성공한다() {
            //given
            LoginRequestDto loginDto = new LoginRequestDto("svt@pledis.com", "Password123!");

            //when
            TokenResponseDto result = authService.login(loginDto);

            //then
            assertThat(result.getAccessToken()).isNotNull();
            assertThat(result.getRefreshToken()).isNotNull();
        }
    }

    @Nested
    @DisplayName("로그인 실패 테스트")
    class LoginFailure {
        @Test
        void 존재하지_않는_이메일로_로그인하면_예외가_발생한다() {
            //given
            LoginRequestDto loginDto = new LoginRequestDto("nonexistent@example.com", "Password123!");

            //when & then
            assertThatThrownBy(() -> authService.login(loginDto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(AuthErrorStatus.USER_NOT_FOUND_EXCEPTION.getMessage());
        }

        @Test
        void 올바른_이메일이지만_잘못된_비밀번호로_로그인시_예외가_발생한다() {
            //given
            LoginRequestDto loginDto = new LoginRequestDto("svt@pledis.com", "WrongPassword123!");

            //when & then
            assertThatThrownBy(() -> authService.login(loginDto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(AuthErrorStatus.USER_NOT_FOUND_EXCEPTION.getMessage());
        }

        @Test
        @Transactional
        void 회원탈퇴한_유저는_로그인이_불가하다() {
            //given
            user.withdraw();
            userRepository.save(user);
            LoginRequestDto loginDto = new LoginRequestDto("svt@pledis.com", "Password123!");

            //when & then
            assertThatThrownBy(() -> authService.login(loginDto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(AuthErrorStatus.USER_NOT_ACTIVE.getMessage());
        }
    }

    @Nested
    @DisplayName("유저 로드 테스트")
    class LoadUserByEmailTest {
        @Test
        void 존재하는_이메일로_사용자를_조회한다() {
            //given
            String email = "svt@pledis.com";

            //when
            User result = authService.loadUserByEmail(email);

            //then
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getName()).isEqualTo("권순영");
        }

        @Test
        void 존재하지_않는_이메일로_사용자_조회시_예외가_발생한다() {
            //given
            String email = "nonexistent@example.com";

            //when & then
            assertThatThrownBy(() -> authService.loadUserByEmail(email))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(AuthErrorStatus.USER_NOT_FOUND_EXCEPTION.getMessage() + "(email: " + email + ")");
        }
    }

    @Nested
    @DisplayName("액세스 토큰 재발급 테스트")
    class RefreshAccessTokenTest {
        private String validRefreshToken;

        @BeforeEach
        void setUp() {
            // 로그인을 통해 유효한 refresh token 생성
            LoginRequestDto loginDto = new LoginRequestDto("svt@pledis.com", "Password123!");
            TokenResponseDto tokens = authService.login(loginDto);
            validRefreshToken = tokens.getRefreshToken();
        }

        @Test
        void 유효한_리프레시_토큰으로_액세스_토큰을_갱신한다() {
            //when
            TokenResponseDto result = authService.refreshAccessToken(validRefreshToken);

            //then
            assertThat(result.getAccessToken()).isNotNull();
            assertThat(result.getRefreshToken()).isEqualTo(validRefreshToken);
        }

        @Test
        void 유효하지_않은_리프레시_토큰으로_갱신시_예외가_발생한다() {
            //given
            String invalidRefreshToken = "invalid.refresh.token";

            //when & then
            assertThatThrownBy(() -> authService.refreshAccessToken(invalidRefreshToken))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(AuthErrorStatus.INVALID_TOKEN_EXCEPTION.getMessage());
        }
    }

    // 추가: 비밀번호 암호화 검사 전용 테스트
    @Nested
    @DisplayName("비밀번호 암호화 검사")
    class PasswordEncoding {

        @Test
        @DisplayName("회원가입 시 저장된 비밀번호는 원문과 다르고, 매칭이 성공한다")
        void signup_passwordIsEncodedAndMatches() {
            // given
            SignupRequestDto dto = new SignupRequestDto(
                    "테스터",
                    LocalDate.of(1995, 5, 5),
                    "encode@example.com",
                    "Encode123!",
                    "encUser",
                    "bio"
            );

            // when
            authService.signup(dto, null);
            User saved = userRepository.findByEmail("encode@example.com").orElseThrow();

            // then
            assertThat(saved.getPassword()).isNotEqualTo(dto.getPassword());
            assertThat(passwordEncoder.matches(dto.getPassword(), saved.getPassword())).isTrue();
        }

        @Test
        @DisplayName("비밀번호 재설정 시 저장된 비밀번호는 원문과 다르고, 매칭이 성공한다")
        void reset_passwordIsEncodedAndMatches() {
            // given
            String email = "reset@example.com";
            User u = new User(email, passwordEncoder.encode("Old123!"), "리셋",
                    LocalDate.of(1991, 1, 2), null, "resetU", null, null);
            userRepository.save(u);

            PasswordRequestDto dto = new PasswordRequestDto(email, "NewPass123!");

            // when
            authService.resetPassword(dto);
            User updated = userRepository.findByEmail(email).orElseThrow();

            // then
            assertThat(updated.getPassword()).isNotEqualTo("NewPass123!");
            assertThat(passwordEncoder.matches("NewPass123!", updated.getPassword())).isTrue();
        }
    }

    @Nested
    @DisplayName("회원탈퇴 테스트")
    class SignOutTest {
        @Test
        @Transactional
        void 회원탈퇴를_정상적으로_완료한다() {
            //given
            User testUser = new User("withdraw@example.com", passwordEncoder.encode("Password123!"), "탈퇴유저",
                    LocalDate.of(1990, 1, 1), "https://example.com/profile.jpg",
                    "withdrawUser", "안녕하세요", null);
            userRepository.save(testUser);

            //when
            authService.signOut(testUser);

            //then
            User withdrawnUser = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(withdrawnUser.getUserStatus()).isEqualTo(com.daramg.server.user.domain.UserStatus.DELETED);
            assertThat(withdrawnUser.getDeletedAt()).isNotNull();
        }

        @Test
        @Transactional
        void 회원탈퇴_후_사용자는_비활성화_상태가_된다() {
            //given
            User testUser = new User("inactive@example.com", passwordEncoder.encode("Password123!"), "비활성유저",
                    LocalDate.of(1990, 1, 1), "https://example.com/profile.jpg",
                    "inactiveUser", "안녕하세요", null);
            userRepository.save(testUser);

            //when
            authService.signOut(testUser);

            //then
            User withdrawnUser = userRepository.findById(testUser.getId()).orElseThrow();
            assertThat(withdrawnUser.isActive()).isFalse();
        }
    }
}
