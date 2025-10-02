package com.daramg.server.domain.post.controller;

import com.daramg.server.domain.post.dto.PostRequest;
import com.daramg.server.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @PostMapping("/free")
    @ResponseStatus(HttpStatus.CREATED)
    public void createFreePost(@RequestParam Long userId, @RequestBody PostRequest.CreateFree request) {
        postService.create(request);
    }

    @PostMapping("/curation")
    @ResponseStatus(HttpStatus.CREATED)
    public void createCurationPost(@RequestParam Long userId, @RequestBody PostRequest.CreateCuration request) {
        postService.create(request);
    }

    @PostMapping("/story")
    @ResponseStatus(HttpStatus.CREATED)
    public void createStoryPost(@RequestParam Long userId, @RequestBody PostRequest.CreateStory request) {
        postService.create(request);
    }

}
