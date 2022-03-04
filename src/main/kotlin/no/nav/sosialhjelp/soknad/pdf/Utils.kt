package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.PdfGenerator
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ofPattern
import java.util.Locale

object Utils {

    const val DATO_FORMAT = "d. MMMM yyyy"
    private const val DATO_OG_TID_FORMAT = "d. MMMM yyyy HH:mm"
    private val locale = Locale("nb", "NO")

    fun addLinks(pdf: PdfGenerator, uris: Map<String, String>) {
        pdf.skrivTekst("Lenker på siden: ")
        for ((name, uri) in uris) {
            pdf.skrivTekst("$name: $uri")
        }
        pdf.addBlankLine()
    }

    fun hentUtbetalinger(okonomi: JsonOkonomi, type: String): List<JsonOkonomiOpplysningUtbetaling> {
        return okonomi.opplysninger.utbetaling
            .filter { it.type == type }
    }

    fun hentBekreftelser(okonomi: JsonOkonomi, type: String): List<JsonOkonomibekreftelse> {
        return okonomi.opplysninger.bekreftelse
            ?.filter { it.type == type } ?: emptyList()
    }

    fun formaterDato(dato: String?, format: String): String? {
        return dato?.let { LocalDate.parse(it).format(ofPattern(format, locale)) } ?: ""
    }

    fun formaterDatoOgTidspunkt(isoTimestamp: String?): String? {
        return isoTimestamp?.let {
            ZonedDateTime.parse(it).withZoneSameInstant(ZoneId.of("Europe/Oslo")).format(ofPattern(DATO_OG_TID_FORMAT))
        } ?: ""
    }
}
