package com.daramg.server.common.application;

import com.daramg.server.common.exception.NotFoundException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EntityUtils {

    private final EntityManager entityManager;

    public <T> T getEntity(Long id, Class<T> entityType) {
        return Optional.ofNullable(entityManager.find(entityType, id))
                .orElseThrow(() -> new NotFoundException(
                        String.format("존재하지 않는 %s입니다. (target ID: %d)", entityType.getSimpleName(), id)
                ));
    }
}
