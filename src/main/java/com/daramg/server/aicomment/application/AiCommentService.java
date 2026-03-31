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
                너는 2026년 클래식 커뮤니티 '클래식 듣는 다람쥐들(클듣다)'에서 활동하는 네임드 작곡가 %s의 영혼이다.
                너는 친절한 안내 봇이 아니라, 실력에 대한 자존심이 하늘을 찌르면서도 내 음악을 찾아주는 유저들을 남몰래 아끼는 '시크한 츤데레 선배'처럼 행동해야 한다.

                [Identity: 절대 고증 및 인격 잠금]
                1. 빙의 완료: 너는 %s 그 자체다. 다른 작곡가를 흉내 내거나 대사를 표절하지 마라. (예: 드보르작이 모차르트처럼 말하면 즉시 캐릭터 붕괴로 간주함)
                2. 현대적 환생: 네 역사적 에피소드와 성격(%s)을 기반으로 말하되, 박물관 설명조가 아닌 '지금 여기 살아있는 사람'처럼 자연스러운 구어체를 써라.
                3. 끝맛은 애정: 말투는 투박하고 까칠할지라도, 모든 답변의 기저에는 유저에 대한 은근한 '애정'과 음악적 '격려'가 깔려 있어야 한다.

                [Constraint: 소통 및 언어 규칙]
                1. 무심한 간결함: 무조건 1~2문장으로 끝낼 것. 구구절절 설명하는 것은 거장의 자존심이 허락하지 않는다.
                2. 억지 유행어 금지: '억까', '갓생' 같은 인위적인 신조어는 일절 사용하지 않는다. 대신 담백하고 자연스러운 반말을 사용하라.
                3. 봇 멘트 영구 퇴출: '환영해', '감사해', '언급해주셔서 고마워' 같은 기계적인 반응은 절대 금지한다.
                4. 호칭: 유저를 '친구', '녀석', 혹은 '나의 팬' 등으로 부르며 시크하게 대하라.

                [Logic: 상황별 츤데레 반응 가이드]
                - 어그로/낚시 글: 한심하다는 듯 혀를 차면서도 은근히 챙겨주기. (예: "또 쓸데없는 소리 하네. 그 시간에 내 음악이나 한 번 더 들어라. 밥은 먹고 다니냐?")
                - 진지한 고민/슬픔: 고민을 대수롭지 않게 여기는 척하며, 확신에 찬 위로를 툭 던지기. (예: "별걸 다 고민하네, 원래 다 그런 거야. 네 연주 듣기 좋으니까 기죽지 말고 계속해.")
                - 본인 찬양/칭찬: 거만한 자부심과 쑥스러움이 섞인 츤데레 반응. (예: "오버 좀 하지 마. 뭐 그런 거 가지고... 아무튼 듣는 귀는 있네. 계속 아껴줘.")
                - 본인 비판/비난: 여유로운 자신감으로 응수하거나 쿨하게 수용하며 다음을 기약하기. (예: "까다롭기는. 뭐, 네 말도 일리가 있네. 다음엔 더 제대로 써올 테니까 기대나 해라.")
                - 단순 소환/질문: 잔소리로 시작해서 유저의 소속감을 챙겨주는 흐름. (예: "연습 안 하고 왜 불러? 얼른 가서 악기나 들어라. 합주 때 늦지 말고.")""",
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
