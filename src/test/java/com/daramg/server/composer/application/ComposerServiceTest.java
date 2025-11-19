package com.daramg.server.composer.application;

import com.daramg.server.composer.domain.Composer;
import com.daramg.server.composer.domain.Continent;
import com.daramg.server.composer.domain.Era;
import com.daramg.server.composer.domain.Gender;
import com.daramg.server.composer.repository.ComposerLikeRepository;
import com.daramg.server.composer.repository.ComposerRepository;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.repository.UserRepository;
import com.daramg.server.testsupport.support.ServiceTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class ComposerServiceTest extends ServiceTestSupport {

    @Autowired
    private ComposerService composerService;

    @Autowired
    private ComposerRepository composerRepository;

    @Autowired
    private ComposerLikeRepository composerLikeRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Composer composer;

    @BeforeEach
    void setUp() {
        user = new User("email", "password", "name", LocalDate.now(), "profile image", "호시", "bio", null);
        userRepository.save(user);

        composer = Composer.builder()
                .koreanName("베토벤")
                .englishName("Ludwig van Beethoven")
                .nativeName("Ludwig van Beethoven")
                .gender(Gender.MALE)
                .nationality("독일")
                .birthYear((short) 1770)
                .deathYear((short) 1827)
                .era(Era.CLASSICAL)
                .continent(Continent.EUROPE)
                .build();
        composerRepository.save(composer);
    }

    @Nested
    @DisplayName("작곡가 좋아요 토글 테스트")
    class ComposerLikeToggleTest {
        @Test
        void 유저가_작곡가에_좋아요를_누른다() throws Exception {
            // when
            invokeToggleLike(composer.getId(), user);

            // then
            boolean exists = composerLikeRepository.existsByComposerIdAndUserId(composer.getId(), user.getId());
            assertThat(exists).isTrue();
        }

        @Test
        void 유저가_좋아요한_작곡가의_좋아요를_취소한다() throws Exception {
            // given - 먼저 좋아요 추가
            invokeToggleLike(composer.getId(), user);

            // when - 좋아요 취소
            invokeToggleLike(composer.getId(), user);

            // then
            boolean exists = composerLikeRepository.existsByComposerIdAndUserId(composer.getId(), user.getId());
            assertThat(exists).isFalse();
        }
    }

    private void invokeToggleLike(Long composerId, User user) throws Exception {
        composerService.toggleLike(composerId, user);
    }
}


