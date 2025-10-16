package com.daramg.server.domain.post.service;

import com.daramg.server.common.exception.NotFoundException;
import com.daramg.server.domain.composer.domain.Composer;
import com.daramg.server.domain.composer.domain.Continent;
import com.daramg.server.domain.composer.domain.Era;
import com.daramg.server.domain.composer.domain.Gender;
import com.daramg.server.domain.composer.repository.ComposerRepository;
import com.daramg.server.domain.post.domain.CurationPost;
import com.daramg.server.domain.post.domain.FreePost;
import com.daramg.server.domain.post.domain.Post;
import com.daramg.server.domain.post.domain.PostStatus;
import com.daramg.server.domain.post.domain.StoryPost;
import com.daramg.server.domain.post.dto.PostCreateDto;
import com.daramg.server.domain.post.dto.PostUpdateDto;
import com.daramg.server.domain.post.repository.PostRepository;
import com.daramg.server.domain.user.domain.User;
import com.daramg.server.domain.user.repository.UserRepository;
import com.daramg.server.testsupport.support.ServiceTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PostServiceTest extends ServiceTestSupport {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComposerRepository composerRepository;

    private User user;
    private Composer composer;

    @BeforeEach
    void setUp() {
        user = new User("email", "password", "name", LocalDate.now(), "profie", "bio", null);
        userRepository.save(user);

        composer = Composer.builder()
                .koreanName("베토벤")
                .englishName("Ludwig van Beethoven")
                .nativeName("Ludwig van Beethoven")
                .gender(Gender.MALE)
                .nationality("독일")
                .birthYear((short) 1770)
                .deathYear((short) 1827)
                .era(Era.CLASSICAL)
                .continent(Continent.EUROPE)
                .build();
        composerRepository.save(composer);
    }

    @Nested
    @DisplayName("포스트 생성 정상 테스트")
    class CreatePostSuccess{
        @Test
        void 자유_포스트를_정상적으로_생성한다(){
            //given
            PostCreateDto.CreateFree requestDto = new PostCreateDto.CreateFree(
                    "제목", "내용", PostStatus.PUBLISHED, List.of("image 1", "image 2"), "video url", List.of("#hash_tag")
            );

            //when
            postService.createFree(requestDto, user);

            //then
            List<Post> allPosts = postRepository.findAll();
            assertThat(allPosts).hasSize(1);

            FreePost savedPost = (FreePost) allPosts.getFirst();

            assertThat(savedPost.getTitle()).isEqualTo(requestDto.getTitle());
            assertThat(savedPost.getContent()).isEqualTo(requestDto.getContent());
            assertThat(savedPost.getPostStatus()).isEqualTo(requestDto.getPostStatus());
            assertThat(savedPost.getUser().getId()).isEqualTo(user.getId());
        }

        @Test
        void 큐레이션_포스트를_정상적으로_생성한다(){
            //given
            PostCreateDto.CreateCuration requestDto = new PostCreateDto.CreateCuration(
                    "큐레이션 제목", "큐레이션 내용", PostStatus.PUBLISHED, 
                    List.of("image 1", "image 2"), "video url", List.of("#classical", "#beethoven"), 
                    composer.getId(), List.of()
            );

            //when
            postService.createCuration(requestDto, user);

            //then
            List<Post> allPosts = postRepository.findAll();
            assertThat(allPosts).hasSize(1);

            CurationPost savedPost = (CurationPost) allPosts.getFirst();

            assertThat(savedPost.getTitle()).isEqualTo(requestDto.getTitle());
            assertThat(savedPost.getContent()).isEqualTo(requestDto.getContent());
            assertThat(savedPost.getPostStatus()).isEqualTo(requestDto.getPostStatus());
            assertThat(savedPost.getUser().getId()).isEqualTo(user.getId());
            assertThat(savedPost.getPrimaryComposer().getId()).isEqualTo(composer.getId());
        }

        @Test
        void 스토리_포스트를_정상적으로_생성한다(){
            //given
            PostCreateDto.CreateStory requestDto = new PostCreateDto.CreateStory(
                    "스토리 제목", "스토리 내용", PostStatus.PUBLISHED, 
                    List.of("image 1", "image 2"), "video url", List.of("#story", "#music"), 
                    composer.getId()
            );

            //when
            postService.createStory(requestDto, user);

            //then
            List<Post> allPosts = postRepository.findAll();
            assertThat(allPosts).hasSize(1);

            StoryPost savedPost = (StoryPost) allPosts.getFirst();

            assertThat(savedPost.getTitle()).isEqualTo(requestDto.getTitle());
            assertThat(savedPost.getContent()).isEqualTo(requestDto.getContent());
            assertThat(savedPost.getPostStatus()).isEqualTo(requestDto.getPostStatus());
            assertThat(savedPost.getUser().getId()).isEqualTo(user.getId());
            assertThat(savedPost.getPrimaryComposer().getId()).isEqualTo(composer.getId());
        }

    }

    @Nested
    @DisplayName("포스트 생성 실패 테스트")
    class CreatePostFail {
        @Test
        void 큐레이션_포스트에_존재하지_않는_추가_작곡가를_포함하면_에러가_발생한다() {
            //given
            Long nonExistentComposerId = 999L;
            PostCreateDto.CreateCuration requestDto = new PostCreateDto.CreateCuration(
                    "큐레이션 제목", "큐레이션 내용", PostStatus.PUBLISHED,
                    List.of("image 1"), "video url", List.of("#classical"),
                    composer.getId(), List.of(nonExistentComposerId)
            );

            //when & then
            assertThatThrownBy(() -> postService.createCuration(requestDto, user))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("존재하지 않는 작곡가 ID가 포함되어 있습니다.");
        }

        @Test
        void 스토리_포스트에_존재하지_않는_메인_작곡가가_포함되면_에러가_발생한다() {
            //given
            Long nonExistentComposerId = 999L;
            PostCreateDto.CreateStory requestDto = new PostCreateDto.CreateStory(
                    "스토리 제목", "스토리 내용", PostStatus.PUBLISHED,
                    List.of("image 1"), "video url", List.of("#story"),
                    nonExistentComposerId
            );

            //when & then
            assertThatThrownBy(() -> postService.createStory(requestDto, user))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("존재하지 않는 Composer입니다.");
        }
    }

    @Nested
    @Transactional
    @DisplayName("포스트 수정 정상 테스트")
    class UpdatePostSuccess{
        @Test
        void 자유_포스트를_정상적으로_수정한다(){
            //given
            PostCreateDto.CreateFree createDto = new PostCreateDto.CreateFree(
                    "원래 제목", "원래 내용", PostStatus.PUBLISHED, List.of("original image"), "original video", List.of("#original")
            );
            postService.createFree(createDto, user);
            
            Post savedPost = postRepository.findAll().getFirst();
            Long postId = savedPost.getId();
            
            PostUpdateDto.UpdateFree updateDto = new PostUpdateDto.UpdateFree(
                    "수정된 제목", "수정된 내용", PostStatus.DRAFT, List.of("updated image 1", "updated image 2"), "updated video", List.of("#updated", "#free")
            );

            //when
            postService.updateFree(postId, updateDto, user);

            //then
            FreePost updatedPost = (FreePost) postRepository.findById(postId).orElseThrow();
            
            assertThat(updatedPost.getTitle()).isEqualTo(updateDto.getTitle());
            assertThat(updatedPost.getContent()).isEqualTo(updateDto.getContent());
            assertThat(updatedPost.getPostStatus()).isEqualTo(updateDto.getPostStatus());
            assertThat(updatedPost.getImages()).isEqualTo(updateDto.getImages());
            assertThat(updatedPost.getVideoUrl()).isEqualTo(updateDto.getVideoUrl());
            assertThat(updatedPost.getHashtags()).isEqualTo(updateDto.getHashtags());
        }

        @Test
        void 비디오_url에_빈문자열이_들어오면_null로_수정한다(){
            //given
            PostCreateDto.CreateFree createDto = new PostCreateDto.CreateFree(
                    "원래 제목", "원래 내용", PostStatus.PUBLISHED, List.of("original image"), "original video", List.of("#original")
            );
            postService.createFree(createDto, user);

            Post savedPost = postRepository.findAll().getFirst();
            Long postId = savedPost.getId();

            PostUpdateDto.UpdateFree updateDto = new PostUpdateDto.UpdateFree(
                    "원래 제목", "원래 내용", PostStatus.PUBLISHED, List.of("original image"), "", List.of("#original")
            );

            //when
            postService.updateFree(postId, updateDto, user);

            //then
            FreePost updatedPost = (FreePost) postRepository.findById(postId).orElseThrow();

            assertThat(updatedPost.getVideoUrl()).isEqualTo(null);
        }

        @Test
        void 큐레이션_포스트를_정상적으로_수정한다(){
            //given
            PostCreateDto.CreateCuration createDto = new PostCreateDto.CreateCuration(
                    "원래 큐레이션 제목", "원래 큐레이션 내용", PostStatus.PUBLISHED,
                    List.of("original image"), "original video", List.of("#original"),
                    composer.getId(), List.of()
            );
            postService.createCuration(createDto, user);
            
            Post savedPost = postRepository.findAll().getFirst();
            Long postId = savedPost.getId();
            
            PostUpdateDto.UpdateCuration updateDto = new PostUpdateDto.UpdateCuration(
                    "수정된 큐레이션 제목", "수정된 큐레이션 내용", PostStatus.DRAFT,
                    List.of("updated image 1", "updated image 2"), "updated video", List.of("#updated", "#curation"),
                    List.of()
            );

            //when
            postService.updateCuration(postId, updateDto, user);

            //then
            CurationPost updatedPost = (CurationPost) postRepository.findById(postId).orElseThrow();
            
            assertThat(updatedPost.getTitle()).isEqualTo(updateDto.getTitle());
            assertThat(updatedPost.getContent()).isEqualTo(updateDto.getContent());
            assertThat(updatedPost.getPostStatus()).isEqualTo(updateDto.getPostStatus());
            assertThat(updatedPost.getImages()).isEqualTo(updateDto.getImages());
            assertThat(updatedPost.getVideoUrl()).isEqualTo(updateDto.getVideoUrl());
            assertThat(updatedPost.getHashtags()).isEqualTo(updateDto.getHashtags());
        }

        @Test
        void 스토리_포스트를_정상적으로_수정한다(){
            //given
            PostCreateDto.CreateStory createDto = new PostCreateDto.CreateStory(
                    "원래 스토리 제목", "원래 스토리 내용", PostStatus.PUBLISHED,
                    List.of("original image"), "original video", List.of("#original"),
                    composer.getId()
            );
            postService.createStory(createDto, user);
            
            Post savedPost = postRepository.findAll().getFirst();
            Long postId = savedPost.getId();
            
            PostUpdateDto.UpdateStory updateDto = new PostUpdateDto.UpdateStory(
                    "수정된 스토리 제목", "수정된 스토리 내용", PostStatus.DRAFT,
                    List.of("updated image 1", "updated image 2"), "updated video", List.of("#updated", "#story")
            );

            //when
            postService.updateStory(postId, updateDto, user);

            //then
            StoryPost updatedPost = (StoryPost) postRepository.findById(postId).orElseThrow();
            
            assertThat(updatedPost.getTitle()).isEqualTo(updateDto.getTitle());
            assertThat(updatedPost.getContent()).isEqualTo(updateDto.getContent());
            assertThat(updatedPost.getPostStatus()).isEqualTo(updateDto.getPostStatus());
            assertThat(updatedPost.getImages()).isEqualTo(updateDto.getImages());
            assertThat(updatedPost.getVideoUrl()).isEqualTo(updateDto.getVideoUrl());
            assertThat(updatedPost.getHashtags()).isEqualTo(updateDto.getHashtags());
        }
    }

    @Nested
    @DisplayName("포스트 수정 실패 테스트")
    class UpdatePostFail{
        @Test
        void 존재하지_않는_포스트_ID로_수정하면_에러가_발생한다() {
            //given
            Long nonExistentPostId = 999L;
            PostUpdateDto.UpdateFree updateDto = new PostUpdateDto.UpdateFree(
                    "수정된 제목", "수정된 내용", PostStatus.DRAFT, 
                    List.of("updated image"), "updated video", List.of("#updated")
            );

            //when & then
            assertThatThrownBy(() -> postService.updateFree(nonExistentPostId, updateDto, user))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void 큐레이션_포스트에_존재하지_않는_추가_작곡가_ID를_포함하면_에러가_발생한다() {
            //given
            PostCreateDto.CreateCuration createDto = new PostCreateDto.CreateCuration(
                    "원래 큐레이션 제목", "원래 큐레이션 내용", PostStatus.PUBLISHED,
                    List.of("original image"), "original video", List.of("#original"),
                    composer.getId(), List.of()
            );
            postService.createCuration(createDto, user);
            
            Post savedPost = postRepository.findAll().getFirst();
            Long postId = savedPost.getId();
            
            Long nonExistentComposerId = 999L;
            PostUpdateDto.UpdateCuration updateDto = new PostUpdateDto.UpdateCuration(
                    "수정된 큐레이션 제목", "수정된 큐레이션 내용", PostStatus.DRAFT,
                    List.of("updated image"), "updated video", List.of("#updated"),
                    List.of(nonExistentComposerId)
            );

            //when & then
            assertThatThrownBy(() -> postService.updateCuration(postId, updateDto, user))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("존재하지 않는 작곡가 ID가 포함되어 있습니다.");
        }

        @Test
        void 다른_사용자의_포스트를_수정하면_에러가_발생한다() {
            //given
            User anotherUser = new User("another@email.com", "password", "another name", LocalDate.now(), "profile", "bio", null);
            userRepository.save(anotherUser);
            
            PostCreateDto.CreateFree createDto = new PostCreateDto.CreateFree(
                    "원래 제목", "원래 내용", PostStatus.PUBLISHED, List.of("original image"), "original video", List.of("#original")
            );
            postService.createFree(createDto, anotherUser);
            
            Post savedPost = postRepository.findAll().getFirst();
            Long postId = savedPost.getId();
            
            PostUpdateDto.UpdateFree updateDto = new PostUpdateDto.UpdateFree(
                    "수정된 제목", "수정된 내용", PostStatus.DRAFT, 
                    List.of("updated image"), "updated video", List.of("#updated")
            );

            //when & then
            assertThatThrownBy(() -> postService.updateFree(postId, updateDto, user))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("포스트와 작성자가 일치하지 않습니다.");
        }
    }

    @Test
    void 포스트를_정상적으로_삭제한다(){
        //given
        PostCreateDto.CreateFree createDto = new PostCreateDto.CreateFree(
                "삭제할 제목", "삭제할 내용", PostStatus.PUBLISHED, 
                List.of("image"), "video", List.of("#delete")
        );
        postService.createFree(createDto, user);
        
        Post savedPost = postRepository.findAll().getFirst();
        Long postId = savedPost.getId();
        
        // 삭제 전 포스트 존재 확인
        assertThat(postRepository.findById(postId)).isPresent();

        //when
        postService.delete(postId, user);

        //then
        assertThat(postRepository.findById(postId)).isEmpty();
    }

    @Test
    void 포스트와_작성자가_일치하지_않으면_삭제하지_못한다(){
        //given
        User anotherUser = new User("another@email.com", "password", "another name", LocalDate.now(), "profile", "bio", null);
        userRepository.save(anotherUser);
        
        PostCreateDto.CreateFree createDto = new PostCreateDto.CreateFree(
                "삭제할 제목", "삭제할 내용", PostStatus.PUBLISHED, 
                List.of("image"), "video", List.of("#delete")
        );
        postService.createFree(createDto, anotherUser);
        
        Post savedPost = postRepository.findAll().getFirst();
        Long postId = savedPost.getId();

        //when & then
        assertThatThrownBy(() -> postService.delete(postId, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("포스트와 작성자가 일치하지 않습니다.");
        
        // 삭제되지 않았는지 확인
        assertThat(postRepository.findById(postId)).isPresent();
    }

}
