package org.jetbrains.dekaf.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.dekaf.CommonIntegrationCase;
import org.jetbrains.dekaf.sql.Rewriters;
import org.jetbrains.dekaf.sql.SqlQuery;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jetbrains.dekaf.core.Layouts.*;



/**
 * @author Leonid Bushuev from JetBrains
 **/
@FixMethodOrder(MethodSorters.JVM)
public class CommonQueryRunnerTest extends CommonIntegrationCase {

  protected static boolean isOracle =
      DB.rdbms().code.equalsIgnoreCase("ORACLE");


  @Before
  public void setUp() throws Exception {
    DB.connect();
    TH.prepareX1();
    TH.prepareX1000();
    TH.prepareX1000000();
  }


  public static class PrimitiveNumbers {
    byte  B;
    short S;
    int   I;
    long  L;
  }

  public static class BoxedNumbers {
    Byte    B;
    Short   S;
    Integer I;
    Long    L;
  }


  @Test
  public void query_existence_0 () {
    String queryText = "select 1 from "+(isOracle ? "dual" : "X1")+" where 1 is null";
    SqlQuery<Boolean> q = new SqlQuery<Boolean>(queryText, existence());
    final Boolean b = query(q);
    assertThat(b).isNotNull()
                 .isFalse();
  }

  @Test
  public void query_existence_1() {
    String queryText = "select 1 "+(isOracle ? "from dual" : "");
    SqlQuery<Boolean> q = new SqlQuery<Boolean>(queryText, existence());
    final Boolean b = query(q);
    assertThat(b).isNotNull()
                 .isTrue();
  }

  @Test
  public void query_primitive_numbers_positive() {
    final String queryText =
        "select 127 as B, 32767 as S, 2147483647 as I, 9223372036854775807 as L from X1";
    final SqlQuery<PrimitiveNumbers> query =
        new SqlQuery<PrimitiveNumbers>(queryText, rowOf(structOf(PrimitiveNumbers.class)));
    PrimitiveNumbers pn = query(query);

    assertThat(pn.B).isEqualTo((byte)127);
    assertThat(pn.S).isEqualTo((short)32767);
    assertThat(pn.I).isEqualTo(2147483647);
    assertThat(pn.L).isEqualTo(9223372036854775807L);
  }

  @Test
  public void query_primitive_numbers_negative() {
    final String queryText =
        "select -128 as B, -32768 as S, -2147483648 as I, -9223372036854775808 as L from X1";
    final SqlQuery<PrimitiveNumbers> query =
        new SqlQuery<PrimitiveNumbers>(queryText, rowOf(structOf(PrimitiveNumbers.class)));
    PrimitiveNumbers pn = query(query);

    assertThat(pn.B).isEqualTo((byte)-128);
    assertThat(pn.S).isEqualTo((short)-32768);
    assertThat(pn.I).isEqualTo(-2147483648);
    assertThat(pn.L).isEqualTo(-9223372036854775808L);
  }

  @Test
  public void query_boxed_numbers_positive() {
    final String queryText =
        "select 127 as B, 32767 as S, 2147483647 as I, 9223372036854775807 as L from X1";
    final SqlQuery<BoxedNumbers> query =
        new SqlQuery<BoxedNumbers>(queryText, rowOf(structOf(BoxedNumbers.class)));
    BoxedNumbers bn = query(query);

    assertThat(bn.B).isEqualTo((byte) 127);
    assertThat(bn.S).isEqualTo((short)32767);
    assertThat(bn.I).isEqualTo(2147483647);
    assertThat(bn.L).isEqualTo(9223372036854775807L);
  }


  @Test
  public void query_raw_numbers() {
    final String queryText =
        "select 127 as B, 32767 as S, 2147483647 as I, 9223372036854775807 as L from X1";
    final SqlQuery<Object[]> query =
        new SqlQuery<Object[]>(queryText, rowOf(rawArray()));
    final Object[] numbers = query(query);

    assertThat(numbers).hasSize(4);
    assertThat(numbers[0]).isInstanceOf(Number.class);
    assertThat(numbers[1]).isInstanceOf(Number.class);
    assertThat(numbers[2]).isInstanceOf(Number.class);
    assertThat(numbers[3]).isInstanceOf(Number.class);
    assertThat(((Number)numbers[0]).intValue()).isEqualTo(127);
    assertThat(((Number)numbers[1]).intValue()).isEqualTo(32767);
    assertThat(((Number)numbers[2]).intValue()).isEqualTo(2147483647);
    assertThat(((Number)numbers[3]).longValue()).isEqualTo(9223372036854775807L);
  }

  @Test
  public void query_raw_strings() {
    final String queryText =
        "select 'C', 'String' from X1";
    final SqlQuery<Object[]> query =
        new SqlQuery<Object[]>(queryText, rowOf(rawArray()));
    final Object[] strings = query(query);

    assertThat(strings).hasSize(2);

    assertThat(strings[0]).isInstanceOf(String.class);
    assertThat(strings[1]).isInstanceOf(String.class);

    assertThat(strings[0]).isEqualTo("C");
    assertThat(strings[1]).isEqualTo("String");
  }


