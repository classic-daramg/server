package com.daramg.server.composer.application;

import com.daramg.server.common.application.EntityUtils;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.common.exception.CommonErrorStatus;
import com.daramg.server.common.exception.NotFoundException;
import com.daramg.server.composer.domain.Composer;
import com.daramg.server.composer.domain.ComposerLike;
import com.daramg.server.composer.domain.ComposerPersona;
import com.daramg.server.composer.dto.ComposerCreateDto;
import com.daramg.server.composer.dto.ComposerLikeResponseDto;
import com.daramg.server.composer.dto.ComposerPersonaCreateDto;
import com.daramg.server.composer.dto.ComposerPersonaResponseDto;
import com.daramg.server.composer.dto.ComposerPersonaUpdateDto;
import com.daramg.server.composer.dto.ComposerUpdateDto;
import com.daramg.server.composer.repository.ComposerLikeRepository;
import com.daramg.server.composer.repository.ComposerPersonaRepository;
import com.daramg.server.composer.repository.ComposerRepository;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComposerService {

    private final ComposerLikeRepository composerLikeRepository;
    private final ComposerRepository composerRepository;
    private final ComposerPersonaRepository composerPersonaRepository;
    private final EntityUtils entityUtils;

    @Transactional
    public void createComposer(ComposerCreateDto dto, User user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(CommonErrorStatus.FORBIDDEN);
        }
        Composer composer = Composer.builder()
                .koreanName(dto.getKoreanName())
                .englishName(dto.getEnglishName())
                .nativeName(dto.getNativeName())
                .gender(dto.getGender())
                .nationality(dto.getNationality())
                .birthYear(dto.getBirthYear())
                .deathYear(dto.getDeathYear())
                .bio(dto.getBio())
                .era(dto.getEra())
                .continent(dto.getContinent())
                .build();
        composerRepository.save(composer);
    }

    /**
     * 작곡가 좋아요 상태를 토글(Toggle)합니다.
     * <p>
     * 유저가 이미 좋아요를 누른 상태라면 삭제(취소)하고 누르지 않은 상태라면 저장(추가)합니다.
     * <br>
     * <b>Note:</b> 동시성 이슈(Race Condition)를 최소화하기 위해 {@code exists} 대신
     * {@code findBy}를 사용하여 엔티티를 직접 조회한 후 처리합니다.
     *
     * @param composerId 대상 작곡가 ID
     * @param user       요청한 유저 (인증 객체)
     * @return 좋아요 생성 시 {@code true}, 취소 시 {@code false}를 담은 DTO
     */
    @Transactional
    public void updateComposer(Long composerId, ComposerUpdateDto dto, User user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(CommonErrorStatus.FORBIDDEN);
        }
        Composer composer = entityUtils.getEntity(composerId, Composer.class);
        composer.update(dto.getKoreanName(), dto.getEnglishName(), dto.getNativeName(),
                dto.getGender(), dto.getNationality(), dto.getBirthYear(), dto.getDeathYear(),
                dto.getBio(), dto.getEra(), dto.getContinent());
    }

    @Transactional
    public void deleteComposer(Long composerId, User user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(CommonErrorStatus.FORBIDDEN);
        }
        Composer composer = entityUtils.getEntity(composerId, Composer.class);
        composerRepository.delete(composer);
    }

    @Transactional
    public void createPersona(Long composerId, ComposerPersonaCreateDto dto, User user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(CommonErrorStatus.FORBIDDEN);
        }
        Composer composer = entityUtils.getEntity(composerId, Composer.class);
        if (composerPersonaRepository.findByComposerId(composerId).isPresent()) {
            throw new BusinessException("이미 페르소나가 존재합니다. 수정 API를 사용하세요.");
        }
        ComposerPersona persona = ComposerPersona.builder()
                .composer(composer)
                .identity(dto.identity())
                .mission(dto.mission())
                .constraintsText(dto.constraintsText())
                .build();
        composerPersonaRepository.save(persona);
    }

    @Transactional
    public void updatePersona(Long composerId, ComposerPersonaUpdateDto dto, User user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(CommonErrorStatus.FORBIDDEN);
        }
        ComposerPersona persona = composerPersonaRepository.findByComposerId(composerId)
                .orElseThrow(() -> new NotFoundException("해당 작곡가의 페르소나를 찾을 수 없습니다."));
        persona.update(dto.identity(), dto.mission(), dto.constraintsText());
    }

    @Transactional
    public void togglePersonaActive(Long composerId, User user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(CommonErrorStatus.FORBIDDEN);
        }
        ComposerPersona persona = composerPersonaRepository.findByComposerId(composerId)
                .orElseThrow(() -> new NotFoundException("해당 작곡가의 페르소나를 찾을 수 없습니다."));
        if (persona.isActive()) {
            persona.deactivate();
        } else {
            persona.activate();
        }
    }

    @Transactional(readOnly = true)
    public List<ComposerPersonaResponseDto> getAllPersonas(User user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(CommonErrorStatus.FORBIDDEN);
        }
        return composerPersonaRepository.findAllWithComposer().stream()
                .map(ComposerPersonaResponseDto::from)
                .toList();
    }

    @Transactional
    public ComposerLikeResponseDto toggleLike(Long composerId, User user) {
        Composer composer = entityUtils.getEntity(composerId, Composer.class);

        return composerLikeRepository.findByComposerIdAndUserId(composerId, user.getId())
                .map(like -> {
                    composerLikeRepository.delete(like);
                    return new ComposerLikeResponseDto(false);
                })
                .orElseGet(() -> {
                    try {
                        composerLikeRepository.save(ComposerLike.of(composer, user));
                        return new ComposerLikeResponseDto(true);
                    } catch (DataIntegrityViolationException e) {
                        return new ComposerLikeResponseDto(true);
                    }
                });
    }
}
