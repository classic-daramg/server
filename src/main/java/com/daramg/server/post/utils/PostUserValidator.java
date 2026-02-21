package com.daramg.server.post.utils;

import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.post.domain.Post;
import com.daramg.server.post.exception.PostErrorStatus;
import com.daramg.server.user.domain.User;

public class PostUserValidator {

    public static void check(Post post, User user) {
        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException(PostErrorStatus.NOT_POST_AUTHOR);
        }
    }
}
