package com.daramg.server.post.repository;

import com.daramg.server.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Post p
        set p.isDeleted = true,
            p.deletedAt = :now,
            p.updatedAt = :now
        where p.user.id = :userId
          and p.isDeleted = false
        """)
    int softDeleteAllByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}

