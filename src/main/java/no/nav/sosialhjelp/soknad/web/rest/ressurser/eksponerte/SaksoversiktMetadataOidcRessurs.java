//package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte;
//
//import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.EttersendingerRespons;
//import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.EttersendingsSoknad;
//import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.InnsendtSoknad;
//import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.InnsendteSoknaderRespons;
//import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.PabegyntSoknad;
//import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.PabegynteSoknaderRespons;
//import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.PingRespons;
//import no.nav.security.token.support.core.api.ProtectedWithClaims;
//import no.nav.security.token.support.core.api.Unprotected;
//import no.nav.sosialhjelp.metrics.aspects.Timed;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
//import no.nav.sosialhjelp.soknad.web.service.SaksoversiktMetadataService;
//import org.slf4j.Logger;
//import org.springframework.stereotype.Controller;
//
//import javax.ws.rs.GET;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import java.util.List;
//
//import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_3;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
//import static org.slf4j.LoggerFactory.getLogger;
//
///**
// * Eksponerer metadata om brukers søknader for bruk i Saksoversikt
// * Implementerer speccen definert i soeknadsskjemasosialhjelp-v1-saksoversiktdefinisjon
// */
//@Controller
//@ProtectedWithClaims(issuer = SELVBETJENING, combineWithOr = true, claimMap = {CLAIM_ACR_LEVEL_3, CLAIM_ACR_LEVEL_4})
//@Path("/metadata/oidc")
//@Timed
//@Produces(APPLICATION_JSON)
//public class SaksoversiktMetadataOidcRessurs {
//
//    private static final Logger logger = getLogger(SaksoversiktMetadataOidcRessurs.class);
//
//    private final SaksoversiktMetadataService saksoversiktMetadataService;
//
//    public SaksoversiktMetadataOidcRessurs(SaksoversiktMetadataService saksoversiktMetadataService) {
//        this.saksoversiktMetadataService = saksoversiktMetadataService;
//    }
//
//    @GET
//    @Path("/innsendte")
//    public InnsendteSoknaderRespons hentInnsendteSoknaderForBruker() {
//        String fnr = SubjectHandler.getUserId();
//        logger.debug("Henter metadata for innsendte soknader med oidc");
//
//        List<InnsendtSoknad> innsendteSoknader = saksoversiktMetadataService.hentInnsendteSoknaderForFnr(fnr);
//
//        return new InnsendteSoknaderRespons()
//                .withInnsendteSoknader(innsendteSoknader);
//    }
//
//    @GET
//    @Path("/ettersendelse")
//    public EttersendingerRespons hentSoknaderBrukerKanEttersendePa() {
//        String fnr = SubjectHandler.getUserId();
//        logger.debug("Henter metadata for ettersendelse med oidc");
//
//        List<EttersendingsSoknad> ettersendingsSoknader = saksoversiktMetadataService.hentSoknaderBrukerKanEttersendePa(fnr);
//
//        return new EttersendingerRespons()
//                .withEttersendingsSoknader(ettersendingsSoknader);
//    }
//
//    @GET
//    @Path("/pabegynte")
//    public PabegynteSoknaderRespons hentPabegynteSoknaderForBruker() {
//        String fnr = SubjectHandler.getUserId();
//        logger.debug("Henter metadata for pabegynte med oidc");
//
//        try {
//            List<PabegyntSoknad> pabegynte = saksoversiktMetadataService.hentPabegynteSoknaderForBruker(fnr);
//
//            return new PabegynteSoknaderRespons()
//                    .withPabegynteSoknader(pabegynte);
//        } catch (Exception e) {
//            logger.error("Uthenting av påbegynte søknader feilet. Var fnr tom? {}", fnr == null || fnr.equals(""), e) ;
//            throw e;
//        }
//    }
//
//    @GET
//    @Unprotected
//    @Path("/ping")
//    public PingRespons ping() {
//        logger.debug("Ping for saksoversikt med oidc");
//        return new PingRespons()
//                .withStatus(PingRespons.Status.OK)
//                .withMelding("Sosialhjelp Saksoversikt API er oppe");
//    }
//}
