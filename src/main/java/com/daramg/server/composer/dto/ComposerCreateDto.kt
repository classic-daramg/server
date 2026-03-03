package com.daramg.server.composer.dto

import com.daramg.server.composer.domain.Continent
import com.daramg.server.composer.domain.Era
import com.daramg.server.composer.domain.Gender
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ComposerCreateDto(
    @get:NotBlank val koreanName: String,
    @get:NotBlank val englishName: String,
    val nativeName: String? = null,
    @get:NotNull val gender: Gender,
    val nationality: String? = null,
    val birthYear: Short? = null,
    val deathYear: Short? = null,
    val bio: String? = null,
    val era: Era? = null,
    val continent: Continent? = null,
)
