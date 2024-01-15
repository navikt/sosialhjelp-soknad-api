package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.JsonOkonomiUtils.isOkonomiskeOpplysningerBekreftet
import no.nav.sosialhjelp.soknad.pdf.Utils.hentUtbetalinger

object OkonomiskeOpplysningerOgVedlegg {
    fun leggTilOkonomiskeOpplysningerOgVedlegg(
        pdf: PdfGenerator,
        pdfUtils: PdfUtils,
        okonomi: JsonOkonomi,
        vedleggSpesifikasjon: JsonVedleggSpesifikasjon?,
        utvidetSoknad: Boolean,
    ) {
        val utgifterBarnAlternativer: MutableList<String> = ArrayList(5)
        utgifterBarnAlternativer.add("barnFritidsaktiviteter")
        utgifterBarnAlternativer.add("barnehage")
        utgifterBarnAlternativer.add("sfo")
        utgifterBarnAlternativer.add("barnTannregulering")
        utgifterBarnAlternativer.add("annenBarneutgift")

        val boutgiftAlternativer: MutableList<String> = ArrayList(7)
        boutgiftAlternativer.add("husleie")
        boutgiftAlternativer.add("strom")
        boutgiftAlternativer.add("kommunalAvgift")
        boutgiftAlternativer.add("oppvarming")
        boutgiftAlternativer.add("boliglanAvdrag")
        boutgiftAlternativer.add("boliglanRenter")
        boutgiftAlternativer.add("annenBoutgift")

        pdf.skrivH4Bold(pdfUtils.getTekst("opplysningerbolk.tittel"))
        pdf.addBlankLine()
        if (utvidetSoknad) {
            if (isOkonomiskeOpplysningerBekreftet(okonomi)) {
                pdfUtils.skrivInfotekst(
                    pdf,
                    "opplysninger.informasjon.avsnitt1",
                    "opplysninger.informasjon.avsnitt2",
                    "opplysninger.informasjon.lenke",
                )
                skrivOkonomiskeOpplysningerModal(pdf, pdfUtils)
            } else {
                pdfUtils.skrivInfotekst(
                    pdf,
                    "opplysninger.ikkebesvart.avsnitt1",
                    "opplysninger.ikkebesvart.avsnitt2",
                    "opplysninger.informasjon.lenke",
                )
                skrivOkonomiskeOpplysningerModal(pdf, pdfUtils)
            }
        }

        // Inntekt
        pdf.skrivTekstBold(pdfUtils.getTekst("inntektbolk.tittel"))

        // Kan ikke være null i filformatet
        for (inntekt in okonomi.oversikt.inntekt) {
            pdf.skrivTekst(inntekt.tittel)
            if (inntekt.type == "studielanOgStipend") {
                pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(
                    pdf,
                    inntekt.netto,
                    "opplysninger.arbeid.student.utbetaling.label",
                )
            }
            if (inntekt.type == "jobb") {
                pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(
                    pdf,
                    inntekt.brutto,
                    "opplysninger.arbeid.jobb.bruttolonn.label",
                )
                pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(pdf, inntekt.netto, "opplysninger.arbeid.jobb.nettolonn.label")
            }
            if (inntekt.type == "barnebidrag") {
                pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(
                    pdf,
                    inntekt.netto,
                    "opplysninger.familiesituasjon.barnebidrag.mottar.mottar.label",
                )
            }
            pdf.addBlankLine()
        }
        val husbankenUtbetalinger: List<JsonOkonomiOpplysningUtbetaling> =
            hentUtbetalinger(okonomi, UTBETALING_HUSBANKEN)
        for (husbankenUtbetaling in husbankenUtbetalinger) {
            if (husbankenUtbetaling.kilde == JsonKilde.BRUKER) {
                pdf.skrivTekst(husbankenUtbetaling.tittel)
                pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(
                    pdf,
                    husbankenUtbetaling.netto,
                    "opplysninger.inntekt.bostotte.utbetaling.label",
                )
                pdf.addBlankLine()
            }
        }

        // Formue
        val sparingTyper: MutableList<String> = ArrayList(6)
        sparingTyper.add("verdipapirer")
        sparingTyper.add("brukskonto")
        sparingTyper.add("bsu")
        sparingTyper.add("livsforsikringssparedel")
        sparingTyper.add("sparekonto")
        sparingTyper.add("belop")
        for (formue in okonomi.oversikt.formue) {
            if (sparingTyper.contains(formue.type)) {
                pdf.skrivTekst(formue.tittel)
                pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(
                    pdf,
                    formue.belop,
                    "opplysninger.inntekt.bankinnskudd." + formue.type + ".saldo.label",
                )
                pdf.addBlankLine()
            }
        }

        // Utbetaling
        for (utbetaling in okonomi.opplysninger.utbetaling) {
            if (utbetaling.type != "skatteetaten" && utbetaling.type != "navytelse" && utbetaling.type != "husbanken") {
                pdf.skrivTekst(utbetaling.tittel)
                if (utbetaling.type == "sluttoppgjoer") {
                    pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(
                        pdf,
                        utbetaling.belop,
                        "opplysninger.arbeid.avsluttet.netto.label",
                    )
                } else {
                    pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(
                        pdf,
                        utbetaling.belop,
                        "opplysninger.inntekt.inntekter." + utbetaling.type + ".sum.label",
                    )
                }
                pdf.addBlankLine()
            }
        }

        // Utgift
        pdf.skrivTekstBold(pdfUtils.getTekst("utgifterbolk.tittel"))
        for (utgift in okonomi.opplysninger.utgift) {
            pdf.skrivTekst(utgift.tittel)
            if (utgifterBarnAlternativer.contains(utgift.type)) {
                pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(
                    pdf,
                    utgift.belop,
                    "opplysninger.utgifter.barn." + utgift.type + ".sisteregning.label",
                )
            }
            if (boutgiftAlternativer.contains(utgift.type)) {
                pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(
                    pdf,
                    utgift.belop,
                    "opplysninger.utgifter.boutgift." + utgift.type + ".sisteregning.label",
                )
            }
            if (utgift.type == "annen") {
                pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(
                    pdf,
                    utgift.belop,
                    "opplysninger.ekstrainfo.utgifter.utgift.label",
                )
            }
            pdf.addBlankLine()
        }
        for (utgift in okonomi.oversikt.utgift) {
            pdf.skrivTekst(utgift.tittel)
            if (utgifterBarnAlternativer.contains(utgift.type)) {
                pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(
                    pdf,
                    utgift.belop,
                    "opplysninger.utgifter.barn." + utgift.type + ".sistemnd.label",
                )
            }
            if (utgift.type == "barnebidrag") {
                pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(
                    pdf,
                    utgift.belop,
                    "opplysninger.familiesituasjon.barnebidrag.betaler.betaler.label",
                )
            }
            if (utgift.type == "husleie") {
                pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(
                    pdf,
                    utgift.belop,
                    "opplysninger.utgifter.boutgift.husleie.permnd.label",
                )
            }
            if (utgift.type == "boliglanAvdrag") {
                pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(
                    pdf,
                    utgift.belop,
                    "opplysninger.utgifter.boutgift.avdraglaan.avdrag.label",
                )
            }
            if (utgift.type == "boliglanRenter") {
                pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(
                    pdf,
                    utgift.belop,
                    "opplysninger.utgifter.boutgift.avdraglaan.renter.label",
                )
            }
            pdf.addBlankLine()
        }

        // Vedlegg
        pdf.skrivTekstBold(pdfUtils.getTekst("vedlegg.oppsummering.tittel"))
        if (vedleggSpesifikasjon != null && vedleggSpesifikasjon.vedlegg != null) {
            for (vedlegg in vedleggSpesifikasjon.vedlegg) {
                pdf.skrivTekst(pdfUtils.getTekst("vedlegg." + vedlegg.type + "." + vedlegg.tilleggsinfo + ".tittel"))
                if (vedlegg.filer != null) {
                    for (fil in vedlegg.filer) {
                        pdf.skrivTekst(" - " + fil.filnavn)
                    }
                }
                if (vedlegg.status != null && vedlegg.status == "VedleggKreves") {
                    pdf.skrivTekst(pdfUtils.getTekst("vedlegg.oppsummering.ikkelastetopp"))
                }
                if (vedlegg.status != null && vedlegg.status == "VedleggAlleredeSendt") {
                    pdf.skrivTekst(pdfUtils.getTekst("opplysninger.vedlegg.alleredelastetopp"))
                }
                pdf.addBlankLine()
            }
        }
    }

