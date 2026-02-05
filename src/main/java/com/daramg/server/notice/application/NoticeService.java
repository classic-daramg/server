package com.daramg.server.notice.application;

import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.notice.domain.Notice;
import com.daramg.server.notice.domain.vo.NoticeCreateVo;
import com.daramg.server.notice.domain.vo.NoticeUpdateVo;
import com.daramg.server.notice.dto.NoticeCreateDto;
import com.daramg.server.notice.dto.NoticeUpdateDto;
import com.daramg.server.notice.repository.NoticeRepository;
import com.daramg.server.notice.utils.NoticeUserValidator;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final EntityUtils entityUtils;

    @Transactional
    public void create(NoticeCreateDto dto, User user) {

        NoticeCreateVo vo = new NoticeCreateVo(
                user,
                dto.getTitle(),
                dto.getContent(),
                dto.getImages()
        );

        Notice notice = Notice.from(vo);
        noticeRepository.save(notice);
    }

    @Transactional
    public void updateNotice(Long noticeId, NoticeUpdateDto dto, User user) {
        Notice notice = entityUtils.getEntity(noticeId, Notice.class);
        NoticeUserValidator.check(notice, user);

        NoticeUpdateVo vo = toUpdateVo(dto);
        notice.update(vo);
    }

    @Transactional
    public void delete(Long noticeId, User user) {
        Notice notice = entityUtils.getEntity(noticeId, Notice.class);
        NoticeUserValidator.check(notice, user);
        notice.softDelete();
    }

    private NoticeUpdateVo toUpdateVo(NoticeUpdateDto dto) {
        return new NoticeUpdateVo(
                dto.getTitle(),
                dto.getContent(),
                dto.getImages()
        );
    }
}
