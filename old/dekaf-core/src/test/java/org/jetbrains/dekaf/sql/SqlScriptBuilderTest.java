package org.jetbrains.dekaf.sql;

import org.jetbrains.dekaf.junitft.FineRunner;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;



/**
 * @author Leonid Bushuev from JetBrains
 */
@FixMethodOrder(MethodSorters.JVM)
@RunWith(FineRunner.class)
public class SqlScriptBuilderTest {


  @Test
  public void parse_1() {
    String commandText = "select * from dual";
    final SqlScript script = build(commandText);

    assertThat(script.hasStatements()).isTrue();
    assertThat(script.getStatements()).hasSize(1);
    SqlStatement statement = script.getStatements().get(0);
    assertThat(statement.getSourceText()).isEqualTo(commandText);
    assertThat(statement.getRow()).isEqualTo(1);
  }


  @Test
  public void parse_1x() {
    final String commandText =
        "select something\n" +
        "from some_table\n";
    SqlStatement statement = parseScriptWithOneStatement(commandText);
    assertThat(statement.getSourceText()).contains("select something", "from some_table");
  }


  @Test
  public void parse_1_create_view() {
    final String commandText =
        "create view my_view \n" +
        "as                  \n" +
        "select *            \n" +
        "from some_table     \n";
    SqlStatement statement = parseScriptWithOneStatement(commandText);
    assertThat(statement.getSourceText()).contains("create view", "from some_table");
  }

  @Test
  public void parse_1_PL_block() {
    final String commandText =
        "create or replace procedure My_Proc as \n" +
        "declare X natural := 2;                \n" +
        "begin                                  \n" +
        "    dbms_Output.put_line(X);           \n" +
        "end;                                   \n";
    SqlStatement statement = parseScriptWithOneStatement(commandText);
    assertThat(statement.getSourceText()).contains("create or replace procedure My_Proc",
                                                   "declare X natural := 2;",
                                                   "begin",
                                                   "dbms_Output.put_line(X);",
                                                   "end;");
  }

  private static SqlStatement parseScriptWithOneStatement(final String commandText) {
    final SqlScript script = build(commandText);

    List<? extends SqlStatement> statements = script.getStatements();
    assertThat(statements).hasSize(1);
    return statements.get(0);
  }


  @Test
  public void parse_2_in_2_lines() {
    String text = "create table X;\n drop table X";
    final SqlScript script = build(text);

    assertThat(script.count()).isEqualTo(2);
    assertThat(script.getStatements().get(0).getSourceText()).isEqualTo("create table X");
    assertThat(script.getStatements().get(1).getSourceText()).isEqualTo("drop table X");
  }


  @Test
  public void parse_singleLineComment() {
    String text = "-- a single line comment \n" +
                  "do something";
    final SqlScript script = build(text);

    assertThat(script.count()).isEqualTo(1);
    assertThat(script.getStatements().get(0).getSourceText()).isEqualTo("do something");
  }


  @Test
  public void parse_singleLineComment_preserveOracleHint() {
    String text = "select --+index(i) \n" +
                  "      all fields   \n" +
                  "from my_table      \n";
    final SqlScript script = build(text);

    assertThat(script.hasStatements()).isTrue();
    assertThat(script.count()).isEqualTo(1);
    final String queryText = script.getStatements().get(0).getSourceText();
    assertThat(queryText).contains("--+index(i)");
  }


  @Test
  public void parse_multiLineComment_1() {
    String text = "/* a multi-line comment*/\n" +
                  "do something";
    final SqlScript script = build(text);

    assertThat(script.count()).isEqualTo(1);
    assertThat(script.getStatements().get(0).getSourceText()).isEqualTo("do something");
  }


  @Test
  public void parse_multiLineComment_3() {
    String text = "/*                      \n" +
                  " * a multi-line comment \n" +
                  " */                     \n" +
                  "do something            \n";
    final SqlScript script = build(text);

    assertThat(script.count()).isEqualTo(1);
    assertThat(script.getStatements().get(0).getSourceText()).isEqualTo("do something");
  }


  @Test
  public void parse_multiLineComment_preserveOracleHint() {
    String text = "select /*+index(i)*/ * \n" +
                  "from table             \n";
    final SqlScript script = build(text);

    assertThat(script.count()).isEqualTo(1);
    final String queryText = script.getStatements().get(0).getSourceText();
    assertThat(queryText).contains("/*+index(i)*/");
  }


  @Test
  public void parse_OraclePackage() {
    final SqlScript script = build(ORACLE_PKG1);

    assertThat(script.getStatements()).hasSize(2);
    assertThat(script.getStatements().get(0).getSourceText()).startsWith("create package pkg1")
                                                             .endsWith("end pkg1;");
    assertThat(script.getStatements().get(1).getSourceText()).startsWith("create package body")
                                                             .endsWith("end;");
  }

  @Test
  public void parse_OraclePackage_lines() {
    final SqlScript script = build(ORACLE_PKG1);

    assertThat(script.getStatements()).hasSize(2)
                                      .extracting("row")
                                      .containsSequence(1, 9);
  }

  private static final String ORACLE_PKG1 =
          "create package pkg1 is            \n" +     //  line 1
          "                                  \n" +     //
          "  procedure pro1 (n natural);     \n" +     //
          "  function fun1 return number;    \n" +     //
          "                                  \n" +     //
          "end pkg1;                         \n" +     //
          "/                                 \n" +     //
          "                                  \n" +     //
          "create package body pkg1 is       \n" +     //  line 9
          "                                  \n" +     //
          "  procedure pro1(n natural) is    \n" +     //
          "  begin                           \n" +     //
          "    null;                         \n" +     //
          "  end;                            \n" +     //
          "                                  \n" +     //
          "  function fun1 return number is  \n" +     //
          "  begin                           \n" +     //
          "    return 44 / 13;               \n" +     //
          "  end;                            \n" +     //
          "                                  \n" +     //
          "end;                              \n" +     //
          "/                                 \n";      //


  private static SqlScript build(String text) {
    SqlScriptBuilder b = new SqlScriptBuilder();
    b.parse(text);
    return b.build();
  }


}
