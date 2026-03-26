package com.daramg.server.composer.domain;

import com.daramg.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "composer_personas")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComposerPersona extends BaseEntity<ComposerPersona> {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "composer_id", nullable = false, unique = true)
    private Composer composer;

    @Column(name = "identity", nullable = false, columnDefinition = "TEXT")
    private String identity;

    @Column(name = "mission", nullable = false, columnDefinition = "TEXT")
    private String mission;

    @Column(name = "constraints_text", nullable = false, columnDefinition = "TEXT")
    private String constraintsText;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder
    public ComposerPersona(Composer composer, String identity, String mission, String constraintsText) {
        this.composer = composer;
        this.identity = identity;
        this.mission = mission;
        this.constraintsText = constraintsText;
        this.isActive = true;
    }

    public void update(String identity, String mission, String constraintsText) {
        this.identity = identity;
        this.mission = mission;
        this.constraintsText = constraintsText;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
