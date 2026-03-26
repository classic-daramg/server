package com.daramg.server.composer.repository;

import com.daramg.server.composer.domain.ComposerPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComposerPersonaRepository extends JpaRepository<ComposerPersona, Long> {

    Optional<ComposerPersona> findByComposerId(Long composerId);

    @Query("SELECT cp FROM ComposerPersona cp JOIN FETCH cp.composer WHERE cp.isActive = true")
    List<ComposerPersona> findAllActiveWithComposer();

    @Query("SELECT cp FROM ComposerPersona cp JOIN FETCH cp.composer")
    List<ComposerPersona> findAllWithComposer();
}
