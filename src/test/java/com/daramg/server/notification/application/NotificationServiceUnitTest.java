package com.daramg.server.notification.application;

import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.notification.domain.Notification;
import com.daramg.server.notification.domain.NotificationType;
import com.daramg.server.notification.repository.NotificationRepository;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.PostStatus;
import com.daramg.server.post.domain.vo.PostCreateVo;
import com.daramg.server.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceUnitTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EntityUtils entityUtils;

    private User receiver;
    private User sender;
    private Post post;
    private Notification notification;

    @BeforeEach
    void setUp() {
        receiver = new User("receiver@test.com", "password", "receiver", LocalDate.now(), "profile", "받는사람", "bio", null);
        ReflectionTestUtils.setField(receiver, "id", 1L);

        sender = new User("sender@test.com", "password", "sender", LocalDate.now(), "profile", "보내는사람", "bio", null);
        ReflectionTestUtils.setField(sender, "id", 2L);

        post = FreePost.from(new PostCreateVo.Free(
                receiver, "제목", "내용", PostStatus.PUBLISHED, List.of(), null, List.of()
        ));
        ReflectionTestUtils.setField(post, "id", 1L);

        notification = Notification.of(receiver, sender, post, NotificationType.COMMENT);
        ReflectionTestUtils.setField(notification, "id", 1L);
    }

    @Nested
    @DisplayName("알림 읽음 처리")
    class MarkAsReadTest {
        @Test
        void 알림을_읽음_처리한다() {
            // given
            when(entityUtils.getEntity(1L, Notification.class)).thenReturn(notification);

            // when
            notificationService.markAsRead(1L, receiver);

            // then
            assertThat(notification.isRead()).isTrue();
        }

        @Test
        void 본인의_알림이_아니면_읽음_처리할_수_없다() {
            // given
            when(entityUtils.getEntity(1L, Notification.class)).thenReturn(notification);

            // when & then
            assertThatThrownBy(() -> notificationService.markAsRead(1L, sender))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("본인의 알림만 처리할 수 있습니다.");
        }
    }

    @Nested
    @DisplayName("전체 읽음 처리")
    class MarkAllAsReadTest {
        @Test
        void 전체_알림을_읽음_처리한다() {
            // when
            notificationService.markAllAsRead(receiver);

            // then
            verify(notificationRepository).markAllAsReadByReceiverId(receiver.getId());
        }
    }

    @Nested
    @DisplayName("알림 삭제")
    class DeleteTest {
        @Test
        void 알림을_삭제한다() {
            // given
            when(entityUtils.getEntity(1L, Notification.class)).thenReturn(notification);

            // when
            notificationService.delete(1L, receiver);

            // then
            verify(notificationRepository).delete(notification);
        }

        @Test
        void 본인의_알림이_아니면_삭제할_수_없다() {
            // given
            when(entityUtils.getEntity(1L, Notification.class)).thenReturn(notification);

            // when & then
            assertThatThrownBy(() -> notificationService.delete(1L, sender))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("본인의 알림만 처리할 수 있습니다.");
        }
    }
}
