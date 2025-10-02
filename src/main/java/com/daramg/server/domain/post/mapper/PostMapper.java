package com.daramg.server.domain.post.mapper;

import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.domain.post.domain.FreePost;
import com.daramg.server.domain.post.domain.StoryPost;
import com.daramg.server.domain.post.domain.CurationPost;
import com.daramg.server.domain.post.domain.Post;
import com.daramg.server.domain.post.domain.PostType;
import com.daramg.server.domain.post.dto.PostRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final EntityUtils entityUtils;

    private final Map<PostType, PostSupplier> mappers = Map.of(
            PostType.FREE, request -> toFreePost((PostRequest.CreateFree) request),
            PostType.STORY, request -> toStoryPost((PostRequest.CreateStory) request),
            PostType.CURATION, request -> toCurationPost((PostRequest.CreateCuration) request)
    );

    public Post toEntity(PostRequest request) {
        return Optional.ofNullable(mappers.get(request.getPostType()))
                .map(supplier -> supplier.get(request))
                .orElseThrow(BusinessException::new); // TODO: 메시지 포함하도록
    }

    private Post toFreePost(PostRequest.CreateFree request) {
        return FreePost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .images(request.getImages())
                .videoUrl(request.getVideoUrl())
                .postStatus(request.getPostStatus())
                .hashtags(request.getHashtags())
                .build();

    }

    private Post toStoryPost(PostRequest.CreateStory request) {
        return StoryPost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .images(request.getImages())
                .videoUrl(request.getVideoUrl())
                .postStatus(request.getPostStatus())
                .hashtags(request.getHashtags())
                .primaryComposer(request.getPrimaryComposer())
                .build();

    }

    private Post toCurationPost(PostRequest.CreateCuration request) {
        return CurationPost.builder()
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