package com.daramg.server.aicomment.repository;

import com.daramg.server.aicomment.domain.AiCommentSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiCommentSettingsRepository extends JpaRepository<AiCommentSettings, Long> {
}
