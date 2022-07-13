package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sosialhjelp.soknad.pdf.Utils.hentBekreftelser

fun leggTilUtgifterOgGjeld(
    pdf: PdfGenerator,
    pdfUtils: PdfUtils,
    okonomi: JsonOkonomi?,
    soknad: JsonSoknad,
    utvidetSoknad: Boolean
) {
    pdf.skrivH4Bold(pdfUtils.getTekst("utgifterbolk.tittel"))
    pdf.addBlankLine()

    if (okonomi != null) {
        // Boutgifter
        pdf.skrivTekstBold(pdfUtils.getTekst("utgifter.boutgift.sporsmal"))
        if (utvidetSoknad) {
            pdfUtils.skrivInfotekst(pdf, "utgifter.boutgift.infotekst.tekst")
        }
        val boutgifterBekreftelser: List<JsonOkonomibekreftelse> = hentBekreftelser(okonomi, "boutgifter")
        if (boutgifterBekreftelser.isNotEmpty()) {
            val boutgifterBekreftelse = boutgifterBekreftelser[0]

            pdf.skrivTekst(pdfUtils.getTekst("utgifter.boutgift." + boutgifterBekreftelse.verdi))
            if (boutgifterBekreftelse.verdi) {
                pdf.addBlankLine()
                pdf.skrivTekstBold(pdfUtils.getTekst("utgifter.boutgift.true.type.sporsmal"))

                val boutgiftAlternativer: MutableList<String> = ArrayList(6)
                boutgiftAlternativer.add("husleie")
                boutgiftAlternativer.add("strom")
                boutgiftAlternativer.add("kommunalAvgift")
                boutgiftAlternativer.add("oppvarming")
                boutgiftAlternativer.add("boliglanAvdrag") // boliglanRenter er ikke tatt med her, da det kun er ett valg for disse i frontend
                boutgiftAlternativer.add("annenBoutgift")

                for (opplysningUtgift in okonomi.opplysninger.utgift) {
                    if (boutgiftAlternativer.contains(opplysningUtgift.type)) {
                        pdf.skrivTekst(pdfUtils.getTekst("utgifter.boutgift.true.type." + opplysningUtgift.type))
                    }
                }
                for (oversiktUtgift in okonomi.oversikt.utgift) {
                    if (boutgiftAlternativer.contains(oversiktUtgift.type)) {
                        pdf.skrivTekst(pdfUtils.getTekst("utgifter.boutgift.true.type." + oversiktUtgift.type))
                    }
                }
            }
        } else {
            pdfUtils.skrivIkkeUtfylt(pdf)
        }
        if (utvidetSoknad) {
            val boutgifterSvaralternativer: MutableList<String> = ArrayList(2)
            boutgifterSvaralternativer.add("utgifter.boutgift.true")
            boutgifterSvaralternativer.add("utgifter.boutgift.false")
            pdfUtils.skrivSvaralternativer(pdf, boutgifterSvaralternativer)
            pdf.skrivTekst("Under: " + pdfUtils.getTekst("utgifter.boutgift.true"))

            val boutgifterJaSvaralternativer: MutableList<String> = ArrayList()
            boutgifterJaSvaralternativer.add("utgifter.boutgift.true.type.husleie")
            boutgifterJaSvaralternativer.add("utgifter.boutgift.true.type.strom")
            boutgifterJaSvaralternativer.add("utgifter.boutgift.true.type.kommunalAvgift")
            boutgifterJaSvaralternativer.add("utgifter.boutgift.true.type.oppvarming")
            boutgifterJaSvaralternativer.add("utgifter.boutgift.true.type.boliglanAvdrag")
            boutgifterJaSvaralternativer.add("utgifter.boutgift.true.type.annenBoutgift")
            pdfUtils.skrivSvaralternativer(pdf, boutgifterJaSvaralternativer)
        }
        pdf.addBlankLine()

        // Forsørgerplikt
        if (soknad.data.familie != null && soknad.data.familie.forsorgerplikt != null && soknad.data.familie.forsorgerplikt.harForsorgerplikt != null && soknad.data.familie.forsorgerplikt.harForsorgerplikt.verdi) {
            pdf.skrivTekstBold(pdfUtils.getTekst("utgifter.barn.sporsmal"))
            if (utvidetSoknad) {
                pdfUtils.skrivInfotekst(pdf, "utgifter.barn.infotekst.tekst")
            }
            val barneutgifterBekreftelser: List<JsonOkonomibekreftelse> = hentBekreftelser(okonomi, "barneutgifter")
            if (barneutgifterBekreftelser.isNotEmpty()) {
                val barneutgiftBekreftelse = barneutgifterBekreftelser[0]

                pdf.skrivTekst(pdfUtils.getTekst("utgifter.barn." + barneutgiftBekreftelse.verdi))

                if (barneutgiftBekreftelse.verdi) {
                    pdf.addBlankLine()
                    pdf.skrivTekstBold(pdfUtils.getTekst("utgifter.barn.true.utgifter.sporsmal"))

                    val utgifterBarnAlternativer: MutableList<String> = ArrayList(5)
                    utgifterBarnAlternativer.add("barnFritidsaktiviteter")
                    utgifterBarnAlternativer.add("barnehage")
                    utgifterBarnAlternativer.add("sfo")
                    utgifterBarnAlternativer.add("barnTannregulering")
                    utgifterBarnAlternativer.add("annenBarneutgift")

                    for (opplysningUtgift in okonomi.opplysninger.utgift) {
                        if (utgifterBarnAlternativer.contains(opplysningUtgift.type)) {
                            pdf.skrivTekst(pdfUtils.getTekst("utgifter.barn.true.utgifter." + opplysningUtgift.type))
                        }
                    }
                    for (oversiktUtgift in okonomi.oversikt.utgift) {
                        if (utgifterBarnAlternativer.contains(oversiktUtgift.type)) {
                            pdf.skrivTekst(pdfUtils.getTekst("utgifter.barn.true.utgifter." + oversiktUtgift.type))
                        }
                    }
                }
            } else {
                pdfUtils.skrivIkkeUtfylt(pdf)
            }

            pdf.addBlankLine()

            if (utvidetSoknad) {
                val utgifterBarnSvaralternativer: MutableList<String> = ArrayList(2)
                utgifterBarnSvaralternativer.add("utgifter.barn.true")
                utgifterBarnSvaralternativer.add("utgifter.barn.false")
                pdfUtils.skrivSvaralternativer(pdf, utgifterBarnSvaralternativer)
                pdf.skrivTekst("Under: " + pdfUtils.getTekst("utgifter.barn.true"))

                val utgifterBarnJaSvaralternativer: MutableList<String> = ArrayList(5)
                utgifterBarnJaSvaralternativer.add("utgifter.barn.true.utgifter.barnFritidsaktiviteter") // Fritid
                utgifterBarnJaSvaralternativer.add("utgifter.barn.true.utgifter.barnehage") // Barnehage
                utgifterBarnJaSvaralternativer.add("utgifter.barn.true.utgifter.sfo") // SFO
                utgifterBarnJaSvaralternativer.add("utgifter.barn.true.utgifter.barnTannregulering") // Regulering
                utgifterBarnJaSvaralternativer.add("utgifter.barn.true.utgifter.annenBarneutgift") // Annet
                pdfUtils.skrivSvaralternativer(pdf, utgifterBarnJaSvaralternativer)
            }
        }
    }
}
