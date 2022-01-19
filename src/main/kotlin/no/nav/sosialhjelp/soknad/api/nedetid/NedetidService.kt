package no.nav.sosialhjelp.soknad.api.nedetid

import no.nav.sosialhjelp.kotlin.utils.logger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class NedetidService(
    private val nedetidStart: String?,
    private val nedetidSlutt: String?
) {

    private fun getNedetid(time: String?): LocalDateTime? {
        return if (time.isNullOrEmpty()) {
            null
        } else try {
            LocalDateTime.parse(time, dateTimeFormatter)
        } catch (e: DateTimeParseException) {
            log.error("Klarte ikke parse $time. Skal være på formatet: $nedetidFormat")
            null
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

    private fun getNedetidAsFormattedStringOrNull(time: String?, formatter: DateTimeFormatter): String? {
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
                now.plusDays(planlagtNedetidVarselAntallDager.toLong()).isAfter(start) && now.isBefore(start)
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

        private const val planlagtNedetidVarselAntallDager = 14

        private const val nedetidFormat = "dd.MM.yyyy HH:mm:ss"
        private const val humanreadableFormat = "EEEE dd.MM.yyyy 'kl.' HH:mm"
        private const val humanreadableFormatEn = "EEEE d MMM yyyy 'at' HH:mm '(CET)'"

        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(nedetidFormat)
        val humanreadableFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(humanreadableFormat, Locale("nb", "NO"))
        val humanreadableFormatterEn: DateTimeFormatter = DateTimeFormatter.ofPattern(humanreadableFormatEn, Locale.ENGLISH)
    }
}
