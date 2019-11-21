package org.jetbrains.dekaf.core;

import org.jetbrains.dekaf.intermediate.IntegralIntermediateFacade;
import org.jetbrains.dekaf.jdbc.JdbcIntermediateFacade;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;



/**
 * @author Leonid Bushuev from JetBrains
 **/
public class BaseFacadeTest extends BaseInMemoryDBFacadeCase {


  @Test
  public void leaseSession_basic() {
    final DBLeasedSession session = myFacade.leaseSession();

    assertThat(session.isClosed()).isFalse();

    session.ping();

    session.close();

    assertThat(session.isClosed()).isTrue();
  }


  @Test
  public void get_intermediate_service() {

    IntegralIntermediateFacade intermediateSession =
        myFacade.getSpecificService(
            IntegralIntermediateFacade.class,
            ImplementationAccessibleService.Names.INTERMEDIATE_SERVICE);
    assertThat(intermediateSession).isInstanceOf(JdbcIntermediateFacade.class);

  }

}