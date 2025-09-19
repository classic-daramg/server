package com.daramg.server.domain.post;

import com.daramg.server.common.domain.BaseEntity;
import com.daramg.server.domain.composer.ComposerPost;
import com.daramg.server.domain.composer.Composer;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity<Post> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "composer_id")
    private Composer composer;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PostType type;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ElementCollection
    @CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    @Column(name = "video_url", nullable = true)
    private String videoUrl;

    @ElementCollection
    @CollectionTable(name = "post_hashtags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "hashtag")
    private List<String> hashtags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PostStatus status;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "scrap_count", nullable = false)
    private int scrapCount = 0;

    @Column(name = "comment_count", nullable = false)
    private int commentCount = 0;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = false;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComposerPost> composerPosts = new ArrayList<>();

    @Builder
    public Post(Composer composer, @NonNull PostType type, @NonNull String title, 
               @NonNull String content, List<String> images, String videoUrl,
               List<String> hashtags, @NonNull PostStatus status) {
        this.composer = composer;
        this.type = type;
        this.title = title;
        this.content = content;
        this.images = images != null ? images : new ArrayList<>();
        this.videoUrl = videoUrl;
        this.hashtags = hashtags != null ? hashtags : new ArrayList<>();
        this.status = status;
    }

    public void update(Composer composer, PostType type, String title, String content,
                      List<String> images, String videoUrl, List<String> hashtags, 
                      PostStatus status) {
        if (composer != null) this.composer = composer;
        if (type != null) this.type = type;
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (images != null) this.images = images;
        if (videoUrl != null) this.videoUrl = videoUrl;
        if (hashtags != null) this.hashtags = hashtags;
        if (status != null) this.status = status;
    }

}
