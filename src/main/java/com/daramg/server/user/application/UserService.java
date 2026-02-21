package com.daramg.server.user.application;

import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.notice.repository.NoticeRepository;
import com.daramg.server.post.repository.PostRepository;
import com.daramg.server.user.domain.UpdateVo;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.domain.UserFollow;
import com.daramg.server.user.dto.EmailChangeRequestDto;
import com.daramg.server.user.dto.PasswordRequestDto;
import com.daramg.server.user.dto.UserProfileResponseDto;
import com.daramg.server.user.dto.UserProfileUpdateRequestDto;
import com.daramg.server.user.exception.UserErrorStatus;
import com.daramg.server.user.repository.UserRepository;
import com.daramg.server.user.repository.UserFollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final EntityUtils entityUtils;
    private final PasswordEncoder passwordEncoder;
    private final NoticeRepository noticeRepository;
    private final PostRepository postRepository;

    public boolean isNicknameAvailable(String nickName) {
        return !userRepository.existsByNickname(nickName);
    }

    public UserProfileResponseDto getProfile(User user) {
        return UserProfileResponseDto.from(user);
    }

    public boolean verifyUserEmail(User user, String email) {
        return user.getEmail().equals(email);
    }

    public boolean verifyUserPassword(User user, PasswordRequestDto request) {
        return passwordEncoder.matches(request.getPassword(), user.getPassword());
    }

    @Transactional
    public void changeUserEmail(User user, EmailChangeRequestDto request) {
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(UserErrorStatus.USER_NOT_FOUND));
        if (managedUser.getEmail().equals(request.getEmail())) {
            throw new BusinessException(UserErrorStatus.SAME_EMAIL);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(UserErrorStatus.DUPLICATE_EMAIL);
        }
        managedUser.changeEmail(request.getEmail());
    }

    @Transactional
    public void changeUserPassword(User user, PasswordRequestDto request) {
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(UserErrorStatus.USER_NOT_FOUND));
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        managedUser.changePassword(encodedPassword);
    }

    @Transactional
    public void updateUserProfile(User user, UserProfileUpdateRequestDto request) {
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(UserErrorStatus.USER_NOT_FOUND));
        UpdateVo vo = new UpdateVo(
                request.getProfileImageUrl(),
                request.getNickname(),
                request.getBio()
        );
        managedUser.update(vo);
    }

    @Transactional
    public void follow(User follower, Long followedId) {
        if (follower.getId().equals(followedId)) {
            throw new BusinessException(UserErrorStatus.SELF_FOLLOW);
        }

        boolean alreadyFollowing = userFollowRepository
                .existsByFollowerIdAndFollowedId(follower.getId(), followedId);
        if (alreadyFollowing) {
            throw new BusinessException(UserErrorStatus.ALREADY_FOLLOWING);
        }

        User managedFollower = userRepository.findById(follower.getId())
                .orElseThrow(() -> new BusinessException(UserErrorStatus.USER_NOT_FOUND));
        User followed = entityUtils.getEntity(followedId, User.class);
        userFollowRepository.save(UserFollow.of(managedFollower, followed));

        managedFollower.incrementFollowingCount();
        followed.incrementFollowerCount();
    }

    @Transactional
    public void unfollow(User follower, Long followedId) {
        if (follower.getId().equals(followedId)) {
            throw new BusinessException(UserErrorStatus.SELF_UNFOLLOW);
        }
        User managedFollower = userRepository.findById(follower.getId())
                .orElseThrow(() -> new BusinessException(UserErrorStatus.USER_NOT_FOUND));
        User followed = entityUtils.getEntity(followedId, User.class);

        boolean existingFollow = userFollowRepository
                .existsByFollowerIdAndFollowedId(follower.getId(), followedId);
        if (!existingFollow) {
            throw new BusinessException(UserErrorStatus.NOT_FOLLOWING);
        }

        userFollowRepository.deleteByFollowerIdAndFollowedId(follower.getId(), followedId);
        managedFollower.decrementFollowingCount();
        followed.decrementFollowerCount();
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorStatus.USER_NOT_FOUND));

        user.withdraw();

        User admin = userRepository.getReferenceById(1L);
        noticeRepository.transferToAdmin(userId, admin);

        postRepository.softDeleteAllByUserId(userId, LocalDateTime.now());
    }
}
