package no.nav.sosialhjelp.soknad.api.saksoversikt

import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.EttersendingerRespons
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.InnsendteSoknaderRespons
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.PabegynteSoknaderRespons
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.PingRespons
import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.web.saml.SamlSubjectHandler
import no.nav.sosialhjelp.soknad.web.service.SaksoversiktMetadataService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

/**
 * Eksponerer metadata om brukers søknader for bruk i Saksoversikt
 * Implementerer speccen definert i soeknadsskjemasosialhjelp-v1-saksoversiktdefinisjon
 */
@Controller
@Unprotected // Er sikret med SAML frem til team personbruker er klare med å bytte ut SAML med OIDC
@Path("/metadata")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class SaksoversiktMetadataRessurs(
    private val saksoversiktMetadataService: SaksoversiktMetadataService
) {
    @GET
    @Path("/innsendte")
    open fun hentInnsendteSoknaderForBruker(): InnsendteSoknaderRespons {
        val fnr = SamlSubjectHandler.getUserId()
        logger.debug("Henter metadata for innsendte soknader uten oidc")
        val innsendteSoknader = saksoversiktMetadataService.hentInnsendteSoknaderForFnr(fnr)
        return InnsendteSoknaderRespons()
            .withInnsendteSoknader(innsendteSoknader)
    }

    @GET
    @Path("/ettersendelse")
    open fun hentSoknaderBrukerKanEttersendePa(): EttersendingerRespons {
        val fnr = SamlSubjectHandler.getUserId()
        logger.debug("Henter metadata for ettersendelse uten oidc")
        val ettersendingsSoknader = saksoversiktMetadataService.hentSoknaderBrukerKanEttersendePa(fnr)
        return EttersendingerRespons()
            .withEttersendingsSoknader(ettersendingsSoknader)
    }

    @GET
    @Path("/pabegynte")
    open fun hentPabegynteSoknaderForBruker(): PabegynteSoknaderRespons {
        val fnr = SamlSubjectHandler.getUserId()
        logger.debug("Henter metadata for pabegynte uten oidc")
        val pabegynte = saksoversiktMetadataService.hentPabegynteSoknaderForBruker(fnr)
        return PabegynteSoknaderRespons()
            .withPabegynteSoknader(pabegynte)
    }

    @GET
    @Path("/ping")
    open fun ping(): PingRespons {
        logger.debug("Ping for saksoversikt uten oidc")
        return PingRespons()
            .withStatus(PingRespons.Status.OK)
            .withMelding("Sosialhjelp Saksoversikt API er oppe")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SaksoversiktMetadataRessurs::class.java)
    }
}
