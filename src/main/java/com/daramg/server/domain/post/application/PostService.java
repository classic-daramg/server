package com.daramg.server.domain.post.application;

import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.common.exception.NotFoundException;
import com.daramg.server.domain.composer.domain.Composer;
import com.daramg.server.domain.composer.repository.ComposerRepository;
import com.daramg.server.domain.post.domain.CurationPost;
import com.daramg.server.domain.post.domain.FreePost;
import com.daramg.server.domain.post.domain.Post;
import com.daramg.server.domain.post.domain.StoryPost;
import com.daramg.server.domain.post.domain.vo.PostCreateVo;
import com.daramg.server.domain.post.domain.vo.PostUpdateVo;
import com.daramg.server.domain.post.dto.PostCreateDto;
import com.daramg.server.domain.post.dto.PostUpdateDto;
import com.daramg.server.domain.post.repository.PostRepository;
import com.daramg.server.domain.post.utils.PostUserValidator;
import com.daramg.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ComposerRepository composerRepository;
    private final EntityUtils entityUtils;

    private static final String INVALID_POST_TYPE = "게시글 타입이 올바르지 않습니다.";

    @Transactional
    public void createFree(PostCreateDto.CreateFree dto, User user) {
        PostCreateVo.Free vo = new PostCreateVo.Free(
                user,
                dto.getTitle(),
                dto.getContent(),
                dto.getPostStatus(),
                dto.getImages(),
                dto.getVideoUrl(),
                dto.getHashtags()
        );
        Post post = FreePost.from(vo);
        postRepository.save(post);
    }

    @Transactional
    public void createCuration(PostCreateDto.CreateCuration dto, User user) {
        Composer primaryComposer = entityUtils.getEntity(dto.getPrimaryComposerId(), Composer.class);
        List<Composer> additionalComposers = new ArrayList<>();

        if (!dto.getAdditionalComposerIds().isEmpty()){
            List<Long> composerIds = dto.getAdditionalComposerIds();
            additionalComposers = composerRepository.findAllById(dto.getAdditionalComposerIds());

            if (additionalComposers.size() != composerIds.size()) {
                throw new NotFoundException("존재하지 않는 작곡가 ID가 포함되어 있습니다.");
            }
        }

        PostCreateVo.Curation vo = new PostCreateVo.Curation(
                user,
                dto.getTitle(),
                dto.getContent(),
                dto.getPostStatus(),
                dto.getImages(),
                dto.getVideoUrl(),
                dto.getHashtags(),
                primaryComposer,
                additionalComposers
        );
        Post post = CurationPost.from(vo);
        postRepository.save(post);
    }

    @Transactional
    public void createStory(PostCreateDto.CreateStory dto, User user) {
        Composer primaryComposer = entityUtils.getEntity(dto.getPrimaryComposerId(), Composer.class);
        PostCreateVo.Story vo = new PostCreateVo.Story(
                user,
                dto.getTitle(),
                dto.getContent(),
                dto.getPostStatus(),
                dto.getImages(),
                dto.getVideoUrl(),
                dto.getHashtags(),
                primaryComposer
        );
        Post post = StoryPost.from(vo);
        postRepository.save(post);
    }

    @Transactional
    public void updateFree(Long postId, PostUpdateDto.UpdateFree dto, User user) {
        Post post = entityUtils.getEntity(postId, Post.class);
        if (!(post instanceof FreePost freePost)) {
            throw new BusinessException(INVALID_POST_TYPE);
        }
        PostUserValidator.check(freePost, user);
        PostUpdateVo vo = toUpdateVo(dto);
        freePost.update(vo);
    }

    @Transactional
    public void updateStory(Long postId, PostUpdateDto.UpdateStory dto, User user) {
        Post post = entityUtils.getEntity(postId, Post.class);
        if (!(post instanceof StoryPost storyPost)) {
            throw new BusinessException(INVALID_POST_TYPE);
        }
        PostUserValidator.check(storyPost, user);
        PostUpdateVo vo = toUpdateVo(dto);
        storyPost.update(vo);
    }

    @Transactional
    public void updateCuration(Long postId, PostUpdateDto.UpdateCuration dto, User user) {
        Post post = entityUtils.getEntity(postId, Post.class);
        if (!(post instanceof CurationPost curationPost)) {
            throw new BusinessException(INVALID_POST_TYPE);
        }
        PostUserValidator.check(curationPost, user);
        PostUpdateVo vo = toUpdateVo(dto);
        curationPost.update(vo);

        if (dto.getAdditionalComposersId() != null){
            List<Long> composerIds = dto.getAdditionalComposersId();
            List<Composer> composersToUpdate = composerRepository.findAllById(dto.getAdditionalComposersId());

            if (composersToUpdate.size() != composerIds.size()) {
                throw new NotFoundException("존재하지 않는 작곡가 ID가 포함되어 있습니다.");
            }
            curationPost.updateAdditionalComposers(composersToUpdate);
        }
    }

    private PostUpdateVo toUpdateVo(PostUpdateDto dto){
        return new PostUpdateVo(
                dto.getTitle(), dto.getContent(), dto.getPostStatus(),
                dto.getImages(), dto.getVideoUrl(), dto.getHashtags());
    }

    @Transactional
    public void delete(Long postId, User user){
        Post post = entityUtils.getEntity(postId, Post.class);
        PostUserValidator.check(post, user);
        postRepository.deleteById(postId);
    }
}
