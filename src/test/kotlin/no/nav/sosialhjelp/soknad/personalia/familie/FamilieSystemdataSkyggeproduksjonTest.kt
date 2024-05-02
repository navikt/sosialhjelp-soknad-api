package no.nav.sosialhjelp.soknad.personalia.familie

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.familie.service.FamilieRegisterService
import no.nav.sosialhjelp.soknad.v2.shadow.SoknadV2AdapterService
import no.nav.sosialhjelp.soknad.v2.shadow.V2AdapterService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import no.nav.sosialhjelp.soknad.personalia.person.domain.Barn as BarnOld
import no.nav.sosialhjelp.soknad.personalia.person.domain.Ektefelle as EktefelleOld
import no.nav.sosialhjelp.soknad.v2.familie.Barn as BarnV2
import no.nav.sosialhjelp.soknad.v2.familie.Ektefelle as EktefelleV2

class FamilieSystemdataSkyggeproduksjonTest {
    private val personService: PersonService = mockk()
    private val familieService: FamilieRegisterService = mockk()
    private val v2AdapterService: V2AdapterService =
        SoknadV2AdapterService(mockk(), mockk(), mockk(), mockk(), familieService, mockk())
    private val familieSystemdata = FamilieSystemdata(personService, v2AdapterService)

    @Test
    fun `skal legge til ektefelle fra systemdata i skyggeproduksjon`() {
        val ektefelleSlot = slot<EktefelleV2>()

        every { personService.hentPerson(EIER) } returns
            createPerson(
                JsonSivilstatus.Status.GIFT.toString(),
                EKTEFELLE,
            )
        every { personService.hentBarnForPerson(EIER) } returns emptyList()
        every { familieService.updateSivilstatusFraRegister(any(), any(), capture(ektefelleSlot)) } just Runs

        val soknadUnderArbeid = createSoknadUnderArbeid()
        familieSystemdata.updateSystemdataIn(soknadUnderArbeid)

        verify(exactly = 1) {
            familieService.updateSivilstatusFraRegister(any(), any(), any())
            assertThat(ektefelleSlot.captured.personId).isEqualTo(EKTEFELLE.fnr)
        }
    }

    @Test
    fun `skal legge til barn fra systemdata i skyggeproduksjon`() {
        val barnSlotListe = slot<MutableList<BarnV2>>()

        every { personService.hentPerson(EIER) } returns createPerson(JsonSivilstatus.Status.UGIFT.toString(), null)
        every { personService.hentBarnForPerson(any()) } returns listOf(BARN)
        every { familieService.updateForsorgerpliktRegister(any(), any(), capture(barnSlotListe)) } just Runs

        val soknadUnderArbeid = createSoknadUnderArbeid()
        familieSystemdata.updateSystemdataIn(soknadUnderArbeid)

        verify(exactly = 1) {
            familieService.updateForsorgerpliktRegister(UUID.fromString(SOKNADSID), true, any())
            assertThat(barnSlotListe.captured).hasSize(1)
            assertThat(barnSlotListe.captured[0].personId).isEqualTo(BARN.fnr)
        }
    }

    private fun createPerson(
        sivilstatus: String,
        ektefelle: EktefelleOld?,
    ): Person {
        return Person(
            fornavn = "fornavn",
            mellomnavn = "mellomnavn",
            etternavn = "etternavn",
            fnr = EIER,
            sivilstatus = sivilstatus,
            statsborgerskap = emptyList(),
            ektefelle = ektefelle,
            bostedsadresse = null,
            oppholdsadresse = null,
        )
    }

    private fun createSoknadUnderArbeid(
        jsonInternalSoknad: JsonInternalSoknad =
            SoknadServiceOld.createEmptyJsonInternalSoknad(
                EIER,
            ),
    ): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = SOKNADSID,
            eier = EIER,
            jsonInternalSoknad = jsonInternalSoknad,
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now(),
        )
    }

    companion object {
        private const val EIER = "12345678901"
        private const val SOKNADSID = "acac6db9-c124-4ba2-a60c-97800ebfc7fc"
        private const val FORNAVN_BARN = "Rudolf"
        private const val MELLOMNAVN_BARN = "Rød På"
        private const val ETTERNAVN_BARN = "Nesen"
        private val FODSELSDATO_BARN = LocalDate.parse("2001-02-03")
        private const val FNR_BARN = "22222222222"
        private const val ER_FOLKEREGISTRERT_SAMMEN_BARN = true

        private val BARN =
            BarnOld(
                FORNAVN_BARN,
                MELLOMNAVN_BARN,
                ETTERNAVN_BARN,
                FNR_BARN,
                FODSELSDATO_BARN,
                ER_FOLKEREGISTRERT_SAMMEN_BARN,
            )
        private val EKTEFELLE =
            EktefelleOld("Av", "Og", "På", LocalDate.parse("1993-02-01"), "11111111111", false, false)
    }
}
