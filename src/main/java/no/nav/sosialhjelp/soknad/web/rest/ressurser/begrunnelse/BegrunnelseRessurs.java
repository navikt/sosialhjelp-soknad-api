package no.nav.sosialhjelp.soknad.web.rest.ressurser.begrunnelse;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
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
@Path("/soknader/{behandlingsId}/begrunnelse")
@Timed
@Produces(APPLICATION_JSON)
public class BegrunnelseRessurs {

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;


    @GET
    public BegrunnelseFrontend hentBegrunnelse(@PathParam("behandlingsId") String behandlingsId) {
        String eier = SubjectHandler.getUserId();
        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        JsonBegrunnelse begrunnelse = soknad.getSoknad().getData().getBegrunnelse();

        return new BegrunnelseFrontend()
                .withHvaSokesOm(begrunnelse.getHvaSokesOm())
                .withHvorforSoke(begrunnelse.getHvorforSoke());
    }

    @PUT
    public void updateBegrunnelse(@PathParam("behandlingsId") String behandlingsId, BegrunnelseFrontend begrunnelseFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = SubjectHandler.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonBegrunnelse begrunnelse = soknad.getJsonInternalSoknad().getSoknad().getData().getBegrunnelse();
        begrunnelse.setKilde(JsonKildeBruker.BRUKER);
        begrunnelse.setHvaSokesOm(begrunnelseFrontend.hvaSokesOm);
        begrunnelse.setHvorforSoke(begrunnelseFrontend.hvorforSoke);
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class BegrunnelseFrontend {
        public String hvaSokesOm;
        public String hvorforSoke;

        public BegrunnelseFrontend withHvaSokesOm(String hvaSokesOm) {
            this.hvaSokesOm = hvaSokesOm;
            return this;
        }

        public BegrunnelseFrontend withHvorforSoke(String hvorforSoke) {
            this.hvorforSoke = hvorforSoke;
            return this;
        }
    }
}
