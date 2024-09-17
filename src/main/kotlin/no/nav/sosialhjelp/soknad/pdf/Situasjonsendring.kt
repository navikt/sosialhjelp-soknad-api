package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.situasjonendring.JsonSituasjonendring

object Situasjonsendring {
    fun leggTilSituasjonsendring(
        pdf: PdfGenerator,
        pdfUtils: PdfUtils,
        utvidetSoknad: Boolean,
        situasjonendring: JsonSituasjonendring?,
    ) {
        pdf.skrivH4Bold(pdfUtils.getTekst("situasjonsendring.tittel"))
        pdf.addBlankLine()

        if (situasjonendring != null) {
            pdf.skrivTekstBold(pdfUtils.getTekst("situasjonsendring.hvaHarEndretSeg.sporsmal"))
            if (utvidetSoknad) {
                pdfUtils.skrivInfotekst(pdf, "situasjonsendring.hvaHarEndretSeg.infotekst")
            }
            if (situasjonendring.hvaHarEndretSeg.isNullOrBlank() == null) {
                pdfUtils.skrivIkkeUtfylt(pdf)
            } else {
                pdf.skrivTekst(situasjonendring.hvaHarEndretSeg)
            }
        }
        pdf.addBlankLine()
    }
}
