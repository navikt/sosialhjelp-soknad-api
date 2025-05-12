package no.nav.sosialhjelp.soknad.tilgangskontroll

import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadAlleredeSendtException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class Tilgangskontroll(
    private val soknadService: SoknadService,
    private val personService: PersonService,
    private val soknadMetadataService: SoknadMetadataService,
) {
    fun verifiserBrukerHarTilgangTilSoknad(behandlingsId: String?) {
        val personId = getUserIdFromToken()

        soknadMetadataService.getMetadataForSoknad(UUID.fromString(behandlingsId)).status
            .also {
                if (it in listOf(SoknadStatus.SENDT, SoknadStatus.MOTTATT_FSL)) {
                    throw SoknadAlleredeSendtException("Søknad $behandlingsId har allerede blitt sendt inn.")
                }
            }

        val soknadEier =
            behandlingsId?.let { soknadService.getSoknadOrNull(UUID.fromString(it)) }?.eierPersonId
                ?: throw AuthorizationException("Bruker har ikke tilgang til søknaden.")

        if (personId != soknadEier) throw AuthorizationException("Fnr stemmer ikke overens med eieren til søknaden")

        verifiserAtBrukerIkkeHarAdressebeskyttelse(personId)
    }

    /**
     * Verifiserer at bruker ikke har adressebeskyttelse.
     * Merk: Selve autentiseringen sjekkes allerede på RestController-nivå.
     */
    fun verifiserAtBrukerHarTilgang() = verifiserAtBrukerIkkeHarAdressebeskyttelse(getUserIdFromToken())

    private fun verifiserAtBrukerIkkeHarAdressebeskyttelse(ident: String) {
        if (personService.hasAdressebeskyttelse(ident)) throw AuthorizationException("Bruker har ikke tilgang til søknaden.")
    }
}
