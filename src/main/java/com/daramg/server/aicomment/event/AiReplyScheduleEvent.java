package com.daramg.server.aicomment.event;

public record AiReplyScheduleEvent(
        Long aiCommentId,
        Long postId
) {
}
