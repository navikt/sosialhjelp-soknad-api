package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.PdfGenerator
import no.nav.sosialhjelp.soknad.pdf.Utils.formaterDatoOgTidspunkt

fun leggTilMetainformasjon(pdf: PdfGenerator, soknad: JsonSoknad) {
    pdf.skrivTekst("Søknaden er sendt " + formaterDatoOgTidspunkt(soknad.innsendingstidspunkt))
    pdf.skrivTekst("Versjonsnummer: " + soknad.version)
    if (soknad.mottaker != null) {
        pdf.skrivTekst("Sendt til: " + soknad.mottaker.navEnhetsnavn)
    }
}
