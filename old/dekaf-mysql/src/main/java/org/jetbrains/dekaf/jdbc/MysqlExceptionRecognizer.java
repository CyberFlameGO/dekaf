package org.jetbrains.dekaf.jdbc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.dekaf.exceptions.DBException;
import org.jetbrains.dekaf.exceptions.NoTableOrViewException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;



/**
 * @author Leonid Bushuev from JetBrains
 */
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class MysqlExceptionRecognizer extends BaseExceptionRecognizer {

  public static final MysqlExceptionRecognizer INSTANCE = new MysqlExceptionRecognizer();


  private static final Map<String, Class<? extends DBException>> simpleExceptionMap =
      new HashMap<String, Class<? extends DBException>>();

  static {
    simpleExceptionMap.put("42S02", NoTableOrViewException.class);
  }



  @Nullable
  @Override
  protected DBException recognizeSpecificException(@NotNull final SQLException sqle,
                                                   @Nullable final String statementText) {
    String errCode = sqle.getSQLState();
    Class<? extends DBException> simpleExceptionClass = simpleExceptionMap.get(errCode);
    if (simpleExceptionClass != null) {
      return instantiateDBException(simpleExceptionClass, sqle, statementText);
    }
    else {
      return null;
    }
  }


}
