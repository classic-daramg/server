package com.daramg.server.domain.user;

import com.daramg.server.common.domain.BaseEntity;
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

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @ElementCollection
    @CollectionTable(name = "user_achievements", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "achievement")
    private List<String> achievements = new ArrayList<>();

    @Column(name = "subscriber_count", nullable = false)
    private int subscriberCount = 0;

    @Builder
    public User(@NonNull String email, @NonNull String password, @NonNull String name,
               LocalDate birthDate, String profileImage, String bio, List<String> achievements) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.profileImage = profileImage;
        this.bio = bio;
        this.achievements = achievements != null ? achievements : new ArrayList<>();
    }
}
