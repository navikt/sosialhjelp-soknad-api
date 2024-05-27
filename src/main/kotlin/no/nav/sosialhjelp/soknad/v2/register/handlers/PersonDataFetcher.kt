package no.nav.sosialhjelp.soknad.v2.register.handlers

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataFetcher
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.UUID

interface PersonRegisterDataFetcher {
    fun fetchAndSave(
        soknadId: UUID,
        person: Person,
    )
}

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Sørger for at denne mapperen er den første som kjører
class PersonDataFetcher(
    private val personService: PersonService,
    private val personRegisterDataFetchers: List<PersonRegisterDataFetcher>,
) : RegisterDataFetcher {
    private val logger by logger()

    override fun fetchAndSave(soknadId: UUID) {
        logger.info("NyModell: Register: Henter søker i PDL")

        personService.hentPerson(getUserIdFromToken())?.let { person ->
            personRegisterDataFetchers.forEach { it.fetchAndSave(soknadId, person) }
        }
            ?: logger.error("Fant ikke søker i PDL")
    }
}
