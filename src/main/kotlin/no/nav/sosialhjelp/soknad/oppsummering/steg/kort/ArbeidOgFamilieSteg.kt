package no.nav.sosialhjelp.soknad.oppsummering.steg.kort

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.Steg
import no.nav.sosialhjelp.soknad.oppsummering.dto.Svar
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.fulltnavn
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.harSystemRegistrerteBarn

class ArbeidOgFamilieSteg {
    fun get(jsonInternalSoknad: JsonInternalSoknad): Steg {
        val familie = jsonInternalSoknad.soknad.data.familie
        val sivilstatus = familie.sivilstatus
        val barn = familie.forsorgerplikt
        val arbeid = jsonInternalSoknad.soknad.data.arbeid

        return Steg(
            stegNr = 3,
            tittel = "arbeidbolk.tittel",
            avsnitt =
                listOf(
                    Avsnitt(
                        tittel = "arbeidsforhold.sporsmal",
                        sporsmal = arbeidsforholdSporsmal(arbeid),
                    ),
                    Avsnitt(
                        tittel = "system.familie.sivilstatus.sporsmal",
                        sporsmal = sivilstatusSporsmal(sivilstatus),
                    ),
                    Avsnitt(
                        tittel = "familierelasjon.faktum.sporsmal",
                        sporsmal = forsorgerpliktSporsmal(barn),
                    ),
                ),
        )
    }

    private fun forsorgerpliktSporsmal(forsorgerplikt: JsonForsorgerplikt): List<Sporsmal> {
        val harSystemBarn = harSystemRegistrerteBarn(forsorgerplikt)
        val sporsmal = mutableListOf<Sporsmal>()
        if (!harSystemBarn) {
            sporsmal.add(ingenRegistrerteBarnSporsmal())
        }
        if (harSystemBarn) {
            forsorgerplikt.ansvar
                ?.filter { it.barn.kilde == JsonKilde.SYSTEM }
                ?.forEach { barn: JsonAnsvar ->
                    sporsmal.add(systemBarnSporsmal(barn))
                }
        }
        return sporsmal
    }

    private fun ingenRegistrerteBarnSporsmal(): Sporsmal =
        Sporsmal(
            tittel = "familierelasjon.ingen_registrerte_barn_tittel",
            erUtfylt = true,
            felt =
                listOf(
                    Felt(
                        type = Type.SYSTEMDATA,
                        svar = createSvar("familierelasjon.ingen_registrerte_barn_tekst", SvarType.LOCALE_TEKST),
                    ),
                ),
        )

    private fun systemBarnSporsmal(barn: JsonAnsvar): Sporsmal {
        val labelSvarMap = LinkedHashMap<String, Svar>()
        if (barn.barn.navn != null) {
            labelSvarMap["familie.barn.true.barn.navn.label"] = createSvar(fulltnavn(barn.barn.navn), SvarType.TEKST)
        }
        if (barn.barn.fodselsdato != null) {
            labelSvarMap["familierelasjon.fodselsdato"] = createSvar(barn.barn.fodselsdato, SvarType.DATO)
        }
        if (barn.erFolkeregistrertSammen != null) {
            labelSvarMap["familierelasjon.samme_folkeregistrerte_adresse"] =
                createSvar(
                    if (java.lang.Boolean.TRUE == barn.erFolkeregistrertSammen.verdi) "system.familie.barn.true.barn.folkeregistrertsammen.true" else "system.familie.barn.true.barn.folkeregistrertsammen.false",
                    SvarType.LOCALE_TEKST,
                )
        }
        return Sporsmal(
            tittel = "familie.barn.true.barn.sporsmal",
            erUtfylt = true,
            felt =
                listOf(
                    Felt(
                        type = Type.SYSTEMDATA_MAP,
                        labelSvarMap = labelSvarMap,
                    ),
                ),
        )
    }

    private fun sivilstatusSporsmal(sivilstatus: JsonSivilstatus?): List<Sporsmal> {
        if (sivilstatus == null) {
            return emptyList()
        }
        val harSystemEktefelle =
            sivilstatus.kilde == JsonKilde.SYSTEM && sivilstatus.status == JsonSivilstatus.Status.GIFT
        val harSystemEktefelleMedAdressebeskyttelse =
            harSystemEktefelle && java.lang.Boolean.TRUE == sivilstatus.ektefelleHarDiskresjonskode

        val sporsmal = mutableListOf<Sporsmal>()
        when {
            harSystemEktefelleMedAdressebeskyttelse -> {
                sporsmal.add(systemEktefelleMedAdressebeskyttelseSporsmal())
            }
            harSystemEktefelle -> {
                sporsmal.add(systemEktefelleSporsmal(sivilstatus))
            }
            else -> {
                sporsmal.add(
                    Sporsmal(
                        tittel = "system.familie.sivilstatus.infotekst",
                        erUtfylt = true,
                        felt =
                            listOf(
                                Felt(
                                    type = Type.SYSTEMDATA,
                                    svar = createSvar("system.familie.sivilstatus.empty", SvarType.LOCALE_TEKST),
                                ),
                            ),
                    ),
                )
            }
        }
        return sporsmal
    }

