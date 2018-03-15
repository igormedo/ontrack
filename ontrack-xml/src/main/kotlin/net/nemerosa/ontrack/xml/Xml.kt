package net.nemerosa.ontrack.xml

import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class Xml {

    companion object {
        fun document(root: String, code: XmlElement.() -> Unit) =
                Xml().apply { document(root, code) }
    }

    val dom: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()

    infix fun to(file: File) {
        val stream = file.outputStream()
        @Suppress("ConvertTryFinallyToUseCall")
        try {
            to(stream)
        } finally {
            stream.close()
        }
    }

    private infix fun to(stream: OutputStream) {
        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
        val source = DOMSource(dom)
        val result = StreamResult(stream)
        transformer.transform(source, result)
    }

    override fun toString(): String {
        val stream = ByteArrayOutputStream()
        try {
            to(stream)
        } finally {
            stream.close()
        }
        return String(stream.toByteArray())
    }

    fun document(root: String, code: XmlElement.() -> Unit) {
        val e = dom.createElement(root)
        dom.appendChild(e)
        XmlElement(e).code()
    }

    inner class XmlElement(private val e: Element) {
        infix fun String.to(o: Any) {
            e.setAttribute(this, o.toString())
        }

        fun element(name: String, code: XmlElement.() -> Unit = {}) {
            val child = dom.createElement(name)
            e.appendChild(child)
            XmlElement(child).code()
        }

        operator fun String.unaryPlus() {
            e.appendChild(dom.createTextNode(this))
        }
    }

}