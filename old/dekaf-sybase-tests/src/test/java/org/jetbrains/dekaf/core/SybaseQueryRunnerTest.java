package org.jetbrains.dekaf.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.dekaf.sql.SqlQuery;
import org.jetbrains.dekaf.util.Strings;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jetbrains.dekaf.core.Layouts.*;


/**
 * @author Leonid Bushuev from JetBrains
 **/
@FixMethodOrder(MethodSorters.JVM)
public class SybaseQueryRunnerTest extends CommonQueryRunnerTest {


  @Test
  public void query_bit_as_boolean() {
    TH.prepareX1();
    SqlQuery<Boolean> q = new SqlQuery<Boolean>("select * from X1", singleOf(Boolean.class));
    final Boolean b = query(q);
    assertThat(b).isTrue();
  }

  @Test
  public void query_bit_as_int() {
    SqlQuery<Byte> q = new SqlQuery<Byte>("select * from X1", singleOf(Byte.class));
    final Byte b = query(q);
    assertThat(b).isEqualTo((byte)1);
  }

  @Test
  public void query_int_as_boolean() {
    SqlQuery<Boolean> q = new SqlQuery<Boolean>("select 1",singleOf(Boolean.class));
    final Boolean b = query(q);
    assertThat(b).isTrue();
  }

  @Test
  public void query_varchar255() {
    final String str255 = Strings.repeat("0123456789", null, 26).substring(0, 255);
    assert str255.length() == 255;
    query_string(str255);
  }

  @Test
  public void query_varchar65535() {
    final String str65535 = Strings.repeat("0123456789", null, 6554).substring(0, 65535);
    assert str65535.length() == 65535;
    query_string(str65535);
  }

  private void query_string(@NotNull final String string) {
    final String query = "select '" + string + "' as test_str";
    String result =
        DB.inTransaction(new InTransaction<String>() {
          @Override
          public String run(@NotNull final DBTransaction tran) {

            return tran.query(query, singleOf(String.class)).run();

          }
        });

    assertThat(result).isEqualTo(string);
  }


  @NotNull
  @Override
  protected String sqlNow() {
    //noinspection SpellCheckingInspection
    return "getdate()";
  }


  @Test
  public void query_binary() {
    SqlQuery<byte[]> query =
        new SqlQuery<byte[]>("select top 1 keys1 from dbo.sysindexes where keycnt > 1",
                             singleOf(byte[].class));
    final byte[] rowValue = query(query);
    assertThat(rowValue).isNotNull()
                        .isNotEmpty();
  }


  @Test
  public void query_from_sysusers() {
    SqlQuery<List<Object[]>> query =
        new SqlQuery<List<Object[]>>("select top 1 uid as id, name from dbo.sysusers",
                                     listOf(arrayOf(2, Object.class)));
    final List<Object[]> rows = query(query);

    assertThat(rows).isNotEmpty();
    assertThat(rows.get(0)[0]).isExactlyInstanceOf(Integer.class);
  }


  static class UserBrief {
    Integer user_id;
    String  user_name;
  }

  @Test
  public void query_correct_column_name() {
    String queryText = "select top 1 uid as user_id, name as user_name from dbo.sysusers";
    SqlQuery<UserBrief> query = new SqlQuery<UserBrief>(queryText, rowOf(structOf(UserBrief.class)));

    final UserBrief ub = query(query);

    assertThat(ub.user_id).isNotNull();
    assertThat(ub.user_name).isNotNull();
  }



}
