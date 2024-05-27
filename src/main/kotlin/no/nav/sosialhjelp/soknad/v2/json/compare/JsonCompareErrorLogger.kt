package no.nav.sosialhjelp.soknad.v2.json.compare

import no.nav.sosialhjelp.soknad.v2.json.compare.LoggerComparisonErrorTypes.ARRAY_SIZE
import no.nav.sosialhjelp.soknad.v2.json.compare.LoggerComparisonErrorTypes.FIELD_FAILURE
import no.nav.sosialhjelp.soknad.v2.json.compare.LoggerComparisonErrorTypes.MISSING_FIELD
import org.skyscreamer.jsonassert.JSONCompareResult
import org.slf4j.LoggerFactory

class JsonCompareErrorLogger(
    private val result: JSONCompareResult,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun logAllErrors(asOneString: Boolean) {
        if (asOneString) {
            logAllerrorsAsOneString()
        } else {
            getFieldFailures().forEach { logError(it) }
            getFieldsMissing().forEach { logError(it) }
            getArraySizeError().forEach { logError(it) }
        }
    }

    private fun logAllerrorsAsOneString() {
        mutableListOf<String>()
            .apply {
                getFieldFailures().map { createStringForError(it) }.also { addAll(it) }
                getFieldsMissing().map { createStringForError(it) }.also { addAll(it) }
                getArraySizeError().map { createStringForError(it) }.also { addAll(it) }
            }
            .also { logger.warn(it.joinToString(separator = "\n")) }
    }

    private fun createStringForError(error: ErrorRow): String {
        return "${error.type} - ${error.message}"
    }

    private fun logError(error: ErrorRow) {
        logger.warn("${error.type} - ${error.message}")
    }

    private fun getFieldFailures(): List<ErrorRow> {
        return result.fieldFailures
            .map {
                ErrorRow(FIELD_FAILURE, it.field)
            }
    }

    private fun getFieldsMissing(): List<ErrorRow> {
        return result.fieldMissing
            .map { "${it.field} {expected: ${it.expected}, actual: ${it.actual}}" }
            .map { ErrorRow(MISSING_FIELD, it) }
    }

    private fun getArraySizeError() =
        result.message
            .split(";")
            .filter { isArraySizeErrorMessage(it) }
            .map { ErrorRow(ARRAY_SIZE, it) }

    private fun isArraySizeErrorMessage(message: String): Boolean {
        return message.contains("Expected [\\d][\\d]?[\\d]? values but got [\\d][\\d]?[\\d]?".toRegex())
    }

    private data class ErrorRow(
        val type: LoggerComparisonErrorTypes,
        val message: String,
    )
}

enum class LoggerComparisonErrorTypes(private val logString: String) {
    FIELD_FAILURE("** FieldFailure **"),
    MISSING_FIELD("** MissingField **"),
    ARRAY_SIZE("** ArraySize **"),
    ;

    override fun toString(): String {
        return logString
    }
}
