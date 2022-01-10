package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag.Verdi
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle
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
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.harBrukerRegistrerteBarn
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.harSystemRegistrerteBarn
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.isNotNullOrEmtpy

class FamiliesituasjonSteg {

    fun get(jsonInternalSoknad: JsonInternalSoknad): Steg {
        val familie = jsonInternalSoknad.soknad.data.familie
        return Steg(
            stegNr = 4,
            tittel = "familiebolk.tittel",
            avsnitt = listOf(
                sivilstatusAvsnitt(familie.sivilstatus),
                forsorgerpliktAvsnitt(familie.forsorgerplikt)
            )
        )
    }

    private fun sivilstatusAvsnitt(sivilstatus: JsonSivilstatus?): Avsnitt {
        return Avsnitt(
            tittel = "system.familie.sivilstatus.sporsmal",
            sporsmal = sivilstatusSporsmal(sivilstatus)
        )
    }

    private fun sivilstatusSporsmal(sivilstatus: JsonSivilstatus?): List<Sporsmal> {
        val harUtfyltSivilstatusSporsmal = sivilstatus != null && sivilstatus.kilde != null
        val harSystemEktefelle =
            harUtfyltSivilstatusSporsmal && sivilstatus!!.kilde == JsonKilde.SYSTEM && sivilstatus.status == JsonSivilstatus.Status.GIFT
        val harSystemEktefelleMedAdressebeskyttelse =
            harSystemEktefelle && java.lang.Boolean.TRUE == sivilstatus!!.ektefelleHarDiskresjonskode
        val harBrukerUtfyltSivilstatus =
            harUtfyltSivilstatusSporsmal && !harSystemEktefelle && sivilstatus!!.kilde == JsonKilde.BRUKER
        val harBrukerUtfyltEktefelle =
            harBrukerUtfyltSivilstatus && sivilstatus!!.status == JsonSivilstatus.Status.GIFT && sivilstatus.ektefelle != null
        val sporsmal = ArrayList<Sporsmal>()
        if (!harUtfyltSivilstatusSporsmal) {
            sporsmal.add(brukerSivilstatusSporsmal(false, null))
        }
        if (harBrukerUtfyltSivilstatus && !harBrukerUtfyltEktefelle) {
            sporsmal.add(brukerSivilstatusSporsmal(true, sivilstatus!!.status))
        }
        if (harBrukerUtfyltEktefelle) {
            sporsmal.add(brukerRegistrertEktefelle(sivilstatus))
        }
        if (harSystemEktefelleMedAdressebeskyttelse) {
            sporsmal.add(systemEktefelleMedAdressebeskyttelseSporsmal())
        }
        if (harSystemEktefelle && !harSystemEktefelleMedAdressebeskyttelse) {
            sporsmal.add(systemEktefelleSporsmal(sivilstatus))
        }
        return sporsmal
    }

    private fun brukerSivilstatusSporsmal(erUtfylt: Boolean, status: JsonSivilstatus.Status?): Sporsmal {
        return Sporsmal(
            tittel = "familie.sivilstatus.sporsmal",
            erUtfylt = erUtfylt,
            felt = if (erUtfylt) listOf(
                Felt(
                    type = Type.CHECKBOX,
                    svar = createSvar(statusToTekstKey(status), SvarType.LOCALE_TEKST)
                )
            ) else null
        )
    }

    private fun statusToTekstKey(status: JsonSivilstatus.Status?): String {
        val key: String = when (status) {
            JsonSivilstatus.Status.ENKE -> "familie.sivilstatus.enke"
            JsonSivilstatus.Status.GIFT -> "familie.sivilstatus.gift"
            JsonSivilstatus.Status.SKILT -> "familie.sivilstatus.skilt"
            JsonSivilstatus.Status.SAMBOER -> "familie.sivilstatus.samboer"
            JsonSivilstatus.Status.SEPARERT -> "familie.sivilstatus.separert"
            JsonSivilstatus.Status.UGIFT -> "familie.sivilstatus.ugift"
            else -> "familie.sivilstatus.ugift"
        }
        return key
    }

