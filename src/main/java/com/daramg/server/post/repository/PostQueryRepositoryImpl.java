package com.daramg.server.post.repository;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.util.PagingUtils;
import com.daramg.server.post.domain.CurationPost;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.PostStatus;
import com.daramg.server.post.domain.QPost;
import com.daramg.server.post.domain.StoryPost;
import com.querydsl.core.types.dsl.EntityPathBase;
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
        return getAllPostsWithPaging(pageRequest, freePost, freePost._super);
    }

    @Override
    public List<CurationPost> getAllCurationPostsWithPaging(PageRequestDto pageRequest) {
        return getAllPostsWithPaging(pageRequest, curationPost, curationPost._super);
    }

    @Override
    public List<StoryPost> getAllStoryPostsWithPaging(PageRequestDto pageRequest) {
        return getAllPostsWithPaging(pageRequest, storyPost, storyPost._super);
    }

    private <T extends Post> List<T> getAllPostsWithPaging(
            PageRequestDto pageRequest,
            EntityPathBase<T> qEntity,
            QPost qPost
    ) {
        JPAQuery<T> query = queryFactory
                .selectFrom(qEntity)
                .leftJoin(qPost.user, user).fetchJoin()
                .where(
                        qPost.isBlocked.isFalse()
                                .and(qPost.postStatus.eq(PostStatus.PUBLISHED))
                );

        return pagingUtils.applyCursorPagination(
                query,
                pageRequest,
                qPost.createdAt,
                qPost.id
        );
    }
}
