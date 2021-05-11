package no.nav.sosialhjelp.soknad.web.rest.ressurser.bosituasjon;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.sosialhjelp.metrics.aspects.Timed;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;

@Controller
@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
@Path("/soknader/{behandlingsId}/bosituasjon")
@Timed
@Produces(APPLICATION_JSON)
public class BosituasjonRessurs {

    private final Tilgangskontroll tilgangskontroll;
    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    public BosituasjonRessurs(Tilgangskontroll tilgangskontroll, SoknadUnderArbeidRepository soknadUnderArbeidRepository) {
        this.tilgangskontroll = tilgangskontroll;
        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
    }

    @GET
    public BosituasjonFrontend hentBosituasjon(@PathParam("behandlingsId") String behandlingsId) {
        tilgangskontroll.verifiserAtBrukerHarTilgang();
        String eier = SubjectHandler.getUserId();
        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        JsonBosituasjon bosituasjon = soknad.getSoknad().getData().getBosituasjon();

        return new BosituasjonFrontend()
                .withBotype(bosituasjon.getBotype())
                .withAntallPersoner(bosituasjon.getAntallPersoner());
    }

    @PUT
    public void updateBosituasjon(@PathParam("behandlingsId") String behandlingsId, BosituasjonFrontend bosituasjonFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = SubjectHandler.getUserId();
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
