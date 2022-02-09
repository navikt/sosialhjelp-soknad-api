package no.nav.sosialhjelp.soknad.api.saksoversikt

import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.EttersendingerRespons
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.InnsendteSoknaderRespons
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.PabegynteSoknaderRespons
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.PingRespons
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.common.Constants.CLAIM_ACR_LEVEL_3
import no.nav.sosialhjelp.soknad.common.Constants.CLAIM_ACR_LEVEL_4
import no.nav.sosialhjelp.soknad.common.Constants.SELVBETJENING
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
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
@ProtectedWithClaims(issuer = SELVBETJENING, combineWithOr = true, claimMap = [CLAIM_ACR_LEVEL_3, CLAIM_ACR_LEVEL_4])
@Path("/metadata/oidc")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class SaksoversiktMetadataOidcRessurs(
    private val saksoversiktMetadataService: SaksoversiktMetadataService
) {
    @GET
    @Path("/innsendte")
    open fun hentInnsendteSoknaderForBruker(): InnsendteSoknaderRespons {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        logger.debug("Henter metadata for innsendte soknader med oidc")
        val innsendteSoknader = saksoversiktMetadataService.hentInnsendteSoknaderForFnr(fnr)
        return InnsendteSoknaderRespons()
            .withInnsendteSoknader(innsendteSoknader)
    }

    @GET
    @Path("/ettersendelse")
    open fun hentSoknaderBrukerKanEttersendePa(): EttersendingerRespons {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        logger.debug("Henter metadata for ettersendelse med oidc")
        val ettersendingsSoknader = saksoversiktMetadataService.hentSoknaderBrukerKanEttersendePa(fnr)
        return EttersendingerRespons()
            .withEttersendingsSoknader(ettersendingsSoknader)
    }

    @GET
    @Path("/pabegynte")
    open fun hentPabegynteSoknaderForBruker(): PabegynteSoknaderRespons {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        logger.debug("Henter metadata for pabegynte med oidc")
        return try {
            val pabegynte = saksoversiktMetadataService.hentPabegynteSoknaderForBruker(fnr)
            PabegynteSoknaderRespons()
                .withPabegynteSoknader(pabegynte)
        } catch (e: Exception) {
            logger.error("Uthenting av påbegynte søknader feilet. Var fnr tom? {}", fnr.isBlank(), e)
            throw e
        }
    }

    @GET
    @Unprotected
    @Path("/ping")
    open fun ping(): PingRespons {
        logger.debug("Ping for saksoversikt med oidc")
        return PingRespons()
            .withStatus(PingRespons.Status.OK)
            .withMelding("Sosialhjelp Saksoversikt API er oppe")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SaksoversiktMetadataOidcRessurs::class.java)
    }
}