    private fun systemEktefelleMedAdressebeskyttelseSporsmal(): Sporsmal =
        Sporsmal(
            tittel = "system.familie.sivilstatus",
            erUtfylt = true,
            felt =
                listOf(
                    Felt(
                        type = Type.SYSTEMDATA,
                        svar = createSvar("system.familie.sivilstatus.ikkeTilgang.label", SvarType.LOCALE_TEKST),
                    ),
                ),
        )

    private fun systemEktefelleSporsmal(sivilstatus: JsonSivilstatus): Sporsmal {
        val ektefelle = sivilstatus.ektefelle
        val labelSvarMap = LinkedHashMap<String, Svar>()
        if (ektefelle.navn != null) {
            labelSvarMap["system.familie.sivilstatus.gift.ektefelle.navn"] =
                createSvar(fulltnavn(ektefelle.navn), SvarType.TEKST)
        }
        if (ektefelle.fodselsdato != null) {
            labelSvarMap["system.familie.sivilstatus.gift.ektefelle.fodselsdato"] =
                createSvar(ektefelle.fodselsdato, SvarType.DATO)
        }
        if (sivilstatus.folkeregistrertMedEktefelle != null) {
            labelSvarMap["system.familie.sivilstatus.gift.ektefelle.folkereg"] =
                createSvar(
                    if (java.lang.Boolean.TRUE == sivilstatus.folkeregistrertMedEktefelle) "system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen.true" else "system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen.false",
                    SvarType.LOCALE_TEKST,
                )
        }
        return Sporsmal(
            tittel = "system.familie.sivilstatus.infotekst",
            erUtfylt = true,
            felt =
                listOf(
                    Felt(
                        type = Type.SYSTEMDATA_MAP,
                        labelSvarMap = labelSvarMap,
                    ),
                ),
        )
    }

    private fun arbeidsforholdSporsmal(arbeid: JsonArbeid): List<Sporsmal> {
        val harArbeidsforhold = arbeid.forhold != null && arbeid.forhold.isNotEmpty()
        val sporsmal = mutableListOf<Sporsmal>()
        sporsmal.add(
            Sporsmal(
                tittel = if (harArbeidsforhold) "arbeidsforhold.infotekst" else "arbeidsforhold.ingen",
                erUtfylt = true,
                felt = if (harArbeidsforhold) arbeidsforholdFelter(arbeid.forhold) else null,
            ),
        )
        // TODO: Er denne med?
        //        val harKommentarTilArbeidsforhold =
//            arbeid.kommentarTilArbeidsforhold != null && arbeid.kommentarTilArbeidsforhold.verdi != null
//        if (harKommentarTilArbeidsforhold) {
//            sporsmal.add(
//                Sporsmal(
//                    tittel = "opplysninger.arbeidsituasjon.kommentarer.label",
//                    erUtfylt = true,
//                    felt = kommentarFelter(arbeid.kommentarTilArbeidsforhold),
//                ),
//            )
//        }
        return sporsmal
    }

    private fun arbeidsforholdFelter(arbeidsforholdList: List<JsonArbeidsforhold>): List<Felt> = arbeidsforholdList.map { it.toFelt() }

    private fun JsonArbeidsforhold.toFelt(): Felt {
        // arbeidsgiver, startet i jobben, (sluttet i jobben), stillingsprosent
        val labelSvarMap = LinkedHashMap<String, Svar>()
        if (arbeidsgivernavn != null) {
            labelSvarMap["arbeidsforhold.arbeidsgivernavn.label"] =
                createSvar(arbeidsgivernavn, SvarType.TEKST)
        }
        if (fom != null) {
            labelSvarMap["arbeidsforhold.fom.label"] = createSvar(fom, SvarType.DATO)
        }
        if (tom != null) {
            labelSvarMap["arbeidsforhold.tom.label"] = createSvar(tom, SvarType.DATO)
        }
        if (stillingsprosent != null) {
            labelSvarMap["arbeidsforhold.stillingsprosent.label"] =
                createSvar(stillingsprosent.toString(), SvarType.TEKST)
        }
        return Felt(
            type = Type.SYSTEMDATA_MAP,
            labelSvarMap = labelSvarMap,
        )
    }
}
