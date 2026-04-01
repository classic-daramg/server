package com.daramg.server.aicomment.application;

import com.daramg.server.aicomment.domain.AiCommentJob;
import com.daramg.server.aicomment.domain.AiCommentJobStatus;
import com.daramg.server.aicomment.domain.AiCommentJobTriggerType;
import com.daramg.server.aicomment.infrastructure.GeminiClient;
import com.daramg.server.aicomment.repository.AiCommentJobRepository;
import com.daramg.server.comment.domain.Comment;
import com.daramg.server.comment.repository.CommentRepository;
import com.daramg.server.composer.domain.Composer;
import com.daramg.server.composer.domain.ComposerPersona;
import com.daramg.server.composer.domain.Gender;
import com.daramg.server.composer.repository.ComposerPersonaRepository;
import com.daramg.server.composer.repository.ComposerRepository;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.PostStatus;
import com.daramg.server.post.domain.StoryPost;
import com.daramg.server.post.domain.vo.PostCreateVo;
import com.daramg.server.post.repository.PostRepository;
import com.daramg.server.testsupport.support.ServiceTestSupport;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class AiCommentServiceTest extends ServiceTestSupport {

    @Autowired
    private AiCommentService aiCommentService;

    @Autowired
    private AiCommentJobRepository aiCommentJobRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ComposerRepository composerRepository;

    @Autowired
    private ComposerPersonaRepository composerPersonaRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private GeminiClient geminiClient;

    private User user;
    private User botUser;
    private Composer composer;

    @BeforeEach
    void setUp() {
        user = new User("user@test.com", "password", "테스터", LocalDate.now(), null, "테스터닉", null, null);
        userRepository.save(user);

        botUser = new User("ai-bot@classicaldaramz.com", "LOCKED", "AI", LocalDate.of(2000, 1, 1), null, "ai_bot", null, null);
        userRepository.save(botUser);
        ReflectionTestUtils.setField(aiCommentService, "botUser", botUser);
        ReflectionTestUtils.setField(aiCommentService, "initialDelayMinutes", 10);
        ReflectionTestUtils.setField(aiCommentService, "replyDelayMinutes", 5);

        composer = Composer.builder()
                .koreanName("베토벤")
                .englishName("Beethoven")
                .gender(Gender.MALE)
                .build();
        composerRepository.save(composer);
    }

    private ComposerPersona savePersona(Composer c) {
        ComposerPersona persona = ComposerPersona.builder()
                .composer(c)
                .identity("까칠한 완벽주의자")
                .mission("연습을 독려한다")
                .constraintsText("반말 사용, 150자 이내")
                .build();
        return composerPersonaRepository.save(persona);
    }

    @Nested
    @DisplayName("게시물 생성 시 AI 잡 스케줄링")
    class ScheduleForPostTest {

        @Test
        void DRAFT_게시물은_잡이_등록되지_않는다() {
            // given
            savePersona(composer);
            Post post = postRepository.save(FreePost.from(new PostCreateVo.Free(
                    user, "베토벤 이야기", "베토벤의 곡은 훌륭합니다", PostStatus.DRAFT,
                    List.of(), null, List.of()
            )));

            // when
            aiCommentService.scheduleForPost(post);

            // then
            assertThat(aiCommentJobRepository.findAll()).isEmpty();
        }

        @Test
        void FreePost에서_본문에_작곡가_이름이_없으면_잡이_등록되지_않는다() {
            // given
            savePersona(composer);
            Post post = postRepository.save(FreePost.from(new PostCreateVo.Free(
                    user, "오늘의 음악", "클래식 음악은 좋습니다", PostStatus.PUBLISHED,
                    List.of(), null, List.of()
            )));

            // when
            aiCommentService.scheduleForPost(post);

            // then
            assertThat(aiCommentJobRepository.findAll()).isEmpty();
        }

        @Test
        void FreePost_본문에_작곡가_이름이_있으면_잡이_등록된다() {
            // given
            savePersona(composer);
            Post post = postRepository.save(FreePost.from(new PostCreateVo.Free(
                    user, "베토벤의 9번 교향곡", "베토벤의 9번 교향곡을 들었습니다", PostStatus.PUBLISHED,
                    List.of(), null, List.of()
            )));

            // when
            aiCommentService.scheduleForPost(post);

            // then
            List<AiCommentJob> jobs = aiCommentJobRepository.findAll();
            assertThat(jobs).hasSize(1);
            assertThat(jobs.get(0).getComposer().getId()).isEqualTo(composer.getId());
            assertThat(jobs.get(0).getTriggerType()).isEqualTo(AiCommentJobTriggerType.POST_CREATED);
            assertThat(jobs.get(0).getStatus()).isEqualTo(AiCommentJobStatus.PENDING);
        }

        @Test
        void StoryPost에_연결된_작곡가로_잡이_등록된다() {
            // given
            savePersona(composer);
            Post post = postRepository.save(StoryPost.from(new PostCreateVo.Story(
                    user, "스토리 제목", "스토리 내용", PostStatus.PUBLISHED,
                    List.of(), null, List.of(), composer
            )));

            // when
            aiCommentService.scheduleForPost(post);

            // then
            List<AiCommentJob> jobs = aiCommentJobRepository.findAll();
            assertThat(jobs).hasSize(1);
            assertThat(jobs.get(0).getComposer().getId()).isEqualTo(composer.getId());
        }

        @Test
        void 작곡가에_페르소나가_없으면_잡이_등록되지_않는다() {
            // given - 페르소나 없음
            Post post = postRepository.save(StoryPost.from(new PostCreateVo.Story(
                    user, "스토리 제목", "스토리 내용", PostStatus.PUBLISHED,
                    List.of(), null, List.of(), composer
            )));

            // when
            aiCommentService.scheduleForPost(post);

            // then
            assertThat(aiCommentJobRepository.findAll()).isEmpty();
        }

        @Test
        void 비활성화된_페르소나는_잡이_등록되지_않는다() {
            // given
            ComposerPersona persona = savePersona(composer);
            ReflectionTestUtils.setField(persona, "isActive", false);
            composerPersonaRepository.save(persona);

            Post post = postRepository.save(StoryPost.from(new PostCreateVo.Story(
                    user, "스토리 제목", "스토리 내용", PostStatus.PUBLISHED,
                    List.of(), null, List.of(), composer
            )));

            // when
            aiCommentService.scheduleForPost(post);

            // then
            assertThat(aiCommentJobRepository.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("AI 댓글에 답글 달 때 AI 답글 잡 스케줄링")
    class ScheduleReplyTest {

        @Test
        void aiReplyCount가_2_미만이면_잡이_등록된다() {
            // given
            savePersona(composer);
            Post post = postRepository.save(FreePost.from(new PostCreateVo.Free(
                    user, "제목", "내용", PostStatus.PUBLISHED, List.of(), null, List.of()
            )));

            Comment aiComment = Comment.ofAi(post, botUser, "AI 댓글", null, composer);
            commentRepository.save(aiComment);

            // when
            aiCommentService.scheduleReplyForAiComment(aiComment.getId(), post.getId());

            // then
            List<AiCommentJob> jobs = aiCommentJobRepository.findAll();
            assertThat(jobs).hasSize(1);
            assertThat(jobs.get(0).getTriggerType()).isEqualTo(AiCommentJobTriggerType.USER_REPLY);
            assertThat(jobs.get(0).getParentComment().getId()).isEqualTo(aiComment.getId());
            assertThat(commentRepository.findById(aiComment.getId()).get().getAiReplyCount()).isEqualTo((byte) 1);
        }

        @Test
        void aiReplyCount가_2_이상이면_잡이_등록되지_않는다() {
            // given
            Post post = postRepository.save(FreePost.from(new PostCreateVo.Free(
                    user, "제목", "내용", PostStatus.PUBLISHED, List.of(), null, List.of()
            )));

            Comment aiComment = Comment.ofAi(post, botUser, "AI 댓글", null, composer);
            commentRepository.save(aiComment);
            ReflectionTestUtils.setField(aiComment, "aiReplyCount", (byte) 2);
            commentRepository.save(aiComment);

            // when
            aiCommentService.scheduleReplyForAiComment(aiComment.getId(), post.getId());

            // then
            assertThat(aiCommentJobRepository.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("AI 댓글 잡 실행")
    class ProcessJobTest {

        @Test
        void 잡_실행_시_AI_댓글이_생성되고_잡이_DONE_상태가_된다() {
            // given
            savePersona(composer);
            Post post = postRepository.save(FreePost.from(new PostCreateVo.Free(
                    user, "베토벤 이야기", "내용", PostStatus.PUBLISHED, List.of(), null, List.of()
            )));

            AiCommentJob job = aiCommentJobRepository.save(
                    AiCommentJob.of(post, composer, AiCommentJobTriggerType.POST_CREATED, null,
                            java.time.Instant.now())
            );

            when(geminiClient.generateComment(anyString(), anyString())).thenReturn("테스트 AI 댓글입니다.");

            // when
            aiCommentService.processJob(job);

            // then
            AiCommentJob savedJob = aiCommentJobRepository.findById(job.getId()).orElseThrow();
            assertThat(savedJob.getStatus()).isEqualTo(AiCommentJobStatus.DONE);

            List<Comment> comments = commentRepository.findAll();
            assertThat(comments).hasSize(1);
            Comment saved = comments.get(0);
            assertThat(saved.isAi()).isTrue();
            assertThat(saved.getContent()).isEqualTo("테스트 AI 댓글입니다.");
            assertThat(saved.getComposer().getId()).isEqualTo(composer.getId());
        }

        @Test
        void Gemini_호출_실패_시_잡이_FAILED_상태가_된다() {
            // given
            savePersona(composer);
            Post post = postRepository.save(FreePost.from(new PostCreateVo.Free(
                    user, "제목", "내용", PostStatus.PUBLISHED, List.of(), null, List.of()
            )));

            AiCommentJob job = aiCommentJobRepository.save(
                    AiCommentJob.of(post, composer, AiCommentJobTriggerType.POST_CREATED, null,
                            java.time.Instant.now())
            );

            when(geminiClient.generateComment(anyString(), anyString()))
                    .thenThrow(new RuntimeException("Gemini API 오류"));

            // when
            aiCommentService.processJob(job);

            // then
            AiCommentJob savedJob = aiCommentJobRepository.findById(job.getId()).orElseThrow();
            assertThat(savedJob.getStatus()).isEqualTo(AiCommentJobStatus.FAILED);
            assertThat(commentRepository.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("실행 대상 잡 조회")
    class FindPendingJobsTest {

        @Test
        void scheduled_at이_지난_PENDING_잡만_조회된다() {
            // given
            Post post = postRepository.save(FreePost.from(new PostCreateVo.Free(
                    user, "제목", "내용", PostStatus.PUBLISHED, List.of(), null, List.of()
            )));

            java.time.Instant past = java.time.Instant.now().minusSeconds(60);
            java.time.Instant future = java.time.Instant.now().plusSeconds(600);

            aiCommentJobRepository.save(AiCommentJob.of(post, composer, AiCommentJobTriggerType.POST_CREATED, null, past));
            aiCommentJobRepository.save(AiCommentJob.of(post, composer, AiCommentJobTriggerType.POST_CREATED, null, future));

            // when
            List<AiCommentJob> result = aiCommentService.findPendingJobsDue();

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        void DONE_상태_잡은_조회되지_않는다() {
            // given
            Post post = postRepository.save(FreePost.from(new PostCreateVo.Free(
                    user, "제목", "내용", PostStatus.PUBLISHED, List.of(), null, List.of()
            )));

            AiCommentJob job = aiCommentJobRepository.save(
                    AiCommentJob.of(post, composer, AiCommentJobTriggerType.POST_CREATED, null,
                            java.time.Instant.now().minusSeconds(60))
            );
            job.markDone();
            aiCommentJobRepository.save(job);

            // when
            List<AiCommentJob> result = aiCommentService.findPendingJobsDue();

            // then
            assertThat(result).isEmpty();
        }
    }
}
