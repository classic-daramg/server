package com.daramg.server.user.application;

import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.common.exception.NotFoundException;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.dto.EmailChangeRequestDto;
import com.daramg.server.user.dto.PasswordRequestDto;
import com.daramg.server.user.dto.UserProfileResponseDto;
import com.daramg.server.user.dto.UserProfileUpdateRequestDto;
import com.daramg.server.user.repository.UserRepository;
import com.daramg.server.user.repository.UserFollowRepository;
import com.daramg.server.testsupport.support.ServiceTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserServiceTest extends ServiceTestSupport {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFollowRepository userFollowRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User follower;
    private User followed;

    @BeforeEach
    void setUp() {
        String encodedPassword = passwordEncoder.encode("password");
        follower = new User("follower@email.com", encodedPassword, "팔로워", LocalDate.now(), "profile image", "팔로워닉네임", "bio", null);
        followed = new User("followed@email.com", encodedPassword, "팔로우당하는사람", LocalDate.now(), "profile image", "팔로우당하는닉네임", "bio", null);
        
        userRepository.save(follower);
        userRepository.save(followed);
    }

    @Nested
    @DisplayName("프로필 조회 테스트")
    class ProfileTest {
        @Test
        void 유저_프로필을_정상적으로_조회한다() {
            //given
            String profileImage = "https://example.com/profile.jpg";
            String nickname = "테스트닉네임";
            String bio = "테스트 소개글";
            User user = new User("test@email.com", "password", "테스트", LocalDate.now(), profileImage, nickname, bio, null);
            userRepository.save(user);

            //when
            UserProfileResponseDto result = userService.getProfile(user);

            //then
            assertThat(result.profileImage()).isEqualTo(profileImage);
            assertThat(result.nickname()).isEqualTo(nickname);
            assertThat(result.bio()).isEqualTo(bio);
        }

        @Test
        void 프로필_이미지와_소개글이_null인_유저의_프로필을_조회한다() {
            //given
            String nickname = "테스트닉네임";
            User user = new User("test@email.com", "password", "테스트", LocalDate.now(), null, nickname, null, null);
            userRepository.save(user);

            //when
            UserProfileResponseDto result = userService.getProfile(user);

            //then
            assertThat(result.profileImage()).isNull();
            assertThat(result.nickname()).isEqualTo(nickname);
            assertThat(result.bio()).isNull();
        }
    }

    @Nested
    @DisplayName("닉네임 중복 확인 테스트")
    class NicknameCheckTest {
        @Test
        void 사용가능한_닉네임이면_true를_반환한다() {
            //given
            String availableNickname = "사용가능한닉네임";

            //when
            boolean result = userService.isNicknameAvailable(availableNickname);

            //then
            assertThat(result).isTrue();
        }

        @Test
        void 이미_사용중인_닉네임이면_false를_반환한다() {
            //given
            String existingNickname = follower.getNickname();

            //when
            boolean result = userService.isNicknameAvailable(existingNickname);

            //then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("유저 이메일 검증 테스트")
    class VerifyUserEmailTest {
        @Test
        void 유저의_이메일과_입력한_이메일이_일치하면_true를_반환한다() {
            //given
            String userEmail = follower.getEmail();

            //when
            boolean result = userService.verifyUserEmail(follower, userEmail);

            //then
            assertThat(result).isTrue();
        }

        @Test
        void 유저의_이메일과_입력한_이메일이_일치하지_않으면_false를_반환한다() {
            //given
            String differentEmail = "different@email.com";

            //when
            boolean result = userService.verifyUserEmail(follower, differentEmail);

            //then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("유저 비밀번호 검증 테스트")
    class VerifyUserPasswordTest {
        @Test
        void 유저의_비밀번호와_입력한_비밀번호가_일치하면_true를_반환한다() {
            //given
            PasswordRequestDto request = new PasswordRequestDto("password");

            //when
            boolean result = userService.verifyUserPassword(follower, request);

            //then
            assertThat(result).isTrue();
        }

        @Test
        void 유저의_비밀번호와_입력한_비밀번호가_일치하지_않으면_false를_반환한다() {
            //given
            PasswordRequestDto request = new PasswordRequestDto("wrongPassword");

            //when
            boolean result = userService.verifyUserPassword(follower, request);

            //then
            assertThat(result).isFalse();
        }

        @Test
        void 인코딩된_비밀번호와_평문_비밀번호를_정상적으로_비교한다() {
            //given
            PasswordRequestDto request = new PasswordRequestDto("testPassword123!");

            String encodedPassword = passwordEncoder.encode(request.getPassword());
            User testUser = new User("test@email.com", encodedPassword, "테스트", LocalDate.now(), 
                    "profile image", "테스트닉네임", "bio", null);
            userRepository.save(testUser);

            //when
            boolean result = userService.verifyUserPassword(testUser, request);

            //then
            assertThat(result).isTrue();
            // 인코딩된 비밀번호와 평문 비밀번호가 다름을 확인
            assertThat(testUser.getPassword()).isNotEqualTo(request.getPassword());
        }
    }

    @Nested
    @DisplayName("유저 프로필 수정 테스트")
    class UpdateUserProfileTest {
        @Test
        @Transactional
        void 유저_프로필을_정상적으로_수정한다() {
            //given
            String newProfileImage = "https://example.com/new-profile.jpg";
            String newNickname = "새닉네임";
            String newBio = "새로운 소개글";
            UserProfileUpdateRequestDto request = new UserProfileUpdateRequestDto(
                    newProfileImage,
                    newNickname,
                    newBio
            );

            //when
            userService.updateUserProfile(follower, request);

            //then
            User updatedUser = userRepository.findById(follower.getId()).orElseThrow();
            assertThat(updatedUser.getProfileImage()).isEqualTo(newProfileImage);
            assertThat(updatedUser.getNickname()).isEqualTo(newNickname);
            assertThat(updatedUser.getBio()).isEqualTo(newBio);
        }

        @Test
        @Transactional
        void 프로필_이미지와_소개글이_null인_프로필을_수정한다() {
            //given
            String newNickname = "새닉네임";
            UserProfileUpdateRequestDto request = new UserProfileUpdateRequestDto(
                    null,
                    newNickname,
                    null
            );

            //when
            userService.updateUserProfile(follower, request);

            //then
            User updatedUser = userRepository.findById(follower.getId()).orElseThrow();
            assertThat(updatedUser.getProfileImage()).isNull();
            assertThat(updatedUser.getNickname()).isEqualTo(newNickname);
            assertThat(updatedUser.getBio()).isNull();
        }
    }

    @Nested
    @DisplayName("유저 이메일 변경 테스트")
    class ChangeUserEmailTest {
        @Test
        @Transactional
        void 유저_이메일을_정상적으로_변경한다() {
            //given
            String newEmail = "newemail@example.com";
            EmailChangeRequestDto request = new EmailChangeRequestDto(newEmail);

            //when
            userService.changeUserEmail(follower, request);

            //then
            User updatedUser = userRepository.findById(follower.getId()).orElseThrow();
            assertThat(updatedUser.getEmail()).isEqualTo(newEmail);
        }

        @Test
        @Transactional
        void 기존_이메일과_동일한_이메일로_변경하면_예외가_발생한다() {
            //given
            String sameEmail = follower.getEmail();
            EmailChangeRequestDto request = new EmailChangeRequestDto(sameEmail);

            //when & then
            assertThatThrownBy(() -> userService.changeUserEmail(follower, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("기존 이메일과 동일한 이메일로 변경할 수 없습니다.");
        }

        @Test
        @Transactional
        void 이미_가입되어_있는_이메일로_변경하면_예외가_발생한다() {
            //given
            String existingEmail = followed.getEmail();
            EmailChangeRequestDto request = new EmailChangeRequestDto(existingEmail);

            //when & then
            assertThatThrownBy(() -> userService.changeUserEmail(follower, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 가입되어 있는 이메일입니다.");
        }
    }

    @Nested
    @DisplayName("유저 비밀번호 변경 테스트")
    class ChangeUserPasswordTest {
        @Test
        @Transactional
        void 유저_비밀번호를_정상적으로_변경한다() {
            //given
            String newPassword = "NewPassword123!";
            PasswordRequestDto request = new PasswordRequestDto(newPassword);
            String oldEncodedPassword = follower.getPassword();

            //when
            userService.changeUserPassword(follower, request);

            //then
            User updatedUser = userRepository.findById(follower.getId()).orElseThrow();
            // 비밀번호가 인코딩되어 변경되었는지 확인
            assertThat(updatedUser.getPassword()).isNotEqualTo(oldEncodedPassword);
            assertThat(updatedUser.getPassword()).isNotEqualTo(newPassword);
            // 새로운 비밀번호로 검증 가능한지 확인
            assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isTrue();
        }

        @Test
        @Transactional
        void 변경된_비밀번호로_로그인이_가능한지_확인한다() {
            //given
            String newPassword = "NewPassword123!";
            PasswordRequestDto request = new PasswordRequestDto(newPassword);

            //when
            userService.changeUserPassword(follower, request);

            //then
            User updatedUser = userRepository.findById(follower.getId()).orElseThrow();
            // 변경된 비밀번호로 검증
            boolean matches = passwordEncoder.matches(newPassword, updatedUser.getPassword());
            assertThat(matches).isTrue();
            // 이전 비밀번호로는 검증 실패
            boolean oldMatches = passwordEncoder.matches("password", updatedUser.getPassword());
            assertThat(oldMatches).isFalse();
        }
    }

    @Nested
    @DisplayName("팔로우 정상 테스트")
    class FollowSuccessTest {
        @Test
        @Transactional
        void 팔로우를_정상적으로_수행한다() {
            //given
            Long followedId = followed.getId();
            int initialFollowerCount = followed.getFollowerCount();
            int initialFollowingCount = follower.getFollowingCount();

            //when
            userService.follow(follower, followedId);

            //then
            boolean followExists = userFollowRepository.existsByFollowerIdAndFollowedId(follower.getId(), followedId);
            assertThat(followExists).isTrue();

            // 팔로우 카운트 확인
            User updatedFollower = userRepository.findById(follower.getId()).orElseThrow();
            User updatedFollowed = userRepository.findById(followedId).orElseThrow();
            
            assertThat(updatedFollower.getFollowingCount()).isEqualTo(initialFollowingCount + 1);
            assertThat(updatedFollowed.getFollowerCount()).isEqualTo(initialFollowerCount + 1);
        }

        @Test
        @Transactional
        void 이미_팔로우한_사용자를_다시_팔로우하면_예외가_발생한다() {
            //given
            Long followedId = followed.getId();
            userService.follow(follower, followedId);

            //when & then
            assertThatThrownBy(() -> userService.follow(follower, followedId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 팔로우하고 있는 상태입니다.");
        }
    }

    @Nested
    @DisplayName("언팔로우 정상 테스트")
    class UnfollowSuccessTest {
        @Test
        @Transactional
        void 언팔로우를_정상적으로_수행한다() {
            //given
            Long followedId = followed.getId();
            userService.follow(follower, followedId);
            
            int followerCountAfterFollow = followed.getFollowerCount();
            int followingCountAfterFollow = follower.getFollowingCount();

            //when
            userService.unfollow(follower, followedId);

            //then
            boolean followExists = userFollowRepository.existsByFollowerIdAndFollowedId(follower.getId(), followedId);
            assertThat(followExists).isFalse();

            // 팔로우 카운트 확인
            User updatedFollower = userRepository.findById(follower.getId()).orElseThrow();
            User updatedFollowed = userRepository.findById(followedId).orElseThrow();
            
            assertThat(updatedFollower.getFollowingCount()).isEqualTo(followingCountAfterFollow - 1);
            assertThat(updatedFollowed.getFollowerCount()).isEqualTo(followerCountAfterFollow - 1);
        }

        @Test
        void 팔로우하지_않은_사용자를_언팔로우하면_예외가_발생한다() {
            //given
            Long followedId = followed.getId();

            //when & then
            assertThatThrownBy(() -> userService.unfollow(follower, followedId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("언팔로우할 유저를 팔로우하지 않은 상태입니다.");
        }
    }

    @Nested
    @DisplayName("팔로우 실패 테스트")
    class FollowFailTest {
        @Test
        void 자신을_팔로우하면_에러가_발생한다() {
            //given
            Long selfId = follower.getId();

            //when & then
            assertThatThrownBy(() -> userService.follow(follower, selfId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("팔로우 대상과 주체의 유저가 동일합니다.");
        }

        @Test
        void 존재하지_않는_유저를_팔로우하면_에러가_발생한다() {
            //given
            Long nonExistentUserId = 999L;

            //when & then
            assertThatThrownBy(() -> userService.follow(follower, nonExistentUserId))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("언팔로우 실패 테스트")
    class UnfollowFailTest {
        @Test
        void 존재하지_않는_유저를_언팔로우하면_에러가_발생한다() {
            //given
            Long nonExistentUserId = 999L;

            //when & then
            assertThatThrownBy(() -> userService.unfollow(follower, nonExistentUserId))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void 자신을_언팔로우하면_에러가_발생한다() {
            //given
            Long selfId = follower.getId();

            //when & then
            assertThatThrownBy(() -> userService.unfollow(follower, selfId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("언팔로우 대상과 주체의 유저가 동일합니다.");
        }
    }
}
