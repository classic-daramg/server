package com.daramg.server.notification.event;

import com.daramg.server.notification.application.NotificationService;
import com.daramg.server.notification.domain.NotificationType;
import com.daramg.server.post.domain.FreePost;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.domain.PostStatus;
import com.daramg.server.post.domain.vo.PostCreateVo;
import com.daramg.server.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @InjectMocks
    private NotificationEventListener notificationEventListener;

    @Mock
    private NotificationService notificationService;

    private NotificationEvent event;

    @BeforeEach
    void setUp() {
        User receiver = new User("receiver@test.com", "password", "receiver", LocalDate.now(), "profile", "받는사람", "bio", null);
        ReflectionTestUtils.setField(receiver, "id", 1L);

        User sender = new User("sender@test.com", "password", "sender", LocalDate.now(), "profile", "보내는사람", "bio", null);
        ReflectionTestUtils.setField(sender, "id", 2L);

        Post post = FreePost.from(new PostCreateVo.Free(
                receiver, "제목", "내용", PostStatus.PUBLISHED, List.of(), null, List.of()
        ));
        ReflectionTestUtils.setField(post, "id", 1L);

        event = new NotificationEvent(receiver, sender, post, NotificationType.COMMENT);
    }

    @Test
    @DisplayName("알림 저장이 실패해도 예외를 다시 던지지 않는다")
    void handleNotificationEventDoesNotRethrow() {
        // given
        doThrow(new RuntimeException("notification failed"))
                .when(notificationService).createFromEvent(any(NotificationEvent.class));

        // when & then
        assertThatCode(() -> notificationEventListener.handleNotificationEvent(event))
                .doesNotThrowAnyException();
        verify(notificationService).createFromEvent(event);
    }
}
