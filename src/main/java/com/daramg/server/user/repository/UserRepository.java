package com.daramg.server.user.repository;

import com.daramg.server.user.domain.User;
import com.daramg.server.user.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndUserStatus(String email, UserStatus userStatus);
}
