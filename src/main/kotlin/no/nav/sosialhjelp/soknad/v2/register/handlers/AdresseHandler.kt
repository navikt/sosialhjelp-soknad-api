package no.nav.sosialhjelp.soknad.v2.register.handlers

import java.util.UUID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseService
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktService
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataHandler
import no.nav.sosialhjelp.soknad.v2.shadow.adapter.V2AdresseAdapter.toV2Adresse
import org.springframework.stereotype.Component

@Component
class AdresseHandler(
    private val personService: PersonService,
    private val kontaktService: KontaktService,
    private val hentAdresseService: HentAdresseService,
): RegisterDataHandler {
    private val logger by logger()

    override fun fetchAndSave(soknadId: UUID) {
        personService
            .hentPerson(getUserIdFromToken())
            ?.let {
                kontaktService.saveAdresserRegister(
                    soknadId = soknadId,
                    folkeregistrertAdresse = it.bostedsadresse?.toV2Adresse(hentAdresseService),
                    midlertidigAdresse = it.oppholdsadresse?.toV2Adresse()
                )
            } ?: logger.error("Fant ikke person ved henting av register-data")
    }
}