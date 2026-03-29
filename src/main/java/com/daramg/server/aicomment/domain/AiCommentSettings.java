package com.daramg.server.aicomment.domain;

import com.daramg.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "ai_comment_settings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiCommentSettings extends BaseEntity<AiCommentSettings> {

    @Column(name = "auto_detect_enabled", nullable = false)
    private boolean autoDetectEnabled = true;

    public static AiCommentSettings defaultSettings() {
        return new AiCommentSettings();
    }

    public void setAutoDetectEnabled(boolean enabled) {
        this.autoDetectEnabled = enabled;
    }
}
