package com.daramg.server.aicomment.presentation;

import com.daramg.server.aicomment.application.AiCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/ai-comments")
@RequiredArgsConstructor
public class AiCommentAdminController {

    private final AiCommentService aiCommentService;

    @PostMapping("/posts/{postId}/assign")
    @ResponseStatus(HttpStatus.CREATED)
    public void assignComposer(
            @PathVariable Long postId,
            @RequestParam Long composerId
    ) {
        aiCommentService.scheduleManually(postId, composerId);
    }

    @GetMapping("/settings")
    public AutoDetectSettingsResponse getSettings() {
        return new AutoDetectSettingsResponse(aiCommentService.isAutoDetectEnabled());
    }

    @PutMapping("/settings/auto-detect")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setAutoDetect(@RequestParam boolean enabled) {
        aiCommentService.setAutoDetectEnabled(enabled);
    }

    public record AutoDetectSettingsResponse(boolean autoDetectEnabled) {}
}
