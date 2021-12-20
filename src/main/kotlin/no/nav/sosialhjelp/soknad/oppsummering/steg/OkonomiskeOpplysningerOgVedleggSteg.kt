package no.nav.sosialhjelp.soknad.oppsummering.steg

import com.nimbusds.oauth2.sdk.util.CollectionUtils
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BARNEBIDRAG
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_ANNET
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BRUKSKONTO
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BSU
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_LIVSFORSIKRING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_SPAREKONTO
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_VERDIPAPIRER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.JOBB
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.SLUTTOPPGJOER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.STUDIELAN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_NAVYTELSE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANDRE_UTGIFTER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BARN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BO
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARNEHAGE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_RENTER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_HUSLEIE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_OPPVARMING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_SFO
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_STROM
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.common.mapper.TitleKeyMapper
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.Steg
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.dto.Vedlegg
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.integerVerdiSporsmalMedTittel

class OkonomiskeOpplysningerOgVedleggSteg {
    fun get(jsonInternalSoknad: JsonInternalSoknad, opplastedeVedlegg: List<OpplastetVedlegg>): Steg {
        val okonomi = jsonInternalSoknad.soknad.data.okonomi
        val vedlegg = jsonInternalSoknad.vedlegg
        return Steg(
            stegNr = 8,
            tittel = "opplysningerbolk.tittel",
            avsnitt = okonomiOgVedleggAvsnitt(okonomi, vedlegg, opplastedeVedlegg)
        )
    }

    private fun okonomiOgVedleggAvsnitt(
        okonomi: JsonOkonomi,
        vedleggSpesifikasjon: JsonVedleggSpesifikasjon,
        opplastedeVedlegg: List<OpplastetVedlegg>
    ): List<Avsnitt> {
        val inntektAvsnitt = Avsnitt(
            tittel = "inntektbolk.tittel",
            sporsmal = inntekterSporsmal(okonomi)
        )
        val utgifterAvsnitt = Avsnitt(
            tittel = "utgifterbolk.tittel",
            sporsmal = utgifterSporsmal(okonomi)
        )
        val vedleggAvsnitt = Avsnitt(
            tittel = "vedlegg.oppsummering.tittel",
            sporsmal = vedleggSporsmal(vedleggSpesifikasjon, opplastedeVedlegg)
        )
        return listOf(inntektAvsnitt, utgifterAvsnitt, vedleggAvsnitt)
    }

    private fun inntekterSporsmal(okonomi: JsonOkonomi): List<Sporsmal> {
        val sporsmal = ArrayList<Sporsmal>()
        addInntekter(sporsmal, okonomi)
        addFormuer(sporsmal, okonomi)
        addUtbetalinger(sporsmal, okonomi)
        return sporsmal
    }

    private fun addInntekter(sporsmal: ArrayList<Sporsmal>, okonomi: JsonOkonomi) {
        val inntekter = okonomi.oversikt.inntekt
        if (CollectionUtils.isNotEmpty(inntekter)) {
            // Lønnsinntekt
            inntekter
                .filter { JOBB == it!!.type }
                .forEach {
                    sporsmal.add(
                        integerVerdiSporsmalMedTittel(
                            getTitleKey(it!!.type), "opplysninger.arbeid.jobb.bruttolonn.label", it.brutto
                        )
                    )
                    sporsmal.add(
                        integerVerdiSporsmalMedTittel(
                            getTitleKey(it.type), "opplysninger.arbeid.jobb.nettolonn.label", it.netto
                        )
                    )
                }

            // Studielån
            inntekter
                .filter { STUDIELAN == it!!.type }
                .forEach {
                    sporsmal.add(
                        integerVerdiSporsmalMedTittel(
                            getTitleKey(it!!.type), "opplysninger.arbeid.student.utbetaling.label", it.netto
                        )
                    )
                }

            // Barnebidrag
            inntekter
                .filter { BARNEBIDRAG == it!!.type }
                .forEach {
                    sporsmal.add(
                        integerVerdiSporsmalMedTittel(
                            "json.okonomi.opplysninger.familiesituasjon.barnebidrag.mottar",
                            "opplysninger.familiesituasjon.barnebidrag.mottar.mottar.label",
                            it!!.netto
                        )
                    )
                }

            // Husbanken utbetaling, kilde bruker
            inntekter
                .filter { UTBETALING_HUSBANKEN == it!!.type && JsonKilde.BRUKER == it.kilde }
                .forEach {
                    sporsmal.add(
                        integerVerdiSporsmalMedTittel(
                            "json.okonomi.opplysninger.inntekt.bostotte",
                            "opplysninger.inntekt.bostotte.utbetaling.label",
                            it!!.netto
                        )
                    )
                }
        }
    }

