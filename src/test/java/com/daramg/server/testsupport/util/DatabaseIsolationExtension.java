package com.daramg.server.testsupport.util;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class DatabaseIsolationExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) {
        DatabaseManager databaseManager = getDatabaseManager(context);
        databaseManager.truncateTables();
    }

    private DatabaseManager getDatabaseManager(ExtensionContext context) {
        return SpringExtension
                .getApplicationContext(context)
                .getBean(DatabaseManager.class);
    }
}
