CREATE TABLE notifications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    receiver_id BIGINT       NOT NULL,
    sender_id   BIGINT       NOT NULL,
    post_id     BIGINT       NOT NULL,
    type        VARCHAR(20)  NOT NULL,
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,

    CONSTRAINT fk_notification_receiver FOREIGN KEY (receiver_id) REFERENCES users (id),
    CONSTRAINT fk_notification_sender   FOREIGN KEY (sender_id)   REFERENCES users (id),
    CONSTRAINT fk_notification_post     FOREIGN KEY (post_id)     REFERENCES posts (id),

    INDEX idx_notification_receiver_created (receiver_id, created_at),
    INDEX idx_notification_receiver_unread  (receiver_id, is_read)
);
