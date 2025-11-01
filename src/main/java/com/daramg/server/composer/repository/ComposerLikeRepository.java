package com.daramg.server.composer.repository;

import com.daramg.server.composer.domain.ComposerLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface ComposerLikeRepository extends JpaRepository<ComposerLike, Long> {
    boolean existsByComposerIdAndUserId(Long composerId, Long userId);
    void deleteByComposerIdAndUserId(Long composerId, Long userId);
    Set<Long> findComposerIdsByUserId(Long userId);
}
