package com.daramg.server.composer.presentation;

import com.daramg.server.composer.application.ComposerQueryService;
import com.daramg.server.composer.dto.ComposerResponseDto;
import com.daramg.server.composer.dto.ComposerWithPostsResponseDto;
import com.daramg.server.composer.domain.Continent;
import com.daramg.server.composer.domain.Era;
import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.post.application.PostQueryService;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/composers")
public class ComposerQueryController {

    private final ComposerQueryService composerQueryService;
    private final PostQueryService postQueryService;

    @GetMapping
    public ResponseEntity<List<ComposerResponseDto>> getComposers(
            @AuthenticationPrincipal User user, // 로그인 or 비로그인 유저
            @RequestParam(name = "eras", required = false) List<Era> eras,
            @RequestParam(name = "continents", required = false) List<Continent> continents
    ) {
        List<ComposerResponseDto> response = composerQueryService.getAllComposers(user, eras, continents);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{composerId}/posts")
    public ResponseEntity<ComposerWithPostsResponseDto> getComposerWithPosts(
            @PathVariable Long composerId,
            @AuthenticationPrincipal User user, // 로그인 or 비로그인 유저
            @ModelAttribute PageRequestDto request
    ) {
        ComposerWithPostsResponseDto response = postQueryService.getComposerWithPosts(composerId, request, user);
        return ResponseEntity.ok(response);
    }
}
