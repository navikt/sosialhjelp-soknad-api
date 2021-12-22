package no.nav.sosialhjelp.soknad.api.dittnav

import no.finn.unleash.Unleash
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.api.dittnav.dto.MarkerPabegyntSoknadSomLestDto
import no.nav.sosialhjelp.soknad.api.dittnav.dto.PabegyntSoknadDto
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_3
import no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4
import no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING
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
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class DittNavMetadataRessurs(
    private val dittNavMetadataService: DittNavMetadataService,
    private val unleash: Unleash
) {
    @GET
    @Path("/pabegynte/aktive")
    open fun hentPabegynteSoknaderForBruker(): List<PabegyntSoknadDto> {
        if (!unleash.isEnabled(DITTNAV_PABEGYNTE_ENDEPUNKT_ENABLED, false)) {
            log.info("Endepunkt for å hente info om påbegynte søknader for dittNav er ikke enabled. Returnerer tom liste.")
            return emptyList()
        }
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        return dittNavMetadataService.hentAktivePabegynteSoknader(fnr)
    }

    @GET
    @Path("/pabegynte/inaktive")
    open fun hentPabegynteSoknaderForBrukerLestDittNav(): List<PabegyntSoknadDto> {
        if (!unleash.isEnabled(DITTNAV_PABEGYNTE_ENDEPUNKT_ENABLED, false)) {
            log.info("Endepunkt for å hente info om påbegynte søknader for dittNav er ikke enabled. Returnerer tom liste.")
            return emptyList()
        }
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        return dittNavMetadataService.hentInaktivePabegynteSoknader(fnr)
    }

    @POST
    @Path("/pabegynte/lest")
    open fun oppdaterLestDittNavForPabegyntSoknad(@RequestBody dto: MarkerPabegyntSoknadSomLestDto): Boolean {
        if (!unleash.isEnabled(DITTNAV_PABEGYNTE_ENDEPUNKT_ENABLED, false)) {
            log.info("Endepunkt for å oppdatere lestDittNav for påbegynt søknad er ikke enabled. Returnerer false.")
            return false
        }
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        val behandlingsId = dto.grupperingsId
        val somLest = dittNavMetadataService.oppdaterLestDittNavForPabegyntSoknad(behandlingsId, fnr)
        log.info("Pabegynt søknad med behandlingsId={} har fått lestDittNav={}", behandlingsId, somLest)
        return somLest
    }

    companion object {
        private val log = LoggerFactory.getLogger(DittNavMetadataRessurs::class.java)
        private const val DITTNAV_PABEGYNTE_ENDEPUNKT_ENABLED = "sosialhjelp.soknad.dittnav-pabegynte-endepunkt-enabled"
    }
}
