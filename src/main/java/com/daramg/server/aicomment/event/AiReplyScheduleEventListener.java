package com.daramg.server.aicomment.event;

import com.daramg.server.aicomment.application.AiCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiReplyScheduleEventListener {

    private final AiCommentService aiCommentService;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleAiReplyScheduleEvent(AiReplyScheduleEvent event) {
        try {
            aiCommentService.scheduleReplyForAiComment(event.aiCommentId(), event.postId());
        } catch (Exception e) {
            log.warn("AI 답글 잡 등록 실패 - aiCommentId={}, postId={}", event.aiCommentId(), event.postId(), e);
        }
    }
}
