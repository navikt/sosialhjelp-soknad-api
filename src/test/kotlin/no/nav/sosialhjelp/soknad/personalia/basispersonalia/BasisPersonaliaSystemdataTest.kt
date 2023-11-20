package no.nav.sosialhjelp.soknad.personalia.basispersonalia

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sosialhjelp.soknad.innsending.OldSoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class BasisPersonaliaSystemdataTest {

    private val personService: PersonService = mockk()
    private val basisPersonaliaSystemdata = BasisPersonaliaSystemdata(personService)

    private val defaultSoknadUnderArbeid = SoknadUnderArbeid(
        versjon = 1L,
        behandlingsId = "BEHANDLINGSID",
        tilknyttetBehandlingsId = null,
        eier = EIER,
        jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER),
        status = SoknadUnderArbeidStatus.UNDER_ARBEID,
        opprettetDato = LocalDateTime.now(),
        sistEndretDato = LocalDateTime.now()
    )

    @Test
    fun skalIkkeOppdatereDersomPersonaliaErNull() {
        val soknadUnderArbeid = defaultSoknadUnderArbeid
        every { personService.hentPerson(any()) } returns null

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonPersonalia = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia
        assertThat(jsonPersonalia.personIdentifikator.kilde).isEqualTo(JsonPersonIdentifikator.Kilde.SYSTEM)
        assertThat(jsonPersonalia.personIdentifikator.verdi).isEqualTo(EIER)
        assertThat(jsonPersonalia.navn.kilde).isEqualTo(JsonSokernavn.Kilde.SYSTEM)
        assertThat(jsonPersonalia.navn.fornavn).isBlank
        assertThat(jsonPersonalia.navn.mellomnavn).isBlank
        assertThat(jsonPersonalia.navn.etternavn).isBlank
        assertThat(jsonPersonalia.statsborgerskap).isNull()
        assertThat(jsonPersonalia.nordiskBorger).isNull()
    }

    @Test
    fun skalOppdatereNordiskPersonalia() {
        val person = Person(
            fornavn = FORNAVN,
            mellomnavn = MELLOMNAVN,
            etternavn = ETTERNAVN,
            fnr = EIER,
            sivilstatus = "ugift",
            statsborgerskap = listOf(NORSK_STATSBORGERSKAP),
            ektefelle = null,
            bostedsadresse = null,
            oppholdsadresse = null,
        )
        val soknadUnderArbeid = defaultSoknadUnderArbeid
        every { personService.hentPerson(any()) } returns person

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonPersonalia = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia
        assertThat(jsonPersonalia.personIdentifikator.kilde).isEqualTo(JsonPersonIdentifikator.Kilde.SYSTEM)
        assertThat(jsonPersonalia.personIdentifikator.verdi).isEqualTo(EIER)
        assertThat(jsonPersonalia.navn.kilde).isEqualTo(JsonSokernavn.Kilde.SYSTEM)
        assertThat(jsonPersonalia.navn.fornavn).isEqualTo(FORNAVN)
        assertThat(jsonPersonalia.navn.mellomnavn).isEqualTo(MELLOMNAVN)
        assertThat(jsonPersonalia.navn.etternavn).isEqualTo(ETTERNAVN)
        assertThat(jsonPersonalia.statsborgerskap.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(jsonPersonalia.statsborgerskap.verdi).isEqualTo(NORSK_STATSBORGERSKAP)
        assertThat(jsonPersonalia.nordiskBorger.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(jsonPersonalia.nordiskBorger.verdi).isTrue
    }

    @Test
    fun skalPrioritereNorskOverNordiskStatsborgerskap() {
        val person = Person(
            fornavn = FORNAVN,
            mellomnavn = MELLOMNAVN,
            etternavn = ETTERNAVN,
            fnr = EIER,
            sivilstatus = "ugift",
            statsborgerskap = listOf(NORDISK_STATSBORGERSKAP, NORSK_STATSBORGERSKAP),
            ektefelle = null,
            bostedsadresse = null,
            oppholdsadresse = null,
        )
        val soknadUnderArbeid = defaultSoknadUnderArbeid
        every { personService.hentPerson(any()) } returns person

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonPersonalia = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia
        assertThat(jsonPersonalia.statsborgerskap.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(jsonPersonalia.statsborgerskap.verdi).isEqualTo(NORSK_STATSBORGERSKAP)
        assertThat(jsonPersonalia.nordiskBorger.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(jsonPersonalia.nordiskBorger.verdi).isTrue
    }

    @Test
    fun skalPrioritereNordiskStatsborgerskap() {
        val person = Person(
            fornavn = FORNAVN,
            mellomnavn = MELLOMNAVN,
            etternavn = ETTERNAVN,
            fnr = EIER,
            sivilstatus = "ugift",
            statsborgerskap = listOf(
                IKKE_NORDISK_STATSBORGERSKAP,
                NORDISK_STATSBORGERSKAP
            ),
            ektefelle = null,
            bostedsadresse = null,
            oppholdsadresse = null,
        )
        val soknadUnderArbeid = defaultSoknadUnderArbeid
        every { personService.hentPerson(any()) } returns person

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonPersonalia = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia
        assertThat(jsonPersonalia.statsborgerskap.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(jsonPersonalia.statsborgerskap.verdi).isEqualTo(NORDISK_STATSBORGERSKAP)
        assertThat(jsonPersonalia.nordiskBorger.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(jsonPersonalia.nordiskBorger.verdi).isTrue
    }

    @Test
    fun skalOppdatereIkkeNordiskPersonalia() {
        val person = Person(
            fornavn = FORNAVN,
            mellomnavn = MELLOMNAVN,
            etternavn = ETTERNAVN,
            fnr = EIER,
            sivilstatus = "ugift",
            statsborgerskap = listOf(
                IKKE_NORDISK_STATSBORGERSKAP
            ),
            ektefelle = null,
            bostedsadresse = null,
            oppholdsadresse = null,
        )
        val soknadUnderArbeid = defaultSoknadUnderArbeid
        every { personService.hentPerson(any()) } returns person

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonPersonalia = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia
        assertThat(jsonPersonalia.personIdentifikator.kilde).isEqualTo(JsonPersonIdentifikator.Kilde.SYSTEM)
        assertThat(jsonPersonalia.personIdentifikator.verdi).isEqualTo(EIER)
        assertThat(jsonPersonalia.navn.kilde).isEqualTo(JsonSokernavn.Kilde.SYSTEM)
        assertThat(jsonPersonalia.navn.fornavn).isEqualTo(FORNAVN)
        assertThat(jsonPersonalia.navn.mellomnavn).isEqualTo(MELLOMNAVN)
        assertThat(jsonPersonalia.navn.etternavn).isEqualTo(ETTERNAVN)
        assertThat(jsonPersonalia.statsborgerskap.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(jsonPersonalia.statsborgerskap.verdi).isEqualTo(IKKE_NORDISK_STATSBORGERSKAP)
        assertThat(jsonPersonalia.nordiskBorger.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(jsonPersonalia.nordiskBorger.verdi).isFalse
    }

    @Test
    fun skalikkeSendeMedStatsborgerskapForUkjent() {
        val person = Person(
            fornavn = FORNAVN,
            mellomnavn = MELLOMNAVN,
            etternavn = ETTERNAVN,
            fnr = EIER,
            sivilstatus = "ugift",
            statsborgerskap = listOf(BasisPersonaliaSystemdata.PDL_UKJENT_STATSBORGERSKAP),
            ektefelle = null,
            bostedsadresse = null,
            oppholdsadresse = null,
        )
        val soknadUnderArbeid = defaultSoknadUnderArbeid
        every { personService.hentPerson(any()) } returns person

        basisPersonaliaSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonPersonalia = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia
        assertThat(jsonPersonalia.statsborgerskap).isNull()
        assertThat(jsonPersonalia.nordiskBorger).isNull()
    }

    @Test
    fun skalSetteRiktigNordiskBorger() {
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger(null)).isNull()
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("NOR")).isTrue
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("SWE")).isTrue
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("FRO")).isTrue
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("ISL")).isTrue
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("DNK")).isTrue
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("FIN")).isTrue
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("RUS")).isFalse
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("DEU")).isFalse
        assertThat(BasisPersonaliaSystemdata.erNordiskBorger("GBR")).isFalse
    }

    companion object {
        private const val EIER = "12345678901"
        private const val FORNAVN = "Aragorn"
        private const val MELLOMNAVN = "Elessar"
        private const val ETTERNAVN = "Telcontar"
        private const val NORSK_STATSBORGERSKAP = "NOR"
        private const val NORDISK_STATSBORGERSKAP = "FIN"
        private const val IKKE_NORDISK_STATSBORGERSKAP = "GER"
    }
}
