package net.nemerosa.ontrack.xml

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class XmlTest {

    @Test
    fun `Generating XML into a file`() {
        val file = File.createTempFile("xml", "xml")
        Xml.document("testsuite") {
            "ignored" to 0
            "success" to 1
            "error" to 1
            "failure" to 0
            element("testcase") {
                "name" to "method1"
                "classname" to "myTest"
                "time" to 1234
            }
            element("testcase") {
                "name" to "method2"
                "classname" to "myTest"
                "time" to 1234
                element("failure") {
                    "length" to 100
                    +"stacktrace"
                }
            }
        } to file
        val xml = file.readText()
        val expectedXml = javaClass.getResource("/expected.xml").readText()
        assertEquals(
                expectedXml,
                xml
        )
    }

    @Test
    fun `Generating XML into a string`() {
        val xml = Xml.document("testsuite") {
            "ignored" to 0
            "success" to 1
            "error" to 1
            "failure" to 0
            element("testcase") {
                "name" to "method1"
                "classname" to "myTest"
                "time" to 1234
            }
            element("testcase") {
                "name" to "method2"
                "classname" to "myTest"
                "time" to 1234
                element("failure") {
                    "length" to 100
                    +"stacktrace"
                }
            }
        }.toString()
        val expectedXml = javaClass.getResource("/expected.xml").readText()
        assertEquals(
                expectedXml,
                xml
        )
    }

}