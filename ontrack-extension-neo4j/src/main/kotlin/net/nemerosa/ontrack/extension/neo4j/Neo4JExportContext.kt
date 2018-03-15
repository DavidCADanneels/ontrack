package net.nemerosa.ontrack.extension.neo4j

import net.nemerosa.ontrack.extension.neo4j.model.Neo4JExportRecordDef
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringEscapeUtils
import java.io.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

open class Neo4JExportContext(
        val uuid: String,
        val dir: File
) : Closeable {

    private val records = ConcurrentHashMap<String, RecordFile>()

    val paths: List<String>
        get() = records.values
                .map { r -> getFileName(r.recordDef) }
                .sorted()

    fun writeRow(name: String, row: List<*>) {
        writeCsvLine(
                getWriter(name),
                row
        )
    }

    private fun getWriter(name: String): PrintWriter {
        val recordFile = records[name]
        return if (recordFile != null) {
            recordFile.writer
        } else {
            throw IllegalStateException("No CSV writer has been initialized for " + name)
        }
    }

    private fun writeCsvLine(writer: PrintWriter, row: List<*>) {
        writer.println(
                row.joinToString(NEO4J_EXPORT_CSV_DELIMITER) { this.formatCsvValue(it) }
        )
    }

    private fun formatCsvValue(o: Any?): String {
        return if (o is String) {
            val text = o as String?
            StringEscapeUtils.escapeCsv(text)
        } else o?.toString() ?: ""
    }

    fun init(recordDef: Neo4JExportRecordDef<*>) {
        val file = getFileName(recordDef)
        val writer = createWriter(file, { w ->
            writeCsvLine(
                    w,
                    recordDef.columns.map { it.header }
            )
        })
        records[recordDef.name] = RecordFile(recordDef, writer)
    }

    private fun getFileName(recordDef: Neo4JExportRecordDef<*>): String =
            "${recordDef.type.name.toLowerCase()}/${recordDef.name}.csv"

    private fun createWriter(file: String, initFn: (PrintWriter) -> Unit): PrintWriter {
        try {
            val f = File(dir, file)
            FileUtils.forceMkdirParent(f)
            val writer = PrintWriter(OutputStreamWriter(FileOutputStream(f), "UTF-8"))
            initFn(writer)
            return writer
        } catch (e: IOException) {
            throw RuntimeException("Cannot create file", e)
        }

    }

    override fun close() {
        records.values.forEach(Consumer { it.close() })
    }

    @Throws(FileNotFoundException::class)
    fun open(file: String): InputStream {
        val f = File(dir, file)
        return BufferedInputStream(FileInputStream(f))
    }

    data class RecordFile(
            val recordDef: Neo4JExportRecordDef<*>,
            val writer: PrintWriter) {
        fun close() {
            writer.flush()
            writer.close()
        }
    }
}
