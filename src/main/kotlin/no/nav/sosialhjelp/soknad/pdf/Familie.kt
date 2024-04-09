package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.pdf.Utils.DATO_FORMAT
import no.nav.sosialhjelp.soknad.pdf.Utils.formaterDato
import no.nav.sosialhjelp.soknad.pdf.Utils.getJsonNavnTekst

object Familie {
    fun leggTilFamilie(
        pdf: PdfGenerator,
        pdfUtils: PdfUtils,
        familie: JsonFamilie?,
        utvidetSoknad: Boolean
    ) {
        // Familie
        pdf.skrivH4Bold(pdfUtils.getTekst("familiebolk.tittel"))
        pdf.addBlankLine()
        pdf.skrivTekstBold(pdfUtils.getTekst("familie.sivilstatus.sporsmal"))
        if (familie != null) {
            // Sivilstatus
            val sivilstatus = familie.sivilstatus
            if (sivilstatus != null) {
                val kilde = sivilstatus.kilde

                // System
                if (kilde != null && kilde == JsonKilde.SYSTEM) {
                    val status = sivilstatus.status
                    if (status == JsonSivilstatus.Status.GIFT) {
                        if (utvidetSoknad) {
                            pdf.skrivTekst(pdfUtils.getTekst("system.familie.sivilstatus"))
                            if (sivilstatus.ektefelleHarDiskresjonskode != null && !sivilstatus.ektefelleHarDiskresjonskode) {
                                pdf.skrivTekst(pdfUtils.getTekst("system.familie.sivilstatus.label"))
                            }
                        } else {
                            pdf.skrivTekst(pdfUtils.getTekst("familie.sivilstatus.$status"))
                        }
                        pdf.addBlankLine()

                        if (sivilstatus.ektefelleHarDiskresjonskode != null && sivilstatus.ektefelleHarDiskresjonskode) {
                            pdf.skrivTekstBold(pdfUtils.getTekst("system.familie.sivilstatus.ikkeTilgang.label"))
                            pdf.skrivTekst("Ektefelle/partner har diskresjonskode")
                        } else {
                            val ektefelle = sivilstatus.ektefelle
                            if (ektefelle != null) {
                                if (!utvidetSoknad) {
                                    pdf.skrivTekstBold(pdfUtils.getTekst("system.familie.sivilstatus.infotekst"))
                                }
                                pdfUtils.skrivTekstMedGuard(pdf, getJsonNavnTekst(ektefelle.navn), "system.familie.sivilstatus.gift.ektefelle.navn")
                                pdfUtils.skrivTekstMedGuard(pdf, formaterDato(ektefelle.fodselsdato, DATO_FORMAT), "system.familie.sivilstatus.gift.ektefelle.fodselsdato")

                                sivilstatus.folkeregistrertMedEktefelle?.let {
                                    val folkeregistrertTekst = pdfUtils.getTekst("system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen.$it")
                                    pdfUtils.skrivTekstMedGuard(pdf, folkeregistrertTekst, "system.familie.sivilstatus.gift.ektefelle.folkereg")
                                }
                            }
                        }
                    }
                }

                // Bruker
                if (kilde != null && kilde == JsonKilde.BRUKER) {
                    val status = sivilstatus.status
                    if (status != null) {
                        pdf.skrivTekst(pdfUtils.getTekst("familie.sivilstatus.$status"))
                        pdf.addBlankLine()

                        if (utvidetSoknad) {
                            val sivilstatusSvaralternativer: MutableList<String> = ArrayList(5)
                            sivilstatusSvaralternativer.add("familie.sivilstatus.gift")
                            sivilstatusSvaralternativer.add("familie.sivilstatus.samboer")
                            sivilstatusSvaralternativer.add("familie.sivilstatus.separert")
                            sivilstatusSvaralternativer.add("familie.sivilstatus.skilt")
                            sivilstatusSvaralternativer.add("familie.sivilstatus.ugift")
                            pdfUtils.skrivSvaralternativer(pdf, sivilstatusSvaralternativer)
                        }

                        val ektefelle = sivilstatus.ektefelle
                        if (ektefelle != null && status == JsonSivilstatus.Status.GIFT) {
                            pdf.skrivTekstBold(pdfUtils.getTekst("familie.sivilstatus.gift.ektefelle.sporsmal"))

                            pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(pdf, ektefelle.navn.fornavn, "familie.sivilstatus.gift.ektefelle.fornavn.label")
                            pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(pdf, ektefelle.navn.mellomnavn, "familie.sivilstatus.gift.ektefelle.mellomnavn.label")
                            pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(pdf, ektefelle.navn.etternavn, "familie.sivilstatus.gift.ektefelle.etternavn.label")

                            ektefelle.fodselsdato
                                ?.let { pdfUtils.skrivTekstMedGuard(pdf, formaterDato(it, DATO_FORMAT), "familie.sivilstatus.gift.ektefelle.fnr.label") }
                                ?: pdfUtils.skrivIkkeUtfyltMedGuard(pdf, "familie.sivilstatus.gift.ektefelle.fnr.label")

                            if (ektefelle.personIdentifikator != null && ektefelle.personIdentifikator.length == 11) {
                                pdfUtils.skrivTekstMedGuard(pdf, ektefelle.personIdentifikator.substring(6, 11), "familie.sivilstatus.gift.ektefelle.pnr.label")
                            } else {
                                pdfUtils.skrivIkkeUtfyltMedGuard(pdf, "familie.sivilstatus.gift.ektefelle.pnr.label")
                            }

                            pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(pdf, ektefelle.personIdentifikator, "personalia.fnr")

                            sivilstatus.borSammenMed
                                ?.let { pdfUtils.skrivTekstMedGuard(pdf, pdfUtils.getTekst("familie.sivilstatus.gift.ektefelle.borsammen.$it"), "familie.sivilstatus.gift.ektefelle.borsammen.sporsmal") }
                                ?: pdfUtils.skrivIkkeUtfyltMedGuard(pdf, "familie.sivilstatus.gift.ektefelle.borsammen.sporsmal")

                            if (utvidetSoknad) {
                                val borSammenSvaralternativer: MutableList<String> = ArrayList(2)
                                borSammenSvaralternativer.add("familie.sivilstatus.gift.borsammen.true")
                                borSammenSvaralternativer.add("familie.sivilstatus.gift.borsammen.false")
                                pdfUtils.skrivSvaralternativer(pdf, borSammenSvaralternativer)
                            }
                        }
                    } else {
                        pdfUtils.skrivIkkeUtfylt(pdf)
                    }
                }
                if (utvidetSoknad) {
                    val status = sivilstatus.status
                    if (status != null && status.toString() == "gift" && sivilstatus.ektefelleHarDiskresjonskode != null && !sivilstatus.ektefelleHarDiskresjonskode) {
                        pdf.addBlankLine()
                        pdf.skrivTekstBold(pdfUtils.getTekst("system.familie.sivilstatus.informasjonspanel.tittel"))
                        pdf.skrivTekst(pdfUtils.getTekst("system.familie.sivilstatus.informasjonspanel.tekst"))
                    }
                }
            } else {
                pdfUtils.skrivIkkeUtfylt(pdf)
            }

            pdf.addBlankLine()
            pdf.skrivTekstBold(pdfUtils.getTekst("familierelasjon.faktum.sporsmal"))

            // Forsørgerplikt
            val forsorgerplikt = familie.forsorgerplikt
            if (forsorgerplikt != null && forsorgerplikt.harForsorgerplikt != null && java.lang.Boolean.TRUE == forsorgerplikt.harForsorgerplikt.verdi) {
                if (utvidetSoknad) {
                    pdf.skrivTekst(pdfUtils.getTekst("familierelasjon.ingress_folkeregisteret"))
                    val antallBarnFraFolkeregisteret = forsorgerplikt.ansvar.count { it.barn.kilde == JsonKilde.SYSTEM }
                    pdf.skrivTekst("${pdfUtils.getTekst("familierelasjon.ingress_forsorger")} $antallBarnFraFolkeregisteret barn under 18år")
                }

                // TODO: Finnes ikke i handlebarkode?
                // pdf.skrivTekstBold(pdfUtils.getTekst("familie.barn.true.barn.sporsmal"));
                // pdf.addBlankLine();

                val listeOverAnsvar = forsorgerplikt.ansvar
                leggTilBarn(pdf, pdfUtils, utvidetSoknad, listeOverAnsvar)

                // Mottar eller betaler du barnebidrag for ett eller flere av barna?
                pdf.skrivTekstBold(pdfUtils.getTekst("familie.barn.true.barnebidrag.sporsmal"))
                if (listeOverAnsvar.size > 0) {
                    val barnebidrag = forsorgerplikt.barnebidrag
                    if (barnebidrag != null && barnebidrag.verdi != null) {
                        barnebidrag.verdi
                            ?.let { pdf.skrivTekst(pdfUtils.getTekst("familie.barn.true.barnebidrag.${it.value()}")) }
                    }
                } else {
                    pdfUtils.skrivIkkeUtfylt(pdf)
                }
                pdfUtils.skrivUtBarnebidragAlternativer(pdf, utvidetSoknad)
            } else {
                if (utvidetSoknad) {
                    pdf.skrivTekst(pdfUtils.getTekst("familierelasjon.ingen_registrerte_barn_tittel"))
                }
                pdf.skrivTekst(pdfUtils.getTekst("familierelasjon.ingen_registrerte_barn_tekst"))
            }
        } else {
            pdfUtils.skrivIkkeUtfylt(pdf)
        }
        pdf.addBlankLine()
    }

