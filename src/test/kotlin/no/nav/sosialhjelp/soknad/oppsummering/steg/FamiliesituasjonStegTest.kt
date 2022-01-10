package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBorSammenMed
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.Boolean

internal class FamiliesituasjonStegTest {

    private val steg = FamiliesituasjonSteg()

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
        val ugift = JsonSivilstatus()
            .withKilde(JsonKilde.BRUKER)
            .withStatus(JsonSivilstatus.Status.UGIFT)
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
        val gift = JsonSivilstatus()
            .withKilde(JsonKilde.BRUKER)
            .withStatus(JsonSivilstatus.Status.GIFT)
            .withEktefelle(
                JsonEktefelle()
                    .withNavn(JsonNavn().withFornavn("Gul").withEtternavn("Knapp"))
                    .withFodselsdato(null)
                    .withPersonIdentifikator(null)
            )
            .withBorSammenMed(true)
        val soknad = createSoknad(gift, null)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)

        val sivilstatusSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(sivilstatusSporsmal.erUtfylt).isFalse // fnr og pnr mangler
        assertThat(sivilstatusSporsmal.felt).hasSize(1)
        assertThat(sivilstatusSporsmal.felt!![0].type).isEqualTo(Type.SYSTEMDATA_MAP)

        val labelSvarMap = sivilstatusSporsmal.felt!![0].labelSvarMap
        assertThat(labelSvarMap).hasSize(4)
        assertThat(labelSvarMap!!["familie.sivilstatus.gift.ektefelle.navn.label"]!!.value).isEqualTo("Gul Knapp")
        assertThat(labelSvarMap["familie.sivilstatus.gift.ektefelle.fnr.label"]!!.value).isNull()
        assertThat(labelSvarMap["familie.sivilstatus.gift.ektefelle.pnr.label"]!!.value).isNull()
        assertThat(labelSvarMap["familie.sivilstatus.gift.ektefelle.borsammen.sporsmal"]!!.value).isEqualTo("familie.sivilstatus.gift.ektefelle.borsammen.true")
    }

    @Test
    fun systemSivilstatus_ektefelleMedAdressebeskyttelse() {
        val giftMedAdressebeskyttelse = JsonSivilstatus()
            .withKilde(JsonKilde.SYSTEM)
            .withStatus(JsonSivilstatus.Status.GIFT)
            .withEktefelleHarDiskresjonskode(true)
        val soknad = createSoknad(giftMedAdressebeskyttelse, null)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)
        assertThat(res.avsnitt[0].sporsmal).hasSize(1)

        val sivilstatusSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(sivilstatusSporsmal.erUtfylt).isTrue
        assertThat(sivilstatusSporsmal.felt).hasSize(1)
        validateFeltMedSvar(sivilstatusSporsmal.felt!![0], Type.SYSTEMDATA, SvarType.LOCALE_TEKST, "system.familie.sivilstatus.ikkeTilgang.label")
    }

    @Test
    fun systemSivilstatus_ektefelle() {
        val giftMedAdressebeskyttelse = JsonSivilstatus()
            .withKilde(JsonKilde.SYSTEM)
            .withStatus(JsonSivilstatus.Status.GIFT)
            .withEktefelle(
                JsonEktefelle()
                    .withNavn(JsonNavn().withFornavn("Gul").withEtternavn("Knapp"))
                    .withFodselsdato("1999-12-31")
                    .withPersonIdentifikator("11111111111")
            )
            .withFolkeregistrertMedEktefelle(true)
        val soknad = createSoknad(giftMedAdressebeskyttelse, null)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)
        assertThat(res.avsnitt[0].sporsmal).hasSize(1)

        val sivilstatusSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(sivilstatusSporsmal.erUtfylt).isTrue
        assertThat(sivilstatusSporsmal.felt).hasSize(1)
        assertThat(sivilstatusSporsmal.felt!![0].type).isEqualTo(Type.SYSTEMDATA_MAP)

        val labelSvarMap = sivilstatusSporsmal.felt!![0].labelSvarMap
        assertThat(labelSvarMap).hasSize(3)
        assertThat(labelSvarMap!!["system.familie.sivilstatus.gift.ektefelle.navn"]!!.value).isEqualTo("Gul Knapp")
        assertThat(labelSvarMap["system.familie.sivilstatus.gift.ektefelle.fodselsdato"]!!.value).isEqualTo("1999-12-31")
        assertThat(labelSvarMap["system.familie.sivilstatus.gift.ektefelle.folkereg"]!!.value).isEqualTo("system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen.true")
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
        validateFeltMedSvar(forsorgerpliktSporsmal.felt!![0], Type.SYSTEMDATA, SvarType.LOCALE_TEKST, "familierelasjon.ingen_registrerte_barn_tekst")
    }

    @Test
    fun harSystemBarn_ikkeUtfyltDeltBosted_ikkeUtfyltBarnebidrag() {
        val forsorgerplikt = JsonForsorgerplikt()
            .withHarForsorgerplikt(
                JsonHarForsorgerplikt()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(java.lang.Boolean.TRUE)
            )
            .withAnsvar(
                listOf(
                    JsonAnsvar()
                        .withBarn(
                            JsonBarn()
                                .withKilde(JsonKilde.SYSTEM)
                                .withNavn(JsonNavn().withFornavn("Grønn").withEtternavn("Jakke"))
                                .withFodselsdato("2020-02-02")
                                .withPersonIdentifikator("11111111111")
                        )
                        .withErFolkeregistrertSammen(JsonErFolkeregistrertSammen().withVerdi(java.lang.Boolean.TRUE))
                        .withHarDeltBosted(null)
                )
            )
            .withBarnebidrag(null)
        val soknad = createSoknad(null, forsorgerplikt)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)

        val forsorgerpliktSporsmal = res.avsnitt[1].sporsmal
        assertThat(forsorgerpliktSporsmal).hasSize(3)

        val systemBarnSporsmal = forsorgerpliktSporsmal[0]
        assertThat(systemBarnSporsmal.erUtfylt).isTrue
        assertThat(systemBarnSporsmal.felt!![0].type).isEqualTo(Type.SYSTEMDATA_MAP)

        val labelSvarMap = systemBarnSporsmal.felt!![0].labelSvarMap
        assertThat(labelSvarMap).hasSize(3)
        assertThat(labelSvarMap!!["familie.barn.true.barn.navn.label"]!!.value).isEqualTo("Grønn Jakke")
        assertThat(labelSvarMap["familierelasjon.fodselsdato"]!!.value).isEqualTo("2020-02-02")
        assertThat(labelSvarMap["familierelasjon.samme_folkeregistrerte_adresse"]!!.value).isEqualTo("system.familie.barn.true.barn.folkeregistrertsammen.true")
        assertThat(labelSvarMap).doesNotContainKey("familierelasjon.bor_sammen")

        val deltBostedSporsmal = forsorgerpliktSporsmal[1]
        assertThat(deltBostedSporsmal.erUtfylt).isFalse
        assertThat(deltBostedSporsmal.felt).isNull()

        val barnebidragSporsmal = forsorgerpliktSporsmal[2]
        assertThat(barnebidragSporsmal.erUtfylt).isFalse
        assertThat(barnebidragSporsmal.felt).isNull()
    }

    @Test
    fun harSystemBarn_utfyltDeltBosted_utfyltBarnebidrag() {
        val forsorgerplikt = JsonForsorgerplikt()
            .withHarForsorgerplikt(
                JsonHarForsorgerplikt()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(java.lang.Boolean.TRUE)
            )
            .withAnsvar(
                listOf(
                    JsonAnsvar()
                        .withBarn(
                            JsonBarn()
                                .withKilde(JsonKilde.SYSTEM)
                                .withNavn(JsonNavn().withFornavn("Grønn").withEtternavn("Jakke"))
                                .withFodselsdato("2020-02-02")
                                .withPersonIdentifikator("11111111111")
                        )
                        .withErFolkeregistrertSammen(
                            JsonErFolkeregistrertSammen()
                                .withVerdi(java.lang.Boolean.TRUE)
                        )
                        .withHarDeltBosted(
                            JsonHarDeltBosted()
                                .withVerdi(java.lang.Boolean.TRUE)
                        )
                )
            )
            .withBarnebidrag(
                JsonBarnebidrag()
                    .withVerdi(JsonBarnebidrag.Verdi.BETALER)
            )
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
        validateFeltMedSvar(deltBostedSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "system.familie.barn.true.barn.deltbosted.true")

        val barnebidragSporsmal = forsorgerpliktSporsmal[2]
        assertThat(barnebidragSporsmal.erUtfylt).isTrue
        assertThat(barnebidragSporsmal.felt).hasSize(1)
        validateFeltMedSvar(barnebidragSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "familie.barn.true.barnebidrag.betaler")
    }

    @Test
    fun harBrukerregistrerteBarn_ikkeUtfyltDeltBosted_ikkeUtfyltBarnebidrag() {
        val forsorgerplikt = JsonForsorgerplikt()
            .withHarForsorgerplikt(
                JsonHarForsorgerplikt()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(java.lang.Boolean.TRUE)
            )
            .withAnsvar(
                listOf(
                    JsonAnsvar()
                        .withBarn(
                            JsonBarn()
                                .withKilde(JsonKilde.BRUKER)
                                .withNavn(JsonNavn().withFornavn("Grønn").withEtternavn("Jakke"))
                                .withFodselsdato("2020-02-02")
                        )
                        .withBorSammenMed(
                            JsonBorSammenMed()
                                .withVerdi(Boolean.TRUE)
                        )
                        .withHarDeltBosted(null)
                )
            )
            .withBarnebidrag(null)
        val soknad = createSoknad(null, forsorgerplikt)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)

        val forsorgerpliktSporsmal = res.avsnitt[1].sporsmal
        assertThat(forsorgerpliktSporsmal).hasSize(3)

        val brukerregistrerteBarnSporsmal = forsorgerpliktSporsmal[0]
        assertThat(brukerregistrerteBarnSporsmal.erUtfylt).isTrue
        assertThat(brukerregistrerteBarnSporsmal.felt!![0].type).isEqualTo(Type.SYSTEMDATA_MAP)

        val labelSvarMap = brukerregistrerteBarnSporsmal.felt!![0].labelSvarMap
        assertThat(labelSvarMap).hasSize(3)
        assertThat(labelSvarMap!!["familie.barn.true.barn.navn.label"]!!.value).isEqualTo("Grønn Jakke")
        assertThat(labelSvarMap["familierelasjon.fodselsdato"]!!.value).isEqualTo("2020-02-02")
        assertThat(labelSvarMap).doesNotContainKey("familierelasjon.samme_folkeregistrerte_adresse")
        assertThat(labelSvarMap["familierelasjon.bor_sammen"]!!.value).isEqualTo("familie.barn.true.barn.borsammen.true")

        val deltBostedSporsmal = forsorgerpliktSporsmal[1]
        assertThat(deltBostedSporsmal.erUtfylt).isFalse
        assertThat(deltBostedSporsmal.felt).isNull()

        val barnebidragSporsmal = forsorgerpliktSporsmal[2]
        assertThat(barnebidragSporsmal.erUtfylt).isFalse
        assertThat(barnebidragSporsmal.felt).isNull()
    }

    @Test
    fun harBrukerregistrerteBarn_utfyltDeltBosted_utfyltBarnebidrag() {
        val forsorgerplikt = JsonForsorgerplikt()
            .withHarForsorgerplikt(
                JsonHarForsorgerplikt()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(java.lang.Boolean.TRUE)
            )
            .withAnsvar(
                listOf(
                    JsonAnsvar()
                        .withBarn(
                            JsonBarn()
                                .withKilde(JsonKilde.BRUKER)
                                .withNavn(JsonNavn().withFornavn("Grønn").withEtternavn("Jakke"))
                                .withFodselsdato(null)
                        )
                        .withBorSammenMed(
                            JsonBorSammenMed()
                                .withVerdi(Boolean.TRUE)
                        )
                        .withHarDeltBosted(
                            JsonHarDeltBosted()
                                .withVerdi(Boolean.TRUE)
                        )
                )
            )
            .withBarnebidrag(
                JsonBarnebidrag()
                    .withVerdi(JsonBarnebidrag.Verdi.BETALER)
            )
        val soknad = createSoknad(null, forsorgerplikt)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(2)

        val forsorgerpliktSporsmal = res.avsnitt[1].sporsmal
        assertThat(forsorgerpliktSporsmal).hasSize(3)

        val brukerregistrerteBarnSporsmal = forsorgerpliktSporsmal[0]
        assertThat(brukerregistrerteBarnSporsmal.erUtfylt).isTrue

        val labelSvarMap = brukerregistrerteBarnSporsmal.felt!![0].labelSvarMap
        assertThat(labelSvarMap).hasSize(2)
        assertThat(labelSvarMap!!["familie.barn.true.barn.navn.label"]!!.value).isEqualTo("Grønn Jakke")
        assertThat(labelSvarMap).doesNotContainKey("familierelasjon.fodselsdato")
        assertThat(labelSvarMap).doesNotContainKey("familierelasjon.samme_folkeregistrerte_adresse")
        assertThat(labelSvarMap["familierelasjon.bor_sammen"]!!.value).isEqualTo("familie.barn.true.barn.borsammen.true")

        val deltBostedSporsmal = forsorgerpliktSporsmal[1]
        assertThat(deltBostedSporsmal.erUtfylt).isTrue
        assertThat(deltBostedSporsmal.felt).hasSize(1)
        validateFeltMedSvar(deltBostedSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "system.familie.barn.true.barn.deltbosted.true")

        val barnebidragSporsmal = forsorgerpliktSporsmal[2]
        assertThat(barnebidragSporsmal.erUtfylt).isTrue
        assertThat(barnebidragSporsmal.felt).hasSize(1)
        validateFeltMedSvar(barnebidragSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "familie.barn.true.barnebidrag.betaler")
    }

    private fun createSoknad(sivilstatus: JsonSivilstatus?, forsorgerplikt: JsonForsorgerplikt?): JsonInternalSoknad {
        return JsonInternalSoknad()
            .withSoknad(
                JsonSoknad()
                    .withData(
                        JsonData()
                            .withFamilie(
                                JsonFamilie()
                                    .withSivilstatus(sivilstatus)
                                    .withForsorgerplikt(forsorgerplikt)
                            )
                    )
            )
    }
}
