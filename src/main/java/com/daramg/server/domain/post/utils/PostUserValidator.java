package com.daramg.server.domain.post.utils;

import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.domain.post.domain.Post;
import com.daramg.server.domain.user.domain.User;

public class PostUserValidator {

    public static void check(Post post, User user) {
        if (!post.getUser().getId().equals(user.getId())) {
            throw new BusinessException("포스트와 작성자가 일치하지 않습니다.");
        }
    }
}
