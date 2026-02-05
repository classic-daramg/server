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
import com.daramg.server.user.repository.UserRepository;
import com.daramg.server.user.repository.UserFollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.daramg.server.common.exception.CommonErrorStatus.NOT_FOUND;

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
                .orElseThrow(() -> new BusinessException("유저를 찾을 수 없습니다."));
        if (managedUser.getEmail().equals(request.getEmail())){
            throw new BusinessException("기존 이메일과 동일한 이메일로 변경할 수 없습니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())){
            throw new BusinessException("이미 가입되어 있는 이메일입니다.");
        }
        managedUser.changeEmail(request.getEmail());
    }

    @Transactional
    public void changeUserPassword(User user, PasswordRequestDto request) {
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException("유저를 찾을 수 없습니다."));
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        managedUser.changePassword(encodedPassword);
    }

    @Transactional
    public void updateUserProfile(User user, UserProfileUpdateRequestDto request){
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException("유저를 찾을 수 없습니다."));
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
            throw new BusinessException("팔로우 대상과 주체의 유저가 동일합니다.");
        }

        boolean alreadyFollowing = userFollowRepository
                .existsByFollowerIdAndFollowedId(follower.getId(), followedId);
        if (alreadyFollowing) {
            throw new BusinessException("이미 팔로우하고 있는 상태입니다.");
        }

        User managedFollower = userRepository.findById(follower.getId())
                .orElseThrow(() -> new BusinessException("유저를 찾을 수 없습니다."));
        User followed = entityUtils.getEntity(followedId, User.class);
        userFollowRepository.save(UserFollow.of(managedFollower, followed));

        managedFollower.incrementFollowingCount();
        followed.incrementFollowerCount();
    }

    @Transactional
    public void unfollow(User follower, Long followedId) {
        if (follower.getId().equals(followedId)) {
            throw new BusinessException("언팔로우 대상과 주체의 유저가 동일합니다.");
        }
        User managedFollower = userRepository.findById(follower.getId())
                .orElseThrow(() -> new BusinessException("유저를 찾을 수 없습니다."));
        User followed = entityUtils.getEntity(followedId, User.class);

        boolean existingFollow = userFollowRepository
                .existsByFollowerIdAndFollowedId(follower.getId(), followedId);
        if (!existingFollow) {
            throw new BusinessException("언팔로우할 유저를 팔로우하지 않은 상태입니다.");
        }

        userFollowRepository.deleteByFollowerIdAndFollowedId(follower.getId(), followedId);
        managedFollower.decrementFollowingCount();
        followed.decrementFollowerCount();
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new BusinessException(NOT_FOUND));

        user.withdraw();

        User admin = userRepository.getReferenceById(1L);
        noticeRepository.transferToAdmin(userId, admin);

        postRepository.softDeleteAllByUserId(userId, LocalDateTime.now());
    }
}
