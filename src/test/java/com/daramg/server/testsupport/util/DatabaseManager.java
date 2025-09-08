package com.daramg.server.testsupport.util;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class DatabaseManager {

    private final EntityManager entityManager;
    private final List<String> tableNames;

    public DatabaseManager(EntityManager entityManager, TableNameExtractor tableNameExtractor) {
        this.entityManager = entityManager;
        this.tableNames = tableNameExtractor.getNames();
    }

    public void truncateTables() {
        entityManager.createNativeQuery("SET foreign_key_checks = 0").executeUpdate();
        for (String tableName : tableNames) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
        }
        entityManager.createNativeQuery("SET foreign_key_checks = 1").executeUpdate();
    }
}
