package no.nav.sosialhjelp.soknad.v2.json.compare

import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.v2.json.compare.LoggerComparisonErrorTypes.ARRAY_SIZE
import no.nav.sosialhjelp.soknad.v2.json.compare.LoggerComparisonErrorTypes.FIELD_FAILURE
import no.nav.sosialhjelp.soknad.v2.json.compare.LoggerComparisonErrorTypes.MISSING_FIELD
import org.skyscreamer.jsonassert.FieldComparisonFailure
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
            .filter { KeyFilter.isNotFiltered(it.field) }
            .mapNotNull {
                when {
                    ExpectedDiffHandler.isExpectedDiff(it.field) -> null
                    else -> ErrorRow(FIELD_FAILURE, ErrorStringHandler.createErrorString(it))
                }
            }
    }

    private fun getFieldsMissing(): List<ErrorRow> {
        return result.fieldMissing
//            .filter { !KeyFilter.isFiltered(it.field) }
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
    EXPECTED_DIFF("** ExpectedDiff **"),
    ;

    override fun toString(): String {
        return logString
    }
}

object ExpectedDiffHandler {
    fun isExpectedDiff(field: String) = expectedRegExDiffs.any { it.matches(field) }

    private val expectedRegExDiffs =
        listOf(
            Regex("soknad.data.okonomi.opplysninger.utgift\\[\\d+].type"),
            Regex("soknad.data.okonomi.opplysninger.utgift\\[\\d+].tittel"),
            Regex("soknad.data.okonomi.opplysninger.bekreftelse\\[\\d+].bekreftelsesDato"),
        )
}

object KeyFilter {
    fun isNotFiltered(field: String): Boolean = filteredKeys.none { field.contains(it) }

    private val filteredKeys =
        listOf(
            "soknad.data.okonomi.opplysninger.utgift",
            "soknad.data.okonomi.opplysninger.utbetaling",
            "soknad.data.okonomi.opplysninger.utgift",
            "soknad.data.okonomi.oversikt.formue",
            "soknad.data.okonomi.oversikt.utgift",
            "soknad.data.okonomi.oversikt.inntekt",
            "vedlegg.vedlegg",
            "soknad.data.okonomi.opplysninger.bostotte.saker",
            "soknad.data.arbeid.forhold",
        )
}

object ErrorStringHandler {
    fun createErrorString(failure: FieldComparisonFailure): String {
        if (MiljoUtils.isNonProduction()) return createErrorStringWithExpectedAndActual(failure)

        return if (!writeComparisonFields.any { it.matches(failure.field) }) {
            failure.field
        } else {
            "${failure.field} -> actual=( ${failure.actual} ) : expected=( ${failure.expected} ) "
        }
    }

    private fun createErrorStringWithExpectedAndActual(failure: FieldComparisonFailure): String {
        return "${failure.field} -> actual=( ${failure.actual} ) : expected=( ${failure.expected} ) "
    }

    private val writeComparisonFields =
        listOf(
            Regex("soknad.innsendingstidspunkt"),
        )
}
