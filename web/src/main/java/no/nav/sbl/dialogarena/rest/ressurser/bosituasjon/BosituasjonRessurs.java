package no.nav.sbl.dialogarena.rest.ressurser.bosituasjon;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandlerWrapper;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/bosituasjon")
@Timed
@Produces(APPLICATION_JSON)
public class BosituasjonRessurs {

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private SubjectHandlerWrapper subjectHandlerWrapper;

    @GET
    public BosituasjonFrontend hentBosituasjon(@PathParam("behandlingsId") String behandlingsId) {
        String eier = subjectHandlerWrapper.getIdent();
        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        JsonBosituasjon bosituasjon = soknad.getSoknad().getData().getBosituasjon();

        return new BosituasjonFrontend()
                .withBotype(bosituasjon.getBotype())
                .withAntallPersoner(bosituasjon.getAntallPersoner());
    }

    @PUT
    public void updateBosituasjon(@PathParam("behandlingsId") String behandlingsId, BosituasjonFrontend bosituasjonFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = subjectHandlerWrapper.getIdent();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonBosituasjon bosituasjon = soknad.getJsonInternalSoknad().getSoknad().getData().getBosituasjon();
        bosituasjon.setKilde(JsonKildeBruker.BRUKER);
        if (bosituasjonFrontend.botype != null) {
            bosituasjon.setBotype(bosituasjonFrontend.botype);
        }
        bosituasjon.setAntallPersoner(bosituasjonFrontend.antallPersoner);
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class BosituasjonFrontend {
        public JsonBosituasjon.Botype botype;
        public Integer antallPersoner;

        public BosituasjonFrontend withBotype(JsonBosituasjon.Botype botype) {
            this.botype = botype;
            return this;
        }

        public BosituasjonFrontend withAntallPersoner(Integer antallPersoner) {
            this.antallPersoner = antallPersoner;
            return this;
        }
    }
}
