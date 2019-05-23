package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.TelefonnummerSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
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
@Path("/soknader/{behandlingsId}/personalia/telefonnummer")
@Timed
@Produces(APPLICATION_JSON)
public class TelefonnummerRessurs {

    @Inject
    private SoknadService soknadService;

    @Inject
    private FaktaService faktaService;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    TelefonnummerSystemdata telefonnummerSystemdata;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;


    @GET
    public TelefonnummerFrontend hentTelefonnummer(@PathParam("behandlingsId") String behandlingsId) {
        String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        telefonnummerSystemdata.updateSystemdataIn(soknadUnderArbeid);
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier);
        JsonTelefonnummer telefonnummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getTelefonnummer();
        String systemverdi;
        if (telefonnummer != null && telefonnummer.getKilde().equals(JsonKilde.SYSTEM)) {
            systemverdi = telefonnummer.getVerdi();
        } else {
            systemverdi = telefonnummerSystemdata.innhentSystemverdiTelefonnummer(eier);
        }

        return new TelefonnummerFrontend()
                .withBrukerdefinert(telefonnummer == null || telefonnummer.getKilde() == JsonKilde.BRUKER)
                .withSystemverdi(systemverdi)
                .withBrukerutfyltVerdi(telefonnummer != null && telefonnummer.getKilde() == JsonKilde.BRUKER ? telefonnummer.getVerdi() : null);
    }

    @PUT
    public void updateTelefonnummer(@PathParam("behandlingsId") String behandlingsId, TelefonnummerFrontend telefonnummerFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        if ("".equals(telefonnummerFrontend.brukerutfyltVerdi)) {
            telefonnummerFrontend.brukerutfyltVerdi = null;
        }
        update(behandlingsId, telefonnummerFrontend);
        legacyUpdate(behandlingsId, telefonnummerFrontend);
    }

    private void update(String behandlingsId, TelefonnummerFrontend telefonnummerFrontend) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonPersonalia personalia = soknad.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        final JsonTelefonnummer jsonTelefonnummer = personalia.getTelefonnummer() != null ? personalia.getTelefonnummer() :
                personalia.withTelefonnummer(new JsonTelefonnummer()).getTelefonnummer();
        if (telefonnummerFrontend.brukerdefinert) {
            if (telefonnummerFrontend.brukerutfyltVerdi == null) {
                personalia.setTelefonnummer(null);
            } else {
                jsonTelefonnummer.setKilde(JsonKilde.BRUKER);
                jsonTelefonnummer.setVerdi(telefonnummerFrontend.brukerutfyltVerdi);
            }
        } else {
            jsonTelefonnummer.setKilde(JsonKilde.SYSTEM);
            telefonnummerSystemdata.updateSystemdataIn(soknad);
        }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, TelefonnummerFrontend telefonnummerFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        final Faktum brukerdefinert = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "kontakt.telefon.brukerendrettoggle");
        brukerdefinert.setValue(Boolean.toString(telefonnummerFrontend.brukerdefinert));

        faktaService.lagreBrukerFaktum(brukerdefinert);
        final Faktum telefon = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "kontakt.telefon");
        if (telefonnummerFrontend.brukerutfyltVerdi != null){
            telefon.setValue(telefonnummerFrontend.brukerutfyltVerdi.substring(3));
            faktaService.lagreBrukerFaktum(telefon);
        } else {
            telefon.setValue(null);
            faktaService.lagreBrukerFaktum(telefon);
        }
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class TelefonnummerFrontend {
        public boolean brukerdefinert;
        public String systemverdi;
        public String brukerutfyltVerdi;

        public TelefonnummerFrontend withBrukerdefinert(boolean brukerdefinert) {
            this.brukerdefinert = brukerdefinert;
            return this;
        }

        public TelefonnummerFrontend withSystemverdi(String systemverdi) {
            this.systemverdi = systemverdi;
            return this;
        }

        public TelefonnummerFrontend withBrukerutfyltVerdi(String brukerutfyltVerdi) {
            this.brukerutfyltVerdi = brukerutfyltVerdi;
            return this;
        }
    }
}
