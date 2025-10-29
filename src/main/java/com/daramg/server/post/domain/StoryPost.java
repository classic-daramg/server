package com.daramg.server.post.domain;

import com.daramg.server.composer.domain.Composer;
import com.daramg.server.post.domain.vo.PostCreateVo;
import com.daramg.server.user.domain.User;
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

    @Builder(access = AccessLevel.PRIVATE)
    private StoryPost(@NonNull User user, Composer primaryComposer, @NonNull String title,
                     @NonNull String content, @Singular List<String> images, String videoUrl,
                     @Singular List<String> hashtags, @NonNull PostStatus postStatus) {
        super(user, title, content, images, videoUrl, hashtags, postStatus);
        this.primaryComposer = primaryComposer;
    }

    public static StoryPost from(PostCreateVo.Story vo) {
        return StoryPost.builder()
                .user(vo.getUser())
                .title(vo.getTitle())
                .content(vo.getContent())
                .images(vo.getImages())
                .videoUrl(vo.getVideoUrl())
                .postStatus(vo.getPostStatus())
                .hashtags(vo.getHashtags())
                .primaryComposer(vo.getPrimaryComposer())
                .build();
    }
}
