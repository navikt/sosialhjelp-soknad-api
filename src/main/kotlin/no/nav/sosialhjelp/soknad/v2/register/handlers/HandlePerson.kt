package no.nav.sosialhjelp.soknad.v2.register.handlers

import java.util.UUID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataHandler
import no.nav.sosialhjelp.soknad.v2.register.handlers.person.RegisterDataPersonHandler
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Sørger for at denne mapperen er den første som kjører
class HandlePerson(
    private val personService: PersonService,
    private val registerDataPersonHandlers: List<RegisterDataPersonHandler>
): RegisterDataHandler {
    private val log by logger()

    override fun handle(soknadId: UUID) {

        personService.hentPerson(getUserIdFromToken())?.let { person ->
            registerDataPersonHandlers.forEach { it.handle(soknadId, person) }
        }
            ?: log.error("Fant ikke person for $soknadId")
    }
}
