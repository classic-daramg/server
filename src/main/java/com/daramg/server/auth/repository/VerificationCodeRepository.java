package com.daramg.server.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VerificationCodeRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "code:";
    private static final Duration EXPIRY_DURATION = Duration.ofMinutes(3);

    public void save(String email, String code) {
        redisTemplate.opsForValue().set(KEY_PREFIX + email, code, EXPIRY_DURATION);
    }

    public Optional<String> findByEmail(String email) {
        String code = redisTemplate.opsForValue().get(KEY_PREFIX + email);
        return Optional.ofNullable(code);
    }

    public void deleteByEmail(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }
}
