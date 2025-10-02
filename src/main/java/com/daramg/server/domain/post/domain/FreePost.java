package com.daramg.server.domain.post.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.NonNull;

import java.util.List;

@Entity
@DiscriminatorValue("FREE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FreePost extends Post {

    @Builder
    public FreePost(@NonNull String title, @NonNull String content, 
                    @Singular List<String> images, String videoUrl,
                    @Singular List<String> hashtags, @NonNull PostStatus postStatus) {
        super(title, content, images, videoUrl, hashtags, postStatus);
    }
}
