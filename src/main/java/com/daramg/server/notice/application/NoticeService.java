package com.daramg.server.notice.application;

import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.notice.domain.Notice;
import com.daramg.server.notice.domain.vo.NoticeUpdateVo;
import com.daramg.server.notice.dto.NoticeCreateDto;
import com.daramg.server.notice.dto.NoticeUpdateDto;
import com.daramg.server.notice.repository.NoticeRepository;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.utils.PostUserValidator;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@ToString
@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final EntityUtils entityUtils;

    @Transactional
    public void create(NoticeCreateDto dto, User user) {

        Notice notice = dto.toEntity();
        log.info(notice.toString());

        Notice saved = noticeRepository.save(notice);
        log.info(saved.toString());
    }

    @Transactional
    public void updateNotice(Long noticeId, NoticeUpdateDto dto, User user) {
        Notice notice = entityUtils.getEntity(noticeId, Notice.class);
        NoticeUpdateVo vo = toUpdateVo(dto);
        notice.update(vo);
    }

    @Transactional
    public void delete(Long noticeId, User user){
        Notice notice = entityUtils.getEntity(noticeId, Notice.class);

        noticeRepository.deleteById(noticeId);
    }

    private NoticeUpdateVo toUpdateVo(NoticeUpdateDto dto){
        return new NoticeUpdateVo(
                dto.getTitle(), dto.getContent(),
                dto.getImages(), dto.getVideoUrl()
        );
    }
}