    private fun addFormuer(sporsmal: ArrayList<Sporsmal>, okonomi: JsonOkonomi) {
        val formuer = okonomi.oversikt.formue
        if (CollectionUtils.isNotEmpty(formuer)) {
            formuer
                .filter { formueTyper.contains(it!!.type) }
                .forEach {
                    sporsmal.add(
                        integerVerdiSporsmalMedTittel(
                            getTitleKey(it!!.type), "opplysninger.inntekt.bankinnskudd.${it.type}.saldo.label", it.belop
                        )
                    )
                }
        }
    }

    private fun addUtbetalinger(sporsmal: ArrayList<Sporsmal>, okonomi: JsonOkonomi) {
        val utbetalinger = okonomi.opplysninger.utbetaling
        if (CollectionUtils.isNotEmpty(utbetalinger)) {
            val filteredUtbetalinger = utbetalinger
                .filter { !systemdataUtbetalingTyper.contains(it!!.type) }

            filteredUtbetalinger
                .filter { SLUTTOPPGJOER == it!!.type }
                .forEach {
                    sporsmal.add(
                        integerVerdiSporsmalMedTittel(
                            getTitleKey(it!!.type), "opplysninger.arbeid.avsluttet.netto.label", it.belop
                        )
                    )
                }
            filteredUtbetalinger
                .filter { SLUTTOPPGJOER != it!!.type }
                .forEach {
                    sporsmal.add(
                        integerVerdiSporsmalMedTittel(
                            getTitleKey(it.type), "opplysninger.inntekt.inntekter.${it.type}.sum.label", it.belop
                        )
                    )
                }
        }
    }

    private fun utgifterSporsmal(okonomi: JsonOkonomi): List<Sporsmal> {
        val sporsmal = ArrayList<Sporsmal>()
        val opplysningUtgifter = okonomi.opplysninger.utgift
        if (CollectionUtils.isNotEmpty(opplysningUtgifter)) {
            opplysningUtgifter
                .filter { barneutgifter.contains(it.type) }
                .forEach {
                    sporsmal.add(
                        integerVerdiSporsmalMedTittel(
                            getTitleKey(it.type), "opplysninger.utgifter.barn.${it.type}.sisteregning.label", it.belop
                        )
                    )
                }
            opplysningUtgifter
                .filter { boutgifter.contains(it.type) }
                .forEach {
                    sporsmal.add(
                        integerVerdiSporsmalMedTittel(
                            getTitleKey(it.type), "opplysninger.utgifter.boutgift.${it.type}.sisteregning.label", it.belop
                        )
                    )
                }
            opplysningUtgifter
                .filter { UTGIFTER_ANDRE_UTGIFTER == it!!.type }
                .forEach {
                    sporsmal.add(
                        integerVerdiSporsmalMedTittel(
                            it.tittel, "opplysninger.ekstrainfo.utgifter.utgift.label", it.belop
                        )
                    )
                }
        }
        val oversiktUtgifter = okonomi.oversikt.utgift
        if (CollectionUtils.isNotEmpty(oversiktUtgifter)) {
            oversiktUtgifter
                .filter { barneutgifter.contains(it.type) }
                .forEach {
                    sporsmal.add(
                        integerVerdiSporsmalMedTittel(
                            getTitleKey(it.type), "opplysninger.utgifter.barn.${it.type}.sistemnd.label", it.belop
                        )
                    )
                }
            oversiktUtgifter
                .filter { BARNEBIDRAG == it.type }
                .forEach {
                    sporsmal.add(
                        integerVerdiSporsmalMedTittel(
                            getTitleKey(it.type), "opplysninger.familiesituasjon.barnebidrag.betaler.betaler.label", it.belop
                        )
                    )
                }
            oversiktUtgifter
                .filter { UTGIFTER_HUSLEIE == it.type }
                .forEach {
                    sporsmal.add(
                        integerVerdiSporsmalMedTittel(
                            getTitleKey(it.type), "opplysninger.utgifter.boutgift.husleie.permnd.label", it.belop
                        )
                    )
                }
            oversiktUtgifter
                .filter { UTGIFTER_BOLIGLAN_AVDRAG == it.type }
                .forEach {
                    sporsmal.add(
                        integerVerdiSporsmalMedTittel(
                            getTitleKey(it.type), "opplysninger.utgifter.boutgift.avdraglaan.avdrag.label", it.belop
                        )
                    )
                }
            oversiktUtgifter
                .filter { UTGIFTER_BOLIGLAN_RENTER == it.type }
                .forEach {
                    sporsmal.add(
                        integerVerdiSporsmalMedTittel(
                            getTitleKey(it!!.type), "opplysninger.utgifter.boutgift.avdraglaan.renter.label", it.belop
                        )
                    )
                }
        }
        return sporsmal
    }

