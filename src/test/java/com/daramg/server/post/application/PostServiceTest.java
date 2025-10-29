package com.daramg.server.post.application;

import com.daramg.server.common.exception.NotFoundException;
import com.daramg.server.composer.domain.Composer;
import com.daramg.server.composer.domain.Continent;
import com.daramg.server.composer.domain.Era;
import com.daramg.server.composer.domain.Gender;
import com.daramg.server.composer.repository.ComposerRepository;
import com.daramg.server.post.domain.CurationPost;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.PostStatus;
import com.daramg.server.post.domain.StoryPost;
import com.daramg.server.post.dto.PostCreateDto;
import com.daramg.server.post.dto.PostLikeResponseDto;
import com.daramg.server.post.dto.PostScrapResponseDto;
import com.daramg.server.post.dto.PostUpdateDto;
import com.daramg.server.post.repository.PostRepository;
import com.daramg.server.post.repository.PostScrapRepository;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.repository.UserRepository;
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

    @Autowired
    private PostScrapRepository postScrapRepository;

    private User user;
    private Composer composer;

    @BeforeEach
    void setUp() {
        user = new User("email", "password", "name", LocalDate.now(), "profile image", "호시", "bio", null);
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
                    .hasMessageContaining("존재하지 않는 작곡가 ID가 포함되어 있습니다.");
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
                    .hasMessageContaining("존재하지 않는 작곡가 ID가 포함되어 있습니다.");
        }

        @Test
        void 다른_사용자의_포스트를_수정하면_에러가_발생한다() {
            //given
            User anotherUser = new User("another@email.com", "password", "another name", LocalDate.now(), "profile", "햄쥑이", "bio", null);
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
        User anotherUser = new User("another@email.com", "password", "another name", LocalDate.now(), "profile image", "호랑이", "bio", null);
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

    @Nested
    @DisplayName("포스트 좋아요 토글 테스트")
    class PostLikeToggleTest {
        @Test
        void 포스트에_좋아요를_추가한다() {
            //given
            PostCreateDto.CreateFree createDto = new PostCreateDto.CreateFree(
                    "좋아요 테스트 제목", "좋아요 테스트 내용", PostStatus.PUBLISHED,
                    List.of("image"), "video", List.of("#like")
            );
            postService.createFree(createDto, user);
            
            Post savedPost = postRepository.findAll().getFirst();
            Long postId = savedPost.getId();
            
            //when
            PostLikeResponseDto response = postService.toggleLike(postId, user);
            
            //then
            assertThat(response.isLiked()).isTrue();
            assertThat(response.getLikeCount()).isEqualTo(1);
            
            Post updatedPost = postRepository.findById(postId).orElseThrow();
            assertThat(updatedPost.getLikeCount()).isEqualTo(1);
        }

        @Test
        void 이미_좋아요한_포스트의_좋아요를_취소한다() {
            //given
            PostCreateDto.CreateFree createDto = new PostCreateDto.CreateFree(
                    "좋아요 취소 테스트 제목", "좋아요 취소 테스트 내용", PostStatus.PUBLISHED,
                    List.of("image"), "video", List.of("#unlike")
            );
            postService.createFree(createDto, user);
            
            Post savedPost = postRepository.findAll().getFirst();
            Long postId = savedPost.getId();
            
            // 먼저 좋아요 추가
            postService.toggleLike(postId, user);
            
            //when - 좋아요 취소
            PostLikeResponseDto response = postService.toggleLike(postId, user);
            
            //then
            assertThat(response.isLiked()).isFalse();
            assertThat(response.getLikeCount()).isEqualTo(0);
            
            Post updatedPost = postRepository.findById(postId).orElseThrow();
            assertThat(updatedPost.getLikeCount()).isEqualTo(0);
        }

        @Test
        void 여러_사용자가_같은_포스트에_좋아요를_누르면_개수가_증가한다() {
            //given
            PostCreateDto.CreateFree createDto = new PostCreateDto.CreateFree(
                    "다중 좋아요 테스트 제목", "다중 좋아요 테스트 내용", PostStatus.PUBLISHED,
                    List.of("image"), "video", List.of("#multi_like")
            );
            postService.createFree(createDto, user);
            
            Post savedPost = postRepository.findAll().getFirst();
            Long postId = savedPost.getId();
            
            User anotherUser = new User("another@email.com", "password", "another name", 
                    LocalDate.now(), "profile", "햄쥑이", "bio", null);
            userRepository.save(anotherUser);
            
            //when
            PostLikeResponseDto response1 = postService.toggleLike(postId, user);
            PostLikeResponseDto response2 = postService.toggleLike(postId, anotherUser);
            
            //then
            assertThat(response1.isLiked()).isTrue();
            assertThat(response1.getLikeCount()).isEqualTo(1);
            
            assertThat(response2.isLiked()).isTrue();
            assertThat(response2.getLikeCount()).isEqualTo(2);
            
            Post updatedPost = postRepository.findById(postId).orElseThrow();
            assertThat(updatedPost.getLikeCount()).isEqualTo(2);
        }

        @Test
        void 존재하지_않는_포스트에_좋아요를_누르면_에러가_발생한다() {
            //given
            Long nonExistentPostId = 999L;
            
            //when & then
            assertThatThrownBy(() -> postService.toggleLike(nonExistentPostId, user))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("포스트 스크랩 토글 테스트")
    class PostScrapToggleTest {
        @Test
        void 포스트를_스크랩한다() {
            //given
            PostCreateDto.CreateFree createDto = new PostCreateDto.CreateFree(
                    "스크랩 테스트 제목", "스크랩 테스트 내용", PostStatus.PUBLISHED,
                    List.of("image"), "video", List.of("#scrap")
            );
            postService.createFree(createDto, user);

            Post savedPost = postRepository.findAll().getFirst();
            Long postId = savedPost.getId();

            //when
            PostScrapResponseDto response = postService.toggleScrap(postId, user);
            boolean exists = postScrapRepository.existsByPostIdAndUserId(postId, user.getId());

            //then
            assertThat(response.isScrapped()).isTrue();
            assertThat(exists).isTrue();
        }

        @Test
        void 이미_스크랩한_포스트의_스크랩을_취소한다() {
            //given
            PostCreateDto.CreateFree createDto = new PostCreateDto.CreateFree(
                    "스크랩 취소 테스트 제목", "스크랩 취소 테스트 내용", PostStatus.PUBLISHED,
                    List.of("image"), "video", List.of("#unscrap")
            );
            postService.createFree(createDto, user);
            
            Post savedPost = postRepository.findAll().getFirst();
            Long postId = savedPost.getId();
            
            // 먼저 스크랩 추가
            postService.toggleScrap(postId, user);
            
            //when - 스크랩 취소
            PostScrapResponseDto response = postService.toggleScrap(postId, user);
            boolean exists = postScrapRepository.existsByPostIdAndUserId(postId, user.getId());

            //then
            assertThat(response.isScrapped()).isFalse();
            assertThat(exists).isFalse();
        }

        @Test
        void 존재하지_않는_포스트를_스크랩하려고_하면_에러가_발생한다() {
            //given
            Long nonExistentPostId = 999L;
            
            //when & then
            assertThatThrownBy(() -> postService.toggleScrap(nonExistentPostId, user))
                    .isInstanceOf(NotFoundException.class);
        }
    }

}
