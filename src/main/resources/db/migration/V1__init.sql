-- Users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL,
    profile_image VARCHAR(255),
    nickname VARCHAR(255) NOT NULL UNIQUE,
    bio TEXT,
    following_count INT NOT NULL DEFAULT 0,
    follower_count INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE TABLE user_achievements (
    user_id BIGINT NOT NULL,
    achievement VARCHAR(255),
    PRIMARY KEY (user_id, achievement)
);

ALTER TABLE user_achievements ADD CONSTRAINT fk_user_achievements_user_id FOREIGN KEY (user_id) REFERENCES users (id);

CREATE TABLE user_follows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    followed_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uc_follower_followed UNIQUE (follower_id, followed_id)
);

ALTER TABLE user_follows ADD CONSTRAINT fk_user_follows_follower_id FOREIGN KEY (follower_id) REFERENCES users (id);
ALTER TABLE user_follows ADD CONSTRAINT fk_user_follows_followed_id FOREIGN KEY (followed_id) REFERENCES users (id);

-- Composers
CREATE TABLE composers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    korean_name VARCHAR(255) NOT NULL,
    english_name VARCHAR(255) NOT NULL,
    native_name VARCHAR(255),
    gender VARCHAR(255) NOT NULL,
    nationality VARCHAR(255),
    birth_year SMALLINT,
    death_year SMALLINT,
    bio VARCHAR(255),
    era VARCHAR(255),
    continent VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

-- Posts
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    TYPE VARCHAR(31) NOT NULL,
    user_id BIGINT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    video_url VARCHAR(255),
    post_status VARCHAR(255) NOT NULL,
    like_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    composer_id BIGINT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

ALTER TABLE posts ADD CONSTRAINT fk_posts_user_id FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE posts ADD CONSTRAINT fk_posts_composer_id FOREIGN KEY (composer_id) REFERENCES composers (id);

CREATE TABLE post_images (
    post_id BIGINT NOT NULL,
    image_url VARCHAR(255),
    PRIMARY KEY (post_id, image_url)
);

ALTER TABLE post_images ADD CONSTRAINT fk_post_images_post_id FOREIGN KEY (post_id) REFERENCES posts (id);

CREATE TABLE post_hashtags (
    post_id BIGINT NOT NULL,
    hashtag VARCHAR(255),
    PRIMARY KEY (post_id, hashtag)
);

ALTER TABLE post_hashtags ADD CONSTRAINT fk_post_hashtags_post_id FOREIGN KEY (post_id) REFERENCES posts (id);

CREATE TABLE composer_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    composer_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uc_composer_post UNIQUE (composer_id, post_id)
);

ALTER TABLE composer_posts ADD CONSTRAINT fk_composer_posts_composer_id FOREIGN KEY (composer_id) REFERENCES composers (id);
ALTER TABLE composer_posts ADD CONSTRAINT fk_composer_posts_post_id FOREIGN KEY (post_id) REFERENCES posts (id);

CREATE TABLE curation_post_additional_composers (
    post_id BIGINT NOT NULL,
    composer_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, composer_id)
);

ALTER TABLE curation_post_additional_composers ADD CONSTRAINT fk_cpac_post_id FOREIGN KEY (post_id) REFERENCES posts (id);
ALTER TABLE curation_post_additional_composers ADD CONSTRAINT fk_cpac_composer_id FOREIGN KEY (composer_id) REFERENCES composers (id);

-- Comments
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    parent_comment_id BIGINT,
    like_count INT NOT NULL DEFAULT 0,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

ALTER TABLE comments ADD CONSTRAINT fk_comments_post_id FOREIGN KEY (post_id) REFERENCES posts (id);
ALTER TABLE comments ADD CONSTRAINT fk_comments_user_id FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE comments ADD CONSTRAINT fk_comments_parent_comment_id FOREIGN KEY (parent_comment_id) REFERENCES comments (id);

-- Likes & Scraps
CREATE TABLE composer_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    composer_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uc_composer_user_like UNIQUE (composer_id, user_id)
);

ALTER TABLE composer_likes ADD CONSTRAINT fk_composer_likes_composer_id FOREIGN KEY (composer_id) REFERENCES composers (id);
ALTER TABLE composer_likes ADD CONSTRAINT fk_composer_likes_user_id FOREIGN KEY (user_id) REFERENCES users (id);

CREATE TABLE post_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uc_post_user_like UNIQUE (post_id, user_id)
);

ALTER TABLE post_likes ADD CONSTRAINT fk_post_likes_post_id FOREIGN KEY (post_id) REFERENCES posts (id);
ALTER TABLE post_likes ADD CONSTRAINT fk_post_likes_user_id FOREIGN KEY (user_id) REFERENCES users (id);

CREATE TABLE post_scraps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uc_post_user_scrap UNIQUE (post_id, user_id)
);

ALTER TABLE post_scraps ADD CONSTRAINT fk_post_scraps_post_id FOREIGN KEY (post_id) REFERENCES posts (id);
ALTER TABLE post_scraps ADD CONSTRAINT fk_post_scraps_user_id FOREIGN KEY (user_id) REFERENCES users (id);

CREATE TABLE comment_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uc_comment_user_like UNIQUE (comment_id, user_id)
);

ALTER TABLE comment_likes ADD CONSTRAINT fk_comment_likes_comment_id FOREIGN KEY (comment_id) REFERENCES comments (id);
ALTER TABLE comment_likes ADD CONSTRAINT fk_comment_likes_user_id FOREIGN KEY (user_id) REFERENCES users (id);

-- Reports
CREATE TABLE reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    post_id BIGINT,
    comment_id BIGINT,
    reporter_id BIGINT NOT NULL,
    report_reason VARCHAR(255) NOT NULL,
    report_content TEXT,
    is_processed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

ALTER TABLE reports ADD CONSTRAINT fk_reports_post_id FOREIGN KEY (post_id) REFERENCES posts (id);
ALTER TABLE reports ADD CONSTRAINT fk_reports_comment_id FOREIGN KEY (comment_id) REFERENCES comments (id);
ALTER TABLE reports ADD CONSTRAINT fk_reports_reporter_id FOREIGN KEY (reporter_id) REFERENCES users (id);
