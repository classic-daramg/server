package com.daramg.server.domain.post.utils;

import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.domain.post.domain.Post;
import com.daramg.server.domain.user.User;

public class PostUserValidator {

    public static void check(Post post, User user) {
        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException(); // TODO: 예외정의
        }
    }
}
