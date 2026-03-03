package com.daramg.server.composer.dto

import com.daramg.server.composer.domain.Continent
import com.daramg.server.composer.domain.Era
import com.daramg.server.composer.domain.Gender
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ComposerUpdateDto(
    @get:NotBlank val koreanName: String,
    @get:NotBlank val englishName: String,
    val nativeName: String? = null,
    @get:NotNull val gender: Gender,
    @get:NotBlank val nationality: String,
    @get:NotNull val birthYear: Short,
    @get:NotNull val deathYear: Short,
    @get:NotBlank val bio: String,
    @get:NotNull val era: Era,
    @get:NotNull val continent: Continent,
)
