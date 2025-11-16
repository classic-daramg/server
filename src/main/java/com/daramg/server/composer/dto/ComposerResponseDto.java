package com.daramg.server.composer.dto;

import com.daramg.server.composer.domain.Composer;

public record ComposerResponseDto(
        long composerId,
        String koreanName,
        String bio,
        boolean isLiked
) {
    public static ComposerResponseDto from(Composer composer, boolean isLiked) {
        return new ComposerResponseDto(
                composer.getId(),
                composer.getKoreanName(),
                composer.getBio(),
                isLiked
        );
    }
}
