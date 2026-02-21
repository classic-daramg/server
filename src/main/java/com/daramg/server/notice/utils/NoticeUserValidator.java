package com.daramg.server.notice.utils;

import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.notice.domain.Notice;
import com.daramg.server.notification.exception.NotificationErrorStatus;
import com.daramg.server.user.domain.User;

public class NoticeUserValidator {

    public static void check(Notice notice, User user) {
        if (!notice.getUser().getId().equals(user.getId())) {
            throw new BusinessException(NotificationErrorStatus.NOT_NOTICE_AUTHOR);
        }
    }
}
