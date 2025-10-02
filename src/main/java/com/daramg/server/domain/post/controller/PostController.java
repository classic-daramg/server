package com.daramg.server.domain.post.controller;

import com.daramg.server.domain.post.dto.PostRequest;
import com.daramg.server.domain.post.service.PostService;
import com.daramg.server.domain.user.User;
import jakarta.validation.Valid;
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
    public void createFreePost(@Valid @RequestBody PostRequest.CreateFree request, User user) {
        postService.create(request, user);
    }

    @PostMapping("/curation")
    @ResponseStatus(HttpStatus.CREATED)
    public void createCurationPost(@Valid @RequestBody PostRequest.CreateCuration request, User user) {
        postService.create(request, user);
    }

    @PostMapping("/story")
    @ResponseStatus(HttpStatus.CREATED)
    public void createStoryPost(@Valid @RequestBody PostRequest.CreateStory request, User user) {
        postService.create(request, user);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@RequestParam Long postId, User user) {
        postService.delete(postId, user);
    }

}