  protected static class CalendarValues {
    public java.util.Date      javaDate;
    public java.sql.Date       sqlDate;
    public java.sql.Timestamp  sqlTimestamp;
    public java.sql.Time       sqlTime;
  }


  @Test
  public void query_calendar_values_now() {
    String queryText =
        "select NOW as javaDate, NOW as sqlDate, NOW as sqlTimestamp, NOW as sqlTime";
    if (isOracle) queryText += " from dual";
    SqlQuery<CalendarValues> query =
        new SqlQuery<CalendarValues>(queryText, rowOf(structOf(CalendarValues.class)))
          .rewrite(Rewriters.replace("NOW", sqlNow()));

    CalendarValues cv = query(query);

    assertThat(cv.javaDate)    .isExactlyInstanceOf(java.util.Date.class);
    assertThat(cv.sqlDate)     .isExactlyInstanceOf(java.sql.Date.class);
    assertThat(cv.sqlTimestamp).isExactlyInstanceOf(java.sql.Timestamp.class);
    assertThat(cv.sqlTime)     .isExactlyInstanceOf(java.sql.Time.class);
  }

  @Test
  public void query_calendar_values_parameters() {
    String queryText =
        queryCalendarValuesFromParameters();
    if (isOracle) queryText += " from dual";
    SqlQuery<CalendarValues> query =
        new SqlQuery<CalendarValues>(queryText, rowOf(structOf(CalendarValues.class)));

    CalendarValues cv = query(query,
                              new java.sql.Timestamp(System.currentTimeMillis()),
                              new java.sql.Timestamp(System.currentTimeMillis()),
                              new java.sql.Timestamp(System.currentTimeMillis()),
                              new java.sql.Timestamp(System.currentTimeMillis())/*,
                              new java.sql.Time(System.currentTimeMillis())*/
    );

    assertThat(cv.javaDate)    .isExactlyInstanceOf(java.util.Date.class);
    assertThat(cv.sqlDate)     .isExactlyInstanceOf(java.sql.Date.class);
    assertThat(cv.sqlTimestamp).isExactlyInstanceOf(java.sql.Timestamp.class);
    assertThat(cv.sqlTime)     .isExactlyInstanceOf(java.sql.Time.class);
  }

  @NotNull
  protected String queryCalendarValuesFromParameters() {
    return "select ? as javaDate, ? as sqlDate, ? as sqlTimestamp, ? as sqlTime";
  }

  @NotNull
  protected String sqlNow() {
    //noinspection SpellCheckingInspection
    return "current_timestamp";
  }


  @Test
  public void query_1000_values() {
    List<Integer> values =
        DB.inTransaction(new InTransaction<List<Integer>>() {
          @Override
          public List<Integer> run(@NotNull final DBTransaction tran) {
            return tran.query("select X from X1000 order by 1", listOf(oneOf(Integer.class))).run();
          }
        });
    assertThat(values).isNotNull()
                      .hasSize(1000)
                      .contains(1,2,3,4,998,999,1000);
  }


  @Test
  public void query_1000000_values() {
    List<Integer> values =
        DB.inTransaction(new InTransaction<List<Integer>>() {
          @Override
          public List<Integer> run(@NotNull final DBTransaction tran) {
            return tran.query("select X from X1000000 order by 1", listOf(oneOf(Integer.class))).run();
          }
        });
    assertThat(values).isNotNull()
                      .hasSize(1000000)
                      .contains(1,2,3,4,999998,999999,1000000);
  }


  @Test
  public void access_metadata() {
    final SqlQuery<List<Number>> query =
        new SqlQuery<List<Number>>("select X from X1000", listOf(oneOf(Number.class)));

    DB.inSession(new InSessionNoResult() {
      @Override
      public void run(@NotNull final DBSession session) {

        DBQueryRunner<List<Number>> qr = session.query(query).packBy(10);
        qr.run();
        ResultSetMetaData md =
            qr.getSpecificService(ResultSetMetaData.class,
                                  ImplementationAccessibleService.Names.JDBC_METADATA);
        assertThat(md).isNotNull();

        String columnName = null;
        try {
          columnName = md.getColumnName(1);
        }
        catch (SQLException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
        assertThat(columnName).isEqualToIgnoringCase("X");

      }
    });
  }


  protected <T> T query(@NotNull final SqlQuery<T> query) {
    return DB.inTransaction(new InTransaction<T>() {
      @Override
      public T run(@NotNull final DBTransaction tran) {
        return tran.query(query).run();
      }
    });
  }

  protected <T> T query(@NotNull final SqlQuery<T> query, final Object... params) {
    return DB.inTransaction(new InTransaction<T>() {
      @Override
      public T run(@NotNull final DBTransaction tran) {
        return tran.query(query).withParams(params).run();
      }
    });
  }

}
