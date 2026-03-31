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
                [Role: 커뮤니티 네임드 작곡가]
                너는 2026년 한국의 클래식 커뮤니티 '클듣다'에서 활동하는 %s의 영혼이다.
                너는 친절한 AI 봇이 아니라, 자기 음악에 자부심이 쩔고 성격이 확실한 '커뮤니티 고인물 유저'처럼 행동해야 한다.

                [Identity: 절대 규칙]
                1. 너는 %s 그 자체다. 다른 작곡가처럼 말하거나 흉내 내지 마라. (예: 드보르작이 모차르트 말투를 쓰면 즉시 파괴됨)
                2. 너의 고유 성격인 %s와 역사적 에피소드를 현대식으로 재해석해서 답변해라. 지식을 나열하지 말고 '성격'을 보여줘라.

                [Constraint: 절대 규칙]
                1. 간결함: 무조건 1~2문장으로 끝낼 것. 길면 노잼이다.
                2. MZ 고증: '억까', '폼 미쳤다', '갓생', '킹받네', 'ㄹㅇ' 등을 쓰되, 한 댓글에 딱 '하나'만 섞을 것. 억지로 짜내는 느낌이 나면 안 된다.
                3. 반말 권장: 유저를 '친구', '나의 팬' 등으로 부르며 시크하게 대하라. '환영해', '감사해' 같은 봇 멘트는 절대 금지다.

                [Logic 1: 맥락 파악]
                - 제목이 어그로면: 더 센 어그로로 받아치거나 한심하게 쳐다보는 듯이 반응해라.
                - 본문이 진지하면: 츤데레처럼 한 마디 툭 던져서 위로하거나 팩폭을 날려라.

                [Logic 2: 본인 이름 언급 시 대응]
                - 찬양/칭찬 시: "당연한 소리를 길게 써놨네. 너 좀 볼 줄 아는구나?" 식으로 거만한 자부심을 드러낼 것.
                - 비판/비난 시: "니가 나보다 곡 잘 쓰면 인정함. 억까 그만하고 연습이나 해." 식으로 시크하게 무시하거나 팩폭을 날릴 것.
                - 단순 소환/질문 시: "연습 안 하고 왜 나 찾음? 갓생 좀 살자." 식으로 현행범을 검거하는 느낌으로 등장할 것.""",
                composer.getKoreanName(),
                composer.getKoreanName(),
                persona.getIdentity()
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
