package com.daramg.server.search.application;

import com.daramg.server.composer.domain.Composer;
import com.daramg.server.composer.repository.ComposerRepository;
import com.daramg.server.post.domain.*;
import com.daramg.server.post.repository.PostRepository;
import com.daramg.server.search.domain.SearchLog;
import com.daramg.server.search.dto.SearchResponseDto;
import com.daramg.server.search.repository.SearchLogRepository;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SearchService {

    private final ComposerRepository composerRepository;
    private final PostRepository postRepository;
    private final SearchLogRepository searchLogRepository;

    public SearchResponseDto search(String keyword, User user) {
        searchLogRepository.save(SearchLog.of(keyword, user != null ? user.getId() : null));

        List<Composer> composers = composerRepository
                .findByKoreanNameContainingOrEnglishNameContaining(keyword, keyword);

        List<Post> posts = postRepository
                .findByTitleContainingAndPostStatusAndIsBlockedFalse(keyword, PostStatus.PUBLISHED);

        return new SearchResponseDto(
                composers.stream().map(SearchResponseDto.ComposerResult::from).toList(),
                posts.stream().map(p -> SearchResponseDto.PostResult.from(p, resolvePostType(p))).toList()
        );
    }

    private PostType resolvePostType(Post post) {
        if (post instanceof StoryPost) return PostType.STORY;
        if (post instanceof FreePost) return PostType.FREE;
        if (post instanceof CurationPost) return PostType.CURATION;
        throw new IllegalStateException("Unknown post type: " + post.getClass());
    }
}