    private fun leggTilBarn(pdf: PdfGenerator, pdfUtils: PdfUtils, utvidetSoknad: Boolean, listeOverAnsvar: List<JsonAnsvar>) {
        listeOverAnsvar.forEach { ansvar ->
            val barn = ansvar.barn
            if (barn.kilde == JsonKilde.SYSTEM && barn.harDiskresjonskode == null || !barn.harDiskresjonskode) {
                // navn
                val navnPaBarnTekst = getJsonNavnTekst(barn.navn)
                pdfUtils.skrivTekstMedGuard(pdf, navnPaBarnTekst, "familie.barn.true.barn.navn.label")

                // Fødselsdato
                val fodselsdato = formaterDato(barn.fodselsdato, DATO_FORMAT)
                pdfUtils.skrivTekstMedGuard(pdf, fodselsdato, "kontakt.system.personalia.fodselsdato")

                // Samme folkeregistrerte adresse
                val erFolkeregistrertSammen = ansvar.erFolkeregistrertSammen
                if (erFolkeregistrertSammen != null) {
                    if (erFolkeregistrertSammen.verdi != null && erFolkeregistrertSammen.verdi) {
                        pdfUtils.skrivTekstMedGuard(pdf, "Ja", "familierelasjon.samme_folkeregistrerte_adresse")
                        leggTilDeltBosted(pdf, pdfUtils, ansvar, true, utvidetSoknad)
                    } else {
                        pdfUtils.skrivTekstMedGuard(pdf, "Nei", "familierelasjon.samme_folkeregistrerte_adresse")
                        leggTilDeltBosted(pdf, pdfUtils, ansvar, false, utvidetSoknad)
                    }
                }
                pdf.addBlankLine()
            }
        }
    }

