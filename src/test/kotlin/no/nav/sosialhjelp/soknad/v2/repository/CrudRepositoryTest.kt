package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.createFamilie
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.v2.opprettEier
import no.nav.sosialhjelp.soknad.v2.opprettKontakt
import no.nav.sosialhjelp.soknad.v2.opprettLivssituasjon
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Formålet med testklassen:
 * 1. Sjekke at domenemodell samsvarer med database-tabeller
 * 2. Sjekke at "delete on cascade" i databasen fungerer som forventet
 * 3. Sjekke at fremmednøkkel er korrekt koblet til eksisterende soknad
 */
class CrudRepositoryTest : AbstractGenericRepositoryTest() {

    @Test
    fun `CRUD Soknad`() {
        // for "rot"-objektet vil det ikke være constraints som må testes
        UUID.randomUUID().let {
            soknadRepository.save(opprettSoknad(it))
            soknadRepository.save(opprettSoknad(it).copy(eierPersonId = "NOE ANNET"))
        }
    }

    @Test
    fun `CRUD Livssituasjon`() {
        livssituasjonRepository.runCrudOperations(
            opprettLivssituasjon(soknad.id),
            opprettLivssituasjon(soknad.id).copy(bosituasjon = Bosituasjon(antallHusstand = 999))
        )
    }

    @Test
    fun `CRUD Eier`() {
        eierRepository.runCrudOperations(
            opprettEier(soknad.id),
            opprettEier(soknad.id).copy(statsborgerskap = "SPANSK")
        )
    }

    @Test
    fun `CRUD Kontakt`() {
        kontaktRepository.runCrudOperations(
            opprettKontakt(soknad.id),
            opprettKontakt(soknad.id).copy(telefonnummer = Telefonnummer(fraBruker = "99221199"))
        )
    }

    @Test
    fun `CRUD Familie`() {
        familieRepository.runCrudOperations(
            createFamilie(soknad.id),
            createFamilie(soknad.id).copy(sivilstatus = null)
        )
    }
}
