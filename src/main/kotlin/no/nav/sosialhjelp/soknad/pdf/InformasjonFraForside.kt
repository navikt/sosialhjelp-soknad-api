package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.pdf.Utils.addLinks

object InformasjonFraForside {
    fun leggTilInformasjonFraForsiden(
        pdf: PdfGenerator,
        pdfUtils: PdfUtils,
        personalia: JsonPersonalia,
        utvidetSoknad: Boolean
    ) {
        if (utvidetSoknad) {
            pdf.skrivH4Bold(pdfUtils.getTekst("informasjon.tittel"))
            pdf.addBlankLine()
            pdf.skrivTekst("Hei " + personalia.navn.fornavn)
            pdf.skrivTekst(pdfUtils.getTekst("informasjon.hilsen.tittel"))
            pdf.addBlankLine()
            pdf.skrivTekstBold(pdfUtils.getTekst("informasjon.start.undertittel"))
            pdf.skrivTekst(pdfUtils.getTekst("informasjon.start.tekst_del1"))
            pdf.addBlankLine()
            pdf.skrivTekst(pdfUtils.getTekst("informasjon.start.tekst_del2"))
            pdf.addBlankLine()
            pdf.skrivTekst("Se eksempler på hvilke opplysninger du kan bli bedt om å levere.") // informasjon.start.tekst_del3
            pdf.addBlankLine()
            pdf.skrivTekstBold(pdfUtils.getTekst("informasjon.svarpasoknad.undertittel"))
            pdf.skrivTekst(pdfUtils.getTekst("informasjon.svarpasoknad.tekst"))
            pdf.addBlankLine()
            pdf.skrivTekstBold(pdfUtils.getTekst("informasjon.nodsituasjon.undertittel"))
            pdf.skrivTekst("Du er i en nødsituasjon hvis du ikke har penger til det aller mest nødvendige som mat og hygieneartikler, eller du ikke har et sted å sove og oppholde deg det neste døgnet. Hvis du trenger rask hjelp bør du ta kontakt med ditt NAV-kontor eller være tilgjengelig på telefon etter at du har sendt inn søknaden. Ofte vil noen fra NAV-kontoret kontakte deg for å kunne vurdere situasjonen din.") // informasjon.nodsituasjon.tekst
            pdf.addBlankLine()
            pdf.skrivTekstBold(pdfUtils.getTekst("informasjon.tekster.personopplysninger.innhenting.tittel"))
            pdf.skrivTekst(pdfUtils.getTekst("informasjon.tekster.personopplysninger.innhenting.tekst"))
            pdf.addBlankLine()
            pdf.skrivTekstBold(pdfUtils.getTekst("informasjon.tekster.personopplysninger.fordusender.tittel"))
            pdf.skrivTekst(pdfUtils.getTekst("informasjon.tekster.personopplysninger.fordusender.tekst"))
            pdf.addBlankLine()
            pdf.skrivTekstBold(pdfUtils.getTekst("informasjon.tekster.personopplysninger.ettersendt.tittel"))
            pdf.skrivTekst(pdfUtils.getTekst("informasjon.tekster.personopplysninger.ettersendt.tekst"))
            pdf.addBlankLine()
            pdf.skrivTekstBold(pdfUtils.getTekst("informasjon.tekster.personopplysninger.rettigheter.tittel"))
            pdf.skrivTekst(pdfUtils.getTekst("informasjon.tekster.personopplysninger.rettigheter.tekst"))
            pdf.addBlankLine()
            pdf.skrivTekst(pdfUtils.getTekst("informasjon.tekster.personopplysninger.sporsmal"))
            pdf.addBlankLine()

            val urisOnPage: MutableMap<String, String> = HashMap()
            urisOnPage["opplysninger du kan bli bedt om å levere"] = "https://www.nav.no/okonomisk-sosialhjelp#soknad"
            urisOnPage["ditt NAV-kontor"] = "https://www.nav.no/sok-nav-kontor"
            addLinks(pdf, urisOnPage)
        }
    }
}
