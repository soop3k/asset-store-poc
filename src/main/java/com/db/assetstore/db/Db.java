package com.db.assetstore.db;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {
    private Db() {}

    public static Connection connect() throws SQLException {
        String url = System.getProperty("DB_URL", System.getenv().getOrDefault("DB_URL", "jdbc:h2:mem:assetdb;DB_CLOSE_DELAY=-1"));
        String user = System.getProperty("DB_USER", System.getenv().getOrDefault("DB_USER", "sa"));
        String pass = System.getProperty("DB_PASS", System.getenv().getOrDefault("DB_PASS", ""));
        return DriverManager.getConnection(url, user, pass);
    }

    public static void migrate(Connection connection) {
        try {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase("db/changelog/changelog-master.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update((String) null);
        } catch (Exception e) {
            throw new RuntimeException("Liquibase migration failed", e);
        }
    }
}
