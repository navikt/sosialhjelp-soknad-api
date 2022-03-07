package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.PdfGenerator

fun leggTilBegrunnelse(pdf: PdfGenerator, pdfUtils: PdfUtils, jsonBegrunnelse: JsonBegrunnelse, utvidetSoknad: Boolean) {
    pdf.skrivH4Bold(pdfUtils.getTekst("begrunnelsebolk.tittel"))
    pdf.addBlankLine()

    pdf.skrivTekstBold(pdfUtils.getTekst("begrunnelse.hva.sporsmal"))

    if (utvidetSoknad) {
        pdfUtils.skrivInfotekst(pdf, "begrunnelse.hva.infotekst.tekst")
    }
    if (jsonBegrunnelse.hvaSokesOm == null || jsonBegrunnelse.hvaSokesOm.isEmpty()) {
        pdfUtils.skrivIkkeUtfylt(pdf)
    } else {
        pdf.skrivTekst(jsonBegrunnelse.hvaSokesOm)
    }
    pdf.addBlankLine()

    pdf.skrivTekstBold(pdfUtils.getTekst("begrunnelse.hvorfor.sporsmal"))
    if (jsonBegrunnelse.hvorforSoke == null || jsonBegrunnelse.hvorforSoke.isEmpty()) {
        pdfUtils.skrivIkkeUtfylt(pdf)
    } else {
        pdf.skrivTekst(jsonBegrunnelse.hvorforSoke)
    }
    pdf.addBlankLine()
}
