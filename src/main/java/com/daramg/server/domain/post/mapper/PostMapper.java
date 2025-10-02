package com.daramg.server.domain.post.mapper;

import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.domain.post.domain.FreePost;
import com.daramg.server.domain.post.domain.StoryPost;
import com.daramg.server.domain.post.domain.CurationPost;
import com.daramg.server.domain.post.domain.Post;
import com.daramg.server.domain.post.domain.PostType;
import com.daramg.server.domain.post.dto.PostRequest;
import com.daramg.server.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final Map<PostType, PostSupplier> mappers = Map.of(
            PostType.FREE, (request, user) -> toFreePost((PostRequest.CreateFree) request, user),
            PostType.STORY, (request, user) -> toStoryPost((PostRequest.CreateStory) request, user),
            PostType.CURATION, (request, user) -> toCurationPost((PostRequest.CreateCuration) request, user)
    );

    public Post toEntity(PostRequest request, User user) {
        return Optional.ofNullable(mappers.get(request.getPostType()))
                .map(supplier -> supplier.get(request, user))
                .orElseThrow(BusinessException::new); // TODO: 메시지 포함하도록
    }

    private Post toFreePost(PostRequest.CreateFree request, User user) {
        return FreePost.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .images(request.getImages())
                .videoUrl(request.getVideoUrl())
                .postStatus(request.getPostStatus())
                .hashtags(request.getHashtags())
                .build();

    }

    private Post toStoryPost(PostRequest.CreateStory request, User user) {
        return StoryPost.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .images(request.getImages())
                .videoUrl(request.getVideoUrl())
                .postStatus(request.getPostStatus())
                .hashtags(request.getHashtags())
                .primaryComposer(request.getPrimaryComposer())
                .build();

    }

    private Post toCurationPost(PostRequest.CreateCuration request, User user) {
        return CurationPost.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .images(request.getImages())
                .videoUrl(request.getVideoUrl())
                .postStatus(request.getPostStatus())
                .hashtags(request.getHashtags())
                .primaryComposer(request.getPrimaryComposer())
                .additionalComposers(request.getAdditionalComposers())
                .build();

    }

}