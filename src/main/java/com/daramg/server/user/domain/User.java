package com.daramg.server.user.domain;

import com.daramg.server.common.domain.BaseEntity;
import com.daramg.server.auth.domain.SignupVo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity<User> {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @ElementCollection
    @CollectionTable(name = "user_achievements", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "achievement")
    private List<String> achievements = new ArrayList<>();

    @Column(name = "following_count", nullable = false)
    private int followingCount = 0;

    @Column(name = "follower_count", nullable = false)
    private int followerCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    private UserStatus userStatus;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Builder
    public User(@NonNull String email, @NonNull String password, @NonNull String name,
                @NonNull LocalDate birthDate, String profileImage, @NonNull String nickname,
                String bio, List<String> achievements) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.profileImage = profileImage;
        this.nickname = nickname;
        this.bio = bio;
        this.achievements = achievements != null ? achievements : new ArrayList<>();
        this.userStatus = UserStatus.ACTIVE;
    }

    public static User from(SignupVo vo){
        String profileImage = vo.getProfileImage();

        return User.builder()
                .email(vo.getEmail())
                .name(vo.getName())
                .birthDate(vo.getBirthDate())
                .password(vo.getPassword())
                .profileImage(profileImage)
                .nickname(vo.getNickname())
                .bio(vo.getBio())
                .build();
    }

    public void update(UpdateVo vo){
        this.nickname = vo.getNickname();
        this.bio = vo.getBio();
        this.profileImage = vo.getProfileImage();
    }

    public void changePassword(String password){
        this.password = password;
    }

    public void incrementFollowingCount() {
        followingCount++;
    }

    public void decrementFollowingCount() {
        if (followingCount > 0) {
            followingCount--;
        }
    }

    public void incrementFollowerCount() {
        followerCount++;
    }

    public void decrementFollowerCount() {
        if (followerCount > 0) {
            followerCount--;
        }
    }

    public void withdraw(){
        if (this.userStatus == UserStatus.DELETED) return;

        userStatus = UserStatus.DELETED;
        this.deletedAt = Instant.now();
    }

    public boolean isActive() {
        return userStatus == UserStatus.ACTIVE;
    }

    public void changeEmail(String email) {
        this.email = email;
    }
}
