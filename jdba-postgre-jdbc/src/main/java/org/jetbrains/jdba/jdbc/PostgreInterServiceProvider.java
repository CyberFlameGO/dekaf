package org.jetbrains.jdba.jdbc;

import org.jetbrains.jdba.Postgre;
import org.jetbrains.jdba.Rdbms;
import org.jetbrains.jdba.core.exceptions.DBPreparingException;

import java.sql.Driver;
import java.util.regex.Pattern;



/**
 * @author Leonid Bushuev from JetBrains
 */
public class PostgreInterServiceProvider extends JdbcInterBaseServiceProvider {


  //// SETTINGS AND STATE \\\\

  public final static PostgreInterServiceProvider INSTANCE =
          new PostgreInterServiceProvider();


  static final Pattern POSTGRE_CONNECTION_STRING_PATTERN =
          Pattern.compile("^jdbc:postgresql://.+$");

  static final String POSTGRE_CONNECTION_STRING_EXAMPLE =
          "jdbc:postgresql:///";

  private static final String POSTGRE_DRIVER_CLASS_NAME =
          "org.postgresql.Driver";



  //// INITIALIZATION \\\\

  private PostgreInterServiceProvider() {
    loadAndRegisterDriverIfNeeded(POSTGRE_CONNECTION_STRING_EXAMPLE);
    JdbcInterFederatedServiceProvider.INSTANCE.registerProvider(this);
  }


  @Override
  protected Driver loadDriver() {
    Class<Driver> driverClass = getSimpleAccessibleDriverClass(POSTGRE_DRIVER_CLASS_NAME);
    if (driverClass == null) {
      // TODO try to load from jars
    }
    if (driverClass == null) {
      throw new DBPreparingException("Driver class not found");
    }

    final Driver driver;
    try {
      driver = driverClass.newInstance();
    }
    catch (Exception e) {
      throw new DBPreparingException("Failed to instantiate driver: "+e.getMessage(), e);
    }

    return driver;
  }



  //// IMPLEMENTATION \\\\



  @Override
  public Rdbms rdbms() {
    return Postgre.RDBMS;
  }

  @Override
  public Pattern connectionStringPattern() {
    return POSTGRE_CONNECTION_STRING_PATTERN;
  }

  @Override
  public byte specificity() {
    return SPECIFICITY_NATIVE;
  }


  @Override
  public BaseErrorRecognizer getErrorRecognizer() {
    return PostgreErrorRecognizer.INSTANCE;
  }

}
