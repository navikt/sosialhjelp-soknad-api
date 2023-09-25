package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sosialhjelp.soknad.pdf.Utils.hentBekreftelser

object UtgifterOgGjeld {
    fun leggTilUtgifterOgGjeld(
        pdf: PdfGenerator,
        pdfUtils: PdfUtils,
        okonomi: JsonOkonomi?,
        soknad: JsonSoknad,
        utvidetSoknad: Boolean,
    ) {
        pdf.skrivH4Bold(pdfUtils.getTekst("utgifterbolk.tittel"))
        pdf.addBlankLine()

        if (okonomi == null) return

        leggTilBoutgifter(pdf, pdfUtils, okonomi, utvidetSoknad)
        if (soknad.data.familie?.forsorgerplikt?.harForsorgerplikt?.verdi == true) {
            leggTilForsorgerplikt(pdf, pdfUtils, okonomi, utvidetSoknad)
        }
    }

    private fun leggTilBoutgifter(
        pdf: PdfGenerator,
        pdfUtils: PdfUtils,
        okonomi: JsonOkonomi,
        utvidetSoknad: Boolean,
    ) {
        val boutgifterBekreftelse = hentBekreftelser(okonomi, "boutgifter").firstOrNull()

        pdf.skrivTekstBold(pdfUtils.getTekst("utgifter.boutgift.sporsmal"))
        if (utvidetSoknad) pdfUtils.skrivInfotekst(pdf, "utgifter.boutgift.infotekst.tekst")

        if (boutgifterBekreftelse == null) {
            pdfUtils.skrivIkkeUtfylt(pdf)
        } else {
            pdf.skrivTekst(pdfUtils.getTekst("utgifter.boutgift." + boutgifterBekreftelse.verdi))
            if (boutgifterBekreftelse.verdi) {
                pdf.addBlankLine()
                pdf.skrivTekstBold(pdfUtils.getTekst("utgifter.boutgift.true.type.sporsmal"))

                (okonomi.opplysninger.utgift.map { it.type } + okonomi.oversikt.utgift.map { it.type })
                    .filter {
                        listOf(
                            "husleie",
                            "strom",
                            "kommunalAvgift",
                            "oppvarming",
                            "boliglanAvdrag", // boliglanRenter er ikke tatt med her, da det kun er ett valg for disse i frontend
                            "annenBoutgift"
                        ).contains(it)
                    }
                    .forEach { pdf.skrivTekst(pdfUtils.getTekst("utgifter.boutgift.true.type.$it")) }
            }
        }

        if (utvidetSoknad) {
            pdfUtils.skrivSvaralternativer(
                pdf,
                listOf(
                    "utgifter.boutgift.true",
                    "utgifter.boutgift.false"
                )
            )

            pdf.skrivTekst("Under: " + pdfUtils.getTekst("utgifter.boutgift.true"))

            pdfUtils.skrivSvaralternativer(
                pdf,
                listOf(
                    "utgifter.boutgift.true.type.husleie",
                    "utgifter.boutgift.true.type.strom",
                    "utgifter.boutgift.true.type.kommunalAvgift",
                    "utgifter.boutgift.true.type.oppvarming",
                    "utgifter.boutgift.true.type.boliglanAvdrag",
                    "utgifter.boutgift.true.type.annenBoutgift"
                )
            )
        }
        pdf.addBlankLine()
    }

    private fun leggTilForsorgerplikt(
        pdf: PdfGenerator,
        pdfUtils: PdfUtils,
        okonomi: JsonOkonomi,
        utvidetSoknad: Boolean,
    ) {
        pdf.skrivTekstBold(pdfUtils.getTekst("utgifter.barn.sporsmal"))
        if (utvidetSoknad) pdfUtils.skrivInfotekst(pdf, "utgifter.barn.infotekst.tekst")

        val barneutgiftBekreftelse = hentBekreftelser(okonomi, "barneutgifter").firstOrNull()
        if (barneutgiftBekreftelse == null) {
            pdfUtils.skrivIkkeUtfylt(pdf)
        } else {
            pdf.skrivTekst(pdfUtils.getTekst("utgifter.barn." + barneutgiftBekreftelse.verdi))

            if (barneutgiftBekreftelse.verdi) {
                pdf.addBlankLine()
                pdf.skrivTekstBold(pdfUtils.getTekst("utgifter.barn.true.utgifter.sporsmal"))

                (okonomi.opplysninger.utgift.map { it.type } + okonomi.oversikt.utgift.map { it.type })
                    .filter {
                        listOf(
                            "barnFritidsaktiviteter",
                            "barnehage",
                            "sfo",
                            "barnTannregulering",
                            "annenBarneutgift"
                        ).contains(it)
                    }
                    .forEach { pdf.skrivTekst(pdfUtils.getTekst("utgifter.barn.true.utgifter.$it")) }
            }
        }

        pdf.addBlankLine()

        if (!utvidetSoknad) return
        
        pdfUtils.skrivSvaralternativer(pdf, listOf("utgifter.barn.true", "utgifter.barn.false"))
        pdf.skrivTekst("Under: " + pdfUtils.getTekst("utgifter.barn.true"))

        pdfUtils.skrivSvaralternativer(
            pdf,
            listOf(
                "utgifter.barn.true.utgifter.barnFritidsaktiviteter",
                "utgifter.barn.true.utgifter.barnehage",
                "utgifter.barn.true.utgifter.sfo",
                "utgifter.barn.true.utgifter.barnTannregulering",
                "utgifter.barn.true.utgifter.annenBarneutgift"
            )
        )
    }
}
