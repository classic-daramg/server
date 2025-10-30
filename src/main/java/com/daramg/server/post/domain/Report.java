package com.daramg.server.post.domain;

import com.daramg.server.comment.domain.Comment;
import com.daramg.server.common.domain.BaseEntity;
import com.daramg.server.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Entity
@Getter
@Table(name = "reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity<Report> {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ReportType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_reason", nullable = false)
    private ReportReason reportReason;

    @Column(name = "report_content", columnDefinition = "TEXT")
    private String reportContent;

    @Column(name = "is_processed", nullable = false)
    private boolean isProcessed = false;

    @Builder
    public Report(@NonNull ReportType type, Post post, Comment comment,
                 @NonNull User reporter, @NonNull ReportReason reportReason,
                 String reportContent) {
        this.type = type;
        this.post = post;
        this.comment = comment;
        this.reporter = reporter;
        this.reportReason = reportReason;
        this.reportContent = reportContent;
    }

    public enum ReportType {
        POST, COMMENT
    }

    public enum ReportReason {
        SPAM("스팸/광고"),
        PROFANITY("부적절한 언어/욕설"),
        DEFAMATION("명예훼손/비방"),
        COPYRIGHT_VIOLATION("저작권 침해"),
        OFF_TOPIC("주제와 무관한 내용"),
        PRIVACY_VIOLATION("개인정보 유출"),
        OTHER("기타");

        private final String description;

        ReportReason(String description) {
            this.description = description;
        }
    }
}