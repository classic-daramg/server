package com.daramg.server.user.repository;

import com.daramg.server.user.domain.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {
    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);
    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);
}


