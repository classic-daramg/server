package com.daramg.server.notice.application;

import com.daramg.server.notice.domain.Notice;
import com.daramg.server.notice.dto.NoticeCreateDto;
import com.daramg.server.notice.repository.NoticeRepository;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@ToString
@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public void create(NoticeCreateDto dto, User user){

        Notice notice = dto.toEntity();
        log.info(notice.toString());

        Notice saved = noticeRepository.save(notice);
        log.info(saved.toString());
    }
}
