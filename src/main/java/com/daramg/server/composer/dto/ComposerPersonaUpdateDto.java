package com.daramg.server.composer.dto;

import jakarta.validation.constraints.NotBlank;

public record ComposerPersonaUpdateDto(
        @NotBlank String identity,
        @NotBlank String mission,
        @NotBlank String constraintsText
) {}
