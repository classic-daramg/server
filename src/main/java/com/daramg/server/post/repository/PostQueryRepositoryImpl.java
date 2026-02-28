package com.daramg.server.post.repository;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.util.PagingUtils;
import com.daramg.server.composer.domain.Continent;
import com.daramg.server.composer.domain.Era;
import com.daramg.server.post.domain.CurationPost;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.PostStatus;
import com.daramg.server.post.domain.QPost;
import com.daramg.server.post.domain.StoryPost;
import com.daramg.server.post.dto.StoryPostStatsDto;
import com.daramg.server.composer.domain.QComposer;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.daramg.server.post.domain.QPost.post;
import static com.daramg.server.post.domain.QPostScrap.postScrap;
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
    public List<CurationPost> getAllCurationPostsWithPaging(PageRequestDto pageRequest, List<Era> eras, List<Continent> continents) {
        BooleanBuilder whereClause = new BooleanBuilder()
                .and(curationPost._super.isBlocked.isFalse())
                .and(curationPost._super.postStatus.eq(PostStatus.PUBLISHED));
        if (eras != null && !eras.isEmpty()) {
            whereClause.and(curationPost.primaryComposer.era.in(eras));
        }
        if (continents != null && !continents.isEmpty()) {
            whereClause.and(curationPost.primaryComposer.continent.in(continents));
        }

        JPAQuery<CurationPost> query = queryFactory
                .selectFrom(curationPost)
                .leftJoin(curationPost._super.user, user).fetchJoin()
                .leftJoin(curationPost.primaryComposer).fetchJoin()
                .where(whereClause);

        return pagingUtils.applyCursorPagination(
                query,
                pageRequest,
                curationPost._super.createdAt,
                curationPost._super.id
        );
    }

    @Override
    public List<StoryPost> getAllStoryPostsWithPaging(PageRequestDto pageRequest) {
        return getAllPostsWithPaging(pageRequest, storyPost, storyPost._super);
    }

    @Override
    public List<Post> getUserPublishedPostsWithPaging(Long userId, PageRequestDto pageRequest) {
        JPAQuery<Post> query = queryFactory
                .selectFrom(post)
                .leftJoin(post.user, user).fetchJoin()
                .where(
                        post.user.id.eq(userId)
                                .and(post.postStatus.eq(PostStatus.PUBLISHED))
                                .and(post.isBlocked.isFalse())
                );

        return pagingUtils.applyCursorPagination(
                query,
                pageRequest,
                post.createdAt,
                post.id
        );
    }

    @Override
    public List<Post> getUserDraftPostsWithPaging(Long userId, PageRequestDto pageRequest) {
        JPAQuery<Post> query = queryFactory
                .selectFrom(post)
                .leftJoin(post.user, user).fetchJoin()
                .where(
                        post.user.id.eq(userId)
                                .and(post.postStatus.eq(PostStatus.DRAFT))
                                .and(post.isBlocked.isFalse())
                );

        return pagingUtils.applyCursorPagination(
                query,
                pageRequest,
                post.createdAt,
                post.id
        );
    }

    @Override
    public List<Post> getUserScrappedPostsWithPaging(Long userId, PageRequestDto pageRequest) {
        JPAQuery<Post> query = queryFactory
                .selectFrom(post)
                .innerJoin(postScrap).on(postScrap.post.id.eq(post.id))
                .leftJoin(post.user, user).fetchJoin()
                .where(
                        postScrap.user.id.eq(userId)
                                .and(post.postStatus.eq(PostStatus.PUBLISHED))
                                .and(post.isBlocked.isFalse())
                );

        return pagingUtils.applyCursorPagination(
                query,
                pageRequest,
                post.createdAt,
                post.id
        );
    }

    @Override
    public List<Post> getPostsByComposerIdWithPaging(Long composerId, PageRequestDto pageRequest) {
        // StoryPost와 CurationPost를 각각 조회
        List<StoryPost> storyPosts = queryFactory
                .selectFrom(storyPost)
                .leftJoin(storyPost._super.user, user).fetchJoin()
                .where(
                        storyPost.primaryComposer.id.eq(composerId)
                                .and(storyPost._super.isBlocked.isFalse())
                                .and(storyPost._super.postStatus.eq(PostStatus.PUBLISHED))
                )
                .orderBy(storyPost._super.createdAt.desc(), storyPost._super.id.desc())
                .fetch();

        QComposer additionalComposer = new QComposer("additionalComposer");

        List<CurationPost> curationPosts = queryFactory
                .selectDistinct(curationPost)
                .from(curationPost)
                .leftJoin(curationPost._super.user, user).fetchJoin()
                .leftJoin(curationPost.additionalComposers, additionalComposer)
                .where(
                        (curationPost.primaryComposer.id.eq(composerId)
                                .or(additionalComposer.id.eq(composerId)))
                                .and(curationPost._super.isBlocked.isFalse())
                                .and(curationPost._super.postStatus.eq(PostStatus.PUBLISHED))
                )
                .orderBy(curationPost._super.createdAt.desc(), curationPost._super.id.desc())
                .fetch();

        // 두 리스트를 합치고 정렬
        List<Post> allPosts = new ArrayList<>();
        allPosts.addAll(storyPosts);
        allPosts.addAll(curationPosts);
        
        // 생성일시 내림차순, ID 내림차순으로 정렬
        allPosts.sort((p1, p2) -> {
            int dateCompare = p2.getCreatedAt().compareTo(p1.getCreatedAt());
            if (dateCompare != 0) {
                return dateCompare;
            }
            return Long.compare(p2.getId(), p1.getId());
        });

        // 페이징 적용
        return pagingUtils.applyCursorPaginationToList(
                allPosts,
                pageRequest,
                Post::getCreatedAt,
                Post::getId
        );
    }

    @Override
    public Map<Long, StoryPostStatsDto> findStoryPostStatsByAllComposers() {
        List<Tuple> results = queryFactory
                .select(
                        storyPost.primaryComposer.id,
                        storyPost._super.id.count(),
                        storyPost._super.createdAt.max()
                )
                .from(storyPost)
                .where(
                        storyPost._super.isBlocked.isFalse()
                                .and(storyPost._super.postStatus.eq(PostStatus.PUBLISHED))
                )
                .groupBy(storyPost.primaryComposer.id)
                .fetch();

        return results.stream().collect(Collectors.toMap(
                tuple -> tuple.get(storyPost.primaryComposer.id),
                tuple -> new StoryPostStatsDto(
                        tuple.get(storyPost._super.id.count()),
                        tuple.get(storyPost._super.createdAt.max())
                )
        ));
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
