package com.daramg.server.domain.post.domain;

import com.daramg.server.domain.composer.domain.Composer;
import com.daramg.server.domain.post.domain.vo.PostCreateVo;
import com.daramg.server.domain.user.domain.User;
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

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "curation_post_additional_composers",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "composer_id")
    )
    private List<Composer> additionalComposers = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    public CurationPost(@NonNull User user, Composer primaryComposer, @NonNull String title,
                        @NonNull String content, @Singular List<String> images, String videoUrl,
                        @Singular List<String> hashtags, @NonNull PostStatus postStatus,
                        @Singular List<Composer> additionalComposers) {
        super(user, title, content, images, videoUrl, hashtags, postStatus);
        this.primaryComposer = primaryComposer;
        this.additionalComposers = additionalComposers;
    }

    public static CurationPost from(PostCreateVo.Curation vo) {
        return CurationPost.builder()
                .user(vo.getUser())
                .title(vo.getTitle())
                .content(vo.getContent())
                .images(vo.getImages())
                .videoUrl(vo.getVideoUrl())
                .postStatus(vo.getPostStatus())
                .hashtags(vo.getHashtags())
                .primaryComposer(vo.getPrimaryComposer())
                .additionalComposers(vo.getAdditionalComposers())
                .build();
    }

    public void updateAdditionalComposers(List<Composer> additionalComposers) {
        this.additionalComposers.clear();
        if (additionalComposers != null) {
            this.additionalComposers.addAll(additionalComposers);
        }
    }
}
