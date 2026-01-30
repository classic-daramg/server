package com.daramg.server.post.application;

import com.daramg.server.comment.domain.Comment;
import com.daramg.server.comment.domain.CommentLike;
import com.daramg.server.comment.dto.CommentResponseDto;
import com.daramg.server.comment.repository.CommentLikeRepository;
import com.daramg.server.comment.repository.CommentRepository;
import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.common.domain.BaseEntity;
import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.common.util.PagingUtils;
import com.daramg.server.composer.domain.Composer;
import com.daramg.server.composer.domain.Continent;
import com.daramg.server.composer.domain.Era;
import com.daramg.server.composer.dto.ComposerResponseDto;
import com.daramg.server.composer.dto.ComposerWithPostsResponseDto;
import com.daramg.server.composer.repository.ComposerLikeRepository;
import com.daramg.server.post.domain.CurationPost;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.StoryPost;
import com.daramg.server.post.dto.PostDetailResponse;
import com.daramg.server.post.dto.PostResponseDto;
import com.daramg.server.post.repository.PostLikeRepository;
import com.daramg.server.post.repository.PostQueryRepository;
import com.daramg.server.post.repository.PostScrapRepository;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostQueryRepository postQueryRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostScrapRepository postScrapRepository;
    private final PagingUtils pagingUtils;
    private final EntityUtils entityUtils;
    private final ComposerLikeRepository composerLikeRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    public PageResponseDto<PostResponseDto> getAllPublishedFreePosts(PageRequestDto pageRequest, User user){
        List<FreePost> posts = postQueryRepository.getAllFreePostsWithPaging(pageRequest);

        return pagingUtils.createPageResponse(
                posts,
                pageRequest.getValidatedSize(),
                post -> toPostResponseDto(post, user),
                Post::getCreatedAt,
                Post::getId
        );
    }

    public PageResponseDto<PostResponseDto> getAllPublishedCurationPosts(PageRequestDto pageRequest, User user,
                                                                        List<Era> eras, List<Continent> continents) {
        List<CurationPost> posts = postQueryRepository.getAllCurationPostsWithPaging(pageRequest, eras, continents);

        // N+1 방지: 좋아요/스크랩 여부 배치 조회
        List<Post> postsAsPost = new ArrayList<>(posts);
        Set<Long> likedPostIds = getLikedPostIds(postsAsPost, user);
        Set<Long> scrappedPostIds = getScrappedPostIds(postsAsPost, user);

        return pagingUtils.createPageResponse(
                posts,
                pageRequest.getValidatedSize(),
                post -> toPostResponseDto(post, user, likedPostIds, scrappedPostIds),
                Post::getCreatedAt,
                Post::getId
        );
    }

    public PageResponseDto<PostResponseDto> getAllPublishedStoryPosts(PageRequestDto pageRequest, User user){
        List<StoryPost> posts = postQueryRepository.getAllStoryPostsWithPaging(pageRequest);

        return pagingUtils.createPageResponse(
                posts,
                pageRequest.getValidatedSize(),
                post -> toPostResponseDto(post, user),
                Post::getCreatedAt,
                Post::getId
        );
    }

    public PageResponseDto<PostResponseDto> getUserPublishedPosts(Long userId, PageRequestDto pageRequest){
        List<Post> posts = postQueryRepository.getUserPublishedPostsWithPaging(userId, pageRequest);

        return pagingUtils.createPageResponse(
                posts,
                pageRequest.getValidatedSize(),
                PostResponseDto::from,
                Post::getCreatedAt,
                Post::getId
        );
    }

    public PageResponseDto<PostResponseDto> getUserDraftPosts(Long userId, PageRequestDto pageRequest){
        List<Post> posts = postQueryRepository.getUserDraftPostsWithPaging(userId, pageRequest);

        return pagingUtils.createPageResponse(
                posts,
                pageRequest.getValidatedSize(),
                PostResponseDto::from,
                Post::getCreatedAt,
                Post::getId
        );
    }

    public PageResponseDto<PostResponseDto> getUserScrappedPosts(Long userId, PageRequestDto pageRequest){
        List<Post> posts = postQueryRepository.getUserScrappedPostsWithPaging(userId, pageRequest);

        return pagingUtils.createPageResponse(
                posts,
                pageRequest.getValidatedSize(),
                PostResponseDto::from,
                Post::getCreatedAt,
                Post::getId
        );
    }

    public PostDetailResponse getPostById(Long postId, User user) {
        Post post = entityUtils.getEntity(postId, Post.class);
        Boolean isLiked = user != null ? postLikeRepository.existsByPostIdAndUserId(postId, user.getId()) : null;
        Boolean isScrapped = user != null ? postScrapRepository.existsByPostIdAndUserId(postId, user.getId()) : null;
        List<Comment> comments = commentRepository.findByPostIdAndIsBlockedFalseOrderByCreatedAtAsc(postId);

        List<CommentResponseDto> commentDtos = mapCommentsWithChildren(comments, user);

        return PostDetailResponse.from(post, isLiked, isScrapped, commentDtos);
    }

    public ComposerWithPostsResponseDto getComposerWithPosts(Long composerId, PageRequestDto pageRequest, User user) {
        Composer composer = entityUtils.getEntity(composerId, Composer.class);
        boolean isLiked = user != null && composerLikeRepository.existsByComposerIdAndUserId(composerId, user.getId());
        ComposerResponseDto composerDto = ComposerResponseDto.from(composer, isLiked);

        List<Post> posts = postQueryRepository.getPostsByComposerIdWithPaging(composerId, pageRequest);
        
        // N+1 문제 해결: 배치로 좋아요/스크랩 여부 조회
        Set<Long> likedPostIds = getLikedPostIds(posts, user);
        Set<Long> scrappedPostIds = getScrappedPostIds(posts, user);
        
        PageResponseDto<PostResponseDto> postsPage = pagingUtils.createPageResponse(
                posts,
                pageRequest.getValidatedSize(),
                post -> toPostResponseDto(post, user, likedPostIds, scrappedPostIds),
                Post::getCreatedAt,
                Post::getId
        );

        return new ComposerWithPostsResponseDto(composerDto, postsPage);
    }

    private PostResponseDto toPostResponseDto(Post post, User user) {
        Boolean isLiked = null;
        Boolean isScrapped = null;
        
        if (user != null) {
            isLiked = postLikeRepository.existsByPostIdAndUserId(post.getId(), user.getId());
            isScrapped = postScrapRepository.existsByPostIdAndUserId(post.getId(), user.getId());
        }
        
        return PostResponseDto.from(post, isLiked, isScrapped);
    }

    private PostResponseDto toPostResponseDto(Post post, User user, Set<Long> likedPostIds, Set<Long> scrappedPostIds) {
        Boolean isLiked = null;
        Boolean isScrapped = null;
        
        if (user != null) {
            isLiked = likedPostIds.contains(post.getId());
            isScrapped = scrappedPostIds.contains(post.getId());
        }
        
        return PostResponseDto.from(post, isLiked, isScrapped);
    }

    private Set<Long> getLikedPostIds(List<Post> posts, User user) {
        if (user == null || posts.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        return postLikeRepository.findPostIdsByPostIdsAndUserId(postIds, user.getId());
    }

    private Set<Long> getScrappedPostIds(List<Post> posts, User user) {
        if (user == null || posts.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        return postScrapRepository.findPostIdsByPostIdsAndUserId(postIds, user.getId());
    }

    private List<CommentResponseDto> mapCommentsWithChildren(List<Comment> allComments, User user) {
        if (allComments.isEmpty()) {
            return List.of();
        }

        // 부모/자식 댓글 분리 (작성시간 오름차순 정렬)
        List<Comment> parentComments = allComments.stream()
                .filter(comment -> comment.getParentComment() == null)
                .sorted(Comparator.comparing((Comment c) -> c.getCreatedAt()).thenComparingLong(BaseEntity::getId))
                .toList();

        Map<Long, List<Comment>> childrenByParentId = allComments.stream()
                .filter(comment -> comment.getParentComment() != null)
                .collect(Collectors.groupingBy(comment -> comment.getParentComment().getId()));

        // 현재 로그인 유저가 좋아요한 댓글 ID 모음
        Set<Long> likedCommentIds = getLikedCommentIds(allComments, user);

        return parentComments.stream()
                .map(parent -> {
                    List<Comment> children = childrenByParentId.getOrDefault(parent.getId(), List.of())
                            .stream()
                            .sorted(Comparator.comparing((Comment c) -> c.getCreatedAt()).thenComparingLong(BaseEntity::getId))
                            .toList();

                    List<CommentResponseDto.ChildCommentResponseDto> childDtos = children.stream()
                            .map(child -> CommentResponseDto.ChildCommentResponseDto.from(
                                    child,
                                    user != null && likedCommentIds.contains(child.getId())
                            ))
                            .toList();

                    Boolean isParentLiked = user != null ? likedCommentIds.contains(parent.getId()) : null;

                    return CommentResponseDto.from(parent, isParentLiked, childDtos);
                })
                .toList();
    }

    private Set<Long> getLikedCommentIds(List<Comment> comments, User user) {
        if (user == null || comments.isEmpty()) {
            return Collections.emptySet();
        }

        List<Long> commentIds = comments.stream()
                .map(Comment::getId)
                .toList();

        List<CommentLike> likes = commentLikeRepository.findByCommentIdInAndUserId(commentIds, user.getId());

        return likes.stream()
                .map(like -> like.getComment().getId())
                .collect(Collectors.toSet());
    }
}
