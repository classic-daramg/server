package com.daramg.server.user.application;

import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.common.exception.NotFoundException;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.repository.UserRepository;
import com.daramg.server.user.repository.UserFollowRepository;
import com.daramg.server.testsupport.support.ServiceTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

    private User follower;
    private User followed;

    @BeforeEach
    void setUp() {
        follower = new User("follower@email.com", "password", "팔로워", LocalDate.now(), "profile image", "팔로워닉네임", "bio", null);
        followed = new User("followed@email.com", "password", "팔로우당하는사람", LocalDate.now(), "profile image", "팔로우당하는닉네임", "bio", null);
        
        userRepository.save(follower);
        userRepository.save(followed);
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
