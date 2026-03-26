package com.daramg.server.aicomment.scheduler;

import com.daramg.server.aicomment.application.AiCommentService;
import com.daramg.server.aicomment.domain.AiCommentJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiCommentJobSchedulerTest {

    @InjectMocks
    private AiCommentJobScheduler scheduler;

    @Mock
    private AiCommentService aiCommentService;

    @Test
    @DisplayName("실행 대상 잡이 없으면 processJob이 호출되지 않는다")
    void 실행_대상_잡이_없으면_아무것도_하지_않는다() {
        // given
        when(aiCommentService.findPendingJobsDue()).thenReturn(List.of());

        // when
        scheduler.processAiCommentJobs();

        // then
        verify(aiCommentService, never()).processJob(any());
    }

    @Test
    @DisplayName("실행 대상 잡이 있으면 각 잡마다 processJob이 호출된다")
    void 실행_대상_잡이_있으면_processJob이_호출된다() {
        // given
        AiCommentJob job1 = mock(AiCommentJob.class);
        AiCommentJob job2 = mock(AiCommentJob.class);
        when(aiCommentService.findPendingJobsDue()).thenReturn(List.of(job1, job2));

        // when
        scheduler.processAiCommentJobs();

        // then
        verify(aiCommentService).processJob(job1);
        verify(aiCommentService).processJob(job2);
    }

    @Test
    @DisplayName("한 잡 처리 중 예외가 발생해도 나머지 잡은 계속 처리된다")
    void 잡_처리_중_예외가_발생해도_나머지_잡은_처리된다() {
        // given
        AiCommentJob job1 = mock(AiCommentJob.class);
        AiCommentJob job2 = mock(AiCommentJob.class);
        when(aiCommentService.findPendingJobsDue()).thenReturn(List.of(job1, job2));
        doThrow(new RuntimeException("처리 실패")).when(aiCommentService).processJob(job1);

        // when
        scheduler.processAiCommentJobs();

        // then
        verify(aiCommentService).processJob(job1);
        verify(aiCommentService).processJob(job2);
    }
}
