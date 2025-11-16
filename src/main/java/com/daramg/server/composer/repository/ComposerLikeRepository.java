package com.daramg.server.composer.repository;

import com.daramg.server.composer.domain.ComposerLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface ComposerLikeRepository extends JpaRepository<ComposerLike, Long> {
    boolean existsByComposerIdAndUserId(Long composerId, Long userId);

    void deleteByComposerIdAndUserId(Long composerId, Long userId);

    @Query("select cl.composer.id from ComposerLike cl where cl.user.id = :userId")
    Set<Long> findComposerIdsByUserId(@Param("userId") Long userId);
}