    private fun leggTilDeltBosted(
        pdf: PdfGenerator,
        pdfUtils: PdfUtils,
        ansvar: JsonAnsvar,
        erFolkeregistrertSammenVerdi: Boolean,
        utvidetSoknad: Boolean
    ) {
        // Har barnet delt bosted
        if (erFolkeregistrertSammenVerdi) {
            ansvar.harDeltBosted
                ?.let { pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(pdf, pdfUtils.getTekst("system.familie.barn.true.barn.deltbosted." + it.verdi), "system.familie.barn.true.barn.deltbosted.sporsmal") }
                ?: pdfUtils.skrivIkkeUtfyltMedGuard(pdf, "system.familie.barn.true.barn.deltbosted.sporsmal")

            if (utvidetSoknad) {
                val deltBostedSvaralternativer: MutableList<String> = ArrayList(2)
                deltBostedSvaralternativer.add("system.familie.barn.true.barn.deltbosted.true")
                deltBostedSvaralternativer.add("system.familie.barn.true.barn.deltbosted.false")
                pdfUtils.skrivSvaralternativer(pdf, deltBostedSvaralternativer)
            }

            if (utvidetSoknad) {
                pdfUtils.skrivHjelpetest(pdf, "system.familie.barn.true.barn.deltbosted.hjelpetekst.tekst")
            }
        } else {
            if (ansvar.samvarsgrad != null && ansvar.samvarsgrad.verdi != null) {
                pdfUtils.skrivTekstMedGuard(pdf, ansvar.samvarsgrad.verdi.toString() + "%", "system.familie.barn.true.barn.grad.sporsmal")
            } else {
                pdfUtils.skrivIkkeUtfyltMedGuard(pdf, "system.familie.barn.true.barn.grad.sporsmal")
            }

            if (utvidetSoknad) {
                val svaralternativer: MutableList<String> = ArrayList(1)
                svaralternativer.add("system.familie.barn.true.barn.grad.pattern")
                pdfUtils.skrivSvaralternativer(pdf, svaralternativer)
            }
        }
    }
}
