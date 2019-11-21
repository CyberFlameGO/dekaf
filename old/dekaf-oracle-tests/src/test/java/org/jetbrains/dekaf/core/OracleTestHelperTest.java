package org.jetbrains.dekaf.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.dekaf.CommonIntegrationCase;
import org.jetbrains.dekaf.sql.Scriptum;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;



/**
 * @author Leonid Bushuev from JetBrains
 **/
public class OracleTestHelperTest extends CommonIntegrationCase {

  private static final Scriptum ourScriptum =
      Scriptum.of(OracleTestHelperTest.class);



  //// ZAP \\\\

  @BeforeClass
  public static void connect() {
    DB.connect();
    TH.zapSchema();
  }


  @Test
  public void zap_sequence() {
    // create a sequence
    TH.performCommand("create sequence SEQ_101");

    // ensure that we can detect the sequence existence
    assertThat(objectExists("SEQ_101")).isTrue();

    // zap it
    TH.zapSchema();

    // ensure that the sequence is dropped
    assertThat(objectExists("SEQ_101")).isFalse();
  }


  @Test
  public void zap_synonym() {
    // create a synonym
    TH.performCommand("create synonym MY_DUAL for sys.dual");

    // ensure that we can detect the synonym existence
    assertThat(objectExists("MY_DUAL")).isTrue();

    // zap it
    TH.zapSchema();

    // ensure that the synonym is dropped
    assertThat(objectExists("MY_DUAL")).isFalse();
  }


  @Test
  public void zap_operator() {
    // create function and operator
    TH.performScript(ourScriptum, "CreateOperator");

    // ensure that we can detect them
    assertThat(objectExists("EQ_F")).isTrue();
    assertThat(objectExists("EQ_OP")).isTrue();

    // zap it
    TH.zapSchema();

    // verify
    assertThat(objectExists("EQ_OP")).isFalse();
    assertThat(objectExists("EQ_F")).isFalse();
  }


  @Test
  public void zap_mater_log() {
    // create function and operator
    TH.performScript(ourScriptum, "CreateMaterLog");

    // ensure that the table exists
    assertThat(objectExists("WORK_TABLE")).isTrue();

    // zap it
    TH.zapSchema();

    // verify (no table with materialized log)
    assertThat(objectExists("WORK_TABLE")).isFalse();
  }


  @Test
  public void zap_mater_view_1() {
    // create function and operator
    TH.performScript(ourScriptum, "CreateMaterView1");

    // ensure that we can detect a materialized view
    assertThat(objectExists("X_ORDER_STAT")).isTrue();

    // zap it
    TH.zapSchema();

    // verify
    assertThat(objectExists("X_ORDER_STAT")).isFalse();
  }


  @Test
  public void zap_mater_view_dependence() {
    // create function and operator
    TH.performScript(ourScriptum, "CreateMaterViewDependence");

    // ensure that we can detect a materialized view
    assertThat(objectExists("OWNERS")).isTrue();
    assertThat(objectExists("SCHEMAS")).isTrue();

    // zap it
    TH.zapSchema();

    // verify
    assertThat(objectExists("OWNERS")).isFalse();
    assertThat(objectExists("SCHEMAS")).isFalse();
  }


  @Test
  public void zap_mater_view_prebuilt() {
    // create function and operator
    TH.performScript(ourScriptum, "CreateMaterViewPrebuilt");

    // ensure that we can detect a materialized view
    assertThat(objectExists("ONE")).isTrue();

    // zap it
    TH.zapSchema();

    // verify
    assertThat(objectExists("ONE")).isFalse();
  }


  @Test
  public void zap_indexed_cluster() {
    // create a cluster
    TH.performScript(ourScriptum, "CreateIndexedCluster");

    // ensure that we can detect a cluster
    assertThat(objectExists("PROJECT_DATA")).isTrue();

    // zap it
    TH.zapSchema();

    // verify
    assertThat(objectExists("PROJECT_DATA")).isFalse();
  }


  @Test
  public void zap_hash_cluster() {
    // create a hash cluster
    TH.performScript(ourScriptum, "CreateHashCluster");

    // ensure that we can detect a hash cluster
    assertThat(objectExists("DUMP")).isTrue();

    // zap it
    TH.zapSchema();

    // verify
    assertThat(objectExists("DUMP")).isFalse();
  }


  @Test
  public void zap_quoted_objects() {
    // create a lot of quoted objects
    TH.performScript(ourScriptum, "Quotation");

    // ensure that we can detect these objects
    assertThat(objectExists("The Cluster")).isTrue();
    assertThat(objectExists("The Table")).isTrue();
    assertThat(objectExists("The Index")).isTrue();
    assertThat(objectExists("The Package")).isTrue();
    assertThat(objectExists("The Object Type")).isTrue();
    assertThat(objectExists("The Procedure")).isTrue();
    assertThat(objectExists("The Fun")).isTrue();
    assertThat(objectExists("The Operator")).isTrue();

    // zap it
    TH.zapSchema();

    // verify
    assertThat(objectExists("The Cluster")).isFalse();
    assertThat(objectExists("The Table")).isFalse();
    assertThat(objectExists("The Index")).isFalse();
    assertThat(objectExists("The Package")).isFalse();
    assertThat(objectExists("The Object Type")).isFalse();
    assertThat(objectExists("The Procedure")).isFalse();
    assertThat(objectExists("The Fun")).isFalse();
    assertThat(objectExists("The Operator")).isFalse();
  }


  @Test
  public void zap_indextype() {
    // create all that needs to make an INDEXTYPE and use it
    final Scriptum theScriptum =  Scriptum.of(this.getClass(), "OracleTestHelperTest_CreateIndexType");
    TH.performScript(theScriptum, "INDEXTYPE");

    // ensure that we can detect an indextype
    assertThat(objectExists("POSITION_INDEXTYPE")).isTrue();

    // zap it
    TH.zapSchema();

    // verify
    assertThat(objectExists("POSITION_INDEXTYPE")).isFalse();
  }


  @Test
  public void zap_identity_column() {
    if (DB.getConnectionInfo().serverVersion.less(12)) return;

    // create a table with identity column
    TH.performCommand("create table table_with_identity_column(col1 number(9) generated as identity)");

    // ensure that we can detect a sequence
    assertThat(objectLikeExists("ISEQ$$_%")).isTrue();

    // zap it
    TH.zapSchema();

    // verify
    assertThat(objectLikeExists("ISEQ$$_%")).isFalse();
  }


  private static boolean objectExists(@NotNull final String objectName) {
    assert DB != null;

    final String query =
        "select 1 from user_objects where object_name = ?";

    Boolean exists =
        DB.inSession(new InSession<Boolean>() {
          @Override
          public Boolean run(@NotNull final DBSession session) {
            return session.query(query, Layouts.existence()).withParams(objectName).run();
          }
        });
    return exists != null && exists;
  }


  private static boolean objectLikeExists(@NotNull final String objectNamePattern) {
    assert DB != null;

    final String query =
        "select 1 from user_objects where object_name like ?";

    Boolean exists =
        DB.inSession(new InSession<Boolean>() {
          @Override
          public Boolean run(@NotNull final DBSession session) {
            return session.query(query, Layouts.existence()).withParams(objectNamePattern).run();
          }
        });
    return exists != null && exists;
  }


}
