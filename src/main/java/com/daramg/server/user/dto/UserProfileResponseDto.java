package com.daramg.server.user.dto;

import com.daramg.server.user.domain.User;

public record UserProfileResponseDto(
        String profileImage,
        String nickname,
        String bio
) {
    public static UserProfileResponseDto from(User user) {
        return new UserProfileResponseDto(
                user.getProfileImage(),
                user.getNickname(),
                user.getBio()
        );
    }
}
