package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.BehandlingsKjede;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.InnsendtSoknadService;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.rest.mappers.VedleggMapper.mapVedleggToSortedListOfEttersendteVedlegg;


@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = {"acr=Level4"})
@Path("/ettersendelse")
@Timed
@Produces(APPLICATION_JSON)
public class EttersendingRessurs {

    private final InnsendtSoknadService innsendtSoknadService;
    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    private final OpplastetVedleggRepository opplastetVedleggRepository;
    private final Tilgangskontroll tilgangskontroll;

    public EttersendingRessurs(
            InnsendtSoknadService innsendtSoknadService,
            SoknadUnderArbeidRepository soknadUnderArbeidRepository,
            OpplastetVedleggRepository opplastetVedleggRepository,
            Tilgangskontroll tilgangskontroll
    ) {
        this.innsendtSoknadService = innsendtSoknadService;
        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
        this.opplastetVedleggRepository = opplastetVedleggRepository;
        this.tilgangskontroll = tilgangskontroll;
    }

    @GET
    @Path("/innsendte/{behandlingsId}")
    public BehandlingsKjede hentBehandlingskjede(@PathParam("behandlingsId") String behandlingsId) {
        tilgangskontroll.verifiserBrukerHarTilgangTilMetadata(behandlingsId);
        return innsendtSoknadService.hentBehandlingskjede(behandlingsId);
    }

    @GET
    @Path("/ettersendteVedlegg/{behandlingsId}")
    public List<EttersendtVedlegg> hentVedlegg(@PathParam("behandlingsId") String behandlingsId) {
        String eier = SubjectHandler.getUserId();
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        List<OpplastetVedlegg> opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(soknadUnderArbeid.getSoknadId(), eier);
        List<JsonVedlegg> originaleVedlegg = soknadUnderArbeid.getJsonInternalSoknad().getVedlegg().getVedlegg();
        var innsendingstidspunkt = innsendtSoknadService.getInnsendingstidspunkt(behandlingsId);

        return mapVedleggToSortedListOfEttersendteVedlegg(innsendingstidspunkt, opplastedeVedlegg, originaleVedlegg);
    }

}
