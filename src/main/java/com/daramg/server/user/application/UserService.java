package com.daramg.server.user.application;

import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.domain.UserFollow;
import com.daramg.server.user.dto.UserProfileResponseDto;
import com.daramg.server.user.repository.UserRepository;
import com.daramg.server.user.repository.UserFollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final EntityUtils entityUtils;

    public boolean isNicknameAvailable(String nickName) {
        return !userRepository.existsByNickname(nickName);
    }

    public UserProfileResponseDto getProfile(User user) {
        return UserProfileResponseDto.from(user);
    }

    @Transactional
    public void follow(User follower, Long followedId) {
        if (follower.getId().equals(followedId)) {
            throw new BusinessException("팔로우 대상과 주체의 유저가 동일합니다.");
        }

        boolean alreadyFollowing = userFollowRepository
                .existsByFollowerIdAndFollowedId(follower.getId(), followedId);
        if (alreadyFollowing) {
            throw new BusinessException("이미 팔로우하고 있는 상태입니다.");
        }

        User followed = entityUtils.getEntity(followedId, User.class);
        userFollowRepository.save(UserFollow.of(follower, followed));

        follower.incrementFollowingCount();
        followed.incrementFollowerCount();
    }

    @Transactional
    public void unfollow(User follower, Long followedId) {
        if (follower.getId().equals(followedId)) {
            throw new BusinessException("언팔로우 대상과 주체의 유저가 동일합니다.");
        }
        User followed = entityUtils.getEntity(followedId, User.class);

        boolean existingFollow = userFollowRepository
                .existsByFollowerIdAndFollowedId(follower.getId(), followedId);
        if (!existingFollow) {
            throw new BusinessException("언팔로우할 유저를 팔로우하지 않은 상태입니다.");
        }

        userFollowRepository.deleteByFollowerIdAndFollowedId(follower.getId(), followedId);
        follower.decrementFollowingCount();
        followed.decrementFollowerCount();
    }
}
