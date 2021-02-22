package no.nav.sosialhjelp.soknad.web.rest.ressurser.soknadoversikt;

import com.fasterxml.jackson.annotation.JsonFormat;
import no.nav.metrics.aspects.Timed;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.service.SoknadOversiktService;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Date;
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
        String fnr = SubjectHandler.getUserId();
        logger.debug("Henter alle søknader");

        List<SoknadOversikt> soknader = service.hentSvarUtSoknaderFor(fnr);
        logger.debug("Hentet {} søknader for bruker", soknader.size());

        return soknader;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class SoknadOversikt {
        private String fiksDigisosId;
        private String soknadTittel;
        private String status;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        private Date sistOppdatert;
        private Integer antallNyeOppgaver;
        private String kilde;
        private String url;

        public String getFiksDigisosId() {
            return fiksDigisosId;
        }

        public String getSoknadTittel() {
            return soknadTittel;
        }

        public String getStatus() {
            return status;
        }

        public Date getSistOppdatert() {
            return sistOppdatert;
        }

        public Integer getAntallNyeOppgaver() {
            return antallNyeOppgaver;
        }

        public String getKilde() {
            return kilde;
        }

        public String getUrl() {
            return url;
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

        public SoknadOversikt withSistOppdatert(Date sistOppdatert) {
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

        public SoknadOversikt withUrl(String url) {
            this.url = url;
            return this;
        }
    }
}