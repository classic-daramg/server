package com.daramg.server.user.dto;

import com.daramg.server.user.domain.User;
import com.daramg.server.user.domain.UserRole;

public record UserProfileResponseDto(
        String profileImage,
        String nickname,
        String bio,
        String email,
        UserRole role
) {
    public static UserProfileResponseDto from(User user) {
        return new UserProfileResponseDto(
                user.getProfileImage(),
                user.getNickname(),
                user.getBio(),
                user.getEmail(),
                user.getRole()
        );
    }
}
