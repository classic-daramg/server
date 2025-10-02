package com.daramg.server.domain.post.service;

import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.domain.post.domain.Post;
import com.daramg.server.domain.post.dto.PostRequest;
import com.daramg.server.domain.post.mapper.PostMapper;
import com.daramg.server.domain.post.repository.PostRepository;
import com.daramg.server.domain.post.utils.PostUserValidator;
import com.daramg.server.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final EntityUtils entityUtils;

    public void create(PostRequest request, User user) {
        Post post = postMapper.toEntity(request, user);
        postRepository.save(post);
    }

    public void delete(Long postId, User user){
        Post post = entityUtils.getEntity(postId, Post.class);
        PostUserValidator.check(post, user);
        postRepository.deleteById(postId);
    }
}
