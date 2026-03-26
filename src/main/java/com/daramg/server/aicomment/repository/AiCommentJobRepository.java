package com.daramg.server.aicomment.repository;

import com.daramg.server.aicomment.domain.AiCommentJob;
import com.daramg.server.aicomment.domain.AiCommentJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AiCommentJobRepository extends JpaRepository<AiCommentJob, Long> {

    @Query("SELECT j FROM AiCommentJob j JOIN FETCH j.post JOIN FETCH j.composer WHERE j.status = :status AND j.scheduledAt <= :now")
    List<AiCommentJob> findPendingJobsDue(AiCommentJobStatus status, Instant now);
}
