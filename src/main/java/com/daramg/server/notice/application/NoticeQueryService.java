package com.daramg.server.notice.application;

import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.common.util.PagingUtils;
import com.daramg.server.notice.domain.Notice;
import com.daramg.server.notice.dto.NoticeDetailResponse;
import com.daramg.server.notice.dto.NoticeResponseDto;
import com.daramg.server.notice.repository.NoticeQueryRepository;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class NoticeQueryService {

    private final NoticeQueryRepository noticeQueryRepository;
    private final PagingUtils pagingUtils;
    private final EntityUtils entityUtils;

    public PageResponseDto<NoticeResponseDto> getAllPublishedNotices(PageRequestDto pageRequest, User user) {
        List<Notice> notices = noticeQueryRepository.getPublished(pageRequest);

        return pagingUtils.createPageResponse(
                notices,
                pageRequest.getValidatedSize(),
                notice -> toNoticeResponseDto(notice, user),
                Notice::getCreatedAt,
                Notice::getId
        );
    }

    public NoticeDetailResponse getNoticeDetail(Long noticeId) {
        Notice notice = entityUtils.getEntity(noticeId, Notice.class);
        return NoticeDetailResponse.from(notice);
    }

    private NoticeResponseDto toNoticeResponseDto(Notice notice, User user) {
        return NoticeResponseDto.from(notice);
    }
}
