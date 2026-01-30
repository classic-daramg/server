package com.daramg.server.notice.repository;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.notice.domain.Notice;

import java.util.List;

public interface NoticeQueryRepository {

    List<Notice> getPublished(PageRequestDto pageRequest);
}
