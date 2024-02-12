package no.nav.sosialhjelp.soknad.v2.json.compare

import org.skyscreamer.jsonassert.JSONCompareResult
import org.slf4j.LoggerFactory
import java.util.*

class JsonCompareErrorLogger(
    private val soknadId: UUID,
    private val result: JSONCompareResult
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun logAllErrors() {

        parseResultAndReturnErrorList()
            .forEach {
                logError(error = it)
            }
    }
    private fun logError(error: ErrorRow) {
        log.error("$soknadId - ** ${error.errorType} ** - ${error.message}")
    }

    private fun parseResultAndReturnErrorList(): List<ErrorRow> {

        return mutableListOf<ErrorRow>()
            .apply {
                addAll( getFieldFailures() )
                addAll( getFieldMissing() )
                addAll( getArraySizeErrorList() )
            }
    }

    private fun getFieldFailures(): List<ErrorRow> {
        return result.fieldFailures
            .map {
                var errorMessage = it.field
                if (isNotFiltered(it.field)) errorMessage += " {expected: ${it.expected}, actual: ${it.actual}}"
                ErrorRow("FieldFailure", errorMessage)
            }
    }

    private fun getFieldMissing(): List<ErrorRow> {
        return result.fieldMissing
            .map { "${it.field} {expected: ${it.expected}, actual: ${it.actual}}" }
            .map { ErrorRow("MissingField", it) }
    }

    private fun getArraySizeErrorList() = result.message
        .split(";")
        .filter { isArraySizeErrorMessage(it) }
        .map { ErrorRow("ArraySizeError", it) }

    private fun isArraySizeErrorMessage(message: String): Boolean {
        return message.contains("Expected [\\d][\\d]?[\\d]? values but got [\\d][\\d]?[\\d]?".toRegex())
    }

    private data class ErrorRow(
        val errorType: String,
        val message: String
    )

    companion object {
        private fun isNotFiltered(field: String): Boolean {
            return noFilterFields.any { field.contains(it) }
        }

        private val noFilterFields = listOf(
            ".type",
            ".hendelseReferanse",
            ".sha512",
        )
    }
}
