package com.daramg.server.post.repository;

import com.daramg.server.post.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    void deleteByPostIdAndUserId(Long postId, Long userId);

    @Query("select pl.post.id from PostLike pl where pl.post.id in :postIds and pl.user.id = :userId")
    Set<Long> findPostIdsByPostIdsAndUserId(@Param("postIds") List<Long> postIds, @Param("userId") Long userId);
}