    private fun skrivOkonomiskeOpplysningerModal(pdf: PdfGenerator, pdfUtils: PdfUtils) {
        pdf.skrivTekst("Ved trykk på " + pdfUtils.getTekst("opplysninger.informasjon.lenke") + ":")
        pdf.skrivTekstBold(pdfUtils.getTekst("opplysninger.informasjon.modal.overskrift"))
        pdf.skrivTekstBold(pdfUtils.getTekst("opplysninger.informasjon.modal.bolk1.tittel"))
        pdf.skrivTekst(pdfUtils.getTekst("opplysninger.informasjon.modal.bolk1.avsnitt1"))
        pdf.skrivTekst(pdfUtils.getTekst("opplysninger.informasjon.modal.bolk1.avsnitt2"))
        pdf.skrivTekst(pdfUtils.getTekst("opplysninger.informasjon.modal.bolk1.avsnitt3"))
        pdf.skrivTekstBold(pdfUtils.getTekst("opplysninger.informasjon.modal.bolk2.tittel"))
        pdf.skrivTekst(pdfUtils.getTekst("opplysninger.informasjon.modal.bolk2.avsnitt1"))
        pdf.skrivTekstBold(pdfUtils.getTekst("opplysninger.informasjon.modal.bolk3.tittel"))
        pdf.skrivTekst(pdfUtils.getTekst("opplysninger.informasjon.modal.bolk3.avsnitt1"))
        pdf.addBlankLine()
    }
}
