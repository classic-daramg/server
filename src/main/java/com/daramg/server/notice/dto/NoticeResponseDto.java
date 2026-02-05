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
        String thumbnailImageUrl
) {
    public static NoticeResponseDto from(Notice notice) {
        List<String> imageUrls = notice.getImages();

        String thumb = null;
        if (imageUrls != null && !imageUrls.isEmpty()) {
            thumb = imageUrls.get(0); // Java 21+의 getFirst() 대신 안전한 방식
        }

        return new NoticeResponseDto(
                notice.getId(),
                notice.getTitle(),
                notice.getUser().getNickname(),
                notice.getCreatedAt(),
                notice.getContent(),
                thumb
        );
    }
}
