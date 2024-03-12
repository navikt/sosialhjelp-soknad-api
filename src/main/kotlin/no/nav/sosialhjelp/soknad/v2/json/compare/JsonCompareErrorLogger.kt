package no.nav.sosialhjelp.soknad.v2.json.compare

import no.nav.sosialhjelp.soknad.v2.json.compare.LoggerComparisonErrorTypes.ARRAY_SIZE
import no.nav.sosialhjelp.soknad.v2.json.compare.LoggerComparisonErrorTypes.FIELD_FAILURE
import no.nav.sosialhjelp.soknad.v2.json.compare.LoggerComparisonErrorTypes.MISSING_FIELD
import org.skyscreamer.jsonassert.JSONCompareResult
import org.slf4j.LoggerFactory
import java.util.*

class JsonCompareErrorLogger(
    private val soknadId: UUID,
    private val result: JSONCompareResult
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun logAllErrors() {

        getFieldFailures().forEach { logError(it) }
        getFieldMissing().forEach { logError(it) }
        getArraySizeErrorList().forEach { logError(it) }
    }
    private fun logError(error: ErrorRow) {
        logger.error("$soknadId - ${error.type} - ${error.message}")
    }

    private fun getFieldFailures(): List<ErrorRow> {
        return result.fieldFailures
            .map {
                ErrorRow(FIELD_FAILURE, it.field)
            }
    }

    private fun getFieldMissing(): List<ErrorRow> {
        return result.fieldMissing
            .map { "${it.field} {expected: ${it.expected}, actual: ${it.actual}}" }
            .map { ErrorRow(MISSING_FIELD, it) }
    }

    private fun getArraySizeErrorList() = result.message
        .split(";")
        .filter { isArraySizeErrorMessage(it) }
        .map { ErrorRow(ARRAY_SIZE, it) }

    private fun isArraySizeErrorMessage(message: String): Boolean {
        return message.contains("Expected [\\d][\\d]?[\\d]? values but got [\\d][\\d]?[\\d]?".toRegex())
    }

    private data class ErrorRow(
        val type: LoggerComparisonErrorTypes,
        val message: String
    )
}

enum class LoggerComparisonErrorTypes(private val logString: String) {
    FIELD_FAILURE("** FieldFailure **"),
    MISSING_FIELD("** MissingField **"),
    ARRAY_SIZE("** ArraySize **");

    override fun toString(): String {
        return logString
    }
}
