package no.nav.sosialhjelp.soknad.v2.register.handlers

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataFetcher
import no.nav.sosialhjelp.soknad.v2.register.handlers.person.RegisterDataPersonFetcher
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Sørger for at denne mapperen er den første som kjører
class PersonHandler(
    private val personService: PersonService,
    private val registerDataPersonFetchers: List<RegisterDataPersonFetcher>,
) : RegisterDataFetcher {
    private val log by logger()

    override fun fetchAndSave(soknadId: UUID) {
        personService.hentPerson(getUserIdFromToken())?.let { person ->
            registerDataPersonFetchers.forEach { it.fetchAndSave(soknadId, person) }
        }
            ?: log.error("Fant ikke person i PDL")
    }
}
