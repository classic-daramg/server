package com.daramg.server.composer.application;

import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.composer.domain.Composer;
import com.daramg.server.composer.domain.ComposerLike;
import com.daramg.server.composer.dto.ComposerLikeResponseDto;
import com.daramg.server.composer.repository.ComposerLikeRepository;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComposerService {

    private final ComposerLikeRepository composerLikeRepository;
    private final EntityUtils entityUtils;

    @Transactional
    public ComposerLikeResponseDto toggleLike(Long composerId, User user) {
        Composer composer = entityUtils.getEntity(composerId, Composer.class);

        boolean alreadyLiked = composerLikeRepository
                .existsByComposerIdAndUserId(composerId, user.getId());
        if (alreadyLiked) {
            composerLikeRepository.deleteByComposerIdAndUserId(composerId, user.getId());
            return new ComposerLikeResponseDto(false);
        }

        composerLikeRepository.save(ComposerLike.of(composer, user));
        return new ComposerLikeResponseDto(true);
    }
}
