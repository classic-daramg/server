package com.daramg.server.notification.application;

import com.daramg.server.comment.application.CommentService;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.notification.domain.Notification;
import com.daramg.server.notification.domain.NotificationType;
import com.daramg.server.notification.dto.NotificationResponseDto;
import com.daramg.server.notification.repository.NotificationRepository;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.PostStatus;
import com.daramg.server.post.domain.vo.PostCreateVo;
import com.daramg.server.post.application.PostService;
import com.daramg.server.post.dto.CommentCreateDto;
import com.daramg.server.post.dto.CommentReplyCreateDto;
import com.daramg.server.post.repository.PostRepository;
import com.daramg.server.testsupport.support.ServiceTestSupport;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.repository.UserRepository;
import com.daramg.server.comment.domain.Comment;
import com.daramg.server.comment.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class NotificationServiceIntegrationTest extends ServiceTestSupport {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationQueryService notificationQueryService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User postAuthor;
    private User actor;
    private Post post;

    @BeforeEach
    void setUp() {
        postAuthor = new User("author@test.com", "password", "작성자", LocalDate.now(), "profile", "작성자닉", "bio", null);
        userRepository.save(postAuthor);

        actor = new User("actor@test.com", "password", "행위자", LocalDate.now(), "profile", "행위자닉", "bio", null);
        userRepository.save(actor);

        post = FreePost.from(new PostCreateVo.Free(
                postAuthor, "제목", "내용", PostStatus.PUBLISHED, List.of(), null, List.of()
        ));
        postRepository.save(post);
    }

    @Nested
    @DisplayName("댓글 생성 시 알림")
    class CommentNotificationTest {
        @Test
        void 댓글을_작성하면_게시글_작성자에게_알림이_생성된다() {
            // when
            commentService.createComment(post.getId(), new CommentCreateDto("댓글입니다"), actor);

            // then
            List<Notification> notifications = notificationRepository.findAll();
            assertThat(notifications).hasSize(1);

            Notification notification = notifications.getFirst();
            assertThat(notification.getReceiver().getId()).isEqualTo(postAuthor.getId());
            assertThat(notification.getSender().getId()).isEqualTo(actor.getId());
            assertThat(notification.getPost().getId()).isEqualTo(post.getId());
            assertThat(notification.getType()).isEqualTo(NotificationType.COMMENT);
            assertThat(notification.isRead()).isFalse();
        }

        @Test
        void 자신의_게시글에_댓글을_작성하면_알림이_생성되지_않는다() {
            // when
            commentService.createComment(post.getId(), new CommentCreateDto("본인 댓글"), postAuthor);

            // then
            List<Notification> notifications = notificationRepository.findAll();
            assertThat(notifications).isEmpty();
        }
    }

    @Nested
    @DisplayName("대댓글 생성 시 알림")
    class ReplyNotificationTest {
        @Test
        void 대댓글을_작성하면_부모_댓글_작성자에게_알림이_생성된다() {
            // given
            Comment parent = Comment.of(post, postAuthor, "부모 댓글", null);
            commentRepository.save(parent);

            // when
            commentService.createReply(parent.getId(), new CommentReplyCreateDto("대댓글입니다"), actor);

            // then
            List<Notification> notifications = notificationRepository.findAll();
            assertThat(notifications).hasSize(1);

            Notification notification = notifications.getFirst();
            assertThat(notification.getReceiver().getId()).isEqualTo(postAuthor.getId());
            assertThat(notification.getSender().getId()).isEqualTo(actor.getId());
            assertThat(notification.getType()).isEqualTo(NotificationType.REPLY);
        }
    }

    @Nested
    @DisplayName("좋아요 시 알림")
    class PostLikeNotificationTest {
        @Test
        void 게시글에_좋아요를_누르면_작성자에게_알림이_생성된다() {
            // when
            postService.toggleLike(post.getId(), actor);

            // then
            List<Notification> notifications = notificationRepository.findAll();
            assertThat(notifications).hasSize(1);

            Notification notification = notifications.getFirst();
            assertThat(notification.getReceiver().getId()).isEqualTo(postAuthor.getId());
            assertThat(notification.getSender().getId()).isEqualTo(actor.getId());
            assertThat(notification.getType()).isEqualTo(NotificationType.POST_LIKE);
        }

        @Test
        void 자신의_게시글에_좋아요를_누르면_알림이_생성되지_않는다() {
            // when
            postService.toggleLike(post.getId(), postAuthor);

            // then
            List<Notification> notifications = notificationRepository.findAll();
            assertThat(notifications).isEmpty();
        }

        @Test
        void 좋아요_취소_시에는_알림이_생성되지_않는다() {
            // given
            postService.toggleLike(post.getId(), actor); // 좋아요
            notificationRepository.deleteAll();

            // when
            postService.toggleLike(post.getId(), actor); // 좋아요 취소

            // then
            List<Notification> notifications = notificationRepository.findAll();
            assertThat(notifications).isEmpty();
        }
    }

    @Nested
    @DisplayName("알림 조회")
    class QueryNotificationTest {
        @Test
        void 내_알림_목록을_조회한다() {
            // given
            commentService.createComment(post.getId(), new CommentCreateDto("댓글1"), actor);
            postService.toggleLike(post.getId(), actor);

            // when
            List<NotificationResponseDto> notifications = notificationQueryService.getNotifications(postAuthor);

            // then
            assertThat(notifications).hasSize(2);
            assertThat(notifications.getFirst().senderNickname()).isEqualTo("행위자닉");
            assertThat(notifications.getFirst().postTitle()).isEqualTo("제목");
        }

        @Test
        void 안읽은_알림_수를_조회한다() {
            // given
            commentService.createComment(post.getId(), new CommentCreateDto("댓글1"), actor);
            postService.toggleLike(post.getId(), actor);

            // when
            long unreadCount = notificationQueryService.getUnreadCount(postAuthor);

            // then
            assertThat(unreadCount).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("알림 읽음 처리")
    class MarkAsReadIntegrationTest {
        @Test
        void 알림을_읽음_처리하면_안읽은_수가_줄어든다() {
            // given
            commentService.createComment(post.getId(), new CommentCreateDto("댓글"), actor);
            postService.toggleLike(post.getId(), actor);

            Notification notification = notificationRepository.findAll().getFirst();

            // when
            notificationService.markAsRead(notification.getId(), postAuthor);

            // then
            long unreadCount = notificationQueryService.getUnreadCount(postAuthor);
            assertThat(unreadCount).isEqualTo(1);
        }

        @Test
        void 전체_읽음_처리하면_안읽은_수가_0이_된다() {
            // given
            commentService.createComment(post.getId(), new CommentCreateDto("댓글"), actor);
            postService.toggleLike(post.getId(), actor);

            // when
            notificationService.markAllAsRead(postAuthor);

            // then
            long unreadCount = notificationQueryService.getUnreadCount(postAuthor);
            assertThat(unreadCount).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("알림 삭제")
    class DeleteIntegrationTest {
        @Test
        void 알림을_삭제하면_목록에서_사라진다() {
            // given
            commentService.createComment(post.getId(), new CommentCreateDto("댓글"), actor);
            Notification notification = notificationRepository.findAll().getFirst();

            // when
            notificationService.delete(notification.getId(), postAuthor);

            // then
            List<NotificationResponseDto> notifications = notificationQueryService.getNotifications(postAuthor);
            assertThat(notifications).isEmpty();
        }

        @Test
        void 타인의_알림은_삭제할_수_없다() {
            // given
            commentService.createComment(post.getId(), new CommentCreateDto("댓글"), actor);
            Notification notification = notificationRepository.findAll().getFirst();

            // when & then
            assertThatThrownBy(() -> notificationService.delete(notification.getId(), actor))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("본인의 알림만 처리할 수 있습니다.");
        }
    }
}
