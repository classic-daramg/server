package com.daramg.server.post.application;

import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.common.util.PagingUtils;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostQueryRepository postQueryRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostScrapRepository postScrapRepository;
    private final PagingUtils pagingUtils;
    private final EntityUtils entityUtils;

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

    private PostResponseDto toPostResponseDto(Post post, User user) {
        Boolean isLiked = null;
        Boolean isScrapped = null;
        
        if (user != null) {
            isLiked = postLikeRepository.existsByPostIdAndUserId(post.getId(), user.getId());
            isScrapped = postScrapRepository.existsByPostIdAndUserId(post.getId(), user.getId());
        }
        
        return PostResponseDto.from(post, isLiked, isScrapped);
    }
}
