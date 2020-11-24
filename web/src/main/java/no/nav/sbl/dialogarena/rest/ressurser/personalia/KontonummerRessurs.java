package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.KontonummerSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
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
@Path("/soknader/{behandlingsId}/personalia/kontonummer")
@Timed
@Produces(APPLICATION_JSON)
public class KontonummerRessurs {

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private KontonummerSystemdata kontonummerSystemdata;

    @GET
    public KontonummerFrontend hentKontonummer(@PathParam("behandlingsId") String behandlingsId) {
        String eier = SubjectHandler.getUserId();
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonKontonummer kontonummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getKontonummer();
        String systemverdi;
        if (kontonummer.getKilde().equals(JsonKilde.SYSTEM)) {
            systemverdi = kontonummer.getVerdi();
        } else {
            systemverdi = kontonummerSystemdata.innhentSystemverdiKontonummer(eier);
        }

        return new KontonummerFrontend()
                .withBrukerdefinert(kontonummer.getKilde() == JsonKilde.BRUKER)
                .withSystemverdi(systemverdi)
                .withBrukerutfyltVerdi(kontonummer.getKilde() == JsonKilde.BRUKER ? kontonummer.getVerdi() : null)
                .withHarIkkeKonto(kontonummer.getHarIkkeKonto());
    }

    @PUT
    public void updateKontonummer(@PathParam("behandlingsId") String behandlingsId, KontonummerFrontend kontonummerFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = SubjectHandler.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonPersonalia personalia = soknad.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        JsonKontonummer kontonummer = personalia.getKontonummer();
        if (kontonummerFrontend.brukerdefinert) {
            kontonummer.setKilde(JsonKilde.BRUKER);
            if ("".equals(kontonummerFrontend.brukerutfyltVerdi)) {
                kontonummerFrontend.brukerutfyltVerdi = null;
            }
            kontonummer.setVerdi(kontonummerFrontend.brukerutfyltVerdi);
            kontonummer.setHarIkkeKonto(kontonummerFrontend.harIkkeKonto);
        } else if (kontonummer.getKilde() == JsonKilde.BRUKER) {
            kontonummer.setKilde(JsonKilde.SYSTEM);
            kontonummerSystemdata.updateSystemdataIn(soknad, "");
            kontonummer.setHarIkkeKonto(null);
        }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
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
