package com.daramg.server.notice.dto;

import com.daramg.server.notice.domain.Notice;
import com.daramg.server.post.domain.*;

import java.time.LocalDateTime;
import java.util.List;

public record NoticeDetailResponse(
        Long id,
        String writerNickname,
        String writerProfileImage,
        String title,
        String content,
        List<String> images,
        String videoUrl,
        LocalDateTime createdAt
) {

    public static NoticeDetailResponse from(Notice notice) {
        return new NoticeDetailResponse(
                notice.getId(),
                notice.getUser().getNickname(),
                notice.getUser().getProfileImage(),
                notice.getTitle(),
                notice.getContent(),
                notice.getImages(),
                notice.getVideoUrl(),
                notice.getCreatedAt()
        );
    }
}
