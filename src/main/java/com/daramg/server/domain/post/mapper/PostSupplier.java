package com.daramg.server.domain.post.mapper;

import com.daramg.server.domain.post.domain.Post;
import com.daramg.server.domain.post.dto.PostRequest;

@FunctionalInterface
public interface PostSupplier {
    Post get(PostRequest request);
}
