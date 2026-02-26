package com.daramg.server.search.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@Table(name = "search_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "searched_at", nullable = false)
    private Instant searchedAt;

    @Column(name = "user_id")
    private Long userId;

    private SearchLog(String keyword, Long userId) {
        this.keyword = keyword;
        this.userId = userId;
    }

    @PrePersist
    protected void onCreate() {
        this.searchedAt = Instant.now();
    }

    public static SearchLog of(String keyword, Long userId) {
        return new SearchLog(keyword, userId);
    }
}
