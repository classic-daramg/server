package com.daramg.server.auth.application;

import com.daramg.server.auth.dto.PasswordRequestDto;
import com.daramg.server.auth.dto.SignupRequestDto;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServicePasswordEncodingTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 시 비밀번호가 인코딩되어 저장된다")
    void signup_encodesPassword() {
        // given
        String rawPassword = "Aa!23456789";
        String encodedPassword = "{bcrypt}encoded";
        given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);

        SignupRequestDto dto = new SignupRequestDto(
                "홍길동",
                LocalDate.of(1990, 1, 1),
                "user@example.com",
                rawPassword,
                "nickname",
                "hello"
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // when
        authService.signup(dto, null);

        // then
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getPassword()).isEqualTo(encodedPassword);
        assertThat(saved.getPassword()).isNotEqualTo(rawPassword);
    }

    @Test
    @DisplayName("비밀번호 재설정 시 인코딩 반영 및 RT 삭제(로그아웃) 수행")
    void resetPassword_encodesAndLogsOut() {
        // given
        String email = "user2@example.com";
        String rawPassword = "Bb!23456789";
        String encodedPassword = "{bcrypt}encoded2";
        given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);

        User existing = User.builder()
                .email(email)
                .password("{bcrypt}old")
                .name("사용자")
                .birthDate(LocalDate.of(1992, 2, 2))
                .nickname("user2")
                .build();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(existing));

        PasswordRequestDto dto = new PasswordRequestDto(email, rawPassword);

        // when
        authService.resetPassword(dto);

        // then
        assertThat(existing.getPassword()).isEqualTo(encodedPassword);
        verify(redisTemplate).delete(email);
    }
}
