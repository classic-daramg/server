package com.daramg.server.post.application;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.common.util.PagingUtils;
import com.daramg.server.post.domain.CurationPost;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.StoryPost;
import com.daramg.server.post.dto.CurationPostsResponseDto;
import com.daramg.server.post.dto.FreePostsResponseDto;
import com.daramg.server.post.dto.StoryPostsResponseDto;
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

    public PageResponseDto<FreePostsResponseDto> getAllPublishedFreePosts(PageRequestDto pageRequest){
        List<FreePost> posts = postQueryRepository.getAllFreePostsWithPaging(pageRequest);

        return pagingUtils.createPageResponse(
                posts,
                pageRequest.getValidatedSize(),
                FreePostsResponseDto::from,
                Post::getCreatedAt,
                Post::getId
        );
    }

    public PageResponseDto<CurationPostsResponseDto> getAllPublishedCurationPosts(PageRequestDto pageRequest){
        List<CurationPost> posts = postQueryRepository.getAllCurationPostsWithPaging(pageRequest);

        return pagingUtils.createPageResponse(
                posts,
                pageRequest.getValidatedSize(),
                CurationPostsResponseDto::from,
                Post::getCreatedAt,
                Post::getId
        );
    }

    public PageResponseDto<StoryPostsResponseDto> getAllPublishedStoryPosts(PageRequestDto pageRequest){
        List<StoryPost> posts = postQueryRepository.getAllStoryPostsWithPaging(pageRequest);

        return pagingUtils.createPageResponse(
                posts,
                pageRequest.getValidatedSize(),
                StoryPostsResponseDto::from,
                Post::getCreatedAt,
                Post::getId
        );
    }
}
