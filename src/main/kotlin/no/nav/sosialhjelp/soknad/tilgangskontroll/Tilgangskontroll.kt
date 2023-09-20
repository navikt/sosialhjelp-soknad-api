package no.nav.sosialhjelp.soknad.tilgangskontroll

import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadAlleredeSendtException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.FERDIG
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.Objects

@Component
class Tilgangskontroll(
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val personService: PersonService,
) {
    /**
     * Sjekker at bruker er eier av søknad med gitt behandlingsId.
     * @param behandlingsId behandlingsId til søknad
     * @return fnr/dnr for brukerøkt
     */
    fun verifiserBrukerForSoknad(behandlingsId: String): String {
        val user = getUserIdFromToken()

        val metadata = soknadMetadataRepository.hent(behandlingsId)

        if (metadata?.status in listOf(FERDIG, SENDT_MED_DIGISOS_API))
            throw SoknadAlleredeSendtException("Søknad $behandlingsId har allerede blitt sendt inn.")

        if (metadata?.fnr != user) throw AuthorizationException("Bruker har ikke tilgang til søknaden.")

        verifyXsrfIfRequestUnsafe(behandlingsId)
        verifiserAtBrukerIkkeHarAdressebeskyttelse(user)

        return user
    }

    fun verifiserBrukerHarTilgangTilMetadata(behandlingsId: String?) {
        var eier = "undefined"
        try {
            val metadata = soknadMetadataRepository.hent(behandlingsId)
            metadata?.fnr?.let { eier = it }
        } catch (e: Exception) {
            logger.warn("Kunne ikke avgjøre hvem som eier søknad med behandlingsId $behandlingsId -> Ikke tilgang.", e)
        }
        verifiserAtInnloggetBrukerErEierAvSoknad(eier)
    }

    private fun verifiserAtInnloggetBrukerErEierAvSoknad(eier: String) {
        val fnr = getUserIdFromToken()
        if (fnr != eier) {
            throw AuthorizationException("Fnr stemmer ikke overens med eieren til søknaden")
        }
        verifiserAtBrukerIkkeHarAdressebeskyttelse(fnr)
    }

    fun verifiserAtBrukerHarTilgang(): String {
        val fnr = getUserIdFromToken()
        if (Objects.isNull(fnr)) {
            throw AuthorizationException("Ingen tilgang når fnr ikke er satt")
        }
        verifiserAtBrukerIkkeHarAdressebeskyttelse(fnr)
        return fnr
    }

    private fun verifiserAtBrukerIkkeHarAdressebeskyttelse(ident: String) {
        if (personService.hentAdressebeskyttelse(ident) in listOf(
                Gradering.FORTROLIG,
                Gradering.STRENGT_FORTROLIG,
                Gradering.STRENGT_FORTROLIG_UTLAND
            )
        ) {
            throw AuthorizationException("Bruker har ikke tilgang til søknaden.")
        }
    }

    /**
     * Sjekker XSRF-token dersom forespørselen ikke er safe (GET, HEAD, OPTIONS).
     *
     * @see XsrfGenerator.sjekkXsrfToken
     * @param behandlingsId behandlingsId for søknad
     * @throws AuthorizationException dersom XSRF-token ikke stemmer
     * @todo Fjern behandlingsId; XSRF-token skal være for brukerøkt, ikke behandlingsId
     */
    private fun verifyXsrfIfRequestUnsafe(behandlingsId: String) {
        // Check whether we have an active request. If not, we are probably running a test.
        try {
            RequestContextHolder.currentRequestAttributes()
        } catch (e: IllegalStateException) {
            logger.warn("Ingen request, antas å være en test.")
            return
        }

        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request

        when (request.method) {
            "GET", "HEAD", "OPTIONS" -> return
            else -> XsrfGenerator.sjekkXsrfToken(
                request.getHeader("X-XSRF-TOKEN"),
                behandlingsId,
                MiljoUtils.isMockAltProfil()
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Tilgangskontroll::class.java)
    }
}
