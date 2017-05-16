package org.jetbrains.dekaf.jdbc;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;



abstract class Specific {

    /// DRIVERS AND CLASSES \\\

    protected abstract Pattern getConnectionStringPattern();

    protected abstract String getDriverClassName();


    /// TRANSACTIONS \\\

    void transactionBegin(@NotNull Connection connection) throws SQLException {
        connection.setAutoCommit(false);
    }

    void transactionCommit(@NotNull Connection connection) throws SQLException {
        connection.commit();
    }

    void transactionRollback(@NotNull Connection connection) throws SQLException {
        connection.rollback();
    }

    void ping(final @NotNull Connection connection) throws SQLException {
        final String query = pingQuery();
        try (Statement statement = connection.createStatement()) {
            try (ResultSet rset = statement.executeQuery(query)) {
                rset.next();
            }
        }
    }

    String pingQuery() {
        return "select 1";
    }
}
