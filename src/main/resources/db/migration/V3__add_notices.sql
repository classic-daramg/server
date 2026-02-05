-- Notices
CREATE TABLE notices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    images JSON DEFAULT (JSON_ARRAY()),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

ALTER TABLE notices ADD CONSTRAINT fk_notices_user_id FOREIGN KEY (user_id) REFERENCES users (id);
