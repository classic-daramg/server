package com.daramg.server.composer.application;

import com.daramg.server.composer.domain.Composer;
import com.daramg.server.composer.domain.ComposerLike;
import com.daramg.server.composer.domain.Gender;
import com.daramg.server.composer.domain.Continent;
import com.daramg.server.composer.domain.Era;
import com.daramg.server.composer.dto.ComposerResponseDto;
import com.daramg.server.composer.repository.ComposerLikeRepository;
import com.daramg.server.composer.repository.ComposerRepository;
import com.daramg.server.testsupport.support.ServiceTestSupport;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ComposerQueryServiceTest extends ServiceTestSupport {

    @Autowired
    private ComposerQueryService composerQueryService;

    @Autowired
    private ComposerRepository composerRepository;

    @Autowired
    private ComposerLikeRepository composerLikeRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Composer corelli;  // 코렐리
    private Composer vitali;   // 비탈리
    private Composer vivaldi;  // 비발디

    @BeforeEach
    void setUp() {
        user = new User("email@test.com", "password", "name",
                LocalDate.now(), "profile", "닉네임", "bio", null);
        userRepository.save(user);

        corelli = Composer.builder()
                .koreanName("코렐리")
                .englishName("Arcangelo Corelli")
                .gender(Gender.MALE)
                .era(Era.BAROQUE)
                .continent(Continent.EUROPE)
                .build();

        vitali = Composer.builder()
                .koreanName("비탈리")
                .englishName("Tomaso Antonio Vitali")
                .gender(Gender.MALE)
                .era(Era.BAROQUE)
                .continent(Continent.EUROPE)
                .build();

        vivaldi = Composer.builder()
                .koreanName("비발디")
                .englishName("Antonio Vivaldi")
                .gender(Gender.MALE)
                .era(Era.BAROQUE)
                .continent(Continent.EUROPE)
                .build();

        composerRepository.saveAll(List.of(corelli, vitali, vivaldi));
    }

    @Test
    @DisplayName("로그인 시: 좋아요한 작곡가가 먼저, 같은 그룹 내 한글명 오름차순 정렬")
    void getAllComposers_whenLoggedIn_sortedByLikedThenName() {
        // given - 유저가 비발디, 코렐리를 좋아요
        composerLikeRepository.save(ComposerLike.of(vivaldi, user));
        composerLikeRepository.save(ComposerLike.of(corelli, user));

        // when
        List<ComposerResponseDto> result = composerQueryService.getAllComposers(
                user,
                List.of(Era.BAROQUE, Era.CLASSICAL),
                List.of(Continent.EUROPE)
        );

        // then
        assertThat(result).hasSize(3);
        // 좋아요 그룹이 먼저 나오며, 같은 그룹 내에서는 한글명 정렬: 비발디 < 코렐리
        assertThat(result.get(0).koreanName()).isEqualTo("비발디");
        assertThat(result.get(0).isLiked()).isTrue();

        assertThat(result.get(1).koreanName()).isEqualTo("코렐리");
        assertThat(result.get(1).isLiked()).isTrue();

        assertThat(result.get(2).koreanName()).isEqualTo("비탈리");
        assertThat(result.get(2).isLiked()).isFalse();
    }

    @Test
    @DisplayName("비로그인 시: 전부 isLiked=false, 한글명 오름차순 정렬")
    void getAllComposers_whenAnonymous_sortedByNameAndNoLikes() {
        // when
        List<ComposerResponseDto> result = composerQueryService.getAllComposers(
                null,
                List.of(Era.BAROQUE),
                List.of(Continent.EUROPE)
        );

        // then
        assertThat(result).hasSize(3);
        // 모두 false
        assertThat(result).allMatch(dto -> !dto.isLiked());
        // 한글명 오름차순: 비발디, 비탈리, 코렐리
        assertThat(result.get(0).koreanName()).isEqualTo("비발디");
        assertThat(result.get(1).koreanName()).isEqualTo("비탈리");
        assertThat(result.get(2).koreanName()).isEqualTo("코렐리");
    }

    @Test
    @DisplayName("필터(시대/대륙)로 결과를 제한한다: 클래식+유럽만 포함")
    void filters_limitResultsByEraAndContinent() {
        // given - 추가 작곡가(클래식/유럽)
        Composer haydn = Composer.builder()
                .koreanName("하이든")
                .englishName("Joseph Haydn")
                .gender(Gender.MALE)
                .era(Era.CLASSICAL)
                .continent(Continent.EUROPE)
                .build();
        composerRepository.save(haydn);

        // when - 클래식/유럽으로 필터
        List<ComposerResponseDto> result = composerQueryService.getAllComposers(
                null,
                List.of(Era.CLASSICAL),
                List.of(Continent.EUROPE)
        );

        // then - 하이든만 남는다
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().koreanName()).isEqualTo("하이든");
        assertThat(result.getFirst().isLiked()).isFalse();
    }

    @Test
    @DisplayName("빈 리스트 필터는 미적용으로 간주한다")
    void emptyLists_areTreatedAsNoFilter() {
        // when
        List<ComposerResponseDto> result = composerQueryService.getAllComposers(
                null,
                List.of(),  // no filter
                List.of()   // no filter
        );

        // then - 전체 3명
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("여러 필터 값(시대 2+, 대륙 2+)을 동시에 적용한다")
    void multipleFilters_supportsMultipleValuesPerParam() {
        // given - 다양한 조합의 작곡가 추가
        Composer mozart = Composer.builder()
                .koreanName("모차르트")
                .englishName("Wolfgang Amadeus Mozart")
                .gender(Gender.MALE)
                .era(Era.CLASSICAL)
                .continent(Continent.EUROPE)
                .build();
        Composer doho = Composer.builder()
                .koreanName("도호")
                .englishName("Doho Example")
                .gender(Gender.MALE)
                .era(Era.CLASSICAL)
                .continent(Continent.ASIA)
                .build();
        // 제외 대상들 (필터에 걸리지 않아야 함)
        Composer rach = Composer.builder()
                .koreanName("라흐마니노프")
                .englishName("Sergei Rachmaninoff")
                .gender(Gender.MALE)
                .era(Era.ROMANTIC)
                .continent(Continent.EUROPE)
                .build();
        Composer deva = Composer.builder()
                .koreanName("데바")
                .englishName("Deva Example")
                .gender(Gender.MALE)
                .era(Era.MODERN_CONTEMPORARY)
                .continent(Continent.ASIA)
                .build();
        composerRepository.saveAll(List.of(mozart, doho, rach, deva));

        // when - 시대: 바로크/클래식, 대륙: 유럽/아시아
        List<ComposerResponseDto> result = composerQueryService.getAllComposers(
                null,
                List.of(Era.BAROQUE, Era.CLASSICAL),
                List.of(Continent.EUROPE, Continent.ASIA)
        );

        // then - 포함: 코렐리/비탈리/비발디(바로크/유럽) + 모차르트(클래식/유럽) + 도호(클래식/아시아)
        assertThat(result).hasSize(5);
        assertThat(result).extracting(ComposerResponseDto::koreanName)
                .containsExactlyInAnyOrder("코렐리", "비탈리", "비발디", "모차르트", "도호");
        assertThat(result).allMatch(dto -> !dto.isLiked());
    }
}
