package com.daramg.server.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.daramg.server.auth.dto.TokenResponseDto;
import com.daramg.server.auth.exception.AuthErrorStatus;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-time}")
    private long ACCESS_TIME_IN_SECONDS;

    @Value("${jwt.refresh-time}")
    private long REFRESH_TIME_IN_SECONDS;

    /**
     * DB 조회 안 해도 되도록 SecurityContext 에 클레임 등록,
     * 유저 role은 추후 등록
     */
    private static final String CLAIM_ID = "id";
    private static final String CLAIM_EMAIL = "email";
    //private static final String CLAIM_ROLE = "role";

    public TokenResponseDto generateTokens(User user) {
        String accessToken = createAccessToken(user);

        Date now = new Date();
        String refreshToken = JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(now.getTime() + REFRESH_TIME_IN_SECONDS))
                .sign(Algorithm.HMAC512(secretKey));

        return new TokenResponseDto(accessToken, refreshToken);
    }

    public String createAccessToken(User user) {
        Date now = new Date();
        return JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(now.getTime() + ACCESS_TIME_IN_SECONDS))
                .withClaim(CLAIM_ID, user.getId())
                .withClaim(CLAIM_EMAIL, user.getEmail())
                //.withClaim(CLAIM_ROLE, user.getRole().name())
                .sign(Algorithm.HMAC512(secretKey));
    }

    public void validateAccessToken(final String token) {
        try {
            DecodedJWT decodedJWT = getDecodedJWT(token);
            validateClaims(decodedJWT);
        } catch (TokenExpiredException e) {
            throw new BusinessException(AuthErrorStatus.TOKEN_EXPIRED_EXCEPTION);
        } catch (JWTVerificationException e) {
            throw new BusinessException(AuthErrorStatus.INVALID_TOKEN_EXCEPTION);
        }
    }

    public boolean validateRefreshToken(final String token) {
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    private DecodedJWT getDecodedJWT(final String token) {
        Algorithm algorithm = Algorithm.HMAC512(secretKey);
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }

    private void validateClaims(final DecodedJWT decodedJWT) {
        Claim idClaim = decodedJWT.getClaim(CLAIM_ID);
        Claim emailClaim = decodedJWT.getClaim(CLAIM_EMAIL);
        // role 검증

        if (idClaim.isNull() || emailClaim.isNull()) {
            throw new BusinessException(AuthErrorStatus.INVALID_TOKEN_EXCEPTION);
        }

        Long id = idClaim.asLong();
        String email = emailClaim.asString();

        if (id == null || email == null) {
            throw new BusinessException(AuthErrorStatus.INVALID_TOKEN_EXCEPTION);
        }
    }

    public String getUserEmail(final String token) {
        try {
            DecodedJWT decodedJWT = getDecodedJWT(token);
            String subject = decodedJWT.getSubject();

            if (subject == null) {
                throw new BusinessException(AuthErrorStatus.INVALID_TOKEN_EXCEPTION);
            }
            return subject;
        } catch (JWTVerificationException e) {
            throw new BusinessException(AuthErrorStatus.INVALID_TOKEN_EXCEPTION);
        }
    }


    // TODO: 매니저 엔티티 생성 시 role 검증 추가
//    public String getRole(String token) {
//        DecodedJWT decodedJWT = getDecodedJWT(token);
//        Claim roleClaim = decodedJWT.getClaim(CLAIM_ROLE);
//
//        if (roleClaim.isNull()) {
//            throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
//        }
//        return roleClaim.asString();
//    }

}
