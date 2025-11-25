package no.nav.sosialhjelp.soknad.v2.register.fetchers

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataFetcher
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as personId

interface PersonRegisterDataHandler {
    fun fetchAndSave(
        soknadId: UUID,
        person: Person,
    )

    fun continueOnError(): Boolean = true
}

// Sørger for at denne mapperen er den første som kjører
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class PersonDataFetcher(
    private val personService: PersonService,
    private val personRegisterDataHandlers: List<PersonRegisterDataHandler>,
) : RegisterDataFetcher {
    private val logger by logger()

    override fun fetchAndSave(soknadId: UUID) {
        logger.info("Henter person i PDL")

        personService.hentPerson(personId())
            ?.also { it.verifyOver18() }
            ?.let { person ->
                personRegisterDataHandlers
                    .forEach { personDataHandler ->
                        runCatching { personDataHandler.fetchAndSave(soknadId, person) }
                            .onFailure {
                                logger.warn("Feil i PersonData-fetcher: $personDataHandler", it)
                                if (!personDataHandler.continueOnError()) throw it
                            }
                    }
            }
            ?: error("Fant ikke søker i PDL")
    }

    // En Exception i denne logikken skal avbryte alt
    override fun exceptionOnError(): Boolean = true
}

private fun Person.verifyOver18() {
    requireNotNull(fodselsdato) { "Fant ikke fødselsdato" }
    if (fodselsdato.isAfter(LocalDate.now().minusYears(18))) throw SokerUnder18Exception()
}

class SokerUnder18Exception() : SosialhjelpSoknadApiException("Søker er under 18 år")
