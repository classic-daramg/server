package com.daramg.server.post.application;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.common.util.PagingUtils;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.PostStatus;
import com.daramg.server.post.domain.vo.PostCreateVo;
import com.daramg.server.post.dto.FreePostsResponseDto;
import com.daramg.server.post.repository.PostRepository;
import com.daramg.server.testsupport.support.ServiceTestSupport;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PostQueryServiceTest extends ServiceTestSupport {

    @Autowired
    private PostQueryService postQueryService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PagingUtils pagingUtils;

    private User user;
    private List<FreePost> freePosts;

    @BeforeEach
    void setUp() {
        user = new User("email@test.com", "password", "name",
                LocalDate.now(), "profile", "닉네임", "bio", null);
        userRepository.save(user);

        // 25개의 FreePost 생성
        freePosts = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            FreePost post = FreePost.from(
                    new PostCreateVo.Free(
                            user,
                            "제목 " + i,
                            "내용 " + i,
                            PostStatus.PUBLISHED,
                            List.of("image" + i + ".jpg"),
                            null,
                            List.of("#tag" + i)
                    )
            );
            freePosts.add(post);
        }
        postRepository.saveAll(freePosts);
    }

    @Nested
    @DisplayName("정상 테스트")
    class NormalTest {
        @Test
        @DisplayName("Published 상태인 포스트만 반환된다")
        void getAllPublishedFreePosts_ReturnsOnlyPublishedPosts() {
            // given
            // DRAFT 상태인 포스트 추가
            FreePost draftPost1 = FreePost.from(
                    new PostCreateVo.Free(
                            user,
                            "DRAFT 제목 1",
                            "DRAFT 내용 1",
                            PostStatus.DRAFT,
                            List.of("draft1.jpg"),
                            null,
                            List.of("#draft1")
                    )
            );
            FreePost draftPost2 = FreePost.from(
                    new PostCreateVo.Free(
                            user,
                            "DRAFT 제목 2",
                            "DRAFT 내용 2",
                            PostStatus.DRAFT,
                            List.of("draft2.jpg"),
                            null,
                            List.of("#draft2")
                    )
            );
            postRepository.saveAll(List.of(draftPost1, draftPost2));

            PageRequestDto pageRequest = new PageRequestDto(null, 100); // 모든 포스트 조회

            // when
            PageResponseDto<FreePostsResponseDto> response = postQueryService.getAllPublishedFreePosts(pageRequest);

            // then
            // PUBLISHED 포스트 25개만 반환되어야 함 (DRAFT 2개는 제외)
            assertThat(response.getContent()).hasSize(25);
            // 모든 반환된 포스트가 PUBLISHED 상태인지 확인 (제목으로 확인)
            assertThat(response.getContent())
                    .extracting(FreePostsResponseDto::title)
                    .doesNotContain("DRAFT 제목 1", "DRAFT 제목 2");
            // PUBLISHED 포스트는 포함되어야 함
            assertThat(response.getContent())
                    .extracting(FreePostsResponseDto::title)
                    .contains("제목 0", "제목 24");
        }
        @Test
        @DisplayName("size가 20이고 cursor가 null일 때 포스트 요청이 정상적으로 작동")
        void getAllFreePosts_WithSize20AndNullCursor_Returns20Posts() {
            // given
            PageRequestDto pageRequest = new PageRequestDto(null, 20);

            // when
            PageResponseDto<FreePostsResponseDto> response = postQueryService.getAllPublishedFreePosts(pageRequest);

            // then
            assertThat(response.getContent()).hasSize(20);
            assertThat(response.getHasNext()).isTrue(); // 25개 중 20개만 반환되므로 다음 페이지 있음
            assertThat(response.getNextCursor()).isNotNull();
            // createdAt 내림차순, ID 내림차순으로 정렬되므로 가장 최근 생성된 포스트가 먼저 나옴
            // 24번이 마지막에 생성되었으므로 첫 번째로 나와야 함
            assertThat(response.getContent().getFirst().title()).isEqualTo("제목 24");
            assertThat(response.getContent().getLast().title()).isEqualTo("제목 5");
        }

        @Test
        @DisplayName("size가 null이고 cursor 조건이 있을 때 default 값인 10이 정상적으로 적용")
        void getAllFreePosts_WithNullSizeAndValidCursor_Returns10Posts() {
            // given
            PageRequestDto firstRequest = new PageRequestDto(null, 10);
            PageResponseDto<FreePostsResponseDto> firstResponse = postQueryService.getAllPublishedFreePosts(firstRequest);
            String nextCursor = firstResponse.getNextCursor();

            // when - size가 null이고 cursor가 있는 두 번째 요청
            PageRequestDto secondRequest = new PageRequestDto(nextCursor, null);
            PageResponseDto<FreePostsResponseDto> secondResponse = postQueryService.getAllPublishedFreePosts(secondRequest);

            // then
            assertThat(secondResponse.getContent()).hasSize(10); // default size 10이 적용됨
            assertThat(secondResponse.getHasNext()).isTrue(); // 아직 더 있음
            // 첫 번째 요청에서 10개를 가져갔으므로(제목 24~15), 두 번째는 14~5가 나와야 함
            assertThat(secondResponse.getContent().getFirst().title()).isEqualTo("제목 14");
            assertThat(secondResponse.getContent().getLast().title()).isEqualTo("제목 5");
        }
    }

    @Nested
    @DisplayName("비정상 테스트")
    class AbnormalTest {
        @Test
        @DisplayName("size가 음수로 입력될 경우 default 값 10이 적용된다")
        void getAllFreePosts_WithNegativeSize_AppliesDefaultSize10() {
            // given
            PageRequestDto pageRequest = new PageRequestDto(null, -5);

            // when
            PageResponseDto<FreePostsResponseDto> response = postQueryService.getAllPublishedFreePosts(pageRequest);

            // then
            assertThat(response.getContent()).hasSize(10); // 음수는 default 10으로 처리됨
            assertThat(response.getHasNext()).isTrue();
        }

        @Test
        @DisplayName("size가 0으로 입력될 경우 default 값 10이 적용된다")
        void getAllFreePosts_WithZeroSize_AppliesDefaultSize10() {
            // given
            PageRequestDto pageRequest = new PageRequestDto(null, 0);

            // when
            PageResponseDto<FreePostsResponseDto> response = postQueryService.getAllPublishedFreePosts(pageRequest);

            // then
            assertThat(response.getContent()).hasSize(10); // 0은 default 10으로 처리됨
            assertThat(response.getHasNext()).isTrue();
        }

        @Test
        @DisplayName("db에 저장된 포스트의 개수보다 size가 클 경우 모든 포스트를 반환한다")
        void getAllFreePosts_WithSizeLargerThanTotal_ReturnsAllPosts() {
            // given
            PageRequestDto pageRequest = new PageRequestDto(null, 100); // 25개보다 큰 값

            // when
            PageResponseDto<FreePostsResponseDto> response = postQueryService.getAllPublishedFreePosts(pageRequest);

            // then
            assertThat(response.getContent()).hasSize(25); // 모든 포스트 반환
            assertThat(response.getHasNext()).isFalse(); // 다음 페이지 없음
            assertThat(response.getNextCursor()).isNull();
        }

        @Test
        @DisplayName("cursor가 비정상적으로 입력될 경우 BusinessException이 발생한다")
        void getAllFreePosts_WithInvalidCursor_ThrowsBusinessException() {
            // given
            String invalidCursor = "not-a-valid-base64-cursor";
            PageRequestDto pageRequest = new PageRequestDto(invalidCursor, 10);

            // when & then
            assertThatThrownBy(() -> postQueryService.getAllPublishedFreePosts(pageRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("유효하지 않은 커서 포맷입니다.");
        }

        @Test
        @DisplayName("cursor가 잘못된 포맷의 Base64 문자열일 경우 BusinessException이 발생한다")
        void getAllFreePosts_WithMalformedBase64Cursor_ThrowsBusinessException() {
            // given
            String malformedCursor = "invalid@base64#format!";
            PageRequestDto pageRequest = new PageRequestDto(malformedCursor, 10);

            // when & then
            assertThatThrownBy(() -> postQueryService.getAllPublishedFreePosts(pageRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("유효하지 않은 커서 포맷입니다.");
        }

        @Test
        @DisplayName("cursor가 올바른 Base64지만 잘못된 형식일 경우 BusinessException이 발생한다")
        void getAllFreePosts_WithInvalidCursorFormat_ThrowsBusinessException() {
            // given
            // Base64로 인코딩되었지만 날짜_아이디 형식이 아닌 문자열
            String invalidFormatCursor = java.util.Base64.getEncoder().encodeToString("invalid-format".getBytes());
            PageRequestDto pageRequest = new PageRequestDto(invalidFormatCursor, 10);

            // when & then
            assertThatThrownBy(() -> postQueryService.getAllPublishedFreePosts(pageRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("유효하지 않은 커서 포맷입니다.");
        }

        @Test
        @DisplayName("cursor가 유효한 포맷이지만 존재하지 않는 포스트를 가리킬 경우 빈 리스트를 반환한다")
        void getAllFreePosts_WithNonExistentCursor_ReturnsEmptyList() {
            // given
            LocalDateTime pastDateTime = LocalDateTime.now().minusYears(100);
            Long nonExistentId = 0L;
            String validFormatCursor = pagingUtils.encodeCursor(pastDateTime, nonExistentId);
            PageRequestDto pageRequest = new PageRequestDto(validFormatCursor, 10);

            // when
            PageResponseDto<FreePostsResponseDto> response = postQueryService.getAllPublishedFreePosts(pageRequest);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getNextCursor()).isNull();
        }
    }
}

