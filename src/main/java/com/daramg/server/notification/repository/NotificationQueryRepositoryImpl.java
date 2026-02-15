package com.daramg.server.notification.repository;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.util.PagingUtils;
import com.daramg.server.notification.domain.Notification;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.daramg.server.notification.domain.QNotification.notification;
import static com.daramg.server.post.domain.QPost.post;
import static com.daramg.server.user.domain.QUser.user;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepositoryImpl implements NotificationQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final PagingUtils pagingUtils;

    @Override
    public List<Notification> getNotificationsWithPaging(Long receiverId, PageRequestDto pageRequest) {
        JPAQuery<Notification> query = queryFactory
                .selectFrom(notification)
                .leftJoin(notification.sender, user).fetchJoin()
                .leftJoin(notification.post, post).fetchJoin()
                .where(
                        notification.receiver.id.eq(receiverId),
                        notification.createdAt.goe(LocalDateTime.now().minusDays(30))
                );

        return pagingUtils.applyCursorPagination(
                query,
                pageRequest,
                notification.createdAt,
                notification.id
        );
    }
}
