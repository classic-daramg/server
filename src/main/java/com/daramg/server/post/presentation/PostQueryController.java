package com.daramg.server.post.presentation;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.post.application.PostQueryService;
import com.daramg.server.post.dto.PostDetailResponse;
import com.daramg.server.post.dto.PostResponseDto;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostQueryController {

    private final PostQueryService postQueryService;

    @GetMapping("/free")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDto<PostResponseDto> getAllPublishedFreePosts(
            @ModelAttribute PageRequestDto request
    ){
        return postQueryService.getAllPublishedFreePosts(request);
    }

    @GetMapping("/curation")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDto<PostResponseDto> getAllPublishedCurationPosts(
            @ModelAttribute PageRequestDto request
    ){
        return postQueryService.getAllPublishedCurationPosts(request);
    }

    @GetMapping("/story")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDto<PostResponseDto> getAllPublishedStoryPosts(
            @ModelAttribute PageRequestDto request
    ){
        return postQueryService.getAllPublishedStoryPosts(request);
    }

    @GetMapping("/{userId}/published")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDto<PostResponseDto> getUserPublishedPosts(
            @PathVariable Long userId,
            @ModelAttribute PageRequestDto request,
            User user
    ){
        return postQueryService.getUserPublishedPosts(userId, request);
    }

    @GetMapping("/{userId}/drafts")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDto<PostResponseDto> getUserDraftPosts(
            @PathVariable Long userId,
            @ModelAttribute PageRequestDto request,
            User user
    ){
        return postQueryService.getUserDraftPosts(userId, request);
    }

    @GetMapping("/{userId}/scraps")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDto<PostResponseDto> getUserScrappedPosts(
            @PathVariable Long userId,
            @ModelAttribute PageRequestDto request,
            User user
    ){
        return postQueryService.getUserScrappedPosts(userId, request);
    }

    @GetMapping("/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public PostDetailResponse getPostById(
            @PathVariable Long postId
    ){
        return postQueryService.getPostById(postId);
    }

}
