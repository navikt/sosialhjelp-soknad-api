package no.nav.sosialhjelp.soknad.v2.register.handlers.person

import java.util.UUID
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person

interface RegisterDataPersonHandler {
    fun handle(soknadId: UUID, person: Person)
}