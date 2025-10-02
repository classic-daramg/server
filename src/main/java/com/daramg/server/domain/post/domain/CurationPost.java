package com.daramg.server.domain.post.domain;

import com.daramg.server.domain.composer.Composer;
import com.daramg.server.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("CURATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CurationPost extends Post {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "composer_id")
    private Composer primaryComposer;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "curation_post_additional_composers",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "composer_id")
    )
    private List<Composer> additionalComposers = new ArrayList<>();

    @Builder
    public CurationPost(@NonNull User user, Composer primaryComposer, @NonNull String title,
                        @NonNull String content, @Singular List<String> images, String videoUrl,
                        @Singular List<String> hashtags, @NonNull PostStatus postStatus,
                        @Singular List<Composer> additionalComposers) {
        super(user, title, content, images, videoUrl, hashtags, postStatus);
        this.primaryComposer = primaryComposer;
        this.additionalComposers = additionalComposers;
    }
}
