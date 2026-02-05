package com.daramg.server.post.domain;

import com.daramg.server.common.converter.JsonArrayConverter;
import com.daramg.server.common.domain.BaseEntity;
import com.daramg.server.post.domain.vo.PostUpdateVo;
import com.daramg.server.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "posts")
@SQLRestriction("is_deleted = false")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Post extends BaseEntity<Post> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Convert(converter = JsonArrayConverter.class)
    @Column(name = "images", columnDefinition = "JSON")
    private List<String> images = new ArrayList<>();

    @Column(name = "video_url")
    private String videoUrl;

    @Convert(converter = JsonArrayConverter.class)
    @Column(name = "hashtags", columnDefinition = "JSON")
    private List<String> hashtags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "post_status", nullable = false)
    private PostStatus postStatus;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    private int commentCount = 0;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = false;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected Post(@NonNull User user, @NonNull String title, @NonNull String content,
                   @Singular List<String> images, String videoUrl,
                   @Singular List<String> hashtags, @NonNull PostStatus postStatus) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.images = images;
        this.videoUrl = videoUrl;
        this.hashtags = hashtags;
        this.postStatus = postStatus;
    }

    public void update(PostUpdateVo vo){
        if (vo.getTitle() != null) {
            updateTitle(vo.getTitle());
        }
        if (vo.getContent() != null) {
            updateContent(vo.getContent());
        }
        if (vo.getPostStatus() != null) {
            updatePostStatus(vo.getPostStatus());
        }
        if (vo.getImages() != null) {
            updateImages(vo.getImages());
        }
        if (vo.getVideoUrl() != null) {
            updateVideoUrl(vo.getVideoUrl().isEmpty() ? null : vo.getVideoUrl());
        }
        if (vo.getHashtags() != null) {
            updateHashtags(vo.getHashtags());
        }
    }

    protected void updateTitle(String title) {
        this.title = title;
    }

    protected void updateContent(String content) {
        this.content = content;
    }

    protected void updateImages(List<String> images) {
        this.images = images;
    }

    protected void updatePostStatus(PostStatus postStatus) {
        this.postStatus = postStatus;
    }

    protected void updateVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    protected void updateHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public void incrementPostLike(){
        likeCount++;
    }

    public void decrementPostLike(){
        if (likeCount > 0){
            likeCount--;
        }
    }

    public void incrementCommentCount(){
        commentCount++;
    }

    public void softDelete() {
        if (this.isDeleted) return;

        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
