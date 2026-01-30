package com.daramg.server.notice.dto;

import com.daramg.server.notice.domain.Notice;

import java.time.LocalDateTime;
import java.util.List;

public record NoticeResponseDto(
        Long id,
        String title,
        String writerNickname,
        LocalDateTime createdAt,
        String content,
        String ThumbnailImageUrl
) {
    public static NoticeResponseDto from(Notice notice) {
        List<String> imageUrls = notice.getImages();

        return new NoticeResponseDto(
                notice.getId(),
                notice.getTitle(),
                notice.getUser().getNickname(),
                notice.getCreatedAt(),
                notice.getContent(),
                imageUrls.isEmpty() ? null : imageUrls.getFirst()
        );
    }
}
