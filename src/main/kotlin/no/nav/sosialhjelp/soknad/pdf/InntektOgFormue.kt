package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sosialhjelp.soknad.pdf.Utils.DATO_FORMAT
import no.nav.sosialhjelp.soknad.pdf.Utils.formaterDato
import no.nav.sosialhjelp.soknad.pdf.Utils.formaterDatoOgTidspunkt
import no.nav.sosialhjelp.soknad.pdf.Utils.hentBekreftelser
import no.nav.sosialhjelp.soknad.pdf.Utils.hentUtbetalinger

object InntektOgFormue {
    fun leggTilInntektOgFormue(
        pdf: PdfGenerator,
        pdfUtils: PdfUtils,
        okonomi: JsonOkonomi?,
        soknad: JsonSoknad,
        utvidetSoknad: Boolean,
    ) {
        pdf.skrivH4Bold(pdfUtils.getTekst("inntektbolk.tittel"))
        pdf.addBlankLine()

        if (okonomi == null) {
            return
        }

        val urisOnPage: MutableMap<String, String> = HashMap()

        // Skatt
        pdf.skrivTekstBold(pdfUtils.getTekst("utbetalinger.inntekt.skattbar.tittel"))
        val skattetatenSamtykke = hentBekreftelser(okonomi, SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE)
        val harSkattetatenSamtykke = if (skattetatenSamtykke.isEmpty()) false else skattetatenSamtykke[0].verdi
        if (!harSkattetatenSamtykke) {
            if (utvidetSoknad) {
                pdfUtils.skrivInfotekst(pdf, "utbetalinger.inntekt.skattbar.samtykke_sporsmal")
                pdf.skrivTekst(pdfUtils.getTekst("utbetalinger.inntekt.skattbar.samtykke_info"))
                pdf.addBlankLine()
                pdf.skrivTekst(pdfUtils.getTekst("utbetalinger.inntekt.skattbar.gi_samtykke"))
                pdf.addBlankLine()
            }
            pdf.skrivTekst(pdfUtils.getTekst("utbetalinger.inntekt.skattbar.mangler_samtykke"))
            pdf.addBlankLine()
        } else {
            if (utvidetSoknad) {
                pdfUtils.skrivInfotekst(pdf, "utbetalinger.inntekt.skattbar.samtykke_sporsmal")
                pdf.skrivTekst(pdfUtils.getTekst("utbetalinger.inntekt.skattbar.samtykke_info"))
                pdf.addBlankLine()
                pdf.skrivTekst(pdfUtils.getTekst("utbetalinger.inntekt.skattbar.gi_samtykke"))
                pdf.addBlankLine()
            }
            pdf.skrivTekst(pdfUtils.getTekst("utbetalinger.inntekt.skattbar.har_gitt_samtykke"))
            pdf.addBlankLine()
            if (skattetatenSamtykke.isNotEmpty()) {
                pdfUtils.skrivTekstMedGuard(
                    pdf,
                    formaterDatoOgTidspunkt(skattetatenSamtykke[0].bekreftelsesDato),
                    "utbetalinger.inntekt.skattbar.tidspunkt",
                )
            }
            val skatteetatenUtbetalinger = hentUtbetalinger(okonomi, "skatteetaten")
            if (soknad.driftsinformasjon != null && soknad.driftsinformasjon.inntektFraSkatteetatenFeilet) {
                pdf.skrivTekst("Kunne ikke hente utbetalinger fra Skatteetaten")
                pdf.addBlankLine()
            } else {
                if (skatteetatenUtbetalinger.isEmpty()) {
                    pdf.skrivTekst(pdfUtils.getTekst("utbetalinger.inntekt.skattbar.ingen"))
                    pdf.addBlankLine()
                }
            }
            if (skatteetatenUtbetalinger.isNotEmpty()) {
                pdf.skrivTekstBold(pdfUtils.getTekst("utbetalinger.skatt"))
                for (skatt in skatteetatenUtbetalinger) {
                    if (skatt.organisasjon != null && skatt.organisasjon.navn != null) {
                        pdfUtils.skrivTekstMedGuard(pdf, skatt.organisasjon.navn, "utbetalinger.utbetaling.arbeidsgivernavn.label")
                    }
                    if (skatt.periodeFom != null) {
                        pdfUtils.skrivTekstMedGuard(
                            pdf,
                            formaterDato(skatt.periodeFom, DATO_FORMAT),
                            "utbetalinger.utbetaling.periodeFom.label",
                        )
                    }
                    if (skatt.periodeTom != null) {
                        pdfUtils.skrivTekstMedGuard(
                            pdf,
                            formaterDato(skatt.periodeTom, DATO_FORMAT),
                            "utbetalinger.utbetaling.periodeTom.label",
                        )
                    }
                    if (skatt.brutto != null) {
                        pdfUtils.skrivTekstMedGuard(pdf, skatt.brutto.toString(), "utbetalinger.utbetaling.brutto.label")
                    }
                    if (skatt.skattetrekk != null) {
                        pdfUtils.skrivTekstMedGuard(pdf, skatt.skattetrekk.toString(), "utbetalinger.utbetaling.skattetrekk.label")
                    }
                    pdf.addBlankLine()
                }
                if (utvidetSoknad) {
                    pdfUtils.skrivInfotekst(pdf, "utbetalinger.infotekst.tekst.v2")
                    urisOnPage["Dine Utbetalinger"] = pdfUtils.getTekst("utbetalinger.infotekst.tekst.url") ?: ""
                }
            }
            if (utvidetSoknad) {
                pdfUtils.skrivInfotekst(pdf, "utbetalinger.inntekt.skattbar.ta_bort_samtykke")
            }
        }

        // NAV ytelser
        pdf.skrivTekstBold(pdfUtils.getTekst("navytelser.sporsmal"))
        if (utvidetSoknad) {
            pdfUtils.skrivInfotekst(pdf, "navytelser.infotekst.tekst")
        }
        if (soknad.driftsinformasjon != null && soknad.driftsinformasjon.utbetalingerFraNavFeilet) {
            pdf.skrivTekst("Kunne ikke hente utbetalinger fra NAV")
            pdf.addBlankLine()
        } else {
            val navytelseUtbetalinger = hentUtbetalinger(okonomi, "navytelse")
            if (navytelseUtbetalinger.isNotEmpty()) {
                pdf.skrivTekstBold(pdfUtils.getTekst("utbetalinger.sporsmal"))
                navytelseUtbetalinger.forEach { navytelse ->
                    pdfUtils.skrivTekstMedGuard(pdf, navytelse.tittel, "utbetalinger.utbetaling.type.label")
                    if (navytelse.netto != null) {
                        pdfUtils.skrivTekstMedGuard(pdf, navytelse.netto.toString(), "utbetalinger.utbetaling.netto.label")
                    }
                    if (navytelse.brutto != null) {
                        pdfUtils.skrivTekstMedGuard(pdf, navytelse.brutto.toString(), "utbetalinger.utbetaling.brutto.label")
                    }
                    if (navytelse.utbetalingsdato != null) {
                        pdfUtils.skrivTekstMedGuard(
                            pdf,
                            formaterDato(navytelse.utbetalingsdato, DATO_FORMAT),
                            "utbetalinger.utbetaling.erutbetalt.label",
                        )
                    }
                }
                if (utvidetSoknad) {
                    pdfUtils.skrivInfotekst(pdf, "utbetalinger.infotekst.tekst.v2")
                    urisOnPage["Dine Utbetalinger"] = pdfUtils.getTekst("utbetalinger.infotekst.tekst.url") ?: ""
                }
            } else {
                pdf.skrivTekst(pdfUtils.getTekst("utbetalinger.ingen.true"))
            }
            pdf.addBlankLine()
        }

        // Bostotte
        pdf.skrivTekstBold(pdfUtils.getTekst("inntekt.bostotte.overskrift"))
        pdf.skrivTekstBold(pdfUtils.getTekst("inntekt.bostotte.sporsmal.sporsmal"))

        val bostotteBekreftelser = hentBekreftelser(okonomi, SoknadJsonTyper.BOSTOTTE)
        var motarBostotte = false
        if (bostotteBekreftelser.isNotEmpty()) {
            val bostotteBekreftelse = bostotteBekreftelser[0]
            motarBostotte = bostotteBekreftelse.verdi
            pdf.skrivTekst(pdfUtils.getTekst("inntekt.bostotte.sporsmal." + bostotteBekreftelse.verdi))

            if (utvidetSoknad && !bostotteBekreftelse.verdi) {
                pdfUtils.skrivInfotekst(pdf, "informasjon.husbanken.bostotte.v2")
                urisOnPage["støtte fra Husbanken"] = pdfUtils.getTekst("informasjon.husbanken.bostotte.url") ?: ""
            }
        } else {
            pdfUtils.skrivIkkeUtfylt(pdf)
        }
        if (utvidetSoknad) {
            pdf.addBlankLine()
            val bostotteSvaralternativer: MutableList<String> = ArrayList(2)
            bostotteSvaralternativer.add("inntekt.bostotte.sporsmal.true")
            bostotteSvaralternativer.add("inntekt.bostotte.sporsmal.false")
            pdfUtils.skrivSvaralternativer(pdf, bostotteSvaralternativer)
        }
        pdf.addBlankLine()

        val hentingFraHusbankenHarFeilet = soknad.driftsinformasjon != null && soknad.driftsinformasjon.stotteFraHusbankenFeilet
        val bostotteSamtykke = hentBekreftelser(okonomi, SoknadJsonTyper.BOSTOTTE_SAMTYKKE)
        val harBostotteSamtykke = if (bostotteSamtykke.isEmpty()) false else bostotteSamtykke[0].verdi
        if (harBostotteSamtykke) {
            if (utvidetSoknad) {
                pdfUtils.skrivInfotekst(pdf, "inntekt.bostotte.gi_samtykke.overskrift")
                pdf.skrivTekst(pdfUtils.getTekst("inntekt.bostotte.gi_samtykke.tekst"))
                pdf.addBlankLine()
                pdf.skrivTekst(pdfUtils.getTekst("inntekt.bostotte.gi_samtykke"))
                pdf.addBlankLine()
            }
            pdf.skrivTekst(pdfUtils.getTekst("inntekt.bostotte.har_gitt_samtykke"))
            pdf.addBlankLine()
            if (bostotteSamtykke.isNotEmpty()) {
                pdfUtils.skrivTekstMedGuard(
                    pdf,
                    formaterDatoOgTidspunkt(bostotteSamtykke[0].bekreftelsesDato),
                    "inntekt.bostotte.tidspunkt",
                )
            }
            if (hentingFraHusbankenHarFeilet) {
                pdfUtils.skrivInfotekst(pdf, "informasjon.husbanken.bostotte.nedlasting_feilet")
            }
        } else {
            if (utvidetSoknad) {
                pdfUtils.skrivInfotekst(pdf, "inntekt.bostotte.gi_samtykke.overskrift")
                pdf.skrivTekst(pdfUtils.getTekst("inntekt.bostotte.gi_samtykke.tekst"))
                pdf.addBlankLine()
                pdf.skrivTekst(pdfUtils.getTekst("inntekt.bostotte.gi_samtykke"))
                pdf.addBlankLine()
            }
            if (motarBostotte) {
                pdf.skrivTekst(pdfUtils.getTekst("inntekt.bostotte.mangler_samtykke"))
                pdf.addBlankLine()
            }
        }

        var harBostotteUtbetalinger = false
        val husbankenUtbetalinger = hentUtbetalinger(okonomi, SoknadJsonTyper.UTBETALING_HUSBANKEN)
        husbankenUtbetalinger.forEach { husbanken ->
            if (husbanken.kilde == JsonKilde.SYSTEM) {
                if (husbanken.mottaker != null) {
                    pdfUtils.skrivTekstMedGuard(pdf, husbanken.mottaker.value(), "inntekt.bostotte.utbetaling.mottaker")
                }
                pdfUtils.skrivTekstMedGuard(
                    pdf,
                    formaterDato(husbanken.utbetalingsdato, DATO_FORMAT),
                    "inntekt.bostotte.utbetaling.utbetalingsdato",
                )
                if (husbanken.netto != null) {
                    pdfUtils.skrivTekstMedGuard(pdf, husbanken.netto.toString(), "inntekt.bostotte.utbetaling.belop")
                }
                pdf.addBlankLine()
                harBostotteUtbetalinger = true
            }
        }

        var harBostotteSaker = false
        val bostotte = okonomi.opplysninger.bostotte
        if (bostotte != null && bostotte.saker != null) {
            bostotte.saker.forEach { bostotteSak ->
                pdf.skrivTekst(pdfUtils.getTekst("inntekt.bostotte.sak"))
                pdf.skrivTekst(formaterDato(bostotteSak.dato, DATO_FORMAT))
                pdfUtils.skrivTekstMedGuard(pdf, finnSaksStatus(bostotteSak), "inntekt.bostotte.sak.status")
                harBostotteSaker = true
            }
        }

        if (harBostotteSamtykke) {
            if (harBostotteSaker) {
                if (!harBostotteUtbetalinger) {
                    pdf.addBlankLine()
                    pdf.skrivTekst(pdfUtils.getTekst("inntekt.bostotte.utbetalingerIkkefunnet"))
                }
            } else {
                if (harBostotteUtbetalinger) {
                    pdf.skrivTekst(pdfUtils.getTekst("inntekt.bostotte.sakerIkkefunnet"))
                } else {
                    pdf.skrivTekst(pdfUtils.getTekst("inntekt.bostotte.ikkefunnet"))
                }
            }
            pdf.addBlankLine()
            if (utvidetSoknad) {
                pdfUtils.skrivInfotekst(pdf, "inntekt.bostotte.husbanken.lenkeText")
                pdfUtils.skrivInfotekst(pdf, "inntekt.bostotte.ta_bort_samtykke")
                pdfUtils.getTekst("inntekt.bostotte.husbanken.lenkeText")?.let {
                    urisOnPage[it] = pdfUtils.getTekst("inntekt.bostotte.husbanken.url") ?: ""
                }
                pdf.addBlankLine()
            }
        }

        // Student
        if (soknad.data != null && soknad.data.utdanning != null && soknad.data.utdanning.erStudent != null && soknad.data.utdanning.erStudent) {
            pdf.skrivTekstBold(pdfUtils.getTekst("inntekt.studielan.sporsmal"))

            val studielanOgStipendBekreftelser = hentBekreftelser(okonomi, "studielanOgStipend")
            if (studielanOgStipendBekreftelser.isNotEmpty()) {
                val studielanOgStipendBekreftelse = studielanOgStipendBekreftelser[0]
                pdf.skrivTekst(pdfUtils.getTekst("inntekt.studielan." + studielanOgStipendBekreftelse.verdi))

                if (utvidetSoknad && !studielanOgStipendBekreftelse.verdi) {
                    pdf.skrivTekstBold(pdfUtils.getTekst("infotekst.oppsummering.tittel"))
                    pdf.skrivTekst(pdfUtils.getTekst("informasjon.student.studielan.tittel"))
                    pdf.skrivTekst(pdfUtils.getTekst("informasjon.student.studielan.1.v2"))
                    pdf.skrivTekst(pdfUtils.getTekst("informasjon.student.studielan.2"))
                    urisOnPage["lanekassen.no"] = pdfUtils.getTekst("informasjon.student.studielan.url") ?: ""
                }
            } else {
                pdfUtils.skrivIkkeUtfylt(pdf)
            }
            if (utvidetSoknad) {
                val studentSvaralternativer: MutableList<String> = ArrayList(2)
                studentSvaralternativer.add("inntekt.studielan.true")
                studentSvaralternativer.add("inntekt.studielan.false")
                pdfUtils.skrivSvaralternativer(pdf, studentSvaralternativer)
            }
            pdf.addBlankLine()
        }

        // Eierandeler
        pdf.skrivTekstBold(pdfUtils.getTekst("inntekt.eierandeler.sporsmal"))
        if (utvidetSoknad) {
            pdfUtils.skrivHjelpetest(pdf, "inntekt.eierandeler.hjelpetekst.tekst")
        }
        val verdiBekreftelser = hentBekreftelser(okonomi, "verdi")
        if (verdiBekreftelser.isNotEmpty()) {
            val verdiBekreftelse = verdiBekreftelser[0]
            pdf.skrivTekst(pdfUtils.getTekst("inntekt.eierandeler." + verdiBekreftelse.verdi))
            val verdierAlternativer: MutableList<String> = ArrayList(5)
            verdierAlternativer.add("bolig")
            verdierAlternativer.add("campingvogn")
            verdierAlternativer.add("kjoretoy")
            verdierAlternativer.add("fritidseiendom")
            verdierAlternativer.add("annet")

            if (verdiBekreftelse.verdi) {
                pdf.addBlankLine()
                pdf.skrivTekstBold(pdfUtils.getTekst("inntekt.eierandeler.true.type.sporsmal"))
                okonomi.oversikt.formue.forEach { formue ->
                    if (verdierAlternativer.contains(formue.type)) {
                        pdf.skrivTekst(formue.tittel)
                        if (formue.type == "annet") {
                            pdf.addBlankLine()
                            pdf.skrivTekstBold(pdfUtils.getTekst("inntekt.eierandeler.true.type.annet.true.beskrivelse.label"))
                            if (okonomi.opplysninger.beskrivelseAvAnnet != null && okonomi.opplysninger.beskrivelseAvAnnet.verdi != null) {
                                pdf.skrivTekst(okonomi.opplysninger.beskrivelseAvAnnet.verdi)
                            } else {
                                pdfUtils.skrivIkkeUtfylt(pdf)
                            }
                        }
                    }
                }
            }
        } else {
            pdfUtils.skrivIkkeUtfylt(pdf)
        }
        if (utvidetSoknad) {
            val verdiSvaralternativer: MutableList<String> = ArrayList(2)
            verdiSvaralternativer.add("inntekt.eierandeler.true")
            verdiSvaralternativer.add("inntekt.eierandeler.false")
            pdfUtils.skrivSvaralternativer(pdf, verdiSvaralternativer)

            pdf.skrivTekst("Under " + pdfUtils.getTekst("inntekt.eierandeler.true") + ":")
            val verdiJaSvaralternativer: MutableList<String> = ArrayList(5)
            verdiJaSvaralternativer.add("inntekt.eierandeler.true.type.bolig")
            verdiJaSvaralternativer.add("inntekt.eierandeler.true.type.campingvogn")
            verdiJaSvaralternativer.add("inntekt.eierandeler.true.type.kjoretoy")
            verdiJaSvaralternativer.add("inntekt.eierandeler.true.type.fritidseiendom")
            verdiJaSvaralternativer.add("inntekt.eierandeler.true.type.annet")
            pdfUtils.skrivSvaralternativer(pdf, verdiJaSvaralternativer)
        }
        pdf.addBlankLine()

        // Bankinnskudd
        pdf.skrivTekstBold(pdfUtils.getTekst("inntekt.bankinnskudd.true.type.sporsmal"))
        if (utvidetSoknad) {
            pdfUtils.skrivHjelpetest(pdf, "inntekt.bankinnskudd.true.type.hjelpetekst.tekst")
        }
        val sparingBekreftelser = hentBekreftelser(okonomi, "sparing")
        if (sparingBekreftelser.isNotEmpty()) {
            val sparingBekreftelse = sparingBekreftelser[0]
            val sparingAlternativer: MutableList<String> = ArrayList(6)
            sparingAlternativer.add("brukskonto")
            sparingAlternativer.add("sparekonto")
            sparingAlternativer.add("bsu")
            sparingAlternativer.add("livsforsikringssparedel")
            sparingAlternativer.add("verdipapirer")
            sparingAlternativer.add("belop")

            if (sparingBekreftelse.verdi) {
                okonomi.oversikt.formue.forEach { formue ->
                    if (sparingAlternativer.contains(formue.type)) {
                        pdf.skrivTekst(formue.tittel)
                        if (formue.type == "belop") {
                            pdf.addBlankLine()
                            pdf.skrivTekstBold(pdfUtils.getTekst("inntekt.bankinnskudd.true.type.annet.true.beskrivelse.label"))
                            if (okonomi.opplysninger.beskrivelseAvAnnet != null && okonomi.opplysninger.beskrivelseAvAnnet.sparing != null) {
                                pdf.skrivTekst(okonomi.opplysninger.beskrivelseAvAnnet.sparing)
                            } else {
                                pdfUtils.skrivIkkeUtfylt(pdf)
                            }
                        }
                    }
                }
            } else {
                pdfUtils.skrivIkkeUtfylt(pdf)
            }
        } else {
            pdfUtils.skrivIkkeUtfylt(pdf)
        }
        if (utvidetSoknad) {
            val bankinnskuddSvaralternativer: MutableList<String> = ArrayList(6)
            bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.brukskonto")
            bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.sparekonto")
            bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.bsu")
            bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.livsforsikringssparedel")
            bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.verdipapirer")
            bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.annet")
            pdfUtils.skrivSvaralternativer(pdf, bankinnskuddSvaralternativer)
        }
        pdf.addBlankLine()

        // Inntekter
        pdf.skrivTekstBold(pdfUtils.getTekst("inntekt.inntekter.sporsmal"))
        if (utvidetSoknad) {
            pdfUtils.skrivHjelpetest(pdf, "inntekt.inntekter.hjelpetekst.tekst")
        }
        val utbetalingBekreftelser = hentBekreftelser(okonomi, "utbetaling")
        if (utbetalingBekreftelser.isNotEmpty()) {
            val utbetalingBekreftelse = utbetalingBekreftelser[0]
            pdf.skrivTekst(pdfUtils.getTekst("inntekt.inntekter." + utbetalingBekreftelse.verdi))
            val utbetalingAlternativer: MutableList<String> = ArrayList(4)
            utbetalingAlternativer.add("utbytte")
            utbetalingAlternativer.add("salg")
            utbetalingAlternativer.add("forsikring")
            utbetalingAlternativer.add("annen")

            if (utbetalingBekreftelse.verdi) {
                pdf.addBlankLine()
                pdf.skrivTekstBold(pdfUtils.getTekst("inntekt.inntekter.true.type.sporsmal"))
                okonomi.opplysninger.utbetaling.forEach { utbetaling ->
                    if (utbetalingAlternativer.contains(utbetaling.type)) {
                        pdf.skrivTekst(utbetaling.tittel)
                        if (utbetaling.type == "annen") {
                            pdf.addBlankLine()
                            pdf.skrivTekstBold(pdfUtils.getTekst("inntekt.inntekter.true.type.annen.true.beskrivelse.label"))
                            if (okonomi.opplysninger.beskrivelseAvAnnet != null && okonomi.opplysninger.beskrivelseAvAnnet.utbetaling != null) {
                                pdf.skrivTekst(okonomi.opplysninger.beskrivelseAvAnnet.utbetaling)
                            } else {
                                pdfUtils.skrivIkkeUtfylt(pdf)
                            }
                        }
                    }
                }
            }
        } else {
            pdfUtils.skrivIkkeUtfylt(pdf)
        }
        if (utvidetSoknad) {
            val inntektSvaralternativer: MutableList<String> = ArrayList(2)
            inntektSvaralternativer.add("inntekt.inntekter.true")
            inntektSvaralternativer.add("inntekt.inntekter.false")
            pdfUtils.skrivSvaralternativer(pdf, inntektSvaralternativer)
            pdf.skrivTekst("Under " + pdfUtils.getTekst("inntekt.inntekter.true") + ":")

            val inntektJaSvaralternativer: MutableList<String> = ArrayList(4)
            inntektJaSvaralternativer.add("inntekt.inntekter.true.type.utbytte")
            inntektJaSvaralternativer.add("inntekt.inntekter.true.type.salg")
            inntektJaSvaralternativer.add("inntekt.inntekter.true.type.forsikring")
            inntektJaSvaralternativer.add("inntekt.inntekter.true.type.annet")
            pdfUtils.skrivSvaralternativer(pdf, inntektJaSvaralternativer)
        }
        pdf.addBlankLine()

        if (urisOnPage.isNotEmpty()) {
            pdfUtils.addLinks(pdf, urisOnPage)
        }
    }

    private fun finnSaksStatus(sak: JsonBostotteSak): String {
        val status = sak.status ?: return ""
        return if (status.equals("VEDTATT", ignoreCase = true)) {
            if (sak.vedtaksstatus != null && sak.vedtaksstatus == JsonBostotteSak.Vedtaksstatus.INNVILGET) {
                return "Innvilget: ${sak.beskrivelse}"
            }
            if (sak.vedtaksstatus != null && sak.vedtaksstatus == JsonBostotteSak.Vedtaksstatus.AVVIST) {
                return "Avvist: ${sak.beskrivelse}"
            }
            if (sak.vedtaksstatus == null) {
                "Avslag: ${sak.beskrivelse}"
            } else {
                "Vedtatt"
            }
        } else {
            "Under behandling"
        }
    }
}
