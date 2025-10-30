package com.daramg.server.post.domain;

import com.daramg.server.post.domain.vo.PostCreateVo;
import com.daramg.server.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.NonNull;

import java.util.List;

@Entity
@DiscriminatorValue("FREE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FreePost extends Post {

    @Builder(access = AccessLevel.PRIVATE)
    private FreePost(@NonNull User user, @NonNull String title, @NonNull String content,
                    @Singular List<String> images, String videoUrl,
                    @Singular List<String> hashtags, @NonNull PostStatus postStatus) {
        super(user, title, content, images, videoUrl, hashtags, postStatus);
    }

    public static FreePost from(PostCreateVo.Free vo) {
        return FreePost.builder()
                .user(vo.getUser())
                .title(vo.getTitle())
                .content(vo.getContent())
                .images(vo.getImages())
                .videoUrl(vo.getVideoUrl())
                .postStatus(vo.getPostStatus())
                .hashtags(vo.getHashtags())
                .build();
    }
}
