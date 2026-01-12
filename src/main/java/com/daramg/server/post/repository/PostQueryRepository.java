package com.daramg.server.post.repository;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.post.domain.CurationPost;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.StoryPost;

import java.util.List;

public interface PostQueryRepository {
    List<FreePost> getAllFreePostsWithPaging(PageRequestDto pageRequest);
    List<CurationPost> getAllCurationPostsWithPaging(PageRequestDto pageRequest);
    List<StoryPost> getAllStoryPostsWithPaging(PageRequestDto pageRequest);
    List<Post> getUserPublishedPostsWithPaging(Long userId, PageRequestDto pageRequest);
    List<Post> getUserDraftPostsWithPaging(Long userId, PageRequestDto pageRequest);
    List<Post> getUserScrappedPostsWithPaging(Long userId, PageRequestDto pageRequest);
    List<Post> getPostsByComposerIdWithPaging(Long composerId, PageRequestDto pageRequest);
}
