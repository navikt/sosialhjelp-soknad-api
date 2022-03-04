package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.PdfGenerator
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object Utils {

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

    fun formaterDatoOgTidspunkt(isoTimestamp: String?): String? {
        if (isoTimestamp == null) {
            return ""
        }
        val format = "d. MMMM yyyy HH:mm"
        val dateFormatter = DateTimeFormatter.ofPattern(format)
        val zonedDate = ZonedDateTime.parse(isoTimestamp).withZoneSameInstant(ZoneId.of("Europe/Oslo"))
        return zonedDate.format(dateFormatter)
    }
}
