package com.daramg.server.composer.domain;

import com.daramg.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Entity
@Getter
@Table(name = "composers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Composer extends BaseEntity<Composer> {

    @Column(name = "korean_name", nullable = false)
    private String koreanName;

    @Column(name = "english_name", nullable = false)
    private String englishName;

    @Column(name = "native_name")
    private String nativeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "birth_year")
    private Short birthYear;

    @Column(name = "death_year")
    private Short deathYear;

    @Column(name = "bio")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "era")
    private Era era;

    @Enumerated(EnumType.STRING)
    @Column(name = "continent")
    private Continent continent;

    @Builder
    private Composer(@NonNull String koreanName, @NonNull String englishName,
                   String nativeName, @NonNull Gender gender, String nationality,
                   Short birthYear, Short deathYear, Era era, Continent continent) {
        this.koreanName = koreanName;
        this.englishName = englishName;
        this.nativeName = nativeName;
        this.gender = gender;
        this.nationality = nationality;
        this.birthYear = birthYear;
        this.deathYear = deathYear;
        this.era = era;
        this.continent = continent;
    }
}
