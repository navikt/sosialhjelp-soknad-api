package no.nav.sosialhjelp.soknad.tilgangskontroll

import no.nav.sosialhjelp.soknad.common.ServiceUtils
import no.nav.sosialhjelp.soknad.common.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
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
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val personService: PersonService,
    private val serviceUtils: ServiceUtils
) {
    fun verifiserAtBrukerKanEndreSoknad(behandlingsId: String?) {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        XsrfGenerator.sjekkXsrfToken(request.getHeader("X-XSRF-TOKEN"), behandlingsId, serviceUtils.isMockAltProfil())
        verifiserBrukerHarTilgangTilSoknad(behandlingsId)
    }

    fun verifiserBrukerHarTilgangTilSoknad(behandlingsId: String?) {
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknadNullable(behandlingsId, getUserIdFromToken())
            ?: throw AuthorizationException("Bruker har ikke tilgang til søknaden.")

        verifiserAtInnloggetBrukerErEierAvSoknad(soknadUnderArbeid.eier)
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

    fun verifiserAtBrukerHarTilgang() {
        val fnr = getUserIdFromToken()
        if (Objects.isNull(fnr)) {
            throw AuthorizationException("Ingen tilgang når fnr ikke er satt")
        }
        verifiserAtBrukerIkkeHarAdressebeskyttelse(fnr)
    }

    private fun verifiserAtBrukerIkkeHarAdressebeskyttelse(ident: String) {
        val adressebeskyttelse = personService.hentAdressebeskyttelse(ident)
        if (Gradering.FORTROLIG == adressebeskyttelse || Gradering.STRENGT_FORTROLIG == adressebeskyttelse || Gradering.STRENGT_FORTROLIG_UTLAND == adressebeskyttelse) {
            throw AuthorizationException("Bruker har ikke tilgang til søknaden.")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Tilgangskontroll::class.java)
    }
}