    private fun brukerRegistrertEktefelle(sivilstatus: JsonSivilstatus?): Sporsmal {
        // todo: som dette eller som liste av sporsmal?
        val ektefelle = sivilstatus!!.ektefelle
        val erUtfylt = isNotNullOrEmtpy(ektefelle.navn.fornavn) &&
            isNotNullOrEmtpy(ektefelle.navn.etternavn) &&
            isNotNullOrEmtpy(ektefelle.fodselsdato) &&
            isNotNullOrEmtpy(ektefelle.personIdentifikator) && sivilstatus.borSammenMed != null
        val map = LinkedHashMap<String, Svar>()
        map["familie.sivilstatus.gift.ektefelle.navn.label"] =
            createSvar(fulltnavn(ektefelle.navn), SvarType.TEKST)
        map["familie.sivilstatus.gift.ektefelle.fnr.label"] =
            createSvar(ektefelle.fodselsdato, SvarType.DATO)
        map["familie.sivilstatus.gift.ektefelle.pnr.label"] =
            createSvar(personnummerFraFnr(ektefelle), SvarType.TEKST)
        map["familie.sivilstatus.gift.ektefelle.borsammen.sporsmal"] =
            createSvar(borSammenMedSvar(sivilstatus), SvarType.LOCALE_TEKST)
        return Sporsmal(
            tittel = "familie.sivilstatus.gift.ektefelle.sporsmal",
            erUtfylt = erUtfylt,
            felt = listOf(
                Felt(
                    type = Type.SYSTEMDATA_MAP, // selv om dette ikke er systemdata?!?
                    labelSvarMap = map
                )
            )
        )
    }

    private fun personnummerFraFnr(ektefelle: JsonEktefelle): String? {
        return if (ektefelle.personIdentifikator != null && ektefelle.personIdentifikator.length == 11) {
            ektefelle.personIdentifikator.substring(6, 11)
        } else ektefelle.personIdentifikator
    }

    private fun borSammenMedSvar(sivilstatus: JsonSivilstatus?): String? {
        return if (sivilstatus!!.borSammenMed != null) borSammenKey(sivilstatus) else null
    }

    private fun borSammenKey(sivilstatus: JsonSivilstatus?): String {
        return if (java.lang.Boolean.TRUE == sivilstatus!!.borSammenMed) "familie.sivilstatus.gift.ektefelle.borsammen.true" else "familie.sivilstatus.gift.ektefelle.borsammen.false"
    }

    private fun systemEktefelleMedAdressebeskyttelseSporsmal(): Sporsmal {
        return Sporsmal(
            tittel = "system.familie.sivilstatus",
            erUtfylt = true,
            felt = listOf(
                Felt(
                    type = Type.SYSTEMDATA,
                    svar = createSvar("system.familie.sivilstatus.ikkeTilgang.label", SvarType.LOCALE_TEKST)
                )
            )
        )
    }

