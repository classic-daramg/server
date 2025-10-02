package com.daramg.server.domain.post.domain;

import com.daramg.server.domain.composer.Composer;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.NonNull;

import java.util.List;

@Entity
@DiscriminatorValue("STORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryPost extends Post {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "composer_id")
    private Composer primaryComposer;

    @Builder
    public StoryPost(Composer primaryComposer, @NonNull String title,
                     @NonNull String content, @Singular List<String> images, String videoUrl,
                     @Singular List<String> hashtags, @NonNull PostStatus postStatus) {
        super(title, content, images, videoUrl, hashtags, postStatus);
        this.primaryComposer = primaryComposer;
    }
}
