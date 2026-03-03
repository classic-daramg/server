package com.daramg.server.composer.presentation;

import com.daramg.server.composer.application.ComposerService;
import com.daramg.server.composer.dto.ComposerCreateDto;
import com.daramg.server.composer.dto.ComposerUpdateDto;
import com.daramg.server.composer.dto.ComposerLikeResponseDto;
import com.daramg.server.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/composers")
public class ComposerController {

    private final ComposerService composerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createComposer(@Valid @RequestBody ComposerCreateDto dto, User user) {
        composerService.createComposer(dto, user);
    }

    @PutMapping("/{composerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateComposer(@PathVariable Long composerId, @Valid @RequestBody ComposerUpdateDto dto, User user) {
        composerService.updateComposer(composerId, dto, user);
    }

    @DeleteMapping("/{composerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComposer(@PathVariable Long composerId, User user) {
        composerService.deleteComposer(composerId, user);
    }

    @PostMapping("/{composerId}/like")
    public ResponseEntity<ComposerLikeResponseDto> toggleComposerLike(@PathVariable Long composerId, User user) {
        ComposerLikeResponseDto response = composerService.toggleLike(composerId, user);
        return ResponseEntity.ok(response);
    }
}
