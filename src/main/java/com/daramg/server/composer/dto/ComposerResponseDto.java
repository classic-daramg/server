package com.daramg.server.composer.dto;

import com.daramg.server.composer.domain.Composer;

public record ComposerResponseDto(
        long composerId,
        String koreanName,
        String englishName,
        String nativeName,
        String nationality,
        String gender,
        Short birthYear,
        Short deathYear,
        String bio,
        boolean isLiked
) {
    public static ComposerResponseDto from(Composer composer, boolean isLiked) {
        return new ComposerResponseDto(
                composer.getId(),
                composer.getKoreanName(),
                composer.getEnglishName(),
                composer.getNativeName(),
                composer.getNationality(),
                composer.getGender() != null ? composer.getGender().name() : null,
                composer.getBirthYear(),
                composer.getDeathYear(),
                composer.getBio(),
                isLiked
        );
    }
}
