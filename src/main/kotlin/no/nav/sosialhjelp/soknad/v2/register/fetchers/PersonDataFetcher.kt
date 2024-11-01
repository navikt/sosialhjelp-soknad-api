package no.nav.sosialhjelp.soknad.v2.register.fetchers

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

    fun continueOnError(): Boolean = true
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
            personRegisterDataFetchers.forEach { personDataFetcher ->
                runCatching {
                    personDataFetcher.fetchAndSave(soknadId, person)
                }
                    .onFailure {
                        logger.warn("NyModell: Feil i PersonData-fetcher: $personDataFetcher", it)
                        if (!personDataFetcher.continueOnError()) throw it
                    }
            }
        }
            ?: error("Fant ikke søker i PDL")
    }

    // En Exception i denne logikken skal avbryte alt
    override fun continueOnError(): Boolean = false
}
