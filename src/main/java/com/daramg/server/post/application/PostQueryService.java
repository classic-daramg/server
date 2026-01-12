package com.daramg.server.post.application;

import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.common.util.PagingUtils;
import com.daramg.server.composer.domain.Composer;
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

import java.util.List;
import java.util.Set;

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

    public PageResponseDto<PostResponseDto> getAllPublishedCurationPosts(PageRequestDto pageRequest, User user){
        List<CurationPost> posts = postQueryRepository.getAllCurationPostsWithPaging(pageRequest);

        return pagingUtils.createPageResponse(
                posts,
                pageRequest.getValidatedSize(),
                post -> toPostResponseDto(post, user),
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
        return PostDetailResponse.from(post, isLiked, isScrapped);
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
            return java.util.Collections.emptySet();
        }
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        return postLikeRepository.findPostIdsByPostIdsAndUserId(postIds, user.getId());
    }

    private Set<Long> getScrappedPostIds(List<Post> posts, User user) {
        if (user == null || posts.isEmpty()) {
            return java.util.Collections.emptySet();
        }
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        return postScrapRepository.findPostIdsByPostIdsAndUserId(postIds, user.getId());
    }
}
