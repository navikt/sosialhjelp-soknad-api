package no.nav.sbl.dialogarena.rest.ressurser.utdanning;

import no.nav.metrics.aspects.Timed;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
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
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.STUDIELAN;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/utdanning")
@Timed
@Produces(APPLICATION_JSON)
public class UtdanningRessurs {

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;


    @GET
    public UtdanningFrontend hentUtdanning(@PathParam("behandlingsId") String behandlingsId) {
        String eier = SubjectHandler.getUserId();
        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        JsonUtdanning utdanning = soknad.getSoknad().getData().getUtdanning();

        return new UtdanningFrontend()
                .withErStudent(utdanning.getErStudent())
                .withStudengradErHeltid(toStudentgradErHeltid(utdanning.getStudentgrad()));
    }

    @PUT
    public void updateUtdanning(@PathParam("behandlingsId") String behandlingsId, UtdanningFrontend utdanningFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = SubjectHandler.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonUtdanning utdanning = soknad.getJsonInternalSoknad().getSoknad().getData().getUtdanning();
        List<JsonOkonomioversiktInntekt> inntekter = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().getInntekt();
        utdanning.setKilde(JsonKilde.BRUKER);
        utdanning.setErStudent(utdanningFrontend.erStudent);

        if (utdanningFrontend.erStudent){
            utdanning.setStudentgrad(toStudentgrad(utdanningFrontend.studengradErHeltid));
        } else {
            utdanning.setStudentgrad(null);
            JsonOkonomiopplysninger opplysninger = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
            if (opplysninger.getBekreftelse() != null) {
                opplysninger.getBekreftelse().removeIf(bekreftelse -> bekreftelse.getType().equals(STUDIELAN));
                inntekter.removeIf(inntekt -> inntekt.getType().equals(STUDIELAN));
            }
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
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
