package no.nav.sosialhjelp.soknad.ettersending

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.ettersending.dto.EttersendtVedlegg
import no.nav.sosialhjelp.soknad.ettersending.innsendtsoknad.BehandlingsKjede
import no.nav.sosialhjelp.soknad.ettersending.innsendtsoknad.InnsendtSoknadService
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggMapper
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.web.utils.Constants
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/ettersendelse")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class EttersendingRessurs(
    private val innsendtSoknadService: InnsendtSoknadService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val opplastetVedleggRepository: OpplastetVedleggRepository,
    private val tilgangskontroll: Tilgangskontroll
) {
    @GET
    @Path("/innsendte/{behandlingsId}")
    open fun hentBehandlingskjede(@PathParam("behandlingsId") behandlingsId: String): BehandlingsKjede {
        tilgangskontroll.verifiserBrukerHarTilgangTilMetadata(behandlingsId)
        return innsendtSoknadService.hentBehandlingskjede(behandlingsId)
    }

    @GET
    @Path("/ettersendteVedlegg/{behandlingsId}")
    open fun hentVedlegg(@PathParam("behandlingsId") behandlingsId: String): List<EttersendtVedlegg> {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(soknadUnderArbeid.soknadId, eier)
        val originaleVedlegg = soknadUnderArbeid.jsonInternalSoknad.vedlegg.vedlegg
        val innsendingstidspunkt = innsendtSoknadService.getInnsendingstidspunkt(behandlingsId)
        return VedleggMapper.mapVedleggToSortedListOfEttersendteVedlegg(
            innsendingstidspunkt,
            opplastedeVedlegg,
            originaleVedlegg
        )
    }
}
