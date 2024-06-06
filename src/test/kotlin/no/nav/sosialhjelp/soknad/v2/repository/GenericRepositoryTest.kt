package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.createFamilie
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.v2.opprettEier
import no.nav.sosialhjelp.soknad.v2.opprettIntegrasjonstatus
import no.nav.sosialhjelp.soknad.v2.opprettKontakt
import no.nav.sosialhjelp.soknad.v2.opprettLivssituasjon
import no.nav.sosialhjelp.soknad.v2.opprettOkonomi
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.opprettVedlegg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Formålet med testklassen:
 * 1. Sjekke at domenemodell samsvarer med database-tabeller
 * 2. Sjekke at "delete on cascade" i databasen fungerer som forventet
 * 3. Sjekke at fremmednøkkel er korrekt koblet til eksisterende soknad
 */
class GenericRepositoryTest : AbstractGenericRepositoryTest() {
    @Test
    fun `Verifisere CRUD-operasjoner for Soknad`() {
        // for "rot"-objektet vil det ikke være constraints som må testes
        UUID.randomUUID().let {
            val original = soknadRepository.save(opprettSoknad(it))
            val updated = soknadRepository.save(opprettSoknad(it).copy(eierPersonId = "NOE ANNET"))
            assertThat(original).isNotEqualTo(updated)
        }
    }

    @Test
    fun `Verifisere CRUD-operasjoner for Livssituasjon`() {
        livssituasjonRepository.verifyCRUDOperations(
            originalEntity = opprettLivssituasjon(soknad.id),
            updatedEntity = opprettLivssituasjon(soknad.id).copy(bosituasjon = Bosituasjon(antallHusstand = 999)),
        )
    }

    @Test
    fun `Verifisere CRUD-operasjoner for Eier`() {
        eierRepository.verifyCRUDOperations(
            originalEntity = opprettEier(soknad.id),
            updatedEntity = opprettEier(soknad.id).copy(statsborgerskap = "SPANSK"),
        )
    }

    @Test
    fun `Hente eiers personId skal returnere eierPersonId fra Soknad`() {
        val eier = eierRepository.save(opprettEier(soknad.id))
        assertThat(eierRepository.getEierPersonId(eier.soknadId)).isEqualTo(soknad.eierPersonId)
    }

    @Test
    fun `Verifisere CRUD-operasjoner for Kontakt`() {
        kontaktRepository.verifyCRUDOperations(
            originalEntity = opprettKontakt(soknad.id),
            updatedEntity = opprettKontakt(soknad.id).copy(telefonnummer = Telefonnummer(fraBruker = "99221199")),
        )
    }

    @Test
    fun `Verifisere CRUD-operasjoner for Familie`() {
        familieRepository.verifyCRUDOperations(
            originalEntity = createFamilie(soknad.id),
            updatedEntity =
                createFamilie(soknad.id)
                    .copy(sivilstatus = null),
        )
    }

    @Test
    fun `Verifisere CRUD-operasjoner for Integrasjonstatus`() {
        integrasjonstatusRepository.verifyCRUDOperations(
            originalEntity = opprettIntegrasjonstatus(soknad.id),
            updatedEntity = opprettIntegrasjonstatus(soknad.id).copy(feilUtbetalingerNav = true),
        )
    }

    @Test
    fun `Verifisere CRUD-operasjoner for Okonomi`() {
        okonomiRepository.verifyCRUDOperations(
            originalEntity = opprettOkonomi(soknad.id),
            updatedEntity = opprettOkonomi(soknad.id).copy(utgifter = emptySet()),
        )
    }

    @Test
    fun `Verifisere CRUD-operasjoner for Dokumentasjon`() {
        val dbId = UUID.randomUUID()
        dokumentasjonRepository.verifyCRUDOperations(
            originalEntity = opprettVedlegg(dbId, soknad.id),
            updatedEntity = opprettVedlegg(dbId, soknad.id).copy(status = DokumentasjonStatus.LASTET_OPP),
        )
    }
}
