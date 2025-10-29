package com.daramg.server.composer.repository;

import com.daramg.server.composer.domain.Composer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComposerRepository extends JpaRepository<Composer, Long> {
}
