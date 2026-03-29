package com.daramg.server.aicomment.application;

import com.daramg.server.aicomment.domain.AiCommentJob;
import com.daramg.server.aicomment.domain.AiCommentJobTriggerType;
import com.daramg.server.aicomment.domain.AiCommentSettings;
import com.daramg.server.aicomment.infrastructure.GeminiClient;
import com.daramg.server.aicomment.repository.AiCommentJobRepository;
import com.daramg.server.aicomment.repository.AiCommentSettingsRepository;
import com.daramg.server.common.exception.NotFoundException;
import com.daramg.server.composer.domain.Composer;
import com.daramg.server.composer.domain.ComposerPersona;
import com.daramg.server.composer.domain.Gender;
import com.daramg.server.composer.repository.ComposerPersonaRepository;
import com.daramg.server.composer.repository.ComposerRepository;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.PostStatus;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AiCommentAdminServiceTest extends ServiceTestSupport {

    @Autowired
    private AiCommentService aiCommentService;

    @Autowired
    private AiCommentJobRepository aiCommentJobRepository;

    @Autowired
    private AiCommentSettingsRepository aiCommentSettingsRepository;


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
    private Post post;

    @BeforeEach
    void setUp() {
        user = new User("user@test.com", "password", "테스터", LocalDate.now(), null, "테스터닉", null, null);
        userRepository.save(user);

        botUser = new User("ai-bot@classicaldaramz.com", "LOCKED", "AI", LocalDate.of(2000, 1, 1), null, "ai_bot", null, null);
        userRepository.save(botUser);
        ReflectionTestUtils.setField(aiCommentService, "botUser", botUser);

        composer = Composer.builder()
                .koreanName("베토벤").englishName("Beethoven").gender(Gender.MALE).build();
        composerRepository.save(composer);

        post = postRepository.save(FreePost.from(new PostCreateVo.Free(
                user, "제목", "내용", PostStatus.PUBLISHED, List.of(), null, List.of()
        )));

        aiCommentSettingsRepository.save(AiCommentSettings.defaultSettings());
    }

    private ComposerPersona savePersona(Composer c) {
        ComposerPersona persona = ComposerPersona.builder()
                .composer(c).identity("완벽주의자").mission("연습 독려").constraintsText("반말, 150자 이내").build();
        return composerPersonaRepository.save(persona);
    }

    private AiCommentSettings getSettings() {
        return aiCommentSettingsRepository.findAll().get(0);
    }

    @Nested
    @DisplayName("자동 감지 토글")
    class AutoDetectToggleTest {

        @Test
        void 자동감지를_비활성화하면_false가_반환된다() {
            // when
            aiCommentService.setAutoDetectEnabled(false);

            // then
            assertThat(aiCommentService.isAutoDetectEnabled()).isFalse();
        }

        @Test
        void 자동감지를_다시_활성화하면_true가_반환된다() {
            // given
            aiCommentService.setAutoDetectEnabled(false);

            // when
            aiCommentService.setAutoDetectEnabled(true);

            // then
            assertThat(aiCommentService.isAutoDetectEnabled()).isTrue();
        }

        @Test
        void 자동감지_비활성화_시_게시물_발행해도_잡이_등록되지_않는다() {
            // given
            savePersona(composer);
            Post mentionPost = postRepository.save(FreePost.from(new PostCreateVo.Free(
                    user, "베토벤 이야기", "베토벤 내용", PostStatus.PUBLISHED, List.of(), null, List.of()
            )));
            aiCommentService.setAutoDetectEnabled(false);

            // when
            aiCommentService.scheduleForPost(mentionPost);

            // then
            assertThat(aiCommentJobRepository.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("수동 할당")
    class ManualAssignTest {

        @Test
        void 수동_할당_시_ADMIN_ASSIGNED_잡이_즉시_등록된다() {
            // given
            savePersona(composer);

            // when
            aiCommentService.scheduleManually(post.getId(), composer.getId());

            // then
            List<AiCommentJob> jobs = aiCommentJobRepository.findAll();
            assertThat(jobs).hasSize(1);
            assertThat(jobs.get(0).getTriggerType()).isEqualTo(AiCommentJobTriggerType.ADMIN_ASSIGNED);
            assertThat(jobs.get(0).getComposer().getId()).isEqualTo(composer.getId());
        }

        @Test
        void 자동감지_비활성화_상태에서도_수동_할당은_가능하다() {
            // given
            savePersona(composer);
            aiCommentService.setAutoDetectEnabled(false);

            // when
            aiCommentService.scheduleManually(post.getId(), composer.getId());

            // then
            assertThat(aiCommentJobRepository.findAll()).hasSize(1);
        }

        @Test
        void 존재하지_않는_게시물에_수동_할당_시_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> aiCommentService.scheduleManually(999L, composer.getId()))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void 페르소나_없는_작곡가에_수동_할당_시_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> aiCommentService.scheduleManually(post.getId(), composer.getId()))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
