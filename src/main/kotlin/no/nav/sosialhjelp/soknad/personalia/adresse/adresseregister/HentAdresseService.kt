package no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister

import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.domain.KartverketMatrikkelAdresse
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import org.springframework.stereotype.Component

@Component
class HentAdresseService(
    private val hentAdresseClient: HentAdresseClient,
    private val personService: PersonService,
) {
    fun hentKartverketMatrikkelAdresse(matrikkelId: String): KartverketMatrikkelAdresse? {
        return hentAdresseClient.hentMatrikkelAdresse(matrikkelId)
            ?.let {
                KartverketMatrikkelAdresse(
                    kommunenummer = it.matrikkelnummer?.kommunenummer,
                    gaardsnummer = it.matrikkelnummer?.gaardsnummer,
                    bruksnummer = it.matrikkelnummer?.bruksnummer,
                    festenummer = it.matrikkelnummer?.festenummer,
                    seksjonsunmmer = it.matrikkelnummer?.seksjonsnummer,
                    undernummer = it.undernummer,
                    bydelsnummer = it.bydel?.bydelsnummer,
                )
            }
    }

    fun hentKartverketMatrikkelAdresseForInnloggetBruker(): KartverketMatrikkelAdresse? {
        val person = personService.hentPerson(SubjectHandlerUtils.getUserIdFromToken())
        return person?.bostedsadresse?.matrikkeladresse?.matrikkelId?.let { hentKartverketMatrikkelAdresse(it) }
    }
}
