package com.hnsykrh.blooddonation.support;

import com.hnsykrh.blooddonation.db.DatabaseManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestDatabase {

    private TestDatabase() {
    }

    public static DatabaseManager createEmpty() throws IOException {
        Path db = Files.createTempFile("bdms-test-", ".db");
        db.toFile().deleteOnExit();
        DatabaseManager manager = new DatabaseManager(db);
        manager.initialize(false);
        return manager;
    }
}
