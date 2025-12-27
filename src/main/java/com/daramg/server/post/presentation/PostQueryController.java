package com.daramg.server.post.presentation;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.post.application.PostQueryService;
import com.daramg.server.post.dto.CurationPostsResponseDto;
import com.daramg.server.post.dto.FreePostsResponseDto;
import com.daramg.server.post.dto.StoryPostsResponseDto;
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
    public PageResponseDto<FreePostsResponseDto> getAllPublishedFreePosts(
            @ModelAttribute PageRequestDto request
    ){
        return postQueryService.getAllPublishedFreePosts(request);
    }

    @GetMapping("/curation")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDto<CurationPostsResponseDto> getAllPublishedCurationPosts(
            @ModelAttribute PageRequestDto request
    ){
        return postQueryService.getAllPublishedCurationPosts(request);
    }

    @GetMapping("/story")
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDto<StoryPostsResponseDto> getAllPublishedStoryPosts(
            @ModelAttribute PageRequestDto request
    ){
        return postQueryService.getAllPublishedStoryPosts(request);
    }

}
