package com.daramg.server.aicomment.scheduler;

import com.daramg.server.aicomment.application.AiCommentService;
import com.daramg.server.aicomment.domain.AiCommentJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiCommentJobScheduler {

    private final AiCommentService aiCommentService;

    @Scheduled(fixedDelay = 60_000) // 1분마다 실행
    public void processAiCommentJobs() {
        List<AiCommentJob> jobs = aiCommentService.findPendingJobsDue();
        if (jobs.isEmpty()) {
            return;
        }

        log.info("AI 댓글 잡 처리 시작 - 대상 {}건", jobs.size());
        for (AiCommentJob job : jobs) {
            try {
                aiCommentService.processJob(job);
            } catch (Exception e) {
                log.error("AI 댓글 잡 처리 중 예외 - jobId={}", job.getId(), e);
            }
        }
    }
}
