package com.daramg.server.banner.dto;

public record BannerUpdateRequestDto(
        String imageUrl,
        String linkUrl,
        Boolean isActive,
        Integer orderIndex
) {
}
