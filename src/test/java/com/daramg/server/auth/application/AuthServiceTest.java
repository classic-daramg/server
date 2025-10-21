package com.daramg.server.auth.application;

import com.daramg.server.auth.dto.PasswordDto;
import com.daramg.server.auth.dto.SignupDto;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.domain.user.domain.User;
import com.daramg.server.domain.user.repository.UserRepository;
import com.daramg.server.testsupport.support.ServiceTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AuthServiceTest extends ServiceTestSupport {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("회원가입 성공 테스트")
    class SignupSuccess {
        @Test
        void 회원가입을_정상적으로_완료한다() {
            //given
            SignupDto signupDto = new SignupDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "test@example.com",
                    "Password123!",
                    "https://example.com/profile.jpg",
                    "홍길동123",
                    "안녕하세요"
            );

            //when
            authService.signup(signupDto);

            //then
            User savedUser = userRepository.findAll().get(0);
            
            assertThat(savedUser.getName()).isEqualTo(signupDto.getName());
            assertThat(savedUser.getBirthDate()).isEqualTo(signupDto.getBirthdate());
            assertThat(savedUser.getEmail()).isEqualTo(signupDto.getEmail());
            assertThat(savedUser.getPassword()).isEqualTo(signupDto.getPassword());
            assertThat(savedUser.getProfileImage()).isEqualTo(signupDto.getProfileImage());
            assertThat(savedUser.getNickname()).isEqualTo(signupDto.getNickname());
            assertThat(savedUser.getBio()).isEqualTo(signupDto.getBio());
        }
    }

    @Nested
    @Transactional
    @DisplayName("비밀번호 변경 성공 테스트")
    class ResetPasswordSuccess {
        private User user;

        @BeforeEach
        void setUp() {
            user = new User("test@example.com", "OldPassword123!", "홍길동", 
                    LocalDate.of(1990, 1, 1), "https://example.com/profile.jpg", 
                    "홍길동123", "안녕하세요", null);
            userRepository.save(user);
        }

        @Test
        void 비밀번호를_정상적으로_변경한다() {
            //given
            String newPassword = "NewPassword123!";
            PasswordDto passwordDto = new PasswordDto(newPassword);

            //when
            authService.resetPassword(passwordDto, user);

            //then
            User updatedUser = userRepository.findById(user.getId()).orElseThrow();
            assertThat(updatedUser.getPassword()).isEqualTo(newPassword);
        }
    }

    @Nested
    @DisplayName("회원가입 실패 테스트")
    class SignupFailure {
        private User existingUser;

        @BeforeEach
        void setUp() {
            existingUser = new User("existing@example.com", "Password123!", "기존닉네임", 
                    LocalDate.of(1990, 1, 1), "https://example.com/profile.jpg", 
                    "기존닉네임", "안녕하세요", null);
            userRepository.save(existingUser);
        }

        @Test
        void 중복된_이메일로_회원가입시_예외가_발생한다() {
            //given
            SignupDto signupDto = new SignupDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "existing@example.com", // 중복된 이메일
                    "Password123!",
                    "https://example.com/profile.jpg",
                    "새로운닉네임",
                    "안녕하세요"
            );

            //when & then
            assertThatThrownBy(() -> authService.signup(signupDto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("중복된 이메일입니다.");
        }

        @Test
        void 중복된_닉네임으로_회원가입시_예외가_발생한다() {
            //given
            SignupDto signupDto = new SignupDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "new@example.com",
                    "Password123!",
                    "https://example.com/profile.jpg",
                    "기존닉네임", // 중복된 닉네임
                    "안녕하세요"
            );

            //when & then
            assertThatThrownBy(() -> authService.signup(signupDto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("중복된 닉네임입니다.");
        }

        @Test
        void 이메일과_닉네임이_모두_중복될_때_이메일_중복_예외가_먼저_발생한다() {
            //given
            SignupDto signupDto = new SignupDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "existing@example.com", // 중복된 이메일
                    "Password123!",
                    "https://example.com/profile.jpg",
                    "기존닉네임", // 중복된 닉네임
                    "안녕하세요"
            );

            //when & then
            assertThatThrownBy(() -> authService.signup(signupDto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("중복된 이메일입니다.");
        }
    }

}
