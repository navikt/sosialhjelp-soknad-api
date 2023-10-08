package no.nav.sosialhjelp.soknad.ettersending

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.repository.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.ettersending.dto.EttersendtVedlegg
import no.nav.sosialhjelp.soknad.ettersending.innsendtsoknad.BehandlingsKjede
import no.nav.sosialhjelp.soknad.ettersending.innsendtsoknad.InnsendtSoknadService
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggMapper
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true)
@RequestMapping("/ettersendelse", produces = [MediaType.APPLICATION_JSON_VALUE])
class EttersendingRessurs(
    private val innsendtSoknadService: InnsendtSoknadService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val opplastetVedleggRepository: OpplastetVedleggRepository,
    private val tilgangskontroll: Tilgangskontroll
) {
    @GetMapping("/innsendte/{behandlingsId}")
    fun hentBehandlingskjede(@PathVariable("behandlingsId") behandlingsId: String): BehandlingsKjede {
        tilgangskontroll.verifiserBrukerHarTilgangTilMetadata(behandlingsId)
        return innsendtSoknadService.hentBehandlingskjede(behandlingsId)
    }

    @GetMapping("/ettersendteVedlegg/{behandlingsId}")
    fun hentVedlegg(@PathVariable("behandlingsId") behandlingsId: String): List<EttersendtVedlegg> {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(soknadUnderArbeid.soknadId, eier)
        val originaleVedlegg = soknadUnderArbeid.jsonInternalSoknad?.vedlegg?.vedlegg ?: emptyList()
        val innsendingstidspunkt = innsendtSoknadService.getInnsendingstidspunkt(behandlingsId)
        return VedleggMapper.mapVedleggToSortedListOfEttersendteVedlegg(
            innsendingstidspunkt,
            opplastedeVedlegg,
            originaleVedlegg
        )
    }
}
