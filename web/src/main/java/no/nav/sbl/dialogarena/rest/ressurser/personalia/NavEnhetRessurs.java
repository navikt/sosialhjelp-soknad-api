package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/soknader/{behandlingsId}/personalia/navEnhet")
@Timed
@Produces(APPLICATION_JSON)
public class NavEnhetRessurs {

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
    public NavEnhetFrontend hentNavEnhet(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        final JsonSoknadsmottaker jsonSoknadsmottaker = soknad.getMottaker();

        final String kombinertnavn = jsonSoknadsmottaker.getNavEnhetsnavn();
        final String enhetsnavn = kombinertnavn.substring(0, kombinertnavn.indexOf(','));
        final String kommunenavn = kombinertnavn.substring(kombinertnavn.indexOf(',') + 2);

        return new NavEnhetFrontend()
                .withOrgnr(jsonSoknadsmottaker.getOrganisasjonsnummer())
                .withEnhetsnavn(enhetsnavn)
                .withKommunenavn(kommunenavn);
    }

    @PUT
    public void updateAdresse(@PathParam("behandlingsId") String behandlingsId, NavEnhetFrontend navEnhetFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, navEnhetFrontend);
        legacyUpdate(behandlingsId, navEnhetFrontend);
    }

    private void update(String behandlingsId, NavEnhetFrontend navEnhetFrontend) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonInternalSoknad jsonInternalSoknad = soknad.getJsonInternalSoknad();

        jsonInternalSoknad.setMottaker(new JsonSoknadsmottaker()
                .withNavEnhetsnavn(navEnhetFrontend.enhetsnavn + ", " + navEnhetFrontend.kommunenavn)
                .withOrganisasjonsnummer(navEnhetFrontend.orgnr));
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, NavEnhetFrontend navEnhetFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        final Faktum soknadsmottaker = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "soknadsmottaker");

        final Map<String, String> properties = soknadsmottaker.getProperties();
        properties.put("sosialOrgnr", navEnhetFrontend.orgnr);
        properties.put("enhetsnavn", navEnhetFrontend.enhetsnavn);
        properties.put("kommunenavn", navEnhetFrontend.kommunenavn);

        faktaService.lagreBrukerFaktum(soknadsmottaker);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class NavEnhetFrontend {
        public String orgnr;
        public String enhetsnavn;
        public String kommunenavn;

        NavEnhetFrontend withOrgnr(String orgnr) {
            this.orgnr = orgnr;
            return this;
        }

        NavEnhetFrontend withEnhetsnavn(String enhetsnavn) {
            this.enhetsnavn = enhetsnavn;
            return this;
        }

        public NavEnhetFrontend withKommunenavn(String kommunenavn) {
            this.kommunenavn = kommunenavn;
            return this;
        }
    }
}
