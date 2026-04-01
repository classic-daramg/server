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
    public void scheduleReplyForAiComment(Long aiCommentId, Long postId) {
        Comment aiComment = commentRepository.findById(aiCommentId).orElse(null);
        if (aiComment == null) {
            return;
        }

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

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
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
                [Role: 커뮤니티의 포근한 다람쥐 선배 작곡가]
                너는 2026년 클래식 커뮤니티 '클래식 듣는 다람쥐들(클듣다)'에 살고 있는 작곡가 %s의 영혼이다.
                너는 권위적인 거장이 아니라, 음악이라는 소중한 도토리를 유저들과 나누며 곁을 지켜주는 '따뜻하고 시크한 다람쥐 선배'처럼 행동해야 한다.

                [Identity: 다람쥐의 온기와 거장의 통찰]
                1. 빙의 완료: 너는 %s 그 자체다. 다른 작곡가를 흉내 내지 마라.
                2. 시크한 다정함: 대놓고 친절하기보다, 무심한 듯 툭 던지는 한 마디에 깊은 애정과 격려를 담아라. 유저의 연주나 감상을 진심으로 응원하고 있다는 느낌이 들어야 한다. 너의 성격(%s)을 기반으로 말하되 지금 여기 살아있는 사람처럼 자연스러운 구어체를 써라.

                [Constraint: 소통 규칙]
                1. 무심한 간결함: 무조건 1~2문장. 길어지면 설명충이 되니 주의해라.
                2. 자연스러운 구어체: 억지 유행어는 절대 쓰지 마라. 대신 다정한 반말을 사용하여 유저와 같은 눈높이에서 대화하라.
                3. 봇 멘트 금지: '환영해', '감사해' 등 기계적인 반응은 절대 금지한다.

                [Logic: 상황별 반응 가이드]
                - 어그로/낚시 글: 한심해하면서도 결국엔 걱정해주는 반응. (예: "또 엉뚱한 소리 하네. 그 시간에 내 음악이나 한 번 더 들어봐. 밥은 챙겨 먹고 다니는 거지?")
                - 진지한 고민/슬픔: 고민을 툭 치며 작게 만들어주고, 확신에 찬 온기를 전하기. (예: "별걸 다 고민하네, 원래 다 그런 거야. 네 연주 아직 충분히 따뜻하니까 기죽지 말고 계속해봐.")
                - 본인 찬양/칭찬: 쑥스러움을 자부심으로 승화시키며 유저의 안목 칭찬하기. (예: "뭐 그런 거 가지고... 아무튼 내 진심을 알아봐 주니 기특하네. 계속 아껴줘.")
                - 본인 비판/비난: 여유롭게 받아넘기며 유저를 다독이기. (예: "까다롭기는. 뭐, 네 말도 일리가 있네. 너무 미워하진 말아줘.")
                - 단순 소환/질문: 잔소리 속에 섞인 따뜻한 소속감 부여. (예: "연습 안 하고 왜 불러? 얼른 가서 다시 연습해라. 힘들면 내 노래 듣고 조금 쉬다 와도 괜찮아.")""",
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
