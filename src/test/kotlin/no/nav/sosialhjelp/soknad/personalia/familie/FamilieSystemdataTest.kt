package no.nav.sosialhjelp.soknad.personalia.familie

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag.Verdi
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBorSammenMed
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonErFolkeregistrertSammen
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarDeltBosted
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSamvarsgrad
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Barn
import no.nav.sosialhjelp.soknad.personalia.person.domain.Ektefelle
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.shadow.V2AdapterService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class FamilieSystemdataTest {

    private val mapper = JsonSosialhjelpObjectMapper.createObjectMapper()

    private val personService: PersonService = mockk()
    private val v2AdapterService: V2AdapterService = mockk()
    private val familieSystemdata = FamilieSystemdata(personService, v2AdapterService)

    @Test
    fun skalSetteSivilstatusGiftMedEktefelle() {
        val person = createPerson(JsonSivilstatus.Status.GIFT.toString(), EKTEFELLE)
        every { personService.hentPerson(any()) } returns person
        every { personService.hentBarnForPerson(any()) } returns null

        val soknadUnderArbeid = createSoknadUnderArbeid()
        familieSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val internalSoknad = mapper.writeValueAsString(soknadUnderArbeid.jsonInternalSoknad)
        ensureValidInternalSoknad(internalSoknad)
        val sivilstatus = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.familie.sivilstatus
        assertThat(sivilstatus.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(sivilstatus.status).isEqualTo(JsonSivilstatus.Status.GIFT)
        assertThatEktefelleIsCorrectlyConverted(EKTEFELLE, sivilstatus.ektefelle)
        assertThat(sivilstatus.ektefelleHarDiskresjonskode).isFalse
        assertThat(sivilstatus.folkeregistrertMedEktefelle).isFalse
        // borSammenMed er null, men assertion gir NPE?
//        assertThat(sivilstatus.borSammenMed).isNull()
    }

    @Test
    fun skalIkkeSetteSivilstatusDersomEktefelleMangler() {
        val person = createPerson(JsonSivilstatus.Status.GIFT.toString(), null)
        every { personService.hentPerson(any()) } returns person
        every { personService.hentBarnForPerson(any()) } returns null

        val soknadUnderArbeid = createSoknadUnderArbeid()
        familieSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val internalSoknad = mapper.writeValueAsString(soknadUnderArbeid.jsonInternalSoknad)
        ensureValidInternalSoknad(internalSoknad)
        val sivilstatus = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.familie.sivilstatus
        assertThat(sivilstatus).isNull()
    }

    @Test
    fun skalIkkeSetteSivilstatusDersomAnnetEnnGift() {
        sivilstatusSkalIkkeSettes(JsonSivilstatus.Status.UGIFT, null)
        sivilstatusSkalIkkeSettes(JsonSivilstatus.Status.SAMBOER, null)
        sivilstatusSkalIkkeSettes(JsonSivilstatus.Status.ENKE, null)
        sivilstatusSkalIkkeSettes(JsonSivilstatus.Status.SKILT, null)
        sivilstatusSkalIkkeSettes(JsonSivilstatus.Status.SEPARERT, null)
        sivilstatusSkalIkkeSettes(JsonSivilstatus.Status.UGIFT, EKTEFELLE)
        sivilstatusSkalIkkeSettes(JsonSivilstatus.Status.SAMBOER, EKTEFELLE)
        sivilstatusSkalIkkeSettes(JsonSivilstatus.Status.ENKE, EKTEFELLE)
        sivilstatusSkalIkkeSettes(JsonSivilstatus.Status.SKILT, EKTEFELLE)
        sivilstatusSkalIkkeSettes(JsonSivilstatus.Status.SEPARERT, EKTEFELLE)
    }

    @Test
    fun skalSetteSivilstatusGiftMedTomEktefelleDersomEktefelleHarDiskresjonskode() {
        val person = createPerson(JsonSivilstatus.Status.GIFT.toString(), EKTEFELLE_MED_DISKRESJONSKODE)
        every { personService.hentPerson(any()) } returns person
        every { personService.hentBarnForPerson(any()) } returns null

        val soknadUnderArbeid = createSoknadUnderArbeid()
        familieSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val internalSoknad = mapper.writeValueAsString(soknadUnderArbeid.jsonInternalSoknad)
        ensureValidInternalSoknad(internalSoknad)
        val sivilstatus = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.familie.sivilstatus
        assertThat(sivilstatus.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(sivilstatus.status).isEqualTo(JsonSivilstatus.Status.GIFT)
        assertThatEktefelleIsCorrectlyConverted(TOM_EKTEFELLE, sivilstatus.ektefelle)
        assertThat(sivilstatus.ektefelleHarDiskresjonskode).isTrue
        assertThat(sivilstatus.folkeregistrertMedEktefelle).isFalse
        // borSammenMed er null, men assertion gir NPE?
//        assertThat(sivilstatus.borSammenMed).isNull()
    }

    @Test
    fun skalSetteForsorgerpliktMedFlereBarn() {
        every { personService.hentPerson(any()) } returns null
        every { personService.hentBarnForPerson(any()) } returns listOf(BARN, BARN_2)

        val soknadUnderArbeid = createSoknadUnderArbeid()
        familieSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val internalSoknad = mapper.writeValueAsString(soknadUnderArbeid.jsonInternalSoknad)
        ensureValidInternalSoknad(internalSoknad)
        val forsorgerplikt = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.familie.forsorgerplikt
        assertThat(forsorgerplikt.harForsorgerplikt.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(forsorgerplikt.harForsorgerplikt.verdi).isTrue
        val ansvarList = forsorgerplikt.ansvar
        val ansvar = ansvarList[0]
        val ansvar2 = ansvarList[1]
        assertThat(ansvar.barn.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(ansvar2.barn.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThatAnsvarIsCorrectlyConverted(BARN, ansvar)
        assertThatAnsvarIsCorrectlyConverted(BARN_2, ansvar2)
    }

    @Test
    fun skalIkkeSetteForsorgerplikt() {
        every { personService.hentPerson(any()) } returns null
        every { personService.hentBarnForPerson(any()) } returns emptyList()

        val soknadUnderArbeid = createSoknadUnderArbeid()
        familieSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val internalSoknad = mapper.writeValueAsString(soknadUnderArbeid.jsonInternalSoknad)
        ensureValidInternalSoknad(internalSoknad)
        val forsorgerplikt = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.familie.forsorgerplikt
        assertThat(forsorgerplikt.harForsorgerplikt.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(forsorgerplikt.harForsorgerplikt.verdi).isFalse
        val ansvarList = forsorgerplikt.ansvar
        assertThat(ansvarList.isEmpty()).isTrue
    }

    @Test
    fun skalIkkeOverskriveBrukerregistrerteBarnNaarDetFinnesSystemBarn() {
        every { personService.hentBarnForPerson(any()) } returns listOf(BARN, BARN_2)
        every { personService.hentPerson(any()) } returns null

        val jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER)
        jsonInternalSoknad.soknad.data.familie.forsorgerplikt
            .withHarForsorgerplikt(
                JsonHarForsorgerplikt()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(true)
            )
            .withAnsvar(listOf(JSON_ANSVAR, JSON_ANSVAR_2, JSON_ANSVAR_3_BRUKERREGISTRERT))
        val soknadUnderArbeid = createSoknadUnderArbeid(jsonInternalSoknad)
        familieSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val internalSoknad = mapper.writeValueAsString(soknadUnderArbeid.jsonInternalSoknad)
        ensureValidInternalSoknad(internalSoknad)
        val forsorgerplikt = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.familie.forsorgerplikt
        assertThat(forsorgerplikt.harForsorgerplikt.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(forsorgerplikt.harForsorgerplikt.verdi).isTrue
        val ansvarList = forsorgerplikt.ansvar
        val ansvar = ansvarList[0]
        val ansvar2 = ansvarList[1]
        val ansvar3 = ansvarList[2]
        assertThat(ansvar.barn.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(ansvar2.barn.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(ansvar3.barn.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThatAnsvarIsCorrectlyConverted(BARN, JSON_ANSVAR)
        assertThatAnsvarIsCorrectlyConverted(BARN_2, JSON_ANSVAR_2)
        val jsonBarn = ansvar3.barn
        assertThat(jsonBarn.navn.fornavn).isEqualTo(FORNAVN_BARN_3)
        assertThat(jsonBarn.navn.mellomnavn).isEqualTo(MELLOMNAVN_BARN_3)
        assertThat(jsonBarn.navn.etternavn).isEqualTo(ETTERNAVN_BARN_3)
    }

    @Test
    fun skalIkkeOverskriveBrukerregistrerteBarnEllerForsorgerpliktVerdiNaarDetIkkeFinnesSystemBarn() {
        every { personService.hentPerson(any()) } returns null
        every { personService.hentBarnForPerson(any()) } returns emptyList()

        val jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER)
        jsonInternalSoknad.soknad.data.familie.forsorgerplikt
            .withHarForsorgerplikt(
                JsonHarForsorgerplikt()
                    .withKilde(JsonKilde.BRUKER)
                    .withVerdi(true)
            )
            .withAnsvar(listOf(JSON_ANSVAR_3_BRUKERREGISTRERT))
        val soknadUnderArbeid = createSoknadUnderArbeid(jsonInternalSoknad)
        familieSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val internalSoknad = mapper.writeValueAsString(soknadUnderArbeid.jsonInternalSoknad)
        ensureValidInternalSoknad(internalSoknad)
        val forsorgerplikt = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.familie.forsorgerplikt
        assertThat(forsorgerplikt.harForsorgerplikt.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(forsorgerplikt.harForsorgerplikt.verdi).isTrue
        val ansvarList = forsorgerplikt.ansvar
        val ansvar = ansvarList[0]
        assertThat(ansvar.barn.kilde).isEqualTo(JsonKilde.BRUKER)
        val jsonBarn = ansvar.barn
        assertThat(jsonBarn.navn.fornavn).isEqualTo(FORNAVN_BARN_3)
        assertThat(jsonBarn.navn.mellomnavn).isEqualTo(MELLOMNAVN_BARN_3)
        assertThat(jsonBarn.navn.etternavn).isEqualTo(ETTERNAVN_BARN_3)
    }

    @Test
    fun skalIkkeOverskriveSamvaersgradOgHarDeltBostedOgBarnebidrag() {
        every { personService.hentPerson(any()) } returns null
        every { personService.hentBarnForPerson(any()) } returns listOf(BARN, BARN_2)

        val soknadUnderArbeid = createSoknadUnderArbeid(createJsonInternalSoknadWithBarnWithUserFilledInfoOnSystemBarn())
        familieSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val internalSoknad = mapper.writeValueAsString(soknadUnderArbeid.jsonInternalSoknad)
        ensureValidInternalSoknad(internalSoknad)
        val forsorgerplikt = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.familie.forsorgerplikt
        assertThat(forsorgerplikt.harForsorgerplikt.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(forsorgerplikt.harForsorgerplikt.verdi).isTrue
        assertThat(forsorgerplikt.barnebidrag.kilde).isEqualTo(JsonKildeBruker.BRUKER)
        assertThat(forsorgerplikt.barnebidrag.verdi).isEqualTo(Verdi.BEGGE)
        val ansvarList = forsorgerplikt.ansvar
        val ansvar = ansvarList[0]
        val ansvar2 = ansvarList[1]
        assertThat(ansvar.barn.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(ansvar2.barn.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThatAnsvarIsCorrectlyConverted(BARN, JSON_ANSVAR)
        assertThatAnsvarIsCorrectlyConverted(BARN_2, JSON_ANSVAR_2)
    }

    private fun sivilstatusSkalIkkeSettes(status: JsonSivilstatus.Status, ektefelle: Ektefelle?) {
        val person = createPerson(status.toString(), ektefelle)
        every { personService.hentPerson(any()) } returns person
        every { personService.hentBarnForPerson(any()) } returns null

        val soknadUnderArbeid = createSoknadUnderArbeid()
        familieSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val internalSoknad = mapper.writeValueAsString(soknadUnderArbeid.jsonInternalSoknad)
        ensureValidInternalSoknad(internalSoknad)
        val sivilstatus = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.familie.sivilstatus
        assertThat(sivilstatus).isNull()
    }

    private fun createJsonInternalSoknadWithBarnWithUserFilledInfoOnSystemBarn(): JsonInternalSoknad {
        val jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER)
        jsonInternalSoknad.soknad.data.familie.forsorgerplikt
            .withHarForsorgerplikt(
                JsonHarForsorgerplikt()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(true)
            )
            .withAnsvar(listOf(JSON_ANSVAR, JSON_ANSVAR_2))
            .withBarnebidrag(
                JsonBarnebidrag()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi(Verdi.BEGGE)
            )
        return jsonInternalSoknad
    }

    private fun assertThatAnsvarIsCorrectlyConverted(barn: Barn, jsonAnsvar: JsonAnsvar) {
        val jsonBarn = jsonAnsvar.barn
        assertThat(barn.folkeregistrertSammen).isEqualTo(jsonAnsvar.erFolkeregistrertSammen?.verdi)
        assertThat(barn.fnr).isEqualTo(jsonBarn.personIdentifikator)
        if (barn.fodselsdato != null) {
            assertThat(barn.fodselsdato).hasToString(jsonBarn.fodselsdato)
        } else {
            assertThat(jsonBarn.fodselsdato).isNull()
        }
        assertThat(barn.fornavn).isEqualTo(jsonBarn.navn.fornavn)
        assertThat(barn.mellomnavn).isEqualTo(jsonBarn.navn.mellomnavn)
        assertThat(barn.etternavn).isEqualTo(jsonBarn.navn.etternavn)
    }

    private fun assertThatEktefelleIsCorrectlyConverted(ektefelle: Ektefelle, jsonEktefelle: JsonEktefelle) {
        if (ektefelle.fodselsdato != null) {
            assertThat(ektefelle.fodselsdato).hasToString(jsonEktefelle.fodselsdato)
        } else {
            assertThat(jsonEktefelle.fodselsdato).isNull()
        }
        assertThat(ektefelle.fnr).isEqualTo(jsonEktefelle.personIdentifikator)
        assertThat(ektefelle.fornavn).isEqualTo(jsonEktefelle.navn.fornavn)
        assertThat(ektefelle.mellomnavn).isEqualTo(jsonEktefelle.navn.mellomnavn)
        assertThat(ektefelle.etternavn).isEqualTo(jsonEktefelle.navn.etternavn)
    }

    private fun createPerson(sivilstatus: String, ektefelle: Ektefelle?): Person {
        return Person(
            fornavn = "fornavn",
            mellomnavn = "mellomnavn",
            etternavn = "etternavn",
            fnr = EIER,
            sivilstatus = sivilstatus,
            statsborgerskap = emptyList(),
            ektefelle = ektefelle,
            bostedsadresse = null,
            oppholdsadresse = null
        )
    }

    companion object {
        private const val EIER = "12345678901"
        private val EKTEFELLE = Ektefelle("Av", "Og", "På", LocalDate.parse("1993-02-01"), "11111111111", false, false)
        private val TOM_EKTEFELLE = Ektefelle("", "", "", null, null, false, true)
        private val EKTEFELLE_MED_DISKRESJONSKODE = Ektefelle(true)
        private const val FORNAVN_BARN = "Rudolf"
        private const val MELLOMNAVN_BARN = "Rød På"
        private const val ETTERNAVN_BARN = "Nesen"
        private val FODSELSDATO_BARN = LocalDate.parse("2001-02-03")
        private const val FNR_BARN = "22222222222"
        private const val ER_FOLKEREGISTRERT_SAMMEN_BARN = true
        private const val HAR_DELT_BOSTED_BARN = true
        private const val FORNAVN_BARN_2 = "Unna"
        private const val MELLOMNAVN_BARN_2 = "Vei"
        private const val ETTERNAVN_BARN_2 = "Herkommerjeg"
        private val FODSELSDATO_BARN_2 = LocalDate.parse("2003-02-01")
        private const val FNR_BARN_2 = "33333333333"
        private const val ER_FOLKEREGISTRERT_SAMMEN_BARN_2 = false
        private const val SAMVARSGRAD_BARN_2 = 25
        private const val FORNAVN_BARN_3 = "Jula"
        private const val MELLOMNAVN_BARN_3 = "Varer Helt Til"
        private const val ETTERNAVN_BARN_3 = "Påske"
        private val FODSELSDATO_BARN_3 = LocalDate.parse("2003-02-05")
        private const val SAMVARSGRAD_BARN_3 = 30
        private val BARN = Barn(
            FORNAVN_BARN,
            MELLOMNAVN_BARN,
            ETTERNAVN_BARN,
            FNR_BARN,
            FODSELSDATO_BARN,
            ER_FOLKEREGISTRERT_SAMMEN_BARN
        )
        private val BARN_2 = Barn(
            FORNAVN_BARN_2,
            MELLOMNAVN_BARN_2,
            ETTERNAVN_BARN_2,
            FNR_BARN_2,
            FODSELSDATO_BARN_2,
            ER_FOLKEREGISTRERT_SAMMEN_BARN_2
        )
        private val JSON_ANSVAR = JsonAnsvar()
            .withBarn(
                JsonBarn()
                    .withKilde(JsonKilde.SYSTEM)
                    .withNavn(
                        JsonNavn()
                            .withFornavn(FORNAVN_BARN)
                            .withMellomnavn(MELLOMNAVN_BARN)
                            .withEtternavn(ETTERNAVN_BARN)
                    )
                    .withFodselsdato(FODSELSDATO_BARN.toString())
                    .withPersonIdentifikator(FNR_BARN)
            )
            .withErFolkeregistrertSammen(
                JsonErFolkeregistrertSammen()
                    .withKilde(JsonKildeSystem.SYSTEM)
                    .withVerdi(ER_FOLKEREGISTRERT_SAMMEN_BARN)
            )
            .withHarDeltBosted(
                JsonHarDeltBosted()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi(HAR_DELT_BOSTED_BARN)
            )
        private val JSON_ANSVAR_2 = JsonAnsvar()
            .withBarn(
                JsonBarn()
                    .withKilde(JsonKilde.SYSTEM)
                    .withNavn(
                        JsonNavn()
                            .withFornavn(FORNAVN_BARN_2)
                            .withMellomnavn(MELLOMNAVN_BARN_2)
                            .withEtternavn(ETTERNAVN_BARN_2)
                    )
                    .withFodselsdato(FODSELSDATO_BARN_2.toString())
                    .withPersonIdentifikator(FNR_BARN_2)
            )
            .withErFolkeregistrertSammen(
                JsonErFolkeregistrertSammen()
                    .withKilde(JsonKildeSystem.SYSTEM)
                    .withVerdi(ER_FOLKEREGISTRERT_SAMMEN_BARN_2)
            )
            .withSamvarsgrad(
                JsonSamvarsgrad()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi(SAMVARSGRAD_BARN_2)
            )
        private val JSON_ANSVAR_3_BRUKERREGISTRERT = JsonAnsvar()
            .withBarn(
                JsonBarn()
                    .withKilde(JsonKilde.BRUKER)
                    .withNavn(
                        JsonNavn()
                            .withFornavn(FORNAVN_BARN_3)
                            .withMellomnavn(MELLOMNAVN_BARN_3)
                            .withEtternavn(ETTERNAVN_BARN_3)
                    )
                    .withFodselsdato(FODSELSDATO_BARN_3.toString())
            )
            .withBorSammenMed(
                JsonBorSammenMed()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi(false)
            )
            .withSamvarsgrad(
                JsonSamvarsgrad()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi(SAMVARSGRAD_BARN_3)
            )

        private fun createSoknadUnderArbeid(jsonInternalSoknad: JsonInternalSoknad = createEmptyJsonInternalSoknad(EIER)): SoknadUnderArbeid {
            return SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = "BEHANDLINGSID",
                tilknyttetBehandlingsId = null,
                eier = EIER,
                jsonInternalSoknad = jsonInternalSoknad,
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now()
            )
        }
    }
}
