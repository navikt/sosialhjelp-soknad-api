//package no.nav.sosialhjelp.soknad.web.rest.ressurser;
//
//import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
//import no.nav.security.token.support.core.api.ProtectedWithClaims;
//import no.nav.sosialhjelp.metrics.aspects.Timed;
//import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
//import no.nav.sosialhjelp.soknad.business.domain.BehandlingsKjede;
//import no.nav.sosialhjelp.soknad.business.service.soknadservice.InnsendtSoknadService;
//import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
//import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
//import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
//import org.springframework.stereotype.Controller;
//
//import javax.ws.rs.GET;
//import javax.ws.rs.Path;
//import javax.ws.rs.PathParam;
//import javax.ws.rs.Produces;
//import java.util.List;
//
//import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
//
//
//@Controller
//@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
//@Path("/ettersendelse")
//@Timed
//@Produces(APPLICATION_JSON)
//public class EttersendingRessurs {
//
//    private final InnsendtSoknadService innsendtSoknadService;
//    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
//    private final OpplastetVedleggRepository opplastetVedleggRepository;
//    private final Tilgangskontroll tilgangskontroll;
//
//    public EttersendingRessurs(
//            InnsendtSoknadService innsendtSoknadService,
//            SoknadUnderArbeidRepository soknadUnderArbeidRepository,
//            OpplastetVedleggRepository opplastetVedleggRepository,
//            Tilgangskontroll tilgangskontroll
//    ) {
//        this.innsendtSoknadService = innsendtSoknadService;
//        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
//        this.opplastetVedleggRepository = opplastetVedleggRepository;
//        this.tilgangskontroll = tilgangskontroll;
//    }
//
//    @GET
//    @Path("/innsendte/{behandlingsId}")
//    public BehandlingsKjede hentBehandlingskjede(@PathParam("behandlingsId") String behandlingsId) {
//        tilgangskontroll.verifiserBrukerHarTilgangTilMetadata(behandlingsId);
//        return innsendtSoknadService.hentBehandlingskjede(behandlingsId);
//    }
//
//    @GET
//    @Path("/ettersendteVedlegg/{behandlingsId}")
//    public List<EttersendtVedlegg> hentVedlegg(@PathParam("behandlingsId") String behandlingsId) {
//        tilgangskontroll.verifiserAtBrukerHarTilgang();
//        String eier = SubjectHandler.getUserId();
//        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
//        List<OpplastetVedlegg> opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(soknadUnderArbeid.getSoknadId(), eier);
//        List<JsonVedlegg> originaleVedlegg = soknadUnderArbeid.getJsonInternalSoknad().getVedlegg().getVedlegg();
//        var innsendingstidspunkt = innsendtSoknadService.getInnsendingstidspunkt(behandlingsId);
//
//        return mapVedleggToSortedListOfEttersendteVedlegg(innsendingstidspunkt, opplastedeVedlegg, originaleVedlegg);
//    }
//
//}
