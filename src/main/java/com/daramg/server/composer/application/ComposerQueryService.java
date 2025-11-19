package com.daramg.server.composer.application;

import com.daramg.server.composer.domain.Composer;
import com.daramg.server.composer.domain.Continent;
import com.daramg.server.composer.domain.Era;
import com.daramg.server.composer.dto.ComposerResponseDto;
import com.daramg.server.composer.repository.ComposerLikeRepository;
import com.daramg.server.composer.repository.ComposerRepository;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComposerQueryService {

    private final ComposerRepository composerRepository;
    private final ComposerLikeRepository composerLikeRepository;

    public List<ComposerResponseDto> getAllComposers(User user, List<Era> eras, List<Continent> continents) {
        Set<Long> likedComposerIds = getLikedComposerIds(user);
        List<Composer> allComposers = composerRepository.findAll();

        return allComposers.stream()
                .filter(composer -> eras == null || eras.isEmpty() || eras.contains(composer.getEra()))
                .filter(composer -> continents == null || continents.isEmpty() || continents.contains(composer.getContinent()))
                .map(composer -> ComposerResponseDto.from(
                        composer,
                        likedComposerIds.contains(composer.getId())
                ))
                .sorted(
                        Comparator.comparing(ComposerResponseDto::isLiked).reversed()
                                .thenComparing(ComposerResponseDto::koreanName)
                )
                .collect(Collectors.toList());
    }

    /**
     * 유저가 '좋아요'한 작곡가 ID Set 반환 (비로그인 시 빈 Set)
     */
    private Set<Long> getLikedComposerIds(User user) {
        return (user != null)
                ? composerLikeRepository.findComposerIdsByUserId(user.getId())
                : Collections.emptySet();
    }
}
