package com.daramg.server.user.domain;

import com.daramg.server.common.domain.BaseEntity;
import com.daramg.server.auth.domain.SignupVo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

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

    //TODO: 기본 이미지 저장
    private static final String DEFAULT_PROFILE_IMAGE_URL = "https://default-image.png";

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
    }

    public static User from(SignupVo vo){
        String profileImage = (vo.getProfileImage() == null || vo.getProfileImage().isBlank())
                ? DEFAULT_PROFILE_IMAGE_URL
                : vo.getProfileImage();

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

    //TODO: 비밀번호 암호화 추가(PasswordEncoder)
    public void changePassword(String password){
        this.password = password;
    }

    public void incrementFollowingCount() {
        this.followingCount = this.followingCount + 1;
    }

    public void decrementFollowingCount() {
        if (this.followingCount > 0) {
            this.followingCount = this.followingCount - 1;
        }
    }

    public void incrementFollowerCount() {
        this.followerCount = this.followerCount + 1;
    }

    public void decrementFollowerCount() {
        if (this.followerCount > 0) {
            this.followerCount = this.followerCount - 1;
        }
    }
}
