package com.daramg.server.composer.presentation;

import com.daramg.server.composer.application.ComposerService;
import com.daramg.server.composer.dto.ComposerLikeResponseDto;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/composers")
public class ComposerController {

    private final ComposerService composerService;

    @PostMapping("/{composerId}/like")
    public ResponseEntity<ComposerLikeResponseDto> toggleComposerLike(@PathVariable Long composerId, User user) {
        ComposerLikeResponseDto response = composerService.toggleLike(composerId, user);
        return ResponseEntity.ok(response);
    }
}
