package org.jetbrains.dekaf;

import org.jetbrains.dekaf.core.*;
import org.jetbrains.dekaf.exceptions.DBExceptionTest;
import org.jetbrains.dekaf.intermediate.AdaptIntermediateRdbmsProviderTest;
import org.jetbrains.dekaf.sql.*;
import org.jetbrains.dekaf.util.ArrayFunctionsTest;
import org.jetbrains.dekaf.util.NumbersTest;
import org.jetbrains.dekaf.util.StringsTest;
import org.jetbrains.dekaf.util.VersionTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;



/**
 * @author Leonid Bushuev from JetBrains
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
                            // UTILS
                            NumbersTest.class,
                            StringsTest.class,
                            ArrayFunctionsTest.class,
                            DBExceptionTest.class,
                            VersionTest.class,
                            RewritersTest.class,
                            // SQL
                            RdbmsTest.class,
                            ScriptumResourceFromJavaTest.class,
                            ScriptumBasicTest.class,
                            SqlCommandTest.class,
                            SqlScriptBuilderTest.class,
                            SqlScriptTest.class,
                            // Intermediate Layer
                            AdaptIntermediateRdbmsProviderTest.class,
                            // Base client functionality
                            BaseSessionTest.class,
                            BaseQueryRunnerDirectTest.class,
                            BaseQueryRunnerPseudoRemoteTest.class,
                            BaseFacadeTest.class,
                            BaseRdbmsProviderTest.class,
                            BaseFederatedProviderTest.class
})
public class CoreUnitTests {}
