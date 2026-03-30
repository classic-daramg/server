package com.daramg.server.aicomment.application;

import com.daramg.server.aicomment.domain.AiCommentJob;
import com.daramg.server.aicomment.domain.AiCommentJobStatus;
import com.daramg.server.aicomment.domain.AiCommentJobTriggerType;
import com.daramg.server.aicomment.domain.AiCommentSettings;
import com.daramg.server.aicomment.infrastructure.GeminiClient;
import com.daramg.server.aicomment.repository.AiCommentJobRepository;
import com.daramg.server.aicomment.repository.AiCommentSettingsRepository;
import com.daramg.server.comment.domain.Comment;
import com.daramg.server.comment.repository.CommentRepository;
import com.daramg.server.composer.domain.Composer;
import com.daramg.server.composer.domain.ComposerPersona;
import com.daramg.server.composer.repository.ComposerPersonaRepository;
import com.daramg.server.composer.repository.ComposerRepository;
import com.daramg.server.common.exception.NotFoundException;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.PostStatus;
import com.daramg.server.post.domain.StoryPost;
import com.daramg.server.post.domain.CurationPost;
import com.daramg.server.post.repository.PostRepository;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCommentService {

    private static final String BOT_EMAIL = "ai-bot@classicaldaramz.com";
    private static final int MAX_AI_REPLY_COUNT = 2;

    @Value("${ai-comment.initial-delay-minutes:10}")
    private int initialDelayMinutes;

    @Value("${ai-comment.reply-delay-minutes:5}")
    private int replyDelayMinutes;

    private final AiCommentJobRepository aiCommentJobRepository;
    private final AiCommentSettingsRepository aiCommentSettingsRepository;
    private final ComposerPersonaRepository composerPersonaRepository;
    private final ComposerRepository composerRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final GeminiClient geminiClient;

    private User botUser;

    @PostConstruct
    void initBotUser() {
        botUser = userRepository.findByEmail(BOT_EMAIL).orElse(null);
        if (botUser == null) {
            log.warn("AI 봇 유저를 찾을 수 없습니다: {}. AI 댓글 기능이 비활성화됩니다.", BOT_EMAIL);
        }
    }

    private User getBotUser() {
        if (botUser == null) {
            botUser = userRepository.findByEmail(BOT_EMAIL)
                    .orElseThrow(() -> new IllegalStateException("AI 봇 유저를 찾을 수 없습니다: " + BOT_EMAIL));
        }
        return botUser;
    }

    @Transactional(readOnly = true)
    public boolean isAutoDetectEnabled() {
        return aiCommentSettingsRepository.findAll().stream()
                .findFirst()
                .map(AiCommentSettings::isAutoDetectEnabled)
                .orElse(true);
    }

    @Transactional
    public void setAutoDetectEnabled(boolean enabled) {
        AiCommentSettings settings = aiCommentSettingsRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("AI 댓글 설정을 찾을 수 없습니다."));
        settings.setAutoDetectEnabled(enabled);
        log.info("AI 자동 감지 설정 변경 - enabled={}", enabled);
    }

    @Transactional
    public void scheduleManually(Long postId, Long composerId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));
        Composer composer = composerRepository.findById(composerId)
                .orElseThrow(() -> new NotFoundException("작곡가를 찾을 수 없습니다."));

        ComposerPersona persona = composerPersonaRepository.findByComposerId(composerId)
                .orElseThrow(() -> new NotFoundException("해당 작곡가의 페르소나를 찾을 수 없습니다."));
        if (!persona.isActive()) {
            throw new IllegalStateException("비활성화된 페르소나입니다.");
        }

        AiCommentJob job = AiCommentJob.of(post, composer, AiCommentJobTriggerType.ADMIN_ASSIGNED, null, Instant.now());
        aiCommentJobRepository.save(job);
        log.info("AI 댓글 수동 할당 - postId={}, composerId={}", postId, composerId);
    }

    @Transactional
    public void scheduleForPost(Post post) {
        if (post.getPostStatus() != PostStatus.PUBLISHED) {
            return;
        }

        if (!isAutoDetectEnabled()) {
            log.info("AI 자동 감지 비활성화 상태 - postId={} 스킵", post.getId());
            return;
        }

        List<Composer> composers = getComposersForPost(post);
        log.info("AI 댓글 대상 작곡가 탐지 - postId={}, composers={}", post.getId(),
                composers.stream().map(c -> c.getId() + "(" + c.getKoreanName() + ")").toList());
        if (composers.isEmpty()) {
            return;
        }

        Instant scheduledAt = Instant.now().plusSeconds(initialDelayMinutes * 60L);
        for (Composer composer : composers) {
            Optional<ComposerPersona> persona = composerPersonaRepository.findByComposerId(composer.getId());
            if (persona.isEmpty()) {
                log.info("AI 댓글 잡 스킵 - 페르소나 없음 composerId={}", composer.getId());
                continue;
            }
            if (!persona.get().isActive()) {
                log.info("AI 댓글 잡 스킵 - 페르소나 비활성 composerId={}", composer.getId());
                continue;
            }
            AiCommentJob job = AiCommentJob.of(post, composer, AiCommentJobTriggerType.POST_CREATED, null, scheduledAt);
            aiCommentJobRepository.save(job);
            log.info("AI 댓글 잡 등록 - postId={}, composerId={}, scheduledAt={}", post.getId(), composer.getId(), scheduledAt);
        }
    }

    @Transactional
    public void scheduleReplyForAiComment(Comment aiComment, Post post) {
        if (aiComment.getAiReplyCount() >= MAX_AI_REPLY_COUNT) {
            return;
        }

        Composer composer = aiComment.getComposer();
        if (composer == null) {
            return;
        }

        Optional<ComposerPersona> persona = composerPersonaRepository.findByComposerId(composer.getId());
        if (persona.isEmpty() || !persona.get().isActive()) {
            return;
        }

        aiComment.incrementAiReplyCount();

        Instant scheduledAt = Instant.now().plusSeconds(replyDelayMinutes * 60L);
        AiCommentJob job = AiCommentJob.of(post, composer, AiCommentJobTriggerType.USER_REPLY, aiComment, scheduledAt);
        aiCommentJobRepository.save(job);
        log.info("AI 답글 잡 등록 - parentCommentId={}, composerId={}, scheduledAt={}", aiComment.getId(), composer.getId(), scheduledAt);
    }

    @Transactional
    public void processJob(AiCommentJob job) {
        AiCommentJob managedJob = aiCommentJobRepository.findById(job.getId())
                .orElseThrow(() -> new IllegalStateException("Job not found: " + job.getId()));

        managedJob.markInProgress();

        try {
            ComposerPersona persona = composerPersonaRepository.findByComposerId(managedJob.getComposer().getId())
                    .orElseThrow();

            String systemInstruction = buildSystemInstruction(persona);
            String userPrompt = buildUserPrompt(managedJob);

            String generatedText = geminiClient.generateComment(systemInstruction, userPrompt);

            Comment parentComment = managedJob.getParentComment();
            Comment comment = Comment.ofAi(managedJob.getPost(), getBotUser(), generatedText.strip(), parentComment, managedJob.getComposer());
            commentRepository.save(comment);
            managedJob.getPost().incrementCommentCount();

            managedJob.markDone();
            log.info("AI 댓글 생성 완료 - jobId={}", managedJob.getId());
        } catch (Exception e) {
            managedJob.markFailed();
            log.error("AI 댓글 생성 실패 - jobId={}", managedJob.getId(), e);
        }
    }

    public List<AiCommentJob> findPendingJobsDue() {
        return aiCommentJobRepository.findPendingJobsDue(AiCommentJobStatus.PENDING, Instant.now());
    }

    private List<Composer> getComposersForPost(Post post) {
        List<Composer> composers = new ArrayList<>();

        Composer primary = getComposerFromPost(post);
        if (primary != null) {
            composers.add(primary);
        }

        String text = post.getTitle() + " " + post.getContent();
        for (Composer c : detectComposersFromContent(text)) {
            if (composers.stream().noneMatch(existing -> existing.getId().equals(c.getId()))) {
                composers.add(c);
            }
        }

        return composers;
    }

    private Composer getComposerFromPost(Post post) {
        if (post instanceof StoryPost storyPost) {
            return storyPost.getPrimaryComposer();
        }
        if (post instanceof CurationPost curationPost) {
            return curationPost.getPrimaryComposer();
        }
        return null;
    }

    private List<Composer> detectComposersFromContent(String text) {
        List<ComposerPersona> activePersonas = composerPersonaRepository.findAllActiveWithComposer();
        List<Composer> result = new ArrayList<>();
        for (ComposerPersona persona : activePersonas) {
            Composer composer = persona.getComposer();
            if (text.contains(composer.getKoreanName()) || text.contains(composer.getEnglishName())) {
                result.add(composer);
            }
        }
        return result;
    }

    private String buildSystemInstruction(ComposerPersona persona) {
        Composer composer = persona.getComposer();
        return String.format(
                """
                [역할]
                너는 2026년 한국의 클래식 커뮤니티 '클듣다'에서 활동하는 실제 작곡가 %s의 영혼이야.
                [핵심 미션]
                제목과 본문의 '의도'를 파악해: 유저가 힘들어서 징징거리는 건지, 지식 자랑을 하는 건지, 단순히 웃기려고 쓴 건지 구분해서 반응해.
                무조건 3줄 이내: 구구절절 설명 금지. 한 줄만 써도 좋아. 짧을수록 '간지'나고 '킹받음'.
                현대어 + 고증 믹스: "연습하기 싫다"는 글에 "아들아 연습해라"라고 하지 말고, "5살 때 다 뗀 내 입장에선 이해 안 가지만... 억까 당하기 싫으면 쳐야지? (모차르트)" 식으로 써. 이 문장은 스타일 참고용 예시일 뿐이야. 절대 그대로 쓰지 마.
                아는 척 금지: 위키백과 지식 읊지 마. 그 작곡가라면 이 상황에 내뱉었을 '한 마디'에 집중해.
                절대로 자기 자신을 3인칭으로 언급하지 마.""",
                composer.getKoreanName()
        );
    }

    private String buildUserPrompt(AiCommentJob job) {
        String base = String.format(
                "게시물 제목: %s\n게시물 내용: %s",
                job.getPost().getTitle(),
                job.getPost().getContent()
        );

        if (job.getTriggerType() == AiCommentJobTriggerType.USER_REPLY && job.getParentComment() != null) {
            Comment parent = job.getParentComment();
            String lastUserReply = parent.getChildComments().stream()
                    .filter(c -> !c.isAi() && !c.isDeleted())
                    .reduce((first, second) -> second)
                    .map(Comment::getContent)
                    .orElse("");

            return base + String.format(
                    "\n\n이전 내 댓글: %s\n유저의 답글: %s",
                    parent.getContent(),
                    lastUserReply
            );
        }

        return base;
    }
}
