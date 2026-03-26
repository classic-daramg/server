-- AI 봇 시스템 유저
INSERT INTO users (email, password, name, birth_date, nickname, following_count, follower_count, user_status, role, created_at, updated_at)
VALUES ('ai-bot@classicaldaramz.com', 'LOCKED', 'AI', '2000-01-01', 'ai_bot', 0, 0, 'ACTIVE', 'USER', NOW(), NOW());

-- 작곡가 페르소나 테이블
CREATE TABLE composer_personas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    composer_id BIGINT NOT NULL UNIQUE,
    identity TEXT NOT NULL,
    mission TEXT NOT NULL,
    constraints_text TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_composer_personas_composer_id FOREIGN KEY (composer_id) REFERENCES composers(id)
);

-- AI 댓글 잡 큐 테이블
CREATE TABLE ai_comment_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    composer_id BIGINT NOT NULL,
    trigger_type VARCHAR(20) NOT NULL,
    parent_comment_id BIGINT,
    scheduled_at DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_ai_comment_jobs_post_id FOREIGN KEY (post_id) REFERENCES posts(id),
    CONSTRAINT fk_ai_comment_jobs_composer_id FOREIGN KEY (composer_id) REFERENCES composers(id),
    CONSTRAINT fk_ai_comment_jobs_parent_comment_id FOREIGN KEY (parent_comment_id) REFERENCES comments(id)
);

-- comments 테이블에 AI 관련 컬럼 추가
ALTER TABLE comments
    ADD COLUMN is_ai BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN ai_reply_count TINYINT NOT NULL DEFAULT 0,
    ADD COLUMN composer_id BIGINT,
    ADD CONSTRAINT fk_comments_ai_composer_id FOREIGN KEY (composer_id) REFERENCES composers(id);
