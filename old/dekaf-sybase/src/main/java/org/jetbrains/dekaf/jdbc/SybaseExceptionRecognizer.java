package org.jetbrains.dekaf.jdbc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.dekaf.exceptions.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;



/**
 * Error codes:
 * // TODO find the correct list of error codes
 *
 * @author Leonid Bushuev from JetBrains
 */
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class SybaseExceptionRecognizer extends BaseExceptionRecognizer {

  public static final SybaseExceptionRecognizer INSTANCE = new SybaseExceptionRecognizer();


  private static final Map<Integer, Class<? extends DBException>> simpleExceptionMap =
      new HashMap<Integer, Class<? extends DBException>>();

  private static final Map<String, Class<? extends DBException>> nativeExceptionMap =
      new HashMap<String, Class<? extends DBException>>();

  static {
    simpleExceptionMap.put(208,   NoTableOrViewException.class);
    simpleExceptionMap.put(4002,  DBLoginFailedException.class);
    simpleExceptionMap.put(10332, DBColumnAccessDeniedException.class);
    simpleExceptionMap.put(10351, DBSchemaAccessDeniedException.class);

    nativeExceptionMap.put("JZ00L", DBLoginFailedException.class);
  }








  @Nullable
  @Override
  protected DBException recognizeSpecificException(@NotNull final SQLException sqle,
                                                   @Nullable final String statementText) {
    int errCode = sqle.getErrorCode();
    if (errCode > 0) {
      return recognizeForJTDS(sqle, statementText, errCode);
    }
    String state = sqle.getSQLState();
    if (state != null && !state.isEmpty()) {
      return recognizeForNative(sqle, statementText, state);
    }
    return null;
  }

  @Nullable
  private DBException recognizeForJTDS(final @NotNull SQLException sqle,
                                       final @Nullable String statementText, final int errCode) {
    Class<? extends DBException> exceptionClass = simpleExceptionMap.get(errCode);
    if (exceptionClass != null) {
      return instantiateDBException(exceptionClass, sqle, statementText);
    }
    else {
      return null;
    }
  }

  @Nullable
  private DBException recognizeForNative(final SQLException sqle,
                                         final String statementText,
                                         final String state) {
    Class<? extends DBException> exceptionClass = nativeExceptionMap.get(state);
    if (exceptionClass != null) {
      return instantiateDBException(exceptionClass, sqle, statementText);
    }
    else {
      return null;
    }
  }


}
