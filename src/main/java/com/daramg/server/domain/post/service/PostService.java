package com.daramg.server.domain.post.service;

import com.daramg.server.domain.post.domain.Post;
import com.daramg.server.domain.post.dto.PostRequest;
import com.daramg.server.domain.post.mapper.PostMapper;
import com.daramg.server.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    public void create(PostRequest request) {
        Post post = postMapper.toEntity(request);
        postRepository.save(post);
    }
}

