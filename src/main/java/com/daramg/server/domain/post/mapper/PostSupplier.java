package com.daramg.server.domain.post.mapper;

import com.daramg.server.domain.post.domain.Post;
import com.daramg.server.domain.post.dto.PostRequest;
import com.daramg.server.domain.user.User;

@FunctionalInterface
public interface PostSupplier {
    Post get(PostRequest request, User user);
}
