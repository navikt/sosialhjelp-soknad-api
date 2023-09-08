package no.nav.sosialhjelp.soknad.api.dittnav

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.api.dittnav.dto.MarkerPabegyntSoknadSomLestDto
import no.nav.sosialhjelp.soknad.api.dittnav.dto.PabegyntSoknadDto
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LEVEL_3
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LEVEL_4
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LOA_HIGH
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LOA_SUBSTANTIAL
import no.nav.sosialhjelp.soknad.app.Constants.TOKENX
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = TOKENX, combineWithOr = true, claimMap = [CLAIM_ACR_LEVEL_3, CLAIM_ACR_LEVEL_4, CLAIM_ACR_LOA_HIGH, CLAIM_ACR_LOA_SUBSTANTIAL])
@RequestMapping("/dittnav", produces = [MediaType.APPLICATION_JSON_VALUE])
class DittNavMetadataRessurs(
    private val dittNavMetadataService: DittNavMetadataService
) {
    @GetMapping("/pabegynte/aktive")
    fun hentPabegynteSoknaderForBruker(): List<PabegyntSoknadDto> {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        return dittNavMetadataService.hentAktivePabegynteSoknader(fnr)
    }

    @GetMapping("/pabegynte/inaktive")
    fun hentPabegynteSoknaderForBrukerSomErLest(): List<PabegyntSoknadDto> {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        return dittNavMetadataService.hentInaktivePabegynteSoknader(fnr)
    }

    @PostMapping("/pabegynte/lest")
    fun settLestForPabegyntSoknad(
        @RequestBody dto: MarkerPabegyntSoknadSomLestDto
    ): Boolean {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        val behandlingsId = dto.grupperingsId
        val somLest = dittNavMetadataService.oppdaterLestStatusForPabegyntSoknad(behandlingsId, fnr)
        log.info("Pabegynt søknad med behandlingsId=$behandlingsId har fått status lest=$somLest")
        return somLest
    }

    companion object {
        private val log = LoggerFactory.getLogger(DittNavMetadataRessurs::class.java)
    }
}
