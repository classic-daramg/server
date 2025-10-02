package com.daramg.server.domain.post.domain;

import com.daramg.server.domain.composer.Composer;
import com.daramg.server.domain.user.User;
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
    public StoryPost(@NonNull User user, Composer primaryComposer, @NonNull String title,
                     @NonNull String content, @Singular List<String> images, String videoUrl,
                     @Singular List<String> hashtags, @NonNull PostStatus postStatus) {
        super(user, title, content, images, videoUrl, hashtags, postStatus);
        this.primaryComposer = primaryComposer;
    }
}
