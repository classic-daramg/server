package com.daramg.server.domain.post.domain;

import com.daramg.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "posts")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Post extends BaseEntity<Post> {

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
    @Column(name = "post_status", nullable = false)
    private PostStatus postStatus;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "scrap_count", nullable = false)
    private int scrapCount = 0;

    @Column(name = "comment_count", nullable = false)
    private int commentCount = 0;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = false;

    protected Post(@NonNull String title, @NonNull String content, 
                   @Singular List<String> images, String videoUrl,
                   @Singular List<String> hashtags, @NonNull PostStatus postStatus) {
        this.title = title;
        this.content = content;
        this.images = images != null ? images : new ArrayList<>();
        this.videoUrl = videoUrl;
        this.hashtags = hashtags != null ? hashtags : new ArrayList<>();
        this.postStatus = postStatus;
    }
}
