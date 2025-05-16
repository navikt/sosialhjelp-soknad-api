package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.pdf.Utils.formaterDatoOgTidspunkt

object Metainformasjon {
    fun leggTilMetainformasjon(
        pdf: PdfGenerator,
        soknad: JsonSoknad,
    ) {
        logger.info("Innsendingstidspunkt PDF: ${formaterDatoOgTidspunkt(soknad.innsendingstidspunkt)}")
        pdf.skrivTekst("Søknaden er sendt " + formaterDatoOgTidspunkt(soknad.innsendingstidspunkt))
        pdf.skrivTekst("Versjonsnummer: " + soknad.version)
        if (soknad.mottaker != null) {
            pdf.skrivTekst("Sendt til: " + soknad.mottaker.navEnhetsnavn)
        }
    }

    private val logger by logger()
}
