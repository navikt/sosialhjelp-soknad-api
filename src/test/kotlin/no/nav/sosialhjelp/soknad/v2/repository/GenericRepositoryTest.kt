package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.createFamilie
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.v2.opprettEier
import no.nav.sosialhjelp.soknad.v2.opprettKontakt
import no.nav.sosialhjelp.soknad.v2.opprettLivssituasjon
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Formålet med testklassen:
 * 1. Sjekke at domenemodell samsvarer med database-tabeller
 * 2. Sjekke at "delete on cascade" i databasen fungerer som forventet
 * 3. Sjekke at fremmednøkkel er korrekt koblet til eksisterende soknad
 */
class GenericRepositoryTest : AbstractGenericRepositoryTest() {

    @Test
    fun `Verifisere relevante CRUD-operasjoner for Soknad`() {
        // for "rot"-objektet vil det ikke være constraints som må testes
        UUID.randomUUID().let {
            soknadRepository.save(opprettSoknad(it))
            soknadRepository.save(opprettSoknad(it).copy(eierPersonId = "NOE ANNET"))
        }
    }

    @Test
    fun `Verifisere relevante CRUD-operasjoner for Livssituasjon`() {
        livssituasjonRepository.verifyCRUDOperations(
            originalEntity = opprettLivssituasjon(soknad.id),
            updatedEntity = opprettLivssituasjon(soknad.id).copy(bosituasjon = Bosituasjon(antallHusstand = 999))
        )
    }

    @Test
    fun `Verifisere relevante CRUD-operasjoner for Eier`() {
        eierRepository.verifyCRUDOperations(
            originalEntity = opprettEier(soknad.id),
            updatedEntity = opprettEier(soknad.id).copy(statsborgerskap = "SPANSK")
        )
    }

    @Test
    fun `Hente eiers personId skal returnere eierPersonId fra Soknad`() {
        val eier = eierRepository.save(opprettEier(soknad.id))
        Assertions.assertThat(eierRepository.getEierPersonId(eier.soknadId)).isEqualTo(soknad.eierPersonId)
    }

    @Test
    fun `Verifisere relevante CRUD-operasjoner for Kontakt`() {
        kontaktRepository.verifyCRUDOperations(
            originalEntity = opprettKontakt(soknad.id),
            updatedEntity = opprettKontakt(soknad.id).copy(telefonnummer = Telefonnummer(fraBruker = "99221199"))
        )
    }

    @Test
    fun `Verifisere relevante CRUD-operasjoner for Familie`() {
        familieRepository.verifyCRUDOperations(
            originalEntity = createFamilie(soknad.id),
            updatedEntity = createFamilie(soknad.id).copy(sivilstatus = null)
        )
    }
}
