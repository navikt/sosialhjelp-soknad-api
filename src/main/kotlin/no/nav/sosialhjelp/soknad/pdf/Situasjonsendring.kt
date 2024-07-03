package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.situasjonendring.JsonSituasjonendring

object Situasjonsendring {
    fun leggTilSituasjonsendring(
        pdf: PdfGenerator,
        pdfUtils: PdfUtils,
        utvidetSoknad: Boolean,
        situasjonendring: JsonSituasjonendring,
    ) {
        pdf.skrivH4Bold(pdfUtils.getTekst("situasjonsendring.tittel"))
        pdf.addBlankLine()

        pdf.skrivTekstBold(pdfUtils.getTekst("situasjonsendring.harNoeEndretSeg.sporsmal"))

        if (utvidetSoknad) {
            pdfUtils.skrivInfotekst(pdf, "situasjonsendring.hvaHarEndretSeg.infotekst")
        }
        when (situasjonendring.harNoeEndretSeg) {
            null -> {
                pdfUtils.skrivIkkeUtfylt(pdf)
            }
            true -> {
                pdf.skrivTekst(pdfUtils.getTekst("situasjonsendring.harNoeEndretSeg.ja"))
            }
            false -> {
                pdf.skrivTekst(pdfUtils.getTekst("situasjonsendring.harNoeEndretSeg.nei"))
            }
        }
        pdf.addBlankLine()

        pdf.skrivTekstBold(pdfUtils.getTekst("situasjonsendring.hvaHarEndretSeg.sporsmal"))
        if (situasjonendring.hvaHarEndretSeg == null || situasjonendring.hvaHarEndretSeg.isEmpty()) {
            pdfUtils.skrivIkkeUtfylt(pdf)
        } else {
            pdf.skrivTekst(situasjonendring.hvaHarEndretSeg)
        }

        pdf.addBlankLine()
    }
}
