package com.daramg.server.post.repository;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.util.PagingUtils;
import com.daramg.server.post.domain.CurationPost;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.PostStatus;
import com.daramg.server.post.domain.StoryPost;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.daramg.server.post.domain.QCurationPost.curationPost;
import static com.daramg.server.post.domain.QFreePost.freePost;
import static com.daramg.server.post.domain.QStoryPost.storyPost;
import static com.daramg.server.user.domain.QUser.user;

@Repository
@RequiredArgsConstructor
public class PostQueryRepositoryImpl implements PostQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final PagingUtils pagingUtils;

    @Override
    public List<FreePost> getAllFreePostsWithPaging(PageRequestDto pageRequest) {
        JPAQuery<FreePost> query = queryFactory
                .selectFrom(freePost)
                .leftJoin(freePost.user, user).fetchJoin()
                .where(
                        freePost.isBlocked.isFalse()
                                .and(freePost.postStatus.eq(PostStatus.PUBLISHED))
                );

        return pagingUtils.applyCursorPagination(
                query,
                pageRequest,
                freePost.createdAt,
                freePost.id
        );
    }

    @Override
    public List<CurationPost> getAllCurationPostsWithPaging(PageRequestDto pageRequest) {
        JPAQuery<CurationPost> query = queryFactory
                .selectFrom(curationPost)
                .leftJoin(curationPost.user, user).fetchJoin()
                .where(
                        curationPost.isBlocked.isFalse()
                                .and(curationPost.postStatus.eq(PostStatus.PUBLISHED))
                );

        return pagingUtils.applyCursorPagination(
                query,
                pageRequest,
                curationPost.createdAt,
                curationPost.id
        );
    }

    @Override
    public List<StoryPost> getAllStoryPostsWithPaging(PageRequestDto pageRequest) {
        JPAQuery<StoryPost> query = queryFactory
                .selectFrom(storyPost)
                .leftJoin(storyPost.user, user).fetchJoin()
                .where(
                        storyPost.isBlocked.isFalse()
                                .and(storyPost.postStatus.eq(PostStatus.PUBLISHED))
                );

        return pagingUtils.applyCursorPagination(
                query,
                pageRequest,
                storyPost.createdAt,
                storyPost.id
        );
    }
}
