package com.daramg.server.post.repository;

import com.daramg.server.post.domain.PostScrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PostScrapRepository extends JpaRepository<PostScrap, Long> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    void deleteByPostIdAndUserId(Long postId, Long userId);

    @Query("select ps.post.id from PostScrap ps where ps.post.id in :postIds and ps.user.id = :userId")
    Set<Long> findPostIdsByPostIdsAndUserId(@Param("postIds") List<Long> postIds, @Param("userId") Long userId);
}
