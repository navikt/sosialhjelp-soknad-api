package no.nav.sosialhjelp.soknad.api.nedetid

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@Component
class NedetidService(
    @Value("\${nedetid.start}") private val nedetidStart: String?,
    @Value("\${nedetid.slutt}") private val nedetidSlutt: String?,
) {
    private fun getNedetid(time: String?): LocalDateTime? {
        return if (time.isNullOrEmpty()) {
            null
        } else {
            try {
                LocalDateTime.parse(time, dateTimeFormatter)
            } catch (e: DateTimeParseException) {
                log.error("Klarte ikke parse $time. Skal være på formatet: $NEDETID_FORMAT")
                null
            }
        }
    }

    val nedetidStartAsString: String?
        get() = getNedetidAsFormattedStringOrNull(nedetidStart, dateTimeFormatter)

    val nedetidSluttAsString: String?
        get() = getNedetidAsFormattedStringOrNull(nedetidSlutt, dateTimeFormatter)

    val nedetidStartAsHumanReadable: String?
        get() = getNedetidAsFormattedStringOrNull(nedetidStart, humanreadableFormatter)

    val nedetidSluttAsHumanReadable: String?
        get() = getNedetidAsFormattedStringOrNull(nedetidSlutt, humanreadableFormatter)

    val nedetidStartAsHumanReadableEn: String?
        get() = getNedetidAsFormattedStringOrNull(nedetidStart, humanreadableFormatterEn)

    val nedetidSluttAsHumanReadableEn: String?
        get() = getNedetidAsFormattedStringOrNull(nedetidSlutt, humanreadableFormatterEn)

    private fun getNedetidAsFormattedStringOrNull(
        time: String?,
        formatter: DateTimeFormatter,
    ): String? {
        val nedetid = getNedetid(time)
        return nedetid?.format(formatter)
    }

    val isInnenforPlanlagtNedetid: Boolean
        get() {
            val now = LocalDateTime.now()
            val start = getNedetid(nedetidStart)
            val slutt = getNedetid(nedetidSlutt)
            return if (start == null || slutt == null || slutt.isBefore(start)) {
                false
            } else {
                now.plusDays(PLANLAGT_NEDETID_VARSEL_ANTALL_DAGER.toLong()).isAfter(start) && now.isBefore(start)
            }
        }

    val isInnenforNedetid: Boolean
        get() {
            val now = LocalDateTime.now()
            val start = getNedetid(nedetidStart)
            val slutt = getNedetid(nedetidSlutt)
            return if (start == null || slutt == null || slutt.isBefore(start)) {
                false
            } else {
                now.isAfter(start) && now.isBefore(slutt)
            }
        }

    companion object {
        private val log by logger()

        private const val PLANLAGT_NEDETID_VARSEL_ANTALL_DAGER = 14

        private const val NEDETID_FORMAT = "dd.MM.yyyy HH:mm:ss"
        private const val HUMANREADABLE_FORMAT = "EEEE dd.MM.yyyy 'kl.' HH:mm"
        private const val HUMANREADABLE_FORMAT_EN = "EEEE d MMM yyyy 'at' HH:mm '(CET)'"

        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(NEDETID_FORMAT)
        val humanreadableFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(HUMANREADABLE_FORMAT, Locale.forLanguageTag("nb-NO"))
        val humanreadableFormatterEn: DateTimeFormatter = DateTimeFormatter.ofPattern(HUMANREADABLE_FORMAT_EN, Locale.ENGLISH)
    }
}
