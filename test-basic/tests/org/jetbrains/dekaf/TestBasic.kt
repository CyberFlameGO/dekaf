package org.jetbrains.dekaf


import org.jetbrains.dekaf.teamcity.TeamCityListener
import org.jetbrains.dekaf.testing.TerminalListener
import org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns
import org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage
import org.junit.platform.launcher.TagFilter.includeTags
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener


object TestBasic {

    @JvmStatic
    fun main(args: Array<String>) {

        if (System.getProperty("test-db") == null)
            System.setProperty("test-db", "jdbc:h2:mem:default")

        val request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("org.jetbrains.dekaf"))
                .filters(includeClassNamePatterns(".*Test"), includeTags("basic|demo&!fail"))
                .build()

        val launcher = LauncherFactory.create()
        val summaryListener = SummaryGeneratingListener()
        val teamCityListener = TeamCityListener
        launcher.registerTestExecutionListeners(TerminalListener, teamCityListener, summaryListener)
        launcher.execute(request, *arrayOfNulls<TestExecutionListener>(0))

        TerminalListener.displayAll()

        val summary = summaryListener.summary
        val message = "\n" +
                "========== SUMMARY ==========\n" +
                "  All found containers:  ${summary.containersFoundCount}\n" +
                "  Started containers:    ${summary.containersStartedCount}\n" +
                "  Successful containers: ${summary.containersSucceededCount}\n" +
                "  Skipped containers:    ${summary.containersSkippedCount}\n" +
                "  Failed containers:     ${summary.containersFailedCount}\n" +
                "  Aborted containers:    ${summary.containersAbortedCount}\n" +
                "-----------------------------\n" +
                "  All found tests:       ${summary.testsFoundCount}\n" +
                "  Started tests:         ${summary.testsStartedCount}\n" +
                "  Successful tests:      ${summary.testsSucceededCount}\n" +
                "  Skipped tests:         ${summary.testsSkippedCount}\n" +
                "  Failed tests:          ${summary.testsFailedCount}\n" +
                "  Aborted tests:         ${summary.testsAbortedCount}\n" +
                "-----------------------------\n"
        System.out.println(message)
        System.out.flush()
        Thread.sleep(100L)

        val totalFailureCount = summary.totalFailureCount
        if (totalFailureCount == 0L) {
            System.out.println("OK\n")
            System.out.flush()
        }
        else {
            System.err.println("Total $totalFailureCount failures.")
            System.err.flush()
            System.exit(-1)
        }
    }


}
