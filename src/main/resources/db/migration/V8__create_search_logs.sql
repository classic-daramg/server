CREATE TABLE search_logs (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword    VARCHAR(255) NOT NULL,
    searched_at DATETIME    NOT NULL,
    user_id    BIGINT       NULL
);
