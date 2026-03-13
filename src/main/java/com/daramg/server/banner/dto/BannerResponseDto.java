package com.daramg.server.banner.dto;

import com.daramg.server.banner.domain.Banner;

import java.time.Instant;

public record BannerResponseDto(
        Long id,
        String imageUrl,
        String linkUrl,
        boolean isActive,
        int orderIndex,
        Instant createdAt,
        Instant updatedAt
) {
    public static BannerResponseDto from(Banner banner) {
        return new BannerResponseDto(
                banner.getId(),
                banner.getImageUrl(),
                banner.getLinkUrl(),
                banner.isActive(),
                banner.getOrderIndex(),
                banner.getCreatedAt(),
                banner.getUpdatedAt()
        );
    }
}
