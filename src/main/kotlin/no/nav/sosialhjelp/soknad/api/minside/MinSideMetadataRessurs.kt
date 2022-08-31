package no.nav.sosialhjelp.soknad.api.minside

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.api.minside.dto.MarkerPabegyntSoknadSomLestDto
import no.nav.sosialhjelp.soknad.api.minside.dto.PabegyntSoknadDto
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LEVEL_3
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LEVEL_4
import no.nav.sosialhjelp.soknad.app.Constants.SELVBETJENING
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = SELVBETJENING, combineWithOr = true, claimMap = [CLAIM_ACR_LEVEL_3, CLAIM_ACR_LEVEL_4])
@Path("/dittnav")
@Produces(MediaType.APPLICATION_JSON)
open class MinSideMetadataRessurs(
    private val minSideMetadataService: MinSideMetadataService
) {
    @GET
    @Path("/pabegynte/aktive")
    open fun hentPabegynteSoknaderForBruker(): List<PabegyntSoknadDto> {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        return minSideMetadataService.hentAktivePabegynteSoknader(fnr)
    }

    @GET
    @Path("/pabegynte/inaktive")
    open fun hentPabegynteSoknaderForBrukerSomErLest(): List<PabegyntSoknadDto> {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        return minSideMetadataService.hentInaktivePabegynteSoknader(fnr)
    }

    @POST
    @Path("/pabegynte/lest")
    open fun settLestForPabegyntSoknad(@RequestBody dto: MarkerPabegyntSoknadSomLestDto): Boolean {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        val behandlingsId = dto.grupperingsId
        val somLest = minSideMetadataService.oppdaterLestStatusForPabegyntSoknad(behandlingsId, fnr)
        log.info("Pabegynt søknad med behandlingsId=$behandlingsId har fått status lest=$somLest")
        return somLest
    }

    companion object {
        private val log = LoggerFactory.getLogger(MinSideMetadataRessurs::class.java)
    }
}
