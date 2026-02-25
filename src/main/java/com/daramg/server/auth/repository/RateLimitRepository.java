package com.daramg.server.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RateLimitRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "ratelimit:";
    private static final String ATTEMPT_PREFIX = "attempts:";
    private static final Duration RATE_LIMIT_DURATION = Duration.ofMinutes(1);
    private static final Duration ATTEMPT_DURATION = Duration.ofMinutes(3);
    private static final int MAX_ATTEMPTS = 5;

    /** 1분에 1번 제한. 제한 초과 시 true 반환 */
    public boolean isRateLimited(String email) {
        String key = RATE_LIMIT_PREFIX + email;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, "1", RATE_LIMIT_DURATION);
        return !Boolean.TRUE.equals(isNew);
    }

    /** 검증 시도 횟수 초과 여부. MAX_ATTEMPTS 이상이면 true 반환 */
    public boolean isAttemptExceeded(String email) {
        String key = ATTEMPT_PREFIX + email;
        String count = redisTemplate.opsForValue().get(key);
        return count != null && Integer.parseInt(count) >= MAX_ATTEMPTS;
    }

    /** 검증 실패 시 시도 횟수 증가 */
    public void incrementAttempt(String email) {
        String key = ATTEMPT_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, ATTEMPT_DURATION);
        }
    }

    /** 검증 성공 또는 새 코드 발급 시 시도 횟수 초기화 */
    public void resetAttempts(String email) {
        redisTemplate.delete(ATTEMPT_PREFIX + email);
    }
}
