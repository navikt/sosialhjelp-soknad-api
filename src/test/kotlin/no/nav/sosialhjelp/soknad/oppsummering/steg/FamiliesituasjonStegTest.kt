package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonErFolkeregistrertSammen
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarDeltBosted
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar
import no.nav.sosialhjelp.soknad.v2.createValidEmptyJsonInternalSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.Boolean.TRUE

internal class FamiliesituasjonStegTest {
    private val steg = FamiliesituasjonSteg

    @Test
    fun ikkeUtfyltSivilstatus() {
        val soknad = createSoknad(null, null)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)
        assertThat(res.avsnitt[0].sporsmal).hasSize(1)

        val sivilstatusSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(sivilstatusSporsmal.erUtfylt).isFalse
    }

    @Test
    fun brukerUtfyltSivilstatus_ulikGift() {
        val ugift = JsonSivilstatus(JsonKilde.BRUKER, JsonSivilstatus.Status.UGIFT)
        val soknad = createSoknad(ugift, null)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)
        assertThat(res.avsnitt[0].sporsmal).hasSize(1)

        val sivilstatusSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(sivilstatusSporsmal.erUtfylt).isTrue
        assertThat(sivilstatusSporsmal.felt).hasSize(1)
        validateFeltMedSvar(sivilstatusSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "familie.sivilstatus.ugift")
    }

    @Test
    fun brukerUtfyltSivilstatus_ektefelle_manglerFelter() {
        val gift = JsonSivilstatus(JsonKilde.BRUKER, JsonSivilstatus.Status.GIFT, JsonEktefelle(JsonNavn("Gul", "", "Knapp")), borSammenMed = true)
        val soknad = createSoknad(gift, null)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)

        val sivilstatusSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(sivilstatusSporsmal.erUtfylt).isFalse // fnr og pnr mangler
        assertThat(sivilstatusSporsmal.felt).hasSize(1)
        assertThat(sivilstatusSporsmal.felt!![0].type).isEqualTo(Type.SYSTEMDATA_MAP)

        val labelSvarMap = sivilstatusSporsmal.felt[0].labelSvarMap
        assertThat(labelSvarMap).hasSize(4)
        assertThat(labelSvarMap!!["familie.sivilstatus.gift.ektefelle.navn.label"]!!.value).isEqualTo("Gul Knapp")
        assertThat(labelSvarMap["familie.sivilstatus.gift.ektefelle.fnr.label"]!!.value).isNull()
        assertThat(labelSvarMap["familie.sivilstatus.gift.ektefelle.pnr.label"]!!.value).isNull()
        assertThat(
            labelSvarMap["familie.sivilstatus.gift.ektefelle.borsammen.sporsmal"]!!.value,
        ).isEqualTo("familie.sivilstatus.gift.ektefelle.borsammen.true")
    }

    @Test
    fun systemSivilstatus_ektefelleMedAdressebeskyttelse() {
        val giftMedAdressebeskyttelse = JsonSivilstatus(JsonKilde.SYSTEM, JsonSivilstatus.Status.GIFT, ektefelleHarDiskresjonskode = true)
        val soknad = createSoknad(giftMedAdressebeskyttelse, null)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)
        assertThat(res.avsnitt[0].sporsmal).hasSize(1)

        val sivilstatusSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(sivilstatusSporsmal.erUtfylt).isTrue
        assertThat(sivilstatusSporsmal.felt).hasSize(1)
        validateFeltMedSvar(
            sivilstatusSporsmal.felt!![0],
            Type.SYSTEMDATA,
            SvarType.LOCALE_TEKST,
            "system.familie.sivilstatus.ikkeTilgang.label",
        )
    }

    @Test
    fun systemSivilstatus_ektefelle() {
        val giftMedAdressebeskyttelse = JsonSivilstatus(JsonKilde.SYSTEM, JsonSivilstatus.Status.GIFT, JsonEktefelle(JsonNavn("Gul", "", "Knapp"), "1999-12-31", "11111111111"), folkeregistrertMedEktefelle = true)
        val soknad = createSoknad(giftMedAdressebeskyttelse, null)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)
        assertThat(res.avsnitt[0].sporsmal).hasSize(1)

        val sivilstatusSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(sivilstatusSporsmal.erUtfylt).isTrue
        assertThat(sivilstatusSporsmal.felt).hasSize(1)
        assertThat(sivilstatusSporsmal.felt!![0].type).isEqualTo(Type.SYSTEMDATA_MAP)

        val labelSvarMap = sivilstatusSporsmal.felt[0].labelSvarMap
        assertThat(labelSvarMap).hasSize(3)
        assertThat(labelSvarMap!!["system.familie.sivilstatus.gift.ektefelle.navn"]!!.value).isEqualTo("Gul Knapp")
        assertThat(labelSvarMap["system.familie.sivilstatus.gift.ektefelle.fodselsdato"]!!.value).isEqualTo("1999-12-31")
        assertThat(
            labelSvarMap["system.familie.sivilstatus.gift.ektefelle.folkereg"]!!.value,
        ).isEqualTo("system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen.true")
    }

    @Test
    fun ingenBarn() {
        val forsorgerplikt = JsonForsorgerplikt()
        val soknad = createSoknad(null, forsorgerplikt)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)
        assertThat(res.avsnitt[1].sporsmal).hasSize(1)

        val forsorgerpliktSporsmal = res.avsnitt[1].sporsmal[0]
        assertThat(forsorgerpliktSporsmal.erUtfylt).isTrue
        validateFeltMedSvar(
            forsorgerpliktSporsmal.felt!![0],
            Type.SYSTEMDATA,
            SvarType.LOCALE_TEKST,
            "familierelasjon.ingen_registrerte_barn_tekst",
        )
    }

    @Test
    fun harSystemBarn_ikkeUtfyltDeltBosted_ikkeUtfyltBarnebidrag() {
        val forsorgerplikt = JsonForsorgerplikt(JsonHarForsorgerplikt(JsonKilde.SYSTEM, TRUE), null, listOf(JsonAnsvar(JsonBarn(JsonKilde.SYSTEM, JsonNavn("Grønn", "", "Jakke"), "2020-02-02", "11111111111", false), null, JsonErFolkeregistrertSammen(JsonKildeSystem.SYSTEM, TRUE), null)))
        val soknad = createSoknad(null, forsorgerplikt)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)

        val forsorgerpliktSporsmal = res.avsnitt[1].sporsmal
        assertThat(forsorgerpliktSporsmal).hasSize(3)

        val systemBarnSporsmal = forsorgerpliktSporsmal[0]
        assertThat(systemBarnSporsmal.erUtfylt).isTrue
        assertThat(systemBarnSporsmal.felt!![0].type).isEqualTo(Type.SYSTEMDATA_MAP)

        val labelSvarMap = systemBarnSporsmal.felt[0].labelSvarMap
        assertThat(labelSvarMap).hasSize(3)
        assertThat(labelSvarMap!!["familie.barn.true.barn.navn.label"]!!.value).isEqualTo("Grønn Jakke")
        assertThat(labelSvarMap["familierelasjon.fodselsdato"]!!.value).isEqualTo("2020-02-02")
        assertThat(
            labelSvarMap["familierelasjon.samme_folkeregistrerte_adresse"]!!.value,
        ).isEqualTo("system.familie.barn.true.barn.folkeregistrertsammen.true")

        val deltBostedSporsmal = forsorgerpliktSporsmal[1]
        assertThat(deltBostedSporsmal.erUtfylt).isFalse
        assertThat(deltBostedSporsmal.felt).isNull()

        val barnebidragSporsmal = forsorgerpliktSporsmal[2]
        assertThat(barnebidragSporsmal.erUtfylt).isFalse
        assertThat(barnebidragSporsmal.felt).isNull()
    }

    @Test
    fun harSystemBarn_utfyltDeltBosted_utfyltBarnebidrag() {
        val forsorgerplikt = JsonForsorgerplikt(JsonHarForsorgerplikt(JsonKilde.SYSTEM, TRUE), JsonBarnebidrag(JsonKildeBruker.BRUKER, JsonBarnebidrag.Verdi.BETALER), listOf(JsonAnsvar(JsonBarn(JsonKilde.SYSTEM, JsonNavn("Grønn", "", "Jakke"), "2020-02-02", "11111111111", false), null, JsonErFolkeregistrertSammen(JsonKildeSystem.SYSTEM, TRUE), JsonHarDeltBosted(JsonKildeBruker.BRUKER, TRUE))))
        val soknad = createSoknad(null, forsorgerplikt)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)

        val forsorgerpliktSporsmal = res.avsnitt[1].sporsmal
        assertThat(forsorgerpliktSporsmal).hasSize(3)

        val systemBarnSporsmal = forsorgerpliktSporsmal[0]
        assertThat(systemBarnSporsmal.erUtfylt).isTrue

        val deltBostedSporsmal = forsorgerpliktSporsmal[1]
        assertThat(deltBostedSporsmal.erUtfylt).isTrue
        assertThat(deltBostedSporsmal.felt).hasSize(1)
        validateFeltMedSvar(
            deltBostedSporsmal.felt!![0],
            Type.CHECKBOX,
            SvarType.LOCALE_TEKST,
            "system.familie.barn.true.barn.deltbosted.true",
        )

        val barnebidragSporsmal = forsorgerpliktSporsmal[2]
        assertThat(barnebidragSporsmal.erUtfylt).isTrue
        assertThat(barnebidragSporsmal.felt).hasSize(1)
        validateFeltMedSvar(barnebidragSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "familie.barn.true.barnebidrag.betaler")
    }

    private fun createSoknad(
        sivilstatus: JsonSivilstatus?,
        forsorgerplikt: JsonForsorgerplikt?,
    ): JsonInternalSoknad {
        val base = createValidEmptyJsonInternalSoknad()
        val soknad = requireNotNull(base.soknad)
        return base.copy(soknad = soknad.copy(data = soknad.data.copy(familie = JsonFamilie(forsorgerplikt ?: JsonForsorgerplikt(), sivilstatus))))
    }
}
