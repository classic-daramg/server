package com.daramg.server.composer.dto;

import com.daramg.server.composer.domain.Composer;
import com.daramg.server.post.dto.StoryPostStatsDto;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

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
        boolean isLiked,
        long storyPostCount,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Instant lastStoryPostAt
) {
    public static ComposerResponseDto from(Composer composer, boolean isLiked, StoryPostStatsDto stats) {
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
                isLiked,
                stats != null ? stats.storyPostCount() : 0L,
                stats != null ? stats.lastStoryPostAt() : null
        );
    }
}
