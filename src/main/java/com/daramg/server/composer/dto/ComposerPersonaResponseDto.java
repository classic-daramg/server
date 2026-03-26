package com.daramg.server.composer.dto;

import com.daramg.server.composer.domain.ComposerPersona;

public record ComposerPersonaResponseDto(
        Long id,
        Long composerId,
        String composerName,
        String identity,
        String mission,
        String constraintsText,
        boolean isActive
) {
    public static ComposerPersonaResponseDto from(ComposerPersona persona) {
        return new ComposerPersonaResponseDto(
                persona.getId(),
                persona.getComposer().getId(),
                persona.getComposer().getKoreanName(),
                persona.getIdentity(),
                persona.getMission(),
                persona.getConstraintsText(),
                persona.isActive()
        );
    }
}
