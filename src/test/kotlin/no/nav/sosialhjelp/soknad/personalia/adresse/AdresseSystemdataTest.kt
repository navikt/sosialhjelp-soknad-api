package no.nav.sosialhjelp.soknad.personalia.adresse

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.innsending.OldSoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseService
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.domain.KartverketMatrikkelAdresse
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Bostedsadresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Matrikkeladresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Oppholdsadresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.personalia.person.domain.Vegadresse
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class AdresseSystemdataTest {

    private val personService: PersonService = mockk()
    private val hentAdresseService: HentAdresseService = mockk()
    private val adresseSystemdata = AdresseSystemdata(personService, hentAdresseService)

    @Test
    fun skalOppdatereFolkeregistrertAdresse_vegadresse_fraPdl() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        val personWithBostedsadresseVegadresse =
            createPersonWithBostedsadresse(Bostedsadresse("", DEFAULT_VEGADRESSE, null))
        every { personService.hentPerson(any()) } returns personWithBostedsadresseVegadresse

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val folkeregistrertAdresse = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.folkeregistrertAdresse
        val bostedsadresseVegadresse = personWithBostedsadresseVegadresse.bostedsadresse?.vegadresse
        assertThat(folkeregistrertAdresse.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(folkeregistrertAdresse.type).isEqualTo(JsonAdresse.Type.GATEADRESSE)
        assertThatVegadresseIsCorrectlyConverted(bostedsadresseVegadresse, folkeregistrertAdresse)
    }

    @Test
    fun `skal oppdatere folkeregistrert matrikkeladresse - uten utfyllende matrikkeladresse`() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        val personWithBostedsadresseMatrikkeladresse = createPersonWithBostedsadresse(
            Bostedsadresse(
                "",
                null,
                Matrikkeladresse(
                    "matrikkelId",
                    "postnummer",
                    "poststed",
                    "tilleggsnavn",
                    "kommunenummer",
                    "bruksenhetsnummer"
                )
            )
        )
        every { personService.hentPerson(any()) } returns personWithBostedsadresseMatrikkeladresse
        every { hentAdresseService.hentKartverketMatrikkelAdresse(any()) } returns null

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val folkeregistrertAdresse = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.folkeregistrertAdresse
        assertThat(folkeregistrertAdresse.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(folkeregistrertAdresse.type).isEqualTo(JsonAdresse.Type.MATRIKKELADRESSE)
        val matrikkeladresse = folkeregistrertAdresse as JsonMatrikkelAdresse
        val bostedsadresse = personWithBostedsadresseMatrikkeladresse.bostedsadresse?.matrikkeladresse
        assertThat(matrikkeladresse.kommunenummer).isEqualTo(bostedsadresse?.kommunenummer)
        assertThat(matrikkeladresse.gaardsnummer).isNull()
        assertThat(matrikkeladresse.bruksnummer).isEqualTo(bostedsadresse?.bruksenhetsnummer)
        assertThat(matrikkeladresse.festenummer).isNull()
        assertThat(matrikkeladresse.seksjonsnummer).isNull()
        assertThat(matrikkeladresse.undernummer).isNull()
    }

    @Test
    fun `skal oppdatere folkeregistrert matrikkeladresse - med utfyllende matrikkeladresse`() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        val personWithBostedsadresseMatrikkeladresse = createPersonWithBostedsadresse(
            Bostedsadresse(
                "",
                null,
                Matrikkeladresse(
                    "matrikkelId",
                    "postnummer",
                    "poststed",
                    "tilleggsnavn",
                    "kommunenummer",
                    "bruksenhetsnummer"
                )
            )
        )

        val kartverketMatrikkelAdresse = KartverketMatrikkelAdresse(
            kommunenummer = "0301",
            gaardsnummer = "gaardsnummer",
            bruksnummer = "H0101",
            festenummer = "F4",
            seksjonsunmmer = null,
            undernummer = "under1",
            bydelsnummer = "030107"
        )

        every { personService.hentPerson(any()) } returns personWithBostedsadresseMatrikkeladresse
        every { hentAdresseService.hentKartverketMatrikkelAdresse(any()) } returns kartverketMatrikkelAdresse

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val folkeregistrertAdresse = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.folkeregistrertAdresse
        assertThat(folkeregistrertAdresse.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(folkeregistrertAdresse.type).isEqualTo(JsonAdresse.Type.MATRIKKELADRESSE)
        val matrikkeladresse = folkeregistrertAdresse as JsonMatrikkelAdresse
        assertThat(matrikkeladresse.kommunenummer).isEqualTo(kartverketMatrikkelAdresse.kommunenummer)
        assertThat(matrikkeladresse.gaardsnummer).isEqualTo(kartverketMatrikkelAdresse.gaardsnummer)
        assertThat(matrikkeladresse.bruksnummer).isEqualTo(kartverketMatrikkelAdresse.bruksnummer)
        assertThat(matrikkeladresse.festenummer).isEqualTo(kartverketMatrikkelAdresse.festenummer)
        assertThat(matrikkeladresse.seksjonsnummer).isEqualTo(kartverketMatrikkelAdresse.seksjonsunmmer)
        assertThat(matrikkeladresse.undernummer).isEqualTo(kartverketMatrikkelAdresse.undernummer)
    }

    @Test
    fun skalOppdatereOppholdsadresseOgPostAdresseMedMidlertidigAdresse_oppholdsadresse_fraPdl() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia
            .withOppholdsadresse(JsonAdresse().withAdresseValg(JsonAdresseValg.MIDLERTIDIG))
            .withPostadresse(JsonAdresse().withAdresseValg(JsonAdresseValg.MIDLERTIDIG))
        val personWithOppholdsadresse = createPersonWithBostedsadresseOgOppholdsadresse(
            Bostedsadresse("", DEFAULT_VEGADRESSE, null),
            Oppholdsadresse("", ANNEN_VEGADRESSE)
        )
        every { personService.hentPerson(any()) } returns personWithOppholdsadresse

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val folkeregistrertAdresse = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.folkeregistrertAdresse
        val oppholdsadresse = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.oppholdsadresse
        val postadresse = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.postadresse
        assertThat(folkeregistrertAdresse.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(oppholdsadresse.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(postadresse.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(folkeregistrertAdresse.type).isEqualTo(JsonAdresse.Type.GATEADRESSE)
        assertThat(oppholdsadresse.type).isEqualTo(JsonAdresse.Type.GATEADRESSE)
        assertThat(postadresse.type).isEqualTo(JsonAdresse.Type.GATEADRESSE)
        val bostedsadresseVegadresse = personWithOppholdsadresse.bostedsadresse?.vegadresse
        val oppholdsadresseVegadresse = personWithOppholdsadresse.oppholdsadresse?.vegadresse
        assertThatVegadresseIsCorrectlyConverted(bostedsadresseVegadresse, folkeregistrertAdresse)
        assertThatVegadresseIsCorrectlyConverted(oppholdsadresseVegadresse, oppholdsadresse)
        assertThatVegadresseIsCorrectlyConverted(oppholdsadresseVegadresse, postadresse)
    }

    @Test
    fun skalOppdatereOppholdsadresseOgPostAdresseMedFolkeregistrertAdresse_fraPdl() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia
            .withOppholdsadresse(JsonAdresse().withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT))
            .withPostadresse(JsonAdresse().withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT))
        val personWithBostedsadresseVegadresse =
            createPersonWithBostedsadresse(Bostedsadresse("", DEFAULT_VEGADRESSE, null))
        every { personService.hentPerson(any()) } returns personWithBostedsadresseVegadresse

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val folkeregistrertAdresse = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.folkeregistrertAdresse
        val oppholdsadresse = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.oppholdsadresse
        val postadresse = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.postadresse
        assertThat(folkeregistrertAdresse.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(oppholdsadresse.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(postadresse.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(folkeregistrertAdresse)
            .isEqualTo(oppholdsadresse.withAdresseValg(null))
            .isEqualTo(postadresse)
    }

    @Test
    fun skalIkkeOppdatereOppholdsadresseEllerPostAdresseDersomAdresseValgErNull_fraPdl() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia
            .withOppholdsadresse(JsonAdresse())
            .withPostadresse(JsonAdresse())
        val personWithBostedsadresseVegadresse =
            createPersonWithBostedsadresse(Bostedsadresse("", DEFAULT_VEGADRESSE, null))
        every { personService.hentPerson(any()) } returns personWithBostedsadresseVegadresse
        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid)
        val oppholdsadresse = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.oppholdsadresse
        val postadresse = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.postadresse
        assertThat(postadresse.adresseValg).isNull()
        assertThat(postadresse.type).isNull()
        assertThat(oppholdsadresse.adresseValg).isNull()
        assertThat(oppholdsadresse.type).isNull()
    }

    private fun assertThatVegadresseIsCorrectlyConverted(vegadresse: Vegadresse?, jsonAdresse: JsonAdresse) {
        val gateAdresse = jsonAdresse as JsonGateAdresse
        assertThat(gateAdresse.bolignummer).isEqualTo(vegadresse?.bruksenhetsnummer)
        assertThat(gateAdresse.gatenavn).isEqualTo(vegadresse?.adressenavn)
        assertThat(gateAdresse.husbokstav).isEqualTo(vegadresse?.husbokstav)
        assertThat(gateAdresse.husnummer).isEqualTo(vegadresse?.husnummer.toString())
        assertThat(gateAdresse.kommunenummer).isEqualTo(vegadresse?.kommunenummer)
        assertThat(gateAdresse.landkode).isEqualTo("NOR")
        assertThat(gateAdresse.postnummer).isEqualTo(vegadresse?.postnummer)
        assertThat(gateAdresse.poststed).isEqualTo(vegadresse?.poststed)
    }

    private fun createPersonWithBostedsadresse(bostedsadresse: Bostedsadresse): Person {
        return Person(
            fornavn = "fornavn",
            mellomnavn = "mellomnavn",
            etternavn = "etternavn",
            fnr = EIER,
            sivilstatus = "ugift",
            statsborgerskap = emptyList(),
            ektefelle = null,
            bostedsadresse = bostedsadresse,
            oppholdsadresse = null,
        )
    }

    private fun createPersonWithBostedsadresseOgOppholdsadresse(
        bostedsadresse: Bostedsadresse,
        oppholdsadresse: Oppholdsadresse
    ): Person {
        return Person(
            fornavn = "fornavn",
            mellomnavn = "mellomnavn",
            etternavn = "etternavn",
            fnr = EIER,
            sivilstatus = "ugift",
            statsborgerskap = emptyList(),
            ektefelle = null,
            bostedsadresse = bostedsadresse,
            oppholdsadresse = oppholdsadresse,
        )
    }

    companion object {
        private const val EIER = "12345678901"
        private val DEFAULT_VEGADRESSE = Vegadresse(
            adressenavn = "gateveien",
            husnummer = 1,
            husbokstav = "A",
            tilleggsnavn = "",
            postnummer = "0123",
            poststed = "poststed",
            kommunenummer = "0301",
            bruksenhetsnummer = "H0101",
            bydelsnummer = "123456"
        )
        private val ANNEN_VEGADRESSE = Vegadresse(
            adressenavn = "en annen sti",
            husnummer = 32,
            husbokstav = null,
            tilleggsnavn = null,
            postnummer = "0456",
            poststed = "oslo",
            kommunenummer = "0302",
            bruksenhetsnummer = null,
            bydelsnummer = null
        )

        private fun createSoknadUnderArbeid(): SoknadUnderArbeid {
            return SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = "BEHANDLINGSID",
                tilknyttetBehandlingsId = null,
                eier = EIER,
                jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER),
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now()
            )
        }
    }
}