    private fun vedleggSporsmal(
        vedleggSpesifikasjon: JsonVedleggSpesifikasjon,
        opplastedeVedlegg: List<OpplastetVedlegg>
    ): List<Sporsmal> {
        return vedleggSpesifikasjon.vedlegg
            .map {
                Sporsmal(
                    tittel = getTittelFrom(it.type, it.tilleggsinfo),
                    erUtfylt = true,
                    felt = vedleggFelter(it, opplastedeVedlegg)
                )
            }
    }

    private fun getTittelFrom(type: String, tilleggsinfo: String): String {
        return "vedlegg.$type.$tilleggsinfo.tittel"
    }

    private fun vedleggFelter(vedlegg: JsonVedlegg, opplastedeVedlegg: List<OpplastetVedlegg>): List<Felt> {
        val felt: Felt = if ("LastetOpp" == vedlegg.status && CollectionUtils.isNotEmpty(vedlegg.filer)) {
            Felt(
                type = Type.VEDLEGG,
                vedlegg = vedlegg.filer.map { Vedlegg(it.filnavn, getUuidFraOpplastetVedlegg(it, opplastedeVedlegg)) }
            )
        } else {
            Felt(
                type = Type.TEKST,
                svar = StegUtils.createSvar(
                    if ("VedleggAlleredeSendt" == vedlegg.status) "opplysninger.vedlegg.alleredelastetopp" else "vedlegg.oppsummering.ikkelastetopp",
                    SvarType.LOCALE_TEKST
                )
            )
        }
        return listOf(felt)
    }

    private fun getUuidFraOpplastetVedlegg(fil: JsonFiler, opplastedeVedlegg: List<OpplastetVedlegg>): String? {
        return opplastedeVedlegg.firstOrNull { fil.filnavn == it.filnavn }?.uuid
    }

    private fun getTitleKey(type: String): String {
        return "json.okonomi." + TitleKeyMapper.soknadTypeToTitleKey[type]
    }

    companion object {
        private val formueTyper = listOf(
            FORMUE_VERDIPAPIRER,
            FORMUE_BRUKSKONTO,
            FORMUE_BSU,
            FORMUE_LIVSFORSIKRING,
            FORMUE_SPAREKONTO,
            FORMUE_ANNET
        )
        private val systemdataUtbetalingTyper = listOf(
            UTBETALING_NAVYTELSE,
            UTBETALING_SKATTEETATEN,
            UTBETALING_HUSBANKEN
        )
        private val barneutgifter = listOf(
            UTGIFTER_BARNEHAGE,
            UTGIFTER_SFO,
            UTGIFTER_BARN_FRITIDSAKTIVITETER,
            UTGIFTER_BARN_TANNREGULERING,
            UTGIFTER_ANNET_BARN
        )
        private val boutgifter = listOf(
            UTGIFTER_HUSLEIE,
            UTGIFTER_STROM,
            UTGIFTER_KOMMUNAL_AVGIFT,
            UTGIFTER_OPPVARMING,
            UTGIFTER_BOLIGLAN_AVDRAG,
            UTGIFTER_BOLIGLAN_RENTER,
            UTGIFTER_ANNET_BO
        )
    }
}
