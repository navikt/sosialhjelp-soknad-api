package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning
import no.nav.sosialhjelp.soknad.pdf.Utils.DATO_FORMAT
import no.nav.sosialhjelp.soknad.pdf.Utils.formaterDato

fun leggTilArbeidOgUtdanning(
    pdf: PdfGenerator,
    pdfUtils: PdfUtils,
    arbeid: JsonArbeid?,
    utdanning: JsonUtdanning?,
    utvidetSoknad: Boolean,
) {
    pdf.skrivH4Bold(pdfUtils.getTekst("arbeidbolk.tittel"))
    pdf.addBlankLine()

    pdf.skrivTekstBold(pdfUtils.getTekst("arbeidsforhold.sporsmal"))
    pdf.addBlankLine()

    if (utvidetSoknad) {
        pdfUtils.skrivInfotekst(pdf, "arbeidsforhold.infotekst")
    }

    if (arbeid != null && arbeid.forhold != null) {
        arbeid.forhold.forEach { forhold ->
            pdfUtils.skrivTekstMedGuard(pdf, forhold.arbeidsgivernavn, "arbeidsforhold.arbeidsgivernavn.label")
            pdfUtils.skrivTekstMedGuard(pdf, formaterDato(forhold.fom, DATO_FORMAT), "arbeidsforhold.fom.label")
            pdfUtils.skrivTekstMedGuard(pdf, formaterDato(forhold.tom, DATO_FORMAT), "arbeidsforhold.tom.label")

            forhold.stillingsprosent?.let { pdf.skrivTekst("${pdfUtils.getTekst("arbeidsforhold.stillingsprosent.label")}: $it%") }
            pdf.addBlankLine()
        }
    } else {
        pdf.skrivTekst(pdfUtils.getTekst("arbeidsforhold.ingen"))
        pdf.addBlankLine()
    }

    if (arbeid != null && arbeid.kommentarTilArbeidsforhold != null && arbeid.kommentarTilArbeidsforhold.verdi != null) {
        pdf.skrivTekst(pdfUtils.getTekst("opplysninger.arbeidsituasjon.kommentarer.label"))
        pdf.addBlankLine()
        pdf.skrivTekstBold("Kommentar til arbeidsforhold:")
        pdf.skrivTekst(arbeid.kommentarTilArbeidsforhold.verdi)
        pdf.addBlankLine()
    } else if (utvidetSoknad) {
        pdf.skrivTekst(pdfUtils.getTekst("opplysninger.arbeidsituasjon.kommentarer.label"))
        pdf.addBlankLine()
        pdf.skrivTekstBold("Kommentar til arbeidsforhold:")
        pdfUtils.skrivIkkeUtfylt(pdf)
        pdf.addBlankLine()
    }

    pdf.skrivTekstBold(pdfUtils.getTekst("arbeid.dinsituasjon.studerer.undertittel"))
    pdf.addBlankLine()

    pdf.skrivTekstBold(pdfUtils.getTekst("dinsituasjon.studerer.sporsmal"))
    utdanning?.erStudent
        ?.let { pdf.skrivTekst(pdfUtils.getTekst("dinsituasjon.studerer.$it")) }
        ?: pdfUtils.skrivIkkeUtfylt(pdf)

    pdf.addBlankLine()

    if (utvidetSoknad) {
        val svaralternativer: MutableList<String> = ArrayList(2)
        svaralternativer.add("dinsituasjon.studerer.true")
        svaralternativer.add("dinsituasjon.studerer.false")
        pdfUtils.skrivSvaralternativer(pdf, svaralternativer)
    }

    if (utdanning != null && utdanning.erStudent != null && utdanning.erStudent) {
        pdf.skrivTekstBold(pdfUtils.getTekst("dinsituasjon.studerer.true.grad.sporsmal"))

        utdanning.studentgrad
            ?.let { pdf.skrivTekst(pdfUtils.getTekst("dinsituasjon.studerer.true.grad.$it")) }
            ?: pdfUtils.skrivIkkeUtfylt(pdf)

        pdf.addBlankLine()

        if (utvidetSoknad) {
            val svaralternativer: MutableList<String> = ArrayList(2)
            svaralternativer.add("dinsituasjon.jobb.true.grad.deltid")
            svaralternativer.add("dinsituasjon.jobb.true.grad.heltid")
            pdfUtils.skrivSvaralternativer(pdf, svaralternativer)
        }
    }
}
