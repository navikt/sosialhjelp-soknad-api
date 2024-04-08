package no.nav.sosialhjelp.soknad.tilgangskontroll

import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadAlleredeSendtException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.FERDIG
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Component
class Tilgangskontroll(
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val personService: PersonService,
    private val environment: Environment,
) {
    fun verifiserAtBrukerKanEndreSoknad(behandlingsId: String?) {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        XsrfGenerator.sjekkXsrfToken(
            request.getHeader("X-XSRF-TOKEN"),
            behandlingsId,
            environment.activeProfiles.contains("mock-alt")
        )
        verifiserBrukerHarTilgangTilSoknad(behandlingsId)
    }

    fun verifiserBrukerHarTilgangTilSoknad(behandlingsId: String?) {
        val personId = getUserIdFromToken()
        val soknadStatus = soknadMetadataRepository.hent(behandlingsId)?.status

        if (soknadStatus in listOf(FERDIG, SENDT_MED_DIGISOS_API)) {
            throw SoknadAlleredeSendtException("Søknad $behandlingsId har allerede blitt sendt inn.")
        }

        val soknadEier = soknadUnderArbeidRepository.hentSoknadNullable(behandlingsId, getUserIdFromToken())?.eier
            ?: throw AuthorizationException("Bruker har ikke tilgang til søknaden.")

        if (personId != soknadEier) throw AuthorizationException("Fnr stemmer ikke overens med eieren til søknaden")

        verifiserAtBrukerIkkeHarAdressebeskyttelse(personId)
    }

    fun verifiserBrukerHarTilgangTilMetadata(behandlingsId: String?) {
        val personId = getUserIdFromToken()
        val soknadEier =
            soknadMetadataRepository.hent(behandlingsId)?.fnr ?: AuthorizationException("henting av eier for søknad $behandlingsId feilet, nekter adgang")
        if (personId != soknadEier) throw AuthorizationException("Fnr stemmer ikke overens med eieren til søknaden")
        verifiserAtBrukerIkkeHarAdressebeskyttelse(personId)
    }

    /**
     * Verifiserer at bruker ikke har adressebeskyttelse.
     * Merk: Selve autentiseringen sjekkes allerede på RestController-nivå.
     */
    fun verifiserAtBrukerHarTilgang() = verifiserAtBrukerIkkeHarAdressebeskyttelse(getUserIdFromToken())

    fun verifiserBrukerId(): String {
        val eier = getUserIdFromToken()
        verifiserAtBrukerIkkeHarAdressebeskyttelse(eier)
        return eier
    }

    private fun verifiserAtBrukerIkkeHarAdressebeskyttelse(ident: String) {
        if (personService.harAdressebeskyttelse(ident)) throw AuthorizationException("Bruker har ikke tilgang til søknaden.")
    }
}
