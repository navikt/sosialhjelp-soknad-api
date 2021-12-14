package no.nav.sosialhjelp.soknad.api.nedetid

import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object NedetidUtils {
    private val log = LoggerFactory.getLogger(NedetidUtils::class.java)
    private const val planlagtNedetidVarselAntallDager = 14
    const val NEDETID_START = "nedetid.start"
    const val NEDETID_SLUTT = "nedetid.slutt"
    const val nedetidFormat = "dd.MM.yyyy HH:mm:ss"
    const val humanreadableFormat = "EEEE dd.MM.yyyy 'kl.' HH:mm"
    const val humanreadableFormatEn = "EEEE d MMM yyyy 'at' HH:mm '(CET)'"
    val dateTimeFormatter = DateTimeFormatter.ofPattern(nedetidFormat)
    val humanreadableFormatter = DateTimeFormatter.ofPattern(humanreadableFormat, Locale("nb", "NO"))
    val humanreadableFormatterEn = DateTimeFormatter.ofPattern(humanreadableFormatEn, Locale.ENGLISH)

    private fun getNedetid(propertyname: String): LocalDateTime? {
        val nedetid = System.getProperty(propertyname, null)
        return if (nedetid == null || nedetid.isEmpty()) null else try {
            LocalDateTime.parse(nedetid, dateTimeFormatter)
        } catch (e: DateTimeParseException) {
            log.error("Klarte ikke parse {}: {}. Skal være på formatet: {}", propertyname, nedetid, nedetidFormat)
            null
        }
    }

    fun getNedetidAsStringOrNull(propertyname: String): String? {
        return getNedetidAsFormattedStringOrNull(propertyname, dateTimeFormatter)
    }

    fun getNedetidAsHumanReadable(propertyname: String): String? {
        return getNedetidAsFormattedStringOrNull(propertyname, humanreadableFormatter)
    }

    fun getNedetidAsHumanReadableEn(propertyname: String): String? {
        return getNedetidAsFormattedStringOrNull(propertyname, humanreadableFormatterEn)
    }

    private fun getNedetidAsFormattedStringOrNull(propertyname: String, formatter: DateTimeFormatter): String? {
        val nedetid = getNedetid(propertyname)
        return nedetid?.format(formatter)
    }

    val isInnenforPlanlagtNedetid: Boolean
        get() {
            val now = LocalDateTime.now()
            val start = getNedetid(NEDETID_START)
            val slutt = getNedetid(NEDETID_SLUTT)
            return if (start == null || slutt == null || slutt.isBefore(start)) {
                false
            } else {
                now.plusDays(planlagtNedetidVarselAntallDager.toLong()).isAfter(start) && now.isBefore(start)
            }
        }

    val isInnenforNedetid: Boolean
        get() {
            val now = LocalDateTime.now()
            val start = getNedetid(NEDETID_START)
            val slutt = getNedetid(NEDETID_SLUTT)
            return if (start == null || slutt == null || slutt.isBefore(start)) {
                false
            } else {
                now.isAfter(start) && now.isBefore(slutt)
            }
        }
}
