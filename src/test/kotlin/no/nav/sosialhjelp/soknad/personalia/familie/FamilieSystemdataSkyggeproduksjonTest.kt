package no.nav.sosialhjelp.soknad.personalia.familie


import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.familie.FamilieService
import no.nav.sosialhjelp.soknad.v2.shadow.SoknadV2AdapterService
import no.nav.sosialhjelp.soknad.v2.shadow.V2AdapterService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.sosialhjelp.soknad.personalia.person.domain.Ektefelle as EktefelleOld
import no.nav.sosialhjelp.soknad.v2.familie.Ektefelle as EktefelleV2

class FamilieSystemdataSkyggeproduksjonTest {

    private val personService: PersonService = mockk()
    private val familieService: FamilieService = mockk()
    private val v2AdapterService: V2AdapterService =
        SoknadV2AdapterService(mockk(), mockk(), mockk(), mockk(), mockk(), familieService)
    private val familieSystemdata = FamilieSystemdata(personService, v2AdapterService)

    @Test
    fun `skal legge til ektefelle i skyggeproduksjon`() {
        val ektefelleSlot = slot<EktefelleV2>()

        every { personService.hentPerson(EIER) } returns createPerson(
            JsonSivilstatus.Status.GIFT.toString(),
            EKTEFELLE
        )
        every { personService.hentBarnForPerson(EIER) } returns emptyList()
        every { familieService.addEktefelle(any(), capture(ektefelleSlot)) } just Runs

        val soknadUnderArbeid = createSoknadUnderArbeid()
        familieSystemdata.updateSystemdataIn(soknadUnderArbeid)

        verify(exactly = 1) {
            familieService.addEktefelle(any(), any())
            assertThat(ektefelleSlot.captured.personId).isEqualTo(EKTEFELLE.fnr)
        }
    }

    private fun createPerson(sivilstatus: String, ektefelle: EktefelleOld?): Person {
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

    private fun createSoknadUnderArbeid(
        jsonInternalSoknad: JsonInternalSoknad = SoknadServiceOld.createEmptyJsonInternalSoknad(
            EIER
        )
    ): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = "acac6db9-c124-4ba2-a60c-97800ebfc7fc",
            tilknyttetBehandlingsId = null,
            eier = EIER,
            jsonInternalSoknad = jsonInternalSoknad,
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )
    }

    companion object {
        private const val EIER = "12345678901"
        private val EKTEFELLE =
            EktefelleOld("Av", "Og", "På", LocalDate.parse("1993-02-01"), "11111111111", false, false)
    }

}