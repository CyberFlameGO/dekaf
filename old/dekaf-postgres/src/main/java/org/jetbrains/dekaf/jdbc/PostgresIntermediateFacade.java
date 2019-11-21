package org.jetbrains.dekaf.jdbc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.dekaf.Postgres;
import org.jetbrains.dekaf.Rdbms;
import org.jetbrains.dekaf.core.ConnectionInfo;
import org.jetbrains.dekaf.intermediate.DBExceptionRecognizer;

import javax.sql.DataSource;
import java.sql.Driver;
import java.util.Properties;
import java.util.regex.Pattern;


/**
 * @author Leonid Bushuev from JetBrains
 **/
public class PostgresIntermediateFacade extends JdbcIntermediateFacade {

  public PostgresIntermediateFacade(@NotNull final String connectionString,
                                    @Nullable final Properties connectionProperties,
                                    @NotNull final Driver driver,
                                    final int connectionsLimit,
                                    @NotNull final DBExceptionRecognizer exceptionRecognizer) {
    super(connectionString, connectionProperties, driver, connectionsLimit, exceptionRecognizer);
  }

  public PostgresIntermediateFacade(@NotNull final DataSource dataSource,
                                    final int connectionsLimit,
                                    boolean ownConnections,
                                    @NotNull final DBExceptionRecognizer exceptionRecognizer) {
    super(dataSource, connectionsLimit, ownConnections, exceptionRecognizer);
  }

  @NotNull
  @Override
  public Rdbms rdbms() {
    return Postgres.RDBMS;
  }


  @Override
  public ConnectionInfo obtainConnectionInfoNatively() {
    return getConnectionInfoSmartly(CONNECTION_INFO_QUERY,
                                    ENHANCED_VERSION_PATTERN, 1,
                                    SIMPLE_VERSION_PATTERN, 1);
  }

  protected static final Pattern ENHANCED_VERSION_PATTERN =
          Pattern.compile("(\\d{1,2}(\\.\\d{1,3}|alpha|beta|rc){1,5})");

  @SuppressWarnings("SpellCheckingInspection")
  public static final String CONNECTION_INFO_QUERY =
      "select current_database(), current_schema(), current_user";

}
