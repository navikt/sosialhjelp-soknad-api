package no.nav.sbl.dialogarena.rest.ressurser.utdanning;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.FaktumNoklerOgBelopNavnMapper.soknadTypeToFaktumKey;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.addInntektIfCheckedElseDeleteInOversikt;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/utdanning")
@Timed
@Produces(APPLICATION_JSON)
public class UtdanningRessurs {

    @Inject
    private SoknadService soknadService;

    @Inject
    private FaktaService faktaService;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private LegacyHelper legacyHelper;

    @Inject
    private TextService textService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;


    @GET
    public UtdanningFrontend hentUtdanning(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier, false).getJsonInternalSoknad();
        final JsonUtdanning utdanning = soknad.getSoknad().getData().getUtdanning();

        return new UtdanningFrontend()
                .withErStudent(utdanning.getErStudent())
                .withStudengradErHeltid(toStudentgradErHeltid(utdanning.getStudentgrad()));
    }

    @PUT
    public void updateUtdanning(@PathParam("behandlingsId") String behandlingsId, UtdanningFrontend utdanningFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, utdanningFrontend);
        legacyUpdate(behandlingsId, utdanningFrontend);
    }

    private void update(String behandlingsId, UtdanningFrontend utdanningFrontend) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonUtdanning utdanning = soknad.getJsonInternalSoknad().getSoknad().getData().getUtdanning();
        final List<JsonOkonomioversiktInntekt> inntekter = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().getInntekt();
        utdanning.setKilde(JsonKilde.BRUKER);
        utdanning.setErStudent(utdanningFrontend.erStudent);
        if (utdanningFrontend.erStudent){
            utdanning.setStudentgrad(toStudentgrad(utdanningFrontend.studengradErHeltid));
        } else {
            utdanning.setStudentgrad(null);
        }

        if (utdanning.getErStudent() != null){
            String soknadstype = "studielanOgStipend";
            String tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(soknadstype));
            addInntektIfCheckedElseDeleteInOversikt(inntekter, soknadstype, tittel, utdanning.getErStudent());
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, UtdanningFrontend utdanningFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        final Faktum studerer = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "dinsituasjon.studerer");
        studerer.setValue(Boolean.toString(utdanningFrontend.erStudent));
        faktaService.lagreBrukerFaktum(studerer);

        final Faktum studentgrad = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "dinsituasjon.studerer.true.grad");
        studentgrad.setValue(tilFaktumStudentgrad(utdanningFrontend.studengradErHeltid));
        faktaService.lagreBrukerFaktum(studentgrad);
    }

    private static Boolean toStudentgradErHeltid(JsonUtdanning.Studentgrad studentgrad) {
        if (studentgrad == null) {
            return null;
        }
        return studentgrad == JsonUtdanning.Studentgrad.HELTID;
    }

    private static JsonUtdanning.Studentgrad toStudentgrad(Boolean studentgrad) {
        if (studentgrad == null) {
            return null;
        }
        return studentgrad ? JsonUtdanning.Studentgrad.HELTID : JsonUtdanning.Studentgrad.DELTID;
    }

    private static String tilFaktumStudentgrad(Boolean studentgrad) {
        if (studentgrad == null) {
            return null;
        }
        return studentgrad ? "heltid" : "deltid";
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class UtdanningFrontend {
        public Boolean erStudent;
        public Boolean studengradErHeltid;

        public UtdanningFrontend withErStudent(Boolean erStudent) {
            this.erStudent = erStudent;
            return this;
        }

        public UtdanningFrontend withStudengradErHeltid(Boolean studengradErHeltid) {
            this.studengradErHeltid = studengradErHeltid;
            return this;
        }
    }
}
