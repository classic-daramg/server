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
import com.daramg.server.post.repository.PostQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostQueryRepository postQueryRepository;
    private final PagingUtils pagingUtils;
    private final EntityUtils entityUtils;

    public PageResponseDto<PostResponseDto> getAllPublishedFreePosts(PageRequestDto pageRequest){
        List<FreePost> posts = postQueryRepository.getAllFreePostsWithPaging(pageRequest);

        return pagingUtils.createPageResponse(
                posts,
                pageRequest.getValidatedSize(),
                PostResponseDto::from,
                Post::getCreatedAt,
                Post::getId
        );
    }

    public PageResponseDto<PostResponseDto> getAllPublishedCurationPosts(PageRequestDto pageRequest){
        List<CurationPost> posts = postQueryRepository.getAllCurationPostsWithPaging(pageRequest);

        return pagingUtils.createPageResponse(
                posts,
                pageRequest.getValidatedSize(),
                PostResponseDto::from,
                Post::getCreatedAt,
                Post::getId
        );
    }

    public PageResponseDto<PostResponseDto> getAllPublishedStoryPosts(PageRequestDto pageRequest){
        List<StoryPost> posts = postQueryRepository.getAllStoryPostsWithPaging(pageRequest);

        return pagingUtils.createPageResponse(
                posts,
                pageRequest.getValidatedSize(),
                PostResponseDto::from,
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

    public PostDetailResponse getPostById(Long postId) {
        Post post = entityUtils.getEntity(postId, Post.class);
        return PostDetailResponse.from(post);
    }
}
