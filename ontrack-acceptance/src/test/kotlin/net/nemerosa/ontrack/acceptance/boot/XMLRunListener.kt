package net.nemerosa.ontrack.acceptance.boot

import net.nemerosa.ontrack.test.plural
import net.nemerosa.ontrack.xml.Xml
import org.apache.commons.lang3.exception.ExceptionUtils
import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import java.io.File
import java.io.PrintStream

class XMLRunListener(
        private val stream: PrintStream
) : RunListener() {

    private val runs = mutableMapOf<Description, TestRun>()

    private class TestRun(
            val description: Description
    ) {

        private val start: Long = System.currentTimeMillis()
        private var end: Long? = null
        var error: Failure? = null
        var failure: Failure? = null
        var ignored = false

        val time: Long
            get() = if (ignored) 0 else end?.let { it - start } ?: 0

        fun end() {
            end = System.currentTimeMillis()
        }

    }

    private fun trace(message: String) {
        stream.println(message)
    }

    override fun testRunStarted(description: Description) {
        trace("Starting tests in ${description.className}...")
    }

    override fun testRunFinished(result: Result) {
        if (result.wasSuccessful()) {
            trace("")
            trace("Tests OK (${result.runCount} test${plural(result.runCount)})")

        } else {
            trace("")
            trace("FAILED!")
            trace("Tests run: ${result.runCount}, Failures: ${result.failureCount}")
        }
    }

    override fun testStarted(description: Description) {
        trace("Running test: ${description.className}: ${description.methodName}")
        runs[description] = TestRun(description)
    }

    override fun testFinished(description: Description) {
        runs[description]?.end()
    }

    override fun testFailure(failure: Failure) {
        runs[failure.description]?.error = failure
    }

    override fun testAssumptionFailure(failure: Failure) {
        runs[failure.description]?.failure = failure
    }

    override fun testIgnored(description: Description) {
        trace("(*) Ignoring test: ${description.className}: ${description.methodName}")
        val run = TestRun(description)
        run.ignored = true
        runs[description] = run
    }

    fun render(file: File) {
        Xml.document("testsuite") {
            "tests" to runs.size
            "skipped" to runs.values.count { it.ignored }
            "failures" to runs.values.count { it.failure != null }
            "errors" to runs.values.count { it.error != null }
            "time" to ((runs.values.sumByDouble { it.time.toDouble() }) / 1000L)
            runs.values.forEach { run ->
                element("testcase") {
                    "name" to run.description.methodName
                    "classname" to run.description.className
                    "time" to (run.time / 1000)
                    if (run.ignored) {
                        element("skipped")
                    } else if (run.error != null) {
                        element("failure") {
                            "message" to run.error?.message
                            "type" to run.error!!::class.qualifiedName
                            "errorMessage" to run.error?.errorMessage()
                        }
                    } else if (run.failure != null) {
                        element("failure") {
                            "message" to run.error?.message
                            "type" to run.error!!::class.qualifiedName
                            "errorMessage" to run.error?.errorMessage()
                        }
                    }
                }
            }
        } to file
    }

    private fun Failure.errorMessage() =
            ExceptionUtils.getStackTrace(this.exception)

}