    private fun systemEktefelleSporsmal(sivilstatus: JsonSivilstatus?): Sporsmal {
        val ektefelle = sivilstatus!!.ektefelle
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
            labelSvarMap["system.familie.sivilstatus.gift.ektefelle.folkereg"] = createSvar(
                if (java.lang.Boolean.TRUE == sivilstatus.folkeregistrertMedEktefelle) "system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen.true" else "system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen.false",
                SvarType.LOCALE_TEKST
            )
        }
        return Sporsmal(
            tittel = "system.familie.sivilstatus.infotekst",
            erUtfylt = true,
            felt = listOf(
                Felt(
                    type = Type.SYSTEMDATA_MAP,
                    labelSvarMap = labelSvarMap
                )
            )
        )
    }

    private fun forsorgerpliktAvsnitt(forsorgerplikt: JsonForsorgerplikt?): Avsnitt {
        return Avsnitt(
            tittel = "familierelasjon.faktum.sporsmal",
            sporsmal = forsorgerpliktSporsmal(forsorgerplikt)
        )
    }

    private fun forsorgerpliktSporsmal(forsorgerplikt: JsonForsorgerplikt?): List<Sporsmal> {
        val harSystemBarn = harSystemRegistrerteBarn(forsorgerplikt)
        val harBrukerBarn = harBrukerRegistrerteBarn(forsorgerplikt)
        val sporsmal = ArrayList<Sporsmal>()
        if (!harSystemBarn && !harBrukerBarn) {
            sporsmal.add(ingenRegistrerteBarnSporsmal())
        }
        if (harSystemBarn) {
            forsorgerplikt?.ansvar
                ?.filter { it.barn.kilde == JsonKilde.SYSTEM }
                ?.forEach { barn: JsonAnsvar ->
                    sporsmal.add(systemBarnSporsmal(barn))
                    if (java.lang.Boolean.TRUE == barn.erFolkeregistrertSammen.verdi) {
                        sporsmal.add(deltBostedSporsmal(barn))
                    }
                }
        }
        if (harBrukerBarn) {
            forsorgerplikt?.ansvar
                ?.filter { it.barn.kilde == JsonKilde.BRUKER }
                ?.forEach {
                    sporsmal.add(brukerBarnSporsmal(it))
                    if (it.borSammenMed != null && java.lang.Boolean.TRUE == it.borSammenMed.verdi) {
                        sporsmal.add(deltBostedSporsmal(it))
                    }
                }
        }
        if (harSystemBarn || harBrukerBarn) {
            sporsmal.add(barneBidragSporsmal(forsorgerplikt))
        }
        return sporsmal
    }

    private fun ingenRegistrerteBarnSporsmal(): Sporsmal {
        return Sporsmal(
            tittel = "familierelasjon.ingen_registrerte_barn_tittel",
            erUtfylt = true,
            felt = listOf(
                Felt(
                    type = Type.SYSTEMDATA,
                    svar = createSvar("familierelasjon.ingen_registrerte_barn_tekst", SvarType.LOCALE_TEKST)
                )
            )
        )
    }

    private fun systemBarnSporsmal(barn: JsonAnsvar): Sporsmal {
        return barnSporsmal(barn, erSystemRegistrert = true)
    }

    private fun brukerBarnSporsmal(barn: JsonAnsvar): Sporsmal {
        return barnSporsmal(barn, erSystemRegistrert = false)
    }

    private fun barnSporsmal(barn: JsonAnsvar, erSystemRegistrert: Boolean): Sporsmal {
        val labelSvarMap = LinkedHashMap<String, Svar>()
        if (barn.barn.navn != null) {
            labelSvarMap["familie.barn.true.barn.navn.label"] = createSvar(fulltnavn(barn.barn.navn), SvarType.TEKST)
        }
        if (barn.barn.fodselsdato != null) {
            labelSvarMap["familierelasjon.fodselsdato"] = createSvar(barn.barn.fodselsdato, SvarType.DATO)
        }
        if (erSystemRegistrert && barn.erFolkeregistrertSammen != null) {
            labelSvarMap["familierelasjon.samme_folkeregistrerte_adresse"] = createSvar(
                if (java.lang.Boolean.TRUE == barn.erFolkeregistrertSammen.verdi) "system.familie.barn.true.barn.folkeregistrertsammen.true" else "system.familie.barn.true.barn.folkeregistrertsammen.false",
                SvarType.LOCALE_TEKST
            )
        }
        if (!erSystemRegistrert && barn.borSammenMed != null) {
            labelSvarMap["familierelasjon.bor_sammen"] = createSvar(
                if (java.lang.Boolean.TRUE == barn.borSammenMed.verdi) "familie.barn.true.barn.borsammen.true" else "familie.barn.true.barn.borsammen.false",
                SvarType.LOCALE_TEKST
            )
        }
        return Sporsmal(
            tittel = if (erSystemRegistrert) "familie.barn.true.barn.sporsmal" else "familierelasjon.faktum.lagttil",
            erUtfylt = true,
            felt = listOf(
                Felt(
                    type = Type.SYSTEMDATA_MAP,
                    labelSvarMap = labelSvarMap
                )
            )
        )
    }

    private fun deltBostedSporsmal(barn: JsonAnsvar): Sporsmal {
        val harUtfyltDeltBostedSporsmal = barn.harDeltBosted != null && barn.harDeltBosted.verdi != null
        val harSvartJaDeltBosted = harUtfyltDeltBostedSporsmal && java.lang.Boolean.TRUE == barn.harDeltBosted.verdi
        return Sporsmal(
            tittel = "system.familie.barn.true.barn.deltbosted.sporsmal",
            erUtfylt = harUtfyltDeltBostedSporsmal,
            felt = if (harUtfyltDeltBostedSporsmal) StegUtils.booleanVerdiFelt(
                harSvartJaDeltBosted,
                "system.familie.barn.true.barn.deltbosted.true",
                "system.familie.barn.true.barn.deltbosted.false"
            ) else null
        )
    }

    private fun barneBidragSporsmal(forsorgerplikt: JsonForsorgerplikt?): Sporsmal {
        val erUtfylt = forsorgerplikt?.barnebidrag != null && forsorgerplikt.barnebidrag.verdi != null
        return Sporsmal(
            tittel = "familie.barn.true.barnebidrag.sporsmal",
            erUtfylt = erUtfylt,
            felt = if (erUtfylt) listOf(
                Felt(
                    type = Type.CHECKBOX,
                    svar = createSvar(verdiToTekstKey(forsorgerplikt!!.barnebidrag.verdi), SvarType.LOCALE_TEKST)
                )
            ) else null
        )
    }

    private fun verdiToTekstKey(verdi: Verdi): String {
        val key: String = when (verdi) {
            Verdi.MOTTAR -> "familie.barn.true.barnebidrag.mottar"
            Verdi.BETALER -> "familie.barn.true.barnebidrag.betaler"
            Verdi.BEGGE -> "familie.barn.true.barnebidrag.begge"
            Verdi.INGEN -> "familie.barn.true.barnebidrag.ingen"
            else -> "familie.barn.true.barnebidrag.ingen"
        }
        return key
    }
}
