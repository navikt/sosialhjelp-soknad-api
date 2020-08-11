package no.nav.sbl.dialogarena.rest.ressurser.eksponerte;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.server.SamlSubjectHandler;
import no.nav.sbl.dialogarena.service.SaksoversiktMetadataService;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.EttersendingerRespons;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.EttersendingsSoknad;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.InnsendtSoknad;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.InnsendteSoknaderRespons;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.PabegyntSoknad;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.PabegynteSoknaderRespons;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.PingRespons;
import no.nav.security.token.support.core.api.Unprotected;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Eksponerer metadata om brukers søknader for bruk i Saksoversikt
 * Implementerer speccen definert i soeknadsskjemaSosialhjelp-v1-saksoversiktdefinisjon
 */
@Controller
@Unprotected // Er sikret med SAML frem til team personbruker er klare med å bytte ut SAML med OIDC
@Path("/metadata")
@Timed
@Produces(APPLICATION_JSON)
public class SaksoversiktMetadataRessurs {

    private static final Logger logger = getLogger(SaksoversiktMetadataRessurs.class);

    @Inject
    private SaksoversiktMetadataService saksoversiktMetadataService;

    @GET
    @Path("/innsendte")
    public InnsendteSoknaderRespons hentInnsendteSoknaderForBruker() {
        String fnr = SamlSubjectHandler.getUid();
        logger.info("DEBUG - fnr fra ny SAMLSubjecthandler er: " + fnr);
        fnr = SubjectHandler.getSubjectHandler().getUid();
        logger.info("DEBUG - fnr fra modig-Subjecthandler er: " + fnr);



        logger.info("Henter metadata for innsendte soknader uten oidc");

        List<InnsendtSoknad> innsendteSoknader = saksoversiktMetadataService.hentInnsendteSoknaderForFnr(fnr);

        return new InnsendteSoknaderRespons()
                .withInnsendteSoknader(innsendteSoknader);
    }

    @GET
    @Path("/ettersendelse")
    public EttersendingerRespons hentSoknaderBrukerKanEttersendePa() {
        String fnr = SamlSubjectHandler.getUid();
        logger.info("DEBUG - fnr fra ny SAMLSubjecthandler er: " + fnr);
        fnr = SubjectHandler.getSubjectHandler().getUid();
        logger.info("DEBUG - fnr fra modig-Subjecthandler er: " + fnr);


        logger.info("Henter metadata for ettersendelse uten oidc");

        List<EttersendingsSoknad> ettersendingsSoknader = saksoversiktMetadataService.hentSoknaderBrukerKanEttersendePa(fnr);

        return new EttersendingerRespons()
                .withEttersendingsSoknader(ettersendingsSoknader);
    }

    @GET
    @Path("/pabegynte")
    public PabegynteSoknaderRespons hentPabegynteSoknaderForBruker() {
        String fnr = SamlSubjectHandler.getUid();
        logger.info("DEBUG - fnr fra ny SAMLSubjecthandler er: " + fnr);
//        Optional<String> ident = no.nav.common.auth.SubjectHandler.getIdent();
//        logger.info("DEBUG - fnr fra common-java-module subjecthandler er: " + ident);
        fnr = SubjectHandler.getSubjectHandler().getUid();
        logger.info("DEBUG - fnr fra modig-Subjecthandler er: " + fnr);

        logger.info("Henter metadata for pabegynte uten oidc");

        List<PabegyntSoknad> pabegynte = saksoversiktMetadataService.hentPabegynteSoknaderForBruker(fnr);

        return new PabegynteSoknaderRespons()
                .withPabegynteSoknader(pabegynte);
    }

    @GET
    @Path("/ping")
    public PingRespons ping() {
        logger.debug("Ping for saksoversikt uten oidc");
        return new PingRespons()
                .withStatus(PingRespons.Status.OK)
                .withMelding("Sosialhjelp Saksoversikt API er oppe");
    }
}
