//package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte;
//
//import no.finn.unleash.Unleash;
//import no.nav.security.token.support.core.api.ProtectedWithClaims;
//import no.nav.sosialhjelp.metrics.aspects.Timed;
//import no.nav.sosialhjelp.soknad.business.service.dittnav.DittNavMetadataService;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
//import no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto.MarkerPabegyntSoknadSomLestDto;
//import no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto.PabegyntSoknadDto;
//import org.slf4j.Logger;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestBody;
//
//import javax.ws.rs.GET;
//import javax.ws.rs.POST;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import java.util.Collections;
//import java.util.List;
//
//import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_3;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
//import static org.slf4j.LoggerFactory.getLogger;
//
//@Controller
//@ProtectedWithClaims(issuer = SELVBETJENING, combineWithOr = true, claimMap = {CLAIM_ACR_LEVEL_3, CLAIM_ACR_LEVEL_4})
//@Path("/dittnav")
//@Timed
//@Produces(APPLICATION_JSON)
//public class DittNavMetadataRessurs {
//
//    private static final Logger log = getLogger(DittNavMetadataRessurs.class);
//    private static final String DITTNAV_PABEGYNTE_ENDEPUNKT_ENABLED = "sosialhjelp.soknad.dittnav-pabegynte-endepunkt-enabled";
//
//    private final DittNavMetadataService dittNavMetadataService;
//    private final Unleash unleash;
//
//    public DittNavMetadataRessurs(DittNavMetadataService dittNavMetadataService, Unleash unleash) {
//        this.dittNavMetadataService = dittNavMetadataService;
//        this.unleash = unleash;
//    }
//
//    @GET
//    @Path("/pabegynte/aktive")
//    public List<PabegyntSoknadDto> hentPabegynteSoknaderForBruker() {
//        if (!unleash.isEnabled(DITTNAV_PABEGYNTE_ENDEPUNKT_ENABLED, false)) {
//            log.info("Endepunkt for å hente info om påbegynte søknader for dittNav er ikke enabled. Returnerer tom liste.");
//            return Collections.emptyList();
//        }
//        var fnr = SubjectHandler.getUserId();
//        return dittNavMetadataService.hentAktivePabegynteSoknader(fnr);
//    }
//
//    @GET
//    @Path("/pabegynte/inaktive")
//    public List<PabegyntSoknadDto> hentPabegynteSoknaderForBrukerLestDittNav() {
//        if (!unleash.isEnabled(DITTNAV_PABEGYNTE_ENDEPUNKT_ENABLED, false)) {
//            log.info("Endepunkt for å hente info om påbegynte søknader for dittNav er ikke enabled. Returnerer tom liste.");
//            return Collections.emptyList();
//        }
//        var fnr = SubjectHandler.getUserId();
//        return dittNavMetadataService.hentInaktivePabegynteSoknader(fnr);
//    }
//
//    @POST
//    @Path("/pabegynte/lest")
//    public boolean oppdaterLestDittNavForPabegyntSoknad(@RequestBody MarkerPabegyntSoknadSomLestDto dto) {
//        if (!unleash.isEnabled(DITTNAV_PABEGYNTE_ENDEPUNKT_ENABLED, false)) {
//            log.info("Endepunkt for å oppdatere lestDittNav for påbegynt søknad er ikke enabled. Returnerer false.");
//            return false;
//        }
//        var fnr = SubjectHandler.getUserId();
//        var behandlingsId = dto.getGrupperingsId();
//        var somLest = dittNavMetadataService.oppdaterLestDittNavForPabegyntSoknad(behandlingsId, fnr);
//        log.info("Pabegynt søknad med behandlingsId={} har fått lestDittNav={}", behandlingsId, somLest);
//        return somLest;
//    }
//
//}
