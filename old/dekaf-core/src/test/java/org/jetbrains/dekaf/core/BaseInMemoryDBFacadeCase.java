package org.jetbrains.dekaf.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.dekaf.jdbc.BaseInMemoryDBCase;
import org.jetbrains.dekaf.jdbc.JdbcIntermediateFacade;
import org.jetbrains.dekaf.jdbc.UnknownDatabaseProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import static org.assertj.core.api.Assertions.assertThat;



/**
 * @author Leonid Bushuev from JetBrains
 */
public class BaseInMemoryDBFacadeCase extends BaseInMemoryDBCase {


  protected static UnknownDatabaseProvider ourUnknownDatabaseProvider =
          new UnknownDatabaseProvider();

  protected BaseFacade myFacade;
  protected JdbcIntermediateFacade myJdbcFacade;



  @BeforeClass
  public static void setup() {
  }

  @Before
  public void connect() {
    myJdbcFacade = ourUnknownDatabaseProvider.openFacade(H2_CONNECTION_STRING, null, 1);
    myFacade = prepareBaseFacade(myJdbcFacade);
    myFacade.connect();
  }

  @NotNull
  protected BaseFacade prepareBaseFacade(@NotNull final JdbcIntermediateFacade jdbcFacade) {
    return new BaseFacade(jdbcFacade);
    /*
    AdaptIntermediateFacade intermediateFacade = new AdaptIntermediateFacade(jdbcFacade);
    return new BaseFacade(intermediateFacade);
    */
  }

  @After
  public void disconnect() {
    myFacade.disconnect();
  }

  protected void checkAllAreClosed() {
    assertThat(myJdbcFacade.countOpenedCursors()).isZero();
    assertThat(myJdbcFacade.countOpenedSeances()).isZero();
    assertThat(myJdbcFacade.countOpenedSessions()).isZero();
  }
}
