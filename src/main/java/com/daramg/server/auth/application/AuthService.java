package com.daramg.server.auth.application;

import com.daramg.server.auth.dto.TokenResponseDto;
import com.daramg.server.auth.exception.AuthErrorStatus;
import com.daramg.server.auth.util.JwtUtil;
import com.daramg.server.common.application.S3ImageService;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.user.domain.User;
import com.daramg.server.auth.domain.SignupVo;
import com.daramg.server.auth.dto.LoginRequestDto;
import com.daramg.server.auth.dto.PasswordRequestDto;
import com.daramg.server.auth.dto.SignupRequestDto;
import com.daramg.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    @Value("${jwt.refresh-time}")
    private long REFRESH_TOKEN_VALID_TIME;

    private final UserRepository userRepository;
    private final JwtUtil jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final S3ImageService s3ImageService;

    public void signup(SignupRequestDto dto, MultipartFile image){
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("중복된 이메일입니다.");
        }
        if (userRepository.existsByNickname(dto.getNickname())){
            throw new BusinessException("중복된 닉네임입니다.");
        }
        // TODO: bio, 닉네임에 금칙어 검사
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        
        // 이미지가 있으면 S3에 업로드하고 URL을 받아옴, 없으면 null (기본 이미지 사용)
        String profileImageUrl = null;
        if (image != null && !image.isEmpty()) {
            profileImageUrl = s3ImageService.uploadImage(image);
        }
        
        SignupVo vo = new SignupVo(
                dto.getName(),
                dto.getBirthdate(),
                dto.getEmail(),
                encodedPassword,
                profileImageUrl,
                dto.getNickname(),
                dto.getBio()
        );
        User user = User.from(vo);
        userRepository.save(user);
    }

    public TokenResponseDto login(LoginRequestDto dto){
        User user = userRepository.findByEmail(dto.getEmail())
                .filter(u -> passwordEncoder.matches(dto.getPassword(), u.getPassword()))
                .orElseThrow(() -> new BusinessException(AuthErrorStatus.USER_NOT_FOUND_EXCEPTION));

        if (!user.isActive()){
            throw new BusinessException(AuthErrorStatus.USER_NOT_ACTIVE);
        }
        TokenResponseDto tokens = jwtTokenProvider.generateTokens(user);
        redisTemplate.opsForValue().set(
                user.getEmail(),
                tokens.getRefreshToken(),
                REFRESH_TOKEN_VALID_TIME,
                TimeUnit.MILLISECONDS
        );
        return tokens;
    }

    public TokenResponseDto refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new BusinessException(AuthErrorStatus.INVALID_TOKEN_EXCEPTION);
        }

        String userEmail = jwtTokenProvider.getUserEmail(refreshToken);
        String originRefreshToken = redisTemplate.opsForValue().get(userEmail);

        if (originRefreshToken == null || !originRefreshToken.equals(refreshToken)) {
            throw new BusinessException(AuthErrorStatus.INVALID_TOKEN_EXCEPTION);
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(AuthErrorStatus.USER_NOT_FOUND_EXCEPTION));

        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        return new TokenResponseDto(newAccessToken, refreshToken);
    }

    public void logout(User user){
        redisTemplate.delete(user.getEmail());
    }

    public void signOut(User user){
        redisTemplate.delete(user.getEmail());
        user.withdraw();
    }

    public void resetPassword(PasswordRequestDto dto){
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BusinessException(AuthErrorStatus.USER_NOT_FOUND_EXCEPTION));

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        user.changePassword(encodedPassword);
        logout(user);
    }

    public User loadUserByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("(email: " + email + ")", AuthErrorStatus.USER_NOT_FOUND_EXCEPTION));
    }
}
