package com.daramg.server.auth.domain;

import com.daramg.server.auth.dto.SignupRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Nested
    @DisplayName("SignupDto 이메일 검증 테스트")
    class SignupDtoEmailValidationTest {

        @Test
        @DisplayName("이메일이 빈 값이면 검증에 실패한다")
        void email_NotBlank_실패() {
            // given
            SignupRequestDto dto = new SignupRequestDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "", // 빈 이메일
                    "Password123!",
                    "홍길동123",
                    "안녕하세요"
            );

            // when
            Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("이메일은 필수입니다");
        }

        @Test
        @DisplayName("이메일 형식이 올바르지 않으면 검증에 실패한다")
        void email_InvalidFormat_실패() {
            // given
            SignupRequestDto dto = new SignupRequestDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "invalid-email", // 잘못된 이메일 형식
                    "Password123!",
                    "홍길동123",
                    "안녕하세요"
            );

            // when
            Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("이메일 형식이 올바르지 않습니다");
        }

        @Test
        @DisplayName("이메일 형식이 올바르면 검증에 성공한다")
        void email_ValidFormat_성공() {
            // given
            SignupRequestDto dto = new SignupRequestDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "test@example.com", // 올바른 이메일 형식
                    "Password123!",
                    "홍길동123",
                    "안녕하세요"
            );

            // when
            Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("SignupDto 비밀번호 검증 테스트")
    class SignupDtoPasswordValidationTest {

        @Test
        @DisplayName("비밀번호가 빈 값이면 검증에 실패한다")
        void password_NotBlank_실패() {
            // given
            SignupRequestDto dto = new SignupRequestDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "test@example.com",
                    "", // 빈 비밀번호
                    "홍길동123",
                    "안녕하세요"
            );

            // when
            Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("비밀번호는 영어 대/소문자, 숫자, 특수문자를 모두 포함하여 10자 이상이어야 합니다");
        }

        @Test
        @DisplayName("비밀번호가 10자 미만이면 검증에 실패한다")
        void password_TooShort_실패() {
            // given
            SignupRequestDto dto = new SignupRequestDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "test@example.com",
                    "Pass123!", // 9자리 비밀번호
                    "홍길동123",
                    "안녕하세요"
            );

            // when
            Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("비밀번호는 영어 대/소문자, 숫자, 특수문자를 모두 포함하여 10자 이상이어야 합니다");
        }

        @Test
        @DisplayName("비밀번호에 대문자가 없으면 검증에 실패한다")
        void password_NoUppercase_실패() {
            // given
            SignupRequestDto dto = new SignupRequestDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "test@example.com",
                    "password123!", // 대문자 없음
                    "홍길동123",
                    "안녕하세요"
            );

            // when
            Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("비밀번호는 영어 대/소문자, 숫자, 특수문자를 모두 포함하여 10자 이상이어야 합니다");
        }

        @Test
        @DisplayName("비밀번호에 소문자가 없으면 검증에 실패한다")
        void password_NoLowercase_실패() {
            // given
            SignupRequestDto dto = new SignupRequestDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "test@example.com",
                    "PASSWORD123!", // 소문자 없음
                    "홍길동123",
                    "안녕하세요"
            );

            // when
            Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("비밀번호는 영어 대/소문자, 숫자, 특수문자를 모두 포함하여 10자 이상이어야 합니다");
        }

        @Test
        @DisplayName("비밀번호에 숫자가 없으면 검증에 실패한다")
        void password_NoNumber_실패() {
            // given
            SignupRequestDto dto = new SignupRequestDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "test@example.com",
                    "Password!", // 숫자 없음
                    "홍길동123",
                    "안녕하세요"
            );

            // when
            Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("비밀번호는 영어 대/소문자, 숫자, 특수문자를 모두 포함하여 10자 이상이어야 합니다");
        }

        @Test
        @DisplayName("비밀번호에 특수문자가 없으면 검증에 실패한다")
        void password_NoSpecialChar_실패() {
            // given
            SignupRequestDto dto = new SignupRequestDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "test@example.com",
                    "Password123", // 특수문자 없음
                    "홍길동123",
                    "안녕하세요"
            );

            // when
            Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("비밀번호는 영어 대/소문자, 숫자, 특수문자를 모두 포함하여 10자 이상이어야 합니다");
        }

        @Test
        @DisplayName("비밀번호 형식이 올바르면 검증에 성공한다")
        void password_ValidFormat_성공() {
            // given
            SignupRequestDto dto = new SignupRequestDto(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    "test@example.com",
                    "Password123!", // 올바른 비밀번호 형식
                    "홍길동123",
                    "안녕하세요"
            );

            // when
            Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).isEmpty();
        }
    }

}
