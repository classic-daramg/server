package com.daramg.server.notice.repository;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.util.PagingUtils;
import com.daramg.server.notice.domain.Notice;
import com.daramg.server.notice.domain.QNotice;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.daramg.server.user.domain.QUser.user;

@RequiredArgsConstructor
@Repository
public class NoticeQueryRepositoryImpl implements NoticeQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final PagingUtils pagingUtils;

    @Override
    public List<Notice> getPublished(PageRequestDto pageRequest) {
        QNotice notice = QNotice.notice;

        JPAQuery<Notice> query = queryFactory
                .selectFrom(notice)
                .leftJoin(notice.user, user).fetchJoin();

        return pagingUtils.applyCursorPagination(
                query,
                pageRequest,
                notice.createdAt,
                notice.id
        );
    }
}
