package no.nav.sosialhjelp.soknad.v2.register.handlers.person

import java.util.UUID
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person

interface PersonDataHandler {
    fun handle(soknadId: UUID, person: Person)
}