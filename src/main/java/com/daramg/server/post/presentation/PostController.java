package com.daramg.server.post.presentation;

import com.daramg.server.post.dto.PostCreateDto;
import com.daramg.server.post.dto.PostUpdateDto;
import com.daramg.server.post.application.PostService;
import com.daramg.server.user.domain.User;
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
    public void createFreePost(@Valid @RequestBody PostCreateDto.CreateFree request, User user) {
        postService.createFree(request, user);
    }

    @PostMapping("/curation")
    @ResponseStatus(HttpStatus.CREATED)
    public void createCurationPost(@Valid @RequestBody PostCreateDto.CreateCuration request, User user) {
        postService.createCuration(request, user);
    }

    @PostMapping("/story")
    @ResponseStatus(HttpStatus.CREATED)
    public void createStoryPost(@Valid @RequestBody PostCreateDto.CreateStory request, User user) {
        postService.createStory(request, user);
    }

    @PatchMapping("/free/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateFreePost(@PathVariable Long postId,
                               @Valid @RequestBody PostUpdateDto.UpdateFree freeUpdateRequest,
                               User user) {
        postService.updateFree(postId, freeUpdateRequest, user);
    }

    @PatchMapping("/story/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateStoryPost(@PathVariable Long postId,
                                @Valid @RequestBody PostUpdateDto.UpdateStory storyUpdateRequest,
                                User user) {
        postService.updateStory(postId, storyUpdateRequest, user);
    }

    @PatchMapping("/curation/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateCurationPost(@PathVariable Long postId,
                                   @Valid @RequestBody PostUpdateDto.UpdateCuration curationUpdateRequest,
                                   User user) {
        postService.updateCuration(postId, curationUpdateRequest, user);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long postId, User user) {
        postService.delete(postId, user);
    }
}
