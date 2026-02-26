package com.daramg.server.post.application;

import com.daramg.server.comment.domain.Comment;
import com.daramg.server.comment.domain.CommentLike;
import com.daramg.server.comment.repository.CommentLikeRepository;
import com.daramg.server.comment.repository.CommentRepository;
import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.common.exception.NotFoundException;
import com.daramg.server.common.util.PagingUtils;
import com.daramg.server.composer.domain.*;
import com.daramg.server.composer.dto.ComposerWithPostsResponseDto;
import com.daramg.server.composer.repository.ComposerLikeRepository;
import com.daramg.server.composer.repository.ComposerRepository;
import com.daramg.server.post.domain.CurationPost;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.PostLike;
import com.daramg.server.post.domain.PostScrap;
import com.daramg.server.post.domain.PostStatus;
import com.daramg.server.post.domain.StoryPost;
import com.daramg.server.post.domain.vo.PostCreateVo;
import com.daramg.server.post.dto.PostDetailResponse;
import com.daramg.server.post.dto.PostResponseDto;
import com.daramg.server.post.repository.PostLikeRepository;
import com.daramg.server.post.repository.PostRepository;
import com.daramg.server.post.repository.PostScrapRepository;
import com.daramg.server.testsupport.support.ServiceTestSupport;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    @Autowired
    private PostScrapRepository postScrapRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private ComposerRepository composerRepository;

    @Autowired
    private ComposerLikeRepository composerLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    private User user;
    private User otherUser;
    private List<FreePost> freePosts;

    @BeforeEach
    void setUp() {
        user = new User("email@test.com", "password", "name",
                LocalDate.now(), "profile", "닉네임", "bio", null);
        userRepository.save(user);

        otherUser = new User("other@test.com", "password", "otherName",
                LocalDate.now(), "profile", "다른닉네임", "bio", null);
        userRepository.save(otherUser);

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
            PageResponseDto<PostResponseDto> response = postQueryService.getAllPublishedFreePosts(pageRequest, null);

            // then
            // PUBLISHED 포스트 25개만 반환되어야 함 (DRAFT 2개는 제외)
            assertThat(response.getContent()).hasSize(25);
            // 모든 반환된 포스트가 PUBLISHED 상태인지 확인 (제목으로 확인)
            assertThat(response.getContent())
                    .extracting(PostResponseDto::title)
                    .doesNotContain("DRAFT 제목 1", "DRAFT 제목 2");
            // PUBLISHED 포스트는 포함되어야 함
            assertThat(response.getContent())
                    .extracting(PostResponseDto::title)
                    .contains("제목 0", "제목 24");
        }
        @Test
        @DisplayName("size가 20이고 cursor가 null일 때 포스트 요청이 정상적으로 작동")
        void getAllFreePosts_WithSize20AndNullCursor_Returns20Posts() {
            // given
            PageRequestDto pageRequest = new PageRequestDto(null, 20);

            // when
            PageResponseDto<PostResponseDto> response = postQueryService.getAllPublishedFreePosts(pageRequest, null);

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
            PageResponseDto<PostResponseDto> firstResponse = postQueryService.getAllPublishedFreePosts(firstRequest, null);
            String nextCursor = firstResponse.getNextCursor();

            // when - size가 null이고 cursor가 있는 두 번째 요청
            PageRequestDto secondRequest = new PageRequestDto(nextCursor, null);
            PageResponseDto<PostResponseDto> secondResponse = postQueryService.getAllPublishedFreePosts(secondRequest, null);

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
            PageResponseDto<PostResponseDto> response = postQueryService.getAllPublishedFreePosts(pageRequest, null);

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
            PageResponseDto<PostResponseDto> response = postQueryService.getAllPublishedFreePosts(pageRequest, null);

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
            PageResponseDto<PostResponseDto> response = postQueryService.getAllPublishedFreePosts(pageRequest, null);

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
            assertThatThrownBy(() -> postQueryService.getAllPublishedFreePosts(pageRequest, null))
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
            assertThatThrownBy(() -> postQueryService.getAllPublishedFreePosts(pageRequest, null))
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
            assertThatThrownBy(() -> postQueryService.getAllPublishedFreePosts(pageRequest, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("유효하지 않은 커서 포맷입니다.");
        }

        @Test
        @DisplayName("cursor가 유효한 포맷이지만 존재하지 않는 포스트를 가리킬 경우 빈 리스트를 반환한다")
        void getAllFreePosts_WithNonExistentCursor_ReturnsEmptyList() {
            // given
            Instant pastDateTime = Instant.now().minus(100 * 365L, ChronoUnit.DAYS);
            Long nonExistentId = 0L;
            String validFormatCursor = pagingUtils.encodeCursor(pastDateTime, nonExistentId);
            PageRequestDto pageRequest = new PageRequestDto(validFormatCursor, 10);

            // when
            PageResponseDto<PostResponseDto> response = postQueryService.getAllPublishedFreePosts(pageRequest, null);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getNextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("유저가 작성한 발행된 포스트 조회 테스트")
    class UserPublishedPostsTest {
        @Test
        @DisplayName("특정 유저가 작성한 PUBLISHED 상태 포스트만 반환된다")
        void getUserPublishedPosts_ReturnsOnlyPublishedPostsOfUser() {
            // given
            // 다른 유저가 작성한 포스트 추가
            FreePost otherUserPost = FreePost.from(
                    new PostCreateVo.Free(
                            otherUser,
                            "다른 유저 포스트",
                            "다른 유저 내용",
                            PostStatus.PUBLISHED,
                            List.of("other.jpg"),
                            null,
                            List.of("#other")
                    )
            );
            // 같은 유저가 작성한 DRAFT 포스트 추가
            FreePost draftPost = FreePost.from(
                    new PostCreateVo.Free(
                            user,
                            "DRAFT 포스트",
                            "DRAFT 내용",
                            PostStatus.DRAFT,
                            List.of("draft.jpg"),
                            null,
                            List.of("#draft")
                    )
            );
            postRepository.saveAll(List.of(otherUserPost, draftPost));

            PageRequestDto pageRequest = new PageRequestDto(null, 100);

            // when
            PageResponseDto<PostResponseDto> response = postQueryService.getUserPublishedPosts(user.getId(), pageRequest);

            // then
            // 해당 유저가 작성한 PUBLISHED 포스트 25개만 반환되어야 함
            assertThat(response.getContent()).hasSize(25);
            assertThat(response.getContent())
                    .extracting(PostResponseDto::title)
                    .doesNotContain("다른 유저 포스트", "DRAFT 포스트");
            assertThat(response.getContent())
                    .extracting(PostResponseDto::title)
                    .contains("제목 0", "제목 24");
        }

        @Test
        @DisplayName("페이징이 정상적으로 작동한다")
        void getUserPublishedPosts_WithPaging_ReturnsCorrectPosts() {
            // given
            PageRequestDto pageRequest = new PageRequestDto(null, 10);

            // when
            PageResponseDto<PostResponseDto> response = postQueryService.getUserPublishedPosts(user.getId(), pageRequest);

            // then
            assertThat(response.getContent()).hasSize(10);
            assertThat(response.getHasNext()).isTrue();
            assertThat(response.getNextCursor()).isNotNull();
            // 최신순으로 정렬되어야 함
            assertThat(response.getContent().getFirst().title()).isEqualTo("제목 24");
        }

        @Test
        @DisplayName("존재하지 않는 유저 ID로 조회하면 빈 리스트를 반환한다")
        void getUserPublishedPosts_WithNonExistentUserId_ReturnsEmptyList() {
            // given
            Long nonExistentUserId = 999L;
            PageRequestDto pageRequest = new PageRequestDto(null, 10);

            // when
            PageResponseDto<PostResponseDto> response = postQueryService.getUserPublishedPosts(nonExistentUserId, pageRequest);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getNextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("유저가 작성한 임시저장 포스트 조회 테스트")
    class UserDraftPostsTest {
        @Test
        @DisplayName("특정 유저가 작성한 DRAFT 상태 포스트만 반환된다")
        void getUserDraftPosts_ReturnsOnlyDraftPostsOfUser() {
            // given
            // DRAFT 포스트 10개 생성
            List<FreePost> draftPosts = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                FreePost draftPost = FreePost.from(
                        new PostCreateVo.Free(
                                user,
                                "DRAFT 제목 " + i,
                                "DRAFT 내용 " + i,
                                PostStatus.DRAFT,
                                List.of("draft" + i + ".jpg"),
                                null,
                                List.of("#draft" + i)
                        )
                );
                draftPosts.add(draftPost);
            }
            postRepository.saveAll(draftPosts);

            // 다른 유저가 작성한 DRAFT 포스트 추가
            FreePost otherUserDraft = FreePost.from(
                    new PostCreateVo.Free(
                            otherUser,
                            "다른 유저 DRAFT",
                            "다른 유저 DRAFT 내용",
                            PostStatus.DRAFT,
                            List.of("otherDraft.jpg"),
                            null,
                            List.of("#otherDraft")
                    )
            );
            postRepository.save(otherUserDraft);

            PageRequestDto pageRequest = new PageRequestDto(null, 100);

            // when
            PageResponseDto<PostResponseDto> response = postQueryService.getUserDraftPosts(user.getId(), pageRequest);

            // then
            // 해당 유저가 작성한 DRAFT 포스트 10개만 반환되어야 함
            assertThat(response.getContent()).hasSize(10);
            assertThat(response.getContent())
                    .extracting(PostResponseDto::title)
                    .doesNotContain("다른 유저 DRAFT");
            assertThat(response.getContent())
                    .extracting(PostResponseDto::title)
                    .contains("DRAFT 제목 0", "DRAFT 제목 9");
        }

        @Test
        @DisplayName("페이징이 정상적으로 작동한다")
        void getUserDraftPosts_WithPaging_ReturnsCorrectPosts() {
            // given
            // DRAFT 포스트 15개 생성
            List<FreePost> draftPosts = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                FreePost draftPost = FreePost.from(
                        new PostCreateVo.Free(
                                user,
                                "DRAFT 제목 " + i,
                                "DRAFT 내용 " + i,
                                PostStatus.DRAFT,
                                List.of("draft" + i + ".jpg"),
                                null,
                                List.of("#draft" + i)
                        )
                );
                draftPosts.add(draftPost);
            }
            postRepository.saveAll(draftPosts);

            PageRequestDto pageRequest = new PageRequestDto(null, 10);

            // when
            PageResponseDto<PostResponseDto> response = postQueryService.getUserDraftPosts(user.getId(), pageRequest);

            // then
            assertThat(response.getContent()).hasSize(10);
            assertThat(response.getHasNext()).isTrue();
            assertThat(response.getNextCursor()).isNotNull();
        }

        @Test
        @DisplayName("DRAFT 포스트가 없으면 빈 리스트를 반환한다")
        void getUserDraftPosts_WithNoDraftPosts_ReturnsEmptyList() {
            // given
            // setUp에서 생성된 포스트는 모두 PUBLISHED 상태
            PageRequestDto pageRequest = new PageRequestDto(null, 10);

            // when
            PageResponseDto<PostResponseDto> response = postQueryService.getUserDraftPosts(user.getId(), pageRequest);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getNextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("유저가 스크랩한 포스트 조회 테스트")
    class UserScrappedPostsTest {
        @Test
        @DisplayName("특정 유저가 스크랩한 PUBLISHED 포스트만 반환된다")
        void getUserScrappedPosts_ReturnsOnlyScrappedPublishedPosts() {
            // given
            // 스크랩할 포스트 생성 (PUBLISHED)
            List<Post> postsToScrap = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                FreePost post = FreePost.from(
                        new PostCreateVo.Free(
                                otherUser,
                                "스크랩 포스트 " + i,
                                "스크랩 내용 " + i,
                                PostStatus.PUBLISHED,
                                List.of("scrap" + i + ".jpg"),
                                null,
                                List.of("#scrap" + i)
                        )
                );
                postsToScrap.add(post);
            }
            postRepository.saveAll(postsToScrap);

            // DRAFT 포스트 생성 (스크랩하지 않음)
            FreePost draftPost = FreePost.from(
                    new PostCreateVo.Free(
                            otherUser,
                            "DRAFT 스크랩 불가",
                            "DRAFT 내용",
                            PostStatus.DRAFT,
                            List.of("draft.jpg"),
                            null,
                            List.of("#draft")
                    )
            );
            postRepository.save(draftPost);

            // 유저가 포스트 스크랩
            for (Post post : postsToScrap) {
                postScrapRepository.save(PostScrap.of(post, user));
            }

            // 다른 유저가 스크랩한 포스트 (제외되어야 함)
            PostScrap otherUserScrap = PostScrap.of(postsToScrap.get(0), otherUser);
            postScrapRepository.save(otherUserScrap);

            PageRequestDto pageRequest = new PageRequestDto(null, 100);

            // when
            PageResponseDto<PostResponseDto> response = postQueryService.getUserScrappedPosts(user.getId(), pageRequest);

            // then
            // 해당 유저가 스크랩한 PUBLISHED 포스트 5개만 반환되어야 함
            assertThat(response.getContent()).hasSize(5);
            assertThat(response.getContent())
                    .extracting(PostResponseDto::title)
                    .contains("스크랩 포스트 0", "스크랩 포스트 4");
        }

        @Test
        @DisplayName("페이징이 정상적으로 작동한다")
        void getUserScrappedPosts_WithPaging_ReturnsCorrectPosts() {
            // given
            // 스크랩할 포스트 15개 생성
            List<Post> postsToScrap = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                FreePost post = FreePost.from(
                        new PostCreateVo.Free(
                                otherUser,
                                "스크랩 포스트 " + i,
                                "스크랩 내용 " + i,
                                PostStatus.PUBLISHED,
                                List.of("scrap" + i + ".jpg"),
                                null,
                                List.of("#scrap" + i)
                        )
                );
                postsToScrap.add(post);
            }
            postRepository.saveAll(postsToScrap);

            // 유저가 포스트 스크랩
            for (Post post : postsToScrap) {
                postScrapRepository.save(PostScrap.of(post, user));
            }

            PageRequestDto pageRequest = new PageRequestDto(null, 10);

            // when
            PageResponseDto<PostResponseDto> response = postQueryService.getUserScrappedPosts(user.getId(), pageRequest);

            // then
            assertThat(response.getContent()).hasSize(10);
            assertThat(response.getHasNext()).isTrue();
            assertThat(response.getNextCursor()).isNotNull();
        }

        @Test
        @DisplayName("스크랩한 포스트가 없으면 빈 리스트를 반환한다")
        void getUserScrappedPosts_WithNoScrappedPosts_ReturnsEmptyList() {
            // given
            PageRequestDto pageRequest = new PageRequestDto(null, 10);

            // when
            PageResponseDto<PostResponseDto> response = postQueryService.getUserScrappedPosts(user.getId(), pageRequest);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getNextCursor()).isNull();
        }

        @Test
        @DisplayName("DRAFT 상태 포스트는 스크랩해도 조회되지 않는다")
        void getUserScrappedPosts_WithDraftScrappedPost_DoesNotReturnDraft() {
            // given
            // DRAFT 포스트 생성 및 스크랩
            FreePost draftPost = FreePost.from(
                    new PostCreateVo.Free(
                            otherUser,
                            "DRAFT 스크랩 포스트",
                            "DRAFT 내용",
                            PostStatus.DRAFT,
                            List.of("draft.jpg"),
                            null,
                            List.of("#draft")
                    )
            );
            postRepository.save(draftPost);
            postScrapRepository.save(PostScrap.of(draftPost, user));

            // PUBLISHED 포스트 생성 및 스크랩
            FreePost publishedPost = FreePost.from(
                    new PostCreateVo.Free(
                            otherUser,
                            "PUBLISHED 스크랩 포스트",
                            "PUBLISHED 내용",
                            PostStatus.PUBLISHED,
                            List.of("published.jpg"),
                            null,
                            List.of("#published")
                    )
            );
            postRepository.save(publishedPost);
            postScrapRepository.save(PostScrap.of(publishedPost, user));

            PageRequestDto pageRequest = new PageRequestDto(null, 100);

            // when
            PageResponseDto<PostResponseDto> response = postQueryService.getUserScrappedPosts(user.getId(), pageRequest);

            // then
            // PUBLISHED 포스트만 반환되어야 함
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().getFirst().title()).isEqualTo("PUBLISHED 스크랩 포스트");
            assertThat(response.getContent())
                    .extracting(PostResponseDto::title)
                    .doesNotContain("DRAFT 스크랩 포스트");
        }
    }

    @Nested
    @DisplayName("포스트 상세 조회 테스트")
    class GetPostByIdTest {
        @Test
        @DisplayName("포스트 ID로 포스트 상세 정보를 조회한다")
        void getPostById_ReturnsPostDetails() {
            // given
            FreePost savedPost = freePosts.get(0);
            Long postId = savedPost.getId();

            // when
            PostDetailResponse response = postQueryService.getPostById(postId, null);

            // then
            assertThat(response.id()).isEqualTo(postId);
            assertThat(response.writerNickname()).isEqualTo("닉네임");
            assertThat(response.writerProfileImage()).isEqualTo("profile");
            assertThat(response.title()).isEqualTo("제목 0");
            assertThat(response.content()).isEqualTo("내용 0");
            assertThat(response.images()).containsExactly("image0.jpg");
            assertThat(response.videoUrl()).isNull();
            assertThat(response.hashtags()).containsExactly("#tag0");
            assertThat(response.postStatus()).isEqualTo(PostStatus.PUBLISHED);
            assertThat(response.likeCount()).isEqualTo(0);
            assertThat(response.commentCount()).isEqualTo(0);
            assertThat(response.isBlocked()).isFalse();
            assertThat(response.createdAt()).isNotNull();
            assertThat(response.updatedAt()).isNotNull();
            assertThat(response.type()).isEqualTo(com.daramg.server.post.domain.PostType.FREE);
            assertThat(response.primaryComposer()).isNull();
            assertThat(response.additionalComposers()).isNull();
            assertThat(response.isLiked()).isNull(); // 비로그인 유저는 null
            assertThat(response.isScrapped()).isNull(); // 비로그인 유저는 null
            assertThat(response.comments()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 포스트 ID로 조회하면 NotFoundException이 발생한다")
        void getPostById_WithNonExistentId_ThrowsNotFoundException() {
            // given
            Long nonExistentPostId = 999L;

            // when & then
            assertThatThrownBy(() -> postQueryService.getPostById(nonExistentPostId, null))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("존재하지 않는 Post입니다");
        }

        @Test
        @DisplayName("포스트 상세 조회 시 Post의 모든 필드가 포함된다")
        void getPostById_IncludesAllPostFields() {
            // given
            FreePost savedPost = freePosts.get(0);
            Long postId = savedPost.getId();

            // when
            PostDetailResponse response = postQueryService.getPostById(postId, null);

            // then
            assertThat(response.id()).isNotNull();
            assertThat(response.id()).isEqualTo(postId);
            assertThat(response.writerNickname()).isNotNull();
            assertThat(response.writerProfileImage()).isNotNull();
            assertThat(response.title()).isNotNull();
            assertThat(response.content()).isNotNull();
            assertThat(response.images()).isNotNull();
            assertThat(response.hashtags()).isNotNull();
            assertThat(response.postStatus()).isNotNull();
            assertThat(response.createdAt()).isNotNull();
            assertThat(response.updatedAt()).isNotNull();
            assertThat(response.type()).isNotNull();
            assertThat(response.isLiked()).isNull();
            assertThat(response.isScrapped()).isNull();
            assertThat(response.comments()).isNotNull();
        }

        @Test
        @DisplayName("포스트 상세 조회 시 댓글과 대댓글이 작성시간 오름차순으로 정렬된다")
        void getPostById_CommentsAndRepliesAreSortedByCreatedAtAsc() {
            // given
            FreePost savedPost = freePosts.get(0);
            Long postId = savedPost.getId();

            Instant baseTime = Instant.now().minus(1, ChronoUnit.HOURS);

            Comment parent1 = commentRepository.save(Comment.of(savedPost, user, "부모1", null));
            Comment parent2 = commentRepository.save(Comment.of(savedPost, user, "부모2", null));
            Comment parent3 = commentRepository.save(Comment.of(savedPost, user, "부모3", null));

            ReflectionTestUtils.setField(parent1, "createdAt", baseTime);
            ReflectionTestUtils.setField(parent2, "createdAt", baseTime.plus(10, ChronoUnit.MINUTES));
            ReflectionTestUtils.setField(parent3, "createdAt", baseTime.plus(20, ChronoUnit.MINUTES));

            commentRepository.saveAll(List.of(parent1, parent2, parent3));

            Comment child1 = commentRepository.save(Comment.of(savedPost, user, "자식1", parent1));
            Comment child2 = commentRepository.save(Comment.of(savedPost, user, "자식2", parent2));
            Comment child3 = commentRepository.save(Comment.of(savedPost, user, "자식3", parent3));

            ReflectionTestUtils.setField(child1, "createdAt", baseTime.plus(1, ChronoUnit.MINUTES));
            ReflectionTestUtils.setField(child2, "createdAt", baseTime.plus(11, ChronoUnit.MINUTES));
            ReflectionTestUtils.setField(child3, "createdAt", baseTime.plus(21, ChronoUnit.MINUTES));

            commentRepository.saveAll(List.of(child1, child2, child3));

            // 좋아요: 부모2와 자식3에만 좋아요 추가
            commentLikeRepository.save(CommentLike.of(parent2, user));
            commentLikeRepository.save(CommentLike.of(child3, user));

            // when
            PostDetailResponse response = postQueryService.getPostById(postId, user);

            // then - 부모 댓글 정렬 확인
            assertThat(response.comments()).hasSize(3);
            assertThat(response.comments().get(0).content()).isEqualTo("부모1");
            assertThat(response.comments().get(1).content()).isEqualTo("부모2");
            assertThat(response.comments().get(2).content()).isEqualTo("부모3");

            // 각 부모의 대댓글 정렬 및 내용 확인
            assertThat(response.comments().get(0).childComments()).hasSize(1);
            assertThat(response.comments().get(0).childComments().get(0).content()).isEqualTo("자식1");

            assertThat(response.comments().get(1).childComments()).hasSize(1);
            assertThat(response.comments().get(1).childComments().get(0).content()).isEqualTo("자식2");

            assertThat(response.comments().get(2).childComments()).hasSize(1);
            assertThat(response.comments().get(2).childComments().get(0).content()).isEqualTo("자식3");

            // 좋아요 여부 확인
            assertThat(response.comments().get(0).isLiked()).isFalse();
            assertThat(response.comments().get(1).isLiked()).isTrue();
            assertThat(response.comments().get(2).isLiked()).isFalse();

            assertThat(response.comments().get(0).childComments().get(0).isLiked()).isFalse();
            assertThat(response.comments().get(1).childComments().get(0).isLiked()).isFalse();
            assertThat(response.comments().get(2).childComments().get(0).isLiked()).isTrue();
        }
    }

    @Nested
    @DisplayName("큐레이션 포스트 목록 조회 테스트")
    class GetAllPublishedCurationPostsTest {

        @Test
        @DisplayName("eras, continents 필터 없이 조회 시 PUBLISHED 큐레이션 포스트 전체가 반환된다")
        void getAllPublishedCurationPosts_WithoutFilter_ReturnsAllPublishedCurationPosts() {
            // given
            Composer composerBaroque = Composer.builder()
                    .koreanName("비발디")
                    .englishName("Antonio Vivaldi")
                    .gender(Gender.MALE)
                    .era(Era.BAROQUE)
                    .continent(Continent.EUROPE)
                    .build();

            Composer composerClassical = Composer.builder()
                    .koreanName("모차르트")
                    .englishName("Wolfgang Amadeus Mozart")
                    .gender(Gender.MALE)
                    .era(Era.CLASSICAL)
                    .continent(Continent.EUROPE)
                    .build();

            composerRepository.save(composerBaroque);
            composerRepository.save(composerClassical);

            CurationPost post1 = CurationPost.from(
                    new PostCreateVo.Curation(
                            user,
                            "비발디 큐레이션",
                            "비발디에 대한 큐레이션",
                            PostStatus.PUBLISHED,
                            List.of("curation1.jpg"),
                            null,
                            List.of("#비발디"),
                            composerBaroque,
                            List.of()
                    )
            );
            CurationPost post2 = CurationPost.from(
                    new PostCreateVo.Curation(
                            user,
                            "모차르트 큐레이션",
                            "모차르트에 대한 큐레이션",
                            PostStatus.PUBLISHED,
                            List.of("curation2.jpg"),
                            null,
                            List.of("#모차르트"),
                            composerClassical,
                            List.of()
                    )
            );
            postRepository.save(post1);
            postRepository.save(post2);

            PageRequestDto pageRequest = new PageRequestDto(null, 100);

            // when
            PageResponseDto<PostResponseDto> response =
                    postQueryService.getAllPublishedCurationPosts(pageRequest, null, null, null);

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getContent())
                    .extracting(PostResponseDto::title)
                    .containsExactlyInAnyOrder("비발디 큐레이션", "모차르트 큐레이션");
        }

        @Test
        @DisplayName("eras 필터 적용 시 해당 시대의 primaryComposer를 가진 큐레이션 포스트만 반환된다")
        void getAllPublishedCurationPosts_WithErasFilter_ReturnsOnlyMatchingEra() {
            // given
            Composer composerBaroque = Composer.builder()
                    .koreanName("비발디")
                    .englishName("Antonio Vivaldi")
                    .gender(Gender.MALE)
                    .era(Era.BAROQUE)
                    .continent(Continent.EUROPE)
                    .build();

            Composer composerClassical = Composer.builder()
                    .koreanName("모차르트")
                    .englishName("Wolfgang Amadeus Mozart")
                    .gender(Gender.MALE)
                    .era(Era.CLASSICAL)
                    .continent(Continent.EUROPE)
                    .build();

            composerRepository.save(composerBaroque);
            composerRepository.save(composerClassical);

            CurationPost baroquePost = CurationPost.from(
                    new PostCreateVo.Curation(
                            user,
                            "바로크 큐레이션",
                            "바로크 내용",
                            PostStatus.PUBLISHED,
                            List.of(),
                            null,
                            List.of(),
                            composerBaroque,
                            List.of()
                    )
            );

            CurationPost classicalPost = CurationPost.from(
                    new PostCreateVo.Curation(
                            user,
                            "고전 큐레이션",
                            "고전 내용",
                            PostStatus.PUBLISHED,
                            List.of(),
                            null,
                            List.of(),
                            composerClassical,
                            List.of()
                    )
            );
            postRepository.save(baroquePost);
            postRepository.save(classicalPost);

            PageRequestDto pageRequest = new PageRequestDto(null, 100);

            // when - BAROQUE만 필터
            PageResponseDto<PostResponseDto> response =
                    postQueryService.getAllPublishedCurationPosts(pageRequest, null, List.of(Era.BAROQUE), null);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().getFirst().title()).isEqualTo("바로크 큐레이션");
            assertThat(response.getContent().getFirst().primaryComposer().era()).isEqualTo(Era.BAROQUE);
        }

        @Test
        @DisplayName("continents 필터 적용 시 해당 대륙의 primaryComposer를 가진 큐레이션 포스트만 반환된다")
        void getAllPublishedCurationPosts_WithContinentsFilter_ReturnsOnlyMatchingContinent() {
            // given
            Composer composerEurope = Composer.builder()
                    .koreanName("비발디")
                    .englishName("Antonio Vivaldi")
                    .gender(Gender.MALE)
                    .era(Era.BAROQUE)
                    .continent(Continent.EUROPE)
                    .build();

            Composer composerAsia = Composer.builder()
                    .koreanName("홍길동")
                    .englishName("Hong Gildong")
                    .gender(Gender.MALE)
                    .era(Era.BAROQUE)
                    .continent(Continent.ASIA)
                    .build();

            composerRepository.save(composerEurope);
            composerRepository.save(composerAsia);

            CurationPost europePost = CurationPost.from(
                    new PostCreateVo.Curation(
                            user,
                            "유럽 큐레이션",
                            "유럽 내용",
                            PostStatus.PUBLISHED,
                            List.of(),
                            null,
                            List.of(),
                            composerEurope,
                            List.of()
                    )
            );

            CurationPost asiaPost = CurationPost.from(
                    new PostCreateVo.Curation(
                            user,
                            "아시아 큐레이션",
                            "아시아 내용",
                            PostStatus.PUBLISHED,
                            List.of(),
                            null,
                            List.of(),
                            composerAsia,
                            List.of()
                    )
            );
            postRepository.save(europePost);
            postRepository.save(asiaPost);

            PageRequestDto pageRequest = new PageRequestDto(null, 100);

            // when - EUROPE만 필터
            PageResponseDto<PostResponseDto> response =
                    postQueryService.getAllPublishedCurationPosts(pageRequest, null, null, List.of(Continent.EUROPE));

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().getFirst().title()).isEqualTo("유럽 큐레이션");
            assertThat(response.getContent().getFirst().primaryComposer().continent()).isEqualTo(Continent.EUROPE);
        }

        @Test
        @DisplayName("eras와 continents 필터를 함께 적용하면 두 조건을 모두 만족하는 큐레이션 포스트만 반환된다")
        void getAllPublishedCurationPosts_WithErasAndContinents_ReturnsOnlyMatchingBoth() {
            // given
            Composer baroqueEurope = Composer.builder()
                    .koreanName("비발디")
                    .englishName("Antonio Vivaldi")
                    .gender(Gender.MALE)
                    .era(Era.BAROQUE)
                    .continent(Continent.EUROPE)
                    .build();

            Composer classicalEurope = Composer.builder()
                    .koreanName("모차르트")
                    .englishName("Mozart")
                    .gender(Gender.MALE)
                    .era(Era.CLASSICAL)
                    .continent(Continent.EUROPE)
                    .build();

            Composer baroqueAsia = Composer.builder()
                    .koreanName("아시아 바로크")
                    .englishName("Asia Baroque")
                    .gender(Gender.MALE)
                    .era(Era.BAROQUE)
                    .continent(Continent.ASIA)
                    .build();

            composerRepository.save(baroqueEurope);
            composerRepository.save(classicalEurope);
            composerRepository.save(baroqueAsia);

            CurationPost post1 = CurationPost.from(
                    new PostCreateVo.Curation(user, "바로크 유럽", "내용", PostStatus.PUBLISHED, List.of(), null, List.of(), baroqueEurope, List.of()));
            CurationPost post2 = CurationPost.from(
                    new PostCreateVo.Curation(user, "고전 유럽", "내용", PostStatus.PUBLISHED, List.of(), null, List.of(), classicalEurope, List.of()));
            CurationPost post3 = CurationPost.from(
                    new PostCreateVo.Curation(user, "바로크 아시아", "내용", PostStatus.PUBLISHED, List.of(), null, List.of(), baroqueAsia, List.of()));
            postRepository.save(post1);
            postRepository.save(post2);
            postRepository.save(post3);

            PageRequestDto pageRequest = new PageRequestDto(null, 100);

            // when - BAROQUE + EUROPE
            PageResponseDto<PostResponseDto> response = postQueryService.getAllPublishedCurationPosts(
                    pageRequest, null, List.of(Era.BAROQUE), List.of(Continent.EUROPE));

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().getFirst().title()).isEqualTo("바로크 유럽");
            assertThat(response.getContent().getFirst().primaryComposer().era()).isEqualTo(Era.BAROQUE);
            assertThat(response.getContent().getFirst().primaryComposer().continent()).isEqualTo(Continent.EUROPE);
        }

        @Test
        @DisplayName("응답에 primaryComposer(id, koreanName, era, continent)가 포함된다")
        void getAllPublishedCurationPosts_ResponseContainsPrimaryComposer() {
            // given
            Composer composer = Composer.builder()
                    .koreanName("베토벤")
                    .englishName("Ludwig van Beethoven")
                    .gender(Gender.MALE)
                    .era(Era.CLASSICAL)
                    .continent(Continent.EUROPE)
                    .build();

            composerRepository.save(composer);

            CurationPost curationPost = CurationPost.from(
                    new PostCreateVo.Curation(
                            user,
                            "베토벤 큐레이션",
                            "베토벤에 대한 큐레이션",
                            PostStatus.PUBLISHED,
                            List.of("curation.jpg"),
                            null,
                            List.of("#베토벤", "#큐레이션"),
                            composer,
                            List.of()
                    )
            );
            postRepository.save(curationPost);

            PageRequestDto pageRequest = new PageRequestDto(null, 10);

            // when
            PageResponseDto<PostResponseDto> response =
                    postQueryService.getAllPublishedCurationPosts(pageRequest, null, null, null);

            // then
            assertThat(response.getContent()).hasSize(1);
            PostResponseDto dto = response.getContent().getFirst();
            assertThat(dto.primaryComposer()).isNotNull();
            assertThat(dto.primaryComposer().id()).isEqualTo(composer.getId());
            assertThat(dto.primaryComposer().koreanName()).isEqualTo("베토벤");
            assertThat(dto.primaryComposer().era()).isEqualTo(Era.CLASSICAL);
            assertThat(dto.primaryComposer().continent()).isEqualTo(Continent.EUROPE);
        }
    }

    @Nested
    @DisplayName("작곡가 정보와 포스트 목록 조회 테스트")
    class GetComposerWithPostsTest {

        @Test
        @DisplayName("작곡가 정보와 해당 작곡가의 STORY, CURATION 포스트 목록을 조회한다")
        void getComposerWithPosts_ReturnsComposerAndPosts() {
            // given
            Composer composer = Composer.builder()
                    .koreanName("베토벤")
                    .englishName("Ludwig van Beethoven")
                    .nativeName("Ludwig van Beethoven")
                    .nationality("독일")
                    .gender(Gender.MALE)
                    .birthYear((short) 1770)
                    .deathYear((short) 1827)
                    .era(Era.CLASSICAL)
                    .continent(Continent.EUROPE)
                    .build();

            composerRepository.save(composer);

            // StoryPost 생성
            StoryPost storyPost = StoryPost.from(
                    new PostCreateVo.Story(
                            user,
                            "베토벤 스토리",
                            "베토벤에 대한 스토리",
                            PostStatus.PUBLISHED,
                            List.of("story.jpg"),
                            null,
                            List.of("#베토벤", "#스토리"),
                            composer
                    )
            );
            postRepository.save(storyPost);

            // CurationPost 생성
            CurationPost curationPost = CurationPost.from(
                    new PostCreateVo.Curation(
                            user,
                            "베토벤 큐레이션",
                            "베토벤에 대한 큐레이션",
                            PostStatus.PUBLISHED,
                            List.of("curation.jpg"),
                            null,
                            List.of("#베토벤", "#큐레이션"),
                            composer,
                            List.of()
                    )
            );
            postRepository.save(curationPost);

            // FreePost 생성 (제외되어야 함)
            FreePost freePost = FreePost.from(
                    new PostCreateVo.Free(
                            user,
                            "자유 포스트",
                            "자유 포스트 내용",
                            PostStatus.PUBLISHED,
                            List.of("free.jpg"),
                            null,
                            List.of("#자유")
                    )
            );
            postRepository.save(freePost);

            PageRequestDto pageRequest = new PageRequestDto(null, 100);

            // when
            ComposerWithPostsResponseDto response =
                    postQueryService.getComposerWithPosts(composer.getId(), pageRequest, null);

            // then
            assertThat(response.composer().composerId()).isEqualTo(composer.getId());
            assertThat(response.composer().koreanName()).isEqualTo("베토벤");
            assertThat(response.composer().englishName()).isEqualTo("Ludwig van Beethoven");
            assertThat(response.composer().isLiked()).isFalse(); // 비로그인 유저

            // STORY와 CURATION 포스트만 포함되어야 함
            assertThat(response.posts().getContent()).hasSize(2);
            assertThat(response.posts().getContent())
                    .extracting(PostResponseDto::title)
                    .containsExactlyInAnyOrder("베토벤 스토리", "베토벤 큐레이션");
            assertThat(response.posts().getContent())
                    .extracting(PostResponseDto::title)
                    .doesNotContain("자유 포스트");
        }

        @Test
        @DisplayName("로그인한 유저가 좋아요한 작곡가의 경우 isLiked가 true로 반환된다")
        void getComposerWithPosts_WithLikedComposer_ReturnsIsLikedTrue() {
            // given
            Composer composer = Composer.builder()
                    .koreanName("베토벤")
                    .englishName("Ludwig van Beethoven")
                    .gender(Gender.MALE)
                    .era(Era.CLASSICAL)
                    .continent(Continent.EUROPE)
                    .build();

            composerRepository.save(composer);

            // 유저가 작곡가를 좋아요
            composerLikeRepository.save(ComposerLike.of(composer, user));

            PageRequestDto pageRequest = new PageRequestDto(null, 10);

            // when
            ComposerWithPostsResponseDto response =
                    postQueryService.getComposerWithPosts(composer.getId(), pageRequest, user);

            // then
            assertThat(response.composer().isLiked()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 작곡가 ID로 조회하면 NotFoundException이 발생한다")
        void getComposerWithPosts_WithNonExistentId_ThrowsNotFoundException() {
            // given
            Long nonExistentComposerId = 999L;
            PageRequestDto pageRequest = new PageRequestDto(null, 10);

            // when & then
            assertThatThrownBy(() -> postQueryService.getComposerWithPosts(nonExistentComposerId, pageRequest, null))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("존재하지 않는 Composer입니다");
        }

        @Test
        @DisplayName("PUBLISHED 상태인 포스트만 반환된다")
        void getComposerWithPosts_ReturnsOnlyPublishedPosts() {
            // given
            Composer composer = Composer.builder()
                    .koreanName("베토벤")
                    .englishName("Ludwig van Beethoven")
                    .gender(Gender.MALE)
                    .era(Era.CLASSICAL)
                    .continent(Continent.EUROPE)
                    .build();

            composerRepository.save(composer);

            // PUBLISHED StoryPost
            StoryPost publishedPost = StoryPost.from(
                    new PostCreateVo.Story(
                            user,
                            "PUBLISHED 스토리",
                            "PUBLISHED 내용",
                            PostStatus.PUBLISHED,
                            List.of(),
                            null,
                            List.of(),
                            composer
                    )
            );
            postRepository.save(publishedPost);

            // DRAFT StoryPost (제외되어야 함)
            StoryPost draftPost = StoryPost.from(
                    new PostCreateVo.Story(
                            user,
                            "DRAFT 스토리",
                            "DRAFT 내용",
                            PostStatus.DRAFT,
                            List.of(),
                            null,
                            List.of(),
                            composer
                    )
            );
            postRepository.save(draftPost);

            PageRequestDto pageRequest = new PageRequestDto(null, 100);

            // when
            ComposerWithPostsResponseDto response =
                    postQueryService.getComposerWithPosts(composer.getId(), pageRequest, null);

            // then
            assertThat(response.posts().getContent()).hasSize(1);
            assertThat(response.posts().getContent().getFirst().title()).isEqualTo("PUBLISHED 스토리");
            assertThat(response.posts().getContent())
                    .extracting(PostResponseDto::title)
                    .doesNotContain("DRAFT 스토리");
        }
    }
}

