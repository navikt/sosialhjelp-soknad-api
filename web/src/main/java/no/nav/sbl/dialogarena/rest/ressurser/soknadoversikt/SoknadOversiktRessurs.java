package no.nav.sbl.dialogarena.rest.ressurser.soknadoversikt;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.service.SoknadOversiktService;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.time.LocalDateTime;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.slf4j.LoggerFactory.getLogger;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = {"acr=Level4"})
@Path("/soknadoversikt")
@Timed
@Produces(APPLICATION_JSON)
public class SoknadOversiktRessurs {

    private static final Logger logger = getLogger(SoknadOversiktRessurs.class);

    @Inject
    private SoknadOversiktService service;

    @GET
    @Path("/soknader")
    public List<SoknadOversikt> hentInnsendteSoknaderForBruker() {
        String fnr = SubjectHandler.getUserIdFromToken();
        logger.debug("Henter alle søknader for fnr {}", fnr);

        List<SoknadOversikt> soknader = service.hentAlleSoknaderFor(fnr);
        logger.debug("Hentet {} søknader for bruker", soknader.size());

        return soknader;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class SoknadOversikt {
        private String fiksDigisosId;
        private String soknadTittel;
        private String status;
        private LocalDateTime sistOppdatert;
        private Integer antallNyeOppgaver;
        private String kilde;

        public String getFiksDigisosId() {
            return fiksDigisosId;
        }

        public String getSoknadTittel() {
            return soknadTittel;
        }

        public String getStatus() {
            return status;
        }

        public LocalDateTime getSistOppdatert() {
            return sistOppdatert;
        }

        public Integer getAntallNyeOppgaver() {
            return antallNyeOppgaver;
        }

        public String getKilde() {
            return kilde;
        }

        public SoknadOversikt withFiksDigisosId(String fiksDigisosId) {
            this.fiksDigisosId = fiksDigisosId;
            return this;
        }

        public SoknadOversikt withSoknadTittel(String soknadTittel) {
            this.soknadTittel = soknadTittel;
            return this;
        }

        public SoknadOversikt withStatus(String status) {
            this.status = status;
            return this;
        }

        public SoknadOversikt withSistOppdatert(LocalDateTime sistOppdatert) {
            this.sistOppdatert = sistOppdatert;
            return this;
        }

        public SoknadOversikt withAntallNyeOppgaver(Integer antallNyeOppgaver) {
            this.antallNyeOppgaver = antallNyeOppgaver;
            return this;
        }

        public SoknadOversikt withKilde(String kilde) {
            this.kilde = kilde;
            return this;
        }
    }
}