package no.nav.sosialhjelp.soknad.v2.register.handlers

import java.util.UUID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseService
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.familie.FamilieService
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktService
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataHandler
import no.nav.sosialhjelp.soknad.v2.register.handlers.person.HandleEierData
import no.nav.sosialhjelp.soknad.v2.register.handlers.person.PersonDataHandler
import no.nav.sosialhjelp.soknad.v2.register.handlers.person.toV2Adresse
import org.springframework.stereotype.Component

@Component
class HandlePerson(
    private val personService: PersonService,
    private val personDataHandlers: List<PersonDataHandler>
): RegisterDataHandler {
    private val log by logger()

    override fun handle(soknadId: UUID) {

        personService.hentPerson(getUserIdFromToken())?.let { person ->
            personDataHandlers.forEach { it.handle(soknadId, person) }
        }
            ?: log.error("Fant ikke person for $soknadId")
    }
}
