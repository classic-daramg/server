package com.daramg.server.testsupport.util;

import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("local")
public class H2TableNameExtractor implements TableNameExtractor {

    private final EntityManager entityManager;

    public H2TableNameExtractor(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getNames() {
        List<Object[]> tables = entityManager.createNativeQuery("SHOW TABLES").getResultList();
        return tables.stream()
                .map(table -> (String) table[0])
                .map(String::toLowerCase)
                .filter(name -> !name.equals("flyway_schema_history"))
                .toList();
    }
}
