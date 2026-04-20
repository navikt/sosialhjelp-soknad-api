package no.nav.sosialhjelp.soknad.v2.register.fetchers

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadApiErrorType
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.register.PrimaryFetcher
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

interface PersonRegisterDataHandler {
    fun saveData(
        soknadId: UUID,
        person: Person,
    )

    fun continueOnError(): Boolean = true
}

// PrimaryFetcher - er nødvendig å kjøre før "alle andre"
@Component
class PersonDataFetcher(
    private val personService: PersonService,
    private val personRegisterDataHandlers: List<PersonRegisterDataHandler>,
) : PrimaryFetcher {
    private val logger by logger()

    override suspend fun fetchAndSave(
        soknadId: UUID,
    ) {
        logger.info("Henter person i PDL")

        val hentPerson = personService.hentPerson()
        hentPerson
            ?.also { it.verifyOver18() }
            ?.let { person ->
                personRegisterDataHandlers
                    .forEach { personDataHandler ->
                        runCatching { personDataHandler.saveData(soknadId, person) }
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
    if (fodselsdato.isUnder18()) {
        throw AuthorizationException(
            "Søker er under 18",
            SoknadApiErrorType.SokerUnder18,
        )
    }
}

private fun LocalDate.isUnder18(): Boolean = isAfter(LocalDate.now().minusYears(18))
