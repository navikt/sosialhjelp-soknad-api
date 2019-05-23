package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.KontonummerSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
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
@Path("/soknader/{behandlingsId}/personalia/kontonummer")
@Timed
@Produces(APPLICATION_JSON)
public class KontonummerRessurs {

    @Inject
    private SoknadService soknadService;

    @Inject
    private FaktaService faktaService;

    @Inject
    private LegacyHelper legacyHelper;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private KontonummerSystemdata kontonummerSystemdata;


    @GET
    public KontonummerFrontend hentKontonummer(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier, false).getJsonInternalSoknad();
        final JsonKontonummer kontonummer = soknad.getSoknad().getData().getPersonalia().getKontonummer();
        final String systemverdi = kontonummerSystemdata.innhentSystemverdiKontonummer(eier);

        return new KontonummerFrontend()
                .withBrukerdefinert(kontonummer.getKilde() == JsonKilde.BRUKER)
                .withSystemverdi(systemverdi)
                .withBrukerutfyltVerdi(kontonummer.getKilde() == JsonKilde.BRUKER ? kontonummer.getVerdi() : null)
                .withHarIkkeKonto(kontonummer.getHarIkkeKonto());
    }

    @PUT
    public void updateKontonummer(@PathParam("behandlingsId") String behandlingsId, KontonummerFrontend kontonummerFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, kontonummerFrontend);
        legacyUpdate(behandlingsId, kontonummerFrontend);
    }


    private void update(String behandlingsId, KontonummerFrontend kontonummerFrontend) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonPersonalia personalia = soknad.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        final JsonKontonummer kontonummer = personalia.getKontonummer();
        if (kontonummerFrontend.brukerdefinert) {
            kontonummer.setKilde(JsonKilde.BRUKER);
            if ("".equals(kontonummerFrontend.brukerutfyltVerdi)) {
                kontonummerFrontend.brukerutfyltVerdi = null;
            }
            kontonummer.setVerdi(kontonummerFrontend.brukerutfyltVerdi);
            kontonummer.setHarIkkeKonto(kontonummerFrontend.harIkkeKonto);
        } else if (kontonummer.getKilde() == JsonKilde.BRUKER) {
            kontonummer.setKilde(JsonKilde.SYSTEM);
            kontonummer.setVerdi(kontonummerFrontend.systemverdi);
            kontonummer.setHarIkkeKonto(null);
        }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, KontonummerFrontend kontonummerFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        final Faktum brukerdefinert = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "kontakt.kontonummer.brukerendrettoggle");
        brukerdefinert.setValue(Boolean.toString(kontonummerFrontend.brukerdefinert));
        faktaService.lagreBrukerFaktum(brukerdefinert);

        final Faktum kontonummer = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "kontakt.kontonummer");
        kontonummer.setValue(kontonummerFrontend.brukerutfyltVerdi);
        faktaService.lagreBrukerFaktum(kontonummer);

        final Faktum harIkke = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "kontakt.kontonummer.harikke");
        harIkke.setValue(booleanToString(kontonummerFrontend.harIkkeKonto));
        faktaService.lagreBrukerFaktum(harIkke);
    }


    private String booleanToString(Boolean b) {
        if (b == null) {
            return null;
        }
        return b.toString();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class KontonummerFrontend {
        public boolean brukerdefinert;
        public String systemverdi;
        public String brukerutfyltVerdi;
        public Boolean harIkkeKonto;

        public KontonummerFrontend withBrukerdefinert(boolean brukerdefinert) {
            this.brukerdefinert = brukerdefinert;
            return this;
        }

        public KontonummerFrontend withSystemverdi(String systemverdi) {
            this.systemverdi = systemverdi;
            return this;
        }

        public KontonummerFrontend withBrukerutfyltVerdi(String brukerutfyltVerdi) {
            this.brukerutfyltVerdi = brukerutfyltVerdi;
            return this;
        }

        public KontonummerFrontend withHarIkkeKonto(Boolean harIkkeKonto) {
            this.harIkkeKonto = harIkkeKonto;
            return this;
        }
    }
}
