package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.pdf.Utils.addLinks

object InformasjonFraForside {
    fun leggTilInformasjonFraForsiden(
        pdf: PdfGenerator,
        pdfUtils: PdfUtils,
        personalia: JsonPersonalia,
        utvidetSoknad: Boolean,
    ) {
        if (utvidetSoknad) {
            pdf.skrivH4Bold(pdfUtils.getTekst("informasjon.tittel"))
            pdf.addBlankLine()
            pdf.skrivTekst("Hei " + personalia.navn.fornavn)
            pdf.skrivTekst(pdfUtils.getTekst("informasjon.hilsen.tittel"))
            pdf.addBlankLine()
            pdf.skrivTekstBold(pdfUtils.getTekst("informasjon.start.undertittel"))
            pdf.skrivTekst("Før du søker bør du undersøke andre muligheter til å forsørge deg selv. Les mer om andre muligheter.")
            pdf.addBlankLine()
            pdf.skrivTekst("Du må i utgangspunktet ha lovlig opphold og fast bopel i Norge for å ha rett til økonomisk sosialhjelp. Hvis du oppholder deg i utlandet, har du ikke rett til økonomisk sosialhjelp.")
            pdf.addBlankLine()
            pdf.skrivTekst("Søknaden skal bare brukes til å søke om økonomisk sosialhjelp. Skal du søke om andre sosiale tjenester, som for eksempel kvalifiseringsprogram, må du ta kontakt med NAV-kontoret ditt")
            pdf.addBlankLine()
            pdf.skrivTekstBold(pdfUtils.getTekst("informasjon.nodsituasjon.undertittel"))
            pdf.skrivTekst("Hvis du ikke har penger til det aller mest nødvendige, som mat, bør du kontakte NAV-kontoret ditt før du sender inn søknaden eller så snart som mulig etter du har søkt. NAV skal også hjelpe deg med å finne et midlertidig botilbud hvis du ikke har et sted å sove eller oppholde deg det nærmeste døgnet.")
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
            urisOnPage["Les mer om andre muligheter"] = "https://www.nav.no/sosialhjelp/"
            urisOnPage["NAV-kontoret ditt"] = "https://www.nav.no/person/personopplysninger/nb/#ditt-nav-kontor"
            urisOnPage["kontakte NAV-kontoret"] = "https://www.nav.no/person/personopplysninger/#ditt-nav-kontor"
            addLinks(pdf, urisOnPage)
        }
    }
}
