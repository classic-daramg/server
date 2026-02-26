package com.daramg.server.comment.application;

import com.daramg.server.comment.domain.Comment;
import com.daramg.server.comment.dto.CommentLikeResponseDto;
import com.daramg.server.comment.repository.CommentLikeRepository;
import com.daramg.server.comment.repository.CommentRepository;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.PostStatus;
import com.daramg.server.post.domain.vo.PostCreateVo;
import com.daramg.server.post.dto.CommentCreateDto;
import com.daramg.server.post.dto.CommentReplyCreateDto;
import com.daramg.server.post.repository.PostRepository;
import com.daramg.server.testsupport.support.ServiceTestSupport;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CommentServiceTest extends ServiceTestSupport {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = new User("email@test.com", "password", "name", LocalDate.now(), "profile", "별명", "bio", null);
        userRepository.save(user);

        post = FreePost.from(
                new PostCreateVo.Free(
                        user,
                        "제목",
                        "내용",
                        PostStatus.PUBLISHED,
                        List.of("img1"),
                        "video",
                        List.of("#tag")
                )
        );
        post = postRepository.save(post);
    }

    @Nested
    @DisplayName("댓글/대댓글 생성 성공")
    class CreateCommentSuccess {
        @Test
        void 댓글을_정상적으로_생성한다() {
            // given
            CommentCreateDto request = new CommentCreateDto("댓글 내용");

            // when
            commentService.createComment(post.getId(), request, user);

            // then
            List<Comment> all = commentRepository.findAll();
            assertThat(all).hasSize(1);
            Comment saved = all.getFirst();
            assertThat(saved.getPost().getId()).isEqualTo(post.getId());
            assertThat(saved.getUser().getId()).isEqualTo(user.getId());
            assertThat(saved.getContent()).isEqualTo("댓글 내용");
        }

        @Test
        void 대댓글을_정상적으로_생성한다() {
            // given - 부모 댓글 생성
            Comment parent = Comment.of(post, user, "부모 댓글", null);
            commentRepository.save(parent);

            CommentReplyCreateDto request = new CommentReplyCreateDto("대댓글 내용");

            // when
            commentService.createReply(parent.getId(), request, user);

            // then
            List<Comment> all = commentRepository.findAll();
            assertThat(all).hasSize(2);
            Comment reply = all.stream().filter(c -> c.getParentComment() != null).findFirst().orElseThrow();
            assertThat(reply.getParentComment().getId()).isEqualTo(parent.getId());
            assertThat(reply.getPost().getId()).isEqualTo(post.getId());
            assertThat(reply.getUser().getId()).isEqualTo(user.getId());
        }
    }

    @Nested
    @Transactional
    @DisplayName("댓글/대댓글 생성 실패")
    class CreateCommentFail {
        @Test
        void 블락된_포스트에는_댓글을_남길_수_없다() {
            // given
            ReflectionTestUtils.setField(post, "isBlocked", true);
            CommentCreateDto request = new CommentCreateDto("댓글 내용");

            // when & then
            assertThatThrownBy(() -> commentService.createComment(post.getId(), request, user))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("블락된 포스트에는 댓글을 남길 수 없습니다.");
        }

        @Test
        void 삭제된_댓글에는_대댓글을_남길_수_없다() {
            // given
            Comment parent = Comment.of(post, user, "부모", null);
            commentRepository.save(parent);
            parent.softDelete();

            CommentReplyCreateDto request = new CommentReplyCreateDto("대댓글");

            // when & then
            assertThatThrownBy(() -> commentService.createReply(parent.getId(), request, user))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("삭제되었거나 블락된 댓글에는 대댓글을 남길 수 없습니다.");
        }

        @Test
        void 블락된_댓글에는_대댓글을_남길_수_없다() {
            // given
            Comment parent = Comment.of(post, user, "부모", null);
            commentRepository.save(parent);
            ReflectionTestUtils.setField(parent, "isBlocked", true);

            CommentReplyCreateDto request = new CommentReplyCreateDto("대댓글");

            // when & then
            assertThatThrownBy(() -> commentService.createReply(parent.getId(), request, user))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("삭제되었거나 블락된 댓글에는 대댓글을 남길 수 없습니다.");
        }

        @Test
        void 블락된_포스트에는_대댓글을_남길_수_없다() {
            // given
            Comment parent = Comment.of(post, user, "부모", null);
            commentRepository.save(parent);
            ReflectionTestUtils.setField(post, "isBlocked", true);

            CommentReplyCreateDto request = new CommentReplyCreateDto("대댓글");

            // when & then
            assertThatThrownBy(() -> commentService.createReply(parent.getId(), request, user))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("블락된 포스트에는 댓글을 남길 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("댓글 좋아요 토글")
    class CommentLikeToggleTest {
        @Test
        void 좋아요를_추가한다() {
            // given
            Comment comment = commentRepository.save(Comment.of(post, user, "내용", null));

            // when
            CommentLikeResponseDto response = commentService.toggleCommentLike(comment.getId(), user);

            // then
            assertThat(response.isLiked()).isTrue();
            assertThat(response.getLikeCount()).isEqualTo(1);
        }

        @Test
        void 이미_좋아요한_댓글은_좋아요가_취소된다() {
            // given
            Comment comment = commentRepository.save(Comment.of(post, user, "내용", null));
            commentService.toggleCommentLike(comment.getId(), user); // first like

            // when
            CommentLikeResponseDto response = commentService.toggleCommentLike(comment.getId(), user);

            // then
            assertThat(response.isLiked()).isFalse();
            assertThat(response.getLikeCount()).isEqualTo(0);
        }

        @Test
        @Transactional
        void 삭제되었거나_블락된_댓글에는_좋아요를_누를_수_없다_삭제된_경우() {
            // given
            Comment comment = commentRepository.save(Comment.of(post, user, "내용", null));
            comment.softDelete();

            // when & then
            assertThatThrownBy(() -> commentService.toggleCommentLike(comment.getId(), user))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("삭제되었거나 블락된 댓글에는 좋아요를 누를 수 없습니다.");
        }

        @Test
        @Transactional
        void 삭제되었거나_블락된_댓글에는_좋아요를_누를_수_없다_블락된_경우() {
            // given
            Comment comment = commentRepository.save(Comment.of(post, user, "내용", null));
            ReflectionTestUtils.setField(comment, "isBlocked", true);

            // when & then
            assertThatThrownBy(() -> commentService.toggleCommentLike(comment.getId(), user))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("삭제되었거나 블락된 댓글에는 좋아요를 누를 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteCommentTest {
        @Test
        void 댓글을_정상적으로_삭제한다() {
            // given
            Comment comment = commentRepository.save(Comment.of(post, user, "삭제할 댓글", null));
            // 좋아요 데이터 추가 후 삭제 시 초기화 확인
            commentService.toggleCommentLike(comment.getId(), user);

            // when
            commentService.deleteComment(comment.getId(), user);

            // then
            Comment deleted = commentRepository.findById(comment.getId()).orElseThrow();
            assertThat(ReflectionTestUtils.getField(deleted, "isDeleted")).isEqualTo(true);
            assertThat(deleted.getLikeCount()).isEqualTo(0);
            assertThat(commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), user.getId())).isFalse();
        }

        @Test
        @Transactional
        void 이미_삭제된_댓글은_삭제할_수_없다() {
            // given
            Comment comment = commentRepository.save(Comment.of(post, user, "이미 삭제됨", null));
            comment.softDelete();

            // when & then
            assertThatThrownBy(() -> commentService.deleteComment(comment.getId(), user))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("이미 삭제 처리된 댓글입니다.");
        }

        @Test
        void 작성자가_아니면_댓글을_삭제할_수_없다() {
            // given
            Comment comment = commentRepository.save(Comment.of(post, user, "본인 댓글", null));
            User another = new User("another@test.com", "password", "name2", LocalDate.now(), "profile2", "별명2", "bio2", null);
            userRepository.save(another);

            // when & then
            assertThatThrownBy(() -> commentService.deleteComment(comment.getId(), another))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("댓글을 작성한 유저만 댓글을 삭제할 수 있습니다.");
        }

        @Test
        void 댓글_삭제_시_포스트의_댓글_카운트가_감소한다() {
            // given
            commentService.createComment(post.getId(), new CommentCreateDto("댓글"), user);
            assertThat(postRepository.findById(post.getId()).orElseThrow().getCommentCount()).isEqualTo(1);
            Comment comment = commentRepository.findAll().getFirst();

            // when
            commentService.deleteComment(comment.getId(), user);

            // then
            assertThat(postRepository.findById(post.getId()).orElseThrow().getCommentCount()).isEqualTo(0);
        }
    }

    @Test
    void 엔티티_생성_시_createdAt이_UTC_Instant로_저장된다() {
        Instant before = Instant.now();
        commentRepository.save(Comment.of(post, user, "테스트", null));
        Instant after = Instant.now();

        Comment saved = commentRepository.findAll().getFirst();
        assertThat(saved.getCreatedAt()).isAfterOrEqualTo(before);
        assertThat(saved.getCreatedAt()).isBeforeOrEqualTo(after);
    }
}


