package com.daramg.server.notice.presentation;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.notice.application.NoticeQueryService;
import com.daramg.server.notice.dto.NoticeDetailResponse;
import com.daramg.server.notice.dto.NoticeResponseDto;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeQueryController {

    private final NoticeQueryService noticeQueryService;

    @GetMapping
    public PageResponseDto<NoticeResponseDto> getAllNotices(
            PageRequestDto pageRequest,
            User user
    ) {
        return noticeQueryService.getAllPublishedNotices(pageRequest, user);
    }

    @GetMapping("/{noticeId}")
    public NoticeDetailResponse getNoticeDetail(
            @PathVariable Long noticeId
    ) {
        return noticeQueryService.getNoticeDetail(noticeId);
    }
}
