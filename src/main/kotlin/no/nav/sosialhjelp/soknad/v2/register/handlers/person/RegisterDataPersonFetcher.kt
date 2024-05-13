package no.nav.sosialhjelp.soknad.v2.register.handlers.person

import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import java.util.UUID

interface RegisterDataPersonFetcher {
    fun fetchAndSave(
        soknadId: UUID,
        person: Person,
    )
}
