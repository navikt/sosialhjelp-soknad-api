package no.nav.sbl.dialogarena.rest.ressurser.bosituasjon;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
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
    private SoknadService soknadService;

    @Inject
    private FaktaService faktaService;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private LegacyHelper legacyHelper;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @GET
    public BosituasjonFrontend hentBosituasjon(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier, false).getJsonInternalSoknad();
        final JsonBosituasjon bosituasjon = soknad.getSoknad().getData().getBosituasjon();

        return new BosituasjonFrontend()
                .withBotype(bosituasjon.getBotype())
                .withAntallPersoner(bosituasjon.getAntallPersoner());
    }

    @PUT
    public void updateBosituasjon(@PathParam("behandlingsId") String behandlingsId, BosituasjonFrontend bosituasjonFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, bosituasjonFrontend);
        legacyUpdate(behandlingsId, bosituasjonFrontend);
    }

    private void update(String behandlingsId, BosituasjonFrontend bosituasjonFrontend) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonBosituasjon bosituasjon = soknad.getJsonInternalSoknad().getSoknad().getData().getBosituasjon();
        bosituasjon.setKilde(JsonKildeBruker.BRUKER);
        if (bosituasjonFrontend.botype != null) {
            bosituasjon.setBotype(bosituasjonFrontend.botype);
        }
        bosituasjon.setAntallPersoner(bosituasjonFrontend.antallPersoner);
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, BosituasjonFrontend bosituasjonFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        if (bosituasjonFrontend.botype != null) {
            final Faktum bosituasjon = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "bosituasjon");
            final Faktum annenBosituasjon = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "bosituasjon.annet.botype");

            if (isAnnenBotype(bosituasjonFrontend.botype)) {
                bosituasjon.setType(Faktum.FaktumType.BRUKERREGISTRERT);
                bosituasjon.setValue("annet");

                annenBosituasjon.setType(Faktum.FaktumType.BRUKERREGISTRERT);
                annenBosituasjon.setValue(bosituasjonFrontend.botype.toString());
            } else {
                annenBosituasjon.setType(Faktum.FaktumType.BRUKERREGISTRERT);
                annenBosituasjon.setValue(null);
                bosituasjon.setType(Faktum.FaktumType.BRUKERREGISTRERT);
                bosituasjon.setValue(bosituasjonFrontend.botype.toString());
            }

            faktaService.lagreBrukerFaktum(bosituasjon);
            faktaService.lagreBrukerFaktum(annenBosituasjon);
        }

        final Faktum antallPersoner = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "bosituasjon.antallpersoner");
        antallPersoner.setType(Faktum.FaktumType.BRUKERREGISTRERT);
        antallPersoner.setValue(bosituasjonFrontend.antallPersoner == null ? null : bosituasjonFrontend.antallPersoner.toString());

        faktaService.lagreBrukerFaktum(antallPersoner);
    }

    private boolean isAnnenBotype(JsonBosituasjon.Botype botype){
        switch (botype) {
            case INSTITUSJON:
            case KRISESENTER:
            case FENGSEL:
            case VENNER:
            case FORELDRE:
            case FAMILIE:
                return true;
            default:
                return false;
        }
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
